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

## 📁 Cấu trúc dự án

```bash
medlearn/
├── backend/          # Spring Boot
├── frontend/         # React + TS
├── docs/             # Tài liệu SRS, Database, API
├── docker-compose.yml
└── README.md