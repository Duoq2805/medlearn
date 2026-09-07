# MedLearn - Nền tảng Học Tập Y Khoa

**Hệ thống hỗ trợ học tập y khoa** với nội dung bệnh học có cấu trúc, Symptom Checker và quy trình kiểm duyệt nội dung (moderation).

---

## ✨ Tính năng chính

- **Disease Management** với Version Control & Moderation
- **Symptom Checker** thông minh (gợi ý bệnh theo triệu chứng)
- **Moderation System** (Draft → Review → Approve)
- **Case Study** học tập lâm sàng
- **Role-based Access**: Student, Contributor, Reviewer, Admin

---

## 🛠 Công nghệ sử dụng

### Backend
- **Spring Boot 3.4** + Java 21
- Spring Data JPA + Flyway
- PostgreSQL
- Spring Security + JWT
- Springdoc OpenAPI (Swagger)

### Frontend
- React + TypeScript + Vite
- TailwindCSS (sẽ dùng)

### DevOps
- Docker + Docker Compose
- Monorepo structure

---

## 🔑 Environment Variables

> **Lưu ý:** `JWT_SECRET` là bắt buộc — app sẽ **KHÔNG khởi động** nếu thiếu.
> Không nên commit secret thật; mỗi môi trường tự sinh.

```bash
# Sinh secret (>= 32 ký tự)
openssl rand -base64 48

# Sao chép template rồi điền giá trị
cp backend/.env.example backend/.env
```

| Variable | Bắt buộc | Mô tả |
|---|---|---|
| `JWT_SECRET` | ✅ | Khóa ký JWT (min 32 chars) |
| `SPRING_DATASOURCE_PASSWORD` | phụ thuộc profile | Password DB (docker fallback: `Medlearn@123`) |
| `SPRING_MAIL_USERNAME` / `SPRING_MAIL_PASSWORD` | email verify/reset | SMTP (Gmail app password) |
| `SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_ID` / `..._SECRET` | Google login | Google OAuth2 client |
| `AI_GATEWAY_API_KEY` | AI features | API key 9router.com |

### Chạy local (backend)

```bash
# 1. tạo .env
cp backend/.env.example backend/.env
# 2. khởi động DB (docker)
docker compose up -d postgres
# 3. chạy backend (đặt JWT_SECRET)
export SPRING_DATASOURCE_PASSWORD=... JWT_SECRET=...
cd backend && mvn spring-boot:run
```

### Chạy bằng Docker (full stack)

```bash
# backend/.env phải có JWT_SECRET
docker compose up --build
```

---

## 📁 Cấu trúc dự án

```bash
medlearn/
├── backend/          # Spring Boot
├── frontend/         # React + TS
├── docs/             # Tài liệu SRS, Database, API
├── docker-compose.yml
└── README.md