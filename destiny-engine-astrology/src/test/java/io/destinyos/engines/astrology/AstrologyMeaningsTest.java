package io.destinyos.engines.astrology;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Nội dung diễn giải chiêm tinh (bản thảo §B1–§B3).
 *
 * <p>Không dùng {@code isNotBlank()} làm bài kiểm tra chính. Dự án này đã một
 * lần có 287 trên 386 mục nội dung sai trong khi mọi test có-mặt đều xanh, nên
 * chỗ nào suy ra được thì suy ra: chủ đề Nhà phải khớp số nhà, và ranh giới nội
 * dung phải được canh bằng test chứ không bằng lời hứa.
 */
class AstrologyMeaningsTest {

    @Test
    @DisplayName("Đủ mười hai cung hoàng đạo")
    void everySignHasKeywords() {
        assertThat(AstrologyMeanings.allSigns())
                .hasSize(ZodiacSign.values().length)
                .containsOnlyKeys(ZodiacSign.values());
        for (ZodiacSign sign : ZodiacSign.values()) {
            assertThat(AstrologyMeanings.ofSign(sign).keywordsVi())
                    .as("Cung %s phải có từ khóa", sign)
                    .isNotEmpty();
        }
    }

    @Test
    @DisplayName("Đủ mười hai Nhà, và chủ đề gắn đúng số nhà")
    void houseThemesAreAlignedToTheirNumber() {
        assertThat(AstrologyMeanings.allHouseThemes())
                .hasSize(AstrologicalHouse.values().length)
                .containsOnlyKeys(AstrologicalHouse.values());

        // Bài test suy ra, không phải bài test có mặt. Lệch một ô sẽ gán chủ đề
        // sự nghiệp cho Nhà 9 và chủ đề du hành cho Nhà 10 - hai câu hoàn toàn
        // trôi chảy, cùng sai. Neo vào ba nhà có chủ đề đặc trưng nhất.
        assertThat(AstrologyMeanings.themeOf(AstrologicalHouse.HOUSE_1)).contains("Bản thân");
        assertThat(AstrologyMeanings.themeOf(AstrologicalHouse.HOUSE_7)).contains("đối tác");
        assertThat(AstrologyMeanings.themeOf(AstrologicalHouse.HOUSE_10)).contains("Sự nghiệp");
    }

    @Test
    @DisplayName("Không soạn nội dung cho Mặt Trăng, hành tinh hay góc chiếu")
    void saysNothingAboutUncomputedBodies() {
        // R5 và R6 còn mở: Mặt Trăng, bảy hành tinh còn lại và các góc chiếu
        // chưa được tính. Viết nội dung cho chúng là mô tả một phép tính không
        // tồn tại - đúng thứ Rule C cấm.
        List<String> forbidden = List.of(
                "mặt trăng", "sao thủy", "sao kim", "sao hỏa", "sao mộc", "sao thổ",
                "thiên vương", "hải vương", "diêm vương", "góc chiếu", "aspect",
                "hợp chiếu", "đối đỉnh", "tam hợp", "lục hợp");

        String all = corpus().toLowerCase(Locale.ROOT);
        for (String phrase : forbidden) {
            assertThat(all)
                    .as("Nội dung chiêm tinh chưa được phép nhắc tới '%s'", phrase)
                    .doesNotContain(phrase);
        }
    }

    @Test
    @DisplayName("Không hứa hẹn, không chẩn đoán, không con số xác suất")
    void noPromisesNoDiagnosisNoProbability() {
        List<String> forbidden = List.of(
                "chắc chắn", "bảo đảm", "đảm bảo", "sẽ khiến bạn", "chẩn đoán",
                "ly hôn", "tử vong", "phá sản", "%");
        String all = corpus().toLowerCase(Locale.ROOT);
        for (String phrase : forbidden) {
            assertThat(all)
                    .as("Nội dung chiêm tinh không được chứa '%s'", phrase)
                    .doesNotContain(phrase);
        }
    }

    @Test
    @DisplayName("Chủ đề mười hai Nhà luôn đi kèm tên hệ chia nhà")
    void houseThemesCarryTheirHouseSystem() {
        // §B3 yêu cầu: Placidus chia nhà khác đi, nên một chủ đề nêu ra mà
        // không nói hệ nào là một khẳng định về lá số mà người đọc có thể
        // không có.
        assertThat(AstrologyMeanings.HOUSE_SYSTEM_NOTE_VI)
                .contains("Whole Sign")
                .contains("Placidus");
    }

    private static String corpus() {
        var sb = new StringBuilder();
        AstrologyMeanings.allSigns().values()
                .forEach(m -> sb.append(String.join(" ", m.keywordsVi())).append(' '));
        AstrologyMeanings.allHouseThemes().values().forEach(t -> sb.append(t).append(' '));
        sb.append(AstrologyMeanings.SUN_MEANING_VI).append(' ');
        sb.append(AstrologyMeanings.ASCENDANT_MEANING_VI).append(' ');
        sb.append(AstrologyMeanings.SOURCE_NOTE_VI);
        return sb.toString();
    }
}
