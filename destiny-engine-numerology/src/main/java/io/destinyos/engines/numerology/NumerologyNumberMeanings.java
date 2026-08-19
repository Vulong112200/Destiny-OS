package io.destinyos.engines.numerology;

import io.destinyos.core.signal.Polarity;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lookup for {@link NumerologyNumberMeaning}, keyed by (type, value).
 * Every (type, value) pair the engine can ever produce is authored here -
 * unlike Tarot's TarotCardMeaning.EMPTY pattern, there is no partial-content
 * fallback needed since this corpus is small enough to complete in one pass.
 */
public final class NumerologyNumberMeanings {

    private NumerologyNumberMeanings() {
    }

    private static final Map<String, NumerologyNumberMeaning> ENTRIES = build();

    public static Optional<NumerologyNumberMeaning> of(NumerologyNumberType type, int value) {
        return Optional.ofNullable(ENTRIES.get(key(type, value)));
    }

    private static String key(NumerologyNumberType type, int value) {
        return type.name() + "_" + value;
    }

    private static Map<String, NumerologyNumberMeaning> build() {
        Map<String, NumerologyNumberMeaning> m = new LinkedHashMap<>();

        // =====================================================================
        // LIFE_PATH - huong di va muc dich tong the cua hanh trinh cuoc doi.
        // =====================================================================
        put(m, NumerologyNumberType.LIFE_PATH, 1,
                List.of("độc lập", "tiên phong", "lãnh đạo", "tự lực"),
                "Con đường của người tiên phong, được sinh ra để dẫn đầu và tự mình mở lối; "
                        + "cả đời học cách đứng vững trên đôi chân mình và biến ý tưởng thành hành động cụ thể.",
                Polarity.SUPPORT);
        put(m, NumerologyNumberType.LIFE_PATH, 2,
                List.of("hợp tác", "ngoại giao", "hòa giải", "nhạy cảm"),
                "Con đường của người kiến tạo hòa bình, học cách hợp tác, lắng nghe và cân bằng "
                        + "các mối quan hệ xung quanh mình. Sứ mệnh cả đời thường gắn với vai trò trung gian, "
                        + "gắn kết những mảnh ghép rời rạc lại với nhau.",
                Polarity.SUPPORT);
        put(m, NumerologyNumberType.LIFE_PATH, 3,
                List.of("sáng tạo", "biểu đạt", "lạc quan", "nghệ thuật"),
                "Con đường của người nghệ sĩ và người truyền cảm hứng, được mời gọi thể hiện bản thân "
                        + "qua lời nói, nghệ thuật hoặc sự sáng tạo để lan tỏa niềm vui sống cho những người xung quanh.",
                Polarity.SUPPORT);
        put(m, NumerologyNumberType.LIFE_PATH, 4,
                List.of("kỷ luật", "nền tảng", "lao động bền bỉ", "thử thách"),
                "Con đường đòi hỏi sự kiên trì, kỷ luật và lao động bền bỉ để xây dựng nền tảng vững chắc; "
                        + "đây thường là hành trình nhiều thử thách thực tế hơn là thuận lợi dễ dàng, đòi hỏi "
                        + "học cách chấp nhận giới hạn và trách nhiệm.",
                Polarity.CAUTION);
        put(m, NumerologyNumberType.LIFE_PATH, 5,
                List.of("tự do", "thay đổi", "trải nghiệm", "bất ổn định"),
                "Con đường của sự tự do và trải nghiệm đa dạng, luôn thôi thúc khám phá cái mới; "
                        + "bài học lớn của hành trình này là học cách tìm thấy sự nhất quán và cam kết giữa "
                        + "muôn vàn lựa chọn và thay đổi.",
                Polarity.CAUTION);
        put(m, NumerologyNumberType.LIFE_PATH, 6,
                List.of("trách nhiệm", "chăm sóc", "gia đình", "phụng sự"),
                "Con đường của người chăm lo và gìn giữ hạnh phúc cho những người xung quanh, thường gắn liền "
                        + "với vai trò trụ cột trong gia đình hoặc cộng đồng, tìm thấy ý nghĩa sống qua việc "
                        + "phụng sự và nuôi dưỡng người khác.",
                Polarity.SUPPORT);
        put(m, NumerologyNumberType.LIFE_PATH, 7,
                List.of("nội tâm", "tìm hiểu chân lý", "phân tích", "cô tịch"),
                "Con đường của người tìm kiếm chân lý qua chiêm nghiệm nội tâm và nghiên cứu sâu sắc, "
                        + "thường đòi hỏi những khoảng thời gian đơn độc; bài học lớn là học cách kết nối với "
                        + "người khác mà không đánh mất chiều sâu nội tâm của mình.",
                Polarity.CAUTION);
        put(m, NumerologyNumberType.LIFE_PATH, 8,
                List.of("thành công vật chất", "quyền lực", "tham vọng", "quản trị"),
                "Con đường hướng đến thành tựu vật chất, quyền lực và vị thế thông qua năng lực tổ chức "
                        + "và tầm nhìn kinh doanh, đòi hỏi học cách sử dụng quyền lực và tiền bạc một cách "
                        + "có trách nhiệm.",
                Polarity.SUPPORT);
        put(m, NumerologyNumberType.LIFE_PATH, 9,
                List.of("nhân đạo", "từ bi", "lý tưởng", "phụng sự nhân loại"),
                "Con đường của người mang tinh thần nhân đạo rộng lớn, hướng đến việc cho đi và phụng sự "
                        + "vì lợi ích chung hơn là lợi ích cá nhân, thường gắn với hành trình buông bỏ và "
                        + "hoàn thiện bản thân.",
                Polarity.SUPPORT);
        put(m, NumerologyNumberType.LIFE_PATH, 11,
                List.of("trực giác mạnh", "cảm hứng", "tầm nhìn tâm linh", "sứ mệnh soi đường"),
                "Con đường của người mang trực giác và cảm hứng vượt trội, được kỳ vọng soi sáng và "
                        + "truyền cảm hứng cho người khác; đây là hành trình đầy tiềm năng nhưng cũng đòi hỏi "
                        + "vượt qua sự căng thẳng nội tâm để biến tầm nhìn thành hiện thực.",
                Polarity.SUPPORT);
        put(m, NumerologyNumberType.LIFE_PATH, 22,
                List.of("kiến tạo tầm nhìn lớn", "năng lực phi thường", "hiện thực hóa lý tưởng"),
                "Con đường của 'Người Kiến Tạo Bậc Thầy', có tiềm năng biến những giấc mơ lớn lao thành "
                        + "công trình cụ thể mang lại lợi ích lâu dài cho nhiều người; đi kèm với tiềm năng "
                        + "lớn là áp lực và trách nhiệm cũng lớn không kém.",
                Polarity.SUPPORT);
        put(m, NumerologyNumberType.LIFE_PATH, 33,
                List.of("chữa lành", "giảng dạy", "yêu thương vô điều kiện", "phụng sự quy mô lớn"),
                "Con đường của 'Người Thầy Bậc Thầy', hướng đến việc chữa lành và nâng đỡ tinh thần cho "
                        + "nhiều người thông qua tình yêu thương vô điều kiện; thử thách lớn nhất là học cách "
                        + "yêu thương bản thân đủ để không kiệt sức vì cho đi.",
                Polarity.SUPPORT);

        // =====================================================================
        // EXPRESSION - tai nang va nang luc tu nhien de phat trien va the hien.
        // =====================================================================
        put(m, NumerologyNumberType.EXPRESSION, 1,
                List.of("năng lực lãnh đạo", "sáng kiến", "quyết đoán", "bản lĩnh khởi xướng"),
                "Sở hữu năng khiếu bẩm sinh về khởi xướng và dẫn dắt, có khả năng biến ý tưởng mới thành "
                        + "hiện thực bằng chính nghị lực của bản thân. Đây là tài năng thiên về việc đi đầu, "
                        + "mở đường cho người khác đi theo.",
                Polarity.SUPPORT);
        put(m, NumerologyNumberType.EXPRESSION, 2,
                List.of("khả năng ngoại giao", "lắng nghe", "làm việc nhóm", "tinh tế"),
                "Có tài năng tự nhiên trong việc dung hòa các quan điểm khác biệt và làm việc ăn ý cùng "
                        + "người khác, phù hợp với những vai trò cần sự tinh tế, kiên nhẫn và khéo léo trong "
                        + "giao tiếp.",
                Polarity.SUPPORT);
        put(m, NumerologyNumberType.EXPRESSION, 3,
                List.of("tài ăn nói", "óc sáng tạo", "khiếu nghệ thuật", "thu hút"),
                "Sở hữu năng khiếu giao tiếp và sáng tạo nổi bật, có khả năng diễn đạt ý tưởng một cách "
                        + "sinh động, cuốn hút và dễ dàng chạm đến cảm xúc người nghe.",
                Polarity.SUPPORT);
        put(m, NumerologyNumberType.EXPRESSION, 4,
                List.of("năng lực tổ chức", "tính hệ thống", "đáng tin cậy", "bền bỉ"),
                "Có tài năng xây dựng cấu trúc vững chắc và làm việc có hệ thống, là kiểu năng lực được "
                        + "tin cậy để hiện thực hóa những kế hoạch dài hạn một cách chắc chắn, từng bước.",
                Polarity.SUPPORT);
        put(m, NumerologyNumberType.EXPRESSION, 5,
                List.of("linh hoạt", "thích nghi nhanh", "đa năng", "ham học hỏi"),
                "Có tài năng thích nghi nhanh với hoàn cảnh mới và xử lý tốt nhiều lĩnh vực khác nhau, "
                        + "phù hợp với những công việc đòi hỏi sự đa dạng, di chuyển và không ngừng đổi mới.",
                Polarity.SUPPORT);
        put(m, NumerologyNumberType.EXPRESSION, 6,
                List.of("khả năng chăm sóc", "tổ chức gia đình/cộng đồng", "hòa giải", "thẩm mỹ"),
                "Có tài năng tự nhiên trong việc chăm sóc, nuôi dưỡng và tạo ra sự hài hòa cho những người "
                        + "xung quanh, phù hợp với các vai trò liên quan đến giáo dục, y tế, thẩm mỹ hoặc quản "
                        + "lý gia đình.",
                Polarity.SUPPORT);
        put(m, NumerologyNumberType.EXPRESSION, 7,
                List.of("tư duy phân tích", "nghiên cứu", "chuyên môn sâu", "trực giác"),
                "Có năng khiếu phân tích sắc bén và khả năng đào sâu vào một lĩnh vực chuyên môn, phù hợp "
                        + "với công việc đòi hỏi nghiên cứu, tư duy độc lập và sự chính xác.",
                Polarity.SUPPORT);
        put(m, NumerologyNumberType.EXPRESSION, 8,
                List.of("năng lực quản trị", "tầm nhìn kinh doanh", "quyết đoán", "hiệu quả"),
                "Có tài năng bẩm sinh trong việc tổ chức nguồn lực, ra quyết định lớn và theo đuổi hiệu quả, "
                        + "phù hợp với vai trò lãnh đạo doanh nghiệp hoặc quản lý tài chính.",
                Polarity.SUPPORT);
        put(m, NumerologyNumberType.EXPRESSION, 9,
                List.of("tài năng nhân văn", "tầm nhìn rộng", "khả năng truyền cảm hứng"),
                "Có năng khiếu kết nối với những vấn đề lớn của nhân loại và truyền cảm hứng cho người khác, "
                        + "phù hợp với các lĩnh vực nghệ thuật, giáo dục hoặc hoạt động vì cộng đồng.",
                Polarity.SUPPORT);
        put(m, NumerologyNumberType.EXPRESSION, 11,
                List.of("tài năng truyền cảm hứng", "trực giác sắc bén", "sáng tạo tâm linh"),
                "Sở hữu năng khiếu đặc biệt trong việc truyền cảm hứng và khơi gợi nhận thức mới ở người "
                        + "khác thông qua trực giác nhạy bén, một dạng tài năng thiên về ý tưởng và tầm nhìn "
                        + "hơn là chi tiết thực tế.",
                Polarity.SUPPORT);
        put(m, NumerologyNumberType.EXPRESSION, 22,
                List.of("năng lực xây dựng quy mô lớn", "thực thi tầm nhìn", "tổ chức phi thường"),
                "Có tài năng hiếm có trong việc kết hợp tầm nhìn lớn với khả năng tổ chức thực tế, đủ sức "
                        + "triển khai những dự án quy mô lớn mà ít người khác làm được.",
                Polarity.SUPPORT);
        put(m, NumerologyNumberType.EXPRESSION, 33,
                List.of("khả năng chữa lành", "giảng dạy truyền cảm hứng", "nuôi dưỡng tinh thần"),
                "Sở hữu năng khiếu đặc biệt trong việc nuôi dưỡng, giảng dạy và chữa lành cho người khác "
                        + "ở quy mô vượt xa gia đình hay bạn bè thân thiết, phù hợp với sứ mệnh phụng sự cộng "
                        + "đồng rộng lớn.",
                Polarity.SUPPORT);

        // =====================================================================
        // SOUL_URGE - dong luc noi tam, dieu khao khat sau xa nhat.
        // =====================================================================
        put(m, NumerologyNumberType.SOUL_URGE, 1,
                List.of("khao khát dẫn đầu", "cần được công nhận", "ngại phụ thuộc"),
                "Trong sâu thẳm, khao khát được là người đầu tiên, được tự quyết định và được công nhận "
                        + "vì năng lực riêng, đến mức đôi khi khó chấp nhận chia sẻ quyền kiểm soát hay dựa "
                        + "vào người khác.",
                Polarity.CAUTION);
        put(m, NumerologyNumberType.SOUL_URGE, 2,
                List.of("khao khát hòa hợp", "cần được yêu thương", "gắn bó"),
                "Điều mong muốn sâu xa nhất là được sống trong hòa hợp, được yêu thương và được là một phần "
                        + "không thể thiếu của một mối quan hệ hay tập thể gắn bó.",
                Polarity.SUPPORT);
        put(m, NumerologyNumberType.SOUL_URGE, 3,
                List.of("khao khát được thể hiện", "cần niềm vui", "tìm kiếm sự công nhận sáng tạo"),
                "Trong lòng luôn khao khát được tự do thể hiện bản thân, được vui chơi, sáng tạo và được "
                        + "công nhận vì sự độc đáo trong cách nhìn cuộc sống của mình.",
                Polarity.SUPPORT);
        put(m, NumerologyNumberType.SOUL_URGE, 4,
                List.of("khao khát ổn định", "cần trật tự", "an toàn"),
                "Sâu thẳm bên trong mong muốn một cuộc sống ổn định, có trật tự và an toàn, nơi mọi thứ "
                        + "đều rõ ràng và có thể kiểm soát được.",
                Polarity.SUPPORT);
        put(m, NumerologyNumberType.SOUL_URGE, 5,
                List.of("khao khát tự do", "sợ bị ràng buộc", "tìm kiếm kích thích mới"),
                "Mong muốn sâu xa nhất là được tự do trải nghiệm và không bị ràng buộc, điều này đôi khi "
                        + "khiến việc cam kết lâu dài với một người, một nơi chốn hay một lựa chọn trở nên "
                        + "khó khăn.",
                Polarity.CAUTION);
        put(m, NumerologyNumberType.SOUL_URGE, 6,
                List.of("khao khát được cần đến", "yêu thương", "đôi khi ôm đồm"),
                "Sâu thẳm mong muốn được yêu thương, được chăm sóc người khác và được cảm thấy mình có ích, "
                        + "nhưng nhu cầu này đôi khi khiến bản thân ôm đồm trách nhiệm không thuộc về mình.",
                Polarity.CAUTION);
        put(m, NumerologyNumberType.SOUL_URGE, 7,
                List.of("khao khát hiểu biết", "cần không gian riêng", "tìm kiếm ý nghĩa"),
                "Mong muốn sâu xa nhất là được yên tĩnh suy ngẫm, tìm hiểu ý nghĩa sâu xa của cuộc sống, "
                        + "hơn là chạy theo những giá trị bề nổi hay đám đông.",
                Polarity.NEUTRAL);
        put(m, NumerologyNumberType.SOUL_URGE, 8,
                List.of("khao khát quyền lực", "cần thành công", "đề cao địa vị"),
                "Sâu thẳm khao khát đạt được thành công, quyền lực và sự công nhận về mặt vật chất, đôi khi "
                        + "khiến giá trị bản thân bị gắn quá chặt với tiền bạc hay địa vị xã hội.",
                Polarity.CAUTION);
        put(m, NumerologyNumberType.SOUL_URGE, 9,
                List.of("khao khát cho đi", "lý tưởng hóa", "dễ hy sinh bản thân"),
                "Mong muốn sâu xa nhất là được yêu thương và phụng sự một điều gì đó lớn lao hơn bản thân "
                        + "mình, nhưng lý tưởng cao đẹp ấy đôi khi khiến người này quên chăm sóc nhu cầu của "
                        + "chính mình.",
                Polarity.CAUTION);
        put(m, NumerologyNumberType.SOUL_URGE, 11,
                List.of("khao khát giác ngộ", "nhạy cảm cao độ", "dễ hoài nghi bản thân"),
                "Trong lòng khao khát đạt đến một sự thật hay lý tưởng cao hơn, nhưng độ nhạy cảm và cường "
                        + "độ cảm xúc lớn cũng khiến người này dễ dao động, hoài nghi chính khả năng của mình.",
                Polarity.CAUTION);
        put(m, NumerologyNumberType.SOUL_URGE, 22,
                List.of("khao khát để lại di sản", "áp lực tự đặt ra", "cầu toàn"),
                "Sâu thẳm khao khát tạo ra điều gì đó to lớn và bền vững cho đời, nhưng chính kỳ vọng khổng "
                        + "lồ ấy đôi khi trở thành áp lực nặng nề tự mình đặt lên vai.",
                Polarity.CAUTION);
        put(m, NumerologyNumberType.SOUL_URGE, 33,
                List.of("khao khát cứu giúp", "yêu thương vô bờ", "dễ quên nhu cầu bản thân"),
                "Mong muốn sâu xa nhất là được yêu thương và chữa lành cho thế giới xung quanh, nhưng lòng "
                        + "trắc ẩn lớn lao ấy dễ khiến người này hy sinh quá mức nhu cầu và giới hạn của "
                        + "chính mình.",
                Polarity.CAUTION);

        // =====================================================================
        // PERSONALITY - an tuong ben ngoai, chiec mat na xa hoi.
        // =====================================================================
        put(m, NumerologyNumberType.PERSONALITY, 1,
                List.of("ấn tượng mạnh mẽ", "tự tin", "có phần xa cách", "uy quyền"),
                "Gây ấn tượng ban đầu là một người tự tin, quyết đoán và có chủ kiến rõ ràng, nhưng đôi "
                        + "khi vẻ ngoài ấy lại khiến người khác cảm thấy xa cách hoặc bị lấn át.",
                Polarity.CAUTION);
        put(m, NumerologyNumberType.PERSONALITY, 2,
                List.of("dịu dàng", "dễ gần", "biết lắng nghe", "khiêm nhường"),
                "Toát lên vẻ ngoài nhẹ nhàng, dễ gần và biết lắng nghe, khiến người đối diện nhanh chóng "
                        + "cảm thấy an toàn và được thấu hiểu khi tiếp xúc.",
                Polarity.SUPPORT);
        put(m, NumerologyNumberType.PERSONALITY, 3,
                List.of("duyên dáng", "hài hước", "cuốn hút", "hoạt bát"),
                "Gây thiện cảm ngay từ lần gặp đầu nhờ sự hài hước, duyên dáng và tinh thần lạc quan dễ "
                        + "lan tỏa, khiến bầu không khí xung quanh trở nên nhẹ nhàng, sôi nổi hơn.",
                Polarity.SUPPORT);
        put(m, NumerologyNumberType.PERSONALITY, 4,
                List.of("nghiêm túc", "chỉn chu", "có phần cứng nhắc", "thực dụng"),
                "Tạo ấn tượng là người nghiêm túc, đáng tin cậy và chỉn chu, nhưng vẻ ngoài thực dụng và "
                        + "nguyên tắc đôi khi khiến người khác cảm thấy khó gần hoặc thiếu sự linh hoạt.",
                Polarity.CAUTION);
        put(m, NumerologyNumberType.PERSONALITY, 5,
                List.of("năng động", "cuốn hút", "phóng khoáng", "thú vị"),
                "Toát lên vẻ năng động, phóng khoáng và đầy sức hút, khiến người xung quanh cảm thấy thú "
                        + "vị và bị lôi cuốn bởi tinh thần tự do, không ngại thử điều mới.",
                Polarity.SUPPORT);
        put(m, NumerologyNumberType.PERSONALITY, 6,
                List.of("ấm áp", "chu đáo", "đáng tin", "có trách nhiệm"),
                "Gây ấn tượng là một người ấm áp, chu đáo và đáng tin cậy, khiến người khác cảm thấy được "
                        + "quan tâm và an tâm khi ở gần.",
                Polarity.SUPPORT);
        put(m, NumerologyNumberType.PERSONALITY, 7,
                List.of("bí ẩn", "trầm lặng", "có phần xa cách", "sâu sắc"),
                "Toát lên vẻ trầm lặng, bí ẩn và sâu sắc, thu hút những ai thích sự tinh tế, nhưng cũng dễ "
                        + "khiến người mới gặp cảm thấy khó tiếp cận hoặc lạnh lùng.",
                Polarity.CAUTION);
        put(m, NumerologyNumberType.PERSONALITY, 8,
                List.of("uy quyền", "mạnh mẽ", "có phần áp đảo", "thực dụng"),
                "Toát lên khí chất mạnh mẽ, quyền lực và thành đạt ngay từ ấn tượng đầu tiên, nhưng vẻ "
                        + "ngoài đầy uy lực này đôi khi khiến người khác cảm thấy bị áp đảo hoặc coi là "
                        + "thực dụng.",
                Polarity.CAUTION);
        put(m, NumerologyNumberType.PERSONALITY, 9,
                List.of("rộng lượng", "ấm áp", "có sức hút", "từ bi"),
                "Gây ấn tượng là một người rộng lượng, ấm áp và có tầm nhìn rộng, khiến người khác cảm "
                        + "nhận được sự bao dung và trưởng thành trong cách ứng xử.",
                Polarity.SUPPORT);
        put(m, NumerologyNumberType.PERSONALITY, 11,
                List.of("cuốn hút đặc biệt", "truyền cảm hứng", "khí chất khác biệt"),
                "Toát lên khí chất đặc biệt, vừa cuốn hút vừa có phần khó nắm bắt, khiến người khác cảm "
                        + "nhận được nguồn năng lượng truyền cảm hứng mạnh mẽ ngay từ lần gặp đầu tiên.",
                Polarity.SUPPORT);
        put(m, NumerologyNumberType.PERSONALITY, 22,
                List.of("đĩnh đạc", "đáng tin cậy", "tầm vóc lớn", "điềm tĩnh"),
                "Gây ấn tượng là một người điềm tĩnh, đĩnh đạc và có tầm vóc vượt trội, khiến người khác "
                        + "tin tưởng giao phó những việc lớn và quan trọng.",
                Polarity.SUPPORT);
        put(m, NumerologyNumberType.PERSONALITY, 33,
                List.of("ấm áp sâu sắc", "che chở", "đầy lòng trắc ẩn"),
                "Toát lên sự ấm áp và lòng trắc ẩn sâu sắc hiếm có, khiến người khác cảm thấy được che chở "
                        + "và thấu hiểu ngay cả trong những cuộc gặp gỡ ngắn ngủi.",
                Polarity.SUPPORT);

        // =====================================================================
        // BIRTHDAY - mot nang khieu/anh huong phu, lop bo sung cho Life Path.
        // =====================================================================
        put(m, NumerologyNumberType.BIRTHDAY, 1,
                List.of("tài lãnh đạo nhỏ", "chủ động", "sáng kiến cá nhân"),
                "Mang thêm một năng khiếu phụ về sự chủ động và khả năng khởi xướng, giúp bổ sung tinh "
                        + "thần tiên phong cho con đường đời chính, dù ảnh hưởng này chỉ ở mức độ hỗ trợ.",
                Polarity.SUPPORT);
        put(m, NumerologyNumberType.BIRTHDAY, 2,
                List.of("nhạy cảm", "dễ tổn thương", "cần sự công nhận nhẹ nhàng"),
                "Mang thêm một tầng nhạy cảm và tinh tế trong cách ứng xử, đôi khi khiến người này dễ bị "
                        + "tổn thương trước những lời nhận xét hoặc xung đột nhỏ hơn người khác.",
                Polarity.CAUTION);
        put(m, NumerologyNumberType.BIRTHDAY, 3,
                List.of("khiếu ăn nói nhỏ", "óc hài hước", "khả năng biểu đạt phụ"),
                "Mang thêm chút khiếu ăn nói và óc hài hước làm điểm nhấn cho tính cách, một tài lẻ dễ "
                        + "chịu nhưng không phải yếu tố quyết định hướng đi lớn của cuộc đời.",
                Polarity.NEUTRAL);
        put(m, NumerologyNumberType.BIRTHDAY, 4,
                List.of("tính kỷ luật phụ", "thận trọng", "đôi khi cứng nhắc"),
                "Bổ sung thêm khuynh hướng thận trọng và kỷ luật vào tính cách chung, nhưng cũng có thể "
                        + "khiến người này dễ trở nên cứng nhắc hoặc ngại thay đổi hơn trong những việc nhỏ.",
                Polarity.CAUTION);
        put(m, NumerologyNumberType.BIRTHDAY, 5,
                List.of("thích thay đổi nhỏ", "linh hoạt", "tò mò"),
                "Mang thêm chút tinh thần ham thay đổi và tò mò với cái mới, một nét điểm xuyết dễ chịu "
                        + "cho tính cách nhưng ảnh hưởng không quá sâu đến định hướng chung của cuộc đời.",
                Polarity.NEUTRAL);
        put(m, NumerologyNumberType.BIRTHDAY, 6,
                List.of("khiếu chăm sóc nhỏ", "tinh thần trách nhiệm", "thẩm mỹ"),
                "Bổ sung thêm sự chu đáo và tinh thần trách nhiệm với những người xung quanh, một tài lẻ "
                        + "dễ mến dù chỉ đóng vai trò hỗ trợ cho con đường đời chính.",
                Polarity.SUPPORT);
        put(m, NumerologyNumberType.BIRTHDAY, 7,
                List.of("thiên hướng suy tư", "cần riêng tư", "phân tích tỉ mỉ"),
                "Mang thêm khuynh hướng thích suy tư và cần không gian riêng tư hơn để nạp lại năng lượng, "
                        + "có thể khiến người này đôi lúc khó hòa nhập ngay với môi trường đông người.",
                Polarity.CAUTION);
        put(m, NumerologyNumberType.BIRTHDAY, 8,
                List.of("khiếu quản lý nhỏ", "nhạy bén tài chính", "tham vọng phụ"),
                "Bổ sung thêm sự nhạy bén với tiền bạc và tổ chức công việc, một năng khiếu hỗ trợ hữu ích "
                        + "cho việc theo đuổi mục tiêu vật chất, dù chỉ ở tầm ảnh hưởng thứ yếu.",
                Polarity.SUPPORT);
        put(m, NumerologyNumberType.BIRTHDAY, 9,
                List.of("lòng trắc ẩn nhỏ", "hướng đến người khác", "bao dung"),
                "Mang thêm chút thiên hướng quan tâm đến người khác và lòng bao dung, một nét tính cách "
                        + "dễ chịu nhưng chỉ là yếu tố bổ trợ bên cạnh con đường đời chính.",
                Polarity.NEUTRAL);
        put(m, NumerologyNumberType.BIRTHDAY, 11,
                List.of("nhạy cảm cao", "trực giác phụ", "dễ căng thẳng"),
                "Mang thêm một lớp nhạy cảm và trực giác vượt trội vào tính cách chung, nhưng cường độ "
                        + "cảm xúc này cũng có thể khiến người này dễ căng thẳng hơn trước áp lực nhỏ.",
                Polarity.CAUTION);
        put(m, NumerologyNumberType.BIRTHDAY, 22,
                List.of("tiềm năng lớn phụ", "áp lực hoàn thiện", "tổ chức quy mô nhỏ"),
                "Mang thêm tiềm năng tổ chức và thực thi ở quy mô lớn hơn bình thường vào tính cách, nhưng "
                        + "cũng có thể tạo áp lực vô hình khiến người này luôn cảm thấy chưa đủ ngay cả với "
                        + "những việc nhỏ.",
                Polarity.CAUTION);
        put(m, NumerologyNumberType.BIRTHDAY, 33,
                List.of("thiên hướng chăm sóc lớn", "dễ ôm đồm", "trắc ẩn phụ"),
                "Mang thêm một thiên hướng chăm sóc và che chở người khác vượt mức bình thường, nhưng "
                        + "cũng dễ khiến người này ôm đồm gánh nặng cảm xúc không phải của mình.",
                Polarity.CAUTION);

        return m;
    }

    private static void put(Map<String, NumerologyNumberMeaning> m, NumerologyNumberType type, int value,
            List<String> keywords, String meaning, Polarity polarity) {
        m.put(key(type, value), new NumerologyNumberMeaning(keywords, meaning, polarity));
    }
}
