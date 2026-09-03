package io.destinyos.engines.astrology;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Vietnamese prose for the twelve signs, the Ascendant, and the twelve Whole
 * Sign houses.
 *
 * <p>Transcribed from {@code docs/content/NARRATIVE_CONTENT_DRAFT.md} §B1–§B3,
 * a reviewed content draft written 2026-08-25 and never wired in. Until it
 * landed, this engine produced a correct chart — Sun, Midheaven, Ascendant and
 * twelve houses — and said <strong>nothing</strong> about what any of it means,
 * which is what makes a result page read as a table of coordinates.
 *
 * <p><strong>What is not here, and must not be added.</strong> Nothing about
 * the Moon, the seven remaining planets, or aspects between them. Those are not
 * computed at all (research items R5 and R6), so writing meanings for them
 * would be Rule C — content describing a calculation that does not exist. They
 * are already reported as blocked sections on the chart.
 *
 * <p><strong>The Midheaven has no authored meaning</strong> and gets none. §B's
 * header names it, but §B1 and §B2 author text only for the Sun and the
 * Ascendant. Writing an MC paragraph to fill the gap would be inventing
 * content, so the chart carries its sign meaning and an explicit statement that
 * no point meaning was authored.
 *
 * <p><strong>House themes carry their house system.</strong> §B3's own source
 * paragraph requires it: Placidus — the more common system in other software —
 * divides houses differently, so a theme stated without naming the system is a
 * claim about a chart the reader may not have. That is also why this content
 * lives here rather than in the label registry, which cannot carry a
 * qualifier: {@code VietnameseLabels} deliberately keeps houses as "Nhà 10",
 * an identity, not a reading.
 */
public final class AstrologyMeanings {

    /** Bumped whenever the wording below changes, independently of code version. */
    public static final String CONTENT_VERSION = "1.0";

    public static final String HOUSE_SYSTEM_NOTE_VI =
            "Chủ đề mười hai Nhà dưới đây tính theo hệ nhà Whole Sign (hệ đang dùng). "
                    + "Phần mềm khác thường dùng hệ Placidus và chia nhà khác đi, nên cùng một "
                    + "lá số có thể cho ra số nhà khác.";

    public static final String SOURCE_NOTE_VI =
            "Từ khóa mười hai cung và chủ đề mười hai Nhà là kiến thức nền phổ biến của chiêm "
                    + "tinh học Tropical hiện đại, không gắn với một tác giả hay trường phái đang "
                    + "tranh chấp. Đây là ý nghĩa khái quát của một cung hay một nhà, không phải "
                    + "phán đoán cho riêng lá số của bạn.";

    /** @param keywordsVi từ khóa chung của cung, theo chiêm tinh Tropical hiện đại */
    public record SignMeaning(List<String> keywordsVi) {
    }

    private static final Map<ZodiacSign, SignMeaning> SIGNS = buildSigns();
    private static final Map<AstrologicalHouse, String> HOUSE_THEMES = buildHouses();

    /**
     * Ý nghĩa khái quát của vị trí Mặt Trời (§B1).
     */
    public static final String SUN_MEANING_VI =
            "Vị trí Mặt Trời đại diện cho bản sắc cốt lõi, ý chí và cách một người thể hiện bản "
                    + "thân ra thế giới — theo chiêm tinh học hiện đại phương Tây (hoàng đạo "
                    + "Tropical), không phải một phát biểu khoa học đã được kiểm chứng.";

    /**
     * Ý nghĩa khái quát của Cung Mọc (§B2).
     */
    public static final String ASCENDANT_MEANING_VI =
            "Cung Mọc là dấu hiệu hoàng đạo mọc lên ở đường chân trời đúng thời điểm sinh — đại "
                    + "diện cho ấn tượng ban đầu một người tạo ra và cách họ thích nghi với môi "
                    + "trường xung quanh, khác với Mặt Trời là bản sắc cốt lõi bên trong. Vì Cung "
                    + "Mọc dịch chuyển khoảng 1 độ mỗi 4 phút, nó chỉ tính được chính xác khi có "
                    + "giờ sinh.";

    private AstrologyMeanings() {
    }

    public static SignMeaning ofSign(ZodiacSign sign) {
        return SIGNS.get(sign);
    }

    public static String themeOf(AstrologicalHouse house) {
        return HOUSE_THEMES.get(house);
    }

    public static Map<ZodiacSign, SignMeaning> allSigns() {
        return SIGNS;
    }

    public static Map<AstrologicalHouse, String> allHouseThemes() {
        return HOUSE_THEMES;
    }

    private static Map<ZodiacSign, SignMeaning> buildSigns() {
        var map = new EnumMap<ZodiacSign, SignMeaning>(ZodiacSign.class);
        map.put(ZodiacSign.ARIES, new SignMeaning(
                List.of("khởi đầu", "quyết đoán", "tiên phong", "thích thử thách")));
        map.put(ZodiacSign.TAURUS, new SignMeaning(
                List.of("ổn định", "kiên nhẫn", "coi trọng sự bền vững", "cần cảm giác an toàn")));
        map.put(ZodiacSign.GEMINI, new SignMeaning(
                List.of("linh hoạt", "ham học hỏi", "giỏi giao tiếp", "kết nối thông tin")));
        map.put(ZodiacSign.CANCER, new SignMeaning(
                List.of("giàu tình cảm", "che chở", "gắn bó với gia đình", "coi trọng ký ức")));
        map.put(ZodiacSign.LEO, new SignMeaning(
                List.of("tự tin", "hào phóng", "thích được công nhận", "thể hiện bản thân")));
        map.put(ZodiacSign.VIRGO, new SignMeaning(
                List.of("tỉ mỉ", "thực tế", "coi trọng sự hoàn thiện", "chú ý chi tiết")));
        map.put(ZodiacSign.LIBRA, new SignMeaning(
                List.of("hài hòa", "công bằng", "coi trọng quan hệ", "tìm sự cân bằng")));
        map.put(ZodiacSign.SCORPIO, new SignMeaning(
                List.of("sâu sắc", "mãnh liệt", "nhìn thấu bản chất vấn đề")));
        map.put(ZodiacSign.SAGITTARIUS, new SignMeaning(
                List.of("phóng khoáng", "ham khám phá", "hướng tới ý nghĩa lớn hơn")));
        map.put(ZodiacSign.CAPRICORN, new SignMeaning(
                List.of("kỷ luật", "kiên trì", "coi trọng thành quả lâu dài")));
        map.put(ZodiacSign.AQUARIUS, new SignMeaning(
                List.of("độc lập", "sáng tạo", "hướng tới cái mới", "quan tâm cộng đồng rộng")));
        map.put(ZodiacSign.PISCES, new SignMeaning(
                List.of("nhạy cảm", "giàu trí tưởng tượng", "dễ đồng cảm với người khác")));

        if (map.size() != ZodiacSign.values().length) {
            throw new IllegalStateException("Thiếu từ khóa cho "
                    + (ZodiacSign.values().length - map.size()) + " cung hoàng đạo.");
        }
        return Map.copyOf(map);
    }

    private static Map<AstrologicalHouse, String> buildHouses() {
        var map = new EnumMap<AstrologicalHouse, String>(AstrologicalHouse.class);
        var themes = List.of(
                "Bản thân, ngoại hình, cách bước vào cuộc sống",
                "Tài sản, giá trị vật chất, cách kiếm và giữ của cải",
                "Giao tiếp, học hỏi thường ngày, anh chị em, môi trường gần",
                "Gia đình, gốc rễ, cảm giác thuộc về, nền tảng nội tâm",
                "Sáng tạo, tình yêu lãng mạn, con cái, niềm vui tự thể hiện",
                "Công việc thường nhật, sức khỏe, thói quen, sự phục vụ",
                "Quan hệ đối tác, hôn nhân, hợp tác và cả đối thủ",
                "Chuyển hóa sâu sắc, tài sản chung, những gì được chia sẻ hoặc thừa kế",
                "Mở rộng tầm nhìn, triết lý sống, du hành xa, học vấn cao",
                "Sự nghiệp, danh tiếng, vị trí trong xã hội",
                "Bạn bè, cộng đồng, mục tiêu dài hạn, mạng lưới quan hệ",
                "Nội tâm sâu kín, tiềm thức, những gì diễn ra trong lặng lẽ");

        AstrologicalHouse[] houses = AstrologicalHouse.values();
        if (houses.length != themes.size()) {
            throw new IllegalStateException("Số Nhà (" + houses.length
                    + ") không khớp số chủ đề đã soạn (" + themes.size() + ").");
        }
        // Ghép theo `number()` chứ không theo thứ tự khai báo của enum: thứ tự
        // khai báo có thể đổi mà không ai để ý, và ghép lệch một ô sẽ gán chủ
        // đề sự nghiệp cho Nhà 9 trong khi mọi test có mặt vẫn xanh.
        for (AstrologicalHouse house : houses) {
            map.put(house, themes.get(house.number() - 1));
        }
        return Map.copyOf(map);
    }
}
