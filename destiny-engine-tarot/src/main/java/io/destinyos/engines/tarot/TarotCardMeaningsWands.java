package io.destinyos.engines.tarot;

import io.destinyos.core.signal.Polarity;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Wands (Fire) suit Vietnamese interpretive content (research item R11).
 * Grounded in standard Rider-Waite-Smith tradition.
 */
final class TarotCardMeaningsWands {

    private TarotCardMeaningsWands() {
    }

    static Map<String, TarotCardMeaning> entries() {
        Map<String, TarotCardMeaning> m = new LinkedHashMap<>();
        m.put("MINOR_WANDS_01_ACE", new TarotCardMeaning(
                List.of("cảm hứng mới", "khởi đầu đầy năng lượng", "tiềm năng sáng tạo"),
                List.of("trì hoãn", "thiếu động lực", "cảm hứng lụi tàn"),
                Polarity.SUPPORT, Polarity.CAUTION,
                "Cơ hội sự nghiệp mới đầy hứng khởi xuất hiện, là lúc thích hợp để bắt đầu một dự án.",
                "Tín hiệu tích cực cho một nguồn thu nhập hoặc cơ hội tài chính mới đang manh nha.",
                "Sự bùng nổ đam mê, khởi đầu nồng nhiệt trong tình cảm.",
                "Nên nắm bắt ý tưởng mới trong khi cảm hứng còn tươi mới, đừng chần chừ quá lâu.",
                "Ngọn lửa khởi đầu, nguồn cảm hứng và tiềm năng sáng tạo dồi dào."
        ));
        m.put("MINOR_WANDS_02_TWO", new TarotCardMeaning(
                List.of("lập kế hoạch dài hạn", "tầm nhìn tương lai", "quyền lực cá nhân"),
                List.of("do dự không quyết", "sợ bước ra khỏi vùng an toàn", "thiếu tầm nhìn"),
                Polarity.SUPPORT, Polarity.CAUTION,
                "Đang đứng trước một ngã rẽ sự nghiệp, cần hoạch định chiến lược dài hạn và dám mở rộng tầm nhìn ra ngoài phạm vi quen thuộc.",
                "Là thời điểm thích hợp để cân nhắc kế hoạch tài chính dài hạn, đầu tư cho tương lai thay vì chỉ nhìn vào lợi ích trước mắt.",
                "Mối quan hệ đang ở giai đoạn cùng nhau bàn bạc, định hướng cho tương lai chung.",
                "Hãy cân nhắc kỹ các lựa chọn và dám đưa ra quyết định táo bạo cho bước tiếp theo, thay vì chỉ đứng nhìn.",
                "Đứng giữa thành quả ban đầu và một chân trời rộng lớn hơn, đây là lúc hoạch định bước đi kế tiếp bằng sự tự tin và tầm nhìn xa."
        ));
        m.put("MINOR_WANDS_03_THREE", new TarotCardMeaning(
                List.of("mở rộng thành công", "tầm nhìn xa", "chờ đợi kết quả"),
                List.of("kế hoạch bị trì hoãn", "trở ngại bất ngờ", "thiếu kiên nhẫn"),
                Polarity.SUPPORT, Polarity.CAUTION,
                "Những nỗ lực và kế hoạch mở rộng trước đây bắt đầu cho thấy tín hiệu khả quan, cơ hội hợp tác hoặc thị trường mới đang mở ra.",
                "Các khoản đầu tư hoặc dự án tài chính đã triển khai đang trong giai đoạn chờ sinh lời, kết quả cụ thể sẽ sớm xuất hiện.",
                "Hai người đang cùng hướng về một tương lai chung, sẵn sàng mở rộng mối quan hệ ra ngoài giới hạn hiện tại.",
                "Đây là lúc kiên nhẫn theo dõi tiến triển của kế hoạch đã đặt ra, đồng thời chuẩn bị cho bước mở rộng tiếp theo.",
                "Thuyền đã ra khơi, giờ là lúc dõi theo chân trời và chuẩn bị đón nhận thành quả của tầm nhìn xa."
        ));
        m.put("MINOR_WANDS_04_FOUR", new TarotCardMeaning(
                List.of("ăn mừng thành quả", "ổn định vững chắc", "đoàn tụ sum vầy"),
                List.of("niềm vui chưa trọn vẹn", "nền tảng lung lay", "hoãn lễ ăn mừng"),
                Polarity.SUPPORT, Polarity.CAUTION,
                "Một cột mốc quan trọng trong công việc được hoàn thành, xứng đáng được công nhận và ăn mừng cùng đồng đội.",
                "Nền tảng tài chính đã đạt được sự ổn định nhất định sau một giai đoạn nỗ lực, mang lại cảm giác an tâm.",
                "Đây là biểu tượng của sự hòa hợp, đoàn tụ gia đình hoặc một dấu mốc gắn kết như đính hôn, về chung một mái nhà.",
                "Hãy dừng lại một nhịp để ghi nhận thành quả đã đạt được trước khi bước sang giai đoạn tiếp theo.",
                "Niềm vui, sự ổn định và cảm giác thuộc về đang hiện diện, đánh dấu một nền tảng vững chắc vừa được xây xong."
        ));
        m.put("MINOR_WANDS_05_FIVE", new TarotCardMeaning(
                List.of("cạnh tranh gay gắt", "bất đồng quan điểm", "xung đột nhỏ"),
                List.of("mâu thuẫn âm ỉ", "né tránh đối đầu cần thiết", "căng thẳng nội bộ kéo dài"),
                Polarity.CAUTION, Polarity.NEGATIVE,
                "Môi trường làm việc xuất hiện cạnh tranh hoặc bất đồng quan điểm giữa các bên, đòi hỏi kỹ năng dung hòa để tránh đổ vỡ.",
                "Có thể xảy ra tranh chấp hoặc bất đồng về tiền bạc với đối tác, cần làm rõ quyền lợi trước khi mâu thuẫn lan rộng.",
                "Những va chạm, tranh cãi nhỏ nhặt đang làm rạn nứt sự hòa hợp, cần đối thoại thẳng thắn thay vì để bụng.",
                "Đừng né tránh xung đột cần thiết, nhưng cũng cần chọn cách tranh luận xây dựng thay vì đối đầu vô ích.",
                "Va chạm và cạnh tranh là điều khó tránh khỏi lúc này, thử thách là biến xung đột thành động lực thay vì để nó gây tổn hại."
        ));
        m.put("MINOR_WANDS_06_SIX", new TarotCardMeaning(
                List.of("chiến thắng vẻ vang", "được công nhận", "thành công sau nỗ lực"),
                List.of("thành quả chưa được ghi nhận", "kiêu ngạo tự mãn", "chiến thắng bị trì hoãn"),
                Polarity.SUPPORT, Polarity.CAUTION,
                "Những nỗ lực bền bỉ được đền đáp bằng thành công rõ rệt và sự công nhận từ cấp trên hay đồng nghiệp.",
                "Một khoản đầu tư hoặc kế hoạch tài chính mang lại kết quả tốt, củng cố thêm sự tự tin về hướng đi đã chọn.",
                "Niềm tự hào chung, được người xung quanh nhìn nhận như một cặp đôi hay một gia đình hạnh phúc, thành công.",
                "Đây là thời điểm thuận lợi để tiến lên phía trước một cách tự tin, thành quả đang ủng hộ lựa chọn đã chọn.",
                "Vòng nguyệt quế của chiến thắng đang chờ đợi, ghi nhận xứng đáng cho những nỗ lực đã bỏ ra."
        ));
        m.put("MINOR_WANDS_07_SEVEN", new TarotCardMeaning(
                List.of("giữ vững lập trường", "kiên cường phòng thủ", "vượt qua thách thức"),
                List.of("đuối sức trước áp lực", "buông xuôi lập trường", "bị áp đảo bởi đối thủ"),
                Polarity.SUPPORT, Polarity.CAUTION,
                "Đang phải bảo vệ vị trí hoặc thành quả của mình trước sự cạnh tranh, nhưng lợi thế vẫn nghiêng về phía kiên trì đứng vững.",
                "Cần bảo vệ tài sản hoặc quan điểm tài chính của mình trước áp lực hoặc sự cạnh tranh từ bên ngoài.",
                "Phải kiên định bảo vệ quan điểm hoặc ranh giới cá nhân trong mối quan hệ trước áp lực từ người khác.",
                "Hãy giữ vững lập trường đã chọn, sự kiên trì lúc này nhiều khả năng sẽ mang lại lợi thế.",
                "Đứng ở thế cao hơn để phòng thủ, đòi hỏi bản lĩnh và sự kiên định để không bị lấn át."
        ));
        m.put("MINOR_WANDS_08_EIGHT", new TarotCardMeaning(
                List.of("hành động nhanh chóng", "tiến triển thần tốc", "tin tức dồn dập"),
                List.of("chậm trễ bất ngờ", "vội vàng hấp tấp", "mất phương hướng vì quá nhanh"),
                Polarity.SUPPORT, Polarity.CAUTION,
                "Mọi việc đang tăng tốc nhanh chóng, các dự án hoặc quyết định công việc di chuyển với tốc độ chưa từng có.",
                "Dòng tiền hoặc các giao dịch tài chính diễn ra nhanh chóng, cần nắm bắt kịp thời để không bỏ lỡ cơ hội.",
                "Mối quan hệ đang tiến triển với tốc độ nhanh, tin tức hoặc sự kiện quan trọng đến dồn dập.",
                "Đây là lúc hành động dứt khoát và nhanh nhạy, sự chậm trễ có thể khiến cơ hội trôi qua.",
                "Tốc độ và đà tiến đang ở mức cao nhất, mọi thứ chuyển động nhanh chóng hướng đến đích."
        ));
        m.put("MINOR_WANDS_09_NINE", new TarotCardMeaning(
                List.of("kiên cường bền bỉ", "cảnh giác phòng bị", "gắng thêm một chút nữa"),
                List.of("kiệt sức bỏ cuộc", "hoang mang nghi kỵ", "quá tải vì lo lắng"),
                Polarity.CAUTION, Polarity.NEGATIVE,
                "Dù đã trải qua nhiều thử thách và có phần mệt mỏi, chỉ cần thêm một nỗ lực cuối cùng là có thể chạm tới mục tiêu.",
                "Cần thận trọng bảo toàn những gì đã gây dựng được sau một giai đoạn khó khăn về tài chính, đừng mạo hiểm thêm.",
                "Còn mang tâm lý phòng thủ hoặc dè chừng do những tổn thương trước đó, cần thời gian để mở lòng trở lại.",
                "Hãy kiên trì thêm một chặng ngắn nữa, dù mệt mỏi nhưng đích đến đã gần kề.",
                "Vết thương cũ khiến trở nên cảnh giác, nhưng sức bền và ý chí vẫn còn đủ để hoàn thành chặng đường."
        ));
        m.put("MINOR_WANDS_10_TEN", new TarotCardMeaning(
                List.of("gánh nặng trách nhiệm", "quá tải công việc", "ôm đồm quá sức"),
                List.of("buông bỏ bớt gánh nặng", "kiệt sức vì trách nhiệm", "học cách san sẻ"),
                Polarity.NEGATIVE, Polarity.CAUTION,
                "Khối lượng công việc và trách nhiệm đang chồng chất quá mức, dẫn đến nguy cơ kiệt sức nếu không biết san sẻ.",
                "Gánh nặng nợ nần hoặc trách nhiệm tài chính đang trở nên nặng nề, cần xem xét lại để giảm tải.",
                "Đang gánh vác quá nhiều trách nhiệm trong mối quan hệ hoặc gia đình đến mức cảm thấy ngộp thở.",
                "Đã đến lúc cân nhắc san sẻ bớt gánh nặng hoặc từ bỏ những gì không thực sự cần thiết phải một mình gánh vác.",
                "Đích đến đã gần nhưng gánh nặng trên vai ngày càng nặng, cảnh báo về nguy cơ kiệt sức nếu cố ôm đồm mọi thứ."
        ));
        m.put("MINOR_WANDS_11_PAGE", new TarotCardMeaning(
                List.of("tin tức đầy hứng khởi", "tinh thần học hỏi", "khởi đầu nhiệt huyết"),
                List.of("tin tức trì hoãn", "thiếu kiên định", "hành động hấp tấp non nớt"),
                Polarity.SUPPORT, Polarity.CAUTION,
                "Xuất hiện một cơ hội học hỏi mới hoặc tin tức tích cực liên quan đến công việc, phù hợp với người mới bắt đầu con đường sự nghiệp.",
                "Một ý tưởng kiếm tiền mới mẻ hoặc cơ hội tài chính đang chớm nở nhưng cần thời gian để phát triển thêm.",
                "Sự nhiệt tình, hồn nhiên của giai đoạn tìm hiểu ban đầu mang đến cảm giác tươi mới trong tình cảm.",
                "Hãy giữ tinh thần cởi mở học hỏi, nhưng đừng vội hành động khi kiến thức và kinh nghiệm chưa đủ vững.",
                "Ngọn lửa nhiệt huyết của người mới bắt đầu, tràn đầy tò mò và sẵn sàng khám phá điều mới mẻ."
        ));
        m.put("MINOR_WANDS_12_KNIGHT", new TarotCardMeaning(
                List.of("hành động táo bạo", "nhiệt huyết phiêu lưu", "nóng vội xông pha"),
                List.of("hấp tấp thiếu suy nghĩ", "dự án dở dang", "kiêu ngạo bốc đồng"),
                Polarity.SUPPORT, Polarity.CAUTION,
                "Tinh thần dấn thân, dám nghĩ dám làm thúc đẩy tiến độ công việc, nhưng cần tránh vội vàng bỏ qua chi tiết quan trọng.",
                "Có xu hướng đưa ra quyết định tài chính táo bạo hoặc mạo hiểm, cần cân nhắc để không hành động theo cảm hứng nhất thời.",
                "Sự theo đuổi nồng nhiệt, đầy đam mê nhưng có thể thiếu kiên nhẫn để duy trì lâu dài.",
                "Hành động nhanh và quyết đoán sẽ mang lại lợi thế, miễn là không bỏ qua bước chuẩn bị cần thiết.",
                "Năng lượng phiêu lưu và nhiệt huyết xông pha, luôn sẵn sàng lên đường theo đuổi mục tiêu mới."
        ));
        m.put("MINOR_WANDS_13_QUEEN", new TarotCardMeaning(
                List.of("tự tin lôi cuốn", "độc lập quyết đoán", "ấm áp truyền cảm hứng"),
                List.of("ghen tị bất an", "áp đặt kiểm soát", "thiếu tự tin che giấu"),
                Polarity.SUPPORT, Polarity.CAUTION,
                "Thể hiện năng lực lãnh đạo tự tin, khả năng truyền cảm hứng và thu hút sự ủng hộ từ những người xung quanh.",
                "Quản lý tài chính với sự tự tin và độc lập, biết cách tạo dựng nguồn thu nhập từ chính năng lực bản thân.",
                "Mang đến sự ấm áp, quyến rũ và chân thành, đồng thời vẫn giữ vững sự độc lập trong tình cảm.",
                "Hãy tin vào bản năng và sự tự tin của bản thân để đưa ra lựa chọn, không cần phụ thuộc vào ý kiến người khác.",
                "Hiện thân của sự tự tin rực rỡ, độc lập và khả năng truyền cảm hứng cho những người xung quanh."
        ));
        m.put("MINOR_WANDS_14_KING", new TarotCardMeaning(
                List.of("tầm nhìn lãnh đạo", "tinh thần khởi nghiệp", "uy quyền tự nhiên"),
                List.of("độc đoán nóng nảy", "áp đặt cực đoan", "tham vọng thiếu kiểm soát"),
                Polarity.SUPPORT, Polarity.CAUTION,
                "Thể hiện tầm nhìn chiến lược và khả năng lãnh đạo bẩm sinh, phù hợp để khởi xướng dự án lớn hoặc dẫn dắt đội nhóm.",
                "Có tầm nhìn dài hạn và bản lĩnh để đưa ra những quyết định tài chính táo bạo nhưng có tính toán.",
                "Mang phong thái người bạn đời đầy nhiệt huyết, quyết đoán nhưng cần tránh áp đặt ý muốn lên đối phương.",
                "Hãy quyết đoán dẫn dắt tình huống theo tầm nhìn dài hạn, thay vì để cảm xúc nhất thời chi phối.",
                "Hiện thân trọn vẹn của ngọn lửa Wands, một nhà lãnh đạo giàu tầm nhìn, quyết đoán và truyền cảm hứng."
        ));
        return m;
    }
}
