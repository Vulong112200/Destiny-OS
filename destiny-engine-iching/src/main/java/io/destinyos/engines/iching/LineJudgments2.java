package io.destinyos.engines.iching;

import java.util.ArrayList;
import java.util.List;

/**
 * Hào từ (爻辭) for hexagrams 17-32, King Wen order.
 *
 * <p>Hán tự from zh.wikisource.org; Hán-Việt and nghĩa from Ngô Tất Tố.
 * See {@link LineJudgment} for provenance (R24/R25).
 */
final class LineJudgments2 {

    private LineJudgments2() {
    }

    static List<LineJudgment> entries() {
        List<LineJudgment> list = new ArrayList<>();

        // Quẻ 17 — TÙY (隨)
        list.add(new LineJudgment(17, 1, "Sơ Cửu", "官有渝，貞吉。出門交有功。", "Quan hữu thâu, trinh cát, xuất môn giao hữu công.", "Hào Chín Đầu: Quan có thay đổi, chính thì tốt, ra cửa kết bạn thì có công.", 337, false, null));
        list.add(new LineJudgment(17, 2, "Lục Nhị", "系小子，失丈夫。", "Hệ tiểu tử, thất trượng phu. Dịch nghũu -", "Hào Sáu Hai: Quâi quít kẻ tiểu tử, mất đấng trượng phu.", 338, true, null));
        list.add(new LineJudgment(17, 3, "Lục Tam", "系丈夫，失小子。隨，有求得利，居貞。", "Hệ trượng phu, thất tiểu tử, tùy hữu cầu, đắc lợi, cự trinh,", "Hào Sáu Ba: Quấn quít đấng trượng phu, mất kẻ tiểu tử, theo mà có tìm, lợi về ở trinh.", 339, true, null));
        list.add(new LineJudgment(17, 4, "Cửu Tứ", "隨有獲，貞凶。有孚在道，以明，何咎。", "Tùy hữu hoạch, trinh hung, hữu phu, tại đạo, dĩ minh, hà cữu?", "Hào Chín tư: Theo có được, chính mà hung; có tin, ở đạo, dùng sáng, lỗi gì?", 341, true, null));
        list.add(new LineJudgment(17, 5, "Cửu Ngũ", "孚于嘉，吉。", "Phu vu gia, cát.", "Hào Chín Năm: Tin về kẻ lành, tốt.", 342, true, null));
        list.add(new LineJudgment(17, 6, "Thượng Lục", "拘系之，乃從維之。王用亨于西山。", "Câu hệ chi, nải tòng duy chi, vương dụng hưởng vu Tây sơn.", "Hào Sáu Trên: Cầm buộc đó, bèn theo ràng đó, vua dùng hưởng ở non Tây.", 343, true, null));

        // Quẻ 18 — CỔ (蠱)
        list.add(new LineJudgment(18, 1, "Sơ Lục", "幹父之蠱，有子考，无咎，厲終吉。", "Cán phụ chi cổ, hữu tử, khảo vô cữu, lệ, chung cát.", "Hào Sáu Đầu: Cán đáng cuộc cổ của cha, có con, cho khônglỗi, nguy ! Sau chót tốt.", 350, false, null));
        list.add(new LineJudgment(18, 2, "Cửu Nhị", "幹母之蠱，不可貞。", "Cán mẫu chi cổ, bất khả trinh. Dịch âm. -", "Hào Chín Hai: Cán đáng sự cổ của mẹ, không thể trinh.", 352, true, null));
        list.add(new LineJudgment(18, 3, "Cửu Tam", "幹父之蠱，小有悔，无大咎。", "Cán phụ chi cổ, tiểu hữu hối, vô đại cữu.", "Hào Chín Ba: Cán đáng sự cổ của cha, hơi có ăn năn, không có lỗi lớn.", 353, true, null));
        list.add(new LineJudgment(18, 4, "Lục Tứ", "裕父之蠱，往見吝。", "Dụ phụ chi cổ, vãng kiến lận.", "Hào Sáu Tư: Rộng rãi với sự cổ của cha, đi thấy tiếc.", 354, true, null));
        list.add(new LineJudgment(18, 5, "Lục Ngũ", "幹父之蠱，用譽。", "Cán phụ chi cổ, dụng dự.", "Hào Sáu Năm: Cán đáng sự cổ của cha, dùng nhen.", 355, true, null));
        list.add(new LineJudgment(18, 6, "Thượng Cửu", "不事王侯，高尚其事。", "Bất sự vương hầu, cao thượng kỳ sự.", "Hào Chín Trên: Chẳng thờ tước vương tước hầu, cao nâng thửa việc.", 356, false, null));

        // Quẻ 19 — LÂM (臨)
        list.add(new LineJudgment(19, 1, "Sơ Cửu", "咸臨，貞吉。", "Hàm lâm, trình cát", "Hào Chín Đầu: Đều tới, chính tốt.", 362, false, null));
        list.add(new LineJudgment(19, 2, "Cửu Nhị", "咸臨，吉无不利。", "Hàm lâm, cát, vô bất lợi.", "Hào Chín Hai: Đều tới, tốt, không gi không lợi.", 363, true, null));
        list.add(new LineJudgment(19, 3, "Lục Tam", "甘臨，无攸利。既憂之，无咎。", "Cam Lâm, vô do lợi, ký ưu chi, vô cữu.", "Hào Sáu Ba: Ngọt tới, không thửa lợi, đã lo đó, không lỗi.", 365, true, null));
        list.add(new LineJudgment(19, 4, "Lục Tứ", "至臨，无咎。", "Chí lâm, vô cữu.", "Hào Sáu Tư; Rất tới, không lỗi.", 366, true, null));
        list.add(new LineJudgment(19, 5, "Lục Ngũ", "知臨，大君之宜，吉。", "Trí lâm, đại quân chi nghi, cát!", "Hào Sáu Năm: Khôn tới, sự nên của vua cả, tốt!", 367, false, null));
        list.add(new LineJudgment(19, 6, "Thượng Lục", "敦臨，吉无咎。", "Đôn lâm, cát, vô cữu.", "Hào Sáu Trên: Dầy tới, tốt, không lỗi.", 368, true, null));

        // Quẻ 20 — QUÁN (觀)
        list.add(new LineJudgment(20, 1, "Sơ Lục", "童觀，小人无咎，君子吝。", "Đồng quán, tiểu nhân vô cữu, quân tử lận.", "Hào Sáu Đầu: Trẻ xem, kẻ tiểu nhân không lỗi, đấng quân tử đáng tiếc.", 374, true, null));
        list.add(new LineJudgment(20, 2, "Lục Nhị", "窺觀，利女貞。", "Khuy quan, lợi nữ trinh.", "Hào Sáu Hai: Nhòm xem, lợi về sự trinh của con gái.", 375, true, null));
        list.add(new LineJudgment(20, 3, "Lục Tam", "觀我生，進退。", "Quan ngã sinh tiến thoái.", "Hào Sáu Ba: Xem ta sinh tiến lui.", 376, false, null));
        list.add(new LineJudgment(20, 4, "Lục Tứ", "觀國之光，利用賓于王。", "Quạn quốc chi quang, lợi dụng tân vu vương.", "Hào Sáu Tư: Xem sự sáng láng của nước, lợi dụng làm khách chưng vua.", 377, true, null));
        list.add(new LineJudgment(20, 5, "Cửu Ngũ", "觀我生，君子无咎。", "Quan ngã sinh, quân tử vô cữu.", "Hào Chín Năm: Xem ta sinh, quân tử, không lỗi.", 378, true, null));
        list.add(new LineJudgment(20, 6, "Thượng Cửu", "觀其生，君子无咎。", "Quan kỳ sinh, quân tử vô cữu.", "Hào Chín Trên: Xem thửa sinh, quân tử không lỗi.", 379, true, "Bộ trích cũ nối liền phần lời bình của sách (mở đầu bằng GIẢI NGHĨA) vào ngay sau lời dịch nghĩa, không có dấu phân cách, khiến trường nghia dài 818 ký tự thay vì 48. Đã cắt tại mốc GIẢI NGHĨA; phần giữ lại là nguyên văn lời dịch của Ngô Tất Tố, không sửa một chữ. Lời bình bị cắt không mất giá trị nhưng không thuộc hào từ, nên không được để lẫn trong trường dịch nghĩa."));

        // Quẻ 21 — PHỆ HẠP (噬嗑)
        list.add(new LineJudgment(21, 1, "Sơ Cửu", "屨校滅趾，无咎。", "Lý hiệu, diệt chỉ, vô cữu.", "Hào Chín Đầu; Xéo xiềng, đứt ngón chân, không lỗi.", 386, true, null));
        list.add(new LineJudgment(21, 2, "Lục Nhị", "噬膚滅鼻，无咎。", "Phệ phu, diệt ty, vô cữu.", "Hào Sáu Hai: Cắn da, dứt mũi, không lỗi.", 387, true, null));
        list.add(new LineJudgment(21, 3, "Lục Tam", "噬臘肉，遇毒；小吝，无咎。", "Phệ tích nhục, ngộ độc tiểu lận, vô cữu.", "Hào Sáu Ba: cắn mắm khô, gặp độc, hơi tiếc, không lỗi.", 388, true, null));
        list.add(new LineJudgment(21, 4, "Cửu Tứ", "噬乾胏，得金矢，利艱貞，吉。", "Phệ can tỷ, đắc kim thỉ, lợi gian trình, cát!", "Hào Chín Tư: Cắn chạo khô, được tên vàng, lợi về khó nhọc, chính bền tốt.", 389, true, null));
        list.add(new LineJudgment(21, 5, "Lục Ngũ", "噬乾肉，得黃金，貞厲，无咎。", "Phệ can nhục, đắc hoàng kim, trinh lệ, vô cữu.", "Hào Sáu Năm: Cắn thịt khô, được vàng vàng, chính bền, lo sợ, không lỗi.", 391, true, null));
        list.add(new LineJudgment(21, 6, "Thượng Cửu", "何校滅耳，凶。", "Hạ hiệu, diệt nhĩ, hung!", "Hào Chín trên: Đội xiềng, đứt tai, hung!", 392, true, null));

        // Quẻ 22 — BÍ (賁)
        list.add(new LineJudgment(22, 1, "Sơ Cửu", "賁其趾，舍車而徒。", "Bí kỳ chỉ, xả xa nhi đồ.", "Hào Chín Đầu: Trang sức thửa ngón chân, bỏ xe mà đi không.", 399, true, null));
        list.add(new LineJudgment(22, 2, "Lục Nhị", "賁其須。", "Bí kỳ tu.", "Hào Sáu Hai: Trang sức cái râu của mình.", 401, true, null));
        list.add(new LineJudgment(22, 3, "Cửu Tam", "賁如濡如，永貞吉。", "Bí như, nhu như, vĩnh trinh, cát.", "Hào Chín Ba: Rõ ràng vậy,bóng mượt vậy; mãi mãi chính bền tốc.", 402, true, null));
        list.add(new LineJudgment(22, 4, "Lục Tứ", "賁如皤如，白馬翰如，匪寇婚媾。", "Bí như, phan như, bạch mâ hản nhưĩ Phì khấu, hôn câu.", "Hào Sáu Tư: Rỡ ràng vậy, phơ phơ vậy, ngựa trắng có cánh vậy. Chẳng phải giặc, dâu gia.", 403, true, "Bộ trích cũ nối liền phần lời bình của sách (mở đầu bằng GIẢI NGHĨA) vào ngay sau lời dịch nghĩa, không có dấu phân cách, khiến trường nghia dài 1239 ký tự thay vì 87. Đã cắt tại mốc GIẢI NGHĨA; phần giữ lại là nguyên văn lời dịch của Ngô Tất Tố, không sửa một chữ. Lời bình bị cắt không mất giá trị nhưng không thuộc hào từ, nên không được để lẫn trong trường dịch nghĩa."));
        list.add(new LineJudgment(22, 5, "Lục Ngũ", "賁於丘園，束帛戔戔，吝，終吉。", "Bí vu khâu viênt thúc bạch tiên tiên, lận chung cát.", "Hào Sáu Năm: Trang sức ở gò vườn, bó lụa mỏng hẹp, đáng tiếc, sau chót tốt.", 405, true, null));
        list.add(new LineJudgment(22, 6, "Thượng Cửu", "白賁，无咎。", "Bạch bi, vô cữu.", "Hào Chín Trên: Trang sức bằng màu trắng, không lỗi.", 406, true, null));

        // Quẻ 23 — BÁC (剝)
        list.add(new LineJudgment(23, 1, "Sơ Lục", "剝牀以足，蔑貞凶。", "Bác sàng dĩ túc, miệt trinh, hung.", "Hào Sáu Đầu: Đẽo giường bằng[5] chân, không trinh, hung.", 411, true, null));
        list.add(new LineJudgment(23, 2, "Lục Nhị", "剝牀以辨，蔑貞凶。", "Bác sàng dĩ biện, miệt trinh hung.", "Hào Sáu Hai: Đẽo giường bằng bễ, chẳng trinh, hung!", 412, true, null));
        list.add(new LineJudgment(23, 3, "Lục Tam", "剝之，无咎。", "Bác chi, vô cữu.", "Hào Sáu Ba: Đẽo đó, không lỗi.", 413, true, null));
        list.add(new LineJudgment(23, 4, "Lục Tứ", "剝牀以膚，凶。", "Bác sàng dĩ phu, hung!", "Hào Sáu Tư: Đẽo giường bằng da, hung!", 414, true, null));
        list.add(new LineJudgment(23, 5, "Lục Ngũ", "貫魚，以宮人寵，无不利。", "Quán ngư dĩ cung nhân sủng, vô bất lợi.", "Hào Sáu Năm: Xâu cá; lấy cung nhân được yên, không gì không lợi.", 415, true, null));
        list.add(new LineJudgment(23, 6, "Thượng Cửu", "碩果不食，君子得輿，小人剝廬。", "Thạc quả bất thực, quân tử đắc dư, tiểu nhân bác lư.", "Hào Chín Trên: Trái lớn không ăn, đấng quân tử được xe, kẻ tiểu nhân đẽo nhà.", 416, true, null));

        // Quẻ 24 — PHỤC (復)
        list.add(new LineJudgment(24, 1, "Sơ Cửu", "不復遠，无袛悔，元吉。", "Bất viễn phục, vô chỉ hối, nguyên cát", "Hào Chín Đầu: Chẳng xa trở lại, không đến ăn năn, cả tốt.", 424, true, null));
        list.add(new LineJudgment(24, 2, "Lục Nhị", "休復，吉。", "Hưu phục cát!", "Hào Sáu Hai: Đẹp sự trở lại, tốt!", 425, true, null));
        list.add(new LineJudgment(24, 3, "Lục Tam", "頻復，厲无咎。", "Tần phục lệ, vô cữu. Dỉch nghĩa. -", "Hào Sáu Ba: Luôn luôn trở lại, nguy! Không lỗi.", 426, true, null));
        list.add(new LineJudgment(24, 4, "Lục Tứ", "中行獨復。", "Trung hành độc phục.", "Hào Sáu Tư: Đi giữa một mình trở lại.", 427, true, null));
        list.add(new LineJudgment(24, 5, "Lục Ngũ", "敦復，无悔。", "Đôn phục, vô hối..", "Hào Sáu Năm: Dốc lòng về sự trở lại,không phải ăn năn.", 428, true, null));
        list.add(new LineJudgment(24, 6, "Thượng Lục", "迷復，凶，有災眚。用行師，終有大敗，以其國君，凶；至于十年，不克征。", "Mê phục, hung, hữu tai sảnh, dụng hành sư, chung hữu đại bại; dĩ kỳ quốc quân,hung, chí vu thập niên, bất khắc chính.", "Hào Sáu Trên: Lú lấp sự trở lại, hung! Có vạ tội, dùng để trẩy quân, sau chót có thua lớn; tới cả vua nước đó, đến chừng mười năm, không thể đi.", 429, true, "Bộ trích cũ nối liền phần lời bình của sách (mở đầu bằng GIẢI NGHĨA) vào ngay sau lời dịch nghĩa, không có dấu phân cách, khiến trường nghia dài 2329 ký tự thay vì 144. Đã cắt tại mốc GIẢI NGHĨA; phần giữ lại là nguyên văn lời dịch của Ngô Tất Tố, không sửa một chữ. Lời bình bị cắt không mất giá trị nhưng không thuộc hào từ, nên không được để lẫn trong trường dịch nghĩa."));

        // Quẻ 25 — VÔ VỌNG (无妄)
        list.add(new LineJudgment(25, 1, "Sơ Cửu", "无妄，往吉。", "Vô vọng vãng cát", "Hào Chín Đầu: Không càn, đi tốt.", 437, true, null));
        list.add(new LineJudgment(25, 2, "Lục Nhị", "不耕穫，不菑畬，則利有攸往。", "Bất canh hoạch, bất tri dư, tắc lợi hữu du vãng.,", "Hào Sáu Hai: Chẳng cây, gặt; chẳng ngả, ngấu[4] thì lợi có thửa đi.", 438, true, null));
        list.add(new LineJudgment(25, 3, "Lục Tam", "无妄之災，或系之牛，行人之得，邑人之災。", "Vô vọng chi tai, hoặc hệ chi ngưu, hành nhân chi đắc, ấp nhân chi tai.", "Hào Sáu Ba: Cái hại của sự không càn, hoặc buộc con trâu, kẻ đi đường mà được, người ấp bị hại.", 440, true, null));
        list.add(new LineJudgment(25, 4, "Cửu Tứ", "可貞，无咎。", "Khả trinh, vô cữu", "Hào Chín Tư: Khả trinh, không lỗi.", 441, false, null));
        list.add(new LineJudgment(25, 5, "Cửu Ngũ", "无妄之疾，勿藥有喜。", "Vô vọng chi tật, vật dược, hữu hỷ.", "Hào Chín Năm: Cái tật không càn, đừng thuốc, có mừng.", 442, true, null));
        list.add(new LineJudgment(25, 6, "Thượng Cửu", "无妄，行有眚，无攸利。", "Vô Vọng hành, hữu sảnh, vô du lợi.", "Hào Chín Trên: Không càn mà đi, có tội, không thửa lợi.,", 443, true, null));

        // Quẻ 26 — ĐẠI SÚC (大畜)
        list.add(new LineJudgment(26, 1, "Sơ Cửu", "有厲利已。", "Hữu lệ, lợi dĩ", "Hào Chín Đầu: Có nguy, lợi thôi.", 450, false, null));
        list.add(new LineJudgment(26, 2, "Cửu Nhị", "輿說輹。", "Dư thoát bức.", "Hào Chín Hai: Xe trút bánh.", 451, true, null));
        list.add(new LineJudgment(26, 3, "Cửu Tam", "良馬逐，利艱貞。曰閑輿衛，利有攸往。", "Lương mã trục, lợi gian trinh, nhật nhàn dư vệ, lợi hữu du vàng.", "Hào Chín Ba: Ngựa hay ruổi, ngày quen xe, lại có thửa đi.", 452, true, null));
        list.add(new LineJudgment(26, 4, "Lục Tứ", "童牛之牿，元吉。", "Đồng ngưu chi cốc, nguyên cát.", "Hào Sáu Tư: Cái cùm trâu non, cả tốt.", 454, true, null));
        list.add(new LineJudgment(26, 5, "Lục Ngũ", "豶豕之牙，吉。", "Phần thi chi nha, cát.", "Hào Sáu Năm: Nanh con lợn thiến, tốt.", 455, true, null));
        list.add(new LineJudgment(26, 6, "Thượng Cửu", "何天之衢，亨。", "Hà thiên chí cù hanh.", "Hào Chín Trên: Sao đường trời hanh.", 457, false, null));

        // Quẻ 27 — DI (頤)
        list.add(new LineJudgment(27, 1, "Sơ Cửu", "舍爾靈龜，觀我朵頤，凶。", "Xả nhĩ linh quy, quan ngã đóa di, hung.", "Hào Chín Đầu: Bỏ con rùa thiêng của mày, xem ta trễ mép, hung!", 462, false, null));
        list.add(new LineJudgment(27, 2, "Lục Nhị", "顛頤，拂經，于丘頤，征凶。", "Điên di, phất kinh vu khâu, chinh hung dã.", "Hào Sáụ Hai: Đảo nuôi, trái thường ở gò, đi thì hung.", 464, true, null));
        list.add(new LineJudgment(27, 3, "Lục Tam", "拂頤，貞凶，十年勿用，无攸利。", "Phất di, trinh hung, thập niên vật dụng, vô du lợi.", "Hào Sáu Bã: Trái nuôi, chính hung, mười năm chớ dùng, không thửa lợi.", 465, true, null));
        list.add(new LineJudgment(27, 4, "Lục Tứ", "顛頤吉，虎視眈眈，其欲逐逐，无咎。", "Điên di, cát, hổ thị đam đam, kỳ dục trục trục vô cữu.", "Hào Sáu Tư: Đảo nuôi, tốt, hổ trông hau háu, lòng muốn của nó liền liền, không lỗi.", 466, true, null));
        list.add(new LineJudgment(27, 5, "Lục Ngũ", "拂經，居貞吉，不可涉大川。", "Phất kinh, cư trinh cát, bất khả thiệp đại xuyên.", "Hào Sáu Năm: Trái thường, ở chính tốt, chăng khá sang sông lớn.", 468, false, null));
        list.add(new LineJudgment(27, 6, "Thượng Cửu", "由頤，厲吉，利涉大川。", "Do di, lệ, cát, lợi thiệp đại xuyên.", "Hào Chín Trên: Bởi nuôi, nguy, tốt, lợi sang sông lớn.", 469, false, null));

        // Quẻ 28 — ĐẠI QUÁ (大過)
        list.add(new LineJudgment(28, 1, "Sơ Lục", "藉用白茅，无咎。", "Tạ dụng bạch mao, vô cữu.", "Hào Sáu Đầu: Trải dùng cỏ tranh trắng, không lỗi.", 476, true, null));
        list.add(new LineJudgment(28, 2, "Cửu Nhị", "枯楊生稊，老夫得其女妻，无不利。", "Khô dương sinh đề, lão phu đắc kỳ nữ thê vô bất lợi.", "Hào Chín Hai: Cây Dương khô mọc rễ, chồng già được vợ con gái, không gì không lợi.", 477, true, null));
        list.add(new LineJudgment(28, 3, "Cửu Tam", "棟橈，凶。", "Đống nạo hung!", "Hào Chín Ba: Cột ỏe, hung!", 478, false, null));
        list.add(new LineJudgment(28, 4, "Cửu Tứ", "棟隆，吉。有它吝。", "Đống long cát, hữu tha, lận.", "Hào Chín Tư: cột cao, tốt; có khí khác, đáng tiếc.", 479, false, null));
        list.add(new LineJudgment(28, 5, "Cửu Ngũ", "枯楊生華，老婦得其士夫，无咎无譽。", "Khô dương sinh huê, lão phụ đác kỳ sỹ phu, vô cữu vô dự.", "Hào Chín Năm: Cây dương khô mọc hoa, vợ già được chồng con trai, không lỗi không khen.", 480, true, "Bộ trích cũ nối liền phần lời bình của sách (mở đầu bằng GIẢI NGHĨA) vào ngay sau lời dịch nghĩa, không có dấu phân cách, khiến trường nghia dài 1055 ký tự thay vì 86. Đã cắt tại mốc GIẢI NGHĨA; phần giữ lại là nguyên văn lời dịch của Ngô Tất Tố, không sửa một chữ. Lời bình bị cắt không mất giá trị nhưng không thuộc hào từ, nên không được để lẫn trong trường dịch nghĩa."));
        list.add(new LineJudgment(28, 6, "Thượng Lục", "過涉滅頂，凶，无咎。", "Quả thiệp, diệt đính, hung, vô cữu.", "Hào Sáu Trên: Quá lội ngập đỉnh đầu, hung, không lỗi.", 481, true, null));

        // Quẻ 29 — TẬP KHẢM (坎)
        list.add(new LineJudgment(29, 1, "Sơ Lục", "習坎，入于坎窞，凶。", "Tập Khảm, nhập vu Khảm tăm hung!", "Hào Sáu Đầu: Chỗ hiểm kép, vào cái hố trong chỗ hiểm, hung!", 487, true, null));
        list.add(new LineJudgment(29, 2, "Cửu Nhị", "坎有險，求小得。", "Khảm hữu hiềm, cầu tiểu đắc.", "Hào Chín Hai: Chỗ trũng có sự hiểm, tìm hơi được.", 487, true, null));
        list.add(new LineJudgment(29, 3, "Lục Tam", "來之坎坎，險且枕，入于坎窞，勿用。", "Lai chi khảm khảm, hiền thả chẩm, nhập vu khảm tãm,vật dụng.", "Hào Sáu Ba: Lại đi hiểm hiểm, hiểm vào gối, vào cái hố của hiểm, chớ dùng.", 488, true, null));
        list.add(new LineJudgment(29, 4, "Lục Tứ", "樽酒簋貳，用缶，納約自牖，終无咎。", "Tôn tửu, quỹ nhị, dụng phẫu, nạp ước tự dũ chung vô cữu.", "Hào Sáu Tư: Chén rượu, rá[6] xôi, thêm dùng hồ sành, nộp ước tự cửa sổ tròn, sau chót không lỗi.", 490, true, null));
        list.add(new LineJudgment(29, 5, "Cửu Ngũ", "坎不盈，祗既平，无咎。", "Khảm bất doanh, chỉ ký bình, vô cữu.", "Hào Chín Năm: Chỗ trũng chẳng đầy, đến đã phẳng, không lỗi.", 493, true, null));
        list.add(new LineJudgment(29, 6, "Thượng Lục", "係用徽纆，寘于叢棘，三歲不得，凶。", "Hệ dụng huy chiền, chỉ vu tòng cức, tam tuế bất đắc, hung!", "Hào Sáu Trên: Trói dùng chạc thừng, đặt ở bụi gai, ba năm chẳng được, hung!", 494, true, null));

        // Quẻ 30 — LY (離)
        list.add(new LineJudgment(30, 1, "Sơ Cửu", "履錯然，敬之无咎。", "Lý thác nhiên, kinh chi vô cữu.", "Hào Chín Đầu: Xéo bừa vậy, kính đó không lỗi.", 499, true, null));
        list.add(new LineJudgment(30, 2, "Lục Nhị", "黃離，元吉。", "Hoàng Ly, nguyên cát.", "Hào Sáu Hai: Vàng sáng, cả tốt.", 500, true, null));
        list.add(new LineJudgment(30, 3, "Cửu Tam", "日昃之離，不鼓缶而歌，則大耋之嗟，凶。", "Nhật trắc chi ly, bất cỗ phẫu nhị nhi ca, tắc đại diệt chi ta, hung!", "Hào Chín Ba: Sự sáng của mặt trời dé. Chẳng gõ chậu sành mà hát, thì là cả già mà than, hung!", 501, true, null));
        list.add(new LineJudgment(30, 4, "Cửu Tứ", "突如其來如，焚如，死如，棄如。", "Đột như kỳ lai như, phần như, tử như, khí như!", "Hào Chín Tư: Đột như thửa lai vậy, cháy vậy, chết vậy, bỏ vậy!", 502, false, null));
        list.add(new LineJudgment(30, 5, "Lục Ngũ", "出涕沱若，戚嗟若，吉。", "Thế đà nhược! Thích ta nhược! Cát!", "Hào Sáu Năm: Nước mắt giàn giụa vậy, ngậm ngùi than vậy, tốt!", 503, true, null));
        list.add(new LineJudgment(30, 6, "Thượng Cửu", "王用出征，有嘉折首，獲匪其醜，无咎。", "Vương dụng xuất chinh, hữu giá, chiết thủ, hoạch phi kỳ sũ, vô cữu.", "Hào Chín Trên: Vua dùng ra đánh, có sự tốt, bẻ đầu, bắt chẳng phải loài, không lỗi.", 504, true, null));

        // Quẻ 31 — HÀM (咸)
        list.add(new LineJudgment(31, 1, "Sơ Lục", "咸其拇。", "Hàm kỳ mẫu.", "Hào Sáu Đầu: Cảm thửa ngón chân cái.", 512, false, null));
        list.add(new LineJudgment(31, 2, "Lục Nhị", "咸其腓，凶，居吉。", "Hàm kỳ phi, hung! cư cắt!", "Hào Sáu Hai: Cảm thửa bụng chân hung! Ở yên, tốt!", 513, true, null));
        list.add(new LineJudgment(31, 3, "Cửu Tam", "咸其股，執其隨，往吝。", "Hàm kỳ cổ, chấp kỳ tùy, vãng lận.", "Hào Chính Ba: Cảm thửa đùi, giữ thửa sự theo, đi thì đáng tiếc.", 514, true, null));
        list.add(new LineJudgment(31, 4, "Cửu Tứ", "貞吉悔亡，憧憧往來，朋從爾思。", "Trinh cát, hối vong, đồng vãng lai, bằng tòng nhĩ tư.", "Hào Chín Tư: Chính bền thì tốt, sự ăn năn sẽ mất; săng sắc đi lại, bạn theo sự nghĩ của mày.", 515, true, null));
        list.add(new LineJudgment(31, 5, "Cửu Ngũ", "咸其脢，无悔。", "Hàm kỳ môi, vô hối.", "Hào Chín Năm: Cảm thửa thăn thịt, không ăn năn.", 517, true, "Bộ trích cũ nối liền phần lời bình của sách (mở đầu bằng GIẢI NGHĨA) vào ngay sau lời dịch nghĩa, không có dấu phân cách, khiến trường nghia dài 840 ký tự thay vì 47. Đã cắt tại mốc GIẢI NGHĨA; phần giữ lại là nguyên văn lời dịch của Ngô Tất Tố, không sửa một chữ. Lời bình bị cắt không mất giá trị nhưng không thuộc hào từ, nên không được để lẫn trong trường dịch nghĩa."));
        list.add(new LineJudgment(31, 6, "Thượng Lục", "咸其輔，頰，舌。", "Hàm kỳ phụ, giáp, thiệt.", "Hào Sáu Trên: Cảm thửa xương má, mép, lưỡi.", 519, true, null));

        // Quẻ 32 — HẰNG (恆)
        list.add(new LineJudgment(32, 1, "Sơ Lục", "浚恆，貞凶，无攸利。", "Tuấn Hằng, trinh hung, vô du lợi.", "Hào Sáu Đầu: Đào sâu sự thường, trinh cũng hung, không thửa lợi.", 526, true, null));
        list.add(new LineJudgment(32, 2, "Cửu Nhị", "悔亡。", "Hối vong.", "Hào Chín Hai: Sự ăn năn mất.", 527, true, null));
        list.add(new LineJudgment(32, 3, "Cửu Tam", "不恆其德，或承之羞，貞吝。", "Bất hằng kỳ đức, hoặc thừa chi tu, trinh lận.", "Hào Chín Ba: Chẳng thường thửa đức, hoặc vâng đây thẹn, trinh cùng đáng tiếc.", 529, false, null));
        list.add(new LineJudgment(32, 4, "Cửu Tứ", "田无禽。", "Điền vô cầm.", "Hào Chín Tư: Săn không loài cầm.", 530, true, null));
        list.add(new LineJudgment(32, 5, "Lục Ngũ", "恆其德，貞，婦人吉，夫子凶。", "Hằng kỳ đức, trinh, phụ nhân cát, phu tử hung.", "Hào Sáu Năm: Thường thửa đức, chính, đàn bà tốt, đàn ông hung.", 531, true, null));
        list.add(new LineJudgment(32, 6, "Thượng Lục", "振恆，凶。", "Chấn hằng, hung.", "Hào Chín Trên: Xốc thường, hung.", 532, true, null));

        return List.copyOf(list);
    }
}
