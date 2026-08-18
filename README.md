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
| `destiny-engine-api` | SPI `MetaphysicalEngine<I,O>` |
| `destiny-execution` | Virtual Threads, timeout, cô lập lỗi |
| `destiny-i18n` | Registry nhãn tiếng Việt |
| `destiny-persistence` | JPA + Flyway migrations + methodology registry |
| `destiny-engine-tarot` | Tarot — bộ bài RWS, rút bài có seed, tái lập được |
| `destiny-engine-numerology` | Thần số học Pythagoras — 5 chỉ số, chuẩn hóa tên tiếng Việt |
| `destiny-fusion` | Tổng hợp kết luận theo luật (không phải trung bình có trọng số) |
| `destiny-scenario` | Điều phối kịch bản — chọn engine áp dụng, không phụ thuộc engine cụ thể |
| `destiny-app` | Spring Boot assembly + bộ test kiến trúc |

Hai ràng buộc kiến trúc được **kiểm tra tự động bằng ArchUnit**, không chỉ ghi trong tài liệu:

- **Fusion không phụ thuộc bất kỳ engine nào** — chỉ phụ thuộc contract `Signal`. Đây là tính chất cho phép xây Fusion trước khi có Bát Tự.
- **Không kiểu miền nào chứa `double`/`float`/`BigDecimal`** làm điểm số hay độ tin cậy. Trung bình có trọng số trở thành *không biểu diễn được*, chứ không chỉ là "không khuyến khích".

---

## Trạng thái hiện tại

**V4-V6 — lưu trữ tính toán.** Toàn bộ pipeline MVP chạy được từ đầu đến cuối **và kết quả được lưu lại thật sự**: chọn engine theo kịch bản → chạy song song → tổng hợp theo luật → ghi vào database trong một transaction. Chưa có nội dung diễn giải tiếng Việt và chưa có API/UI.

| Phase | Nội dung | Trạng thái |
|---|---|---|
| 0 | Kiểm toán kiến trúc | Xong |
| 1 | Nền tảng: domain, SPI, harness | Xong |
| 2 | Database + methodology registry | Xong |
| 3 | Nền tảng lịch | Chờ — xem ghi chú bên dưới |
| 4 | Thần số học (Pythagoras) | Xong — 5 chỉ số; Chaldean vẫn chặn (không có nguồn) |
| 5 | Tarot | Xong — cấu trúc + rút bài; nội dung ý nghĩa còn thiếu |
| 6 | Fusion | **Xong** — đủ 14/14 test case bắt buộc theo đặc tả |
| 7 | Scenario | Xong — 2/10 scenario có chính sách thật (BUSINESS, DAILY_ACTION), 8 còn lại đăng ký nhưng chưa có chính sách |
| — | Lưu trữ Calculation/Evidence/Signal/Fusion (V4-V6) | **Xong** — `CalculationRecorder`, `result_hash` tái lập được |
| 8–11 | Bát Tự, Tử Vi, Phong Thủy, Chiêm tinh | Chờ nghiên cứu |

**Cập nhật nghiên cứu lịch:** sau nhiều vòng tra cứu, 4/6 mục (thuật toán tiết khí/điểm sóc/tháng nhuận, và quy tắc múi giờ lịch sử theo ngày) đã có nguồn trích dẫn cụ thể (Jean Meeus 1998; Công Báo Việt Nam với số quyển/trang) và được nâng lên `DECISION_REQUIRED`. Riêng **ranh giới địa lý Bắc/Nam** giai đoạn 1955–1975 vẫn `RESEARCH_REQUIRED` — không có nguồn nào cho phần này. Chi tiết ở `docs/RESEARCH_BLOCKERS.md` (không push lên git).

11 methodology đã được đăng ký vào registry với trạng thái thật (đối chiếu `docs/RESEARCH_BLOCKERS.md`): 1 ở trạng thái `CONTENT_REQUIRED` (Tarot — thuật toán xong, thiếu nội dung tiếng Việt), còn lại `RESEARCH_REQUIRED`/`DECISION_REQUIRED`/`OUT_OF_SCOPE`.

Các engine chưa triển khai **vẫn được đăng ký và hiển thị**, kèm lý do — thay vì bị ẩn đi. Người dùng nhìn thấy `Chưa triển khai` hoặc `Cần xác minh thuật toán`, không phải một câu trả lời tự tin nhưng sai.

---

## Build

Yêu cầu JDK 21 trở lên và Maven 3.9+.

```bash
mvn verify
```

Bộ test hiện tại — 151 test:

- **bất biến miền** — trạng thái trung thực, tách `NOT_APPLICABLE` khỏi `NEUTRAL`, bảo toàn tính bất định
- **harness thực thi** — timeout, cô lập ngoại lệ, giới hạn đồng thời, thất bại một phần
- **kiến trúc** — ranh giới module, cấm phụ thuộc chéo, cấm số thực trong domain
- **phủ nhãn tiếng Việt** — mọi enum hướng tới người dùng đều có nhãn, không nhãn nào ngụ ý xác suất
- **Tarot** — đúng cấu trúc 78 lá RWS, rút bài xác định theo seed, không thiên vị khi xoay lá, không trùng lá trong một lần rút
- **Thần số học** — golden test đối chiếu ví dụ tính mẫu từ nguồn độc lập (không tự sinh từ code), chuẩn hóa tên tiếng Việt đúng (kể cả trường hợp `đ` không phân rã Unicode), giữ số chủ đạo 11/22/33
- **Fusion** — đủ 14/14 test case bắt buộc của đặc tả: đếm nguồn theo engine riêng biệt (không theo signal), tín hiệu critical sống sót qua đa số, methodology conflict không bị tự động gộp
- **Scenario** — chỉ chạy engine được chính sách nêu tên, applicability chỉ thu hẹp không mở rộng, scenario chưa có chính sách thì không chạy gì cả thay vì đoán
- **Lưu trữ tính toán** — round-trip đầy đủ Calculation/Evidence/Signal/Fusion/Conflict, `result_hash` giống hệt nhau khi cùng input/version/seed/outcome và khác nhau khi bất kỳ yếu tố nào đổi
- **persistence & registry** — round-trip identity, guard "status cho phép tính toán thì bắt buộc có school/source", độ chính xác của 11 methodology đã seed đối chiếu `RESEARCH_BLOCKERS.md`, và một smoke test khởi động toàn bộ Spring context thật

Test persistence chạy trên H2 ở chế độ tương thích PostgreSQL vì môi trường phát triển hiện không có Docker/PostgreSQL cục bộ. CI chạy thêm một job riêng đối chiếu cùng bộ test đó trên PostgreSQL thật (xem `.github/workflows/build.yml`).

---

## Lịch Việt Nam

Hệ thống mặc định dùng **phương pháp lịch Việt Nam**, không phải lịch Trung Quốc.

Quy tắc múi giờ lịch sử được mô hình hóa theo **(ngày, vùng)** — không phải theo năm. Giai đoạn 1955–1975 đặc biệt phức tạp vì hai chính thể ban hành quy định riêng. Khi không có nguồn xác minh cho một trường hợp cụ thể, hệ thống ghi nhận `RESEARCH_REQUIRED` và **giữ nguyên tính bất định đó cho tới giao diện người dùng** — thay vì âm thầm chọn một giá trị mặc định hợp lý.

Phương pháp Trung Quốc có thể được triển khai như một methodology riêng có phiên bản, dùng để đối chiếu — nhưng không bao giờ âm thầm thay thế phương pháp Việt Nam.

---

## Giấy phép

Chưa xác định.
