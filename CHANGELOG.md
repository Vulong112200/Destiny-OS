# Changelog

Mọi thay đổi về thuật toán, methodology hoặc phiên bản rule đều phải được ghi ở đây
(CLAUDE_CODE_WORKFLOW §9). Một golden test chỉ được cập nhật lại kèm một mục ở đây
giải thích vì sao kết quả thay đổi.

Định dạng theo [Keep a Changelog](https://keepachangelog.com/).

## [Unreleased]

### Fixed — ArchUnit `enginesStayIndependent` rule was a false-positive generator

Discovered while verifying Phase 6. `noClasses().that().resideInAPackage("io.destinyos.engines.(*)..").should().dependOnClassesThat().resideInAPackage("io.destinyos.engines.(*)..")`
checks the same wildcard pattern independently on each side — it does not
require the two `(*)` captures to differ. The rule was therefore satisfied
by any class inside **one** engine depending on another class in that
**same** engine (e.g. `TarotEngine` using `TarotCard`), firing 182 false
violations the moment a second real engine package existed to compare
against. Rewritten using ArchUnit's `SlicesRuleDefinition.slices()` API,
which is built for exactly this "siblings must not depend on each other,
internal dependencies are fine" shape.

### Fixed — `destiny-app` never depended on the engine/fusion modules

A second, related gap: `destiny-app` — the only module with every other
module on its classpath, and the one that hosts the architecture test suite
— never actually depended on `destiny-engine-tarot`,
`destiny-engine-numerology`, or `destiny-fusion`. Every ArchUnit rule about
those packages (including the one above, and "Fusion depends only on the
Signal contract") had been scanning zero real classes since Phase 4/5 —
passing, but proving nothing. Added the missing dependencies and
strengthened `importerSeesRealClasses` to assert these packages are
actually visible, so this specific gap cannot recur silently again.

Both gaps were caught by the same discipline applied throughout this
project: verify with a `mvn clean verify`, not an incremental build, and
when a rule is meant to catch something, inject a deliberate violation and
confirm it is actually caught before trusting a green run.

### Added — Phase 6: Fusion engine

- Module mới `destiny-fusion` — phụ thuộc duy nhất `destiny-core` (ADR D5,
  đã kiểm chứng bằng cách tiêm vi phạm giả và xác nhận ArchUnit bắt được)
- **C2 và C5 đã được anh xác nhận** (ghi ở `docs/DECISION_LOG.md`):
  - C2: `FusionOutcome` — hợp cả hai tập enum của Master Spec §9 và
    FUSION_ENGINE_SPEC.md §7, đủ 12 giá trị, không bỏ trạng thái nào
  - C5: `DimensionState` (từ vựng Rule E, 8 giá trị) là tầng **theo từng
    dimension**; `FusionOutcome` là tầng **kết quả tổng của cả kịch bản**
- Cài đặt đầy đủ pipeline theo `FUSION_ENGINE_SPEC.md`: đếm phiếu theo
  **engine riêng biệt** (không theo signal/evidence — 5 signal từ 1 engine
  = 1 nguồn), phát hiện `DIRECT_CONFLICT` và `METHODOLOGY_CONFLICT`, tín
  hiệu critical sống sót qua biểu quyết đa số (R5)
- Ngưỡng biểu quyết cụ thể (đa số tuyệt đối = nhiều nguồn hơn tổng các cực
  còn lại cộng lại) là **quyết định kỹ thuật của dự án**, không phải suy
  đoán về một trường phái bên ngoài — đặc tả cố tình không cho số cụ thể,
  ghi rõ trong Javadoc của `FusionEngine`
- Bắt được 1 lỗi thật khi viết test: outcome tổng hợp dựa nhầm vào state
  đã bị điều chỉnh bởi critical override thay vì đếm phiếu gốc, khiến
  "critical caution vượt qua đa số 3 support" ra sai kết quả — sửa bằng
  cách tính lại từ tập engine gốc
- Đủ **14/14 test case bắt buộc** theo `FUSION_ENGINE_SPEC.md` §12: 0
  engine, 1 support, 1 caution, đồng thuận support/caution, 2 vs 1, 2 vs 2,
  critical caution, trùng lặp cùng engine, methodology conflict, scope
  conflict, engine timeout, NOT_APPLICABLE, evidence chưa đầy đủ
- 19 test mới (136 tổng)

### Added — Phase 4: Numerology (Pythagorean) engine

- Module mới `destiny-engine-numerology` — không phụ thuộc framework,
  không phụ thuộc Calendar (cùng tính chất với Tarot cho phép chạy song
  song với nghiên cứu lịch)
- **R8 đã được nghiên cứu và quyết định** (ghi ở `docs/RESEARCH_BLOCKERS.md`,
  không push git): chuẩn hóa tên tiếng Việt bằng Unicode NFD + loại bỏ
  combining mark, cộng thêm bước thay thế riêng cho `đ/Đ` vì Unicode không
  phân rã ký tự này thành chữ cái gốc + dấu (khác `ế`, `ầ`...) — một sự thật
  kỹ thuật cụ thể, không phải suy đoán
- Thứ tự rút gọn Life Path: rút gọn tháng/ngày/năm **riêng biệt** trước khi
  cộng lại — có nguồn giải thích rõ lý do (tránh mất số chủ đạo 11/22/33 do
  gộp chữ số tùy tiện), không chỉ là quy ước
- Chữ Y luôn được coi là phụ âm cho Soul Urge/Personality — **đơn giản hóa
  có ghi nhận rõ ràng**, không phải kết luận có nguồn: quy tắc "Y là nguyên
  âm khi là âm duy nhất trong âm tiết" của tiếng Anh không có cơ sở áp dụng
  máy móc cho tên Việt đã Latin hóa
- Golden test dùng ví dụ tính mẫu từ nguồn độc lập bên ngoài, không tự sinh
  từ chính code: Life Path ngày 15/3/1990 = 1; ngày sinh 29 giữ số chủ đạo
  11; Expression của "John Doe" = 8; "Jane Marie Doe" = 1
- 5 chỉ số triển khai: Life Path, Expression, Soul Urge, Personality,
  Birthday. **Chưa triển khai** Maturity, Personal Year/Month/Day — công
  thức chưa được nghiên cứu trong đợt này, không đoán
- Engine **không phát sinh signal** — cùng lý do với Tarot: gán ý nghĩa cho
  một con số đòi hỏi nội dung diễn giải tiếng Việt chưa có
- Cập nhật `MethodologyRegistrySeeder`: `NUMEROLOGY_PYTHAGOREAN` chuyển từ
  `DECISION_REQUIRED` sang `CONTENT_REQUIRED` (thuật toán đã xong và có
  golden test, chỉ thiếu nội dung diễn giải) — nhất quán với cách xử lý
  Tarot
- 29 test mới (117 tổng)

### Added — Phase 5: Tarot engine (seeded, reproducible)

- Module mới `destiny-engine-tarot` — không phụ thuộc framework, không phụ
  thuộc engine nào khác, không phụ thuộc Calendar (đúng tính chất cho phép
  Phase 5 chạy song song với nghiên cứu lịch, ADR D2)
- Bộ bài Rider-Waite-Smith đầy đủ 78 lá: 22 Major Arcana (đúng thứ tự
  RWS — Strength lá thứ 8, Justice lá thứ 11, khác với thứ tự Marseille),
  56 Minor Arcana (4 chất × 14 bậc). Đây là dữ kiện cấu trúc có thể kiểm
  chứng, không phải nội dung cần nghiên cứu (khác `R11` — nội dung ý nghĩa
  tiếng Việt vẫn thiếu, mỗi lá bài xuất xưởng với `TarotCardMeaning.EMPTY`,
  không bịa nội dung)
- `TarotEngine` — rút bài có seed: sinh seed bằng CSPRNG khi người gọi không
  cung cấp, dùng seed đó cho `java.util.Random` xác định (Fisher-Yates +
  xoay lá), luôn trả lại seed đã dùng để tái lập được — đúng theo giải pháp
  đã ghi ở DECISION_LOG C6
- Chính sách xoay lá (upright/reversed) mới — DECISION_LOG **C9**: xác định
  bằng bit ngẫu nhiên độc lập từ cùng luồng seed, versioned, có thể tắt
  (`UPRIGHT_ONLY`). Đây là lựa chọn kỹ thuật, không phải tranh chấp trường
  phái, nên không cần xử lý theo Rule D
- Engine **không phát sinh signal nào** — vì gán dimension/polarity cho một
  lá bài đòi hỏi nội dung ý nghĩa (R11) chưa có; phát minh ra signal lúc này
  sẽ đúng là hành vi Rule C cấm. Lần rút bài vẫn hoàn toàn hợp lệ và trung
  thực, chỉ tầng diễn giải là chưa có
- 25 test mới (88 tổng): tính đúng cấu trúc bộ bài, tính xác định theo seed,
  không thiên vị khi xoay lá, không trùng lá trong một lần rút, số lá khớp
  spread, versioning đầy đủ

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
