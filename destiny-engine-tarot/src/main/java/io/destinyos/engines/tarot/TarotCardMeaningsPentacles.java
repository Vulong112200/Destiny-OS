package io.destinyos.engines.tarot;

import io.destinyos.core.signal.Polarity;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Pentacles (Earth) suit Vietnamese interpretive content (research item R11).
 * Grounded in standard Rider-Waite-Smith tradition.
 */
final class TarotCardMeaningsPentacles {

    private TarotCardMeaningsPentacles() {
    }

    static Map<String, TarotCardMeaning> entries() {
        Map<String, TarotCardMeaning> m = new LinkedHashMap<>();
        m.put("MINOR_PENTACLES_01_ACE", new TarotCardMeaning(
                List.of("cơ hội tài chính mới", "nền tảng vững chắc", "khởi đầu thịnh vượng"),
                List.of("cơ hội bị bỏ lỡ", "kế hoạch thiếu chắc chắn", "trì trệ tài chính"),
                Polarity.SUPPORT, Polarity.CAUTION,
                "Một cơ hội nghề nghiệp mới, thiết thực và có tiềm năng phát triển lâu dài xuất hiện.",
                "Khởi đầu tài chính đầy hứa hẹn, nền tảng vững chắc để xây dựng sự thịnh vượng.",
                "Sự ổn định và an toàn bắt đầu được thiết lập trong mối quan hệ.",
                "Nên tận dụng cơ hội thực tế này, đặt nền móng vững chắc trước khi mở rộng.",
                "Hạt giống của sự thịnh vượng vật chất và cơ hội thiết thực."
        ));
        m.put("MINOR_PENTACLES_02_TWO", new TarotCardMeaning(
                List.of("cân bằng nhiều việc", "linh hoạt thích nghi", "quản lý ưu tiên"),
                List.of("mất cân bằng", "quá tải tài chính", "thiếu tổ chức"),
                Polarity.CAUTION, Polarity.NEGATIVE,
                "Công việc đòi hỏi phải xoay xở cùng lúc nhiều nhiệm vụ khác nhau, cần sự linh hoạt và khả năng sắp xếp ưu tiên khéo léo.",
                "Tài chính cần được cân đối khéo léo giữa nhiều khoản chi tiêu và nghĩa vụ, đòi hỏi sự linh hoạt thay vì cứng nhắc.",
                "Mối quan hệ đang phải dung hòa giữa nhiều trách nhiệm khác nhau, cần sự thích nghi để giữ hòa khí.",
                "Nên giữ thái độ linh hoạt, sẵn sàng điều chỉnh kế hoạch khi hoàn cảnh thay đổi thay vì bám cứng một phương án.",
                "Cuộc sống đang đòi hỏi khả năng giữ thăng bằng và thích nghi linh hoạt giữa nhiều ưu tiên cùng lúc."
        ));
        m.put("MINOR_PENTACLES_03_THREE", new TarotCardMeaning(
                List.of("hợp tác chuyên môn", "kỹ năng được công nhận", "làm việc nhóm hiệu quả"),
                List.of("thiếu phối hợp", "công việc thiếu chỉn chu", "mâu thuẫn nhóm"),
                Polarity.SUPPORT, Polarity.CAUTION,
                "Năng lực chuyên môn được đồng nghiệp hoặc cấp trên công nhận thông qua một dự án hợp tác đòi hỏi tay nghề vững.",
                "Thu nhập được cải thiện nhờ sự phối hợp hiệu quả trong công việc mang tính kỹ thuật hoặc chuyên môn.",
                "Mối quan hệ phát triển tốt khi cả hai bên cùng đóng góp và tôn trọng vai trò, thế mạnh của nhau.",
                "Nên tìm kiếm sự hợp tác với người có chuyên môn phù hợp thay vì cố gắng tự mình hoàn thành mọi việc.",
                "Thành quả đến từ sự phối hợp nhịp nhàng giữa các cá nhân có tay nghề và mục tiêu chung."
        ));
        m.put("MINOR_PENTACLES_04_FOUR", new TarotCardMeaning(
                List.of("nắm giữ chặt tài sản", "phòng thủ tài chính", "kiểm soát chi tiêu"),
                List.of("buông bỏ kiểm soát", "chi tiêu mất kiểm soát", "keo kiệt thái quá"),
                Polarity.CAUTION, Polarity.NEGATIVE,
                "Xu hướng bám giữ vị trí hoặc cách làm quen thuộc vì sợ mất an toàn, có thể cản trở cơ hội phát triển.",
                "Tài sản được giữ chặt và tích lũy cẩn trọng, nhưng sự kiểm soát quá mức có thể biến thành keo kiệt hoặc sợ chia sẻ.",
                "Nỗi sợ mất mát khiến một người trở nên khép kín hoặc kiểm soát trong mối quan hệ, khó chia sẻ cảm xúc lẫn vật chất.",
                "Nên xem lại liệu sự thận trọng hiện tại có đang biến thành cố chấp giữ khư khư, cản trở những thay đổi cần thiết.",
                "An toàn vật chất được ưu tiên đến mức có thể tạo ra sự cứng nhắc và khép kín không cần thiết."
        ));
        m.put("MINOR_PENTACLES_05_FIVE", new TarotCardMeaning(
                List.of("khó khăn tài chính", "cảm giác bị bỏ rơi", "lo lắng vật chất"),
                List.of("hồi phục sau khó khăn", "tìm được sự giúp đỡ", "vượt qua giai đoạn khó"),
                Polarity.NEGATIVE, Polarity.SUPPORT,
                "Giai đoạn công việc bấp bênh hoặc mất mát khiến cảm giác an toàn nghề nghiệp bị lung lay.",
                "Khó khăn tài chính hoặc thiếu thốn xuất hiện, kèm theo cảm giác cô lập, nhưng sự hỗ trợ thường ở gần hơn tưởng.",
                "Cảm giác bị bỏ rơi hoặc cô đơn trong khó khăn có thể xuất hiện, cần chủ động tìm kiếm sự nương tựa từ người thân.",
                "Đừng ngần ngại tìm kiếm sự giúp đỡ bên ngoài thay vì cố gắng một mình chịu đựng khó khăn.",
                "Một giai đoạn thiếu thốn hoặc bị gạt ra ngoài lề, nhưng sự trợ giúp thường không xa như cảm giác ban đầu."
        ));
        m.put("MINOR_PENTACLES_06_SIX", new TarotCardMeaning(
                List.of("hào phóng chia sẻ", "cho và nhận cân bằng", "hỗ trợ vật chất"),
                List.of("cho nhận không công bằng", "ràng buộc có điều kiện", "phụ thuộc tài chính"),
                Polarity.SUPPORT, Polarity.CAUTION,
                "Sự hỗ trợ hoặc cố vấn từ người có kinh nghiệm hơn giúp công việc tiến triển thuận lợi.",
                "Nguồn lực được chia sẻ công bằng, có thể là nhận được hỗ trợ tài chính hoặc chủ động giúp đỡ người khác.",
                "Sự cho đi và nhận lại trong mối quan hệ diễn ra hài hòa, tạo cảm giác được quan tâm và trân trọng.",
                "Nên xem xét sự cân bằng giữa cho và nhận trước khi chấp nhận hoặc đề nghị một sự giúp đỡ.",
                "Lòng hào phóng và sự sẻ chia tạo ra dòng chảy cân bằng giữa các bên liên quan."
        ));
        m.put("MINOR_PENTACLES_07_SEVEN", new TarotCardMeaning(
                List.of("đánh giá thành quả", "kiên nhẫn chờ đợi", "nhìn lại công sức"),
                List.of("thiếu kiên nhẫn", "đầu tư sai hướng", "nóng vội bỏ cuộc"),
                Polarity.NEUTRAL, Polarity.CAUTION,
                "Đây là lúc dừng lại đánh giá công sức đã bỏ ra và cân nhắc liệu hướng đi hiện tại có còn xứng đáng để tiếp tục.",
                "Khoản đầu tư hoặc công sức tích lũy cần thêm thời gian mới cho thấy kết quả rõ ràng, đòi hỏi sự kiên nhẫn.",
                "Mối quan hệ đang ở giai đoạn nhìn lại những gì đã vun đắp để quyết định có nên tiếp tục đầu tư công sức hay không.",
                "Nên kiên nhẫn quan sát thêm trước khi vội vàng thay đổi hướng đi, vì thành quả có thể cần thêm thời gian.",
                "Một khoảng dừng để đánh giá thành quả đã gieo trồng, chuẩn bị cho quyết định tiếp theo."
        ));
        m.put("MINOR_PENTACLES_08_EIGHT", new TarotCardMeaning(
                List.of("chăm chỉ rèn nghề", "tập trung kỹ năng", "học việc cần mẫn"),
                List.of("thiếu tập trung", "làm việc hời hợt", "kỹ năng chưa hoàn thiện"),
                Polarity.SUPPORT, Polarity.CAUTION,
                "Sự chăm chỉ, tỉ mỉ rèn luyện kỹ năng chuyên môn đang dần đưa đến trình độ tay nghề vững vàng hơn.",
                "Thu nhập được cải thiện dần thông qua việc trau dồi kỹ năng và làm việc có kỷ luật, không phải may mắn nhất thời.",
                "Sự đầu tư công sức và kiên trì vun đắp từng ngày giúp mối quan hệ trở nên bền chặt hơn.",
                "Nên chọn con đường rèn luyện bền bỉ, tập trung nâng cao năng lực thay vì tìm lối tắt.",
                "Sự cần mẫn và tập trung rèn giũa kỹ năng là nền tảng cho thành quả lâu dài."
        ));
        m.put("MINOR_PENTACLES_09_NINE", new TarotCardMeaning(
                List.of("tự chủ độc lập", "hưởng thành quả lao động", "sung túc tinh tế"),
                List.of("làm việc quá sức", "thành công hời hợt", "bất ổn tài chính"),
                Polarity.SUPPORT, Polarity.CAUTION,
                "Thành quả từ nỗ lực cá nhân bền bỉ mang lại vị thế vững vàng và sự tự chủ trong công việc.",
                "Sự sung túc đạt được là kết quả của nỗ lực độc lập, cho phép tận hưởng thành quả mà không phụ thuộc vào ai.",
                "Sự tự tin và độc lập cá nhân tạo nền tảng vững chắc để bước vào hoặc duy trì một mối quan hệ lành mạnh.",
                "Nên tin tưởng vào năng lực và thành quả tự thân đã gây dựng để đưa ra quyết định độc lập, tự tin.",
                "Sự sung túc tinh tế đến từ nỗ lực tự thân, mang lại cảm giác tự chủ và hài lòng."
        ));
        m.put("MINOR_PENTACLES_10_TEN", new TarotCardMeaning(
                List.of("thịnh vượng lâu dài", "di sản gia đình", "ổn định bền vững"),
                List.of("bất hòa tài chính gia đình", "di sản bị đe dọa", "bất ổn dài hạn"),
                Polarity.SUPPORT, Polarity.CAUTION,
                "Sự nghiệp hướng tới xây dựng nền tảng bền vững, có thể truyền lại hoặc tạo ảnh hưởng lâu dài.",
                "Sự thịnh vượng mang tính dài hạn và ổn định, thường gắn liền với tài sản gia đình hoặc di sản được tích lũy qua nhiều thế hệ.",
                "Mối quan hệ hướng tới sự gắn kết lâu dài, ổn định, có tính chất gia đình và bền vững qua thời gian.",
                "Nên cân nhắc những quyết định có ảnh hưởng lâu dài đến sự ổn định của gia đình hoặc tập thể.",
                "Sự thịnh vượng bền vững và di sản được vun đắp qua thời gian, mang tính chất lâu dài hơn nhất thời."
        ));
        m.put("MINOR_PENTACLES_11_PAGE", new TarotCardMeaning(
                List.of("học hỏi thực tế", "cơ hội mới chín muồi", "chăm chỉ khởi đầu"),
                List.of("trì hoãn kế hoạch", "thiếu thực tế", "cam kết nửa vời"),
                Polarity.SUPPORT, Polarity.CAUTION,
                "Một cơ hội học hỏi hoặc khởi đầu công việc mới mang tính thực tế xuất hiện, đòi hỏi tinh thần cầu tiến.",
                "Tín hiệu tích cực về một kế hoạch tài chính hoặc dự án mới đang trong giai đoạn khởi động cần vun đắp.",
                "Một sự quan tâm chân thành, thực tế đang được thể hiện, có thể là khởi đầu của một sự gắn kết mới.",
                "Nên nghiêm túc học hỏi và chuẩn bị nền tảng vững chắc trước khi bước vào cơ hội mới này.",
                "Một cơ hội thực tế đang chín muồi, đòi hỏi tinh thần ham học hỏi và sự chăm chỉ để hiện thực hóa."
        ));
        m.put("MINOR_PENTACLES_12_KNIGHT", new TarotCardMeaning(
                List.of("kiên trì bền bỉ", "làm việc có phương pháp", "đáng tin cậy"),
                List.of("trì trệ lối mòn", "bảo thủ quá mức", "thiếu động lực"),
                Polarity.SUPPORT, Polarity.CAUTION,
                "Phong cách làm việc có phương pháp, kiên trì và đáng tin cậy giúp hoàn thành mục tiêu dù chậm nhưng chắc chắn.",
                "Sự ổn định tài chính được xây dựng từng bước thông qua kỷ luật và cách tiếp cận thận trọng, không vội vàng.",
                "Sự tận tâm bền bỉ và đáng tin cậy được thể hiện qua từng hành động cụ thể hơn là lời nói.",
                "Nên chọn cách tiếp cận chậm mà chắc, ưu tiên sự ổn định lâu dài hơn là tốc độ.",
                "Sự bền bỉ, có phương pháp và đáng tin cậy là chìa khóa để đạt được mục tiêu một cách chắc chắn."
        ));
        m.put("MINOR_PENTACLES_13_QUEEN", new TarotCardMeaning(
                List.of("chăm sóc thiết thực", "khéo léo vun vén", "nguồn lực dồi dào"),
                List.of("xao nhãng bản thân", "bất an tài chính", "quá tải chăm sóc"),
                Polarity.SUPPORT, Polarity.CAUTION,
                "Khả năng quản lý thực tế và chăm chút chi tiết giúp công việc vận hành ổn định và hiệu quả.",
                "Nguồn lực được quản lý khéo léo, cân bằng giữa tiết kiệm và tận hưởng, tạo cảm giác an toàn vật chất.",
                "Sự quan tâm ấm áp, thiết thực và chu đáo được thể hiện qua hành động chăm sóc cụ thể hằng ngày.",
                "Nên lắng nghe trực giác thực tế và ưu tiên nhu cầu thiết yếu của bản thân lẫn người thân.",
                "Sự nuôi dưỡng thiết thực và khéo léo cân bằng giữa vật chất và cảm xúc là điểm tựa vững chắc."
        ));
        m.put("MINOR_PENTACLES_14_KING", new TarotCardMeaning(
                List.of("làm chủ vật chất", "thành công bền vững", "hào phóng đáng tin"),
                List.of("thực dụng thái quá", "cứng nhắc bảo thủ", "phán đoán tài chính sai lầm"),
                Polarity.SUPPORT, Polarity.CAUTION,
                "Vị thế vững chắc trong sự nghiệp được xây dựng qua kinh nghiệm thực tế và khả năng quản lý xuất sắc.",
                "Sự làm chủ tài chính vững vàng, thể hiện qua khả năng tạo dựng và duy trì thịnh vượng lâu dài.",
                "Sự che chở đáng tin cậy và hào phóng về vật chất lẫn tinh thần được thể hiện trong mối quan hệ.",
                "Nên dựa vào kinh nghiệm thực tế và sự thận trọng đã được chứng minh để đưa ra quyết định quan trọng.",
                "Sự làm chủ vững vàng thế giới vật chất, kết hợp giữa thành công, sự hào phóng và trách nhiệm."
        ));
        return m;
    }
}
