# Changelog

Mọi thay đổi về thuật toán, methodology hoặc phiên bản rule đều phải được ghi ở đây
(CLAUDE_CODE_WORKFLOW §9). Một golden test chỉ được cập nhật lại kèm một mục ở đây
giải thích vì sao kết quả thay đổi.

Định dạng theo [Keep a Changelog](https://keepachangelog.com/).

## [Unreleased]

### Added — Phase 3: Calendar Engine (`destiny-calendar`)

- Module mới `destiny-calendar` — hạ tầng thuần Java, không phải
  `MetaphysicalEngine` (chưa scenario nào gọi tới Calendar; Bát Tự/Tử Vi —
  người tiêu thụ tương lai — vẫn đang chặn nghiên cứu ở chính thuật toán
  của họ). Chỉ phụ thuộc `destiny-core`
- **R10 đã được anh chốt** (2026-08-19): giờ Tý tính sang trụ ngày mới từ
  23:00 (quy ước Tử Bình phổ biến); áp dụng chân thái dương giờ khi có
  kinh độ, fallback giờ dân sự + `Uncertainty` khi không có (không âm thầm
  bỏ qua)
- **R9, R14a, R15, R16 đã RESOLVED** — lấy đúng thuật toán byte-chính-xác
  mà 2 bản port cộng đồng lâu năm dùng (JS `vanng822/amlich`, Lua
  "Mô đun:Âm lịch" của Wikipedia tiếng Việt), cả hai đều trích dẫn trực
  tiếp Jean Meeus, *Astronomical Algorithms* (1998) và trùng khớp từng hệ
  số. Bản Wikipedia còn cho công thức Can Chi Năm/Tháng/Ngày dạng đóng
  (`canchi(năm+57)`, `canchi(năm*12+tháng+14)`, `canchi(floor(JD+51.5))`)
  — chưa từng có nguồn nào trong dự án trước đây
- **Không copy code** — do code gốc của Hồ Ngọc Đức ghi rõ "personal,
  non-commercial use". Các hệ số là công thức trong sách Meeus (giống hệt
  nhau qua 3 giấy phép độc lập là bằng chứng chúng đến từ sách, không phải
  từ một tác giả riêng) — viết lại độc lập bằng Java, trích dẫn Meeus làm
  nguồn, dùng 2 bản port trên làm oracle đối chiếu kết quả (đúng vai trò
  ADR D3 đã gán cho triển khai của Hồ Ngọc Đức)
- Golden test lấy trực tiếp từ bảng ví dụ tính mẫu giây-chính-xác của
  chính Hồ Ngọc Đức (1983-1986) và 4 năm lệch Việt/Trung có tên cụ thể
  (1985 — lệch nguyên 1 tháng, 2007, 2030, 2053) — không có giá trị nào tự
  sinh từ code của dự án (CLAUDE.md §32)
- **Bắt được 1 lỗi thật khi viết test**: công thức trụ ngày ban đầu dùng
  nhầm quy ước Julian Day (JDN theo giờ trưa từ bản port JS) trong khi
  công thức Can Chi ngày của Wikipedia định nghĩa theo JD-0h-UT (lệch đúng
  0.5) — làm lệch toàn bộ trụ ngày/giờ đi một bước. Phát hiện nhờ đối
  chiếu với dữ kiện độc lập "1/1/2000 = ngày Mậu Ngọ"; sửa bằng cách xử lý
  quy đổi một lần duy nhất bên trong `CanChi.dayPillar`
- **Lệch khỏi kế hoạch V3 migration ban đầu (Phase 0)**: theo tiền lệ đã
  thiết lập ở Phase 5 (bộ bài Tarot là dữ liệu Java thuần, không phải
  bảng DB), tiết khí/điểm sóc/tháng nhuận tính runtime (không cần dataset
  tiền tính), bảng múi giờ lịch sử R14a là Java record tĩnh — không tạo
  migration V3
- `CALENDAR_VN_TRADITIONAL` chuyển từ `RESEARCH_REQUIRED` sang
  `PRODUCTION_READY` — R14b (ranh giới địa lý Bắc/Nam) và R17 vẫn mở,
  nhưng chỉ ảnh hưởng các trường hợp (ngày, vùng) cụ thể, không chặn cả
  phương pháp — cùng mô hình đã áp dụng cho Tarot/Numerology với R11/R8
- 86 test mới (257 tổng): bảng golden gốc, 4 năm lệch Việt/Trung, quét
  Tết toàn bộ 1900-2100, chu kỳ Can Chi 60 năm, ranh giới giờ Tý 23:00,
  R14a/R14b không âm thầm chọn một bên khi vùng chưa xác định

### Added — REST API layer (`destiny-api`)

- Module mới `destiny-api` — chỉ phụ thuộc `destiny-scenario`, `destiny-persistence`,
  `destiny-i18n`; **không bao giờ import một `destiny-engine-*` cụ thể nào**
  (CLAUDE.md §3). Ranh giới này được thực thi bằng cơ chế mới
  `EngineTaskFactory`: interface sống ở `destiny-api`, cài đặt cụ thể
  (`TarotTaskFactory`, `NumerologyTaskFactory`) sống ở `destiny-app` — nơi
  duy nhất được phép biết cả hình dạng request lẫn kiểu input cụ thể của
  từng engine. Đã tiêm một vi phạm giả (`TempProbe` gọi thẳng `TarotDeck`
  từ package controller) để xác nhận luật ArchUnit `controllersStayThin`
  thật sự bắt được lỗi trước khi xóa file thử nghiệm
- 3 nhóm endpoint (command §41): `POST /api/v1/scenarios/{scenarioType}`,
  `GET /api/v1/methodologies[/{id}]`, `GET /api/v1/calculations/{id}` — một
  methodology bị chặn nghiên cứu là một dòng 200 với status trung thực
  (ADR D7), không phải 404
- `LabeledValue` — mọi enum trả về API đều kèm nhãn tiếng Việt
  (UI_UX_VIETNAMESE_SPEC §1: không để lộ enum kỹ thuật một mình). Thiết kế
  lại một lần: bản đầu nhận `Function<E, String>` qua `VietnameseLabels::of`
  nhưng method reference bị overload khiến Java không suy luận đồng thời
  được cả "overload nào" lẫn "E là gì" — mọi lời gọi báo lỗi "cannot infer
  type-variable(s) E". Sửa bằng cách nhận thẳng `String` đã tính sẵn
- **Lỗi thật phát hiện bởi `ScenarioApiIntegrationTest`** (test tích hợp
  HTTP thật đầu tiên, chạy `TarotEngine`+`NumerologyEngine` thật qua
  `EngineWiringConfig`): `CalculationRecorder.record()` không bao giờ gọi
  `setScenarioId()`, nên `GET /api/v1/calculations/{id}` luôn trả về
  `scenarioId: null`. Đồng thời `ScenarioOrchestrationService` gán nhầm
  `scenarioType.name()` vào field `school` của `CalculationContext` — sai
  ngữ nghĩa, vì `school` (Rule D) là lựa chọn trường phái của **một
  engine**, không có ý nghĩa ở tầng một scenario chạy nhiều engine khác
  trường phái cùng lúc. Sửa bằng cách thêm overload
  `record(context, scenarioId, execution, fusion)` (bản 3 tham số cũ vẫn
  giữ nguyên, gọi qua bản mới với `scenarioId = null`, không phá test cũ ở
  `destiny-persistence`), và để `school = null` khi tạo context ở tầng
  scenario
- 16 test mới ở `destiny-api` (unit cho 3 service + slice `@WebMvcTest` cho
  3 controller, dùng `StubEngine` cục bộ thay vì phụ thuộc engine thật),
  4 test tích hợp HTTP đầu-cuối mới ở `destiny-app`
  (`ScenarioApiIntegrationTest`, 171 tổng)

### Added — V4-V6: durable calculation storage (reproducibility, closed)

Trước đây một lần chạy tính đúng nhưng **biến mất ngay khi JVM tắt** — domain
model đã hứa reproducibility từ Phase 1 nhưng chưa có gì lưu lại thật sự.

- **V4** `calculations`, `calculation_engine_results` — mỗi calculation là
  một lần chạy scenario; mỗi engine tham gia có dòng riêng (Rule F: timeout
  một engine không đụng tới dòng của engine khác). Bổ sung `result_hash`
  (C7 — bản đặc tả gốc thiếu cột này dù §4 và §10 đều yêu cầu)
- **V5** `evidence`, `signals`, `signal_evidence_refs` — `dimension` trên
  Evidence theo C4; `NOT_APPLICABLE`/`NEUTRAL` vẫn là giá trị tách biệt ở
  tầng DB (không gộp, RK7); `critical` là cách mã hóa duy nhất (C3)
- **V6** `fusion_results`, `conflicts` — không tạo bảng `scenario_evaluations`
  riêng dù tài liệu có nêu tên: `DATA_MODEL_AND_RETENTION.md` chỉ đặt tên
  entity này mà không cho trường nào, khác hẳn Calculation/Evidence/Signal
  đều có khối trường rõ ràng — tạo thêm bảng rỗng nội dung không phải là
  "đặc tả", mà là suy đoán. Vai trò của nó coi như đã được `calculations`
  (đã có scenario_id) + `fusion_results` đảm nhiệm
- `fact_json`, `dimensions_json`, `involved_engines_json` dùng cột **TEXT**
  chứa chuỗi JSON, không dùng kiểu JSONB gốc — để cùng một migration chạy
  giống hệt nhau trên cả PostgreSQL thật lẫn H2 (môi trường test cục bộ)
- `CalculationRecorder` — ghi toàn bộ một lần chạy trong một transaction;
  `resultHash` = SHA-256 của (identity string của context) + (outcome tổng
  hợp) — cùng input/version/seed + cùng outcome ⇒ cùng hash, đổi bất kỳ cái
  nào ⇒ đổi hash (đúng yêu cầu CLAUDE.md §6, có test xác nhận cả hai chiều)
- 20 test mới ở `destiny-persistence` (151 tổng)

### Added — Phase 7: Scenario engine

- Module mới `destiny-scenario` — chỉ phụ thuộc `destiny-execution` và
  `destiny-fusion`, **không phụ thuộc bất kỳ engine cụ thể nào**, có luật
  ArchUnit mới bắt buộc điều này (giống cách D5 bảo vệ Fusion)
- 10 loại scenario theo Master Spec §11, nhưng **chỉ 2 loại có chính sách
  applicability thật** (`BUSINESS`, `DAILY_ACTION`) — lấy đúng nguyên văn
  2 ví dụ cụ thể ở Master Spec §7 ("Mở rộng kinh doanh", "Hôm nay nên làm
  gì"). 8 loại còn lại được đăng ký (có thể truy vấn) nhưng đánh dấu rõ
  `policyDefined = false` — không bịa chính sách cho scenario nào tài liệu
  chưa đặc tả, đúng tinh thần áp dụng cho quyết định sản phẩm chứ không chỉ
  thuật toán huyền học
- `ScenarioEngine`: chỉ chạy các engine mà **chính sách scenario nêu tên**,
  bỏ qua hoàn toàn engine không liên quan (không tốn tài nguyên hỏi rồi bỏ
  qua câu trả lời); engine được nêu tên nhưng người gọi không cung cấp thì
  báo cáo trung thực trong `unavailableEngines`, không im lặng bỏ qua
- Applicability của scenario **chỉ có thể thu hẹp**, không bao giờ mở rộng
  applicability mà chính engine tự khai báo — một engine tự nhận LOW không
  thể được scenario nâng lên HIGH
- 7 test mới (144 tổng)

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
