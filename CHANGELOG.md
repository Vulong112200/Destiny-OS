# Changelog

Mọi thay đổi về thuật toán, methodology hoặc phiên bản rule đều phải được ghi ở đây
(CLAUDE_CODE_WORKFLOW §9). Một golden test chỉ được cập nhật lại kèm một mục ở đây
giải thích vì sao kết quả thay đổi.

Định dạng theo [Keep a Changelog](https://keepachangelog.com/).

## [Unreleased]

### Nghiên cứu — R4 (Tử Vi), vòng thứ hai cùng ngày: thêm ba mục đóng, một mâu thuẫn giả được gỡ, một phát hiện mới cần tách bạch

Tiếp tục vòng vừa xong (đọc chính văn 紫微斗數全書 qua Wikisource/`r.jina.ai`).

**Ba mục trước đây ghi là thiếu, nay có nguồn gốc trực tiếp:**

1. **Khẩu quyết an đủ 14 chính tinh** — 「紫微天機逆行旁，隔一陽武天同當…」. Không phải phát hiện mới về nội dung — đây chính là khẩu quyết cho đúng cách sắp xếp (vòng Tử Vi cách 1/cách 2/cách 3 cung, vòng Thiên Phủ liền 7 sao rồi cách 3) mà dự án đã **kiểm chứng bằng tính toán** ở vòng trước (`VERIFICATION_OPUS.md` §A3). Giờ thuật toán và chính văn xác nhận lẫn nhau từ hai hướng độc lập
2. **Công thức an Văn Xương/Văn Khúc** — trước đây R4b ghi rõ "chưa lấy được công thức cụ thể" cho Văn Xương. Nay có: 「子時戌上起文昌，逆到生時是貴鄉，文曲數從辰上起，順到生時是本鄉」 — Văn Xương khởi cung Tuất tại giờ Tý, đếm nghịch tới giờ sinh; Văn Khúc khởi cung Thìn tại giờ Tý, đếm thuận
3. **Toàn bộ 60 câu Nạp Âm Giáp Tý** — trước đây cả R1 và R4a đều ghi thiếu bảng này. Lấy được đủ 60 cặp qua proxy sau khi lần fetch đầu (qua Wikisource trực tiếp) bị chặn bởi chính bước tóm tắt tự nghi ngờ bản quyền của nó. **Tự bắt được 2 lỗi OCR** bằng cách đối chiếu bảng với chính cấu trúc chu kỳ 60 của nó (không cần nguồn ngoài): "甲戊乙亥" phải là 甲戌乙亥, "戊戌已亥" phải là 戊戌己亥 — cả hai đều là các cặp chữ Hán rất dễ lẫn khi OCR (戊/戌 lệch một nét, 已/己 lệch một nét chưa khép)

**Một mâu thuẫn tưởng có, hóa ra là lỗi tóm tắt:** lần fetch đầu về Đại Hạn đọc như thể vận đầu tiên khởi ở cung Phụ Mẫu/Huynh Đệ, trái với công thức R4b đã tìm (khởi tại chính cung Mệnh). Đối chiếu nguồn thứ hai cho thấy câu gốc nói **vận thứ hai** mới vào Phụ Mẫu/Huynh Đệ — Đại Hạn 1 vẫn là cung Mệnh, đúng như R4b. Ghi lại đúng loại lỗi này vì chữ Hán cổ không dấu câu rất dễ đọc sai theo hướng này, và nó lẽ ra sẽ bị ghi sai vào tài liệu nếu không kiểm tra chéo lần hai.

**Phát hiện mới, cố tình KHÔNG gộp vào các mục trên:** bản 紫微斗數 trên `ctext.org` (thuộc Đạo Tạng — 正統道藏) dùng **một tập tên sao hoàn toàn khác** — 虛, 實, 印, 壽, 空, 鸞, 庫, 貫, 文, 福, 祿 và 天杖, 異, 毛, 刃 — không trùng bất kỳ sao nào trong 14 chính tinh chuẩn (Tử Vi, Thiên Cơ… Phá Quân) mà Wikisource, `iztro`, và mọi nguồn tiếng Việt đều hội tụ về. Nhiều khả năng đây là một truyền thống văn bản khác, chỉ trùng tên thể loại "Tử Vi Đẩu Số" chứ không phải một dị bản của cùng một hệ thống. Ghi lại để vòng sau không phát hiện lại từ đầu, và để không ai nhầm hai hệ là một nếu tra lại bản Đạo Tạng.

**卷四 (chứa lá số ví dụ 進士之命) không tìm thấy ở cả hai bản đã khảo sát** — không có trong mục lục Wikisource, không có trong bản Đạo Tạng 3 quyển trên ctext.org. Có thể thuộc một bản in chú giải hiện đại cụ thể (ví dụ bản Vương Đình Chi). Lấy được quyển này vẫn là bước giá trị nhất tiếp theo cho R4 — không đổi so với vòng trước.

**R4 không đổi trạng thái** (`RESEARCH_REQUIRED`, CRITICAL) — đây là thu hẹp phạm vi thiếu, không phải đóng mục.


### Nghiên cứu — R4 (Tử Vi): lần đầu đọc được chính văn cổ thư, và một kết luận bị điều chỉnh

Các vòng trước chỉ đọc được nguồn thứ cấp hiện đại vì cổ thư nằm sau chặn bot.
Vòng này mở được hai đường, ghi lại để lần sau bắt đầu từ đây:

- **`zh.wikisource.org` có toàn văn 紫微斗數全書** và fetch trực tiếp được. Có
  卷一, 卷二, 卷三 — **không có 卷四**, điều này quan trọng ở dưới
- **`ctext.org` chặn fetch (403) nhưng đọc được qua proxy `r.jina.ai`** — đây là
  cách 三命通會, 滴天髓闡微 và 命理探源 được tiếp cận cho R1/R3

**Xác nhận được từ chính văn (卷二, 安身命例):**

1. **Công thức an Mệnh/Thân** — và đoạn văn tự chứa **ba ví dụ** của chính nó
   (tháng 1 với giờ Tý, Sửu, Dần). Cả ba tái hiện đúng dưới công thức mà dự án
   đã dựng trước đó từ nguồn thứ cấp — **3/3**
2. **Chuỗi dẫn xuất Cục số**: Ngũ Hổ Độn cho Can của cung Mệnh (năm 甲 → 丙寅),
   Nạp Âm của cặp can-chi cho hành (丙寅丁卯 = 炉中火), hành đó là Cục. Lấy được
   từ nguồn, không phải suy ra
3. **Ngũ Hổ Độn chính là quy tắc dự án đã cài** cho Trụ Tháng Bát Tự và đã
   golden-test — không cần cài lại lần hai
4. **Hỏa lục cục, ngày 1 → cung Dậu**, xác nhận độc lập một neo của khẩu quyết
   「六五四三二，酉午亥辰丑」, mà cả năm neo đã được chứng minh riêng là rơi ra
   đúng từ thuật toán an Tử Vi (`VERIFICATION_OPUS.md` §A3)

**Điều chỉnh — kết luận tháng nhuận của vòng thu thập là quá mạnh.** Trích dẫn
「又若閏正月生者，要在二月內起安身命」 là **thật** (lấy lại độc lập), nhưng nó
**không** đóng được tranh cãi:

- **Một cổ thư khác cho quy tắc khác.** 斗數宣微: chia ở ngày 15 (「十五日以前，
  作正月看；十五日以後，作二月看」). Nghĩa là quy ước "1-15 / 16-30" **cũng là cổ
  điển** — thư viện `iztro` có triển khai nó, nhưng nó không bắt nguồn từ đó
- **Chính 紫微斗數全書 tự mâu thuẫn bằng ví dụ của mình.** 卷四 có lá số 進士之命,
  sinh 丙申年閏十二月初十日亥時, lập **theo tháng 12** chứ không theo tháng Giêng
  năm sau. Hai nguồn độc lập xác nhận
- **Ca 閏十二月 cho thấy vì sao.** Lấy "tháng sau" ở đó sẽ đẩy sang năm mới, đổi
  **Trụ Năm** và do đó đổi Tứ Hóa — hệ quả không cổ thư nào bàn tới, tự nó là
  bằng chứng quy tắc không được hiểu theo nghĩa đen

**Tác động thực:** câu hỏi tháng nhuận đi từ "năm ý kiến trên diễn đàn" thành
**ba lập trường, hai có nguồn cổ điển đích danh, cộng một mâu thuẫn nội tại có
thật trong chính văn**. Tiến bộ thật, và vẫn **không phải lời giải** — chọn giữa
chúng vẫn là một lựa chọn trường phái theo Rule D.

**Vẫn mở, và vì sao R4 vẫn CRITICAL:** bảng an Tử Vi 30×5 chưa đối chiếu với bảng
công bố nào; 13 chính tinh còn lại mới chỉ kiểm với nguồn thứ cấp và code
`iztro`; sao phụ chưa có nguồn nào liệt kê một tập đầy đủ có giới hạn (卷一 thuần
luận giải, **không có** khẩu quyết an sao nào); Tứ Hóa có hàng Canh tranh chấp
thật; công thức Đại Hạn không có trong 卷一–卷三. **Lấy được 卷四 là bước tiếp
theo giá trị nhất** — đó là nơi có lá số ví dụ, đúng thứ R4 cần để viết golden
test.


### Fixed — ba thành phần Bát Tự vô hình: mở R20, R21, R22

Một lượt rà soát đối chiếu **Master Spec §13** với `BaziEngine` phát hiện ba
thành phần được đặc tả tường minh nhưng **chưa từng được gán mã nghiên cứu**:

| Master Spec §13 | Trạng thái trước rà soát |
|---|---|
| "combinations/clashes/harm/punishment/break" | không có mã R |
| "Liu Nian; Liu Yue; Liu Ri" | không có mã R |
| "Shen Sha nếu methodology hỗ trợ" | không có mã R |

**Vì sao đây là lỗi nặng hơn một khoảng trống thông thường.** Dụng Thần (R1) và
cường độ Nhật Chủ (R3) *bị chặn nhưng nhìn thấy được*: engine trả về
`BlockedSection` kèm lý do và các trường phái đang khác nhau, và giao diện hiển
thị "Dụng Thần — cần xác minh thuật toán (R1)". Ba mục trên thì **không có gì để
báo cả** — không mã nghiên cứu nghĩa là không `BlockedSection`, nghĩa là người
đọc một lá số Tứ Trụ hôm nay không có cách nào biết chúng còn thiếu. Một khoảng
trống được thừa nhận thì người dùng còn biết đường hỏi; một khoảng trống vô hình
thì không.

Ba mục mới, mỗi mục trung thực về mức độ đã điều tra:

- **R20 — Hợp/Xung/Hình/Hại/Phá** (`RESEARCH_REQUIRED`, HIGH). Một vòng tìm kiếm
  đủ xác nhận đây là **phân kỳ trường phái thật**, chưa đủ để đặc tả: các nguồn
  nói thẳng "学派观点有分歧" về việc hợp có giải được xung không; khoảng cách giữa
  các trụ (紧贴 vs 远隔) có tính hay không; và thứ tự ưu tiên khi nhiều quan hệ
  cùng xuất hiện. **Đây là mục hệ trọng nhất trong ba**: nó không phải phần thêm
  vào mà là tầng thay đổi *cách đọc chính dữ liệu đang hiển thị* — một chi bị
  xung có thể coi như mất gốc, ảnh hưởng trực tiếp tới bảng đếm Ngũ Hành và tới
  R3. Nó cũng chặn R3 theo đa số trường phái
- **R21 — Lưu Niên/Lưu Nguyệt/Lưu Nhật** (`RESEARCH_REQUIRED`, MEDIUM). Chưa
  điều tra. Trở nên lộ rõ **chính vì R2 vừa đóng**: Đại Vận cho biết giai đoạn 10
  năm, và người dùng được báo "vận thứ ba bắt đầu lúc 28 tuổi" sẽ hỏi ngay *năm
  nay* thì sao. Phụ thuộc R20 (tương tác đi qua đúng các quan hệ đó) và R1/R3
  (một năm tốt hay xấu là phán định Dụng Thần)
- **R22 — Thần Sát** (`RESEARCH_REQUIRED`, LOW). Chưa điều tra, và **điều kiện
  trong Master Spec được giữ nguyên chứ không làm phẳng**: bản đặc tả ghi "nếu
  methodology hỗ trợ", nên câu hỏi đầu tiên là *có dùng hay không* — một số phái
  dùng nhiều, một số coi là phần thêm về sau. Kết luận "không dùng" cũng là một
  cách đóng hợp lệ, nên mục này có thể `NOT_APPLICABLE` chứ không nhất thiết cần
  một thuật toán

`BaziEngine` giờ trả về **5 `BlockedSection`** (trước: 2), mỗi mục kèm cảnh báo
critical nên pruning của lớp AI không cắt được.

### Added — test bắt đúng loại lỗi này, không chỉ sửa trường hợp cụ thể

`BaziEngineTest.everySpecifiedComponentIsAccountedFor` liệt kê **từng thành phần
Master Spec §13 nêu tên** và khẳng định mỗi cái *hoặc* được tính *hoặc* được đặt
tên là blocked. Trước đây danh sách blocked chỉ được đối chiếu **với chính nó**,
nên ba mục thiếu không làm hỏng bất cứ test nào. Đây là lưới an toàn cho lần sau,
không chỉ là bản vá cho lần này.

### Fixed — hai hàm hạ tầng lịch chỉ được kiểm gián tiếp

`CanChi.monthPillarOffset` và `SolarYear.solarMonthStartJulianDate` /
`nextSolarMonthStartJulianDate` được thêm cho Đại Vận (R2) và ban đầu **chỉ được
kiểm qua hai golden vector ở `destiny-engine-bazi`**. Sai chỗ: mọi phép tính lịch
khác (`SolarTermInstantTest`, `HiddenStemsTest`, `CanChiAttributesTest`) đều được
chứng minh ở đúng tầng của nó, và một test ở tầng engine không phân biệt được
"tìm ranh giới sai" với "phép quy đổi tiêu thụ nó sai".

- **`SolarYearTest` (10, mới)** — bất biến cấu trúc thay vì chép lại golden
  vector của R2: **cả 12 ranh giới đều là Tiết (節), không ranh giới nào là Trung
  Khí** (khẳng định trụ cột của R2, kiểm bằng chẵn/lẻ ordinal trong `SolarTerm`);
  tập 12 mốc tìm được đúng bằng 12 Tiết có tên; mỗi tháng dài 29–32 ngày (bắt lỗi
  tìm trùng ranh giới hoặc nhảy cóc); ranh giới nối nhau không hở; ranh giới
  thuộc về tháng nó *mở ra*, không phải tháng nó kết thúc. Cộng **ca wraparound**
  — tháng Mão trải 345°→15°, cắt qua điểm gián đoạn 360°/0°, là chỗ lỗi chuẩn hóa
  góc lộ ra và gần như không lộ ở đâu khác
- **`CanChiTest.MonthPillarOffset` (7, mới)** — bước 0/±1; đi qua ranh giới năm
  (tháng 12 + 1 bước = tháng 1 năm sau, **không** quay về tháng 1 cùng năm —
  chính lý do method này tồn tại thay vì truyền tháng ngoài khoảng vào
  `monthPillar`); chu kỳ 60; 12 bước giữ nguyên chi nhưng đổi can; và 8 bước liên
  tiếp đôi một khác nhau (nếu trùng thì hai vận mang cùng một trụ)

### Changed

- `MethodologyRegistrySeeder`: `BAZI` lên `1.3`, research ids từ `{R1, R3}` thành
  `{R1, R3, R20, R21, R22}`

### Tests

532 test (tăng từ 514).


### Added — Phase 7: 7 chính sách scenario mới, từ bằng chứng thực hành truyền thống

Trước thay đổi này, chỉ 2/10 scenario (BUSINESS, DAILY_ACTION) có chính sách
áp dụng thật — tám scenario còn lại đăng ký nhưng `policyDefined == false`,
đúng như CHANGELOG mục "Rejected: deriving the eight missing scenario
policies" (2026-08-22) đã ghi: suy dimension engine tự khai thất bại chính
bài test của nó, và việc gán chính sách là **quyết định sản phẩm**, không
phải bài toán suy luận.

Chủ dự án quyết định (2026-08-23) sau khi xem báo cáo thu thập bằng chứng
thực hành truyền thống theo từng cặp scenario × engine
(`docs/research_drafts/scenario_scope_reference.md`):

- **CAREER, FINANCE, RELATIONSHIP** — Thập Thần Quan/Tài (Bát Tự), cung Quan
  Lộc/Tài Bạch/Phu Thê (Tử Vi), Nhà 10/2&8/7 (Chiêm tinh) đều là nhánh kinh
  điển có tên riêng. FENGSHUI_KUA chỉ MEDIUM ở FINANCE (góc tài lộc là thực
  hành phổ biến thật) và **vắng mặt hẳn** ở RELATIONSHIP (không tìm được
  nhánh nào — Đào Hoa vị thuộc một phương pháp Phong Thủy khác, không phải
  Bát Trạch)
- **PURCHASE, TRAVEL** — cung Điền Trạch/Thiên Di (Tử Vi) và hướng nhà/hướng
  xuất hành theo Kua (Phong Thủy Bát Trạch, đúng mục đích gốc của phương
  pháp) là nhánh mạnh nhất. BAZI/TAROT/NUMEROLOGY **vắng mặt** ở cả hai —
  không tìm được nhánh nào phân biệt việc mua sắm/di chuyển với các chủ đề
  khác
- **PROJECT** — không nguồn nào phân biệt "một dự án cụ thể" với "mở rộng
  kinh doanh" trong thực hành chọn ngày khởi sự truyền thống. Thay vì bịa
  một tiêu chí riêng, dùng **đúng bộ engine của BUSINESS, hạ một bậc mỗi
  engine** — phản ánh quy mô nhỏ hơn mà không tuyên bố một loại liên quan
  khác đã được tìm thấy
- **GENERAL_DECISION** — chỉ TAROT đạt HIGH (trải 3 lá Quá khứ-Hiện tại-
  Tương lai là spread cổ điển nhất, dùng đúng cho câu hỏi mở). BAZI/ASTROLOGY
  giữ LOW (là hệ lá số trọn đời, không có cơ chế truyền thống trả lời một
  câu hỏi tại một thời điểm) chứ không nâng lên MEDIUM — không có bằng chứng
  nào biện minh cho việc đó
- **COMPATIBILITY cố ý vẫn để `policyDefined = false`.** Đây là scenario có
  bằng chứng truyền thống **mạnh nhất** trong cả 8 (Bát Tự hợp hôn, Tử Vi
  xem tuổi, Chiêm tinh synastry đều có tên riêng, quy trình rõ) — nhưng cả
  ba đều cần **hai lá số**, còn `ScenarioDefinition`/mọi input engine hiện
  tại chỉ nhận một. Khai chính sách ở đây sẽ nói sai về việc **hệ thống làm
  được gì hôm nay**, không chỉ về việc truyền thống khuyến nghị gì

**Nguyên tắc xuyên suốt:** một engine vắng mặt trong một scenario nghĩa là
vòng nghiên cứu **không tìm được nhánh truyền thống nào** nối engine đó với
scenario đó — không phải bị quên, và không được gán `LOW` cho có (Rule C:
không có bằng chứng thì bỏ, không đoán).

### Added — R5 (ephemeris) và R6 (quy ước chiêm tinh): quyết định hướng cho Phase 11

**R5 chốt: tự xây trên nền Meeus/VSOP87**, không dùng Swiss Ephemeris (miễn
phí AGPL hay trả phí Professional License). `destiny-calendar/SolarPosition`
đã là một cài đặt Meeus có trích dẫn, đối chiếu chéo, golden-test; cùng cuốn
sách (*Astronomical Algorithms*, 1998) có chương vị trí hành tinh (ch. 32,
VSOP87 rút gọn) và Mặt Trăng (ch. 47). Độ chính xác không phải tiêu chí phân
biệt — giới hạn ~0,01° hiện có dư ~100 lần so với orb hẹp nhất của chiêm
tinh (xem `docs/research_drafts/R5_meeus_path_survey.md`). Việc còn lại
trước khi viết engine: dùng dữ liệu VSOP87 gốc từ IMCCE (không chép bảng rút
gọn có bản quyền trong sách Meeus), và ước lượng công sức ELP-2000 (Mặt
Trăng).

**R6 chốt một phần: Tropical + Whole Sign.** Tropical khớp đúng tên
methodology "Western Astrology" đã đăng ký, không cần ayanamsa. Whole Sign
được chọn cho bản đầu vì lý do kỹ thuật, không phải vì phổ biến nhất
(Placidus mới là mặc định của đa số phần mềm, ~45-70% thị phần tùy khảo
sát): Whole Sign chỉ cần Ascendant nên tính đúng ở **mọi vĩ độ** kể cả vòng
cực, nơi Placidus/Koch không xác định được về mặt toán học. Bộ aspect/orb
vẫn `DECISION_REQUIRED`. `houseSystem` bắt buộc versioned theo từng lá số
(Master Spec §15) nên thêm Placidus ở bản sau không xáo trộn bản này.

**Cả hai vẫn đang ở mức quyết định hướng, chưa implement.** Phase 11 (Chiêm
tinh phương Tây) chưa có dòng code nào — R5/R6 mở khóa việc bắt đầu viết,
không phải bản thân việc viết.

### Nghiên cứu — R4 (Tử Vi): đọc được nguyên văn cổ thư lần đầu

Tìm ra cách đọc trực tiếp *Tử Vi Đẩu Số Toàn Thư* qua Wikisource (không cần
proxy) và *Tam Mệnh Thông Hội*/*Trích Thiên Tủy Xiển Vi* qua `ctext.org`
(chặn bot trực tiếp, đọc được qua proxy `r.jina.ai`). Lần đầu dự án đọc được
nguyên văn cổ thư thay vì chỉ nguồn thứ cấp trên web.

Tìm được câu trả lời **có nguồn gốc trực tiếp** cho tháng nhuận (một trong 5
quan điểm mà nghiên cứu trước chỉ có qua một bài diễn đàn không dẫn nguồn):
"又若闰月正月生者要在二月内起安身命，凡有闰月具要依此为例" — lấy tháng sau. Trích
dẫn được xác nhận hai lần độc lập, và tự kiểm chứng đúng 3/3 với ví dụ ngay
trong đoạn văn. R3 có thêm nguồn cổ điển thứ ba (`命理探源`) minh thị nói
"không có công thức cứng" — củng cố chứ không đảo ngược kết luận đã có.

**Chưa đóng R4.** Đây là một phần nhỏ (chính sách tháng nhuận) trong một mục
lớn (bảng Tứ Hóa, tập sao phụ, công thức Đại Hạn vẫn thiếu). Ghi ở
`docs/research_drafts/R4_primary_source_breakthrough.md`, cố tình chưa qua
xác minh Opus, chưa động vào code.

### Added — UI

- Trung tâm quyết định hiển thị đủ **9 scenario** có chính sách (trước đây
  chỉ 2) — lưới 2-3 cột thay cho hàng ngang 2 nút. COMPATIBILITY không xuất
  hiện, kèm giải thích ngắn vì sao (vướng kiến trúc một-lá-số)

### Tests

514 test (tăng từ 493):

- **`ScenarioRegistryTest` (21, mới)** — khóa **chính xác** từng ô của cả 7
  chính sách mới, không chỉ kiểm "có chứa" — một thay đổi vô tình ở một ô sẽ
  làm hỏng đúng test đặt tên ô đó. Cộng: PROJECT thấp hơn BUSINESS đúng một
  bậc ở **mọi** engine chung; FINANCE's FENGSHUI_KUA cao hơn CAREER's (góc
  tài lộc là thực hành thật, quan hệ sự nghiệp thì gián tiếp); mọi chính
  sách đã định nghĩa đều có ít nhất một engine (chính sách 0-engine là lỗi,
  không phải trạng thái hợp lệ)


### Added — Đại Vận (R2 đóng): chuỗi vận 10 năm trong `destiny-engine-bazi`

R2 là mục nghiên cứu **đầu tiên đóng lại bằng xác minh thuần túy** — không chọn
trường phái nào, vì không nguồn nào bất đồng. Đây cũng là lần đầu quy trình
"model nhỏ thu thập, model lớn xác minh" được chạy theo yêu cầu của chủ dự án, và
nó lập tức chứng minh giá trị: **ba lỗi trong tài liệu thu thập bị bắt**, trong đó
một lỗi kết luận *ngược hẳn* (xem `DECISION_LOG.md`).

**Bốn mục con của R2 và cách từng mục đóng:**

- **Chiều thuận/nghịch** — nhất trí tuyệt đối giữa mọi nguồn. Khóa theo can năm
  **Bát Tự** chứ không phải năm dương lịch: dưới quy ước Lập Xuân (R18), một ca
  sinh tháng Giêng mang can của năm trước, và lấy sai sẽ lật chiều đúng ở những
  ca R18 vốn đã đánh dấu
- **Đếm tới mốc nào** — **không cần chọn**. Mười hai mốc mà `SolarYear` đang dùng
  làm ranh giới tháng (315° + 30k) *chính là* 12 Tiết, rời hẳn 12 Trung Khí mà
  `SolarTerm` đã chú thích sẵn. Mọi lựa chọn khác sẽ mâu thuẫn với Trụ Tháng đang
  được golden-test. Nguồn nói cùng điều đó, nhưng ở đây nguồn chỉ là xác nhận
- **Tỉ lệ quy đổi** — 3 ngày = 1 năm là một **tỉ lệ liên tục chính xác**, không
  phải một quy ước cạnh tranh: 1 ngày = 4 tháng, 1 canh giờ = 10 ngày, 1 giờ = 5
  ngày, 12 phút = 1 ngày đều là *cùng một hằng số* phát biểu lại (720 giây khoảng
  cách cho một ngày tuổi). `LuckCycleResolver` tính bằng **số nguyên giây**, nên
  không có số thực nào lọt vào domain (ADR D6)
- **Làm tròn — mâu thuẫn được báo cáo đã không đứng vững.** Sáu ví dụ có đáp án
  đã công bố **đều** dùng phép quy đổi chính xác; **không ví dụ nào** dùng một
  quy tắc làm tròn. Các mô tả làm tròn chỉ xuất hiện trong *văn xuôi*. Vì vậy
  chúng được coi là cách **nói tuổi khởi vận thành số nguyên** — tức trình bày —
  và cố ý không cài vào engine, nơi nó sẽ không thể lấy lại được

**Golden vector đầu-cuối, cả hai chiều, từ nguồn ngoài dự án:**

- **Nghịch** — `btime`, 1990-01-01 11:10, nam, 己巳 丙子 丙寅: dự án tính ra đúng
  ba trụ đó, khoảng cách **24,9959 ngày** (nguồn: 25) và tuổi khởi vận **8 năm 4
  tháng** (đúng nguồn). Ca này còn kích hoạt quy tắc năm Lập Xuân
- **Thuận** — `k366`, âm 17/1/1994 giờ Dần, nam Giáp Tuất: **7,9788 ngày**
  (nguồn: 8) → **2 năm 7,92 tháng** (nguồn: 2 năm 8 tháng)

**Một ví dụ KHÔNG tái lập được, ghi lại chứ không lặng lẽ bỏ:** ca 1934 mà vòng
thu thập lấy đầu tiên — ca sinh rơi *sau* Đại Tuyết khoảng một giờ, thuận ra 29,4
ngày, nghịch ra 0,045 ngày. Số học nội tại của nó vẫn dùng làm test quy đổi;
phần thiên văn thì không.

**Không có cơ chế bảo vệ mới nào được thêm.** Xa ranh giới Tiết, sai số R19 chỉ
đáng ~1,3 ngày tuổi khởi vận. Sát ranh giới thì thảm khốc *theo đúng cơ chế* của
Trụ Tháng — lật phía là khoảng cách nhảy từ ~0 sang ~30 ngày. Đó là cùng một hiện
tượng mà cửa sổ 40 phút `SOLAR_TERM_BOUNDARY` đã canh, nên Đại Vận **dùng lại** cờ
đó thay vì định nghĩa ngưỡng thứ hai.

### Changed

- **`Gender` chuyển từ `destiny-engine-fengshui` sang `destiny-core`** — Bát Tự
  cần đúng khái niệm đó, mà một engine không được phụ thuộc engine khác
  (`enginesStayIndependent`). Cùng nước đi `SolarYear` đã làm, cùng lý do
- **Giới tính là *tùy chọn* với Bát Tự và *bắt buộc* với Phong Thủy** — có chủ
  đích. Ở Phong Thủy, cung phi **là** kết quả; ở Bát Tự nó chỉ chi phối một mục,
  nên thiếu giới tính thì mất Đại Vận và **giữ nguyên** lá số. Không bao giờ có
  giá trị mặc định: một chiều đoán sẽ chạy ngược toàn bộ chuỗi mà trông vẫn đúng
- `UncertaintyKind.REQUIRED_INPUT_MISSING` (mới) — khác `METHODOLOGY_UNRESOLVED`
  (ở đó phương pháp mới là thứ đáng ngờ) và khác `INVALID_INPUT` (ở đó cả phép
  tính bị từ chối). Ở đây phương pháp đã chốt, chỉ là người gọi chưa cung cấp thứ
  nó cần, và mọi phần không cần trường đó vẫn được giữ
- `CanChi.monthPillarOffset` (mới) — Đại Vận là một bước đi dọc chuỗi trụ tháng.
  Viết thành method thay vì truyền tháng ngoài khoảng 1-12 vào `monthPillar`:
  cách đó *tình cờ* chạy đúng, nhưng dựa vào một tính chất không được ghi trong
  hợp đồng của method
- `SolarYear.solarMonthStartJulianDate` / `nextSolarMonthStartJulianDate` (mới) —
  root-finding bằng bisection trên chính chuỗi Meeus của dự án, nên thừa hưởng
  đúng giới hạn R19 và tái lập được (số bước cố định, không phải ngưỡng hội tụ)
- `MethodologyRegistrySeeder`: `BAZI_TUBINH_CHART` lên `1.1` và đổi tên thành
  "Lập lá số Tứ Trụ và Đại Vận"; `BAZI` lên `1.2`, bỏ R2, đổi tên thành "Luận
  giải (Dụng Thần, cường độ Nhật Chủ)"
- `BaziRequest` thêm trường thứ năm `gender` (đổi arity của record)

### Added — UI

- **`BaziChartCard`** thêm bảng Đại Vận dựng *từ evidence*: chiều vận, tuổi khởi
  vận dạng năm–tháng–ngày, tiết được đếm tới, khoảng cách bằng ngày (con số duy
  nhất người đọc kiểm tay được với sách), và 8 vận kèm tuổi + ngày bắt đầu.
  **Cố ý không có cột đánh giá** — nêu rõ trên giao diện rằng một vận tốt hay xấu
  cần R1/R3
- Ô chọn giới tính trong Trung tâm quyết định, **không có giá trị chọn sẵn**, kèm
  giải thích rằng để trống thì lá số vẫn đầy đủ, chỉ không có Đại Vận

### Nghiên cứu — R1, R3 xác nhận đang bị chặn ĐÚNG; R5 thu hẹp

- **R3 giờ có bằng chứng dương tính, không chỉ là "chưa tìm ra"**: Trích Thiên Tủy
  *minh thị bác bỏ* việc coi vượng/nhược là nhị phân cứng, trong khi mọi thang
  điểm tìm được đều hiện đại, không dẫn nguồn và mâu thuẫn nhau. Hệ quả then chốt:
  lối thoát "tính cả hai, báo cả hai" của R7/R18 **không dùng được** ở đây, vì
  không có đáp án thứ hai nào đủ xác định để tính. R1 phụ thuộc R3 nên không đi
  trước được
- **R5 thu hẹp, chưa đóng**: điều khoản Swiss Ephemeris nói thẳng nghĩa vụ kích
  hoạt "before any public service … is activated", nên câu hỏi pháp lý bỏ ngỏ
  không cần diễn giải AGPLv3 §13 nữa. Và có một lựa chọn thứ tư mà báo cáo gốc
  không thấy vì không được đọc codebase: `SolarPosition` đã là một cài đặt Meeus
  có trích dẫn, đối chiếu chéo và golden-test, còn cùng cuốn sách có cả hành tinh
  lẫn Mặt Trăng. **Độ chính xác không phải tiêu chí phân biệt** — giới hạn ~0,01°
  mà R19 đúng khi gọi là không đủ cho Bát Tự thì bằng ~1/100 orb hẹp nhất của
  chiêm tinh. Chi tiết ở `docs/research_drafts/R5_meeus_path_survey.md`

### Tests

493 test (tăng từ 469):

- **`LuckCycleTest` (22)** — tách đôi có chủ đích, vì hai nửa hỏng vì lý do khác
  nhau: phần **quy đổi** là số học thuần và được assert **chính xác, không dung
  sai**, với đúng số ngày chẵn mà các nguồn công bố (25 → 8 năm 4 tháng; 8 → 2
  năm 8 tháng; 2 → 8 tháng; 7 → 2 năm 4 tháng; 6 ngày 7 canh giờ → 2 năm 2 tháng
  10 ngày); phần **thiên văn** thừa hưởng giới hạn R19 nên mang dung sai. Gộp lại
  sẽ áp dung sai lên thứ vốn không có, và che mất nửa nào hỏng. Cộng: cả 20 tổ
  hợp can × giới tính, và bất biến rằng hai giới **luôn** ngược chiều nhau với
  cùng một can; ca sinh đúng mốc khởi vận từ 0 chứ không phải 1 (không nguồn nào
  chứng thực mức tối thiểu 1 tuổi); `LuckPillar` **không có** trường nào chứa
  được phán định, assert theo tên component
- **Tích hợp HTTP đầu-cuối (+2)** — vector 1990 chạy qua toàn bộ đường dẫn thật;
  và ca không có giới tính giữ **nguyên vẹn** lá số, chỉ thiếu đúng một mục


### Added — CI cho frontend (`web-verify`)

Trước đây `destiny-web` **hoàn toàn không được CI kiểm** — README gọi hệ thống
là một MVP có UI thật, nhưng một lỗi TypeScript hay một build hỏng có thể merge
vào `main` mà không ai biết cho tới khi có người tự chạy tay. Thêm job
`web-verify` trong `.github/workflows/build.yml`: cài dependency qua `npm ci`,
`typecheck` (script mới, `tsc --noEmit`), `lint`, rồi `next build` thật —
`next build` còn tự kiểm type và sinh trang tĩnh, nên đây là ba bước một
contributor vẫn chạy tay trước khi có job này.


### Added — Phase 14: metrics cho từng engine (CLAUDE.md §5)

CLAUDE.md §5 yêu cầu mỗi engine có **timeout, cancellation, error isolation và
metrics**. Ba cái đầu đã là cấu trúc trong `EngineExecutor` từ Phase 1. Cái thứ
tư **không tồn tại** — một engine bắt đầu timeout trên production chỉ để lại đúng
một dòng log, mà một dòng log thì không alert được, không vẽ đồ thị được. Phase 14
được ghi là "continuous", và đó là cách nó ở mức 0.

- **`EngineMetrics` (interface, `destiny-execution`)** — module này chỉ phụ thuộc
  engine SPI và slf4j, và điều đó đáng giữ: nó là phần dễ bị test bằng unit test
  thuần nhất, kéo một framework metrics vào sẽ khiến mọi test đó phải trả giá.
  Implementation Micrometer nằm ở `destiny-app` — cùng cách chia mà
  `AiNarrativeProvider` đang dùng cho OpenRouter
- **Ghi đúng `EngineStatus` thật, không phải một cờ thành/bại.** Dự án này *dự
  kiến* có rất nhiều `RESEARCH_REQUIRED` và `NOT_APPLICABLE` — đó là trạng thái
  bình thường, không phải lỗi. Gộp chúng vào chung với lỗi thật sẽ làm tỉ lệ lỗi
  mất nghĩa. Lớp Micrometer thêm tag `outcome` thô hơn (answered / declined /
  failed) cho dashboard, vẫn giữ status chi tiết bên cạnh
- **`timedOut` là tag riêng, tách khỏi status.** Timeout hiện ra dưới dạng
  `FAILED_RECOVERABLE`, nên không có cờ này thì *đúng cái lỗi có cách chữa đã
  biết* (nâng budget, hoặc làm engine nhanh hơn) trở thành không phân biệt được
  với một engine throw
- **Đo cả thời gian chờ permit.** `ExecutionPolicy` giới hạn concurrency, nên khi
  tải cao thì duration của một engine âm thầm bao gồm cả thời gian nằm chờ. "Engine
  chậm" và "hệ thống bị bão hòa" cần hai cách sửa khác nhau. Ghi cho **mọi** lần
  chạy, thường là ~0 — vì một series chỉ xuất hiện khi tải cao là series không ai
  có baseline
- **Metrics không bao giờ được làm hỏng phép tính.** `EngineMetrics` cấm
  implementation throw, nhưng một hợp đồng không phải một bảo đảm, và hậu quả của
  việc tin nó thì vô lý: một metrics backend cấu hình sai biến một phép tính đúng
  thành một phép tính lỗi. `EngineExecutor` nuốt mọi exception từ metrics (log
  debug), và có test dùng một backend **cố tình throw** để xác nhận

### Added — Actuator, với danh sách endpoint được ghim cứng

- `spring-boot-starter-actuator` + `management.endpoints.web.exposure` ghim rõ
  `health,info,metrics` kèm danh sách exclude tường minh, `health.show-details:
  never`, `info.env.enabled: false`
- **Lý do ghim thay vì để mặc định:** thêm Actuator là thêm bề mặt HTTP, và hai
  endpoint hữu ích nhất với developer — `/actuator/env` và `/actuator/configprops`
  — chính là hai endpoint sẽ render `OPENROUTER_API_KEY` và mật khẩu database cho
  bất kỳ ai chạm được tới cổng (Master Spec §28). Mặc định của Boot hôm nay là
  bảo thủ, nhưng "hôm nay" không phải một bảo đảm xuyên phiên bản, và giá của một
  hồi quy ở đây là **một credential bị lộ**, không phải một test đỏ
- **Không tag nào lấy từ dữ liệu người dùng.** Mọi giá trị tag đến từ tập đóng:
  engine id, `EngineStatus`, ba chuỗi outcome cố định. Một tag theo user hay theo
  calculation sẽ nhân số time series không giới hạn — đúng cách thông thường mà
  một metrics backend bị hạ bởi chính thứ đáng ra để canh nó

### Nghiên cứu — giả thuyết về 8 chính sách scenario còn thiếu: **bị loại**

Tám trong mười scenario vẫn `policyDefined == false`, vì Master Spec §7 chỉ cho ví
dụ với BUSINESS và DAILY_ACTION. Từ sau Phase 8a và Phase 10, các engine đã **tự
khai báo** dimension, nên có một giả thuyết hấp dẫn: *một engine áp dụng cho một
scenario khi và chỉ khi dimension nó khai báo giao với dimension của scenario* —
tức một **quy tắc**, không phải 8 bảng bịa; và hai chính sách đã đặc tả sẽ làm dữ
liệu kiểm chứng, đúng phương pháp vừa giải quyết bảng hướng Bát Trạch.

**Giả thuyết thất bại chính bài kiểm của nó.** `DAILY_ACTION` có dimension
`{DAILY, TIMING}`. `FENGSHUI_KUA` không khai báo dimension nào trong đó — nó khai
HOME, FINANCE, CAREER, RELATIONSHIP, HEALTH_REFLECTION, DECISION — nhưng Master
Spec §7 lại xếp nó `MEDIUM` cho scenario đó. Quy tắc không tái hiện được chính
đặc tả mà nó phải giải thích.

**Quyết định: loại, không chỉnh cho vừa.** Có thể nới quy tắc đến khi khớp cả hai
ví dụ — nhưng một quy tắc được fit vào hai điểm dữ liệu thì không còn là dẫn xuất,
nó là hai ví dụ cộng trang trí, và rồi sẽ được đem áp cho 8 scenario chưa ai kiểm.
Đúng mùi mà vòng 1 của R7 đã dừng lại vì nó. Ghi lại ở
`docs/DECISION_LOG.md` để lần sau bắt đầu từ thất bại này chứ không phát hiện lại,
và để phương án đó không bị âm thầm quên: thứ thực sự đóng được việc này là một
**quyết định của chủ dự án** về việc engine nào quan trọng với 8 scenario còn lại
— đây là câu hỏi sản phẩm, không phải câu hỏi nghiên cứu.

### Tests

469 test (tăng từ 454):

- **`EngineMetrics` (9)** — trọng tâm là các nhánh dễ bị bỏ sót. Counter
  happy-path là phần ai cũng nhớ viết; timeout, exception và một câu trả lời-không
  trung thực là những nhánh đáng ghim, vì đó đúng là các lần chạy mà người vận
  hành cần thấy nhất và là nơi một `return` sai nhánh sẽ âm thầm làm mất số liệu.
  Cộng: một backend **cố tình throw** không làm hỏng được phép tính; harness mặc
  định không cần backend nào; `null` metrics degrade thành no-op
- **`ActuatorExposureTest` (6)** — một test về những gì **vắng mặt**, loại test
  chỉ tồn tại nếu có người cố ý viết: `/actuator/env`,
  `/actuator/env/OPENROUTER_API_KEY`, `/configprops`, `/beans`, `/heapdump`,
  `/threaddump`, `/loggers` đều phải trả 404; health không nêu component/host;
  counter engine tồn tại sau một lần chạy thật và **không** chứa tên người dùng
  hay `calculationId`


### Added — Phase 10: Phong Thủy Bát Trạch (`destiny-engine-fengshui`)

Đây là engine **đầu tiên của Phong Thủy** phát sinh tín hiệu thật cho Fusion, và
là lần đầu R7 chuyển khỏi `RESEARCH_REQUIRED` — nhưng chỉ chuyển sang
`PARTIALLY_RESOLVED`, không phải `RESOLVED`.

Cùng ngày, vòng nghiên cứu R7 **thứ nhất** đã quyết định **không** triển khai vì
bảng 8×8 hướng chỉ tìm được ở một nguồn, mà bảng đó sai cấu trúc ở 4 ô. Vòng
**thứ hai** tìm được **quy tắc sinh ra bảng đó** — và điều này đổi hẳn bản chất
vấn đề: từ "64 ô phải tin" thành "8 trường hợp phải dẫn nguồn", cộng một bảng
**dẫn xuất** có thể *kiểm chứng* được với dữ liệu đã công bố thay vì chỉ chép lại.

**Đã đóng 4/5 mục của R7:**

- **Trường phái** — chỉ Bát Trạch. Master Spec §20 cấm trộn Phi Tinh / Huyền
  Không, và test xác nhận chuỗi `school` không nhắc tới hai phái kia
- **Công thức nam/nữ** — ba nguồn độc lập khớp nhau hoàn toàn, kể cả mốc chia
  tại năm 2000: nam `10 − a` / `9 − a`, nữ `5 + a` / `6 + a`. Golden-test theo
  hai ví dụ có sẵn đáp án trên `nguyenthehoa.com` (nam 1978 → Tốn, nữ 1978 →
  Khôn) và dữ kiện độc lập "nam sinh 1990 là cung Khảm"
- **Trường hợp số 5** — nam về Khôn (2), nữ về Cấn (8), cả hai thuộc Tây tứ
- **Bảng tám hướng** — **dẫn xuất từ quy tắc biến hào** (Bát Biến Du Niên), không
  chép ô nào

**Bảng dẫn xuất và cách nó tự kiểm chứng.** Quan hệ chỉ phụ thuộc *hào nào khác
nhau* giữa hai quái: không hào nào → Phục Vị; chỉ hào thượng → Sinh Khí; chỉ hào
trung → Tuyệt Mệnh; chỉ hào hạ → Hoạ Hại; thượng+trung → Ngũ Quỷ; thượng+hạ →
Lục Sát; trung+hạ → Thiên Y; cả ba hào → Diên Niên.

Quy tắc được coi là **giả thuyết** và mang đi kiểm với các bảng người khác công bố:

- Bảng 8×8 của `masterseanchan.com`: **khớp 60/64 ô**
- Trang từng cung của `nguyenthehoa.com` cho cung Cấn: **khớp 8/8** — kể cả hai ô
  đang tranh chấp, ở đó nguồn Việt đứng cùng quy tắc và **bác** bảng tiếng Anh
- Trang cung Chấn của `phongthuykhaitoan.com`: khớp mọi hướng nêu rõ ràng

Bốn ô lệch được xác định là lỗi của **một** nguồn, bằng ba luận cứ độc lập: nguồn
Việt bác trực tiếp; chúng phá tính đối xứng mà một quy tắc dựa trên *hiệu hào*
bảo đảm theo cấu trúc (hiệu thì không quan tâm tính từ bên nào); và ngay trong
bảng tiếng Anh, hai pattern bị ảnh hưởng chia **6 ô so với 2** nghiêng về quy tắc.
Cả ba đều được assert trong `BatTrachTableTest`, cùng 5 bất biến cấu trúc mà vòng
1 để lại — chính bộ tiêu chí nghiệm thu đó là lý do vòng 2 làm nhanh.

**Mục thứ 5 của R7 — ranh giới năm — vẫn mở, và được *biểu diễn* chứ không được
chọn.** Thực hành Việt dùng năm âm lịch (`nguyenthehoa.com` nói thẳng, kèm ví dụ
đổi "20/02/1983 dương → 08/01/1983 âm, dùng năm 1983"), thực hành cổ điển dùng
năm mặt trời đổi tại Lập Xuân. Không nguồn nào phân định.

**Kết luận R18 của Bát Tự không chuyển sang được** — R18 chọn Lập Xuân dựa trên
bảng Tứ Trụ đã công bố, và bằng chứng đó nói về Tứ Trụ, không nói về thực hành
cung phi. Dùng lại kết luận vì nó ở ngay bên cạnh chính là kiểu âm thầm chọn
trường phái mà Rule D cấm — và nó rất dễ làm, nên được ghi lại như một quyết định
*đã không* làm.

Vì vậy engine **tính cả hai**. Hai quy ước trùng nhau với mọi ca sinh ngoài
khoảng Tết→Lập Xuân, tức đại đa số, và những người đó có đáp án dứt khoát. Trong
khoảng đó — và chỉ khi hai quy ước cho ra **cung khác nhau**, không phải chỉ khác
năm — engine báo cả hai, nêu `METHODOLOGY_UNRESOLVED` (ảnh hưởng kết quả),
**không** công bố bảng tám hướng (công bố một bảng là ngầm coi đáp án Lập Xuân là
đáp án) và **không** phát sinh tín hiệu nào.

### Added — tín hiệu Phong Thủy đầu tiên tới Fusion

- **Tín hiệu cần một hướng để đối chiếu.** Bát Trạch xét *quan hệ giữa người và
  một hướng*; cung phi đứng một mình là thông tin, không phải phán định. Không có
  hướng → trả về bảng tám hướng làm evidence và **không** tín hiệu; gán polarity
  cho bản thân cung phi sẽ là bịa
- **Polarity và strength đọc từ chính truyền thống**, không do dự án gán: cát/hung
  và thượng/trung/tiểu. Ba mức hung được giữ nguyên chứ không làm phẳng — Hoạ Hại
  (tiểu hung) và Lục Sát (thứ hung) ra `CAUTION`, Ngũ Quỷ và Tuyệt Mệnh (đại hung)
  ra `NEGATIVE`; Javadoc của `Polarity` đã nói rõ CAUTION không đồng nghĩa NEGATIVE
- Các tín hiệu của một lần đánh giá **dùng chung một evidence group**, nên Fusion
  không đếm một phát hiện thành nhiều (FUSION_ENGINE_SPEC §5)
- **Giới tính không có giá trị mặc định ở bất kỳ lớp nào.** Engine trả
  `INVALID_INPUT`, và `FengShuiTaskFactory` **không tạo task** thay vì mặc định —
  khác với thiếu giờ sinh (làm kết quả kém đi), một giới tính mặc định cho ra
  **đáp án sai một cách tự tin**, trông y như một đáp án đúng

### Changed

- **`SolarYear` (destiny-calendar, mới)** — phép tính năm theo Lập Xuân trước đây
  nằm package-private trong `destiny-engine-bazi`. Bát Trạch cần đúng câu trả lời
  đó, mà một engine không được phụ thuộc engine khác
  (`enginesStayIndependent`) — nên nó chuyển sang hạ tầng chung. Luật kiến trúc đó
  vừa chứng minh giá trị: hai engine dùng chung một dẫn xuất thì không còn là hai
  nguồn độc lập, và chính test kiến trúc đã buộc code dùng chung đi vào chỗ dùng
  chung thay vì để một phụ thuộc mọc lên giữa hai engine
- `FENGSHUI_KUA` giờ **thật sự chạy** trong BUSINESS và DAILY_ACTION — hai kịch
  bản đã nêu tên engine id này từ đầu (Master Spec §7)
- `MethodologyRegistrySeeder`: `FENGSHUI_KUA` lên version `1.1` và
  `PRODUCTION_READY`, ghi rõ 4/5 mục R7 đã đóng và mục nào chưa — cùng mô hình
  `CALENDAR_VN_TRADITIONAL` đang dùng cho R14b
- `ScenarioRunRequest` thêm trường thứ tư `fengShui` (đổi arity của record)
- **R7 chuyển sang `PARTIALLY_RESOLVED`** — một trạng thái chưa từng có trong
  legend. Làm tròn lên `RESOLVED` thì gọn hơn, và sẽ làm một câu hỏi còn mở trở
  nên vô hình

### Added — UI

- **`BatTrachCard`** dựng bảng tám hướng *từ evidence*, kèm ba trạng thái: hai
  quy ước trùng nhau (cung phi + nhóm Đông/Tây tứ + bảng tám hướng), khác nhau
  (cả hai cung phi, không có bảng, kèm lý do), và có hướng đang xét (nêu trước).
  Thứ tự bảng là **theo la bàn, không theo mức tốt** — hướng nào tốt phụ thuộc
  cung phi của từng người, nên một thứ tự cố định theo mức tốt sẽ sai với phần lớn
  người đọc
- Ô nhập Bát Trạch trong Trung tâm quyết định, nói rõ rằng phải nhập hướng mới có
  phần đánh giá và mới có tín hiệu

### Tests

454 test (tăng từ 406):

- **`BatTrachTable` (11)** — 4 hàng golden từ nguồn đã công bố (Cấn từ nguồn Việt
  là hàng quyết định: chứa cả hai ô tranh chấp), 4 cặp Tuyệt Mệnh đúng như nguồn
  Việt nêu tên, và 6 bất biến cấu trúc: mỗi hàng là phép thế của 8 du niên; Phục Vị
  chỉ ở đúng hướng của chính quái; **đối xứng** (bất biến đã bắt được 4 ô sai); 4
  hướng cát luôn trong nhóm của mình; 4 quan hệ hung tạo hình vuông Latin trên
  Đông×Tây; mỗi du niên xuất hiện đúng 8 lần
- **`KuaNumber` (11)** — ví dụ có sẵn đáp án, mốc gián đoạn 2000, ghi chú rằng
  cộng-rồi-rút và rút-rồi-cộng luôn cho cùng kết quả (căn số cộng bảo toàn), và
  một lượt quét **1900–2100 × 2 giới tính** xác nhận **không bao giờ** lọt ra
  cung 5 — cũng là cách kiểm rằng nhánh thay thế được đi qua trên mọi đường
- **`FengShuiKuaEngine` (20)** — không hướng → không tín hiệu; Sinh Khí →
  SUPPORT/STRONG; Tuyệt Mệnh → NEGATIVE/STRONG; Hoạ Hại → **CAUTION/WEAK** (không
  làm phẳng thành NEGATIVE); tín hiệu dùng chung evidence group và không đánh dấu
  critical; mâu thuẫn ranh giới năm nêu cả hai cung và **không** tín hiệu, không
  bảng hướng; thiếu giới tính là `INVALID_INPUT`; khoảng trống R14b trả
  `RESEARCH_REQUIRED`
- **Tích hợp HTTP đầu-cuối (+4)** — cung phi 1990 nam qua toàn bộ đường dẫn, và
  xác nhận tín hiệu **thật sự tới được Fusion** (`supportingSources` chứa
  `FENGSHUI_KUA`); không hướng → không tín hiệu; thiếu giới tính → engine không
  chạy và nằm trong `unavailableEngines`; ca 03/02/1984 nêu cả hai cung phi


### Added — Retention: dữ liệu không còn được giữ mãi mãi (CLAUDE.md §7)

Trước thay đổi này **mọi calculation được lưu vĩnh viễn**, kể cả một lượt xem
"hôm nay nên làm gì" dùng một lần. CLAUDE.md §7 cấm đúng điều đó
(*"Không lưu mọi JSON khổng lồ mãi mãi trong hot relational tables"*), và
`DATA_MODEL_AND_RETENTION.md` §7–§8, §11 đã đặc tả chi tiết từ đầu. Khoảng
trống này vô hình vì không có gì hỏng — database chỉ đơn giản là phình lên.

- **`RetentionClass`** (destiny-core): `PERSISTENT`, `USER_SAVED`, `EPHEMERAL`,
  `AUDIT`. Enum nói **vì sao** một bản ghi được giữ, không nói giữ *bao lâu* —
  thời hạn thuộc policy cấu hình được (§8), còn class là bản chất của bản ghi
  và không đổi khi người vận hành sửa một con số. `isAutoDeletable()` chỉ đúng
  với `EPHEMERAL`, viết thành method để một class mới trong tương lai **buộc**
  phải ra quyết định ở một chỗ, thay vì âm thầm mặc định là xóa được
- **`V8__add_retention.sql`**: hai cột `retention_class` + `expires_at` trên
  `calculations` (hai cột, không phải một — class nói *vì sao*, expiry là kết
  quả policy; gộp lại thì "đã lưu" và "hết hạn ngày 21" thành cùng một
  trường), index `(retention_class, expires_at)` cho đúng một truy vấn mà job
  dọn dẹp cần, và bảng **`retention_runs`** làm audit trail
- **`RetentionClassifier`**: quyết định class + expiry **một lần, lúc ghi**, và
  lưu lại. Suy lại policy lúc dọn dẹp sẽ khiến người vận hành rút ngắn
  `daily-duration` là *hồi tố* tuyên án những bản ghi viết theo luật cũ — một
  lần sửa config âm thầm biến thành một lần xóa. Đo từ `completedAt` chứ không
  phải `Instant.now()` nên ghi lại cùng một lần chạy cho cùng kết quả
  (Master Spec §25)
- **`CalculationPurger`**: xóa một calculation và toàn bộ bản ghi con trong
  **transaction riêng** (`REQUIRES_NEW`). Dùng chung một transaction thì một
  dòng lỗi sẽ đánh dấu rollback-only và kéo theo 499 dòng còn lại — im lặng.
  Đặt ở bean riêng vì Spring bỏ qua `@Transactional` khi gọi nội bộ cùng
  class, nên một helper cùng class sẽ chỉ *trông như* được cô lập
- **`CalculationRetentionService`**: đủ những gì §11 nêu — chọn theo retention
  class, **dry-run**, **audit**, **batch delete**, **retry có giới hạn** (2 lần,
  CLAUDE.md §5 cấm retry vô hạn), và **không bao giờ chạm vào `USER_SAVED`**.
  Cố ý **không** `@Transactional` để không phá cơ chế cô lập ở trên
- **`RetentionScheduler`** (destiny-app): `@ConditionalOnProperty` — **tắt mặc
  định, và khi tắt thì bean không tồn tại**. Một `if` bên trong method sẽ để
  lại một scheduled task đang sống, chỉ cách một lần refactor là bắt đầu xóa
  thật. Mỗi lần chạy đúng một batch, không lặp cho tới cạn: một backlog rút
  dần qua nhiều đêm giúp bán kính thiệt hại của một cấu hình sai chỉ bằng
  `batchSize`, và người vận hành còn một đêm để nhận ra
- **Thời hạn mặc định 30 ngày (đọc hằng ngày) và 90 ngày (kịch bản khác)** —
  §8 cho khoảng 7–30 và 30–90, cả hai mặc định lấy **đầu dài**. Giữ quá lâu là
  sai lầm còn cứu được; xóa quá sớm thì không

### Added — API và UI cho retention

- `ScenarioRunResponse` có thêm khối `retention` — **luôn có**, không phải khi
  hỏi mới trả. Một hệ thống âm thầm xóa kết quả của người dùng sau 30 ngày mà
  không nói gì là đang giữ lại đúng thứ người dùng cần để hành động, cùng lý
  do khiến `Uncertainty` phải đi tới được giao diện thay vì bị giải quyết nội bộ
- **`POST /api/v1/calculations/{id}/save`** (mới) — chuyển kết quả sang
  `USER_SAVED` và **xóa hẳn expiry** (null, không phải năm 9999: giao diện phải
  nói được "sẽ không bị xóa" mà không cần diễn giải một ngày tháng).
  Idempotent: bấm hai lần không phải lỗi của người dùng
- `RetentionDtoMapper` dùng chung cho đường ghi và đường đọc, nên một kết quả
  không thể báo một hạn lúc tạo ra và một hạn khác lúc đọc lại
- **`RetentionNotice`** (frontend): hiện **đầu trang kết quả** — nó nói về việc
  kết quả này ngày mai còn tồn tại hay không, điều đó xếp trên mọi nội dung mà
  kết quả nói. Hiện *luôn*, không chỉ khi gần hết hạn: một thông báo chỉ xuất
  hiện sát hạn sẽ dạy người đọc rằng kết quả là vĩnh viễn — đúng cái giả định
  làm cho việc tự động xóa trở thành mất dữ liệu. Nêu ngày cụ thể, không phải
  "còn 12 ngày" (sẽ sai ngay khi trang được cache)

### Fixed

- `expires_at` không round-trip chính xác qua database: PostgreSQL và H2 lưu
  `timestamp with time zone` ở độ chính xác micro giây, nên một `Instant` mang
  nano giây quay ra khác lúc ghi vào — API báo **hai** thời điểm hết hạn khác
  nhau cho cùng một dòng, một lúc chạy và một lúc đọc lại. Phát hiện bởi test
  đầu-cuối thật. Sửa bằng cách truncate về mili giây ngay trong
  `RetentionClassifier`: với policy tính theo ngày thì các chữ số bị bỏ là
  nhiễu, còn tái lập được chính xác thì đáng giá hơn (Master Spec §25)

### Changed

- `RetentionClass` được thêm vào registry nhãn tiếng Việt và vào
  `LabelCoverageTest`, kèm một test riêng chặn **nhãn nói giảm nói tránh**:
  "Tạm thời" đúng về mặt kỹ thuật với một kết quả `EPHEMERAL` nhưng không nói
  gì về việc sẽ bị xóa, nên nhãn buộc phải nêu ra
- `ScenarioRunResponse` thêm trường thứ mười `retention` (đổi arity của record)

### Nghiên cứu — R7 (Phong Thủy Kua): đóng 2/5, **vẫn chưa triển khai**

Phase 10 là phần dự định làm trong phiên này và **đã dừng lại vì bằng chứng,
không phải vì hết sức**. Ghi lại đầy đủ ở `docs/RESEARCH_BLOCKERS.md` R7:

- **Đã đóng — công thức Kua và trường hợp số 5.** Hai nguồn độc lập khớp nhau
  hoàn toàn, kể cả mốc chia tại năm 2000: nam `10 − a` (trước 2000) / `9 − a`
  (từ 2000), nữ `5 + a` / `6 + a`; ra 5 thì nam về Khôn (2), nữ về Cấn (8).
  Đối chiếu được với một dữ kiện độc lập (nam sinh 1990 là cung Khảm)
- **Vẫn chặn — ranh giới năm.** Nguồn Việt tính theo **năm âm lịch** (mốc Tết);
  nguồn cổ điển/Anh ngữ tính theo **năm mặt trời** (mốc Lập Xuân). Cả hai đều
  nói rõ ràng. Kết luận R18 của Bát Tự **không** chuyển sang được: R18 chọn Lập
  Xuân *cho Bát Tự* dựa trên bảng Tứ Trụ đã công bố, và bằng chứng đó không nói
  gì về thực hành Kua. Chọn Lập Xuân ở đây chỉ vì engine bên cạnh đã chọn chính
  là kiểu âm thầm chọn trường phái mà Rule D cấm
- **Vẫn chặn — bảng 8×8 hướng.** Chỉ tìm được **một** nguồn có bảng đầy đủ, và
  bảng đó **bất đối xứng** ở đúng 4/28 cặp — điều mà cách dạy Bát Trạch tiêu
  chuẩn (các cặp quái tương hỗ) không cho phép. Các trang riêng từng cung của
  chính nguồn đó tái hiện y nguyên sự bất đối xứng, nên đó là điều nguồn ấy
  dạy, không phải một lỗi chép ở một trang
- **Sản phẩm hữu ích nhất của vòng này:** **5 bất biến cấu trúc** mà bất kỳ bảng
  ứng viên nào cũng phải thỏa (mỗi hàng là một phép thế của 8 sao; Phục Vị nằm
  ở chính hướng của cung; 4 hướng tốt luôn nằm trong nhóm Đông/Tây của mình;
  đối xứng `rel(A, hướng-B) == rel(B, hướng-A)`; 4 quan hệ xấu tạo thành một
  hình vuông Latin trên Đông×Tây). Vòng nghiên cứu sau có **tiêu chí nghiệm
  thu**, không chỉ có hy vọng — và chính bất biến (4) đã bắt được một bảng
  dựng lại từ quy tắc nhớ được, ở bản nháp đầu của vòng này

### Tests

406 test (tăng từ 372):

- **`RetentionClassifier` (11)** — không cần container, vì một câu trả lời sai
  ở đây là mất dữ liệu và việc kiểm chứng nó không nên phụ thuộc vào việc
  Spring context có khởi động đúng hay không. Đọc hằng ngày hết hạn sớm hơn
  kịch bản khác; khớp scenario không phân biệt hoa thường (API nhận
  `/scenarios/daily_action` chữ thường — một classifier chỉ khớp chữ hoa sẽ âm
  thầm cho đọc hằng ngày 90 ngày); expiry đo từ lúc hoàn thành nên tái lập
  được; expiry truncate về mili giây; thời hạn ≤ 0 bị từ chối **lúc khởi động**
  chứ không phải lúc xóa; và hai nửa của một bất biến đều **không biểu diễn
  được**: `EPHEMERAL` không có expiry (giữ mãi mãi mà mang nhãn tạm thời), và
  class không-tự-xóa mà lại có expiry
- **`CalculationRetentionService` (14)** — trên schema thật. Viết theo hướng
  *những gì không bao giờ được xảy ra*: kết quả đã lưu **sống sót dù đã quá
  hạn** (§11 "không xóa USER_SAVED" — assertion quan trọng nhất trong file),
  dry-run không xóa gì nhưng **vẫn ghi audit** (một lần diễn tập đã xảy ra khác
  với chưa từng chạy), xóa không được vướng khóa ngoại của chính nó. Mọi fixture
  là một calculation **đầy đủ** — evidence, signal trích evidence đó, fusion
  result kèm conflict — vì một test purge dựng trên một dòng `calculations`
  trơ sẽ xanh trong khi thứ tự xóa thật đang sai
- **Tích hợp HTTP đầu-cuối (+2)** — vòng tròn đầy đủ: chạy → đọc lại → lưu →
  đọc lại, xác nhận cả bốn bước đồng ý về retention; và đọc hằng ngày có hạn
  ngắn hơn kịch bản BUSINESS qua HTTP thật (ánh xạ scenario → thời hạn đi qua
  ba lớp)
- **Wiring của scheduler (4)** — hai Spring context riêng, vì property này
  quyết định **bean có tồn tại hay không**, không phải bean hành xử thế nào. Ca
  quan trọng nhất là mặc định-tắt: một môi trường chưa từng yêu cầu tự động xóa
  thì không được có một scheduled task đang sống, và cách duy nhất chắc chắn là
  assert bean **vắng mặt** — test nội dung method vẫn sẽ xanh dù task đang sống
  trong container. Ca bật thì xác nhận thời hạn do người vận hành đặt thắng mặc
  định (7 ngày — đúng giới hạn dưới của §8)


### Added — Phase 8a: Bát Tự lập lá số Tứ Trụ (`destiny-engine-bazi`)

Phase 8 được **tách làm hai** (xem `docs/DECISION_LOG.md`, 2026-08-22): 8a lập
lá số, 8b luận giải. Cả ba mục nghiên cứu đang chặn Phase 8 — R1 (Dụng Thần),
R2 (Đại Vận), R3 (cường độ Nhật Chủ) — đều thuộc phần **luận giải**, không mục
nào liên quan đến việc lập lá số. Sau khi cụm Calendar được giải quyết
(2026-08-19), giữ nguyên một lá số đã kiểm chứng được phía sau ba mục đó chính
là một kiểu không trung thực khác: từ chối đưa ra dữ liệu thật vì một phần khác
của cùng phase chưa xong.

**Module mới `destiny-engine-bazi`** — phụ thuộc `destiny-engine-api` và
`destiny-calendar` (hạ tầng, không phải engine, nên luật ArchUnit
`enginesStayIndependent` không bị ảnh hưởng). Tính được:

- **Tứ Trụ** theo quy ước Tử Bình: Trụ Năm đổi tại **Lập Xuân** (không phải
  Tết), Trụ Tháng theo **Tiết Khí** (không phải tháng âm lịch). Phép toán Can
  Chi không viết lại — `CanChi` đã có sẵn Ngũ Hổ Độn và Ngũ Thử Độn đã
  golden-test; phần Bát Tự thêm vào chỉ là *đưa năm nào và số tháng nào* vào
  đó
- **Ngũ Hành và Âm Dương** của mọi Thiên Can / Địa Chi (`FiveElement`,
  `YinYang` trong `destiny-calendar` — đây là thuộc tính của chính hệ Can Chi,
  không phải diễn giải, và đặt ở đó để Bát Tự / Tử Vi / Phong Thủy sau này
  không bao giờ phải phụ thuộc lẫn nhau vì nó)
- **Tàng Can** (`HiddenStems`) — đối chiếu hai nguồn độc lập
- **Thập Thần** (`TenGod`, `TenGods`) — cài bằng *quy tắc* (quan hệ Ngũ Hành ×
  cùng/khác Âm Dương) chứ không phải bảng tra 10×10 chép tay, vì một bảng chép
  tay có thể sai một ô mà không ai đọc ra
- **Số đếm Ngũ Hành** (`ElementTally`) — **ba nhóm đếm riêng** (theo can, theo
  chi, theo tàng can), cố tình không cộng gộp và không có trọng số

### Trung thực về những gì *không* được tính

- **Engine không phát sinh signal nào.** Một `Signal` bắt buộc có `Polarity`,
  và một polarity Bát Tự bắt buộc cần R1 + R3. `BaziEngineTest.emitsNoSignals`
  làm hỏng build nếu danh sách đó khác rỗng — nên việc điền vào đó phải là một
  quyết định có ý thức, chống lại một test đang fail, không phải chuyện tự trôi
- **Status là `PARTIAL`, không phải `SUCCESS`**, kèm `ResearchReference` nói
  rõ phần luận giải thiếu vì sao
- **Ba phần bị chặn là *nội dung trả về*, không phải chỗ trống.**
  `BlockedSection` mang tên tiếng Việt, mã nghiên cứu, lý do, và các trường
  phái khác nhau đang tồn tại; mỗi phần cũng thành một `EngineWarning`
  **critical** để pruning của lớp AI không cắt được. UI hiển thị "Dụng Thần —
  cần xác minh thuật toán (R1)", không bao giờ là một lá số âm thầm thiếu Dụng
  Thần
- **Hai entry registry, không phải một**: `BAZI_TUBINH_CHART` ở
  `CONTENT_REQUIRED` (tính được) và `BAZI` ở `RESEARCH_REQUIRED` (không tính
  được). Gộp một entry thì buộc phải nói sai theo hướng này hoặc hướng kia

### Added — hai mục nghiên cứu mới, phát hiện khi làm 8a

- **R18 — ranh giới năm Lập Xuân hay Tết.** Hai quy ước cho ra Trụ Năm khác
  nhau cho mọi ca sinh giữa Tết và Lập Xuân, và vì Trụ Tháng lấy can từ can
  năm (Ngũ Hổ Độn) nên sai ranh giới là sai **hai** trong tám chữ. Chọn **Lập
  Xuân**, khai báo thẳng trong metadata của engine; quy ước Tết được đặt tên
  trong `BaziYearBoundary` nhưng **cố tình không cài** — nó tồn tại để engine
  phát hiện được mâu thuẫn, không phải để âm thầm chuyển sang. Khi hai quy ước
  khác nhau, engine tính luôn Trụ Năm theo Tết và báo
  `METHODOLOGY_UNRESOLVED` (`affectsResult = true`) nêu **cả hai** đáp án
  → `RESOLVED` như một lựa chọn trường phái *đã khai báo*, không phải như một
  khẳng định chỉ có một quy ước đúng
- **R19 — độ chính xác thời điểm Tiết Khí.** Đo được, không phải phỏng đoán:
  chuỗi Meeus low-precision mà dự án đang dùng tính kinh độ **hình học** của
  mặt trời, bỏ nutation và aberration, nên chạy sớm so với bảng công bố —
  Lập Xuân 1984 lệch −7,4 phút, Lập Xuân 2024 lệch −15,9 phút. Xử lý bằng
  cửa sổ bảo vệ **40 phút**: sinh trong khoảng đó sẽ nhận
  `SOLAR_TERM_BOUNDARY` (`affectsResult = true`) + cảnh báo critical, và cửa
  sổ này được *assert* trực tiếp với cả hai thời điểm công bố trong
  `SolarTermInstantTest` nên không thể âm thầm thu nhỏ. Trụ Tháng vẫn được
  trả về — đó là đáp án tốt nhất của mô hình — nhưng không bao giờ như một
  điều chắc chắn → `DECISION_REQUIRED` (có nên nhận hiệu chỉnh
  apparent-longitude hay không)

### Added — API và UI

- `POST /api/v1/scenarios/{type}` nhận thêm khối `bazi`
  (`birthDate`, `birthTime`, `region`, `longitude`). `birthTime` **null nghĩa
  là không biết**, không phải thiếu trường: `BaziTaskFactory` dịch nó thành
  `BirthTimePrecision.UNKNOWN` và engine chỉ trả Trụ Năm + Trụ Tháng. Giờ
  danh nghĩa dùng nội bộ là **giữa trưa, không phải nửa đêm** — nửa đêm nằm
  trong khung Giờ Tý mà ranh giới 23:00 làm Trụ Ngày nhảy sang ngày sau
- **`GET /api/v1/labels` (mới)** — toàn bộ nhãn tiếng Việt, khóa theo tên enum
  rồi tên kỹ thuật. Cần thiết vì 8a là feature đầu tiên có payload là *dữ liệu
  có cấu trúc* thay vì các trường DTO cố định: lá số đi qua
  `Evidence.fact` với tên kỹ thuật thô (`GIAP`, `TY_KIEN`), và hai phương án
  còn lại đều tệ hơn — nhúng chuỗi tiếng Việt vào output của engine (đặt chữ
  hiển thị vào đúng lớp phải sạch chữ hiển thị), hoặc nhân đôi bảng nhãn sang
  TypeScript (hai registry sẽ lệch nhau, mà `LabelCoverageTest` chỉ canh được
  một cái)
- **Nhãn tiếng Việt mới**: Thiên Can (10), Địa Chi (12), Ngũ Hành (5), Âm
  Dương (2), Tiết Khí (24), Thập Thần (10), vị trí trụ (4), quy ước ranh giới
  năm (2). `LabelCoverageTest` mở rộng phủ cả 8 enum này, cộng một test riêng
  xác nhận **Tý và Tỵ không trùng nhãn** — chính lý do `EarthlyBranch` được
  đặt tên theo con vật thay vì phiên âm
- **`BaziChartCard` (frontend)** dựng bảng Tứ Trụ *từ evidence*, không từ một
  DTO riêng: evidence là bản ghi giải trình của dự án, nên lá số dựng từ
  evidence là lá số không thể lệch khỏi những gì audit trail ghi lại. Hiển thị
  Thiên Can / Địa Chi / Ngũ Hành / Tàng Can / Thập Thần, ba bảng đếm Ngũ Hành
  riêng, và danh sách phần luận giải bị chặn kèm lý do
- **`destiny-i18n` nay phụ thuộc `destiny-calendar` và `destiny-engine-bazi`**
  — cùng tiền lệ với `destiny-ai`: registry nhãn là nơi duy nhất được biết về
  enum hướng người dùng của module phía dưới, vì `UI_UX_VIETNAMESE_SPEC` §1
  coi một enum không có nhãn là một lỗi và `LabelCoverageTest` canh việc đó ở
  một chỗ tập trung

### Changed

- `BAZI` giờ **thật sự chạy** trong kịch bản BUSINESS và DAILY_ACTION — hai
  kịch bản đã nêu tên engine id `BAZI` từ đầu (Master Spec §7), nên trước đây
  nó luôn xuất hiện trong `unavailableEngines`
- `MethodologyRegistrySeeder`: `BAZI` lên version `1.1` và đổi tên hiển thị
  thành "Bát Tự - Luận giải (Dụng Thần, Đại Vận)" để phản ánh việc nó giờ chỉ
  còn là nửa luận giải; thêm entry `BAZI_TUBINH_CHART` version `1.0`
- `ScenarioRunRequest` thêm trường thứ ba `bazi` (thay đổi arity của record)
- `ResultView` nay nói rõ khi một lần chạy **không có tín hiệu nào**, thay vì
  im lặng ẩn mục tín hiệu — trường hợp này xảy ra thật khi người dùng chỉ bật
  Bát Tự

### Changed — luật kiến trúc mạnh thêm

- Luật ArchUnit "không có số thực trong domain" (ADR D6) được mở rộng từ
  `destiny-core`/`destiny-fusion` sang **cả `io.destinyos.engines..`** —
  engine chính là nơi một điểm số bịa dễ được *nghĩ ra* nhất, vì đó là nơi có
  động cơ: mọi bảng Tàng Can đã công bố đều đính tỉ lệ 60/30/10, mọi bài viết
  "Nhật Chủ mạnh yếu thế nào" đều đính một con số, mà R3 nói không tồn tại
  thang điểm nào được công nhận chung. Đúng **một** trường được cho qua và
  được nêu tên tường minh (không dùng khớp mẫu tên, để ngoại lệ luôn kiểm tra
  được): kinh độ nơi sinh trong `BaziInput` — một toạ độ địa lý đưa *vào*, để
  đổi giờ đồng hồ sang giờ mặt trời (R10), không phải điểm số về một kết quả.
  Luật đã được kiểm chứng ngược (bỏ ngoại lệ đi thì test fail đúng 1 vi phạm)
  nên nó không pass rỗng

### Tests

372 test (tăng từ 310):

- **`destiny-engine-bazi` (34)** — golden test đối chiếu bảng Tứ Trụ đã công
  bố cho 6 mốc thời gian, trong đó cặp 4/5-02-1984 nằm hai bên Lập Xuân
  (23:18:44 giờ Bắc Kinh) trong khi Tết 1984 đã qua từ 2/2 — đúng cặp phân
  biệt quy ước này với quy ước Tết; quy tắc Thập Thần kiểm cả 100 cặp can và
  xác nhận mỗi Nhật Chủ ánh xạ **song ánh** lên 10 vai (thuộc tính mà một
  bảng chép tay không đảm bảo được); không có signal; ba phần bị chặn có mã
  nghiên cứu và biến thể trường phái; UNKNOWN precision không sinh Nhật Chủ
  và không sinh Thập Thần ở bất kỳ đâu; khoảng trống R14b trả
  `RESEARCH_REQUIRED` chứ không đoán múi giờ; mâu thuẫn R18 nêu cả hai đáp
  án; cờ ranh giới Tiết Khí có và **không** báo động giả; tái lập được
- **`destiny-calendar` (+16, tổng 106)** — bảng Ngũ Hành/Âm Dương của can chi,
  Thổ xuất hiện đúng 4 lần trong 12 chi, hai vòng Tương Sinh/Tương Khắc là
  song ánh và nghịch đảo của nhau, `relationTo` phân hoạch đúng 5 quan hệ
  (thuộc tính khiến switch 10 nhánh của Thập Thần không có nhánh chết);
  Tàng Can khớp cả hai nguồn, chính khí luôn trùng Ngũ Hành của chi, đúng
  Sửu và Tỵ được cờ "thứ tự vai chưa thống nhất"; thời điểm Tiết Khí đối
  chiếu hai mốc Lập Xuân đã công bố, và **độ lệch được assert trực tiếp** nên
  cửa sổ 40 phút không thể bị thu nhỏ âm thầm
- **Tích hợp HTTP đầu-cuối (+4)** — chạy `BaziEngine` thật qua cổng ngẫu
  nhiên, xác nhận lá số 5/2/1984 khớp bảng công bố sau khi đi qua toàn bộ
  đường dẫn (DTO → dịch region/precision → engine → evidence → database),
  Thập Thần của can năm là Chính Quan so với Nhật Chủ Kỷ, ba phần bị chặn
  đến được client, `GET /api/v1/labels` trả đủ nhãn, và registry liệt kê cả
  hai nửa Phase 8 với hai status khác nhau

### Deliberately not done

- **Nạp Âm** — chưa có gì cần đến nó
- **Thập Thần theo Địa Chi** — sách vở không thống nhất chuyện Tý "thể dương
  dụng âm", mà mọi nguồn đã đối chiếu đều lấy Thập Thần từ *can*; điều này
  được ghi trong Javadoc của `YinYang` và làm cho không code nào chạm tới
- **Thứ tự trung khí / dư khí của Tàng Can** — hai nguồn xếp khác nhau cho
  Sửu và Tỵ; bộ can ẩn và chính khí được ghi lại, thứ tự đang tranh chấp được
  đánh cờ và không dùng vào bất kỳ phép tính nào. Các tỉ lệ 60/30/10 mà cả
  hai nguồn đính kèm **không** được nhập vào — đó đúng là loại trọng số bịa
  mà ADR D6 cấm, và `ArchitectureRulesTest.noProbabilityInTheDomain` sẽ chặn
  vì chúng cần `double`


### Added — Phase 12: AI Narrative layer (`destiny-ai`, ADR D8)

- Module mới `destiny-ai` — chỉ phụ thuộc `destiny-core`; không bao giờ phụ
  thuộc `destiny-fusion`, `destiny-scenario`, hay bất kỳ `destiny-engine-*`
  nào (một luật ArchUnit mới, `aiNarrativeStaysIsolated`, thực thi cơ chế
  này giống hệt ADR D5 bảo vệ Fusion). Đúng tinh thần D8: "không module nào
  phụ thuộc AI" — nghĩa là hệ thống phải chạy đầy đủ khi module này vắng
  mặt, bị tắt, hoặc gọi provider thất bại
- **Pruning** (`NarrativePruner`) đúng thứ tự ưu tiên `AI_NARRATIVE_SPEC.md`
  §3: critical luôn giữ, STRONG luôn giữ, MEDIUM chỉ giữ khi thuộc dimension
  liên quan kịch bản, loại hẳn NEUTRAL/trùng lặp/không liên quan, ngân sách
  8–20 signal (Master Spec §22) — cắt bớt ưu tiên thấp trước, không bao giờ
  cắt một signal critical để lấy chỗ
- **Prompt** (`NarrativePromptBuilder`) dùng nguyên văn system prompt của
  §4 (từng điều cấm: không tính lại, không thêm sao/lá bài/hành
  tinh/quẻ/evidence, không đổi kết quả Fusion, không trình bày huyền học
  như khoa học đã chứng minh) — không diễn giải lại bằng lời khác
- **Model independence** (§7): `AiNarrativeProvider` là interface,
  `OpenRouterNarrativeProvider` là một cài đặt cụ thể (không phải cài đặt
  duy nhất được phép). Không có model OpenRouter mặc định nào được hardcode
  — danh mục model miễn phí đổi theo thời gian, người vận hành phải tự xác
  nhận model còn khả dụng qua `DESTINY_AI_OPENROUTER_MODEL`
- **Failure handling** (§6): timeout, 429, 5xx, provider không khả dụng,
  JSON hỏng, phản hồi rỗng — tất cả rơi về `HardDataNarrativeFallback`, một
  báo cáo tất định dựng thẳng từ dữ liệu tính toán thật (không LLM, không
  bịa), không bao giờ throw exception hay chặn request. Provider OpenRouter
  tự giới hạn tối đa 1 lần thử lại cho lỗi tạm thời (timeout/5xx) — không
  bao giờ retry vô hạn (CLAUDE.md §5)
- **API key** chỉ đọc phía server (`OpenRouterProperties`, biến môi
  trường), không bao giờ trả về response hay lộ ra frontend (Master Spec
  §28)
- Lưu trữ mới (V7 migration, `ai_narratives`): một dòng mỗi calculation
  (upsert khi tạo lại, không tích lũy lịch sử — khác với `fusion_results`
  vì narrative là bản diễn giải của hard data, không phải một sự kiện mới)
- Endpoint mới: `POST`/`GET /api/v1/calculations/{id}/narrative` —
  `NarrativeOrchestrationService` là nơi duy nhất biết cả entity persistence
  (`SignalEntity`/`ConflictEntity`/`FusionResultEntity`) lẫn contract
  `destiny-ai`, dựng `NarrativeInput` trực tiếp từ enum thật (không round-trip
  qua `LabeledValue.technical()`) để giữ đúng ngữ nghĩa cho pruning
- **Khoảng trống ghi nhận trung thực, không che giấu**: `CalculationContext.uncertainties()`
  chưa từng được `CalculationRecorder` (V4-V6) lưu trữ — nên `warnings`/
  `limitations` gửi cho AI hiện để trống thay vì bịa ra nội dung, cho đến
  khi khoảng trống lưu trữ này được đóng ở một phase riêng
- Thêm 2 enum nhãn tiếng Việt mới vào `VietnameseLabels`
  (`NarrativeSource`, `FallbackReason`) — `LabelCoverageTest` xác nhận đủ
  nhãn, không enum kỹ thuật nào lọt ra ngoài
- 53 test mới (310 tổng, tăng từ 257): 38 test riêng cho `destiny-ai`
  (pruning, prompt, parser, service, provider OpenRouter qua
  `MockRestServiceServer` giả HTTP — không gọi mạng thật), 4 test round-trip
  persistence V7, 9 test API (unit service + `@WebMvcTest` controller), 1
  test tích hợp HTTP đầu-cuối mới (`ScenarioApiIntegrationTest`) xác nhận
  nhánh fallback thật (AI tắt theo mặc định) qua Spring context thật, và 1
  luật ArchUnit mới

### Added — Frontend: Next.js Decision Center (ADR D4)

- Module mới `destiny-web` (Next.js 16, App Router, TypeScript, Tailwind) —
  dự án npm riêng, không nằm trong Maven reactor
- Chỉ xây **3 trang thật có backend hỗ trợ** thay vì làm đủ 13 mục nav
  trong `UI_UX_VIETNAMESE_SPEC.md`: **Tổng quan** (danh sách methodology,
  `GET /api/v1/methodologies`), **Trung tâm quyết định** (luồng đúng theo
  đặc tả §3, `POST /api/v1/scenarios/{type}` với BUSINESS/DAILY_ACTION —
  2 kịch bản duy nhất có chính sách thật), **Lịch sử** (tra cứu theo mã,
  không phải danh sách duyệt được vì chưa có hệ thống tài khoản). 10 mục
  nav còn lại hiện "Sắp ra mắt" — nhãn trung thực, không phải trang giả
  trông như hoạt động
- `LabeledBadge` — component duy nhất render `{technical, labelVi}`,
  nhãn tiếng Việt hiển thị, tên kỹ thuật chỉ nằm trong tooltip
  (UI_UX_VIETNAMESE_SPEC §1)
- Thêm `WebCorsConfig` ở `destiny-app` — trước đây chưa có CORS nào, frontend
  không gọi được API
- **Bắt được 1 bug thật khi verify end-to-end bằng trình duyệt thật**: mô tả
  xung đột (`Conflict.description`) do `FusionEngine` tự sinh nhúng thẳng
  token enum tiếng Anh ("SUPPORT", "NEGATIVE") và `Dimension.toString()`
  thô vào câu tiếng Việt — tồn tại từ Phase 6, không bộ test nào bắt được vì
  không test nào assert nội dung `description`. Chỉ lộ ra khi render thật
  ra HTML và soi bằng mắt. Sửa bằng cách bỏ hẳn phần nội dung kỹ thuật khỏi
  câu (thông tin đó đã có sẵn dưới dạng `LabeledValue` riêng trên chính
  `Conflict`) — không thể dùng `VietnameseLabels` vì sẽ tạo phụ thuộc vòng
  (`destiny-i18n` phụ thuộc `destiny-fusion`, không phải chiều ngược lại)
- **Bắt được 1 lỗi kiến trúc thật khi verify với Supabase (database bền
  vững, không phải H2 reset mỗi lần)**: `MethodologyRegistrySeeder` vốn chỉ
  seed khi "chưa có version nào" — nên khi nâng trạng thái `TAROT_RWS`/
  `NUMEROLOGY_PYTHAGOREAN`/`CALENDAR_VN_TRADITIONAL` lên `PRODUCTION_READY`
  trong code, database Supabase đã seed từ trước trong phiên không hề được
  cập nhật, âm thầm. Sửa bằng cách versioned từng entry ("1.1" cho 3 mục
  vừa nâng cấp) và seed theo cặp (methodologyId, version) — đúng triết lý
  "thay đổi là bump version, không phải thay đổi ngầm" áp dụng nhất quán
  với mọi nơi khác trong dự án

### Added — R11/R8: Tarot + Numerology Vietnamese interpretive content

- Cả hai engine giờ đây phát sinh signal thật lần đầu tiên. Trước đây
  `TarotEngine`/`NumerologyEngine` luôn trả `signals=[]` vì chưa có nội
  dung diễn giải để gán Dimension/Polarity — mọi kết quả Fusion trước đó,
  kể cả lần chạy Supabase trực tiếp trong phiên này, đều ra
  `INSUFFICIENT_EVIDENCE` dù Fusion đã cài đặt đúng
- **Tarot (R11)**: viết nội dung tiếng Việt cho cả 78 lá (từ khóa xuôi/ngược,
  polarity xuôi/ngược, và 5 trường ý nghĩa career/finance/relationship/
  decision/general mỗi lá) — bám theo truyền thống Rider-Waite-Smith hội tụ
  hàng chục năm (khởi nguồn từ *Pictorial Key to the Tarot* của A.E. Waite,
  1910, phạm vi công cộng), không tự bịa, không copy nguyên văn từ sách bản
  quyền hiện đại nào
- **Numerology (tương đương R8)**: viết nội dung cho cả 65 tổ hợp (5 loại số ×
  13 giá trị 1-9/11/22/33), bám theo kho tàng ý nghĩa Pythagorean hội tụ
  rộng rãi
- **Nội dung là dữ liệu Java tĩnh, versioned, không sinh lúc runtime** — đúng
  ràng buộc của R11: "Card meanings must be authored or sourced, not
  generated by the LLM at runtime" (CLAUDE.md Rule B)
- **Quyết định thiết kế đã trình bày rõ**: polarity gán theo từng lá×orientation
  (không theo từng dimension riêng — đơn giản hóa có ghi nhận); strength theo
  quy ước Tarot chuẩn (Major→STRONG, lá chân dung→MEDIUM, lá số thường→WEAK);
  không lá nào được đánh dấu `critical`
- Nội dung được viết song song bằng 6 agent độc lập (Major Arcana, 4 bộ Minor,
  Numerology), mỗi agent tự chấm polarity theo ý nghĩa truyền thống riêng của
  từng lá/số thay vì áp một khuôn cố định — và tự báo cáo trung thực những
  trường hợp polarity mang tính diễn giải/tranh cãi (ví dụ: Five of Wands
  đảo ngược, Death xuôi, Seven of Swords xuôi)
- `TAROT_RWS` và `NUMEROLOGY_PYTHAGOREAN` chuyển từ `CONTENT_REQUIRED` sang
  `PRODUCTION_READY` trong registry
- **Kết quả Fusion thật đầu tiên của dự án**: `ScenarioApiIntegrationTest`
  (kịch bản BUSINESS, seed cố định) giờ ra `MAJOR_CONFLICT` thay vì
  `INSUFFICIENT_EVIDENCE` — ba lá Tarot rút được mang polarity trái chiều
  nhau (SUPPORT/CAUTION/NEGATIVE), nên mọi dimension đều có mâu thuẫn thật.
  Đây chính là Rule E ("mâu thuẫn là kết quả hợp lệ") thể hiện bằng dữ liệu
  thật, không phải một sự đồng thuận giả tạo
- 257 test (không đổi về số lượng — các test khẳng định "chưa có signal"
  được viết lại để khẳng định "có signal thật", thay vì thêm test mới)

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
