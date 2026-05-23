# SESSION SUMMARY – Log công việc theo ngày

Tài liệu này ghi lại tất cả các thay đổi và tiến trình trong các phiên làm việc, tổ chức theo ngày. Mỗi ngày là một mục riêng, giúp bạn nắm bắt nhanh công việc đã làm và tiếp tục dễ dàng.

---

## 2026-05-22

### 1. Chuẩn hóa Response API Wrapper
- **File mới:** `src/main/java/com/duoq/medlearn/dto/response/ApiResponse.java`
- **Thay đổi:**
  - `GlobalExceptionHandler` → trả về `ApiResponse.error(...)`.
  - `AuthController` → trả về `ApiResponse.success(...)`.
- **Mục đích:** Đồng nhất cấu trúc JSON trả về cho client.

### 2. Triển khai User Service đầy đủ
- **File chính:** `src/main/java/com/duoq/medlearn/service/impl/UserServiceImpl.java`
- **Các method đã implement:**
  - `getUserById`, `getCurrentUser`, `getAllUsers`, `updateProfile`, `deactivateUser`, `activateUser`, `assignRole`, `removeRole`.
- **Validation & bảo mật:**
  - `UpdateProfileRequest` có đầy đủ trường (username, email, fullName, avatarUrl, phoneNumber) + validation.
  - Đổi email → yêu cầu verify lại, tạo token mới, gửi email.
  - Check duplicate username/email.
- **Transaction & log:** Các operation nhạy cảm có `@Transactional` và log.

### 3. Refactor Realtime Communication (SSE) – Port/Adapter
- **Contract (port):**
  - `RealtimePublisher` (interface) – `publishToUser(channel, userId, eventName, payload)`.
  - `RealtimeChannels` (constants) – `NOTIFICATION`, `AI_STREAM`.
- **Adapter (SSE):**
  - `RealtimeNotificationServiceImpl` implement cả `RealtimeNotificationService` và `RealtimePublisher`.
  - Quản lý emitter theo channel/userId.
  - Timeout 30 phút, tự động remove emitter khi lỗi/hoàn thành.
- **Controller:**
  - `NotificationController` – `/api/notifications/stream`.
  - `AiStreamController` – `/api/ai/stream` (scaffold).
- **DTO:**
  - `NotificationEventResponse` (type, title, message, createdAt).
  - `AiStreamChunkResponse` (chunk, done, createdAt).
- **Tích hợp business:** `UserServiceImpl` gọi `realtimePublisher.publishToUser(...)` khi admin deactivate/activate/assign/remove role.
- **Security:** Endpoint SSE được bảo vệ bởi `SecurityConfig` (authenticated).
- **Hướng dẫn frontend:** Tạo file `SSE_FRONTEND.md`.

### 4. SecurityConfig cập nhật
- Thêm `@EnableMethodSecurity` để `@PreAuthorize` hoạt động.
- Thêm rule `/api/admin/**` → `hasRole("ADMIN")`.

### 5. Các file mới/đã sửa (tóm tắt)
- `dto/request/UpdateProfileRequest.java`
- `dto/request/RoleRequest.java`
- `dto/response/ApiResponse.java`
- `dto/response/NotificationEventResponse.java`
- `dto/response/AiStreamChunkResponse.java`
- `service/RealtimeChannels.java`
- `service/RealtimePublisher.java`
- `service/RealtimeNotificationService.java`
- `service/impl/RealtimeNotificationServiceImpl.java`
- `controller/UserController.java`
- `controller/NotificationController.java`
- `controller/AiStreamController.java`
- `security/CurrentUserResolver.java`
- `config/SecurityConfig.java`
- `service/impl/UserServiceImpl.java`

### 6. Checklist cho lần làm việc tiếp theo
- **Backend:**
  - Tích hợp AI Provider vào AI Stream endpoint (nếu có).
  - Cân nhắc thêm persistence cho Notification (lưu DB).
  - Review lại RBAC cho các API khác.
  - Triển khai rate limiting cho các API nhạy cảm (auth, user management).
  - Tối ưu hóa các N+1 query nếu có.
- **Frontend:**
  - Connect tới SSE endpoints với cơ chế auth an toàn (Cookie HttpOnly).
  - Xử lý nhận và hiển thị notification/AI stream.
  - Implement logic gửi request lên backend (profile update, admin actions).

---

## 2026-05-22 (phiên trước – Authentication hardening)

### 1. JWT filter không còn làm rơi 500 khi token lỗi
- **File:** `src/main/java/com/duoq/medlearn/config/JwtAuthenticationFilter.java`
- **Thay đổi:** Bọc logic parse/validate JWT trong `try/catch` (`JwtException`, `IllegalArgumentException`).
- **Kết quả:** Khi token lỗi → clear security context và cho request đi tiếp để Spring trả đúng unauthorized flow, thay vì nổ 500.

### 2. Sửa logic refresh token reuse detection
- **File:** `src/main/java/com/duoq/medlearn/service/impl/AuthServiceImpl.java`
- **Thay đổi:** Đổi thứ tự kiểm tra trong `refreshToken(...)`:
  1. Check `revokedAt` trước để phát hiện token reuse.
  2. Nếu reuse → revoke toàn bộ session user và ném lỗi bảo mật.
  3. Sau đó mới check expired/deactivated.
- **Mục đích:** Tránh bug cũ: check `isValid()` quá sớm làm nhánh reuse không chạy.

### 3. Logout dùng refresh token đúng cách
- **File:** `src/main/java/com/duoq/medlearn/controller/AuthController.java`
- **Thay đổi:** Endpoint `/api/auth/logout` đổi sang nhận body `RefreshTokenRequest` (giống refresh flow).
- **Mục đích:** Không còn cắt Authorization bearer (access token) rồi coi như refresh token.

### 4. Chống timing attack ở login
- **File:** `src/main/java/com/duoq/medlearn/service/impl/AuthServiceImpl.java`
- **Thay đổi:** Dù user không tồn tại vẫn thực hiện `passwordEncoder.matches(...)` với hash giả cố định.
- **Mục đích:** Giảm khả năng suy luận username/email tồn tại qua khác biệt thời gian phản hồi.

### 5. Session management ở login
- **File:** `src/main/java/com/duoq/medlearn/service/impl/AuthServiceImpl.java`
- **Thay đổi:** Đổi từ `deleteByUserId(...)` sang `revokeAllUserSessions(...)`.
- **Mục đích:** Giữ lại dấu vết session bị revoke để reuse detection có dữ liệu.

### 6. Vá lộ dữ liệu nhạy cảm ở `/api/auth/me`
- **Files:**
  - `src/main/java/com/duoq/medlearn/dto/response/UserDTO.java` (mới)
  - `src/main/java/com/duoq/medlearn/service/AuthService.java`
  - `src/main/java/com/duoq/medlearn/service/impl/AuthServiceImpl.java`
  - `src/main/java/com/duoq/medlearn/controller/AuthController.java`
- **Thay đổi:** `/me` không trả `CustomUserDetails` trực tiếp nữa.
- **Mục đích:** Tránh nguy cơ serialize lộ `passwordHash`.

### 7. Thêm hệ exception rõ nghĩa cho auth
- **Files mới trong:** `src/main/java/com/duoq/medlearn/exception/`
  - `AuthenticationException`
  - `InvalidCredentialsException`
  - `EmailAlreadyExistsException`
  - `UsernameAlreadyExistsException`
  - `EmailNotVerifiedException`
  - `InvalidTokenException`
  - `AccountDeactivatedException`
  - `TokenReusedException`
  - `ResourceNotFoundException`
  - `GlobalExceptionHandler`
- **Ý nghĩa:** Tách loại lỗi rõ ràng thay vì dùng `RuntimeException` chung chung.
- **Map status code hợp lý qua `@RestControllerAdvice`**:
  - 401 cho lỗi authentication
  - 409 cho conflict (email/username trùng)
  - 400 cho token/request không hợp lệ
  - 404 cho resource không tìm thấy

### 8. OAuth2 login đã có refresh session
- **File:** `src/main/java/com/duoq/medlearn/config/OAuth2LoginSuccessHandler.java`
- **Thay đổi:** Khi OAuth2 success:
  - Revoke session cũ của user.
  - Tạo `accessToken` + `refreshToken`.
  - Lưu `UserSession` với `expiresAt` theo `jwt.refresh-expiration`.
  - Redirect hiện mang cả token access + refresh ở query params.
- **Ghi chú bảo mật:** query param vẫn có rủi ro lộ log/history/referer; nên cân nhắc chuyển dần sang cookie HttpOnly/Secure hoặc cơ chế front-channel an toàn hơn.

### 9. Thêm mapper layer cho response user
- **Files:**
  - `src/main/java/com/duoq/medlearn/mapper/MapStructConfig.java`
  - `src/main/java/com/duoq/medlearn/mapper/UserMapper.java`
  - `src/main/java/com/duoq/medlearn/service/impl/AuthServiceImpl.java`
- **Thay đổi:** Thêm MapStruct config và `UserMapper` để map `User` → `UserDTO`.
- **Mục đích:** `/api/auth/me` đi qua mapper thay vì dựng response bằng object security/internal. Giữ DTO boundary rõ hơn và giảm nguy cơ lộ field nhạy cảm khi serialize.

### 10. Chuẩn hóa vị trí service implementation
- **Files tiêu biểu:**
  - `src/main/java/com/duoq/medlearn/service/impl/AuthServiceImpl.java`
  - `src/main/java/com/duoq/medlearn/service/impl/EmailServiceImpl.java`
  - `src/main/java/com/duoq/medlearn/service/impl/SymptomCheckerServiceImpl.java`
  - `src/main/java/com/duoq/medlearn/service/impl/CustomOAuth2UserServiceImpl.java`
  - `src/main/java/com/duoq/medlearn/service/impl/CustomUserDetailsServiceImpl.java`
- **Thay đổi:** Các implementation được đặt về `service/impl` để thống nhất với layered architecture của project.
- **Mục đích:** Interface ở `service/`, class triển khai ở `service/impl/` rõ ràng hơn khi đọc code và mở rộng sau này.

### 11. Xác nhận password hash
- **File:** `src/main/java/com/duoq/medlearn/service/impl/AuthServiceImpl.java`
- **Kiểm tra:** Register đã hash password đúng bằng `passwordEncoder.encode(request.getPassword())`.
- **Kết luận:** Không có thay đổi làm mất hash plaintext protection.

### 12. Build verification trong phiên này
- **Đã chạy:** `mvn -q -DskipTests compile`
- **Kết quả:** compile pass (không lỗi biên dịch).

### 13. Những việc nên làm tiếp (không bắt buộc ngay)
1. **Đổi OAuth2 redirect token khỏi query param**
   - Ưu tiên cookie HttpOnly/Secure + SameSite phù hợp.
2. **Bổ sung rate limit cho auth endpoints**
   - `/login`, `/register`, `/refresh`, `/verify`.
3. **Tăng policy password**
   - Hiện `RegisterRequest` đang min 6 ký tự; cân nhắc regex mạnh hơn (hoa/thường/số/ký tự đặc biệt).
4. **Thêm test integration cho auth critical paths**
   - Login sai đúng, refresh reuse, logout revoke, /me không lộ dữ liệu nhạy cảm.
5. **Bổ sung test cho mapper + boundary DTO**
   - Đảm bảo `UserMapper` không map các field nhạy cảm (`passwordHash`, internal flags không cần public).
6. **Rà soát lại package `service/impl` sau khi chuẩn hóa**
   - Check import wiring, bean scan, và naming consistency để tránh regressions khi merge thêm tính năng mới.

### 14. Checklist nhanh để bắt đầu phiên sau
- [ ] Kiểm tra frontend đã gửi `refreshToken` body cho `/api/auth/logout` chưa.
- [ ] Kiểm tra frontend OAuth2 redirect có xử lý thêm `refreshToken` chưa.
- [ ] Chạy lại smoke test auth APIs: register, login, me, refresh, logout, refresh reuse.
- [ ] Quyết định có chuyển OAuth2 token delivery sang cookie không.
- [ ] Nếu tiếp tục auth hardening: ưu tiên rate limit, password policy, integration tests.
- [ ] Trước khi merge: chạy lại `mvn -q -DskipTests compile` và nếu có test thì chạy test liên quan auth.

### 15. Mốc file thay đổi chính (nhìn nhanh)
- `config/JwtAuthenticationFilter.java`
- `controller/AuthController.java`
- `service/AuthService.java`
- `service/impl/AuthServiceImpl.java`
- `config/OAuth2LoginSuccessHandler.java`
- `dto/response/UserDTO.java` (mới)
- `mapper/MapStructConfig.java` (mới)
- `mapper/UserMapper.java` (mới)
- `service/impl/*` (chuẩn hóa package implementation)
- `exception/*` (mới)

---

## Cách sử dụng file này
- Mỗi ngày làm việc, thêm một mục mới với ngày hiện tại.
- Ghi lại các thay đổi chính, file đã sửa, và checklist cho lần sau.
- Khi bắt đầu phiên mới, đọc phần ngày gần nhất để nắm tiến trình và tiếp tục công việc.

