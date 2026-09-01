package io.destinyos.engines.iching;

import java.util.ArrayList;
import java.util.List;

/**
 * Hào từ (爻辭) for hexagrams 33-48, King Wen order.
 *
 * <p>Hán tự from zh.wikisource.org; Hán-Việt and nghĩa from Ngô Tất Tố.
 * See {@link LineJudgment} for provenance (R24/R25).
 */
final class LineJudgments3 {

    private LineJudgments3() {
    }

    static List<LineJudgment> entries() {
        List<LineJudgment> list = new ArrayList<>();

        // Quẻ 33 — ĐỘN (遯)
        list.add(new LineJudgment(33, 1, "Sơ Lục", "遯尾，厲，勿用有攸往。", "Độn vỹ, lệ! Vật dụng hữa du vãng.", "Hào Sáu Đầu: Trốn đuôi, nguy! Chớ dùng có thửa đi.", 538, true, null));
        list.add(new LineJudgment(33, 2, "Lục Nhị", "執之用黃牛之革，莫之勝說。", "Chấp chi dụng hoàng ngưu chi cách, mạc chi thắng[3] thoát.", "Hào Sáu Hai: Giữ đó, dùng da trâu vàng không ai trút nổi?", 539, true, null));
        list.add(new LineJudgment(33, 3, "Cửu Tam", "系遯，有疾厲，畜臣妾吉。", "Hệ độn, hữu tật, lệ! Xúc thần thiếp, cát.", "Hào Chín Ba: Vướng trốn, có tật nguy! Nuôi đầy tớ nàng hầu, tốt.", 540, true, null));
        list.add(new LineJudgment(33, 4, "Cửu Tứ", "好遯君子吉，小人否。", "Hiếu độn, quân tử cát, tiểu nhân phủ[4].", "Hào Chín Tư: Yêu trốn, đấng quân tử tốt, kẻ tiểu nhân không.", 541, false, null));
        list.add(new LineJudgment(33, 5, "Cửu Ngũ", "嘉遯，貞吉。", "Gia Độn, trinh cát.", "Hào Chín Năm: Tốt trốn, chính tốt.", 542, false, null));
        list.add(new LineJudgment(33, 6, "Thượng Cửu", "肥遯，无不利。", "Phì độn, vô bất lợi.", "Hào Chín Trên: Béo trốn, không gì không lợi.", 543, true, null));

        // Quẻ 34 — ĐẠI TRÁNG (大壯)
        list.add(new LineJudgment(34, 1, "Sơ Cửu", "壯于趾，征凶，有孚。", "Tráng vu chỉ, chính hung, hữu phu.", "Hào Chín Đầu: Mạnh ở ngón chân, đi thì hung, có tin.", 549, false, null));
        list.add(new LineJudgment(34, 2, "Cửu Nhị", "貞吉。", "Trình cát", "Hào Chín Hai: Chính tốt.", 550, true, null));
        list.add(new LineJudgment(34, 3, "Cửu Tam", "小人用壯，君子用罔，貞厲。羝羊觸藩，羸其角。", "Tiểu nhân dụng tráng, quân tử dụng võng, trinh lệ, đê dương xúc phiên, doanh kỳ giốc.", "Hào Chín Ba: Kẻ tiểu nhân dùng mạnh, đấng quân tử dùng chẳng, chính nguy! Dê đực húc giậu, mắc thửa sừng.", 551, true, null));
        list.add(new LineJudgment(34, 4, "Cửu Tứ", "貞吉悔亡，藩決不羸，壯于大輿之輹。", "Trinh cát, hối vong, phiên quyết bất doanh, tráng vụ đại dư chỉ phúc.", "Hào Chín Tư: Chính thì tốt, ăn năn mất, phên bựt chẳng mắc, mạnh ở vành trục xe lớn.", 552, true, "Bộ trích cũ nối liền phần lời bình của sách (mở đầu bằng GIẢI NGHĨA) vào ngay sau lời dịch nghĩa, không có dấu phân cách, khiến trường nghia dài 1270 ký tự thay vì 84. Đã cắt tại mốc GIẢI NGHĨA; phần giữ lại là nguyên văn lời dịch của Ngô Tất Tố, không sửa một chữ. Lời bình bị cắt không mất giá trị nhưng không thuộc hào từ, nên không được để lẫn trong trường dịch nghĩa."));
        list.add(new LineJudgment(34, 5, "Lục Ngũ", "喪羊于易，无悔。", "Táng dương vu dị, vỏ hổi.", "Hào Sáu Năm: Mất dê ở sự dễ không ăn năm.", 554, true, null));
        list.add(new LineJudgment(34, 6, "Thượng Lục", "羝羊觸藩，不能退，不能遂，无攸利，艱則吉。", "Dê dương xúc phiên, bất năng thoái, bất năng toại, vô du lợi, gian tắc cát.", "Hào Sáu Trên: Dê đực húc giậu, chẳng hay lui, chẳng hai toại, không thửa lợi, khó thì tốt.", 555, true, null));

        // Quẻ 35 — TẤN (晉)
        list.add(new LineJudgment(35, 1, "Sơ Lục", "晉如，摧如，貞吉。罔孚，裕无咎。", "Tấn như, tồi như, trình cát, võng phu, dạ vô cữu.", "Hào Sáu Đầu; Dường tiến lên vậy, dường đun lại vậy, chính thì tốt, chẳng tin, khoan thai không lỗi.", 561, true, null));
        list.add(new LineJudgment(35, 2, "Lục Nhị", "晉如，愁如，貞吉。受茲介福，于其王母。", "Tấn như, sầu như, trinh cát, thụ tư giới phúc vu kỳ vương mẫu.", "Hào Sáu Hai: Dường tiến vậy, dường sầu vậy, chính thì tốt, nhận phúc lớn ấy chưng thuở bà nội.", 562, true, null));
        list.add(new LineJudgment(35, 3, "Lục Tam", "眾允，悔亡。", "Chúng doãn, hối vong.", "Hào Sáu Ba: Mọi người tin, ăn năn mất.", 564, true, null));
        list.add(new LineJudgment(35, 4, "Cửu Tứ", "晉如鼫鼠，貞厲。", "Tấn như, thạch thử trinh lệ.", "Hào Chín Tư: Dường tiến vậy, con chuột đồng, chính bền, nguy!", 564, true, null));
        list.add(new LineJudgment(35, 5, "Lục Ngũ", "悔亡，失得勿恤，往吉无不利。", "Hối vong, thất đắc vật tuất, vãng cát, vô bất lợi.", "Hào Sáu Năm: Ăn năn mất, mất được chớ lo, đi thì tốt, không gì không lợi.", 566, true, null));
        list.add(new LineJudgment(35, 6, "Thượng Cửu", "晉其角，維用伐邑，厲吉无咎，貞吝。", "Tấn kỳ dốc, duy dụng phạt ấp, lệ! Cát vô cữu, trinh lận.", "Hào Chín Trên: Tiến thửa sừng, bui dùng đánh làng, lo thì tốt, không lỗi, trinh thì đáng tiếc.", 567, true, null));

        // Quẻ 36 — MINH DI (明夷)
        list.add(new LineJudgment(36, 1, "Sơ Cửu", "明夷于飛，垂其翼。君子于行，三日不食，有攸往，主人有言。", "Minh di vu phi, thùy kỳ dực; quân tử vu hành, tam nhật bất thực; hữu du vãng; chủ nhân hữu ngôn.", "Hào Chín Đầu: Sáng đau chưng bay; đủ thửa cánh; đấng quân tử chưng đi, ba ngày chẳng ăn; có thửa đi; người chủ có nói.", 572, true, null));
        list.add(new LineJudgment(36, 2, "Lục Nhị", "明夷，夷于左股，用拯馬壯，吉。", "Minh di, di vu tả cổ, dụng chửng, mã tráng cát", "Hào Sáu Hai: Sáng đau, đau ở đùi bên tả, dùng cứu, ngựa mạnh tốt.", 574, true, null));
        list.add(new LineJudgment(36, 3, "Cửu Tam", "明夷于南狩，得其大首，不可疾貞。", "Minh di vu nam thù, đắc kỳ đại thủ, bất khả tật trinh.", "Hào Chín Ba: Sáng đau chưng cuộc săn bên nam được thừa đầu lớn, chẳng khá kíp chính.", 575, false, "Bộ trích cũ nối liền phần lời bình của sách (mở đầu bằng GIẢI NGHĨA) vào ngay sau lời dịch nghĩa, không có dấu phân cách, khiến trường nghia dài 1484 ký tự thay vì 84. Đã cắt tại mốc GIẢI NGHĨA; phần giữ lại là nguyên văn lời dịch của Ngô Tất Tố, không sửa một chữ. Lời bình bị cắt không mất giá trị nhưng không thuộc hào từ, nên không được để lẫn trong trường dịch nghĩa."));
        list.add(new LineJudgment(36, 4, "Lục Tứ", "入于左腹，獲明夷之心，于出門庭。", "Nhập vu tả phúc, hoạch minh di chi tâm, vu xuất môn định.", "Hào Sáu Tư: Vào chưng bụng bên tả được cái lòng sáng đau: chưng ra cửa sân.", 577, false, null));
        list.add(new LineJudgment(36, 5, "Lục Ngũ", "箕子之明夷，利貞。", "Cơ Tử chi minh di, lợi trinh.", "Hào Sáu Năm, ông Cơ Tử chưng sáng đau, lợi về chính bền.", 579, true, null));
        list.add(new LineJudgment(36, 6, "Thượng Lục", "不明晦，初登于天，后入于地。", "Bất minh hối; sơ đăng vu thiên, hậu nhập vu địa.", "Hào Sáu Trên: Chẳng sáng; tối đầu lên chưng trời, sau vào chưng đất.", 580, true, null));

        // Quẻ 37 — GIA NHÂN (家人)
        list.add(new LineJudgment(37, 1, "Sơ Cửu", "閑有家，悔亡。", "Nhàn hữu gia, hối vong.", "Hào Chín Đầu: Ngăn ngừa có nhà, ăn năn mất.", 585, true, null));
        list.add(new LineJudgment(37, 2, "Lục Nhị", "无攸遂，在中饋，貞吉。", "Vô du toại, tại trung quỹ, trinh cát.", "Hào Sáu Hai: không thửa thỏa, ở trong, chủ việc ăn uống, chính tốt.", 587, true, null));
        list.add(new LineJudgment(37, 3, "Cửu Tam", "家人嗃嗃，悔厲吉；婦子嘻嘻，終吝。", "Gia nhân hạc hạc, hối, lệ, cát: phu tử hy hy. chung lận.", "Hào Chín Ba: Người nhà nem nép (?), hối dữ, tốt; vợ con hơn hớn, sau chót thẹn tiếc.", 588, true, null));
        list.add(new LineJudgment(37, 4, "Lục Tứ", "富家，大吉。", "Phú gia, đại cát", "Hào Sáu Tư: Giàu nhà, cả tốt.", 589, false, "Bộ trích cũ nối liền phần lời bình của sách (mở đầu bằng GIẢI NGHĨA) vào ngay sau lời dịch nghĩa, không có dấu phân cách, khiến trường nghia dài 430 ký tự thay vì 29. Đã cắt tại mốc GIẢI NGHĨA; phần giữ lại là nguyên văn lời dịch của Ngô Tất Tố, không sửa một chữ. Lời bình bị cắt không mất giá trị nhưng không thuộc hào từ, nên không được để lẫn trong trường dịch nghĩa."));
        list.add(new LineJudgment(37, 5, "Cửu Ngũ", "王假有家，勿恤。吉。", "Vương cách hữu gia, vật, tuất cát.", "Hào Chín Năm: Vua đến có nhà, chớ lo, tốt.", 590, false, null));
        list.add(new LineJudgment(37, 6, "Thượng Cửu", "有孚威如，終吉。", "Hữu phu uy như, chung cát", "Hào Chín Trên: Có tin, đường oai nghiêm vậy, trọn tốt,", 592, false, null));

        // Quẻ 38 — KHUÊ (睽)
        list.add(new LineJudgment(38, 1, "Sơ Cửu", "悔亡，喪馬勿逐，自復；見惡人无咎。", "Hối vong: táng mả, vật trục, tự phục, kiến ác nhân, vô cữu.", "Hào Chín Đầu: Ăn năn mất, mất ngựa, chớ đuổi theo tự nhiên trở lại, thấy người ác, không lỗi.", 599, true, null));
        list.add(new LineJudgment(38, 2, "Cửu Nhị", "遇主于巷，无咎。", "Ngộ chủ vu hạng, vô cữu.", "Hào Chín Hai: Gặp chúa ở ngõ, không lỗi.", 600, true, null));
        list.add(new LineJudgment(38, 3, "Lục Tam", "見輿曳，其牛掣，其人天且劓，无初有終。", "Kiến dư duệ, kỳ ngưu xiết, kỳ nhân, thiên thả ty, vô sơ hữu chung.", "Hào Sáu Ba: Thấy xe kéo, thửa trâu kìm, thửa người gọt đầu và xẻo mũi. Không đầu, có chót.", 602, true, null));
        list.add(new LineJudgment(38, 4, "Cửu Tứ", "睽孤，遇元夫，交孚，厲无咎。", "Khuê cô, ngộ nguyên phu, giao phu, lệ, vôcữu.", "Hào Chín Tư: Lìa côi, gặp chàng lành, tin lẫn, nguy, không lỗi.", 603, true, null));
        list.add(new LineJudgment(38, 5, "Lục Ngũ", "悔亡，厥宗噬膚，往何咎。", "Hối vong, quyết tôngphệ phu, vãng, hà cữu?", "Ăn năn mất, thửa họ cắn da, đi, lỗi gì?", 605, true, null));
        list.add(new LineJudgment(38, 6, "Thượng Cửu", "睽孤， 見豕負涂，載鬼一車， 先張之弧，后說之弧，匪寇婚媾，往遇雨則吉。", "Khuê cố kiến thỉ phụ đồ, tái quỉ nhất xa, tiên trương chi hồ, hậu thoát chi hồ, phỉ khấu, hôn cấu! Văng, ngộ vũ tắc cát.", "Hào Chín Trên: Lìa cô thấy lợn đội bùn, chở ma một xe, trước giương chưng cung, sau tháo chưng cung, chẳng phải giặc, dâu gia, đi, gặp mưa thì tốt.", 606, false, null));

        // Quẻ 39 — KIỂN (蹇)
        list.add(new LineJudgment(39, 1, "Sơ Lục", "往蹇，來譽。", "Vãng kiển, lai dự.", "Hào Sáu Đầu: Đi kiển, lại khen.", 613, false, null));
        list.add(new LineJudgment(39, 2, "Lục Nhị", "王臣蹇蹇，匪躬之故。", "Vương thần kiển kiển, phí cung chi cố.", "Hào Sáu Hai: Tôi vua kiển kiển, chẳng phải cớ của mình.", 614, true, null));
        list.add(new LineJudgment(39, 3, "Cửu Tam", "往蹇來反。", "Vâng kiểu lai phản.", "Hào Chín Ba: Đi kiển lại thì lại.", 615, false, null));
        list.add(new LineJudgment(39, 4, "Lục Tứ", "往蹇來連。", "Vãng kiển lai liên.", "Hào Sáu Tư: Đi kiển, lại thì liền.", 617, false, null));
        list.add(new LineJudgment(39, 5, "Cửu Ngũ", "大蹇朋來。", "Đại kiển bằng lai.", "Hào Chín Năm: Cả Kiển bạn lại.", 618, false, null));
        list.add(new LineJudgment(39, 6, "Thượng Lục", "往蹇來碩，吉；利見大人。", "Vãng kiển lai thạc, cát, lợi kiến đại nhân.", "Hào Sáu trên: Đi kiển lại lớn, tốt, lợi về sự thấy người lớn.", 619, false, null));

        // Quẻ 40 — GIẢI (解)
        list.add(new LineJudgment(40, 1, "Sơ Lục", "无咎。", "Vô cữu.", "Hào Sáu Đầu: Không lỗi.", 626, true, null));
        list.add(new LineJudgment(40, 2, "Cửu Nhị", "田獲三狐，得黃矢，貞吉。", "Điền hoạch tam hồ, đắc hoàng thi, trinh cát.", "Hào Chín Hai: Săn được ba con cáo, được tên vàng, chính bền thì tốt.", 627, true, null));
        list.add(new LineJudgment(40, 3, "Lục Tam", "負且乘，致寇至，貞吝。", "Phụ thả thừa, tri khấu trí, trinh lận.", "Hào Sáu Ba: Đội và cưỡi, dắt giặc đến, chính bền cũng đáng tiếc.", 629, false, null));
        list.add(new LineJudgment(40, 4, "Cửu Tứ", "解而拇，朋至斯孚。", "Giải nhi mẫu, bằng chi tư phu.", "Hào Chín Tư: Giải ngón chân cái mày, bằng đến ấy tin.", 630, false, null));
        list.add(new LineJudgment(40, 5, "Lục Ngũ", "君子維有解，吉；有孚于小人。", "Quân tử duy hữu giải, cắt, hữu phu vu tiểu nhân.", "Hào Sáu Năm: Đấng quân tử chỉ có giải, tốt, có tin chưng kẻ tiểu nhân.", 632, false, null));
        list.add(new LineJudgment(40, 6, "Thượng Lục", "公用射隼，于高墉之上，獲之，无不利。", "Công dụng xạ chuẩn vu cao dung chi thượng, hoạch chi, vô bất lợi.", "Hào Sáu Trên: Ông dùng bắn chim cắt ở trên, tường cao, được nó, không gì không lợi.", 633, true, null));

        // Quẻ 41 — TỔN (損)
        list.add(new LineJudgment(41, 1, "Sơ Cửu", "已事遄往，无咎，酌損之。", "Dĩ sự, xuyền váng, vô cữa, chước tổn chi.", "Hào Chín Đầu: Xong việc[3], mau đi, không lỗi, châm chước mà bớt đấy.", 641, true, "Bộ trích cũ nối liền phần lời bình của sách (mở đầu bằng GIẢI NGHĨA) vào ngay sau lời dịch nghĩa, không có dấu phân cách, khiến trường nghia dài 1089 ký tự thay vì 69. Đã cắt tại mốc GIẢI NGHĨA; phần giữ lại là nguyên văn lời dịch của Ngô Tất Tố, không sửa một chữ. Lời bình bị cắt không mất giá trị nhưng không thuộc hào từ, nên không được để lẫn trong trường dịch nghĩa."));
        list.add(new LineJudgment(41, 2, "Cửu Nhị", "利貞，征凶，弗損益之。", "Lợi trinh, chinh hung! Phất tổn, ích chi.", "Hào Chín Hai: Lợi về chính bền, đi thì hung! Chớ bớt, thêm đấy!", 643, true, null));
        list.add(new LineJudgment(41, 3, "Lục Tam", "三人行，則損一人；一人行，則得其友。", "Tam nhân hành tắc tổn nhất nhân, nhất nhân hành tắc đắc kỳ hữu.", "Hào Sáu Ba: Ba người đi thì tổn một người, một người đi thì được thửa bạn.", 644, true, null));
        list.add(new LineJudgment(41, 4, "Lục Tứ", "損其疾，使遄有喜，无咎。", "Tổn tôn, sử xuyền hữu hỷ, nguyên cắt, vô cữu.", "Hào Sáu Tư: Bớt thửa tật, khiến chóng có mừng, cả tốt, không lỗi.", 646, true, null));
        list.add(new LineJudgment(41, 5, "Lục Ngũ", "或益之，十朋之龜弗克違，元吉。", "Hoặc ích chi thập bằng chi quy, phất khắc vi, nguyên cát.", "Hào Sáu Năm: Hoặc ích cho đấy, chưng con rùa mười “bằng”[6] chẳng hay trái, cả tốt!", 647, true, null));
        list.add(new LineJudgment(41, 6, "Thượng Cửu", "弗損益之，无咎，貞吉，利有攸往，得臣无家。", "Phất tổn, ích chi, vô cữu, trinh cát, lợi hữu du vãng, đắc thần vô gia.", "Hào Chín Trên: Chẳng bớt, thêm đấy, không lỗi, chính bền tốt, lợi có thửa đi, được bề tôi không nhà.", 649, true, null));

        // Quẻ 42 — ÍCH (益)
        list.add(new LineJudgment(42, 1, "Sơ Cửu", "利用為大作，元吉，无咎。", "Lợi dụng vi đại tác, nguyên cát, vô cữu.", "Hào Chín Đầu: Lợi dùng làm việc lớn, cả tốt, không lỗi.", 657, true, null));
        list.add(new LineJudgment(42, 2, "Lục Nhị", "或益之，十朋之龜弗克違，永貞吉。王用享于帝，吉。", "Hoặc ích chi thập bằng chi qui, phất khắc vi vĩnh trinh cát, vương dụng hưởng vu đế, cát!", "Hào Sáu Hai: Hoặc ích đấy, chưng rùa mười bằng[3] chẳng hay trái, vĩnh viễn chính bền, tốt, vua dùng hưởng chưng trời, tốt!", 658, true, null));
        list.add(new LineJudgment(42, 3, "Lục Tam", "益之用凶事，无咎。有孚中行，告公用圭。", "Ích chi, dụng hung sự, vô cữu, hữu phu, trung hàng, cáo công dụng khuê.", "Hào Sáu Ba: Ích đây, dùng việc hung, không lỗi, có tin, đường giữa, bảo tước. Công dùng ngọc khuê.", 660, true, null));
        list.add(new LineJudgment(42, 4, "Lục Tứ", "中行，告公從。利用為依遷國。", "Trung hành, cáp công tòng, lợi dụng vi y thiên quốc.", "Hào Sáu Tư: Đường giữa, tâu tước công theo, lợi dụng làm tựa, dời nước.", 662, false, null));
        list.add(new LineJudgment(42, 5, "Cửu Ngũ", "有孚惠心，勿問元吉。有孚惠我德。", "Hữa phu, huệ tâm, vật vấn, nguyên cắt, hữu phu, huệ ngã đức.", "Hào Chín Năm: Có tin lòng ơn, chớ hỏi, cả tốt, có tin, ơn đức ta.", 663, true, "Bộ trích cũ nối liền phần lời bình của sách (mở đầu bằng GIẢI NGHĨA) vào ngay sau lời dịch nghĩa, không có dấu phân cách, khiến trường nghia dài 934 ký tự thay vì 65. Đã cắt tại mốc GIẢI NGHĨA; phần giữ lại là nguyên văn lời dịch của Ngô Tất Tố, không sửa một chữ. Lời bình bị cắt không mất giá trị nhưng không thuộc hào từ, nên không được để lẫn trong trường dịch nghĩa."));
        list.add(new LineJudgment(42, 6, "Thượng Cửu", "莫益之，或擊之，立心勿恆，凶。", "Mạc ích chi, hoặc kích chi, lập tâm vật hằng, hung!", "Hào Chín Trên: Chẳng ích nó, hoặc đánh nó, lập tâm chớ thường hung!", 665, true, null));

        // Quẻ 43 — QUẢI (夬)
        list.add(new LineJudgment(43, 1, "Sơ Cửu", "壯于前趾，往不勝為咎。", "Tráng vu tiền chỉ, vãng bất thắng, vi cữu.", "Hào Chín Đầu: Mạnh chưng ngón chân trước, đi thẳng được, là lỗi.", 674, false, null));
        list.add(new LineJudgment(43, 2, "Cửu Nhị", "惕號，莫夜有戎，勿恤。", "Dịch hào, mộ dạ hữu nhung, vật tuất", "Hào Chín Hai: Sợ kêu, đêm hôm có quân, chớ ngại.", 675, true, null));
        list.add(new LineJudgment(43, 3, "Cửu Tam", "壯于頄，有凶。君子夬夬，獨行遇雨，若濡有慍，无咎。", "Tráng vu cưu, hữu hung, quân tử quải quải, độc hành ngộ vũ, nhược nhu, hữu uấn, vô cữu.", "Hào Chín Ba: Mạnh chưng gồ má, có sự hung, đấng quân tử quyết quyết, đi một mình, gặp mưa, đường ướt, có giận, không lỗi.", 676, true, null));
        list.add(new LineJudgment(43, 4, "Cửu Tứ", "臀无膚，其行次且。牽羊悔亡，聞言不信。", "Điêu vô phu, kỳ hành từ thư, khiên dương, hối vong, văn ngôn bất tín.", "Hào Chin Tư: Đít không da, thửa đi chật vật, dắt dê, ăn năn mất, nghe nói chẳng tin.", 678, true, null));
        list.add(new LineJudgment(43, 5, "Cửu Ngũ", "莧陸夬夬，中行无咎。", "Nghiến lục quải quải, trung hàng vô cữu.", "Hào Chín Năm: Rau sam quyết quyết, đường giữa không lỗi.", 679, true, null));
        list.add(new LineJudgment(43, 6, "Thượng Lục", "无號，終有凶。", "Vô hào, chung hữu hung.", "Hào Sáu Trên: Không kêu, sau chót có hung.", 681, true, null));

        // Quẻ 44 — CẤU (姤)
        list.add(new LineJudgment(44, 1, "Sơ Lục", "系于金柅，貞吉，有攸往，見凶，羸豕孚踟躅。", "Hệ vu kim nỷ, trính cát, hữu du vãng, kiến hung, luy thỉ phu chích chúc.", "Hào Sáu Đầu: Buộc chưng neo sắt, chính tốt, có thửa đi, thấy hung, con lợn còm tin nhảy nhót.", 688, true, null));
        list.add(new LineJudgment(44, 2, "Cửu Nhị", "包有魚，无咎，不利賓。", "Bao hữu ngư, vô cữu, bất lợi tân.", "Hào Chín Hai: Bọc có cá, không lỗi chẳng lợi khách.", 690, true, null));
        list.add(new LineJudgment(44, 3, "Cửu Tam", "臀无膚，其行次且，厲，无大咎。", "Điến vô phu, kỳ hành từ thư, lệ! Vô đại cữu.", "Hào Chín Ba: Đít không da, thửa đi chật vật, nguy! Không lỗi lớn.", 691, true, null));
        list.add(new LineJudgment(44, 4, "Cửu Tứ", "包无魚，起凶。", "Bao vô ngư, khởi hung. Dịch âm. -", "Hào Chín Tư: Bọc không cá, dấy hung.", 692, true, null));
        list.add(new LineJudgment(44, 5, "Cửu Ngũ", "以杞包瓜，含章，有隕自天。", "Dĩ kỷ bảo qua, hàm chương, hữu vẫn tự thiên,", "Hào Chín Năm: lấy cây kỷ bọc quả dưa, ngậm văn vẻ, có sa tự trời.", 693, false, null));
        list.add(new LineJudgment(44, 6, "Thượng Cửu", "姤其角，吝，无咎。", "Cầu kỳ giốc, lận, vô cữu.", "Hào Chín Trên: Gặp thửa sừng, đáng tiếc, không lỗi.", 695, true, null));

        // Quẻ 45 — TỤY (萃)
        list.add(new LineJudgment(45, 1, "Sơ Lục", "有孚不終，乃亂乃萃，若號一握為笑，勿恤，往无咎。", "Hữu phu, bất chung, nãi loạn, nãi tụy, nhược hào, nhấtác vi tiếu, vật tuất, vãng vô cữu.", "Hào Chín Đầu: Có tin, không chót, bèn loạn, bèn họp, bằng kêu, một nắm làm cười, chớ lo, đi không lỗi.", 703, true, "Sách in nhãn \"初九 / Sơ Cửu\" cho hào này, nhưng quẻ Tụy (Đoài trên, Khôn dưới) có hào 1 là hào ÂM nên nhãn đúng phải là 初六 / Sơ Lục; cổ văn cũng chép 初六. Nhãn đã sửa vì đây là dữ kiện cấu trúc máy phải dùng để khớp hào. LƯU Ý: phần dịch nghĩa dưới đây giữ NGUYÊN VĂN của Ngô Tất Tố và vẫn mở đầu bằng \"Hào Chín Đầu\" — sách sai nhất quán ở cả nhãn lẫn lời dịch, và bản dịch của tác giả không bị sửa."));
        list.add(new LineJudgment(45, 2, "Lục Nhị", "引吉，无咎，孚乃利用禴。", "Dẫn cát, vô cữu, phu nãi lợi, dụng Thược.", "Hào Sáu Hai: Dẫn tốt, không lỗi, tin bèn lời, dùng tế Thược.", 705, true, null));
        list.add(new LineJudgment(45, 3, "Lục Tam", "萃如，嗟如，无攸利，往无咎，小吝。", "Tụy như, tư như, vô du lợi, vãng vô cữu, tiểu lận.", "Hào Sáu Ba: Dường họp vậy, dường than vậy không thửa lợi, đi không lỗi, hơi tiếc.", 707, true, null));
        list.add(new LineJudgment(45, 4, "Cửu Tứ", "大吉，无咎。", "Đại cát, vô cữu.", "Hào Chín Tư: Cả tốt, không lỗi.", 708, true, null));
        list.add(new LineJudgment(45, 5, "Cửu Ngũ", "萃有位，无咎。匪孚，元永貞，悔亡。", "Tụy hữu vị, vô cữu, phỉ phu, nguyên vĩnh trinh hối vong.", "Hào Chín Năm: Họp có ngói, không lỗi, chẳng tin, cả, dài, chính, ăn năn mất.", 709, true, null));
        list.add(new LineJudgment(45, 6, "Thượng Lục", "齎咨涕洟，无咎。", "Tê tư, thế di, vô cữu.", "Hào Sáu Trên: Than thở, nước mắt, nước mũi, không lỗi.", 711, true, "Bản in của sách gõ \"Háo\" thay cho \"Hào\" ở đầu lời dịch nghĩa. Sửa vì nhãn suy dẫn được độc lập với chính dữ liệu: label là Thượng Lục, tức hào 6 và là hào âm, nên nhãn đúng là \"Hào Sáu Trên\" — đúng chữ sách đã in, chỉ sai một dấu. Không chữ nào khác trong lời văn của Ngô Tất Tố bị đổi."));

        // Quẻ 46 — THĂNG (升)
        list.add(new LineJudgment(46, 1, "Sơ Lục", "允升，大吉。", "Doãn thăng, đại cát.", "Hào Sáu Đầu: Tin lên, cả tốt.", 716, false, null));
        list.add(new LineJudgment(46, 2, "Cửu Nhị", "孚乃利用禴，无咎。", "Phu nãi lợi, dụng Thược, vô cữu.", "Hào Chín Hai: Tin bèn lợi, dùng tế Thược, không lỗi.", 718, true, null));
        list.add(new LineJudgment(46, 3, "Cửu Tam", "升虛邑。", "Thăng hư ấp.", "Hào Chín Ba: Lên làng trống không.", 719, false, null));
        list.add(new LineJudgment(46, 4, "Lục Tứ", "王用亨于岐山，吉无咎。", "Vượng dụng hưởng[2] vu Kỳ Sơn, cát, vô cữu.", "Hào Sáu Tư: Vua dùng hưởng ở núi Kỳ, tốt, không lỗi.", 720, true, null));
        list.add(new LineJudgment(46, 5, "Lục Ngũ", "貞吉，升階。", "Trinh cát, thăng giai.", "Hào Sáu Năm: Chính bền, tốt, lên thềm[3].", 721, false, null));
        list.add(new LineJudgment(46, 6, "Thượng Lục", "冥升，利于不息之貞。", "Minh thăng, lợi vu bất tức chi trinh.", "Hào Sáu Trên: Tối lên, lợi về sự chính bền chẳng nghĩ.", 722, true, null));

        // Quẻ 47 — KHỐN (困)
        list.add(new LineJudgment(47, 1, "Sơ Lục", "臀困于株木，入于幽谷，三歲不覿。", "Điến khốn vu châu mộc, nhập vu u cốc, tam tuế bất thục.", "Hào Sáu Đầu: Đít khốn chưng trồi cây, vào chưng hang tối, ba năm chẳng thấy.", 728, false, null));
        list.add(new LineJudgment(47, 2, "Cửu Nhị", "困于酒食，朱紱方來，利用亨祀，征凶，无咎。", "Khốn vu tửu tửu thực, chu phất phương lai, lợi dụng hưởng tự, chính hung, vô cữu.", "Hào Chín Hai: Khốn chưng rượu cơm, cái phất đỏ đương lại, lợi dùng cúng tế, đi hung không lỗi.", 729, true, null));
        list.add(new LineJudgment(47, 3, "Lục Tam", "困于石，據于蒺藜，入于其宮，不見其妻，凶。", "Khốn vu thạch, cứ vu tật lệ, nhập vu kỳ cung, bất kiến thê, hung.", "Hào Sáu Ba: Khốn chưng đá, vin chưng cây cà gai, vào chưng thửa buồng, chẳng thấy thửa vợ, hung.", 731, true, null));
        list.add(new LineJudgment(47, 4, "Cửu Tứ", "來徐徐，困于金車，吝，有終。", "Lai từ từ, khốn vu kim xa, lận hữu chung.", "Hào Chín Tư: Lại thong thả, khốn chưng xe sắt, đáng tiếc! có chót.", 732, false, null));
        list.add(new LineJudgment(47, 5, "Cửu Ngũ", "劓刖，困于赤紱，乃徐有說，利用祭祀。", "Ty ngoạt, khốn vu xích phất, nãi từ hữu duyệt, lợi dụng tế dự.", "Xẻo mũi, chặt chân, khốn chưng cái phất[5] đỏ, bèn thong thả có đẹp lòng, lợi dụng tế tự.", 734, true, null));
        list.add(new LineJudgment(47, 6, "Thượng Lục", "困于葛藟，于臲卼，曰動悔。有悔，征吉。", "Khốn vu cát lũy, vu nghiết ngột, viết: động hối, hữu hối, chính cát.", "Hào Sáu Trên: Khốn chưng cây sắn dây, nhưng cheo leo, rằng: động ăn năn, có ăn năn, đi tốt.", 736, true, null));

        // Quẻ 48 — TỈNH (井)
        list.add(new LineJudgment(48, 1, "Sơ Lục", "井泥不食，舊井无禽。", "Tỉnh nê bất thực, cựu tỉnh vô cầm.", "Hào Sáu Đầu: Giếng bùn chẳng ăn, giếng cũ không chim.", 744, false, "Sách in lặp phần dịch âm hai lần liền nhau (\"...vô cầm Tỉnh mê bất thực, cựu tỉnh vô cầm.\"), bản sau còn sai chính tả \"mê\" thay vì \"nê\". Đã bỏ bản lặp; bản giữ lại khớp đúng 8 chữ của cổ văn 井泥不食，舊井无禽 (8 âm tiết Hán-Việt)."));
        list.add(new LineJudgment(48, 2, "Cửu Nhị", "井谷射鮒，瓮敝漏。", "Tỉnh cốc, xạ phụ, úng tệ lậu.", "Hào Chín Hai: Giếng hang, bắn loài ếch nhái[7], vò nát dò.", 745, true, null));
        list.add(new LineJudgment(48, 3, "Cửu Tam", "井渫不食，為我心惻，可用汲，王明，并受其福。", "Tỉnh điệp bất thực, vi ngã tâm trắc, khả dụng cấp, vương minh, tịnh thụ kỳ phúc", "Hào chín Ba: Giếng trong chẳng ăn, làm sự bùi ngùi cho lòng ta, khá dùng múc nước; vua sáng, cùng chịu thửa phúc.", 746, true, null));
        list.add(new LineJudgment(48, 4, "Lục Tứ", "井甃，无咎。", "Tỉnh thịu, vô cữu.", "Hào Sáu Tư: Giếng xây bờ, không lỗi.", 748, true, null));
        list.add(new LineJudgment(48, 5, "Cửu Ngũ", "井冽，寒泉食。", "Tỉnh liệt, hàn toàn thực.", "Hào Chín Năm: Giếng mát, suối lạnh, ăn.", 749, false, null));
        list.add(new LineJudgment(48, 6, "Thượng Lục", "井收勿幕，有孚元吉。", "Tỉnh thu, vật mục, hữu phu, nguyên cát.", "Hào Sáu Trên: Giếng thu, chớ chùm, có tin, cả tốt.", 750, false, null));

        return List.copyOf(list);
    }
}
