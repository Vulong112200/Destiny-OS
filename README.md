# Destiny OS

**Nền tảng tính toán huyền học tất định và hỗ trợ ra quyết định theo kịch bản.**

Destiny OS **không phải** ứng dụng "AI bói toán". Đây là một rule engine tất định, có kiểm soát phiên bản, trong đó AI chỉ đóng vai trò diễn giải ngôn ngữ ở bước cuối cùng — và hệ thống vẫn hoạt động đầy đủ khi không có AI.

> Nội dung được cung cấp nhằm mục đích tham khảo, nghiên cứu văn hóa/giải trí và hỗ trợ tự phản tư; không phải dự đoán khoa học hay tư vấn y tế, pháp lý, tài chính hoặc đầu tư.

---

## Nguyên tắc cốt lõi

| Nguyên tắc | Ý nghĩa |
|---|---|
| **Tất định trước, AI sau** | Không dùng LLM để tính Bát Tự, Can Chi, Ngũ Hành, Tử Vi, lịch, Tarot hay bất kỳ hard data nào |
| **Không bịa thuật toán** | Phương pháp chưa được xác minh sẽ trả `RESEARCH_REQUIRED`, không trả kết quả "có vẻ đúng" |
| **Mâu thuẫn là kết quả hợp lệ** | Các trường phái bất đồng được giữ nguyên, không ép về một "sự thật duy nhất" |
| **Không có xác suất giả** | Không có điểm số, trọng số hay phần trăm nào trong domain model |
| **Cô lập lỗi** | Một engine hỏng không làm sập các engine độc lập |
| **Tái lập được** | Cùng input + phiên bản + seed ⇒ cùng kết quả |

Nguyên tắc cuối cùng, và là nguyên tắc quan trọng nhất:

> Nếu phải chọn giữa *"có kết quả nhưng thuật toán không chắc"* và *"chưa có kết quả nhưng trung thực về giới hạn"* — **luôn chọn phương án thứ hai.**

---

## Kiến trúc

Modular monolith trên Java 21 + Spring Boot. Luồng xử lý:

```
Input → Validation → Calendar/Astronomy
      → Deterministic Engines (chạy song song)
      → Evidence → Signal → Applicability
      → Fusion (dựa trên luật, không phải trung bình có trọng số)
      → Scenario → Pruning
      → AI Narrative (tùy chọn) → UI tiếng Việt
```

### Module

| Module | Vai trò |
|---|---|
| `destiny-core` | Kiểu miền cốt lõi. **Không phụ thuộc framework** |
| `destiny-calendar` | Lịch Việt Nam truyền thống + Can Chi — hạ tầng, không phải `MetaphysicalEngine` |
| `destiny-engine-api` | SPI `MetaphysicalEngine<I,O>` |
| `destiny-execution` | Virtual Threads, timeout, cô lập lỗi |
| `destiny-i18n` | Registry nhãn tiếng Việt |
| `destiny-persistence` | JPA + Flyway migrations + methodology registry |
| `destiny-engine-tarot` | Tarot — bộ bài RWS, rút bài có seed, tái lập được |
| `destiny-engine-numerology` | Thần số học Pythagoras — 5 chỉ số, chuẩn hóa tên tiếng Việt |
| `destiny-fusion` | Tổng hợp kết luận theo luật (không phải trung bình có trọng số) |
| `destiny-scenario` | Điều phối kịch bản — chọn engine áp dụng, không phụ thuộc engine cụ thể |
| `destiny-ai` | Lớp diễn giải AI (Phase 12) — pruning, prompt, provider OpenRouter, fallback phi-AI. Không phụ thuộc engine/fusion/scenario |
| `destiny-api` | REST controllers — không phụ thuộc engine cụ thể nào (qua `EngineTaskFactory`) |
| `destiny-app` | Spring Boot assembly + bộ test kiến trúc |
| `destiny-web` | Frontend Next.js (TypeScript, Tailwind) — dự án npm riêng, **không** thuộc Maven reactor |

Hai ràng buộc kiến trúc được **kiểm tra tự động bằng ArchUnit**, không chỉ ghi trong tài liệu:

- **Fusion không phụ thuộc bất kỳ engine nào** — chỉ phụ thuộc contract `Signal`. Đây là tính chất cho phép xây Fusion trước khi có Bát Tự.
- **Không kiểu miền nào chứa `double`/`float`/`BigDecimal`** làm điểm số hay độ tin cậy. Trung bình có trọng số trở thành *không biểu diễn được*, chứ không chỉ là "không khuyến khích".
- **`destiny-ai` không phụ thuộc engine/fusion/scenario** (ADR D8) — cùng nguyên tắc cô lập với Fusion, để cả hệ thống vẫn dùng được đầy đủ khi module này vắng mặt, bị tắt, hoặc gặp lỗi.

---

## Trạng thái hiện tại

**MVP hoàn chỉnh đầu-cuối — có UI thật, gọi API thật, ra kết quả Fusion thật.** Toàn bộ luồng chạy từ trình duyệt: chọn kịch bản → nhập dữ liệu → gọi API → chạy engine song song → sinh signal thật → tổng hợp theo luật → ghi database → hiển thị kết quả có nhãn tiếng Việt, đúng thứ tự ưu tiên trong `UI_UX_VIETNAMESE_SPEC.md`.

| Phase | Nội dung | Trạng thái |
|---|---|---|
| 0 | Kiểm toán kiến trúc | Xong |
| 1 | Nền tảng: domain, SPI, harness | Xong |
| 2 | Database + methodology registry | Xong |
| 3 | Nền tảng lịch | **Xong** — Can Chi + lịch âm dương đủ dùng; xem ghi chú bên dưới |
| 4 | Thần số học (Pythagoras) | **Xong** — 5 chỉ số + nội dung diễn giải 65 tổ hợp; Chaldean vẫn chặn (không có nguồn) |
| 5 | Tarot | **Xong** — cấu trúc, rút bài, và nội dung diễn giải đủ 78 lá |
| 6 | Fusion | **Xong** — đủ 14/14 test case bắt buộc theo đặc tả, đã có kết quả thật từ engine thật |
| 7 | Scenario | Xong — 2/10 scenario có chính sách thật (BUSINESS, DAILY_ACTION), 8 còn lại đăng ký nhưng chưa có chính sách |
| — | Lưu trữ Calculation/Evidence/Signal/Fusion (V4-V6) | Xong — `CalculationRecorder`, `result_hash` tái lập được |
| — | REST API (`destiny-api`) | Xong — 3 nhóm endpoint, xác thực bằng test tích hợp HTTP thật với engine thật |
| — | Frontend (`destiny-web`) | **Xong** — Tổng quan, Trung tâm quyết định, Lịch sử; 10 mục nav còn lại ghi "Sắp ra mắt" |
| 12 | AI Narrative (`destiny-ai`) | **Xong** — pruning, prompt, provider OpenRouter (tùy chọn, tắt mặc định), fallback phi-AI luôn render được; lưu trữ V7 |
| 8–11 | Bát Tự, Tử Vi, Phong Thủy, Chiêm tinh | Chờ nghiên cứu |

**Cập nhật AI Narrative (2026-08-20):** Module `destiny-ai` hiện thực hóa Phase 12 theo đúng `AI_NARRATIVE_SPEC.md` và ADR D8. Luồng: prune evidence theo đúng thứ tự ưu tiên đặc tả (CRITICAL → CONFLICT → STRONG → MEDIUM liên quan kịch bản → cảnh báo → giới hạn, ngân sách 8–20 signal) → dựng prompt hệ thống đúng nguyên văn đặc tả → gọi OpenRouter (nếu bật và có API key/model) qua `RestClient`, timeout + tối đa 1 lần thử lại cho lỗi tạm thời → validate schema JSON phản hồi → nếu bất kỳ bước nào thất bại (tắt, thiếu key, timeout, 429, 5xx, JSON hỏng, phản hồi rỗng), hệ thống **luôn** trả về báo cáo phi-AI dựng từ đúng dữ liệu tính toán thật, không bao giờ throw lỗi hay chặn request. Endpoint mới: `POST`/`GET /api/v1/calculations/{id}/narrative`. Không model OpenRouter mặc định nào được hardcode — danh mục model miễn phí thay đổi theo thời gian, người vận hành phải tự xác nhận và cấu hình qua `DESTINY_AI_OPENROUTER_MODEL`. 38 test riêng cho `destiny-ai` (310 tổng, tăng từ 257) cộng 1 luật ArchUnit mới xác nhận `destiny-ai` cô lập khỏi engine/fusion/scenario giống hệt Fusion (ADR D5).

**Cập nhật nội dung diễn giải (2026-08-19):** Tarot (R11) và Numerology đã có đủ nội dung tiếng Việt (78 lá × 7 trường; 65 tổ hợp số), bám theo truyền thống Rider-Waite-Smith và Pythagorean hội tụ rộng rãi, viết một lần thành dữ liệu Java tĩnh — không sinh lúc runtime (CLAUDE.md Rule B). Hai engine giờ phát sinh signal thật, và Fusion lần đầu tiên cho ra kết quả thật (không còn `INSUFFICIENT_EVIDENCE`) — ví dụ một lượt Tarot với 3 lá mang polarity trái chiều cho ra `MAJOR_CONFLICT`, đúng tinh thần Rule E.

**Cập nhật Calendar Engine (2026-08-19):** R10 (ranh giới giờ Tý 23:00 + chính sách giờ mặt trời) đã được chủ dự án chốt. R9, R14a, R15, R16 (tiết khí, múi giờ lịch sử, điểm sóc, tháng nhuận) đã **`RESOLVED`** — cài đặt độc lập bằng Java, trích dẫn Jean Meeus *Astronomical Algorithms* (1998), đối chiếu byte-chính-xác với 2 bản port cộng đồng lâu năm và golden-test trực tiếp với bảng ví dụ gốc của Hồ Ngọc Đức. Riêng **ranh giới địa lý Bắc/Nam** giai đoạn 1955–1975 (R14b) vẫn `RESEARCH_REQUIRED` — không có nguồn nào cho phần này; một lần tính rơi vào vùng chưa xác định sẽ trả về "chưa xác định được", không suy đoán. Chi tiết ở `docs/RESEARCH_BLOCKERS.md` và `docs/DECISION_LOG.md` (không push lên git).

11 methodology đã được đăng ký vào registry với trạng thái thật (đối chiếu `docs/RESEARCH_BLOCKERS.md`): 3 `PRODUCTION_READY` (Tarot, Numerology Pythagoras, Lịch Việt Nam truyền thống), còn lại `RESEARCH_REQUIRED`/`DECISION_REQUIRED`/`OUT_OF_SCOPE`.

Các engine chưa triển khai **vẫn được đăng ký và hiển thị**, kèm lý do — thay vì bị ẩn đi. Người dùng nhìn thấy `Chưa triển khai` hoặc `Cần xác minh thuật toán`, không phải một câu trả lời tự tin nhưng sai.

---

## Build

Yêu cầu JDK 21 trở lên và Maven 3.9+.

```bash
mvn verify
```

### Chạy thật với database (Supabase Postgres)

`mvn verify` chỉ dùng H2 trong bộ nhớ (profile `test`) — không cần cấu hình gì
thêm. Nhưng để **chạy** ứng dụng thật (`mvn -pl destiny-app spring-boot:run`
hoặc `java -jar`), cần một Postgres thật vì profile mặc định
(`application.yml`) chưa cấu hình sẵn datasource nào — tránh hardcode
credentials vào file commit (Master Spec §28).

1. Sao chép `.env.example` thành `.env` ở gốc repo, điền 3 biến lấy từ
   Supabase (Project Settings → Database → Connection string → tab JDBC):
   `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`,
   `SPRING_DATASOURCE_PASSWORD`. Dùng **Session pooler** (cổng 5432), không
   dùng Transaction pooler (cổng 6543) — Hibernate cần prepared statement mà
   transaction pooler không hỗ trợ.
2. `.env` bị `.gitignore` loại trừ, không bao giờ được commit.
3. Chạy `java -jar destiny-app/target/destiny-app-*.jar` **từ thư mục gốc
   repo** (nơi `.env` nằm) — `spring-dotenv` tự nạp file này vào Spring
   Environment lúc khởi động, Flyway tự chạy migration lên Postgres thật.

**Lưu ý (phát hiện khi verify frontend):** `application-test.yml` chỉ nằm ở
`src/test/resources` nên **không** được đóng gói vào jar chạy thật —
`-Dspring.profiles.active=test` trên jar đã build vô nghĩa (không có H2
trong classpath runtime). Chạy jar đã build luôn nghĩa là chạy với Postgres
thật; H2 chỉ tồn tại trong vòng đời `mvn test`. Ngoài ra, nếu chạy jar từ
thư mục gốc repo (để `.env` được nạp), biến `SPRING_DATASOURCE_URL` từ
`.env` có độ ưu tiên **cao hơn** `application-test.yml` dù profile nào đang
bật — một lý do nữa để không cố ép H2 vào jar đã đóng gói.

### Chạy frontend (`destiny-web`)

```bash
cd destiny-web
npm install
cp .env.local.example .env.local   # NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
npm run dev
```

Cần `destiny-app` đang chạy (xem trên) — frontend gọi thẳng 3 nhóm endpoint
của `destiny-api` qua CORS (`WebCorsConfig`, chỉ mở cho
`http://localhost:3000`). Mở `http://localhost:3000`.

### Bật AI Narrative (tùy chọn, tắt mặc định)

Hệ thống chạy đầy đủ mà **không cần** phần này — mọi kịch bản vẫn ra kết quả
Fusion thật và một báo cáo phi-AI dựng từ đúng dữ liệu tính toán
(`ADR D8`). Muốn bật diễn giải AI thật qua OpenRouter, thêm vào `.env`:

```bash
DESTINY_AI_ENABLED=true
OPENROUTER_API_KEY=<api-key-cua-ban>
DESTINY_AI_OPENROUTER_MODEL=<model-mien-phi-ban-da-xac-nhan-con-ton-tai>
```

Không có model mặc định nào được hardcode — danh mục model miễn phí của
OpenRouter thay đổi theo thời gian, phải tự xác nhận model còn khả dụng.
Gọi `POST /api/v1/calculations/{id}/narrative` sau khi đã có `calculationId`
từ một lần chạy kịch bản.

Bộ test hiện tại — 310 test:

- **bất biến miền** — trạng thái trung thực, tách `NOT_APPLICABLE` khỏi `NEUTRAL`, bảo toàn tính bất định
- **harness thực thi** — timeout, cô lập ngoại lệ, giới hạn đồng thời, thất bại một phần
- **kiến trúc** — ranh giới module, cấm phụ thuộc chéo, cấm số thực trong domain
- **phủ nhãn tiếng Việt** — mọi enum hướng tới người dùng đều có nhãn, không nhãn nào ngụ ý xác suất
- **Calendar** — bảng ví dụ tính mẫu gốc của Hồ Ngọc Đức (1983-1986), 4 năm lệch Việt/Trung có tên cụ thể (1985, 2007, 2030, 2053), quét Tết toàn bộ 1900-2100, chu kỳ Can Chi 60 năm, ranh giới giờ Tý 23:00, không suy đoán khi vùng miền chưa xác định (R14b)
- **Tarot** — đúng cấu trúc 78 lá RWS, rút bài xác định theo seed, không thiên vị khi xoay lá, không trùng lá trong một lần rút, cả 78 lá đều có nội dung diễn giải thật và sinh đúng tối đa 5 signal/lá
- **Thần số học** — golden test đối chiếu ví dụ tính mẫu từ nguồn độc lập (không tự sinh từ code), chuẩn hóa tên tiếng Việt đúng (kể cả trường hợp `đ` không phân rã Unicode), giữ số chủ đạo 11/22/33, cả 65 tổ hợp (loại số × giá trị) đều có nội dung diễn giải và sinh signal thật
- **Fusion** — đủ 14/14 test case bắt buộc của đặc tả: đếm nguồn theo engine riêng biệt (không theo signal), tín hiệu critical sống sót qua đa số, methodology conflict không bị tự động gộp
- **Scenario** — chỉ chạy engine được chính sách nêu tên, applicability chỉ thu hẹp không mở rộng, scenario chưa có chính sách thì không chạy gì cả thay vì đoán
- **Lưu trữ tính toán** — round-trip đầy đủ Calculation/Evidence/Signal/Fusion/Conflict, `result_hash` giống hệt nhau khi cùng input/version/seed/outcome và khác nhau khi bất kỳ yếu tố nào đổi
- **persistence & registry** — round-trip identity, guard "status cho phép tính toán thì bắt buộc có school/source", độ chính xác của 11 methodology đã seed đối chiếu `RESEARCH_BLOCKERS.md`, và một smoke test khởi động toàn bộ Spring context thật
- **REST API** — unit test cho từng service (dùng `StubEngine` cục bộ, không phụ thuộc engine thật), slice `@WebMvcTest` cho từng controller (status code, `ApiExceptionHandler`, methodology bị chặn nghiên cứu trả 200 chứ không phải 404), và một test tích hợp HTTP đầu-cuối chạy `TarotEngine`+`NumerologyEngine` thật qua cổng ngẫu nhiên, có ghi vào database rồi đọc lại đúng `resultHash`
- **AI Narrative** — pruning đúng thứ tự ưu tiên và ngân sách 8–20 signal (không bao giờ loại critical để lấy chỗ), prompt hệ thống chứa nguyên văn từng điều cấm của đặc tả, parser từ chối JSON hỏng/rỗng/kèm văn bản thừa, mọi nhánh lỗi provider (timeout, 429, 5xx, unavailable) đều rơi về fallback phi-AI dựng từ đúng dữ liệu tính toán — không throw, không render trống; provider OpenRouter được test bằng `MockRestServiceServer` giả HTTP (không gọi mạng thật); test tích hợp đầu-cuối gọi `POST`/`GET /api/v1/calculations/{id}/narrative` thật qua Spring context thật, AI tắt theo mặc định nên xác nhận đúng nhánh fallback thật, không phải mock

Test persistence chạy trên H2 ở chế độ tương thích PostgreSQL vì môi trường phát triển hiện không có Docker/PostgreSQL cục bộ. CI chạy thêm một job riêng đối chiếu cùng bộ test đó trên PostgreSQL thật (xem `.github/workflows/build.yml`).

---

## Lịch Việt Nam

Hệ thống mặc định dùng **phương pháp lịch Việt Nam**, không phải lịch Trung Quốc.

Quy tắc múi giờ lịch sử được mô hình hóa theo **(ngày, vùng)** — không phải theo năm. Giai đoạn 1955–1975 đặc biệt phức tạp vì hai chính thể ban hành quy định riêng. Khi không có nguồn xác minh cho một trường hợp cụ thể, hệ thống ghi nhận `RESEARCH_REQUIRED` và **giữ nguyên tính bất định đó cho tới giao diện người dùng** — thay vì âm thầm chọn một giá trị mặc định hợp lý.

Phương pháp Trung Quốc có thể được triển khai như một methodology riêng có phiên bản, dùng để đối chiếu — nhưng không bao giờ âm thầm thay thế phương pháp Việt Nam.

---

## Giấy phép

Chưa xác định.
