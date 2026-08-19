package io.destinyos.engines.tarot;

import io.destinyos.core.signal.Polarity;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Cups (Water) suit Vietnamese interpretive content (research item R11).
 * Grounded in standard Rider-Waite-Smith tradition.
 */
final class TarotCardMeaningsCups {

    private TarotCardMeaningsCups() {
    }

    static Map<String, TarotCardMeaning> entries() {
        Map<String, TarotCardMeaning> m = new LinkedHashMap<>();
        m.put("MINOR_CUPS_01_ACE", new TarotCardMeaning(
                List.of("khởi đầu cảm xúc mới", "tình yêu chớm nở", "trái tim rộng mở"),
                List.of("cảm xúc bị kìm nén", "cơ hội tình cảm bị bỏ lỡ", "trống rỗng nội tâm"),
                Polarity.SUPPORT, Polarity.CAUTION,
                "Một khởi đầu tốt đẹp trong các mối quan hệ nơi công sở, tinh thần cởi mở được ghi nhận.",
                "Cảm giác hài lòng nhẹ nhàng về tài chính, dù đây không phải trọng tâm của lá bài này.",
                "Tình yêu mới chớm nở, trái tim rộng mở đón nhận cảm xúc chân thành.",
                "Nên lắng nghe trực giác và cảm xúc của bản thân khi đưa ra quyết định lúc này.",
                "Sự khởi đầu của cảm xúc dạt dào, tình yêu và lòng trắc ẩn."
        ));
        m.put("MINOR_CUPS_02_TWO", new TarotCardMeaning(
                List.of("kết nối tương hỗ", "quan hệ đối tác hài hòa", "hòa hợp cảm xúc", "sự thu hút lẫn nhau"),
                List.of("mất cân bằng trong quan hệ", "hiểu lầm tình cảm", "chia lìa", "quan hệ một chiều"),
                Polarity.SUPPORT, Polarity.CAUTION,
                "Một sự hợp tác đôi bên cùng có lợi đang hình thành, đồng nghiệp hoặc đối tác thể hiện thiện chí hiếm có.",
                "Việc hùn vốn hay chia sẻ tài chính với một người đáng tin cậy có thể mang lại kết quả tích cực.",
                "Hai người tìm thấy sự đồng điệu sâu sắc, tình cảm được đáp lại và mối liên kết trở nên bền chặt.",
                "Nên cân nhắc quyết định cùng với một người đồng hành thay vì tự mình gánh vác một mình.",
                "Sự cân bằng và hòa hợp giữa hai cá nhân, biểu tượng của kết nối và tôn trọng lẫn nhau."
        ));
        m.put("MINOR_CUPS_03_THREE", new TarotCardMeaning(
                List.of("ăn mừng cùng bạn bè", "tình bạn gắn kết", "niềm vui chung", "cộng đồng hỗ trợ"),
                List.of("mâu thuẫn trong nhóm", "buôn chuyện thị phi", "vui chơi quá đà", "cảm giác bị cô lập"),
                Polarity.SUPPORT, Polarity.CAUTION,
                "Thành quả công việc được ghi nhận trong không khí đồng đội vui vẻ, sự hợp tác nhóm mang lại hiệu quả rõ rệt.",
                "Một khoản chi tiêu cho tiệc tùng hoặc dịp kỷ niệm chung là hợp lý, dù không phải thời điểm để đầu tư lớn.",
                "Bạn bè và các mối quan hệ xã giao mang lại niềm vui, tình cảm gắn kết được củng cố qua những dịp sum họp.",
                "Đây là thời điểm phù hợp để tham khảo ý kiến bạn bè thân thiết trước khi quyết định.",
                "Niềm vui được chia sẻ, tình bạn và sự gắn kết cộng đồng là nguồn năng lượng tích cực lúc này."
        ));
        m.put("MINOR_CUPS_04_FOUR", new TarotCardMeaning(
                List.of("thờ ơ với cơ hội", "chán nản dù đủ đầy", "thu mình suy tư", "bỏ lỡ vì thiếu quan tâm"),
                List.of("bắt đầu tỉnh giấc", "nhận ra cơ hội mới", "vẫn còn do dự", "thoát khỏi trì trệ"),
                Polarity.CAUTION, Polarity.NEUTRAL,
                "Sự thiếu hứng thú với công việc hiện tại khiến một cơ hội đáng giá có nguy cơ bị bỏ qua.",
                "Không có động lực xem xét lại tình hình tài chính, dễ dẫn đến trì trệ dù không thiếu thốn.",
                "Cảm giác chán chường khiến người trong cuộc thờ ơ với những cử chỉ quan tâm từ người khác.",
                "Cần thoát khỏi trạng thái thu mình để nhận ra rằng vẫn còn lựa chọn tốt đang chờ được để ý.",
                "Sự bất mãn âm thầm dù đã có đủ, tâm trí đang khép lại trước những gì đang được trao tặng."
        ));
        m.put("MINOR_CUPS_05_FIVE", new TarotCardMeaning(
                List.of("mất mát và tiếc nuối", "tập trung vào điều đã mất", "đau buồn", "bỏ quên điều còn lại"),
                List.of("chấp nhận và buông bỏ", "bắt đầu hàn gắn", "nhìn lại điều còn giữ được", "tha thứ cho bản thân"),
                Polarity.NEGATIVE, Polarity.CAUTION,
                "Một thất bại hoặc cơ hội vuột mất khiến tinh thần sa sút, nhưng vẫn còn những nguồn lực chưa được nhìn nhận.",
                "Một khoản thua lỗ hoặc quyết định tài chính sai lầm trong quá khứ vẫn còn ám ảnh hiện tại.",
                "Nỗi buồn vì chia ly hoặc tổn thương tình cảm đang che khuất những gì tốt đẹp vẫn còn hiện diện.",
                "Trước khi quyết định tiếp theo, cần quay lại nhìn nhận những gì còn lại thay vì chỉ tiếc nuối điều đã mất.",
                "Đau buồn và tiếc nuối chiếm ưu thế, nhưng hy vọng vẫn còn nếu biết quay đầu nhìn lại."
        ));
        m.put("MINOR_CUPS_06_SIX", new TarotCardMeaning(
                List.of("hoài niệm ấm áp", "hội ngộ cũ", "sự ngây thơ chân thành", "cho và nhận từ quá khứ"),
                List.of("mắc kẹt trong quá khứ", "lý tưởng hóa ký ức", "khó trưởng thành", "trốn tránh hiện tại"),
                Polarity.SUPPORT, Polarity.CAUTION,
                "Một mối quan hệ nghề nghiệp cũ hoặc kinh nghiệm trong quá khứ quay lại hỗ trợ cho hiện tại.",
                "Một khoản hỗ trợ, quà tặng hoặc thu nhập liên quan đến điều gì đó từ trước có thể xuất hiện.",
                "Kỷ niệm đẹp hoặc một người quen cũ quay lại mang theo sự ấm áp và chân thành.",
                "Có thể tham khảo kinh nghiệm đã tích lũy trong quá khứ để đưa ra lựa chọn phù hợp hôm nay.",
                "Sự ngọt ngào của hoài niệm và lòng tốt giản dị, kết nối giữa quá khứ và hiện tại."
        ));
        m.put("MINOR_CUPS_07_SEVEN", new TarotCardMeaning(
                List.of("quá nhiều lựa chọn", "ảo tưởng hấp dẫn", "mơ mộng thiếu thực tế", "khó phân biệt thật giả"),
                List.of("nhận ra sự thật", "chọn lọc rõ ràng hơn", "thoát khỏi ảo tưởng", "tập trung vào một mục tiêu"),
                Polarity.CAUTION, Polarity.SUPPORT,
                "Nhiều hướng đi nghề nghiệp hấp dẫn cùng lúc xuất hiện, nhưng không phải lựa chọn nào cũng thực tế.",
                "Các cơ hội đầu tư nghe có vẻ hứa hẹn cần được xem xét kỹ, tránh bị cuốn theo ảo tưởng lợi nhuận.",
                "Kỳ vọng lãng mạn thiếu thực tế có thể khiến người trong cuộc khó nhìn rõ bản chất mối quan hệ.",
                "Trước khi quyết định, cần phân biệt rạch ròi giữa mong muốn viển vông và điều thực sự khả thi.",
                "Tâm trí bị bủa vây bởi nhiều khả năng hấp dẫn, cần tỉnh táo để không lạc trong ảo tưởng."
        ));
        m.put("MINOR_CUPS_08_EIGHT", new TarotCardMeaning(
                List.of("rời bỏ để tìm ý nghĩa", "buông điều chưa trọn vẹn", "hành trình nội tâm", "tìm kiếm điều sâu sắc hơn"),
                List.of("sợ thay đổi", "quay lại điều đã bỏ", "loanh quanh không dứt khoát", "trì hoãn hành trình"),
                Polarity.CAUTION, Polarity.NEGATIVE,
                "Một sự nghiệp tưởng như ổn định được từ bỏ để theo đuổi điều có ý nghĩa hơn với bản thân.",
                "Sẵn sàng rời xa một nguồn thu nhập quen thuộc để tìm kiếm giá trị lâu dài hơn là lợi ích trước mắt.",
                "Một mối quan hệ dù không tệ nhưng thiếu chiều sâu khiến người trong cuộc chọn bước tiếp một mình.",
                "Đây là lúc cần can đảm rời khỏi vùng an toàn để tìm kiếm điều thực sự phù hợp với giá trị bản thân.",
                "Hành trình rời xa sự thỏa mãn bề mặt để đi tìm ý nghĩa sâu sắc hơn cho tâm hồn."
        ));
        m.put("MINOR_CUPS_09_NINE", new TarotCardMeaning(
                List.of("mãn nguyện", "điều ước thành hiện thực", "hài lòng với bản thân", "sung túc về cảm xúc"),
                List.of("thỏa mãn hời hợt", "tự mãn thái quá", "hưởng thụ quá đà", "hài lòng bề ngoài"),
                Polarity.SUPPORT, Polarity.CAUTION,
                "Công sức bỏ ra được đền đáp xứng đáng, mang lại cảm giác hài lòng thực sự với thành quả đạt được.",
                "Tình hình tài chính ổn định và sung túc, đủ để tận hưởng thành quả đã tích lũy.",
                "Cảm giác được yêu thương và trân trọng trọn vẹn, hạnh phúc hiện diện rõ ràng trong mối quan hệ.",
                "Đây là thời điểm thuận lợi để tận hưởng thành quả, nhưng cần tránh chủ quan hay tự mãn.",
                "Điều ước được toại nguyện, sự mãn nguyện lan tỏa từ bên trong ra bên ngoài."
        ));
        m.put("MINOR_CUPS_10_TEN", new TarotCardMeaning(
                List.of("hạnh phúc gia đình", "hòa hợp lâu dài", "viên mãn cảm xúc", "cuộc sống an vui"),
                List.of("bất hòa gia đình", "kỳ vọng không được đáp ứng", "rạn nứt hòa khí", "hạnh phúc bề ngoài"),
                Polarity.SUPPORT, Polarity.CAUTION,
                "Sự cân bằng giữa công việc và cuộc sống gia đình mang lại cảm giác trọn vẹn hiếm có.",
                "Nền tảng tài chính đủ vững để nuôi dưỡng một cuộc sống gia đình ổn định và an tâm.",
                "Hạnh phúc gia đình và sự hòa hợp bền vững là kết quả của những nỗ lực vun đắp lâu dài.",
                "Nên ưu tiên những lựa chọn củng cố sự gắn kết gia đình hơn là lợi ích cá nhân trước mắt.",
                "Sự viên mãn trọn vẹn về mặt cảm xúc, hạnh phúc bền lâu được xây dựng cùng những người thân yêu."
        ));
        m.put("MINOR_CUPS_11_PAGE", new TarotCardMeaning(
                List.of("tin vui tình cảm", "tâm hồn mơ mộng", "khởi đầu sáng tạo", "sự tò mò chân thành"),
                List.of("cảm xúc bất ổn", "thiếu chín chắn", "trốn tránh thực tại", "sáng tạo bị chặn lại"),
                Polarity.SUPPORT, Polarity.CAUTION,
                "Một ý tưởng sáng tạo mới hoặc tin tức tích cực liên quan đến công việc có thể xuất hiện bất ngờ.",
                "Một cơ hội tài chính nhỏ nhưng thú vị có thể đến, cần giữ thái độ cởi mở nhưng không vội vàng.",
                "Một lời tỏ tình chân thành hoặc tình cảm mới chớm nở mang màu sắc ngây thơ, trong sáng.",
                "Nên lắng nghe trực giác và sự tò mò của bản thân, nhưng đừng quên cân nhắc bằng lý trí.",
                "Một thông điệp cảm xúc nhẹ nhàng, tâm hồn cởi mở đón nhận những điều mới mẻ."
        ));
        m.put("MINOR_CUPS_12_KNIGHT", new TarotCardMeaning(
                List.of("theo đuổi lý tưởng tình cảm", "sự lãng mạn chân thành", "lời đề nghị chân tình", "người đưa tin của trái tim"),
                List.of("thất thường trong tình cảm", "hứa hẹn viển vông", "ghen tuông vô cớ", "thiếu đáng tin cậy"),
                Polarity.SUPPORT, Polarity.NEGATIVE,
                "Một lời mời hợp tác hoặc cơ hội mới được đưa ra với thái độ chân thành và đầy cảm hứng.",
                "Một đề nghị tài chính hấp dẫn xuất hiện, song cần xác minh tính khả thi trước khi tin tưởng hoàn toàn.",
                "Một người theo đuổi tình cảm bằng sự lãng mạn và chân thành, mang đến cảm giác được trân trọng.",
                "Nên để trái tim dẫn lối nhưng vẫn giữ một phần tỉnh táo trước những lời hứa hẹn quá đẹp.",
                "Tinh thần lý tưởng hóa và lãng mạn, một lời đề nghị hay thông điệp cảm xúc đang trên đường đến."
        ));
        m.put("MINOR_CUPS_13_QUEEN", new TarotCardMeaning(
                List.of("thấu cảm sâu sắc", "trực giác tinh tế", "chở che dịu dàng", "trưởng thành về cảm xúc"),
                List.of("phụ thuộc cảm xúc", "tự hy sinh quá mức", "bất an nội tâm", "ranh giới mơ hồ"),
                Polarity.SUPPORT, Polarity.CAUTION,
                "Khả năng thấu hiểu và lắng nghe người khác giúp xây dựng môi trường làm việc hài hòa, đầy tin cậy.",
                "Trực giác nhạy bén giúp đưa ra quyết định tài chính phù hợp với cảm nhận thực sự của bản thân.",
                "Sự chăm sóc dịu dàng và thấu cảm sâu sắc nuôi dưỡng một mối quan hệ bền chặt, an toàn.",
                "Nên tin vào trực giác và cảm nhận tinh tế của bản thân khi cân nhắc lựa chọn.",
                "Sự trưởng thành về cảm xúc và lòng trắc ẩn sâu sắc, một tấm gương của sự thấu hiểu."
        ));
        m.put("MINOR_CUPS_14_KING", new TarotCardMeaning(
                List.of("làm chủ cảm xúc", "điềm tĩnh khôn ngoan", "cố vấn đáng tin cậy", "cân bằng lý trí và trái tim"),
                List.of("thao túng cảm xúc", "lạnh lùng che giấu", "tâm trạng thất thường", "kìm nén cảm xúc thật"),
                Polarity.SUPPORT, Polarity.CAUTION,
                "Sự điềm tĩnh và khả năng dung hòa cảm xúc giúp đưa ra những quyết định lãnh đạo sáng suốt.",
                "Cách tiếp cận tài chính điềm đạm, biết cân bằng giữa cảm tính và lý trí mang lại sự ổn định.",
                "Sự bao dung và chín chắn trong cách thể hiện tình cảm tạo nên nền tảng vững chắc cho mối quan hệ.",
                "Nên giữ thái độ điềm tĩnh, cân bằng giữa lý trí và cảm xúc trước khi đưa ra quyết định quan trọng.",
                "Sự làm chủ cảm xúc một cách khôn ngoan, trở thành chỗ dựa vững vàng cho những người xung quanh."
        ));
        return m;
    }
}
