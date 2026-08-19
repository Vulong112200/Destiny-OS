package io.destinyos.engines.tarot;

import io.destinyos.core.signal.Polarity;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Major Arcana Vietnamese interpretive content (research item R11).
 * Grounded in standard Rider-Waite-Smith tradition (A.E. Waite, 1910,
 * "Pictorial Key to the Tarot", and the consistent derivative corpus).
 */
final class TarotCardMeaningsMajor {

    private TarotCardMeaningsMajor() {
    }

    static Map<String, TarotCardMeaning> entries() {
        Map<String, TarotCardMeaning> m = new LinkedHashMap<>();

        m.put("MAJOR_00_THE_FOOL", new TarotCardMeaning(
                List.of("khởi đầu mới", "tự do", "hồn nhiên", "tinh thần phiêu lưu"),
                List.of("liều lĩnh", "bốc đồng", "thiếu chuẩn bị"),
                Polarity.SUPPORT, Polarity.CAUTION,
                "Khởi đầu mới trong sự nghiệp — nên mạnh dạn thử hướng đi khác, nhưng cần chuẩn bị kỹ trước khi bước đi.",
                "Tinh thần lạc quan về tài chính, nhưng dễ chi tiêu bốc đồng nếu không cân nhắc kỹ.",
                "Khởi đầu một mối quan hệ mới với sự cởi mở, hồn nhiên, chưa vướng bận quá khứ.",
                "Nên tin vào trực giác và dám bước đi, nhưng đừng bỏ qua những rủi ro đã thấy rõ.",
                "Biểu tượng của khởi đầu, tiềm năng vô hạn và tinh thần phiêu lưu không sợ hãi."
        ));

        m.put("MAJOR_01_THE_MAGICIAN", new TarotCardMeaning(
                List.of("ý chí", "khả năng hiện thực hóa", "kỹ năng", "sáng tạo chủ động"),
                List.of("lừa dối", "thao túng", "tài năng chưa dùng đúng"),
                Polarity.SUPPORT, Polarity.CAUTION,
                "Đủ kỹ năng và nguồn lực để biến ý tưởng công việc thành hành động cụ thể, chỉ cần tập trung ý chí.",
                "Có khả năng xoay xở và tận dụng nguồn lực sẵn có để tạo ra kết quả tài chính rõ ràng.",
                "Chủ động bày tỏ mong muốn và dẫn dắt mối quan hệ theo hướng mình mong đợi.",
                "Đây là thời điểm để hành động thay vì chỉ suy nghĩ, vì mọi công cụ cần thiết đã có sẵn trong tay.",
                "Biểu tượng của ý chí tập trung, kỹ năng và khả năng biến tiềm năng thành hiện thực."
        ));

        m.put("MAJOR_02_THE_HIGH_PRIESTESS", new TarotCardMeaning(
                List.of("trực giác", "bí ẩn", "tri thức nội tâm", "tiềm thức"),
                List.of("bí mật bị che giấu", "mất kết nối trực giác", "hời hợt"),
                Polarity.NEUTRAL, Polarity.CAUTION,
                "Có những thông tin hoặc yếu tố công việc chưa lộ rõ, cần quan sát và chờ đợi thay vì hành động vội.",
                "Chưa nên đưa ra quyết định tài chính lớn khi thông tin còn mơ hồ, hãy tin vào cảm nhận thận trọng của bản thân.",
                "Có những cảm xúc hoặc điều chưa nói ra trong mối quan hệ, cần lắng nghe trực giác nhiều hơn lời nói.",
                "Nên tạm dừng và lắng nghe tiếng nói bên trong trước khi quyết định, vì câu trả lời chưa hiển lộ hoàn toàn.",
                "Biểu tượng của trực giác, tri thức tiềm ẩn và những điều còn nằm sau tấm màn bí mật."
        ));

        m.put("MAJOR_03_THE_EMPRESS", new TarotCardMeaning(
                List.of("sung túc", "nuôi dưỡng", "sáng tạo", "sinh sôi"),
                List.of("bế tắc sáng tạo", "phụ thuộc", "thiếu chăm sóc bản thân"),
                Polarity.SUPPORT, Polarity.CAUTION,
                "Môi trường làm việc thuận lợi cho sự phát triển và các dự án mang tính sáng tạo, nuôi dưỡng lâu dài.",
                "Tài chính có xu hướng sung túc, dồi dào, thuận lợi cho việc phát triển và vun đắp lâu dài.",
                "Mối quan hệ được nuôi dưỡng bằng sự ấm áp, quan tâm và gắn bó tình cảm sâu sắc.",
                "Nên chọn hướng đi nuôi dưỡng sự phát triển bền vững thay vì chạy theo kết quả nhanh chóng.",
                "Biểu tượng của sự sung túc, khả năng sáng tạo và bản năng nuôi dưỡng tự nhiên."
        ));

        m.put("MAJOR_04_THE_EMPEROR", new TarotCardMeaning(
                List.of("quyền lực", "cấu trúc", "kỷ luật", "ổn định"),
                List.of("độc đoán", "cứng nhắc", "mất kiểm soát"),
                Polarity.SUPPORT, Polarity.CAUTION,
                "Vai trò lãnh đạo, tổ chức và thiết lập cấu trúc rõ ràng sẽ mang lại kết quả vững chắc trong công việc.",
                "Kỷ luật tài chính và kế hoạch dài hạn có hệ thống sẽ giúp xây dựng nền tảng ổn định.",
                "Cần sự ổn định, cam kết rõ ràng và một người biết dẫn dắt trong mối quan hệ.",
                "Nên áp dụng nguyên tắc và trật tự khi ra quyết định, tránh để cảm xúc chi phối.",
                "Biểu tượng của quyền lực chính danh, trật tự và khả năng kiến tạo cấu trúc bền vững."
        ));

        m.put("MAJOR_05_THE_HIEROPHANT", new TarotCardMeaning(
                List.of("truyền thống", "quy chuẩn", "cố vấn", "học hỏi bài bản"),
                List.of("nổi loạn", "phá vỡ quy tắc", "giáo điều cứng nhắc"),
                Polarity.SUPPORT, Polarity.NEUTRAL,
                "Tuân theo quy trình, thể chế hoặc lời khuyên của người có kinh nghiệm sẽ mang lại tiến triển ổn định.",
                "Cách tiếp cận tài chính an toàn, theo khuôn khổ đã được kiểm chứng, phù hợp hơn là mạo hiểm.",
                "Mối quan hệ được củng cố qua cam kết chính thức, giá trị chung và sự công nhận của cộng đồng.",
                "Nên tham khảo ý kiến người có kinh nghiệm hoặc dựa vào phương pháp đã được kiểm chứng.",
                "Biểu tượng của truyền thống, thể chế và con đường học hỏi theo khuôn mẫu đã định hình."
        ));

        m.put("MAJOR_06_THE_LOVERS", new TarotCardMeaning(
                List.of("kết nối", "lựa chọn", "hài hòa giá trị", "gắn bó"),
                List.of("bất hòa", "lựa chọn sai lầm", "giá trị lệch nhau"),
                Polarity.SUPPORT, Polarity.CAUTION,
                "Sự hợp tác ăn ý và lựa chọn con đường phù hợp với giá trị cá nhân sẽ mang lại kết quả tốt trong công việc.",
                "Cần đưa ra một lựa chọn tài chính quan trọng, tốt nhất nên cân nhắc dựa trên giá trị lâu dài thay vì cảm tính nhất thời.",
                "Sự gắn kết sâu sắc, hài hòa giữa hai giá trị và một lựa chọn tình cảm quan trọng đang đến gần.",
                "Cần lựa chọn dựa trên giá trị cốt lõi và sự đồng thuận thực sự, không chỉ vì cảm xúc nhất thời.",
                "Biểu tượng của kết nối sâu sắc, sự hài hòa giữa các giá trị và một lựa chọn mang tính bước ngoặt."
        ));

        m.put("MAJOR_07_THE_CHARIOT", new TarotCardMeaning(
                List.of("quyết tâm", "chiến thắng", "kiểm soát", "tiến về phía trước"),
                List.of("mất phương hướng", "hung hăng", "trở ngại chồng chất"),
                Polarity.SUPPORT, Polarity.CAUTION,
                "Ý chí mạnh mẽ và khả năng kiểm soát các yếu tố đối lập sẽ giúp vượt qua thử thách để đạt mục tiêu công việc.",
                "Quyết tâm và kỷ luật cao độ giúp đạt được mục tiêu tài chính đã đề ra, dù có cạnh tranh.",
                "Cần sự quyết đoán và thống nhất hướng đi chung để vượt qua giai đoạn nhiều lực kéo trái chiều.",
                "Nên tiến về phía trước với quyết tâm rõ ràng, giữ vững kiểm soát thay vì để hoàn cảnh chi phối.",
                "Biểu tượng của ý chí chiến thắng, khả năng làm chủ các lực đối lập để tiến về phía trước."
        ));

        m.put("MAJOR_08_STRENGTH", new TarotCardMeaning(
                List.of("sức mạnh nội tâm", "lòng can đảm", "kiên nhẫn", "lòng trắc ẩn"),
                List.of("tự nghi ngờ", "yếu đuối", "mất kiên nhẫn"),
                Polarity.SUPPORT, Polarity.CAUTION,
                "Sự kiên trì mềm mỏng và lòng can đảm bền bỉ sẽ giúp vượt qua áp lực công việc hơn là đối đầu trực diện.",
                "Cần sự kiên nhẫn và bản lĩnh vững vàng để xử lý áp lực tài chính thay vì phản ứng vội vàng.",
                "Sự dịu dàng, kiên nhẫn và thấu cảm sẽ hàn gắn mối quan hệ tốt hơn là ép buộc hay tranh cãi.",
                "Nên đối diện khó khăn bằng sự điềm tĩnh và lòng kiên nhẫn thay vì dùng vũ lực hay ép buộc.",
                "Biểu tượng của sức mạnh nội tâm dịu dàng, lòng can đảm chế ngự bằng sự kiên nhẫn và trắc ẩn."
        ));

        m.put("MAJOR_09_THE_HERMIT", new TarotCardMeaning(
                List.of("hướng nội", "tìm kiếm bên trong", "cô độc có chủ đích", "chiêm nghiệm"),
                List.of("cô lập", "cô đơn", "rút lui quá mức"),
                Polarity.NEUTRAL, Polarity.CAUTION,
                "Đây là giai đoạn cần lùi lại để suy xét kỹ hướng đi sự nghiệp thay vì vội vàng hành động theo số đông.",
                "Nên tạm dừng và xem xét kỹ tình hình tài chính một mình trước khi đưa ra quyết định tiếp theo.",
                "Cần thời gian ở một mình để hiểu rõ cảm xúc thật của bản thân trước khi tiến xa hơn trong mối quan hệ.",
                "Nên tìm một khoảng lặng để suy ngẫm nội tâm trước khi đưa ra quyết định quan trọng.",
                "Biểu tượng của sự tìm kiếm nội tâm, trí tuệ có được qua chiêm nghiệm một mình."
        ));

        m.put("MAJOR_10_WHEEL_OF_FORTUNE", new TarotCardMeaning(
                List.of("vòng xoay số phận", "bước ngoặt", "vận may", "chu kỳ thay đổi"),
                List.of("vận rủi", "chống lại thay đổi", "chu kỳ lặp lại bất lợi"),
                Polarity.SUPPORT, Polarity.CAUTION,
                "Một bước ngoặt bất ngờ đang mở ra cơ hội mới trong sự nghiệp, nên sẵn sàng đón nhận thay đổi.",
                "Vận may tài chính có thể xoay chuyển thuận lợi, nhưng cần nhớ chu kỳ luôn lên xuống.",
                "Mối quan hệ có thể bước sang một giai đoạn khác do một sự kiện hoặc bước ngoặt bất ngờ.",
                "Nên chấp nhận rằng hoàn cảnh đang thay đổi và điều chỉnh linh hoạt thay vì cưỡng lại vòng xoay tự nhiên.",
                "Biểu tượng của vòng xoay số phận, những bước ngoặt và chu kỳ lên xuống tất yếu của cuộc sống."
        ));

        m.put("MAJOR_11_JUSTICE", new TarotCardMeaning(
                List.of("công bằng", "sự thật", "nhân quả", "quyết định khách quan"),
                List.of("bất công", "thiếu trung thực", "trốn tránh trách nhiệm"),
                Polarity.SUPPORT, Polarity.NEGATIVE,
                "Kết quả công việc sẽ phản ánh đúng công sức đã bỏ ra, sự minh bạch và công bằng được đề cao.",
                "Các vấn đề tài chính cần được giải quyết minh bạch, đúng theo hợp đồng hoặc pháp lý đã cam kết.",
                "Mối quan hệ cần sự công bằng, thẳng thắn và trách nhiệm rõ ràng từ cả hai phía.",
                "Nên cân nhắc quyết định dựa trên lý trí, sự thật khách quan và hệ quả lâu dài, không thiên vị cảm tính.",
                "Biểu tượng của công lý, sự thật và quy luật nhân quả — mỗi hành động đều dẫn đến hệ quả tương xứng."
        ));

        m.put("MAJOR_12_THE_HANGED_MAN", new TarotCardMeaning(
                List.of("tạm dừng", "buông bỏ", "góc nhìn khác", "chờ đợi có chủ đích"),
                List.of("trì hoãn kéo dài", "chống cự vô ích", "hy sinh vô nghĩa"),
                Polarity.NEUTRAL, Polarity.CAUTION,
                "Công việc đang ở giai đoạn đình trệ tạm thời, cần thay đổi góc nhìn thay vì cố gắng thúc đẩy bằng mọi giá.",
                "Nên tạm hoãn các quyết định tài chính lớn và chờ đến khi tình hình rõ ràng hơn.",
                "Cần buông bỏ việc muốn kiểm soát mối quan hệ và chấp nhận nhìn nhận vấn đề theo cách khác.",
                "Đôi khi không hành động lại là quyết định đúng đắn nhất, hãy chấp nhận tạm dừng để nhìn rõ vấn đề hơn.",
                "Biểu tượng của sự tạm dừng có chủ đích, buông bỏ kiểm soát để nhìn thấy một góc nhìn mới."
        ));

        m.put("MAJOR_13_DEATH", new TarotCardMeaning(
                List.of("chuyển hóa", "kết thúc một chu kỳ", "khởi đầu mới sau kết thúc", "buông bỏ cái cũ"),
                List.of("chống lại thay đổi", "trì trệ", "sợ hãi kết thúc"),
                Polarity.NEUTRAL, Polarity.CAUTION,
                "Một giai đoạn công việc cũ đang kết thúc để nhường chỗ cho hướng đi mới, đây là sự chuyển hóa cần thiết.",
                "Cần kết thúc một phương thức tài chính không còn phù hợp để chuyển sang cách tiếp cận mới.",
                "Mối quan hệ đang trải qua một sự chuyển đổi sâu sắc, có thể là kết thúc để mở ra một chương mới.",
                "Nên chấp nhận buông bỏ những gì đã không còn phù hợp để có không gian cho điều mới xuất hiện.",
                "Biểu tượng của sự chuyển hóa sâu sắc — một chu kỳ kết thúc để mở đường cho khởi đầu mới, không phải cái chết theo nghĩa đen."
        ));

        m.put("MAJOR_14_TEMPERANCE", new TarotCardMeaning(
                List.of("cân bằng", "điều độ", "hòa hợp", "kiên nhẫn dung hòa"),
                List.of("mất cân bằng", "thái quá", "xung đột không hòa giải"),
                Polarity.SUPPORT, Polarity.CAUTION,
                "Sự điều độ, kết hợp hài hòa giữa các yếu tố khác nhau trong công việc sẽ mang lại tiến triển bền vững.",
                "Cân đối chi tiêu và tiết chế sẽ giúp tài chính ổn định lâu dài hơn là chạy theo cực đoan.",
                "Sự dung hòa, kiên nhẫn điều chỉnh lẫn nhau sẽ giúp mối quan hệ trở nên hài hòa và bền vững.",
                "Nên tìm điểm cân bằng giữa các lựa chọn đối lập thay vì nghiêng hẳn về một cực đoan nào.",
                "Biểu tượng của sự điều độ, hòa trộn hài hòa giữa các thái cực để tạo nên trạng thái cân bằng bền vững."
        ));

        m.put("MAJOR_15_THE_DEVIL", new TarotCardMeaning(
                List.of("ràng buộc", "vật chất hóa", "cám dỗ", "phụ thuộc"),
                List.of("giải thoát", "nhận ra ràng buộc", "phá vỡ xiềng xích"),
                Polarity.CAUTION, Polarity.NEUTRAL,
                "Có nguy cơ bị mắc kẹt trong môi trường làm việc độc hại hoặc lối mòn không lối thoát do sợ thay đổi.",
                "Cẩn trọng với nợ nần, chi tiêu buông thả hoặc sự phụ thuộc tài chính không lành mạnh.",
                "Mối quan hệ có thể mang tính ràng buộc, ghen tuông hoặc phụ thuộc thái quá cần được nhìn nhận thẳng thắn.",
                "Nên tự hỏi liệu quyết định này xuất phát từ ham muốn nhất thời hay nỗi sợ, thay vì từ nhu cầu thực sự.",
                "Biểu tượng của sự ràng buộc bởi vật chất, cám dỗ và những xiềng xích tự thân dễ bị bỏ qua."
        ));

        m.put("MAJOR_16_THE_TOWER", new TarotCardMeaning(
                List.of("biến động đột ngột", "sụp đổ", "thức tỉnh", "khủng hoảng bất ngờ"),
                List.of("thảm họa được ngăn chặn", "sợ thay đổi", "biến động bị trì hoãn"),
                Polarity.NEGATIVE, Polarity.CAUTION,
                "Một biến động bất ngờ có thể làm đảo lộn kế hoạch công việc hiện tại, buộc phải xây dựng lại nền tảng.",
                "Nguy cơ một cú sốc tài chính bất ngờ xảy ra, cần chuẩn bị tâm lý cho tình huống ngoài dự tính.",
                "Một sự thật bị che giấu có thể bất ngờ bị phơi bày, gây chấn động cho mối quan hệ hiện tại.",
                "Cần chuẩn bị tinh thần cho một thay đổi đột ngột, nền tảng cũ có thể không còn đứng vững được nữa.",
                "Biểu tượng của biến động đột ngột, sự sụp đổ của nền tảng không còn chắc chắn để mở đường cho thức tỉnh."
        ));

        m.put("MAJOR_17_THE_STAR", new TarotCardMeaning(
                List.of("hy vọng", "niềm tin", "hàn gắn", "cảm hứng"),
                List.of("mất niềm tin", "thất vọng", "cạn kiệt cảm hứng"),
                Polarity.SUPPORT, Polarity.CAUTION,
                "Sau giai đoạn khó khăn, công việc đang mở ra hướng đi đầy hy vọng và nguồn cảm hứng mới.",
                "Tình hình tài chính có dấu hiệu phục hồi tích cực sau giai đoạn khó khăn, nên giữ vững niềm tin.",
                "Mối quan hệ được chữa lành và tiếp thêm hy vọng sau những tổn thương trước đó.",
                "Nên giữ niềm tin vào hướng đi đã chọn, vì đây là giai đoạn hàn gắn và tái tạo nguồn cảm hứng.",
                "Biểu tượng của hy vọng, niềm tin được phục hồi và ánh sáng dẫn đường sau giai đoạn tăm tối."
        ));

        m.put("MAJOR_18_THE_MOON", new TarotCardMeaning(
                List.of("ảo giác", "bất an", "tiềm thức", "sự mơ hồ"),
                List.of("sự thật dần sáng tỏ", "nỗi sợ được giải tỏa", "vẫn còn nhầm lẫn"),
                Polarity.CAUTION, Polarity.NEUTRAL,
                "Thông tin công việc chưa rõ ràng có thể gây hiểu lầm, nên thận trọng trước khi đưa ra đánh giá cuối cùng.",
                "Cẩn trọng với những khoản đầu tư hay quyết định tài chính thiếu minh bạch, dễ gây nhầm lẫn.",
                "Có sự lo lắng, nghi ngờ hoặc điều gì đó chưa được nói rõ đang ảnh hưởng đến mối quan hệ.",
                "Nên thận trọng vì tình huống hiện tại chưa hoàn toàn rõ ràng, dễ bị cảm xúc hoặc ảo tưởng chi phối.",
                "Biểu tượng của vùng mờ giữa ý thức và tiềm thức, nơi nỗi sợ và ảo giác có thể lấn át sự thật."
        ));

        m.put("MAJOR_19_THE_SUN", new TarotCardMeaning(
                List.of("thành công", "niềm vui", "sức sống", "sự rõ ràng"),
                List.of("thành công bị trì hoãn", "lạc quan thái quá", "niềm vui tạm thời lu mờ"),
                Polarity.SUPPORT, Polarity.CAUTION,
                "Công việc đang tiến triển thuận lợi, thành quả rõ ràng và được công nhận xứng đáng.",
                "Tài chính khởi sắc rõ rệt, đây là giai đoạn thuận lợi để phát triển và gặt hái thành quả.",
                "Mối quan hệ tràn đầy niềm vui, sự chân thành và gắn kết tích cực giữa hai người.",
                "Đây là thời điểm thuận lợi để hành động với sự tự tin và lạc quan, kết quả có xu hướng tốt đẹp.",
                "Biểu tượng của thành công rực rỡ, niềm vui chân thật và sức sống tràn đầy."
        ));

        m.put("MAJOR_20_JUDGEMENT", new TarotCardMeaning(
                List.of("tái sinh", "lời gọi bên trong", "nhìn lại bản thân", "thức tỉnh"),
                List.of("tự nghi ngờ bản thân", "phớt lờ lời gọi", "phán xét khắt khe"),
                Polarity.SUPPORT, Polarity.CAUTION,
                "Đây là thời điểm nhìn lại chặng đường sự nghiệp đã qua để đưa ra một quyết định mang tính bước ngoặt.",
                "Nên đánh giá lại toàn bộ tình hình tài chính trong quá khứ để rút ra bài học cho giai đoạn tới.",
                "Mối quan hệ bước vào giai đoạn nhìn nhận lại một cách trung thực để quyết định hướng đi tiếp theo.",
                "Nên lắng nghe tiếng gọi từ bên trong và đưa ra quyết định dựa trên sự nhìn nhận trung thực về bản thân.",
                "Biểu tượng của sự thức tỉnh, tái sinh sau khi nhìn lại và đánh giá trung thực chặng đường đã qua."
        ));

        m.put("MAJOR_21_THE_WORLD", new TarotCardMeaning(
                List.of("hoàn thành", "viên mãn", "trọn vẹn", "thành tựu"),
                List.of("dang dở", "trì hoãn hoàn thành", "thiếu trọn vẹn"),
                Polarity.SUPPORT, Polarity.CAUTION,
                "Một chặng đường sự nghiệp dài đang đi đến hồi hoàn thiện, thành quả xứng đáng đã ở trong tầm tay.",
                "Mục tiêu tài chính dài hạn đang đến gần điểm hoàn thành, công sức tích lũy đã cho thấy kết quả.",
                "Mối quan hệ đạt đến sự trọn vẹn, hòa hợp và viên mãn sau một hành trình dài cùng nhau.",
                "Đây là thời điểm thích hợp để hoàn tất một chu kỳ trước khi mở ra một hành trình mới.",
                "Biểu tượng của sự hoàn thành trọn vẹn, viên mãn sau một hành trình dài và khép lại một chu kỳ lớn."
        ));

        return m;
    }
}
