package io.destinyos.engines.iching;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Que tu (卦辭) content for all 64 hexagrams, King Wen order - see
 * {@link HexagramJudgment} for source and verification status (R24).
 */
final class HexagramJudgments {

    private HexagramJudgments() {
    }

    static Map<Integer, HexagramJudgment> entries() {
        Map<Integer, HexagramJudgment> m = new LinkedHashMap<>();

        m.put(1, new HexagramJudgment(1, "乾元亨利貞.", "Kiền nguyên hanh lợi trinh", "Kiền: Đầu cả, hanh thông, lợi tốt, chính bền.", 80, true, null));
        m.put(2, new HexagramJudgment(2, "坤元亨,利牝⾺之貞,君⼦有攸往,先迷,後得,主利,西南得朋,東北喪朋.安 貞,吉.", "Khôn nguyên hanh, lợi tẫn mã chi trinh. Quân tử hữu du vãng. Tiên mê, hậu đắc, chủ lợi. Tây Nam đắc bằng, Đông Bắc táng bằng, an trinh, cát.", "Quẻ khôn: Đầu cả, hanh thông, Lợi về nết trinh của ngựa cái. Quân tử có sự đi. Trước mê, sau được. Chủ về lợi. Phía Tây Nam được bạn, phía Đông Bắc mất bạn. Yên phận giữ nết trinh thì tốt.", 129, true, null));
        m.put(3, new HexagramJudgment(3, "屯元亨利肩,勿⽤有攸往,利建侯.", "Truân nguyên hanh lợi trinh, vật dụng hữu du vãng, lợi kiến hầu.", "Truân đầu cả, hanh thông, lợi tốt, chính bền, chớ dùng có thửa đi, lợi về dựng tước hầu.", 155, true, null));
        m.put(4, new HexagramJudgment(4, "蒙亨, 匪我童蒙, 童蒙求我.初筮告,再三漬, 漬則不告.利貞.", "Mông hanh, phỉ ngã cầu đồng mông, đồng mông cầu ngã. Sơphệ cốc, tái tam độc, độc tắc bất cốc, lợi trinh.", "Quẻ Mông hanh, chẳng phải ta tìm trẻ thơ[1], trẻ thơ tìm ta. Mới bói bảo: hai, ba lần nhàm, không bảo. Lợi về sự chính.", 169, true, null));
        m.put(5, new HexagramJudgment(5, "需,有孚,光,亨,貞,吉,利涉⼤川.", "Nhu, hữu phu, quang, hanh, trinh, cát, lợi thiệp đại xuyên.", "Quẻ Nhu, có đức tin, sáng láng, hanh thông, chính bền, tốt! Lợi sang sông lớn.", 184, true, null));
        m.put(6, new HexagramJudgment(6, "訟有孚室惕,中吉,終凶,利⾒⼤⼈,不利涉⼤川.", "Tụng, hữu phu chất Dịch, trung cát, chung hung, lợi kiến đại nhân, bất lợi thiệp đại xuyên.", "Kiện, có thật, bị lấp, phải sợ, vừa phải, tốt; theo đuổi đến chót, xấu; lợi về sự thấy người lớn, không lợi về sự sang sông lớn.", 196, true, null));
        m.put(7, new HexagramJudgment(7, "師貞, 丈⼈吉,無咎", "Sư Trinh, trượng nhân cát, vô cữu.", "Quân chính, bậc trượng nhân tốt, không lỗi.", 209, true, null));
        m.put(8, new HexagramJudgment(8, "⽐吉, 原策元, 永, 貞, ⽆咎, 不寧⽅來, 後夫凶", "Tỵ cát, nguyên phệ nguyên, vĩnh, trinh, vô cữu. Bất ninh phương lai, hậu phu hung.", "Liền nhau tốt, truy nguyên việc bói, đầu cả, lâu dài, chính bền, không lỗi! Chẳng yên mới lại, sau trễ trượng phu, hung!", 222, true, null));
        m.put(9, new HexagramJudgment(9, "⼩畜亨, 密雲不⾬, ⾃我西郊.", "Tiểu súc hanh, mật vân bất vũ, tự ngã tây giao.", "Chứa nhỏ hanh thông, mây dầy không mưa, tự cõi tây ta.", 235, false, null));
        m.put(10, new HexagramJudgment(10, "履虎尾, 不哇⼈, 亨.", "Lý hổ vĩ, bất chất nhân, hanh!", "Xéo đuôi cọp, không cắn người, hanh!", 248, false, null));
        m.put(11, new HexagramJudgment(11, "泰,⼩往⼤來,吉,亨.", "Thái, tiểu văng, đại lai, cát hanh.", "Quẻ Thái, nhỏ đi, lớn lại, lành tốt hanh thông.", 259, false, null));
        m.put(12, new HexagramJudgment(12, "否之匪⼈. 不利君⼦貞, ⼤往⼩來", "Bĩ chi phỉ nhân. Bất lợi quân tử trinh, đại vãng tiểu lai.", "Bỉ đây (?) chạng phải người. Chẳng lợi cho sự chính bền của đấng quân tử, lớn đi nhỏ lại.", 273, false, null));
        m.put(13, new HexagramJudgment(13, "同⼈于野, 亨. 利涉⼤川,利君⼦貞.", "Đồng nhân vu dã, hanh, lợi thiệp đại xuyên; lợi quân tử trinh.", "Cùng người ở đồng, hanh thông, lợi về sự sang sông lớn, lợi cho sự chính bền của đấng quân tử.", 284, false, null));
        m.put(14, new HexagramJudgment(14, "⼤有元亨.", "Đại hữu nguyên hanh.", "Quẻ Đại hữu cả lớn hanh thông.", 297, false, null));
        m.put(15, new HexagramJudgment(15, "謙亨, 君⼦有終.", "Khiêm hanh, quân tử hữu chung.", "Quẻ Khiêm hanh thông, đấng quân tử có sau chót.", 310, false, null));
        m.put(16, new HexagramJudgment(16, "豫, 利建後, ⾏師.", "Dự, lợi kiến hầu, hành sư.", "Quẻ Dự, lợi cho sự dựng nước hầu, trẩy quân.", 321, false, null));
        m.put(17, new HexagramJudgment(17, "隨元亨利貞, 無咎.", "Tùy nguyên hanh lợi trinh, vô cữu.", "Quẻ Tùy cả, lợi, trinh, không lỗi.", 333, false, null));
        m.put(18, new HexagramJudgment(18, "蠱元亨,利涉⼤川,先甲三⽈,後甲三⽈.", "Cổ nguyên hanh, lợi thiệp đại xuyên, tiên giáp tam nhật, hậu giáp tam nhật.", "Quẻ Cổ cả, hanh, lợi về sang sông lớn. Trước giáp ba ngày, sau giáp ba ngày.", 346, false, null));
        m.put(19, new HexagramJudgment(19, "臨元亨,利貞,⾄于⼋⽉有凶.", "Lâm nguyên hanh lợi trinh, chí vu bát nguyệt hữu hung.", "Quẻ Lâm, cả, hanh, lợi, trinh, đến chưng tám tháng, có hung.", 359, false, null));
        m.put(20, new HexagramJudgment(20, "觀盥⽽不薦,有孚顒若.", "Quan, quán nhi bất tiến, hữu phu ngung nhược.", "Quẻ quan, rửa mà không cứng, có tin, dường cung kính vậy.", 370, false, null));
        m.put(21, new HexagramJudgment(21, "噬嗑亨,利⽤獄.", "Phệ hạp hanh, lợi dụng ngục.", "Quẻ Phệ hạp hanh, lợi dùng việc ngục.", 382, false, null));
        m.put(22, new HexagramJudgment(22, "賁亨,⼩利有攸往.", "Bí hanh, lợi tiểu hửu du vãng.", "Quẻ Bí hanh, hơi lợi có thửa đi,", 395, false, null));
        m.put(23, new HexagramJudgment(23, "剝. 不利有攸往.", "Bác bất lợi hữu du vãng.", "Quẻ bác không lợi có thửa đi.", 408, false, null));
        m.put(24, new HexagramJudgment(24, "復亨,出⼊無疾,明來無咎", "Phục hanh xuất nhập vô tật, bằng lai vô cữu.", "Quẻ Phục hanh, ra vào không tật, bạn đến không lỗi. GLÀI NGHĨA Truyện của Trình Di. - Phục hanh nghĩa là đã trở lại thì hanh thông. Khi Dương đã sinh ở dưới, dần dần hanh thịnh mà sinh nuôi muôn vật; đạo đấng quân tử đã trở lại thì dần dần hanh thông, tưới tắm cho thiên hạ, cho nên quẻ Phục có lẽ hanh thịnh. - Ra vào không tật: Ra vào chỉ về sinh lớn, lại sinh ở trong là vào; lớn tiến ở ngoài là ra, nói “ra” trước, là nói cho thuận mà thôi,khí Dương sinh ra, không phải là tự bên ngoài. Đến ở bên trong gọi là vào. Vật mới sinh khí nó rất nhỏ, cho nên phần nhiều hay gian truân, Dương mới sinh khí nó rất nhỏ, cho nên phần nhiều hay bị gẫy. Khí Dương mùa xuân phát ra khí Âm lạnh bẻ gẫy, cứ coi cây cỏ về lúc sớm tối có thể thây rõ. “Ra vào không tật” nghĩa là cái khí Dương nhỏ sinh rồi lớn, không có cái gì hại nó. Đã không có cái gì hại nó mà loại của nó dần dần tiến đến thì là sắp sửa hanh thịnh, cho nên không lỗi.", 419, false, null));
        m.put(25, new HexagramJudgment(25, "無妄元亨,利貞,其匪正,有眚,不利有攸往.", "Vô Vọng nguyên hanh, lợi trinh, kỳ phỉ chính, hửu sảnh, bất lợi hữu du vãng.", "Quẻ Vô Vọng cả hanh, lợi về sự chính bền; thửa chẳng chính có tội, không lợi có thửa đi.", 432, false, null));
        m.put(26, new HexagramJudgment(26, "⼤畜利貞,不家⾷,吉,利涉⼤川", "Đại Súc lợi trinh bất gia thực, cát, lợi thiệp đại xuyên.", "Quẻ Đại Súc lợi về sự chính, chẳng ăn ở nhà, tốt lợi sang sông lớn.", 446, false, null));
        m.put(27, new HexagramJudgment(27, "頤貞吉, 觀頭, ⾃求⼜實.", "Dì trinh cát, quan di tự cầu khẩu thực.", "Quẻ Di chính tốt, xem sự nuôi, tự tìm cái thật của miệng.", 459, false, null));
        m.put(28, new HexagramJudgment(28, "⼤過, 棟橈, 利有攸往, 亨.", "Đại quá, đống nạo, lợi hữu du văng, hanh.", "Quẻ Đại quá, cột ỏe, lợi có thửa đi, hanh.", 472, false, null));
        m.put(29, new HexagramJudgment(29, "習坎有孚維⼼, 亨, ⾏有尚.", "Tập Khảm hữu phu duy tâm, hanh, hành hữu thượng.", "Quẻ Khảm kép, có tin, bui[2] lòng, hanh, đi có chuộng.", 483, false, null));
        m.put(30, new HexagramJudgment(30, "灕利貞, 亨, 畜牝⽜, 吉.", "Ly lợi trinh, hanh, xúc tẫn ngưu, cát", "Quẻ Ly vệ sự chính, hanh, nuôi trâu cái, tốt.", 496, false, null));
        m.put(31, new HexagramJudgment(31, "咸亨利員, 取⼥吉.", "Hàm hanh, lợi trinh, thù nữ, cát.", "Quẻ Hàm hanh, lợi chính, lấy con gái, tốt.GIẢI NGHĨA Truyện của Trình Di. - Hàm tức là cảm, nhưng không nói cảm, vì Hàm còn có nghĩa nữa là đều, tức là trai gái cảm lẫn nhau vậy. Các vật cảm nhau, không gì thiết tha bằng trai với gái mà hạng tuổi trẻ càng thiết tha hơn. Các vật cảm nhau thì có lẽ hanh thông, cho nên quẻ Hàm mới có lẽ hanh, Lợi trinh nghĩa là cái đạo cảm nhau, lợi về sự chính. Lấy con gái tốt, là nói tài quẻ. Quẻ có mềm trên cứng dưới, hai khí cảm ứng với nhau, đậu mà đẹp lòng nghĩa trai chịu dưới gái, dùng cách đó mà lấy con gái, thì được chính đáng mà tốt lành. Bản nghĩa của Chu Hy. - Hàm là giao cảm. Đoái mềm ở trên, Cấn cứng ở dưới, mà cùng cảm ứng với nhau. Lại, Cấn chủ đậu, thì sự cảm được chuyên nhất. Đoái chủ đẹp lòng thì sự ứng đến tột bậc. Lại nữa, Cấn lấy mình là hạng thiếu nam (trai trẻ), mà chịu dưới Đoái là hạng thiếu nữ (gái trẻ) trai trước gái, được chính đạo của trai gái, vừa đúng thì hôn nhân, cho nên quẻ của nó là Hàm, mà lời Chỉêm của nó là hanh mà lợi về sự chính, lấy con gái thì tốt. Bởi vì cảm thì phải thông, nhưng nếu lấy nhau không theo chính đạo, thì mất sự hanh, mà các việc làm đều hung!", 508, false, null));
        m.put(32, new HexagramJudgment(32, "恆亨,無咎,利貞,利有攸往.", "Hằng hanh, vô cữu, lợi trinh, lợi hữu du vãng.", "Quẻ Hằng hanh, lợi về sự chính, lợi có thửa đi.", 521, false, null));
        m.put(33, new HexagramJudgment(33, "遯恆,⼩利貞.", "Độn hanh, tiểu lợi trinh.", "Quẻ độn hanh, nhỏ lợi trinh.", 534, false, null));
        m.put(34, new HexagramJudgment(34, "⼤壯剩貞.", "Đại tráng lợi trinh.", "Quẻ Đại tráng lợi về sự chính.", 546, false, null));
        m.put(35, new HexagramJudgment(35, "晉,康侯⽤锡⾺蕃庶, 書⽇三接.", "Tấn, khang hầu dụng tích mã phồn thứ, trú nhật tam tiếp.", "Quẻ Tấn, tước hầu yên dừng cho ngựa giậm nhiều, ban ngày ba lần tiếp.", 557, false, null));
        m.put(36, new HexagramJudgment(36, "明夷利艱貞.", "Minh di lợi gian trinh.", "Quẻ Minh di lợi về khó nhọc trinh chính.", 569, false, null));
        m.put(37, new HexagramJudgment(37, "家⼈利⼥真.", "Gia nhân lợi nữ trinh.", "Quẻ gia nhân lợi về gái chính.", 583, false, null));
        m.put(38, new HexagramJudgment(38, "睽,⼩事吉.", "Khuê, tiểu sự cát.", "Quẻ Khuê, việc nhỏ tốt.", 595, false, null));
        m.put(39, new HexagramJudgment(39, "蹇利西南,不利柬北,利⾒⼤⼈,貞吉.", "Kiển lợi Tây Nam, bất lợi Đông Bắc, lợi kiến đại nhân trinh cát.", "Quẻ Kiển, lợi Tây Nam không lợi Đông Bắc, lợi về sự thấy người lớn, chính thì tốt.", 609, false, null));
        m.put(40, new HexagramJudgment(40, "解利西南,無所往,其來復,吉,有攸往,夙吉.", "Giải lợi Tây Nam, vô sở vãng, kỳ lai phục, cát, hữu du vãng, túc cát.", "Quẻ Giải lợi về phương Tây Nam, không thửa đi, thì lại lại, có thửa đi, sớm thì tốt.", 622, false, null));
        m.put(41, new HexagramJudgment(41, "损有孚,元吉, 無咎,可貞,利有攸往", "Tổn hữu phu, nguyên cát, vô cữu, khả trinh, lợi hữu du vãng.", "Quẻ Tổn, có tin, cả tốt, không lỗi, khá trinh, lợi có thửa đi.GIẢI NGHĨA Truyện của Trình Di. - Tổn là giảm bớt, phàm việc nén bớt sự thái quá, để tới nghĩa lý, đều là đạo “bớt” vậy. Đạo “bớt” ắt có thành tín, nghĩa là chí thành thuận lý vậy. Bớt mà thuận lý thì cả thiện mà tốt. Cái đã bớt mà không quá sai, thì có thể chính bền, thường làm mà lợi có thửa đi vậy. Sự bớt của người ta hoặc thái quá hoặc bất cập, hoặc bất thường, đều không hợp chính lý, không phải có tin, thì không tốt mà có lỗi, không phải cái đạo có thể chính bền, thì không nên làm. Bản nghĩa của Chu Hy. - Tổn là giảm bớt. Nó là quẻ bớt vạch Dương trên của quẻ dưới, thêm vào vạch Âm trên của quẻ trên, lấy bớt sự sâu của chằm Đoái, thêm vào sự cao của núi Cấn, tổn dưới ích trên, tổn trong ích ngoài, là Tượng đẽo gọt cửa dân để cung phụng vua, vì vậy mới là quẻ Tổn. Bớt cái đáng bớt mà có tin tín, thì Chiêm của nó sẽ ứng với bốn điều dưới (tức là cả tốt, không lỗi có thể chính bền, lợi có thửa đi).", 636, false, null));
        m.put(42, new HexagramJudgment(42, "益利有攸往,利涉⼤川.", "Ích lợi hữu du vãng, lợi thiệp đại xuyên.", "Quẻ Ích lợi có thửa đi, lợi về sang sông lớn.", 652, false, null));
        m.put(43, new HexagramJudgment(43, "夬揚于王庭,孚號有厲,告⾃⾢.不利即戎,利肴鈦往.", "Quyết, dương vu vương đình, phu hiệu[3] hữu lệ. Cáo tự ấp, bất lợi tức nhưng, lợi hữu dù vãng.", "Quẻ Quải, giơ chưng sân vua, tin gọi, có nguy. Bảo từ làng, chẳng lợi tới quân, lợi có thửa đi.", 668, false, null));
        m.put(44, new HexagramJudgment(44, "垢,⼥壯,勿⽤取⼥", "Cấu, nữ tráng, vật dụng thú nữ.", "Quẻ Cấu, con gái mạnh, chớ dùng lấy con gái.", 684, false, null));
        m.put(45, new HexagramJudgment(45, "萃亨,王假有廟.", "Tụy hanh, vương cách[1] hữu miếu.", "Quẻ Tụy hanh, vua đến có miếu.", 698, false, null));
        m.put(46, new HexagramJudgment(46, "升元亨,⽤⾒⼤⼈,勿恤,南征吉", "Thăng nguyên hanh, dụng kiến đại nhân, vật tuất, nam chinh cát.", "Quẻ Thăng, cả hanh, dùng thấy người lớn, chớ lo, đi về phương Nam tốt.", 713, false, null));
        m.put(47, new HexagramJudgment(47, "困亨貞,⼤⼈吉,無咎, 有⾔不信.", "Khốn, hanh trinh, đại nhân cát, vô cữu, hữu ngôn bất tín.", "Quẻ Khốn hanh, chính bền, người lớn tốt , không lỗi, có nói không tin.", 724, false, null));
        m.put(48, new HexagramJudgment(48, "井,改 ⾢ 不 改 井,無喪無得, 往 來 井 井.", "Tỉnh, cải ấp bất cải tỉnh, vô đắc vô táng, vãng lai tỉnh tỉnh.", "Quẻ Tỉnh, đổi làng chẳng đổi giếng, không mất không được, đi lại giếng giếng[4].", 740, false, null));
        m.put(49, new HexagramJudgment(49, "⾰, 已 ⽇ 乃 孚, 元 亨, 利 貞, 悔 亡.", "Cách, dĩ nhật nãi phu, nguyên hanh lợi trinh, hối vong.", "Quẻ Cách, hết ngày bèn tin, cả hanh lợi trinh, ăn năn mất.", 753, false, null));
        m.put(50, new HexagramJudgment(50, "⿍, 元 吉 亨.", "Đỉnh nguyên cát hanh.", "Quẻ Đỉnh, cả tốt hanh.", 766, false, null));
        m.put(51, new HexagramJudgment(51, "震亨, 震來虢虢, 笑⾔啞啞, 震驚百⾥, 不喪⼔⾿.", "Chấn hanh, Chấn lại khích khích, tiếu ngôn ách ách, chấn kinh bách lý, bất táng … chủy Xưởng.", "Quẻ Chấn hanh, sợ lại ngơm ngớp, cười nói khanh khách, nhức kinh trăm dậm, chẳng mất môi và rượu Xưởng.", 778, false, null));
        m.put(52, new HexagramJudgment(52, "⾉其背, 不獲其⾝, ⾏其庭, 不⾒其⼈, ⽆咎.", "Cấn kỳ bối, bất hoạch kỳ thân, thành kỳ đình, bất kiến kỳ nhân, vô cữu.", "Đậu thửa lưng, chẳng được thửa mình, đi thửa sân, chẳng thấy thửa người, không lỗi.", 790, false, null));
        m.put(53, new HexagramJudgment(53, "漸⼥歸, 吉, 利貞.", "Tiêm, nữ qui, cát, lợi trinh.", "Quẻ Tiệm, con gái về, tốt, lợi về chính bền.", 802, false, null));
        m.put(54, new HexagramJudgment(54, "歸妺征凶,無攸利.", "Qui muội chinh hung, vô du lợi.", "Quẻ Qui muội, đi hung không thửa lợi.", 814, false, null));
        m.put(55, new HexagramJudgment(55, "豐亨, 王假之, 勿憂宜⽇中.", "Phong hanh, vương cách chi, vật ưu nghi nhật trung.", "Quẻ Phong hanh thông, vua đến đấy, chớ lo, nên mặt trời giữa.", 826, false, null));
        m.put(56, new HexagramJudgment(56, "旅⼩亨, 旅貞吉.", "Lữ tiểu hanh, lữ trinh cát.", "Quẻ Lữ nhỏ hanh thông sự đi đường chính bền tốt.", 840, false, null));
        m.put(57, new HexagramJudgment(57, "巽⼩亨, 利有攸往, 利⾒⼤⼈.", "Tốn tiểu hanh, lợi hữu du vãng, lợi kiến đại nhân.", "Quẻ Tốn nhỏ hanh thông, lợi có thửa đi, lời thấy người lớn.", 852, false, null));
        m.put(58, new HexagramJudgment(58, "兌; 亨．利貞", "Đoái hanh lợi trinh.", "Quẻ Đoái hanh, lợi về chính bền.", 864, false, null));
        m.put(59, new HexagramJudgment(59, "渙 亨 . 王 假 有 廟 . 利 涉 ⼤ 川 . 利貞.", "Hoán hanh, vương cách hữu miếu, lợi thiệp đại xuyầg, lợi trinh.", "Quẻ Hoán hanh, vua đến có miếu, lợi sang sông lớn, lợi về chính bền.", 874, false, null));
        m.put(60, new HexagramJudgment(60, "節 . 亨 . 苦 節 不 可 貞 .", "Tiết hanh, khổ tiết bất khả trinh.", "Quẻ Tiết hanh, sự dè dặt khổ không thể chính bền", 885, false, null));
        m.put(61, new HexagramJudgment(61, "中孚.豚⿂吉.利涉⼤川.利貞.", "Trung phu, đôn ngư cát, lợi thiệp đại xuyên, lợi trinh.", "Quẻ Trung phu: Cá lợn tốt, lợi sang sông lớn, lợi về chính bền.", 895, false, null));
        m.put(62, new HexagramJudgment(62, "⼩過.亨.利貞.", "Tiểu quá hanh, lợi trinh.", "Quẻ Tiểu Quá hanh, lợi về chính bền", 905, false, null));
        m.put(63, new HexagramJudgment(63, "既濟.亨.⼩利貞.初吉終亂.", "Ký Tế hanh, tiểu lợi trinh, sơ cát, chung loạn.", "Quẻ ký tế hanh, nhỏ lợi về chính bền, đầu tốt, chót loạn.", 917, false, null));
        m.put(64, new HexagramJudgment(64, "未濟.亨.⼩狐汔濟.濡其尾.無攸利.", "Vị tế hanh, tiểu hồ hất tế, nhu kỳ vĩ, vô du lợi.", "Quẻ vị tế hanh, con cáo nhỏ hầu sang, ướt thửa đuôi, không thửa lợi.", 927, false, null));

        return m;
    }
}
