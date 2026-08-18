# Changelog

Mọi thay đổi về thuật toán, methodology hoặc phiên bản rule đều phải được ghi ở đây
(CLAUDE_CODE_WORKFLOW §9). Một golden test chỉ được cập nhật lại kèm một mục ở đây
giải thích vì sao kết quả thay đổi.

Định dạng theo [Keep a Changelog](https://keepachangelog.com/).

## [Unreleased]

### Added — Phase 1: Project foundation

- Cấu trúc Maven multi-module: `destiny-core`, `destiny-engine-api`,
  `destiny-execution`, `destiny-i18n`, `destiny-app`
- `EngineResult` với đủ 8 trạng thái. `RESEARCH_REQUIRED` và `NOT_IMPLEMENTED`
  bắt buộc kèm `ResearchReference` giải thích thiếu gì
- `CalculationContext` gồm các trường bắt buộc theo `CLAUDE.md` §4, cộng phần
  mở rộng theo ADR D3: `birthRegion`, `calendarMethodology`, `uncertainties[]`
- `Signal` và `Evidence`, đã áp dụng DECISION_LOG C3, C4, C8
- SPI `MetaphysicalEngine<I,O>` kèm capability, metadata, validation
- `EngineExecutor` — Virtual Threads, timeout theo từng engine, hủy tác vụ,
  cô lập lỗi, giới hạn đồng thời, không retry
- Registry nhãn tiếng Việt kèm test phủ toàn bộ
- 7 luật ArchUnit, trong đó có hai luật then chốt: Fusion không phụ thuộc engine
  (ADR D5) và không có kiểu số thực trong domain (ADR D6)
- 47 test

### Decisions

- **D1** Java 21 + Spring Boot, modular monolith, PostgreSQL
- **D2** Thứ tự phase theo command §34, thay cho Master Spec §31
- **D3** Calendar Authority Rule — phương pháp lịch Việt Nam là mặc định;
  cấm mô hình `year < 1968 ⇒ UTC+8`; quy tắc múi giờ theo (ngày, vùng)
- **D4** Next.js + React + TypeScript cho UI
- **D5** Fusion chỉ phụ thuộc contract `Signal`
- **D6** Không có xác suất/điểm số/trọng số trong domain model
- **D7** Engine bị chặn nghiên cứu vẫn được đăng ký và hiển thị
- **D8** AI là bước tùy chọn, không chặn luồng chính

### Open

- **C2** Tập enum fusion outcome — chờ chủ dự án chốt. Chặn Phase 6
- **C5** Phân tầng từ vựng trạng thái — chờ chủ dự án chốt. Chặn Phase 6
- **R1–R17** Các mục nghiên cứu. Cụm lịch (R9, R10, R14–R17) là đường găng

### Not included

Chưa có bất kỳ phép tính huyền học nào. Đây là chủ ý: Phase 1 chỉ xây contract
và harness, nên không có chỗ nào có thể vi phạm `CLAUDE.md` Rule C.
