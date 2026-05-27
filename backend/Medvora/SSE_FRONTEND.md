Hướng dẫn frontend: kết nối Server-Sent Events (SSE)

Tổng quan
- Endpoint SSE backend:
  - Thông báo realtime: GET /api/notifications/stream
  - AI stream (scaffold): GET /api/ai/stream
- Sự kiện trên stream (event.name):
  - "notification" → payload JSON: { type, title, message, createdAt }
  - "ai-stream" → payload JSON: { chunk, done, createdAt }
  - Kết nối thành công: "notification-connected" / "ai-stream-connected"

Bảo mật (Authorization)
- Trình duyệt EventSource không hỗ trợ custom headers (ví dụ Authorization).
- Các lựa chọn an toàn (ưu tiên):
  1) Cookie-based JWT: server đặt cookie HttpOnly, frontend chỉ mở EventSource bình thường.
  2) Proxy/server-side endpoint: frontend mở kết nối đến proxy (cùng origin) và proxy thêm header Authorization trước khi mở SSE tới backend.
- Lựa chọn tạm (ít an toàn hơn): gửi token trong query string: /api/notifications/stream?access_token=XYZ (không khuyến nghị cho token dài hạn).

Ví dụ (option query param)
// Lưu ý: chỉ dùng nếu bạn chấp nhận rủi ro truyền token trong URL
const token = await getJwt();
const evtSource = new EventSource(`/api/notifications/stream?access_token=${encodeURIComponent(token)}`);

evtSource.addEventListener('notification', (e) => {
  try {
    const payload = JSON.parse(e.data);
    // payload: { type, title, message, createdAt }
    console.log('Notification', payload);
    // hiển thị UI
  } catch (err) {
    console.error('Invalid notification payload', err);
  }
});

evtSource.addEventListener('notification-connected', (e) => {
  console.info('SSE connected for notifications');
});

evtSource.onerror = (err) => {
  console.error('SSE error', err);
  // EventSource tự reconnect theo server-sent retry; bạn có thể thêm logic bổ sung
};

// Đóng connection khi chuyển trang
window.addEventListener('beforeunload', () => evtSource.close());

Reconnection + backoff (nâng cao)
// EventSource có reconnect tự động nhưng chưa backoff tùy chỉnh.
// Mẫu: tự đóng và tái tạo với backoff nếu cần.
function createEventSourceWithBackoff(url) {
  let es = null;
  let attempts = 0;
  const maxDelay = 30000;

  function connect() {
    es = new EventSource(url);
    es.onopen = () => { attempts = 0; console.log('SSE open'); };
    es.onerror = (e) => {
      console.warn('SSE error, will retry', e);
      es.close();
      attempts++;
      const delay = Math.min(1000 * Math.pow(2, attempts), maxDelay);
      setTimeout(connect, delay);
    };
    return es;
  }
  return connect();
}

// Usage
const source = createEventSourceWithBackoff('/api/notifications/stream?access_token=' + token);

AI stream client (nhận chunk)
const aiSource = new EventSource('/api/ai/stream?access_token=' + token);
aiSource.addEventListener('ai-stream', (e) => {
  const chunk = JSON.parse(e.data); // { chunk, done, createdAt }
  processChunk(chunk.chunk);
  if (chunk.done) {
    // hoàn tất
  }
});

Gợi ý cho frontend integration
- Xử lý JSON.parse an toàn.
- Hiển thị badge số thông báo chưa đọc bằng API riêng (backend có thể cung cấp endpoint count nếu cần).
- Nếu cần gửi dữ liệu nhiều chiều (client → server) trong phiên realtime, dùng REST/WebSocket thay vì SSE.

Lời khuyên bảo mật
- Tránh truyền token trong URL trên môi trường production.
- Ưu tiên cookie HttpOnly (server set-cookie khi login) hoặc proxy để inject header.
- Giới hạn thời gian token (short-lived) nếu phải dùng query param.

Kết luận
- SSE là lựa chọn nhẹ, phù hợp cho các event server→client (notification, streaming AI tokens).
- Với thiết kế hiện tại, frontend có thể kết nối tới /api/notifications/stream và lắng nghe event 'notification' và 'notification-connected'.

Nếu muốn, mình sẽ:
- Thêm endpoint demo test để FE dễ thử (API để gửi notification test tới user hiện tại).
- Hoặc thêm hướng dẫn mẫu dùng cookie-based auth flow (server-side steps) và code set-cookie trên login.

