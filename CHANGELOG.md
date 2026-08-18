# Changelog

Mọi thay đổi về thuật toán, methodology hoặc phiên bản rule đều phải được ghi ở đây
(CLAUDE_CODE_WORKFLOW §9). Một golden test chỉ được cập nhật lại kèm một mục ở đây
giải thích vì sao kết quả thay đổi.

Định dạng theo [Keep a Changelog](https://keepachangelog.com/).

## [Unreleased]

### Added — Phase 2: Database + methodology registry

- Module mới `destiny-persistence` — JPA + Flyway, khác `destiny-core` ở chỗ
  module này CHỦ ĐÍCH phụ thuộc Spring/JPA
- `V1__create_identity.sql` — `users`, `birth_profiles`. Xác thực (mật khẩu,
  OAuth) chưa được đặc tả ở đâu nên chưa thiết kế, tránh bịa một sơ đồ bảo
  mật không có căn cứ
- `V2__create_methodology_registry.sql` — `methodologies`,
  `methodology_versions`, `methodology_version_research_refs`,
  `rule_versions`. Ràng buộc CHECK ở tầng database bắt buộc: version nào có
  status cho phép tính toán (`PRODUCTION_READY`/`CONTENT_REQUIRED`) thì phải
  có `school` và `source` — bản sao ở tầng DB của guard đã có trong
  `EngineMetadata` từ Phase 1
- `MethodologyRegistryService` + `MethodologyRegistrySeeder` — hiện thực hóa
  ADR D7: 11 methodology được đăng ký với **status thật**, đối chiếu trực
  tiếp từ `docs/RESEARCH_BLOCKERS.md`. Chạy tự động lúc khởi động
  (`ApplicationReadyEvent`), idempotent
- Một lỗi thật được bắt bởi chính guard trên: seed data ban đầu để
  `TAROT_RWS` (status `CONTENT_REQUIRED`, tức là được phép tính) thiếu
  `school`/`source` — bị từ chối ngay lúc khởi tạo. Đã sửa bằng cách khai
  rõ "Rider-Waite-Smith (RWS)" và trích dẫn Master Spec §17
- `DestinyOsApplication` bổ sung `@ComponentScan`, `@EntityScan`,
  `@EnableJpaRepositories` trỏ về `io.destinyos` — nếu không, Spring Boot chỉ
  quét gói của chính nó và toàn bộ bean/entity của `destiny-persistence` sẽ
  bị bỏ sót một cách âm thầm
- 16 test mới (63 tổng): round-trip identity, guard của registry, độ chính
  xác của seeder đối chiếu với `RESEARCH_BLOCKERS.md`, và một smoke test
  khởi động toàn bộ Spring context thật để xác nhận wiring — vì test dạng
  `@DataJpaTest` với `@Import` tường minh không chứng minh được
  `@ComponentScan` tự động hoạt động

### Giới hạn môi trường (ghi nhận trung thực, không che giấu)

Môi trường phát triển hiện tại **không có Docker và không có PostgreSQL cục
bộ**. Do đó:

- Test cục bộ chạy trên H2 ở chế độ tương thích PostgreSQL
  (`MODE=PostgreSQL;DATABASE_TO_UPPER=false`) — đây là giải pháp thay thế
  tạm thời, H2 không đảm bảo mọi ràng buộc riêng của PostgreSQL
- Đã bổ sung job `postgres-verify` trong CI (`.github/workflows/build.yml`)
  chạy cùng bộ test đó trên PostgreSQL 16 thật qua GitHub Actions service
  container, để migration được xác minh trên đúng hệ quản trị CSDL production
  trước khi merge — nhưng job này chưa được chạy thực tế do giới hạn của
  phiên làm việc hiện tại (không thể kích hoạt CI từ đây)

### Scope không bao gồm trong Phase 2

- Chưa expose REST API cho registry — bảng roadmap chỉ định "Database +
  methodology registry", việc lộ ra API là quyết định của các phase sau
- V3–V10 (lịch, calculation/evidence/signal, scenario/fusion, dữ liệu tham
  chiếu Tarot/Numerology, audit, AI narrative) chưa tạo — thuộc Phase 3 trở
  đi theo đúng roadmap
- Xác thực người dùng (password hashing, OAuth, JWT — Master Spec §28) chưa
  thiết kế vì chưa có đặc tả cụ thể nào trong tài liệu

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
