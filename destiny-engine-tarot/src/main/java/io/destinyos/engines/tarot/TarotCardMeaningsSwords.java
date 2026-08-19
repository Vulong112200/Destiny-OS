package io.destinyos.engines.tarot;

import io.destinyos.core.signal.Polarity;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Swords (Air) suit Vietnamese interpretive content (research item R11).
 * Grounded in standard Rider-Waite-Smith tradition. Traditionally the most
 * challenging suit in the deck - many cards here are genuinely CAUTION or
 * NEGATIVE, which is correct, not something to soften.
 */
final class TarotCardMeaningsSwords {

    private TarotCardMeaningsSwords() {
    }

    static Map<String, TarotCardMeaning> entries() {
        Map<String, TarotCardMeaning> m = new LinkedHashMap<>();
        m.put("MINOR_SWORDS_01_ACE", new TarotCardMeaning(
                List.of("minh mẫn đột phá", "sự thật được hé lộ", "khởi đầu rõ ràng"),
                List.of("tư duy hỗn loạn", "thông tin sai lệch", "quyết định vội vàng"),
                Polarity.SUPPORT, Polarity.CAUTION,
                "Một ý tưởng hoặc hướng đi rõ ràng, sắc bén xuất hiện trong công việc, giúp cắt đứt sự mơ hồ.",
                "Sự minh bạch mới trong các vấn đề tài chính, nhìn nhận tình hình một cách khách quan hơn.",
                "Một cuộc trò chuyện thẳng thắn, chân thành có thể làm sáng tỏ mối quan hệ.",
                "Đây là lúc để nhìn nhận sự thật một cách rõ ràng, dùng lý trí thay vì cảm xúc.",
                "Sức mạnh của trí tuệ, sự thật và khởi đầu đầy minh mẫn."
        ));
        m.put("MINOR_SWORDS_02_TWO", new TarotCardMeaning(
                List.of("thế bế tắc", "tránh né quyết định", "cân bằng căng thẳng", "bịt mắt trước sự thật"),
                List.of("căng thẳng vỡ oà", "quá tải thông tin", "buộc phải lựa chọn", "hoang mang gia tăng"),
                Polarity.NEUTRAL, Polarity.CAUTION,
                "Bạn đang né tránh một quyết định quan trọng trong công việc, cố giữ thế cân bằng giả tạo giữa hai lựa chọn trái ngược.",
                "Các vấn đề tài chính bị gác lại thay vì đối mặt trực tiếp, khiến tình hình mơ hồ kéo dài thêm.",
                "Hai người đang giữ khoảng cách phòng thủ, né tránh một cuộc đối thoại cần thiết để không phải đối diện sự thật.",
                "Đây là lá bài của sự bế tắc: bạn không thể tiếp tục bịt mắt mãi, cần tháo bỏ phòng vệ để nhìn thẳng vào lựa chọn.",
                "Một trạng thái cân bằng mong manh sinh ra từ sự né tránh, trong khi một sự thật vẫn đang chờ được nhìn nhận."
        ));
        m.put("MINOR_SWORDS_03_THREE", new TarotCardMeaning(
                List.of("đau lòng", "sự thật gây tổn thương", "phản bội", "mất mát tình cảm"),
                List.of("hàn gắn vết thương", "buông bỏ nỗi đau", "tha thứ dần dần", "nỗi đau âm ỉ kéo dài"),
                Polarity.NEGATIVE, Polarity.CAUTION,
                "Một lời phê bình gay gắt hoặc sự phản bội từ đồng nghiệp có thể gây tổn thương sâu sắc trong công việc.",
                "Một sự thật tài chính phũ phàng, chẳng hạn một khoản lỗ hay một hợp đồng đổ vỡ, khiến bạn thất vọng nặng nề.",
                "Đây là lá bài của trái tim tan vỡ: chia ly, phản bội hoặc một lời nói thẳng thắn gây đau đớn giữa hai người.",
                "Một sự thật đau lòng cần được chấp nhận thẳng thắn trước khi bạn có thể bước tiếp một cách sáng suốt.",
                "Nỗi đau và sự thất vọng hiện diện rõ ràng, nhưng thừa nhận chúng chính là bước đầu tiên của quá trình chữa lành."
        ));
        m.put("MINOR_SWORDS_04_FOUR", new TarotCardMeaning(
                List.of("nghỉ ngơi cần thiết", "tĩnh tâm hồi phục", "rút lui tạm thời", "chuẩn bị cho chặng tiếp theo"),
                List.of("kiệt sức kéo dài", "bị buộc quay lại guồng quay", "trì trệ", "nghỉ ngơi chưa trọn vẹn"),
                Polarity.SUPPORT, Polarity.CAUTION,
                "Đây là lúc cần tạm dừng, rút lui khỏi áp lực công việc để phục hồi tinh thần trước khi tiếp tục.",
                "Nên tạm ngưng các quyết định tài chính lớn, dành thời gian đánh giá lại tình hình trong yên tĩnh.",
                "Một khoảng lặng cần thiết sau xung đột, cho phép cả hai bên nghỉ ngơi và nhìn lại trước khi nói chuyện tiếp.",
                "Đừng vội quyết định ngay lúc này; sự tĩnh lặng và nghỉ ngơi sẽ mang lại góc nhìn rõ ràng hơn.",
                "Một giai đoạn phục hồi, tĩnh tâm sau căng thẳng, chuẩn bị năng lượng cho chặng đường sắp tới."
        ));
        m.put("MINOR_SWORDS_05_FIVE", new TarotCardMeaning(
                List.of("chiến thắng rỗng tuếch", "xung đột gay gắt", "thắng bằng mọi giá", "quan hệ rạn nứt"),
                List.of("hoà giải miễn cưỡng", "buông bỏ hận thù", "hậu quả xung đột còn đó", "khó dứt điểm mâu thuẫn"),
                Polarity.NEGATIVE, Polarity.CAUTION,
                "Một cuộc tranh giành nơi công sở có thể mang lại chiến thắng trước mắt nhưng để lại quan hệ đổ vỡ và uy tín sứt mẻ.",
                "Đạt được lợi ích tài chính bằng thủ đoạn hoặc chèn ép người khác sẽ để lại cái giá phải trả về sau.",
                "Xung đột gay gắt khiến cả hai đều tổn thương; thắng trong một cuộc tranh cãi không đồng nghĩa với việc mối quan hệ tốt hơn.",
                "Hãy cân nhắc liệu việc giành phần thắng bằng mọi giá có thực sự đáng, hay chỉ để lại hậu quả lâu dài.",
                "Xung đột và cạnh tranh gay gắt, nơi chiến thắng đạt được lại mang vị đắng của mất mát."
        ));
        m.put("MINOR_SWORDS_06_SIX", new TarotCardMeaning(
                List.of("chuyển tiếp êm đềm", "rời xa sóng gió", "hướng tới bình yên", "hồi phục dần dần"),
                List.of("chưa thể buông bỏ", "cản trở quá trình chuyển tiếp", "vướng mắc chưa giải quyết", "trì hoãn ra đi"),
                Polarity.SUPPORT, Polarity.CAUTION,
                "Một giai đoạn khó khăn trong công việc đang dần khép lại, mở đường cho một môi trường ổn định hơn.",
                "Tình hình tài chính đang chuyển từ bất ổn sang ổn định hơn, dù cần thêm thời gian để hoàn toàn yên tâm.",
                "Cả hai đang cùng nhau rời khỏi giai đoạn sóng gió, hướng đến sự bình yên hơn, dù dư âm chưa hoàn toàn dứt.",
                "Đây là thời điểm thích hợp để rời bỏ hoàn cảnh khó khăn và tiến về phía trước, dù có thể phải để lại điều gì đó phía sau.",
                "Một cuộc di chuyển hoặc chuyển tiếp cần thiết, rời xa khó khăn để tìm đến vùng nước lặng hơn."
        ));
        m.put("MINOR_SWORDS_07_SEVEN", new TarotCardMeaning(
                List.of("toan tính riêng", "hành động lén lút", "chiến lược một mình", "che giấu ý đồ"),
                List.of("bị phát hiện", "lộ tẩy dối trá", "hối lỗi muộn màng", "hậu quả của sự lừa dối"),
                Polarity.CAUTION, Polarity.NEGATIVE,
                "Có người đang hành động một mình, giấu giếm thông tin hoặc theo đuổi lợi ích riêng mà không công khai trong công việc.",
                "Cẩn trọng với những thỏa thuận thiếu minh bạch hoặc hành vi lách luật để trục lợi tài chính trước mắt.",
                "Sự thiếu trung thực, giấu giếm hoặc toan tính riêng đang len lỏi vào mối quan hệ, làm xói mòn lòng tin.",
                "Hãy tự hỏi liệu con đường bạn chọn có đòi hỏi phải che giấu điều gì đó, và cái giá của sự thiếu trung thực đó là gì.",
                "Sự khôn khéo, chiến lược hoặc toan tính riêng, tiềm ẩn nguy cơ mất lòng tin nếu bị phát hiện."
        ));
        m.put("MINOR_SWORDS_08_EIGHT", new TarotCardMeaning(
                List.of("cảm giác bị mắc kẹt", "tự trói buộc bản thân", "tâm lý nạn nhân", "bế tắc do sợ hãi"),
                List.of("giải thoát bản thân", "tự lấy lại quyền kiểm soát", "nhìn ra lối thoát", "vượt qua nỗi sợ"),
                Polarity.NEGATIVE, Polarity.SUPPORT,
                "Bạn cảm thấy bị trói buộc trong công việc hiện tại, dù trên thực tế có nhiều lựa chọn hơn bạn nghĩ nhưng nỗi sợ đang che mờ chúng.",
                "Cảm giác bế tắc tài chính phần lớn đến từ nỗi sợ hoặc niềm tin giới hạn, chứ không hẳn từ hoàn cảnh thực tế.",
                "Bạn tự giam mình trong vai nạn nhân của mối quan hệ, trong khi thực chất vẫn có những lối thoát chưa được nhìn nhận.",
                "Điều đang trói buộc bạn phần lớn nằm trong tâm trí; hãy tháo bỏ tấm bịt mắt tự áp đặt để thấy các lựa chọn thực sự.",
                "Cảm giác bị mắc kẹt và bất lực, nhưng sự trói buộc này chủ yếu là tự tạo và có thể được tháo gỡ."
        ));
        m.put("MINOR_SWORDS_09_NINE", new TarotCardMeaning(
                List.of("lo âu triền miên", "ác mộng", "dằn vặt tinh thần", "nỗi sợ phóng đại"),
                List.of("bắt đầu nguôi ngoai", "tìm kiếm sự trợ giúp", "đối diện nỗi lo trong ánh sáng", "vực sâu tuyệt vọng"),
                Polarity.NEGATIVE, Polarity.CAUTION,
                "Áp lực công việc gây ra lo âu và mất ngủ, dù nhiều nỗi sợ đang bị phóng đại hơn thực tế đang diễn ra.",
                "Nỗi lo về tiền bạc trở nên nặng nề trong tâm trí, đôi khi vượt xa mức độ nghiêm trọng thực sự của vấn đề.",
                "Sự dằn vặt, nghi ngờ và lo lắng âm thầm đang bào mòn tâm lý trong mối quan hệ, cần được chia sẻ thay vì giữ trong lòng.",
                "Đừng quyết định khi đang chìm trong lo âu tột độ; hãy tìm cách trấn an tâm trí trước khi nhìn nhận vấn đề một cách tỉnh táo.",
                "Đêm tối của sự lo âu và dằn vặt tinh thần, nơi nỗi sợ trong đầu thường lớn hơn thực tế bên ngoài."
        ));
        m.put("MINOR_SWORDS_10_TEN", new TarotCardMeaning(
                List.of("chạm đáy", "kết thúc đau đớn", "phản bội tận cùng", "kiệt quệ hoàn toàn"),
                List.of("hồi phục chậm rãi", "khó chấp nhận sự kết thúc", "ánh sáng cuối đường hầm", "vết thương còn rỉ máu"),
                Polarity.NEGATIVE, Polarity.CAUTION,
                "Một giai đoạn công việc kết thúc theo cách đau đớn, gần như chạm đáy, nhưng đây cũng là điểm không thể tệ hơn nữa.",
                "Một tổn thất tài chính nghiêm trọng hoặc sự sụp đổ hoàn toàn của một kế hoạch đang diễn ra, đánh dấu điểm thấp nhất.",
                "Một mối quan hệ hoặc giai đoạn tình cảm kết thúc trong đau đớn, cảm giác như bị phản bội hoàn toàn.",
                "Hãy chấp nhận rằng một chương đã thực sự khép lại; cố bám víu chỉ kéo dài thêm đau khổ không cần thiết.",
                "Điểm chạm đáy đau đớn nhất, nhưng cũng chính là ranh giới mà sau đó mọi thứ chỉ có thể tốt lên."
        ));
        m.put("MINOR_SWORDS_11_PAGE", new TarotCardMeaning(
                List.of("tò mò quan sát", "người đưa tin", "cảnh giác nhạy bén", "học hỏi ý tưởng mới"),
                List.of("tin đồn thất thiệt", "do thám dò xét", "nói nhiều làm ít", "thông tin sai lệch"),
                Polarity.SUPPORT, Polarity.CAUTION,
                "Một luồng thông tin, tin tức hoặc ý tưởng mới xuất hiện, đòi hỏi sự quan sát nhạy bén và tinh thần ham học hỏi trong công việc.",
                "Hãy thu thập thông tin cẩn thận trước khi hành động về tài chính, tránh tin ngay những gì nghe được mà chưa kiểm chứng.",
                "Sự tò mò, những cuộc trò chuyện thẳng thắn hoặc một tin tức bất ngờ có thể xuất hiện trong mối quan hệ.",
                "Hãy quan sát và thu thập thông tin kỹ càng trước khi hành động, đừng vội kết luận khi chưa nắm rõ sự thật.",
                "Tinh thần tò mò, cảnh giác của người quan sát và đưa tin, luôn để mắt đến những gì đang diễn ra xung quanh."
        ));
        m.put("MINOR_SWORDS_12_KNIGHT", new TarotCardMeaning(
                List.of("hành động thần tốc", "quyết đoán táo bạo", "lao thẳng vào mục tiêu", "thiếu kiên nhẫn"),
                List.of("liều lĩnh mất kiểm soát", "hung hăng gây hấn", "hành động hấp tấp", "kiệt sức vì lao lực"),
                Polarity.CAUTION, Polarity.NEGATIVE,
                "Bạn hành động nhanh và quyết đoán để đạt mục tiêu công việc, nhưng tốc độ đó dễ khiến bạn bỏ qua chi tiết quan trọng.",
                "Một quyết định tài chính táo bạo, chớp nhoáng đang được đưa ra, tiềm ẩn rủi ro do thiếu cân nhắc kỹ.",
                "Cách tiếp cận thẳng thắn, đôi khi nóng vội trong lời nói có thể gây va chạm không cần thiết với người kia.",
                "Tốc độ và sự quyết đoán là điểm mạnh lúc này, nhưng hãy cẩn trọng để không hành động hấp tấp thiếu suy xét.",
                "Năng lượng xông xáo, quyết liệt lao về phía trước, cần đi kèm sự tỉnh táo để không trở thành liều lĩnh."
        ));
        m.put("MINOR_SWORDS_13_QUEEN", new TarotCardMeaning(
                List.of("sắc sảo độc lập", "thẳng thắn trung thực", "ranh giới rõ ràng", "từng trải vượt khó"),
                List.of("lạnh lùng cay nghiệt", "phán xét khắt khe", "tổn thương chưa lành", "cô lập cảm xúc"),
                Polarity.SUPPORT, Polarity.CAUTION,
                "Sự sắc sảo, tư duy độc lập và khả năng nhìn thẳng vào vấn đề giúp bạn đưa ra những nhận định công tâm trong công việc.",
                "Hãy tiếp cận các vấn đề tài chính bằng lý trí rõ ràng, không để cảm xúc chi phối các con số.",
                "Sự thẳng thắn, độc lập và ranh giới rõ ràng là điều mối quan hệ này cần, dù đôi khi lời nói có thể quá thẳng thắn.",
                "Hãy quyết định dựa trên sự thật và lý trí sắc bén, đặt ranh giới rõ ràng cho bản thân.",
                "Trí tuệ sắc sảo, sự độc lập và khả năng nói thẳng sự thật, thường đến sau những trải nghiệm từng làm tổn thương."
        ));
        m.put("MINOR_SWORDS_14_KING", new TarotCardMeaning(
                List.of("công minh sáng suốt", "quyền uy trí tuệ", "phán quyết công bằng", "tư duy logic"),
                List.of("lạnh lùng độc đoán", "lạm dụng quyền lực", "chỉ trích khắc nghiệt", "cứng nhắc giáo điều"),
                Polarity.SUPPORT, Polarity.CAUTION,
                "Vai trò lãnh đạo dựa trên trí tuệ, sự công bằng và khả năng phán đoán rõ ràng được đề cao trong công việc lúc này.",
                "Hãy đưa ra các quyết định tài chính dựa trên phân tích logic, có kỷ luật và tầm nhìn dài hạn.",
                "Sự công bằng, rõ ràng trong giao tiếp và khả năng phân xử hợp lý giúp giải quyết bất đồng trong mối quan hệ.",
                "Hãy dùng lý trí và sự công tâm để đưa ra quyết định, tránh để cảm xúc cá nhân chi phối phán đoán.",
                "Quyền uy trí tuệ, sự công minh và khả năng phán xét sáng suốt dựa trên lý lẽ vững chắc."
        ));
        return m;
    }
}
