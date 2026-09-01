package io.destinyos.engines.iching;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Quẻ từ (卦辭) for all 64 hexagrams, King Wen order.
 *
 * <p>Hán tự from zh.wikisource.org; Hán-Việt and nghĩa from Ngô Tất Tố.
 * See {@link HexagramJudgment} for the full provenance and why the two
 * halves come from different sources (R24/R25 verification, 2026-09-01).
 */
final class HexagramJudgments {

    private HexagramJudgments() {
    }

    static Map<Integer, HexagramJudgment> entries() {
        Map<Integer, HexagramJudgment> m = new LinkedHashMap<>();

        m.put(1, new HexagramJudgment(1, "乾", "乾：元亨。利貞。", "Kiền nguyên hanh lợi trinh", "Kiền: Đầu cả, hanh thông, lợi tốt, chính bền.", 80, false, null));
        m.put(2, new HexagramJudgment(2, "坤", "坤：元亨。利牝馬之貞。君子有攸往，先迷後得主。利西南得朋，東北喪朋。安貞，吉。", "Khôn nguyên hanh, lợi tẫn mã chi trinh. Quân tử hữu du vãng. Tiên mê, hậu đắc, chủ lợi. Tây Nam đắc bằng, Đông Bắc táng bằng, an trinh, cát.", "Quẻ khôn: Đầu cả, hanh thông, Lợi về nết trinh của ngựa cái. Quân tử có sự đi. Trước mê, sau được. Chủ về lợi. Phía Tây Nam được bạn, phía Đông Bắc mất bạn. Yên phận giữ nết trinh thì tốt.", 129, false, null));
        m.put(3, new HexagramJudgment(3, "屯", "屯：元亨，利貞。勿用有攸往，利建侯。", "Truân nguyên hanh lợi trinh, vật dụng hữu du vãng, lợi kiến hầu.", "Truân đầu cả, hanh thông, lợi tốt, chính bền, chớ dùng có thửa đi, lợi về dựng tước hầu.", 155, true, null));
        m.put(4, new HexagramJudgment(4, "蒙", "蒙：亨。匪我求童蒙，童蒙求我。初筮告，再三瀆，瀆則不告。利貞。", "Mông hanh, phỉ ngã cầu đồng mông, đồng mông cầu ngã. Sơphệ cốc, tái tam độc, độc tắc bất cốc, lợi trinh.", "Quẻ Mông hanh, chẳng phải ta tìm trẻ thơ[1], trẻ thơ tìm ta. Mới bói bảo: hai, ba lần nhàm, không bảo. Lợi về sự chính.", 169, true, null));
        m.put(5, new HexagramJudgment(5, "需", "需：有孚，光亨。貞吉，利涉大川。", "Nhu, hữu phu, quang, hanh, trinh, cát, lợi thiệp đại xuyên.", "Quẻ Nhu, có đức tin, sáng láng, hanh thông, chính bền, tốt! Lợi sang sông lớn.", 184, false, null));
        m.put(6, new HexagramJudgment(6, "訟", "訟：有孚，窒，惕，中吉，終凶。利見大人，不利涉大川。", "Tụng, hữu phu chất Dịch, trung cát, chung hung, lợi kiến đại nhân, bất lợi thiệp đại xuyên.", "Kiện, có thật, bị lấp, phải sợ, vừa phải, tốt; theo đuổi đến chót, xấu; lợi về sự thấy người lớn, không lợi về sự sang sông lớn.", 196, true, null));
        m.put(7, new HexagramJudgment(7, "師", "師：貞丈人吉，无咎。", "Sư Trinh, trượng nhân cát, vô cữu.", "Quân chính, bậc trượng nhân tốt, không lỗi.", 209, true, null));
        m.put(8, new HexagramJudgment(8, "比", "比：吉。原筮元永貞，无咎。不寧方來，後夫凶。", "Tỵ cát, nguyên phệ nguyên, vĩnh, trinh, vô cữu. Bất ninh phương lai, hậu phu hung.", "Liền nhau tốt, truy nguyên việc bói, đầu cả, lâu dài, chính bền, không lỗi! Chẳng yên mới lại, sau trễ trượng phu, hung!", 222, true, null));
        m.put(9, new HexagramJudgment(9, "小畜", "小畜：亨。密雲不雨，自我西郊。", "Tiểu súc hanh, mật vân bất vũ, tự ngã tây giao.", "Chứa nhỏ hanh thông, mây dầy không mưa, tự cõi tây ta.", 235, false, null));
        m.put(10, new HexagramJudgment(10, "履", "履虎尾，不咥人，亨。", "Lý hổ vĩ, bất chất nhân, hanh!", "Xéo đuôi cọp, không cắn người, hanh!", 248, true, null));
        m.put(11, new HexagramJudgment(11, "泰", "泰：小往大來，吉亨。", "Thái, tiểu văng, đại lai, cát hanh.", "Quẻ Thái, nhỏ đi, lớn lại, lành tốt hanh thông.", 259, false, null));
        m.put(12, new HexagramJudgment(12, "否", "否之匪人，不利君子貞，大往小來。", "Bĩ chi phỉ nhân. Bất lợi quân tử trinh, đại vãng tiểu lai.", "Bỉ đây (?) chạng phải người. Chẳng lợi cho sự chính bền của đấng quân tử, lớn đi nhỏ lại.", 273, false, null));
        m.put(13, new HexagramJudgment(13, "同人", "同人于野，亨。利涉大川，利君子貞。", "Đồng nhân vu dã, hanh, lợi thiệp đại xuyên; lợi quân tử trinh.", "Cùng người ở đồng, hanh thông, lợi về sự sang sông lớn, lợi cho sự chính bền của đấng quân tử.", 284, false, null));
        m.put(14, new HexagramJudgment(14, "大有", "大有：元亨。", "Đại hữu nguyên hanh.", "Quẻ Đại hữu cả lớn hanh thông.", 297, false, null));
        m.put(15, new HexagramJudgment(15, "謙", "謙：亨，君子有終。", "Khiêm hanh, quân tử hữu chung.", "Quẻ Khiêm hanh thông, đấng quân tử có sau chót.", 310, false, null));
        m.put(16, new HexagramJudgment(16, "豫", "豫：利建侯行師。", "Dự, lợi kiến hầu, hành sư.", "Quẻ Dự, lợi cho sự dựng nước hầu, trẩy quân.", 321, true, null));
        m.put(17, new HexagramJudgment(17, "隨", "隨：元亨。利貞。无咎。", "Tùy nguyên hanh lợi trinh, vô cữu.", "Quẻ Tùy cả, lợi, trinh, không lỗi.", 333, true, null));
        m.put(18, new HexagramJudgment(18, "蠱", "蠱：元亨。利涉大川。先甲三日，後甲三日。", "Cổ nguyên hanh, lợi thiệp đại xuyên, tiên giáp tam nhật, hậu giáp tam nhật.", "Quẻ Cổ cả, hanh, lợi về sang sông lớn. Trước giáp ba ngày, sau giáp ba ngày.", 346, true, null));
        m.put(19, new HexagramJudgment(19, "臨", "臨：元亨。利貞。至于八月有凶。", "Lâm nguyên hanh lợi trinh, chí vu bát nguyệt hữu hung.", "Quẻ Lâm, cả, hanh, lợi, trinh, đến chưng tám tháng, có hung.", 359, false, null));
        m.put(20, new HexagramJudgment(20, "觀", "觀：盥而不荐，有孚顒若。", "Quan, quán nhi bất tiến, hữu phu ngung nhược.", "Quẻ quan, rửa mà không cứng, có tin, dường cung kính vậy.", 370, true, null));
        m.put(21, new HexagramJudgment(21, "噬嗑", "噬嗑：亨。利用獄。", "Phệ hạp hanh, lợi dụng ngục.", "Quẻ Phệ hạp hanh, lợi dùng việc ngục.", 382, false, null));
        m.put(22, new HexagramJudgment(22, "賁", "賁：亨。小利有攸往。", "Bí hanh, lợi tiểu hửu du vãng.", "Quẻ Bí hanh, hơi lợi có thửa đi,", 395, false, null));
        m.put(23, new HexagramJudgment(23, "剝", "剝：不利。有攸往。", "Bác bất lợi hữu du vãng.", "Quẻ bác không lợi có thửa đi.", 408, false, null));
        m.put(24, new HexagramJudgment(24, "復", "復：亨。出入无疾，朋來无咎。反復其道，七日來復，利有攸往。", "Phục hanh xuất nhập vô tật, bằng lai vô cữu. Phản phúc kỳ đạo, thất nhật lai phục, lợi hữu du vãng.", "Quẻ Phục hanh, ra vào không tật, bạn đến không lỗi. Lật đi lật lại thửa đạo, bảy ngày lại trở lại, lợi có thửa đi.", 419, true, null));
        m.put(25, new HexagramJudgment(25, "无妄", "无妄：元亨。利貞。其匪正有眚，不利有攸往。", "Vô Vọng nguyên hanh, lợi trinh, kỳ phỉ chính, hửu sảnh, bất lợi hữu du vãng.", "Quẻ Vô Vọng cả hanh, lợi về sự chính bền; thửa chẳng chính có tội, không lợi có thửa đi.", 432, true, null));
        m.put(26, new HexagramJudgment(26, "大畜", "大畜：利貞，不家食吉，利涉大川。", "Đại Súc lợi trinh bất gia thực, cát, lợi thiệp đại xuyên.", "Quẻ Đại Súc lợi về sự chính, chẳng ăn ở nhà, tốt lợi sang sông lớn.", 446, false, null));
        m.put(27, new HexagramJudgment(27, "頤", "頤：貞吉。觀頤，自求口實。", "Dì trinh cát, quan di tự cầu khẩu thực.", "Quẻ Di chính tốt, xem sự nuôi, tự tìm cái thật của miệng.", 459, true, null));
        m.put(28, new HexagramJudgment(28, "大過", "大過：棟橈，利有攸往，亨。", "Đại quá, đống nạo, lợi hữu du văng, hanh.", "Quẻ Đại quá, cột ỏe, lợi có thửa đi, hanh.", 472, false, null));
        m.put(29, new HexagramJudgment(29, "習坎", "習坎：有孚，維心亨。行有尚。", "Tập Khảm hữu phu duy tâm, hanh, hành hữu thượng.", "Quẻ Khảm kép, có tin, bui[2] lòng, hanh, đi có chuộng.", 483, false, "Trang nguồn zh.wikisource đặt tên quẻ tắt là 坎, nhưng chính lời quẻ mở đầu bằng 習坎, và bản Ngô Tất Tố cũng đặt tên chương là \"Quẻ Tập Khảm\" (Tập = 習). Hai trên ba chứng cứ dùng dạng 習坎 nên lấy dạng này làm tên; ghi lại để không bị đọc thành mâu thuẫn với các bảng khác dùng dạng tắt."));
        m.put(30, new HexagramJudgment(30, "離", "離：利貞。亨。畜牝牛，吉。", "Ly lợi trinh, hanh, xúc tẫn ngưu, cát", "Quẻ Ly vệ sự chính, hanh, nuôi trâu cái, tốt.", 496, true, null));
        m.put(31, new HexagramJudgment(31, "咸", "咸：亨。利貞。取女吉。", "Hàm hanh, lợi trinh, thù nữ, cát.", "Quẻ Hàm hanh, lợi chính, lấy con gái, tốt.", 508, true, "Bộ trích cũ nối liền phần lời bình của sách (mở đầu bằng GIẢI NGHĨA) vào ngay sau lời dịch nghĩa, không có dấu phân cách, khiến trường nghia dài 1147 ký tự thay vì 42. Đã cắt tại mốc GIẢI NGHĨA; phần giữ lại là nguyên văn lời dịch của Ngô Tất Tố, không sửa một chữ. Lời bình bị cắt không mất giá trị nhưng không thuộc hào từ, nên không được để lẫn trong trường dịch nghĩa."));
        m.put(32, new HexagramJudgment(32, "恆", "恆：亨，无咎。利貞，利有攸往。", "Hằng hanh, vô cữu, lợi trinh, lợi hữu du vãng.", "Quẻ Hằng hanh, lợi về sự chính, lợi có thửa đi.", 521, true, null));
        m.put(33, new HexagramJudgment(33, "遯", "遯：亨。小利貞。", "Độn hanh, tiểu lợi trinh.", "Quẻ độn hanh, nhỏ lợi trinh.", 534, true, null));
        m.put(34, new HexagramJudgment(34, "大壯", "大壯：利貞。", "Đại tráng lợi trinh.", "Quẻ Đại tráng lợi về sự chính.", 546, true, null));
        m.put(35, new HexagramJudgment(35, "晉", "晉：康侯用錫馬蕃庶，晝日三接。", "Tấn, khang hầu dụng tích mã phồn thứ, trú nhật tam tiếp.", "Quẻ Tấn, tước hầu yên dừng cho ngựa giậm nhiều, ban ngày ba lần tiếp.", 557, true, null));
        m.put(36, new HexagramJudgment(36, "明夷", "明夷：利艱貞。", "Minh di lợi gian trinh.", "Quẻ Minh di lợi về khó nhọc trinh chính.", 569, false, null));
        m.put(37, new HexagramJudgment(37, "家人", "家人：利女貞。", "Gia nhân lợi nữ trinh.", "Quẻ gia nhân lợi về gái chính.", 583, true, null));
        m.put(38, new HexagramJudgment(38, "睽", "睽：小事吉。", "Khuê, tiểu sự cát.", "Quẻ Khuê, việc nhỏ tốt.", 595, false, null));
        m.put(39, new HexagramJudgment(39, "蹇", "蹇：利西南，不利東北；利見大人，貞吉。", "Kiển lợi Tây Nam, bất lợi Đông Bắc, lợi kiến đại nhân trinh cát.", "Quẻ Kiển, lợi Tây Nam không lợi Đông Bắc, lợi về sự thấy người lớn, chính thì tốt.", 609, true, null));
        m.put(40, new HexagramJudgment(40, "解", "解：利西南，无所往，其來復吉。有攸往，夙吉。", "Giải lợi Tây Nam, vô sở vãng, kỳ lai phục, cát, hữu du vãng, túc cát.", "Quẻ Giải lợi về phương Tây Nam, không thửa đi, thì lại lại, có thửa đi, sớm thì tốt.", 622, true, null));
        m.put(41, new HexagramJudgment(41, "損", "損：有孚，元吉。无咎，可貞，利有攸往。曷之用？二簋可用享。", "Tổn hữu phu, nguyên cát, vô cữu, khả trinh, lợi hữu du vãng. Hạt chi dụng? Nhị quĩ khả dụng hưởng.", "Quẻ Tổn, có tin, cả tốt, không lỗi, khá trinh, lợi có thửa đi.", 636, true, "Bộ trích cũ nối liền phần lời bình của sách (mở đầu bằng GIẢI NGHĨA) vào ngay sau lời dịch nghĩa, không có dấu phân cách, khiến trường nghia dài 1017 ký tự thay vì 62. Đã cắt tại mốc GIẢI NGHĨA; phần giữ lại là nguyên văn lời dịch của Ngô Tất Tố, không sửa một chữ. Lời bình bị cắt không mất giá trị nhưng không thuộc hào từ, nên không được để lẫn trong trường dịch nghĩa."));
        m.put(42, new HexagramJudgment(42, "益", "益：利有攸往。利涉大川。", "Ích lợi hữu du vãng, lợi thiệp đại xuyên.", "Quẻ Ích lợi có thửa đi, lợi về sang sông lớn.", 652, false, null));
        m.put(43, new HexagramJudgment(43, "夬", "夬：揚于王庭，孚號，有厲，告自邑，不利即戎，利有攸往。", "Quyết, dương vu vương đình, phu hiệu[3] hữu lệ. Cáo tự ấp, bất lợi tức nhưng, lợi hữu dù vãng.", "Quẻ Quải, giơ chưng sân vua, tin gọi, có nguy. Bảo từ làng, chẳng lợi tới quân, lợi có thửa đi.", 668, true, null));
        m.put(44, new HexagramJudgment(44, "姤", "姤：女壯，勿用取女。", "Cấu, nữ tráng, vật dụng thú nữ.", "Quẻ Cấu, con gái mạnh, chớ dùng lấy con gái.", 684, true, null));
        m.put(45, new HexagramJudgment(45, "萃", "萃：亨。王假有廟，利見大人，亨。利貞。用大牲吉，利有攸往。", "Tụy hanh, vương cách[1] hữu miếu. Lợi kiến đại nhân, hanh, lợi trinh. Dụng đại sính cát, lợi hữu du vãng.", "Quẻ Tụy hanh, vua đến có miếu. Lợi thấy bậc người lớn, hạnh, lợi về chính bền. Dùng con sinh, lớn, tốt, lợi có thửa đi.", 698, true, null));
        m.put(46, new HexagramJudgment(46, "升", "升：元亨，用見大人，勿恤，南征吉。", "Thăng nguyên hanh, dụng kiến đại nhân, vật tuất, nam chinh cát.", "Quẻ Thăng, cả hanh, dùng thấy người lớn, chớ lo, đi về phương Nam tốt.", 713, false, null));
        m.put(47, new HexagramJudgment(47, "困", "困：亨，貞大人吉，无咎，有言不信。", "Khốn, hanh trinh, đại nhân cát, vô cữu, hữu ngôn bất tín.", "Quẻ Khốn hanh, chính bền, người lớn tốt , không lỗi, có nói không tin.", 724, true, null));
        m.put(48, new HexagramJudgment(48, "井", "井：改邑不改井，无喪无得，往來井井。汔至亦未繘井。羸其瓶，凶。", "Tỉnh, cải ấp bất cải tỉnh, vô đắc vô táng, vãng lai tỉnh tỉnh. Hất chí, diệc vị quất tỉnh, luy kỳ bình hung.", "Quẻ Tỉnh, đổi làng chẳng đổi giếng, không mất không được, đi lại giếng giếng[4]. Hầu đến, cũng chưa dong trạc đến giếng, hỏng thửa lọ, hung.", 739, true, null));
        m.put(49, new HexagramJudgment(49, "革", "革：巳日乃孚，元亨。利貞。悔亡。", "Cách, dĩ nhật nãi phu, nguyên hanh lợi trinh, hối vong.", "Quẻ Cách, hết ngày bèn tin, cả hanh lợi trinh, ăn năn mất.", 753, true, null));
        m.put(50, new HexagramJudgment(50, "鼎", "鼎：元吉，亨。", "Đỉnh nguyên cát hanh.", "Quẻ Đỉnh, cả tốt hanh.", 766, false, null));
        m.put(51, new HexagramJudgment(51, "震", "震：亨。震來虩虩，笑言啞啞。震驚百里，不喪匕鬯。", "Chấn hanh, Chấn lại khích khích, tiếu ngôn ách ách, chấn kinh bách lý, bất táng … chủy Xưởng.", "Quẻ Chấn hanh, sợ lại ngơm ngớp, cười nói khanh khách, nhức kinh trăm dậm, chẳng mất môi và rượu Xưởng.", 778, true, null));
        m.put(52, new HexagramJudgment(52, "艮", "艮：艮其背，不獲其身，行其庭，不見其人，无咎。", "Cấn kỳ bối, bất hoạch kỳ thân, thành kỳ đình, bất kiến kỳ nhân, vô cữu.", "Đậu thửa lưng, chẳng được thửa mình, đi thửa sân, chẳng thấy thửa người, không lỗi.", 790, true, null));
        m.put(53, new HexagramJudgment(53, "漸", "漸：女歸吉，利貞。", "Tiêm, nữ qui, cát, lợi trinh.", "Quẻ Tiệm, con gái về, tốt, lợi về chính bền.", 802, false, null));
        m.put(54, new HexagramJudgment(54, "歸妹", "歸妹：征凶，无攸利。", "Qui muội chinh hung, vô du lợi.", "Quẻ Qui muội, đi hung không thửa lợi.", 814, true, null));
        m.put(55, new HexagramJudgment(55, "豐", "豐：亨。王假之，勿憂，宜日中。", "Phong hanh, vương cách chi, vật ưu nghi nhật trung.", "Quẻ Phong hanh thông, vua đến đấy, chớ lo, nên mặt trời giữa.", 826, false, null));
        m.put(56, new HexagramJudgment(56, "旅", "旅：小亨，旅貞吉。", "Lữ tiểu hanh, lữ trinh cát.", "Quẻ Lữ nhỏ hanh thông sự đi đường chính bền tốt.", 840, false, null));
        m.put(57, new HexagramJudgment(57, "巽", "巽：小亨。利有攸往。利見大人。", "Tốn tiểu hanh, lợi hữu du vãng, lợi kiến đại nhân.", "Quẻ Tốn nhỏ hanh thông, lợi có thửa đi, lời thấy người lớn.", 852, false, null));
        m.put(58, new HexagramJudgment(58, "兌", "兌：亨。利貞。", "Đoái hanh lợi trinh.", "Quẻ Đoái hanh, lợi về chính bền.", 864, false, null));
        m.put(59, new HexagramJudgment(59, "渙", "渙：亨。王假有廟，利涉大川，利貞。", "Hoán hanh, vương cách hữu miếu, lợi thiệp đại xuyầg, lợi trinh.", "Quẻ Hoán hanh, vua đến có miếu, lợi sang sông lớn, lợi về chính bền.", 874, false, null));
        m.put(60, new HexagramJudgment(60, "節", "節：亨。苦節不可貞。", "Tiết hanh, khổ tiết bất khả trinh.", "Quẻ Tiết hanh, sự dè dặt khổ không thể chính bền", 885, false, null));
        m.put(61, new HexagramJudgment(61, "中孚", "中孚：豚魚吉，利涉大川，利貞。", "Trung phu, đôn ngư cát, lợi thiệp đại xuyên, lợi trinh.", "Quẻ Trung phu: Cá lợn tốt, lợi sang sông lớn, lợi về chính bền.", 895, false, null));
        m.put(62, new HexagramJudgment(62, "小過", "小過：亨。利貞。可小事，不可大事。飛鳥遺之音，不宜上宜下，大吉。", "Tiểu quá hanh, lợi trinh. Khả tiểu sự, bất khả đại sự, phi điểu dị chi âm bất nghi thượng, nghi hạ đại cát.", "Quẻ Tiểu Quá hanh, lợi về chính bền Khá Việc nhỏ, chẳng khá việc lớn, chim bay để chưng tiếng, chẳng nên lên, nên xuống, cả tốt.", 905, true, null));
        m.put(63, new HexagramJudgment(63, "既濟", "既濟：亨小。利貞。初吉終亂。", "Ký Tế hanh, tiểu lợi trinh, sơ cát, chung loạn.", "Quẻ ký tế hanh, nhỏ lợi về chính bền, đầu tốt, chót loạn.", 917, false, null));
        m.put(64, new HexagramJudgment(64, "未濟", "未濟：亨。小狐汔濟，濡其尾，无攸利。", "Vị tế hanh, tiểu hồ hất tế, nhu kỳ vĩ, vô du lợi.", "Quẻ vị tế hanh, con cáo nhỏ hầu sang, ướt thửa đuôi, không thửa lợi.", 927, true, null));

        return m;
    }
}
