package io.destinyos.engines.iching;

import java.util.ArrayList;
import java.util.List;

/**
 * Hào từ (爻辭) for hexagrams 1-16, King Wen order.
 *
 * <p>Hán tự from zh.wikisource.org; Hán-Việt and nghĩa from Ngô Tất Tố.
 * See {@link LineJudgment} for provenance (R24/R25).
 */
final class LineJudgments1 {

    private LineJudgments1() {
    }

    static List<LineJudgment> entries() {
        List<LineJudgment> list = new ArrayList<>();

        // Quẻ 1 — KIỀN (乾)
        list.add(new LineJudgment(1, 1, "Sơ Cửu", "潛龍勿用。", "Tiềm long vật dụng.", "Hào Chín Đầu: Rồng lặn chớ dùng.", 82, true, "Bộ trích cũ nối liền phần lời bình của sách (mở đầu bằng GIẢI NGHĨA) vào ngay sau lời dịch nghĩa, không có dấu phân cách, khiến trường nghia dài 2035 ký tự thay vì 32. Đã cắt tại mốc GIẢI NGHĨA; phần giữ lại là nguyên văn lời dịch của Ngô Tất Tố, không sửa một chữ. Lời bình bị cắt không mất giá trị nhưng không thuộc hào từ, nên không được để lẫn trong trường dịch nghĩa."));
        list.add(new LineJudgment(1, 2, "Cửu Nhị", "見龍在田，利見大人。", "Hiện long tại điền, lợi kiến đại nhân.", "Hào Chín Hai; Rồng hiện ở ruộng, lợi về sự thấy người lớn.", 84, true, null));
        list.add(new LineJudgment(1, 3, "Cửu Tam", "君子終日乾乾，夕惕若；厲，无咎。", "Quân tử chung nhật kiến kiền, tịch Dịch nhược! Lệ, vô cữu.", "Hào Chín Ba: Đấng quân tử trọn ngày săng sắc[7], tối dương rùng rợn. Nguy! Không lỗi.", 85, false, null));
        list.add(new LineJudgment(1, 4, "Cửu Tứ", "或躍在淵，无咎。", "Hoặc dược tại uyên, vô cửu.", "Hào Chín Tư: Hặc nhảy ở vực, không lỗi.", 86, true, null));
        list.add(new LineJudgment(1, 5, "Cửu Ngũ", "飛龍在天，利見大人。", "Long phi tại thiên, lợi kiến đại nhân.", "Hào Chín Năm: Rồng bay ở trời, lợi về sự thấy người lớn.", 87, true, null));
        list.add(new LineJudgment(1, 6, "Thượng Cửu", "亢龍，有悔。", "Kháng long hữu hối.", "Hào Chín Trên: Rồng quá cực có ăn năn.", 88, false, null));
        list.add(new LineJudgment(1, 0, "Dụng Cửu", "見羣龍无首，吉。", "Kiền quần long vô thủ, cát.", "Hào dùng Chín: Thấy đàn rồng không đầu, tốt!", 89, true, null));

        // Quẻ 2 — KHÔN (坤)
        list.add(new LineJudgment(2, 1, "Sơ Lục", "履霜，堅冰至。", "lý sương, kiên băng chí.", "Hào Sáu Đầu: Xéo sương, váng rắn tới.", 136, false, null));
        list.add(new LineJudgment(2, 2, "Lục Nhị", "直方大，不習无不利。", "Trực, phương, đại, bất tập, vô bất lợi.", "Hào Sáu Hai: Thẳng, vuông, lớn, không phải tập, không gì không lợi.", 138, true, null));
        list.add(new LineJudgment(2, 3, "Lục Tam", "含章，可貞。或從王事，无成有終。", "Hàm chương khả trinh; hoặc tòng vương sự, vô thành hữu chung.", "Hào Sáu Ba: Ngậm văn vẻ, có thể chính; hoặc theo đuổi việc nhà vua, không cậy công? Thì được tốt lành về sau.", 139, true, null));
        list.add(new LineJudgment(2, 4, "Lục Tứ", "括囊，无咎无譽。", "Quát nang, vô cữu, vô dự.", "Hào Sáu Tư: Thắt túi, không lỗi, không khen.", 141, true, null));
        list.add(new LineJudgment(2, 5, "Lục Ngũ", "黃裳，元吉。", "Hoàng thường, nguyên cát", "Hào Sáu Năm: Quần vàng, cả tốt.", 142, true, null));
        list.add(new LineJudgment(2, 6, "Thượng Lục", "龍戰于野，其血玄黃。", "Long chiến vu dã, kỳ huyết huyền hoàng.", "Rồng đánh nhau ở đồng, máu nó xanh vàng.", 129, true, "Sách in \"Dịch nghĩa\" hai lần liên tiếp thay cho \"Dịch âm\" rồi \"Dịch nghĩa\" nên bộ trích tự động bỏ sót mục này. Nội dung tiếng Việt trong sách đầy đủ, không mơ hồ; khôi phục nguyên văn từ tr.129."));
        list.add(new LineJudgment(2, 0, "Dụng Lục", "利永貞。", "Lợi vĩnh trinh.", "Hào dùng Sáu: Lợi về vĩnh viễn chính đính.", 145, true, null));

        // Quẻ 3 — TRUÂN (屯)
        list.add(new LineJudgment(3, 1, "Sơ Cửu", "磐桓，利居貞，利建侯。", "Bàn hoàn, lợi cư trinh, lợi kiến hầu.", "Hào Chín Đầu: Quanh co, lợi về ở chính bền, lợi về dựng tước hầu.", 159, true, null));
        list.add(new LineJudgment(3, 2, "Lục Nhị", "屯如邅如，乘馬班如，匪寇婚媾，女子貞不字，十年乃字。", "Truân như, chiên như, thừa mã ban như! Phỉ khẩu, hôn cấu. Nữ tử trinh bất tự, thập niên nãi tự.", "Hào Sáu Hai: Dường quanh co vậy, đường cưỡi ngựa rẽ ra vậy. Chẳng phải giặc: dâu gia. Con gái trinh tiết không đặt tên chữ, mười năm mới đặt tên chữ.", 161, true, null));
        list.add(new LineJudgment(3, 3, "Lục Tam", "即鹿无虞，惟入于林中，君子幾不如舍，往吝。", "Tức lộc vô ngu, duy nhập vu lâm trung, quân tử cơ bất như xả, vãng lận.", "Hào Sáu Ba: Theo hươu không có ngu nhân, chỉ vào trong rừng. Đấng quân tử biết cơ, không bằng bỏ đi thì hối tiếc.", 163, true, null));
        list.add(new LineJudgment(3, 4, "Lục Tứ", "乘馬班如，求婚媾，往，吉无不利。", "Thừa mã ban như, cầu hôn cấu, vãng cát, vô bất lợi.", "Hào Sáu Tư: Cưỡi ngựa dường rẽ ra vậy, tìm dâu gia, đi thì tốt, không gì không lợi.", 164, true, null));
        list.add(new LineJudgment(3, 5, "Cửu Ngũ", "屯其膏；小貞吉，大貞凶。", "Truân kỳ cao, tiểu trinh cát, đại trinh hung.", "Hào Chín Năm: Gian truân thửa ơn huệ, nhỏ mà trinh thì lành, lớn mà trinh thì dữ.", 165, true, null));
        list.add(new LineJudgment(3, 6, "Thượng Lục", "乘馬班如，泣血漣如。", "Thừa mã ban như, khấp huyết liên như.", "Hào Sáu Trên: Cưỡi ngựa dường rẽ ra vậy; khóc ra máu đầm đìa vậy.", 167, true, null));

        // Quẻ 4 — MÔNG (蒙)
        list.add(new LineJudgment(4, 1, "Sơ Lục", "發蒙，利用刑人，用說桎梏，以往吝。", "Phát mông, lợi dụng hình nhân, dụng thoát chất cốc, dĩ vãng, lận.", "Hào Sáu Đầu: Mở mang trẻ thơ, lợi dùng về sự hình phạt người ta[5] để thoát gông cùm, đi thì hối tiếc.", 174, false, null));
        list.add(new LineJudgment(4, 2, "Cửu Nhị", "包蒙吉，納婦吉，子克家。", "Bao Mông, cát! Nạp phụ, cát Tử khắc gia.", "Hào Chín Hai: Bao dung trẻ thơ, tốt! Nộp vợ[8], tốt! Con trị nhà!", 176, true, null));
        list.add(new LineJudgment(4, 3, "Lục Tam", "勿用取女，見金夫，不有躬，无攸利。", "Vật dụng thử nữ, kiến kim phu, bất hữu cung, vô du lợi", "Hào Sáu Ba: Chớ dừng lấy gái, thây chồng vàng không có mình, không thửa lợi.", 178, true, null));
        list.add(new LineJudgment(4, 4, "Lục Tứ", "困蒙，吝。", "Khẩn mông, lận.", "Hào Sáu Tư: Khốn về tăm tối, hối tiếc.", 179, false, null));
        list.add(new LineJudgment(4, 5, "Lục Ngũ", "童蒙，吉。", "Đồng mông, cát!", "Hào Sáu năm: Trẻ thơ, tốt!", 181, false, null));
        list.add(new LineJudgment(4, 6, "Thượng Cửu", "擊蒙，不利為寇，利禦寇。", "Kích mông, bất lợi vi khấu, lợi ngự khấu.", "Hào Chín Trên: Đánh kẻ tối tăm, không lợi cho sự làm giặc,lợi cho sự chống giặc.", 181, true, null));

        // Quẻ 5 — NHU (需)
        list.add(new LineJudgment(5, 1, "Sơ Cửu", "需于郊，利用恆，无咎。", "Nhu vu giao, lợi dụng hằng, vô cữu.", "Hào Chín Đầu: Đợi ở đồng, lợi về dùng lẽ hằng. Không có lỗi.", 187, true, null));
        list.add(new LineJudgment(5, 2, "Cửu Nhị", "需于沙，小有言，終吉。", "Nhu vu sa, tiểu hữu ngôn, chung cát", "Hào Chín Hai: Đợi ở bãi cát, hơi có điều tiếng, sau chót tốt.", 188, true, null));
        list.add(new LineJudgment(5, 3, "Cửu Tam", "需于泥，致寇至。", "Nhu vu nê, trí khâu chí.", "Hào Chín ba: Đợi ở bùn, dắt gỉặc đến", 189, true, null));
        list.add(new LineJudgment(5, 4, "Lục Tứ", "需于血，出自穴。", "Nhu vu huyết, xuất tự huyệt", "Hào Sáu Tư: Đợi chưng máu, ra tự hang.", 191, false, null));
        list.add(new LineJudgment(5, 5, "Cửu Ngũ", "需于酒食，貞吉。", "Nhu vu tử thực, trinh cát.", "Hào Chín Năm: Đợi chưng rượu cơm, chính tốt!", 192, false, null));
        list.add(new LineJudgment(5, 6, "Thượng Lục", "入于穴，有不速之客三人來，敬之終吉。", "Nhập vu huyệt, hữu bất tốc chi khách tam nhân lại, kinh chi, chung cát,", "Hào Chín Trên: Vào chung hang, có ba người khách không mời mà đến, kính trọng họ, sau chót tốt.", 193, true, null));

        // Quẻ 6 — TỤNG (訟)
        list.add(new LineJudgment(6, 1, "Sơ Lục", "不永所事，小有言，終吉。", "Bất vĩnh sở sự, tiểu hữu ngôn, chung cát.", "Hào Sáu Đầu: Chẳng lâu dài về việc của mình, hơi có điều tiếng, sau tốt.", 199, false, null));
        list.add(new LineJudgment(6, 2, "Cửu Nhị", "不克訟，歸而逋，其邑人三百戶无眚。", "Bất khắc tụng, quỉ nhi bộ, kỳ ấp nhân tam bách hộ, vô sảnh.", "Hào Chín Hai: Không được kiện, về mà trốn người làng mình ba trăm hộ, không có tội lỗi.", 201, true, null));
        list.add(new LineJudgment(6, 3, "Lục Tam", "食舊德，貞厲，終吉。或從王事，无成。", "Thực cực đức, trinh lệ, chung cát hoặc cống vương sự, vô thành.", "Hào Sáu Ba: Ăn về đức cũ, chính bền, lo sợ sau tốt, hoặc theo việc vua, không thành.", 202, true, "Bộ trích cũ nối liền phần lời bình của sách (mở đầu bằng GIẢI NGHĨA) vào ngay sau lời dịch nghĩa, không có dấu phân cách, khiến trường nghia dài 1315 ký tự thay vì 84. Đã cắt tại mốc GIẢI NGHĨA; phần giữ lại là nguyên văn lời dịch của Ngô Tất Tố, không sửa một chữ. Lời bình bị cắt không mất giá trị nhưng không thuộc hào từ, nên không được để lẫn trong trường dịch nghĩa."));
        list.add(new LineJudgment(6, 4, "Cửu Tứ", "不克訟，復即命渝，安貞吉。", "Bất khắc tụng, phục tức mệnh, thầu an trinh cát.", "Hào Chín Tư: Không thể kiện, lại theo mệnh đổi ra yên bề chính bền, tốt!", 204, false, null));
        list.add(new LineJudgment(6, 5, "Cửu Ngũ", "訟，元吉。", "Tụng nguyên cát.", "Hào Chín Năm: Kiện cả tốt.", 206, false, null));
        list.add(new LineJudgment(6, 6, "Thượng Cửu", "或錫之鞶帶，終朝三褫之。", "Hoặc tích chi bàn đái, chung triêu tam trĩ chi.", "Hào Chín Trên: hoặc cho chiếc dải lưng da, trọn buổi sáng, ba lần lột lại.", 207, true, null));

        // Quẻ 7 — SƯ (師)
        list.add(new LineJudgment(7, 1, "Sơ Lục", "師出以律，否臧，凶。", "Sư xuất dĩ luật, phủ tang, hung.", "Hào Sáu Đầu: Quân ra bằng luật, không khéo thì hung[5].", 212, true, null));
        list.add(new LineJudgment(7, 2, "Cửu Nhị", "在師中吉，无咎；王三錫命。", "Tại Sư trung cát, vộ cữu, vương tam tích mệnh.", "Hào Chín Hai: Ở trong quân, vừa phải thì tốt, không lỗi, nhàvua ba lần cho mệnh.", 213, true, null));
        list.add(new LineJudgment(7, 3, "Lục Tam", "師或輿尸，凶。", "Sư„ hoặc dư thi, hung.", "Hào Sáu Ba: Quân hoặc khiêng thây, hung.", 215, false, null));
        list.add(new LineJudgment(7, 4, "Lục Tứ", "師左次，无咎。", "Sư tả thứ, vô cữu.", "Hào Sáu Tư:Quân lùi đóng, không lỗi.", 216, true, null));
        list.add(new LineJudgment(7, 5, "Lục Ngũ", "田有禽，利執言，无咎。長子帥師，弟子輿尸，貞凶。", "Điền hữu cầm, lợi chấp ngôn, vô cữu, trường tử suất sư, đệ tử dư thi, trinh hung.", "Hào Sáu Năm. Ruộng có chim, lợi chữ sự có lời để nói. Con cả đem quân, con em khiêng thây, chinh cũng hung.", 217, true, null));
        list.add(new LineJudgment(7, 6, "Thượng Lục", "大君有命，開國承家，小人勿用。", "Đại quân hữu mệnh, khai quốc thừa gia, tiểu nhân vật dụng.", "Hào Sáu Trên: Đấng đại quân có mệnh, mở nước vâng nhà, kẻ tiểu nhân chớ dùng.", 219, false, null));

        // Quẻ 8 — TỴ (比)
        list.add(new LineJudgment(8, 1, "Sơ Lục", "有孚，比之，无咎。有孚盈缶，終來有它，吉。", "Hữu phu, tỵ chi, vô cữu. Hữu phu doanh phẫu, chung laihữu tha cát.", "Hào Sáu Đầu: Có tin, liền lại đó, không lỗi. Có tin đầy chậu, trọn lại có sự tốt khác.", 225, true, null));
        list.add(new LineJudgment(8, 2, "Lục Nhị", "比之自內，貞吉。", "Tỵ chi tự nội, trinh cát.", "Hào Sáu Hai: Liền lại tự bên trong, chính và tốt.", 227, true, null));
        list.add(new LineJudgment(8, 3, "Lục Tam", "比之匪人。", "Tỵ chi phỉ nhân.", "Hào Sáu Ba: Liền với người không đáng liền.", 228, false, null));
        list.add(new LineJudgment(8, 4, "Lục Tứ", "外比之，貞吉。", "Ngoại tỵ chi, trinh cát.", "Hào Sáu Tư: Kẻ ngoài liền lại với, chính tốt!", 229, false, null));
        list.add(new LineJudgment(8, 5, "Cửu Ngũ", "顯比。王用三驅，失前禽，邑人不誡，吉。", "Hiền tỵ, vương dụng tam khu, thất tiền cầm, ấp nhân bất giới, cát.", "Hào Chín Năm: Rõ rệt liền lại, nhà vua dùng đuổi ba mặt, mất con chim ở phía trước người làng không bảo, tốt.", 230, true, "Bộ trích cũ nối liền phần lời bình của sách (mở đầu bằng Truyện của Trình Di) vào ngay sau lời dịch nghĩa, không có dấu phân cách, khiến trường nghia dài 3339 ký tự thay vì 109. Đã cắt tại mốc Truyện của Trình Di; phần giữ lại là nguyên văn lời dịch của Ngô Tất Tố, không sửa một chữ. Lời bình bị cắt không mất giá trị nhưng không thuộc hào từ, nên không được để lẫn trong trường dịch nghĩa."));
        list.add(new LineJudgment(8, 6, "Thượng Lục", "比之无首，凶。", "Tỵ chi vô thủ, hung.", "Hào Sáu Trên: Gần liền không đầu, hung.", 233, false, null));

        // Quẻ 9 — TIỂU SÚC (小畜)
        list.add(new LineJudgment(9, 1, "Sơ Cửu", "復自道，何其咎，吉。", "Phục tự đạo, hà kỳ cữu? Cát.", "Hào Chín Đầu: Trở lại từ đường, còn lỗi gì? Tốt!", 239, false, null));
        list.add(new LineJudgment(9, 2, "Cửu Nhị", "牽復，吉。", "Khiên phục, cát.", "Hào Chín Hai: Giật trở lại, tốt.", 240, true, null));
        list.add(new LineJudgment(9, 3, "Cửu Tam", "輿說輻，夫妻反目。", "Dư thoát bức, phu thê phản mục.", "Hào Chín Ba: Xe trụt bánh, chồng vợ trở mắt.", 241, true, null));
        list.add(new LineJudgment(9, 4, "Lục Tứ", "有孚，血去惕出，无咎。", "Hữu phu, huyết khứ, dịch xuất, vô cữu.", "Hào Sáu Tư: Có tin, máu đi, sợ ra, không lỗi.", 243, false, null));
        list.add(new LineJudgment(9, 5, "Cửu Ngũ", "有孚攣如，富以其鄰。", "Hữu phu loan như, phú dĩ kỳ lân.", "Hào Chín Năm: Có tin, dường co quẹo vậy, giàu vì láng giềng.", 244, false, "Bộ trích cũ nối liền phần lời bình của sách (mở đầu bằng Truyện của Trình Di) vào ngay sau lời dịch nghĩa, không có dấu phân cách, khiến trường nghia dài 1176 ký tự thay vì 60. Đã cắt tại mốc Truyện của Trình Di; phần giữ lại là nguyên văn lời dịch của Ngô Tất Tố, không sửa một chữ. Lời bình bị cắt không mất giá trị nhưng không thuộc hào từ, nên không được để lẫn trong trường dịch nghĩa."));
        list.add(new LineJudgment(9, 6, "Thượng Cửu", "既雨既處，尚德載，婦貞厲，月幾望，君子征凶。", "Ký vũ, ký xử, thượng đức tái, phụ trinh lệ, Nguyện cơ vọng, quân tử chinh hung.", "Hào Chín Trên: Đã mưa, đã ở, chuộng đức chở, đàn bà chính bên, nguy! Mặt trăng hầu đến tuần vọng, đấng quân tử đi thì hung.", 245, true, null));

        // Quẻ 10 — LÝ (履)
        list.add(new LineJudgment(10, 1, "Sơ Cửu", "素履，往无咎。", "Tố lý, vãng vô cữu.", "Hào Chín Đầu: Xéo theo sự vốn có, đi, không lỗi.", 250, true, null));
        list.add(new LineJudgment(10, 2, "Cửu Nhị", "履道坦坦，幽人貞吉。", "Lý đạo thản thản, u nhân trinh cát.", "Hào Chín Hai: Xéo đường bằng phẳng, người uẩn chính bền thì tốt.", 251, true, null));
        list.add(new LineJudgment(10, 3, "Lục Tam", "眇能視，跛能履，履虎尾，咥人，凶。武人為于大君。", "Diểu năng thị, bí năng lý, lý hổ vĩ, chất nhân hung, vũ nhân vi vu đại quân.", "Hào Sáu Ba: Chột biết trông, què biết xéo, xéo đuôi cọp, cắn người, hung. Kẻ vũ nhân làm đấng đại quân.", 253, true, null));
        list.add(new LineJudgment(10, 4, "Cửu Tứ", "履虎尾，愬愬終吉。", "Lý hổ vỹ, tố tố chung cát.", "Hào Chín Tư: Xéo đuôi cọp, nơm nớp, sau chót tốt.", 254, true, null));
        list.add(new LineJudgment(10, 5, "Cửu Ngũ", "夬履，貞厲。", "Quải lý, trinh lệ.", "Hào Chín Năm: Quyết xéo, chính bền nguy!", 255, false, null));
        list.add(new LineJudgment(10, 6, "Thượng Cửu", "視履考祥，其旋元吉。", "Thị lý khảo tường, kỳ tuyền nguyên cát.", "Hào Chín Trên: Coi sự xéo, xét điềm lành thửa quanh cả tốt.", 257, false, null));

        // Quẻ 11 — THÁI (泰)
        list.add(new LineJudgment(11, 1, "Sơ Cửu", "拔茅茹以其彙，征吉。", "Bạt mao nhự, dĩ kỳ vâng, chỉnh cắt.", "Hào Chín Đầu: Nhổ cụm cỏ tranh, lấy vầng nó, đi tốt.", 262, true, null));
        list.add(new LineJudgment(11, 2, "Cửu Nhị", "包荒。用馮河，不遐遺；朋亡。得尚于中行。", "Bao Hoang, dụng bằng hà, bất hà dĩ, bằng vong, đắc thượng vu trung hàng.", "Hào Chín Hai: Bao dung sự hoang rậm, dùng để tay không lội sông, không sót việc xa; bè cánh mất, được sánh ở hàng giữa.", 263, true, null));
        list.add(new LineJudgment(11, 3, "Cửu Tam", "无平不陂，无往不復，艱貞无咎。勿恤其孚，于食有福。", "Vô bình bất bi vô vãng bất phục, gian trinh, vô cữu, vật tuất kỳ phu, vu thực hữu phúc.", "Hào Chín Ba: Không chỗ bằng phẳng nào không lồi lõm, không sự đi nào không trở lại. Khó nhọc, chính bền, chớ lo thừa sự tín, chưng việc ăn hưởng có phúc.", 265, true, null));
        list.add(new LineJudgment(11, 4, "Lục Tứ", "翩翩，不富以其鄰；不戒以孚。", "Phiên phiên, bất phú dĩ kỳ lân; bất gìới dĩ phu.", "Hào Sáu Tư: Phơi phới, chẳng giàu lấy thửa láng giềng, chẳng răn lấy tin.", 267, true, null));
        list.add(new LineJudgment(11, 5, "Lục Ngũ", "帝乙歸妹，以祉，元吉。", "Đế Ất quy muội, dĩ chỉ, nguyên cát.", "Hào Sáu Năm: Vua Đế Ất gả em gái, để có phúc cảtốt.", 268, false, "Bộ trích cũ nối liền phần lời bình của sách (mở đầu bằng GIẢI NGHĨA) vào ngay sau lời dịch nghĩa, không có dấu phân cách, khiến trường nghia dài 2694 ký tự thay vì 51. Đã cắt tại mốc GIẢI NGHĨA; phần giữ lại là nguyên văn lời dịch của Ngô Tất Tố, không sửa một chữ. Lời bình bị cắt không mất giá trị nhưng không thuộc hào từ, nên không được để lẫn trong trường dịch nghĩa."));
        list.add(new LineJudgment(11, 6, "Thượng Lục", "城復于隍，勿用師，自邑告命，貞吝。", "Thành phục vu hoàng, vật dụng sư, tự ấp cáo mệnh, trinh lận!.", "Hào Sáu Trên: Thành trở về rãnh, chớ dùng quân tự ấp bảo mệnh, chính bền thẹn tiếc.", 271, true, null));

        // Quẻ 12 — BĨ (否)
        list.add(new LineJudgment(12, 1, "Sơ Lục", "拔茅茹以其彙，貞吉。亨。", "Bạt mao nhự dĩ kỳ vựng, trinh, cát hanh.", "Hào Sáu Đầu: Nhổ cụm cỏ tranh, lấy vầng nó, chính bền tốt lành hanh thông.", 275, true, null));
        list.add(new LineJudgment(12, 2, "Lục Nhị", "包承，小人吉，大人否。亨。", "Bao thừa, tiểu nhân cát, đại nhân bĩ, hanh.", "Hào Sáu Hai: Bọc chứa sự vâng thuận, kẻ tiểu nhân tốt, đấng đại nhân bĩ, thì hanh thông.", 277, true, null));
        list.add(new LineJudgment(12, 3, "Lục Tam", "包羞。", "Bao tu.", "Hào Sáu Ba: Bọc chứa sự hổ thẹn.", 278, false, null));
        list.add(new LineJudgment(12, 4, "Cửu Tứ", "有命，无咎，疇離祉。", "Hữu mệnh vô cữu, trù ly chỉ.", "Hào Chín Tư: Có mệnh không lỗi, bè loại dính phúc.", 279, true, null));
        list.add(new LineJudgment(12, 5, "Cửu Ngũ", "休否，大人吉。其亡其亡，繫于苞桑。", "Hưu phủ, đại nhân cát, kỳ vong, kỳ vong! Hệ vu bào tang.", "Hào Chín Năm: Nghĩ bĩ, đấng đại nhân tốt. Này mất! Này mất! Buộc cây dâu rậm.", 281, true, null));
        list.add(new LineJudgment(12, 6, "Thượng Cửu", "傾否，先否後喜。", "Khuynh bĩ, tiên bĩ hậu hỷ.", "Hào Chín Trên: Nghiêng bĩ, trước bĩ sau mừng.", 282, false, null));

        // Quẻ 13 — ĐỒNG NHÂN (同人)
        list.add(new LineJudgment(13, 1, "Sơ Cửu", "同人于門，無咎。", "Đồng nhân vu môn, vô cữu.", "Hào Chín Đầu: Cùng người ở cửa, không lỗi", 289, false, null));
        list.add(new LineJudgment(13, 2, "Lục Nhị", "同人于宗，吝。", "Đồng nhàn vu tông, lận.", "Hào Sáu Hai: Cùng người ở họ, đáng tiếc.", 290, true, null));
        list.add(new LineJudgment(13, 3, "Cửu Tam", "伏戎于莽，升其高陵，三歲不興。", "Phục nhung vu măng, thăng kỳ cao lăng, tam tuế bất hưng.", "Hào Chín Ba: Phục quân ở rừng, lên thửa gò cao, ba năm không dấy lên.", 291, true, null));
        list.add(new LineJudgment(13, 4, "Cửu Tứ", "乘其墉，弗克，攻吉。", "Thừa kỳ dung, phất khắc công, cát.", "Hào Chín Tư: Cưỡi thừa tường, không đánh được,tốt.", 292, true, null));
        list.add(new LineJudgment(13, 5, "Cửu Ngũ", "同人，先號啕而后笑。大師克相遇。", "Đồng nhân, tiên hào diếu nhi hậu tiếu, đại sự khắctương ngộ.", "Hào Chín Năm: Cùng với người, trước kêu gào mà sau cười, quân lớn được, gặp nhau.", 293, true, null));
        list.add(new LineJudgment(13, 6, "Thượng Cửu", "同人于郊，無悔。", "Đồng nhân vu giao, vô hối.", "Hào Chín Trên: Cùng người ở đồng, không ăn năn.", 295, true, null));

        // Quẻ 14 — ĐẠI HỮU (大有)
        list.add(new LineJudgment(14, 1, "Sơ Cửu", "无交害，匪咎，艱則无咎。", "Vô giao hại, phỉ cữu, nan tắc vô cữu.", "Hào Chín Đầu: Không dính tới sự hại, chẳng phải lỗi. Khó nhọc thì không lỗi.", 300, true, null));
        list.add(new LineJudgment(14, 2, "Cửu Nhị", "大車以載，有攸往，无咎。", "Đại xa dĩ tái, hữu du văng, vô cữu.", "Hào Chín Hai: Xe lớn để chở, có thừa đi, không lỗi.", 302, true, null));
        list.add(new LineJudgment(14, 3, "Cửu Tam", "公用亨于天子，小人弗克。", "Công dụng hưởng[2] vu thiên tử, tiểu nhân phất khắc.", "Hào Chín Ba. Tước Công dùng hưởng của đấng thiên tử, kẻ tiểu nhân không thể được.", 303, true, null));
        list.add(new LineJudgment(14, 4, "Cửu Tứ", "匪其彭，无咎。", "Phỉ kỳ bàng, vô cữu.", "Hào Chín Tư: Chẳng phải sự thịnh của mình; không lỗi.", 304, true, null));
        list.add(new LineJudgment(14, 5, "Lục Ngũ", "厥孚交如，威如；吉。", "Quyết phu giao như, uy như, cát.", "Hào Sáu Năm: Thửa tin dường giao nhau vậy, dường oai nghiêm vây, tốt.", 306, true, null));
        list.add(new LineJudgment(14, 6, "Thượng Cửu", "自天佑之，吉无不利。", "Tự thiên hựu chi, cát, vô bất lợi.", "Hào Chín Trên: Tự trời giúp nó, tốt, không gì không lợi.", 307, true, null));

        // Quẻ 15 — KHIÊM (謙)
        list.add(new LineJudgment(15, 1, "Sơ Lục", "謙謙君子，用涉大川，吉。", "Khiêm khiêm, quân tử, đụng thiệp đại xuyên,cát.", "Hào Sáu Đầu: Đấng quân tử nhún nhún, dùng sang sông lớn, tốt.", 313, false, null));
        list.add(new LineJudgment(15, 2, "Lục Nhị", "鳴謙，貞吉。", "Minh khiêm, trinh cát.", "Hào Sáu Hai: Kiêu sự nhún, chính bền, tốt.", 314, true, null));
        list.add(new LineJudgment(15, 3, "Cửu Tam", "勞謙君子，有終吉。", "Lao khiêm, quân tử hữu chung, cát.", "Hào Chín Ba; Đấng quân tử nhọc mà nhún, sau chót, tốt.", 315, false, null));
        list.add(new LineJudgment(15, 4, "Lục Tứ", "无不利，撝謙。", "Vô bất lợi, vi khiêm.", "Hào Sáu Tư: Không gì không lợi, vung vẩy sự nhún.", 316, true, null));
        list.add(new LineJudgment(15, 5, "Lục Ngũ", "不富，以其鄰，利用侵伐，无不利。", "Bất phú dĩ kỳ lân, lợi dụng xâm phạt, vô bất lợi.", "Hào Sáu Năm: Không giàu, sai khiến được láng giềng, lợi dụng lấn đánh, không gì không lợi.", 318, true, null));
        list.add(new LineJudgment(15, 6, "Thượng Lục", "鳴謙，利用行師，征邑國。", "Minh khiêm, lợi dụng hành sư chính ấp quốc.", "Hào Sáu Trên: Kêu sự nhún, lợi dụng trẩy quân đánh làng nước.", 319, false, null));

        // Quẻ 16 — DỰ (豫)
        list.add(new LineJudgment(16, 1, "Sơ Lục", "鳴豫，凶。", "Minh dự, hung.", "Hào Sáu Đầu: kêu sự vui, hung!", 325, false, null));
        list.add(new LineJudgment(16, 2, "Lục Nhị", "介于石，不終日，貞吉。", "Giới vu thạch, bất chung nhật, trinh cát.", "Hào Sáu Hai: tiết tháo như đá, không trọn ngày, chính bền thì tốt.", 326, true, null));
        list.add(new LineJudgment(16, 3, "Lục Tam", "盱豫，悔。遲有悔。", "Vu dự hối trì, hữu hối.", "Hào Sáu Ba: Nhìn sự vui, ăn năn chậm, có ăn năn.", 327, true, "Bộ trích cũ nối liền phần lời bình của sách (mở đầu bằng GIẢI NGHĨA) vào ngay sau lời dịch nghĩa, không có dấu phân cách, khiến trường nghia dài 848 ký tự thay vì 48. Đã cắt tại mốc GIẢI NGHĨA; phần giữ lại là nguyên văn lời dịch của Ngô Tất Tố, không sửa một chữ. Lời bình bị cắt không mất giá trị nhưng không thuộc hào từ, nên không được để lẫn trong trường dịch nghĩa."));
        list.add(new LineJudgment(16, 4, "Cửu Tứ", "由豫，大有得。勿疑。朋盍簪。", "Do dự, đại hữu đắc, vật nghi, bằng hạp châm.", "Hào Chín Tư: Bởi đó là vui, cả có được, chớ nghi ngờ, bè bạn tụ họp.", 329, true, null));
        list.add(new LineJudgment(16, 5, "Lục Ngũ", "貞疾，恆不死。", "Trinh tật, hằng bất tử.", "Hào Sáu Năm: Chính bền có tật, thường không chết.", 330, false, null));
        list.add(new LineJudgment(16, 6, "Thượng Lục", "冥豫，成有渝，无咎。", "Minh dự thành, hữu thâu, vô cứu.", "Hào Sáu Trên: Mờ tối về sự vui, việc thành rồi, có thay đổi, không lỗi.", 331, true, "Bộ trích cũ lấy lời Tượng truyện thay cho lời dịch hào từ: trường nghia mở đầu bằng Lời Tượng nói rằng, trong khi hanTu lại đúng là hào từ. Đã bỏ tiền tố đó và phục hồi nhãn vị trí Hào Sáu Trên suy dẫn từ position 6 + hào âm. Nội dung lời văn giữ nguyên của Ngô Tất Tố. Đây là ca sót lại của lỗi đã ghi ở VERIFICATION_OPUS_R24 muc B."));

        return List.copyOf(list);
    }
}
