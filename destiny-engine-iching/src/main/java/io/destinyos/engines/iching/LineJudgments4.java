package io.destinyos.engines.iching;

import java.util.ArrayList;
import java.util.List;

/**
 * Hào từ (爻辭) for hexagrams 49-64, King Wen order.
 *
 * <p>Hán tự from zh.wikisource.org; Hán-Việt and nghĩa from Ngô Tất Tố.
 * See {@link LineJudgment} for provenance (R24/R25).
 */
final class LineJudgments4 {

    private LineJudgments4() {
    }

    static List<LineJudgment> entries() {
        List<LineJudgment> list = new ArrayList<>();

        // Quẻ 49 — CÁCH (革)
        list.add(new LineJudgment(49, 1, "Sơ Cửu", "鞏用黃牛之革。", "Củng dụng hoàng ngưu chi cách.", "Hào Chín Đầu: Giàng bó dùng da trâu vàng.", 757, true, null));
        list.add(new LineJudgment(49, 2, "Lục Nhị", "巳日乃革之，征吉，无咎。", "Dĩ nhật nãi cách chi, chinh cát, vô cữu.", "Hào Sáu Hai: Hết ngày bèn đổi đấy, đi tốt, không lỗi.", 759, true, null));
        list.add(new LineJudgment(49, 3, "Cửu Tam", "征凶，貞厲，革言三就，有孚。", "Chính hung trinh lệ, cách ngôn tam tựu, hữu phu.", "Hào Chín Ba: Đi hung, chính nguy, nói đổi ba nên có tin.", 760, false, null));
        list.add(new LineJudgment(49, 4, "Cửu Tứ", "悔亡，有孚改命，吉。", "Hối vong, hữu phu, cải mệnh cát.", "Hào Chín Tư: Ăn năn mất, có tin đổi mệnh tốt.", 761, true, null));
        list.add(new LineJudgment(49, 5, "Cửu Ngũ", "大人虎變，未占有孚。", "Đại nhân hổ biến, vị chiêm hữu phu.", "Hào Chín Năm: Người lớn cọp biến, chưa xem có tin.", 762, true, null));
        list.add(new LineJudgment(49, 6, "Thượng Lục", "君子豹變，小人革面，征凶，居貞吉。", "Quân tử báo biến, tiểu nhân cách diện, chính hung, cư trinh.", "Hào Sáu Trên: Đấng quân tử beo biến, kẻ tiểu nhân đổi mặt, đi hung, ở chính.", 764, true, null));

        // Quẻ 50 — ĐỈNH (鼎)
        list.add(new LineJudgment(50, 1, "Sơ Lục", "鼎顛趾，利出否，得妾以其子，无咎。", "Đỉnh điên chỉ, lợi xuất bĩ, đắc thiếp dĩ kỳ tử, vô cữu.", "Hào Sáu Đầu: Vạc chổng chân, lợi ra vật hư xấu, được, nàng hầu, lấy thửa con, không lỗi.", 769, true, "Bộ trích cũ nối liền phần lời bình của sách (mở đầu bằng GIẢI NGHĨA) vào ngay sau lời dịch nghĩa, không có dấu phân cách, khiến trường nghia dài 1161 ký tự thay vì 88. Đã cắt tại mốc GIẢI NGHĨA; phần giữ lại là nguyên văn lời dịch của Ngô Tất Tố, không sửa một chữ. Lời bình bị cắt không mất giá trị nhưng không thuộc hào từ, nên không được để lẫn trong trường dịch nghĩa."));
        list.add(new LineJudgment(50, 2, "Cửu Nhị", "鼎有實，我仇有疾，不我能即，吉。", "Đỉnh hữu thật, ngã cừu hữu tật, bất ngã năng tức, cát!", "Hào Chín Hai: Vạc có cái chứa, kẻ thù ta có tật, chẳng ta hay tới, tốt!", 771, true, null));
        list.add(new LineJudgment(50, 3, "Cửu Tam", "鼎耳革，其行塞，雉膏不食，方雨虧悔，終吉。", "Đỉnh nhĩ cách, kỳ hành tắc, trĩ cao bất thực, phương vũ, khuy hối chung, cát.", "Hào Chín Ba: Tai vạc đổi, thửa đi lấp, mỡ con chim trĩ không ăn, đang mưa, ăn năn về thiếu, chọn tốt.", 772, true, null));
        list.add(new LineJudgment(50, 4, "Cửu Tứ", "鼎折足，覆公餗，其形渥，凶。", "Đỉnh chiết túc, phúc công tốc, kỳ hình ốc, hung!.", "Hào Chín Tư: Vạc gẫy chân, đổ đồ ăn của Ông, thửa tội giết kín[4] hung!", 774, true, null));
        list.add(new LineJudgment(50, 5, "Lục Ngũ", "鼎黃耳金鉉，利貞。", "Đỉnh hoàng nhĩ, kim huyên, lợi trinh.", "Hào Sáu Năm: Vạc tai vàng, quai màu vàng, lợi về chính bền.", 775, true, null));
        list.add(new LineJudgment(50, 6, "Thượng Cửu", "鼎玉鉉，大吉，无不利。", "Đỉnh ngọc huyên, đại cát, vô bất lợi.", "Hào Chín Trên: Đỉnh quai ngọc, cả tốt, không gì không lợi.", 776, true, null));

        // Quẻ 51 — CHẤN (震)
        list.add(new LineJudgment(51, 1, "Sơ Cửu", "震來虩虩，后笑言啞啞，吉。", "Chấn lai khích khích, hậu tiếu ngôn ách ách cát.", "Hào Chín Đầu: Nhức lại ngơm ngớp, sau cười nói khanh khách, tốt.", 781, true, null));
        list.add(new LineJudgment(51, 2, "Lục Nhị", "震來厲，億喪貝，躋于九陵，勿逐，七日得。", "Chấn lai lệ ức, táng bối, tê vu cửu lăng, vật trục, thất nhật đắc.", "Hào Chín Hai: Nhức lại, nguy đồ (?) mất của, lên chân chín gò, chớ đuổi, bảy ngày được.", 782, true, "Bộ trích cũ nối liền phần lời bình của sách (mở đầu bằng GIẢI NGHĨA) vào ngay sau lời dịch nghĩa, không có dấu phân cách, khiến trường nghia dài 1688 ký tự thay vì 87. Đã cắt tại mốc GIẢI NGHĨA; phần giữ lại là nguyên văn lời dịch của Ngô Tất Tố, không sửa một chữ. Lời bình bị cắt không mất giá trị nhưng không thuộc hào từ, nên không được để lẫn trong trường dịch nghĩa."));
        list.add(new LineJudgment(51, 3, "Lục Tam", "震蘇蘇，震行无眚。", "Chấn tô tô, chấn hành vô sảnh.", "Hào Sáu Ba: Nhức, thom thỏm, nhức đi, không tội.", 784, true, null));
        list.add(new LineJudgment(51, 4, "Cửu Tứ", "震遂泥。", "Chấn toại nê.", "Hào Chín Tư: Nhức bèn đắm.", 785, true, "Sách in nhãn \"六四 / Lục Tứ\", nhưng chính phần dịch nghĩa và lời bình ngay bên dưới đều ghi \"Hào CHÍN Tư\"; quẻ Chấn có hào 4 là hào DƯƠNG nên nhãn đúng là 九四 / Cửu Tứ, khớp cổ văn. Sách tự mâu thuẫn trong ba dòng liền nhau; sửa nhãn làm nó khớp lại với chính lời dịch của sách."));
        list.add(new LineJudgment(51, 5, "Lục Ngũ", "震往來厲，億无喪，有事。", "Chấn vãng lai lệ, ức vô táng hữu.", "Hào Sáu Năm: Nhức đi lại nguy, đồ (?) không mất cái có.", 786, true, null));
        list.add(new LineJudgment(51, 6, "Thượng Lục", "震索索，視矍矍，征凶。震不于其躬，于其鄰，无咎。婚媾有言。", "Chấn tắc tác, thị quắc quắc, chinh hung chấn bất vu kỳ cung, vu kỳ lân, vồ cữu, hôn cấu hữu ngôn.", "Hào Sáu Trên: Nhức xơ xác, trông ngơ ngác, đi thì hung; nhức chẳng chưng thửa mình, chưng thửa láng giềng, không lỗi, dâu gia có nói.", 787, true, null));

        // Quẻ 52 — CẤN (艮)
        list.add(new LineJudgment(52, 1, "Sơ Lục", "艮其趾，无咎，利永貞。", "Cấn kỳ chỉ, vô cữu, lợi vĩnh trinh.", "Hào Sáu Đầu: Đậu thửa ngón chân, không lỗi, lợi về dài lâu chính bền.", 794, true, null));
        list.add(new LineJudgment(52, 2, "Lục Nhị", "艮其腓，不拯其隨，其心不快。", "Cấn kỳ phì, bất chủng kỳ tùy, kỳ tâm bất khoái.", "Hào Sáu Hai: Đậu thửa bụng chân, chẳng cứu thửa theo, thửa lòng chẳng sướng.", 795, true, null));
        list.add(new LineJudgment(52, 3, "Cửu Tam", "艮其限，列其夤，厲薰心。", "Cấn kỳ hạn, liệt kỳ di, lệ huân tâm.", "Hào Chín Ba: Đậu thửa hạn, xé thửa thăn, nguy hun lòng.", 796, true, null));
        list.add(new LineJudgment(52, 4, "Lục Tứ", "艮其身，无咎。", "Cấn kỳ thân, vô cữu.", "Hào Sáu Tư: Đậu thửa mình, không lỗi.", 797, true, "Bộ trích cũ nối liền phần lời bình của sách (mở đầu bằng GIẢI NGHĨA) vào ngay sau lời dịch nghĩa, không có dấu phân cách, khiến trường nghia dài 378 ký tự thay vì 37. Đã cắt tại mốc GIẢI NGHĨA; phần giữ lại là nguyên văn lời dịch của Ngô Tất Tố, không sửa một chữ. Lời bình bị cắt không mất giá trị nhưng không thuộc hào từ, nên không được để lẫn trong trường dịch nghĩa."));
        list.add(new LineJudgment(52, 5, "Lục Ngũ", "艮其輔，言有序，悔亡。", "Cấn kỳ phụ ngôn hữu tự, hối vong.", "Hào Sáu Năm: Đậu thửa mép, nói có thứ tự, ăn năn mất.", 798, false, null));
        list.add(new LineJudgment(52, 6, "Thượng Cửu", "敦艮，吉。", "Đôn Cấn, cát.", "Hào Chín Trên - Dầy đậu, tốt.", 799, false, null));

        // Quẻ 53 — TIỆM (漸)
        list.add(new LineJudgment(53, 1, "Sơ Lục", "鴻漸于干，小子厲，有言，无咎。", "Hồng tiệm vu can, tiểu tử lệ, hữu ngôn, vô cữu.", "Hào Sáu Đầu: Con sếu tiến chưng bến, trẻ nhỏ nguy, có nói, không lỗi.", 806, true, null));
        list.add(new LineJudgment(53, 2, "Lục Nhị", "鴻漸于磐，飲食衎衎，吉。", "Hồng tiệm vu bàn, ẩm thực hãn hãn.", "Con sếu tiến chưng tảng đá, ăn uống hơn hớn.", 802, true, "Sách in THIẾU HẲN nhãn hào (\"六二\"/\"Lục Nhị:\"), chỉ còn phần dịch âm và dịch nghĩa. Vị trí xác định chắc chắn vì nằm giữa Sơ Lục và Cửu Tam và khớp cổ văn 六二：鴻漸于磐. Nhãn là khôi phục theo vị trí, không đọc trực tiếp được từ sách."));
        list.add(new LineJudgment(53, 3, "Cửu Tam", "鴻漸于陸，夫征不復，婦孕不育，凶；利禦寇。", "Hồng tiệm vu lục, phu chinh bất phục, phụ dựng bất dục, hung, lợi ngữ khấu.", "Hào Chín Ba: Con sếu tiến chưng đất liền, chồng đi chẳng lại, vợ chửa chẳng nuôi, hung, lợi về chống giặc.", 808, false, null));
        list.add(new LineJudgment(53, 4, "Lục Tứ", "鴻漸于木，或得其桷，无咎。", "Hồng tiệm vu mộc, hoắc đắc kỳ giốc, vô cữu.", "Hào Sáu Tư: Con sếu tiến chưng cây, hoặc được thửa cành ngang, không lỗi.", 809, false, null));
        list.add(new LineJudgment(53, 5, "Cửu Ngũ", "鴻漸于陵，婦三歲不孕，終莫之勝，吉。", "Hồng tiệm vu lăng, phụ tam tuế bất dựng, chung mạc chi thắng, cát.", "Hào Chín Năm: Con sếu tiến chưng gò, vợ ba năm chẳng chửa, trọn chẳng gì thắng, tốt.", 810, true, "Bộ trích cũ nối liền phần lời bình của sách (mở đầu bằng GIẢI NGHĨA) vào ngay sau lời dịch nghĩa, không có dấu phân cách, khiến trường nghia dài 822 ký tự thay vì 84. Đã cắt tại mốc GIẢI NGHĨA; phần giữ lại là nguyên văn lời dịch của Ngô Tất Tố, không sửa một chữ. Lời bình bị cắt không mất giá trị nhưng không thuộc hào từ, nên không được để lẫn trong trường dịch nghĩa."));
        list.add(new LineJudgment(53, 6, "Thượng Cửu", "鴻漸于陸，其羽可用為儀，吉。", "Hồng tiệm vu quỳ, kỳ vũ khả dụng vi nghi, cát.", "Hào Chín Trên: con sếu tiến chưng đường mây, lộng nó khá dùng làm miều cờ, tốt.", 812, false, null));

        // Quẻ 54 — QUI MUỘI (歸妹)
        list.add(new LineJudgment(54, 1, "Sơ Cửu", "歸妹以娣，跛能履，征吉。", "Quy muội dĩ đệ, bí năng lý, chinh cát.", "Hào Chín Đầu: Em gái về nhà chồng bằng vợ lẽ, quẻ biết xẻo, đi tốt.", 817, false, null));
        list.add(new LineJudgment(54, 2, "Cửu Nhị", "眇能視，利幽人之貞。", "Diểu năng thị, lợi u nhân chi trinh.", "Hào Chín Hai: Chột biết trông, lợi về sự chính bền của bậc u nhân.", 818, true, null));
        list.add(new LineJudgment(54, 3, "Lục Tam", "歸妹以須，反歸以娣。", "Qui muội dĩ tu, phản qui dĩ đệ.", "Hào Sáu Ba: Em gái về bằng sự đợi, lại về bằng vợ lẽ.", 820, true, null));
        list.add(new LineJudgment(54, 4, "Cửu Tứ", "歸妹愆期，遲歸有時。", "Qui muội khiên kỳ, trì qui hữu thì.", "Hào Chín Tư: Em gái về nhà chồng lỗi hẹn, chậm về có thời.", 821, true, "Bộ trích cũ nối liền phần lời bình của sách (mở đầu bằng Truyện của Trình Di) vào ngay sau lời dịch nghĩa, không có dấu phân cách, khiến trường nghia dài 699 ký tự thay vì 58. Đã cắt tại mốc Truyện của Trình Di; phần giữ lại là nguyên văn lời dịch của Ngô Tất Tố, không sửa một chữ. Lời bình bị cắt không mất giá trị nhưng không thuộc hào từ, nên không được để lẫn trong trường dịch nghĩa."));
        list.add(new LineJudgment(54, 5, "Lục Ngũ", "帝乙歸妹，其君之袂，不如其娣之袂良，月幾望，吉。", "Đế Ất qui muội, kỳ quân chi quệ, bất như kỳ đệ chi quệ lương, nguyệt cơ vọng, cát!", "Hào Sáu Năm: Vua Đế Ất gả chồng em gái, vạt áo của vua nó không đẹp bằng vạt áo của vợ lẽ nó. Mặt trăng hầu tuần vọng, tốt!", 822, false, null));
        list.add(new LineJudgment(54, 6, "Thượng Lục", "女承筐无實，士刲羊无血，无攸利。", "Nữ thừa khuông, vô thật: sĩ khuê dương vô huyết; vô du lợi.", "Hào Sáu Trên: Con gái vâng giỏ, không đồ đựng; con trai giết dê, không máu; không thửa lợi.", 824, true, null));

        // Quẻ 55 — PHONG (豐)
        list.add(new LineJudgment(55, 1, "Sơ Cửu", "遇其配主，雖旬无咎，往有尚。", "Ngọ kỳ phối chủ, tuy tuần vô cữu, vãng hữu thượng.", "Hào Chín Đầu: Gặp thử chủ sánh dẫn đều không lỗi, đi có chuộng.", 829, true, null));
        list.add(new LineJudgment(55, 2, "Lục Nhị", "豐其蔀，日中見斗，往得疑疾，有孚發若，吉。", "Phong kỳ bộ, nhật trung kiến Đẩu, vãng đắc nghi tật, hữu phu phát nhược, cát.", "Hào Sáu Hai: Thịnh thửa trướng mặt trời giữa thấy sao Đẩu, đi được ngờ ghét, có tin dường mở ra vậy tốt.", 830, true, null));
        list.add(new LineJudgment(55, 3, "Cửu Tam", "豐其沛，日中見沫，折其右肱，无咎。", "Phong kỳ bái, nhật trung kiến mạt[5], triết kỳ hữu quãng, vô cữu.", "Hào Chín Ba: Thịnh thửa màn, mặt trời giữa thấy sao Mạt, gãy thửa cánh tay phải, không lỗi.", 832, true, null));
        list.add(new LineJudgment(55, 4, "Cửu Tứ", "豐其蔀，日中見斗，遇其夷主，吉。", "Phong kỳ bộ, nhật trung kiến Đẩu, ngộ kỳ di chủ, cát.", "Hào Chín Tư: Thịnh thửa trướng, mặt trời giữa thấy sao Đẩu, gặp thửa chủ ngang tốt.", 834, false, null));
        list.add(new LineJudgment(55, 5, "Lục Ngũ", "來章，有慶譽，吉。", "Lai chương hữu khánh dự, cát.", "Hào Sáu Năm: Lại đẹp có phúc khen, tốt.", 836, false, null));
        list.add(new LineJudgment(55, 6, "Thượng Lục", "豐其屋，蔀其家，窺其戶，闃其无人，三歲不觌，凶。", "Phong kỳ ốc, bộ kỳ gia, khuy kỳ hộ, khuých kỳ vô nhân, tam tuế bất thục, hung.", "Hào Sáu Trên: Thịnh thửa mái, che thửa nhà nhòm thửa cửa, hiu quạnh thửa không người, ba năm chẳng thấy, hung.", 837, true, null));

        // Quẻ 56 — LỮ (旅)
        list.add(new LineJudgment(56, 1, "Sơ Lục", "旅瑣瑣，斯其所取災。", "Lữ tỏa tỏa, tư kỳ sơ thủ tai.", "Hào Sáu Đầu: Hành lữ nhỏ mọn, ấy là cái lấy vạ.", 842, false, null));
        list.add(new LineJudgment(56, 2, "Lục Nhị", "旅即次，懷其資，得童僕貞。", "Lữ tức thứ, hoài kỳ tư, đắc đồng bộc trinh.", "Hào Sáu Hai: Hành lữ tới chỗ trọ, ôm thửa của, được thằngnhỏ, đầy tớ chính bền.", 843, true, null));
        list.add(new LineJudgment(56, 3, "Cửu Tam", "旅焚其次，喪其童僕，貞厲。", "Lữ phần kỳ thứ, táng kỳ đồng bộc, trinh lệ.", "Hào Chín Ba: Hành lữ cháy thửa nhà trợ, mất thửa thằng nhỏ, đầy tớ, chính bền nguy.", 845, false, null));
        list.add(new LineJudgment(56, 4, "Cửu Tứ", "旅于處，得其資斧，我心不快。", "Lữ vu xử, đắc kỳ tư phủ, ngã tâm bất khoái.", "Hào Chín Tư: Hành lữ chưng ở, được thửa của búa, lòng ta chẳng sướng.", 846, true, null));
        list.add(new LineJudgment(56, 5, "Lục Ngũ", "射雉一矢亡，終以譽命。", "Xã trĩ, nhất thỉ vong, chung dĩ dự mệnh.", "Hào Sáu Năm: Bắn chim trĩ, một phát tên mất, chọn lấy khen mệnh.", 848, false, null));
        list.add(new LineJudgment(56, 6, "Thượng Cửu", "鳥焚其巢，旅人先笑后號咷。喪牛于易，凶。", "Điểu phần kỳ sào, lữ nhân tiên tiếu, hậu hào diêu, táng ngưu vu dị, hung!", "Hào Chín Trên: Chim cháy thửa tổ, kẻ hành lữ trước cười, sau kêu gào, mất trâu chưng dễ dàng, hung!", 849, true, null));

        // Quẻ 57 — TỐN (巽)
        list.add(new LineJudgment(57, 1, "Sơ Lục", "進退，利武人之貞。", "Tiến thoái, lợi vũ nhân chi trinh.", "Hào Sáu Đầu: Tiến lui, lợi về sự chính bền của người võ.", 855, false, null));
        list.add(new LineJudgment(57, 2, "Cửu Nhị", "巽在牀下，用史巫紛若，吉无咎。", "Tôn tại sàng hạ, dựng sử vu phân nhược, cát! Vo cữu!", "Hào Chín Hai: Nhún ở dưới giường , dùng thày bói, thày cúng bời bời vậy, tốt! Không lỗi!", 856, true, "Bộ trích cũ nối liền phần lời bình của sách (mở đầu bằng GỊẢI NGHĨA) vào ngay sau lời dịch nghĩa, không có dấu phân cách, khiến trường nghia dài 1056 ký tự thay vì 88. Đã cắt tại mốc GỊẢI NGHĨA; phần giữ lại là nguyên văn lời dịch của Ngô Tất Tố, không sửa một chữ. Lời bình bị cắt không mất giá trị nhưng không thuộc hào từ, nên không được để lẫn trong trường dịch nghĩa."));
        list.add(new LineJudgment(57, 3, "Cửu Tam", "頻巽，吝。", "Tần tốn lận", "Hào Chín Ba: Liền nhún, đáng tiếc.", 857, true, null));
        list.add(new LineJudgment(57, 4, "Lục Tứ", "悔亡，田獲三品。", "Hối vong, điền hoạch tam phẩm.", "Hào Sáu Tư: Ăn năn mất, săn được ba phẩm.", 858, true, null));
        list.add(new LineJudgment(57, 5, "Cửu Ngũ", "貞吉悔亡，无不利。无初有終，先庚三日，后庚三日，吉。", "Trinh cát, hối vong, vô bất lợi, vô cơ hữu chung, tiêu canh tam nhật, hâu canh tam nhật, cát.", "Hào Chín Năm: Chính bền tốt, ăn năn mất, không gì lợi, không đầu có chót, trước canh bà ngày, sau canh ba ngày tốt!", 860, true, null));
        list.add(new LineJudgment(57, 6, "Thượng Cửu", "巽在牀下，喪其資斧，貞凶。", "Tốn tại sàng hạ, táng kỳ tư phủ, hung!", "Hào Chín Trên: Nhún ở dưới giường, mất thửa của búa, hung!.", 861, true, null));

        // Quẻ 58 — ĐOÁI (兌)
        list.add(new LineJudgment(58, 1, "Sơ Cửu", "和兌，吉。", "Hòa đoái, cát!", "Hào Chín Đầu: Hòa đẹp lòng, tốt!", 866, true, null));
        list.add(new LineJudgment(58, 2, "Cửu Nhị", "孚兌，吉，悔亡。", "Phu đoái cát, hối vong.", "Hào Chín Hai: Tin đẹp lòng, tôt ăn năn mất.", 868, true, null));
        list.add(new LineJudgment(58, 3, "Lục Tam", "來兌，凶。", "Lai đoái, hung!", "Hào Sáu Ba: Lại đẹp lòng hung!", 869, false, null));
        list.add(new LineJudgment(58, 4, "Cửu Tứ", "商兌，未寧，介疾有喜。", "Thượng đoái, vị ning, giới tật, hữu hỷ.", "Hào Chín Tư: Đắn đo đẹp lòng chưa yên, thẳng ghét, có mừng.", 869, true, null));
        list.add(new LineJudgment(58, 5, "Cửu Ngũ", "孚于剝，有厲。", "Phu vu hấc, hữu lệ.", "Hào Chín Năm: Tin chung đẽo, có nguy.", 871, true, null));
        list.add(new LineJudgment(58, 6, "Thượng Lục", "引兌。", "Dẩn đoái.", "Hào Sáu Trên: Dẫn đẹp lòng.", 872, true, null));

        // Quẻ 59 — HOÁN (渙)
        list.add(new LineJudgment(59, 1, "Sơ Lục", "用拯馬壯，吉。", "Chửng mã tráng, cát.", "Hào Sáu Đầu: Vớt ngựa mạnh, tốt.", 877, true, null));
        list.add(new LineJudgment(59, 2, "Cửu Nhị", "渙奔其机，悔亡。", "hoán bôn kỳ ngột, hối vong.", "Hào Chín Hai: Tan chạy thửa ghế! Ăn năn mất.", 878, true, null));
        list.add(new LineJudgment(59, 3, "Lục Tam", "渙其躬，无悔。", "Hoán kỳ cung, vô hối.", "Hào Sáu Ba: Tan thửa mình, không ăn năn.", 879, true, null));
        list.add(new LineJudgment(59, 4, "Lục Tứ", "渙其群，元吉。渙有丘，匪夷所思。", "Hoán kỳ quân, nguyên cát; hoán hữu khâu, phi di sở tư.", "Hào Sáu Tư: Tan thửa đàn, cả tốt; tan có gò, chẳng phải thường thửa nghĩ.", 880, true, null));
        list.add(new LineJudgment(59, 5, "Cửu Ngũ", "渙汗其大號，渙王居，无咎。", "Hoán hãn kỳ đại hiệu, hoán vương cư, vô cữu.", "Hào Chín Năm: Tan bồ hôi thửa hiệu lớn, tan chỗ ở của vua, không lỗi.", 882, true, null));
        list.add(new LineJudgment(59, 6, "Thượng Cửu", "渙其血，去逖出，无咎。", "hoán kỳ huyết khứ, địch[4], xuất, vô cữu.", "Hào Chín Trên: Tan thửa máu đi, rùng rợn[5] ra, không lỗi.", 883, false, null));

        // Quẻ 60 — TIẾT (節)
        list.add(new LineJudgment(60, 1, "Sơ Cửu", "不出戶庭，无咎。", "bất xuất hộ đình, vô cữu.", "Hào Chín Đầu: Chẳng ra sân cửa, không lỗi.", 888, true, null));
        list.add(new LineJudgment(60, 2, "Cửu Nhị", "不出門庭，凶。", "bất xuất môn đình, hung.", "Hào Chín Hai: Chẳng ra sân cổng, hung.", 889, true, null));
        list.add(new LineJudgment(60, 3, "Lục Tam", "不節若，則嗟若，无咎。", "Bất tiết nhược, tắt ta nhược, vô cữu", "Hào Sáu Ba: Chẳng dè dặt vậy, thì than thở vậy, không lỗi.", 890, true, null));
        list.add(new LineJudgment(60, 4, "Lục Tứ", "安節，亨。", "An tiết, hanh.", "Hào Sáu Tư: Yên dè dặt, hanh.", 891, false, null));
        list.add(new LineJudgment(60, 5, "Cửu Ngũ", "甘節，吉；往有尚。", "Cam tiết, cát vãng hữu thượng.", "Hào Chín Năm: Sự dè dặt ngọt, tốt, đi có chuộng.", 892, false, null));
        list.add(new LineJudgment(60, 6, "Thượng Lục", "苦節，貞凶，悔亡。", "Khổ tiết, trinh hung, hối vong.", "Hào Sáu Trên: Sự dè dặt khổ, chính bền hung, ăn năn mất.", 893, true, null));

        // Quẻ 61 — TRUNG PHU (中孚)
        list.add(new LineJudgment(61, 1, "Sơ Cửu", "虞吉，有他不燕。", "Ngu cát, hữu tha, bất yên.", "Hào Chín Đầu: Lo tốt, có khác, chẳng yên.", 897, false, null));
        list.add(new LineJudgment(61, 2, "Cửu Nhị", "鳴鶴在陰，其子和之，我有好爵，吾與爾靡之。", "Minh hạc tại âm, kỳ tủ họa chi; ngã hữu hảo tước, ngô dữ nhi my chi.", "Hào Chín Hai: Con Hạc kêu ở chỗ tối, con nó họa đấy, ta có tước tốt, tớ cùng mày ràng[2] đấy.", 899, true, null));
        list.add(new LineJudgment(61, 3, "Lục Tam", "得敵，或鼓或罷，或泣或歌。", "Đắc địch, hoặc cổ hoặc bãi, hoặc khấp hoặc ca", "Hào Sáu Ba: Được kẻ địch, hoặc khua trông, hoặc thôi, hoặc khóc, hoặc múa.", 900, false, null));
        list.add(new LineJudgment(61, 4, "Lục Tứ", "月几望，馬匹亡，无咎。", "Nguyệt cơ vọng, mà xuất vong, vô cữu.", "Hào Sáu Tư: mặt trăng hầu tuần vọng, đôi ngựa mất , không lỗi", 901, true, null));
        list.add(new LineJudgment(61, 5, "Cửu Ngũ", "有孚攣如，无咎。", "Hữu phu loan như, vô cữu.", "Hào Chín Năm: Có tin dường như co quắp vậy, không lỗi.", 902, true, null));
        list.add(new LineJudgment(61, 6, "Thượng Cửu", "翰音登于天，貞凶。", "Hàn âm đăng vu thiên, Trinh hung.", "Hào Chín Trên: Tiếng cánh lên chưng trời, chính bền hung.", 903, false, null));

        // Quẻ 62 — TIỂU QUÁ (小過)
        list.add(new LineJudgment(62, 1, "Sơ Lục", "飛鳥以凶。", "Phi điểu dĩ hung.", "Hào Sáu Đầu: Chim bay lấy hung.", 909, false, null));
        list.add(new LineJudgment(62, 2, "Lục Nhị", "過其祖，遇其妣；不及其君，遇其臣；无咎。", "Quá kỳ tổ, ngộ kỳ tỷ, bất cập kỳ quân, ngộ kỳ thần, vô cữu.", "Hào Sáu Hai: Quá thửa ông, gặp thửa bà; chẳng kịp thửa vua, gặp thửa bề tôi, không lỗi.", 910, true, null));
        list.add(new LineJudgment(62, 3, "Cửu Tam", "弗過防之，從或戕之，凶。", "Phất quá phòng chi, tòng hoặc tường chi, hung!", "Hào Chín Ba: Chẳng quá ngừa đó, theo hoặc hại đó, hung!", 911, false, null));
        list.add(new LineJudgment(62, 4, "Cửu Tứ", "无咎，弗過遇之。往厲必戒，勿用永貞。", "Vô Cữu, phất quá ngộ chi, vãng lệ, tất giới vật dụng vĩnh trinh.", "Hào Chín Tự: Không lỗi chẳng quá gặp đấy, đi nguy, ắt răn, chớ dùng dài lâu chính bền.", 912, true, null));
        list.add(new LineJudgment(62, 5, "Lục Ngũ", "密云不雨，自我西郊，公弋取彼在穴。", "mật vân bất vũ, tự ngã tây giao, công dặc thủ bỉ, tại huyệt.", "Hào Sáu Năm: Mây dầy chẳng mưa, ở cõi tây ta, ông bắn lấy nó ở hang.", 914, false, null));
        list.add(new LineJudgment(62, 6, "Thượng Lục", "弗遇過之，飛鳥離之，凶，是謂災眚。", "Phất ngộ quá chi, phi điểm ly chi, hung thị vị tai sảnh.", "Hào Sáu Trên: Chẳng gặp, quá đây, chim bay lìa đấy, hung, ấy rằng vạ tội.", 915, false, null));

        // Quẻ 63 — KÝ TẾ (既濟)
        list.add(new LineJudgment(63, 1, "Sơ Cửu", "曳其輪，濡其尾，无咎。", "Duệ kỳ luân, nhu kỳ vy, vô cữu.", "Hào Chín Đầu: Kéo thửa bánh xe, ướt thửa đuôi không lỗi.", 920, true, null));
        list.add(new LineJudgment(63, 2, "Lục Nhị", "婦喪其茀，勿逐，七日得。", "Phụ táng kỳ phất, vật trục, thất nhật đắc.", "Hào Sáu Hai: Đàn bà mất thửa khăn trùm[2] chớ đuổi bảy ngày được.", 921, true, null));
        list.add(new LineJudgment(63, 3, "Cửu Tam", "高宗伐鬼方，三年克之，小人勿用。", "Cao Tông phạt Quỷ Phương, tam nhiên khắc chi, tiểu nhân vật dụng.", "Hào Chín Ba: Vua Cao Tông đánh nước Quỷ Phương, ba năm được đấy, kẻ tiểu nhân chờ dùng.", 922, false, null));
        list.add(new LineJudgment(63, 4, "Lục Tứ", "繻有衣袽，終日戒。", "Nhu hữu y như, chung nhật giới.", "Hào Sáu Tư: ướt có áo giẻ, trọn ngày răn.", 923, false, null));
        list.add(new LineJudgment(63, 5, "Cửu Ngũ", "東鄰殺牛，不如西鄰之禴祭，實受其福。", "Đông lân sát ngưu, bất như tây lân chi thược tế, thật thụ kỳ phúc.", "Hào Chín Năm: Láng giềng bên Đông giết trâu, chẳng bằng láng giềng bên Tây tế Thược, thật chịu thửa phúc.", 924, true, null));
        list.add(new LineJudgment(63, 6, "Thượng Lục", "濡其首，厲。", "Nhu kỳ thủ, lệ!", "Hào Sáu Trên: Ướt thửa đầu, nguy!", 925, false, "Bộ trích cũ nối liền phần lời bình của sách (mở đầu bằng GIẢI NGHĨA) vào ngay sau lời dịch nghĩa, không có dấu phân cách, khiến trường nghia dài 516 ký tự thay vì 33. Đã cắt tại mốc GIẢI NGHĨA; phần giữ lại là nguyên văn lời dịch của Ngô Tất Tố, không sửa một chữ. Lời bình bị cắt không mất giá trị nhưng không thuộc hào từ, nên không được để lẫn trong trường dịch nghĩa."));

        // Quẻ 64 — VỊ TẾ (未濟)
        list.add(new LineJudgment(64, 1, "Sơ Lục", "濡其尾，吝。", "Nhu kỳ vĩ lận!", "Hào Sáu Đầu: Ướt thửa đuôi, tiếp đáng!", 929, false, null));
        list.add(new LineJudgment(64, 2, "Cửu Nhị", "曳其輪，貞吉。", "Duệ kỳ luân, trinh cát.", "Hào Chín hai: Kéo thửa bánh xe, chính bền, tốt.", 930, true, null));
        list.add(new LineJudgment(64, 3, "Lục Tam", "未濟，征凶，利涉大川。", "Vị tế chinh hung, lợi thiệp đại xuyên.", "Hào Sáu ba: Chưa sang đi hung, lợi về sang sông lớn.", 931, false, null));
        list.add(new LineJudgment(64, 4, "Cửu Tứ", "貞吉，悔亡，震用伐鬼方，三年有賞于大國。", "Trinh cát, hối vong, chấn dụng phát Quỷ Phương, Tam niên hữu thương vu đại quốc.", "Hào Chín Tư: Chính bền, ăn năn mất, nhức dùng đánh nước Quỷ Phương, ba năm, có thương chưng nước lớn.", 932, false, "Bộ trích cũ nối liền phần lời bình của sách (mở đầu bằng GIẢI NGHĨA) vào ngay sau lời dịch nghĩa, không có dấu phân cách, khiến trường nghia dài 1062 ký tự thay vì 101. Đã cắt tại mốc GIẢI NGHĨA; phần giữ lại là nguyên văn lời dịch của Ngô Tất Tố, không sửa một chữ. Lời bình bị cắt không mất giá trị nhưng không thuộc hào từ, nên không được để lẫn trong trường dịch nghĩa."));
        list.add(new LineJudgment(64, 5, "Lục Ngũ", "貞吉，无悔，君子之光，有孚，吉。", "Trinh cát, vong hối, quân tử chi quang, hữu phu, cát.", "Hào Sáu Năm: chính bền tốt, không ăn năn, sự sáng của đấng quân tử, có tin tốt.", 934, true, null));
        list.add(new LineJudgment(64, 6, "Thượng Cửu", "有孚于飲酒，无咎，濡其首，有孚失是。", "Hữu Phu vu ẩm tửu, vô cữu, nhu kỳ thủ, hữu phu, thất thị.", "Hào Chín Trên: có tin chưng uống rượu, không lỗi, ướt thửa đầu, có tin, mất phải.", 935, true, null));

        return List.copyOf(list);
    }
}
