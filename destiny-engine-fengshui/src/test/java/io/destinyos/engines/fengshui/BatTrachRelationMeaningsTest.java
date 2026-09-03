package io.destinyos.engines.fengshui;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Nội dung diễn giải tám quan hệ Bát Trạch (bản thảo §C2).
 *
 * <p>Các bài test ở đây cố tình <strong>không</strong> chỉ kiểm tra "chuỗi có
 * rỗng không". Dự án này đã bị đúng cái bẫy đó: một vòng xác minh trước từng
 * phát hiện 287 trên 386 mục hào từ mang nội dung sai trong khi mọi test
 * {@code isNotBlank()} đều xanh. Nên phần lớn nội dung ở dưới được kiểm bằng
 * cách đối chiếu với dữ liệu mà nó phải khớp, chứ không phải bằng sự có mặt.
 */
class BatTrachRelationMeaningsTest {

    @Test
    @DisplayName("Đủ tám quan hệ, không thiếu mục nào")
    void everyRelationHasAMeaning() {
        assertThat(BatTrachRelationMeanings.all())
                .hasSize(BatTrachRelation.values().length)
                .containsOnlyKeys(BatTrachRelation.values());
    }

    @Test
    @DisplayName("Nhóm cát/hung trong lời văn khớp với phân loại của chính enum")
    void natureMatchesTheTraditionalClassification() {
        // Đây là bài test suy ra, không phải bài test có mặt: nếu một câu văn
        // mô tả Ngũ Quỷ là "tốt" thì nó phải fail, dù câu đó dài và trôi chảy.
        for (BatTrachRelation relation : BatTrachRelation.values()) {
            var meaning = BatTrachRelationMeanings.of(relation);
            String nature = meaning.natureVi().toLowerCase(Locale.ROOT);
            if (relation.auspicious()) {
                assertThat(nature)
                        .as("Quan hệ cát %s phải được mô tả là thuận", relation)
                        .satisfiesAnyOf(
                                n -> assertThat(n).contains("tốt"),
                                n -> assertThat(n).contains("trung tính"));
                assertThat(nature)
                        .as("Quan hệ cát %s không được mô tả là không thuận", relation)
                        .doesNotContain("không thuận");
            } else {
                assertThat(nature)
                        .as("Quan hệ hung %s phải được mô tả là không thuận", relation)
                        .contains("không thuận");
            }
        }
    }

    @Test
    @DisplayName("Không câu nào hứa hẹn kết quả hay chẩn đoán sức khỏe")
    void noPromisesAndNoDiagnosis() {
        // CLAUDE.md §10: không chẩn đoán bệnh, không bảo đảm giàu nghèo, không
        // khẳng định chắc chắn về tương lai. Bản thảo cũng tự đặt ra ranh giới
        // "hướng này thuộc nhóm xu hướng…" chứ không phải "sẽ khiến bạn…".
        List<String> forbidden = List.of(
                "sẽ khiến bạn", "chắc chắn", "bảo đảm", "đảm bảo",
                "chữa", "chẩn đoán", "khỏi bệnh", "giàu có", "phá sản", "ly hôn", "tử vong");

        for (BatTrachRelation relation : BatTrachRelation.values()) {
            var meaning = BatTrachRelationMeanings.of(relation);
            String all = (meaning.natureVi() + " " + meaning.tendencyVi() + " "
                    + String.join(" ", meaning.domainsVi())).toLowerCase(Locale.ROOT);
            for (String phrase : forbidden) {
                assertThat(all)
                        .as("Diễn giải %s không được chứa '%s'", relation, phrase)
                        .doesNotContain(phrase);
            }
            assertThat(all).doesNotContain("%");
        }
    }

    @Test
    @DisplayName("Mỗi quan hệ nêu ít nhất một khía cạnh đời sống, và nêu bằng tiếng Việt")
    void everyRelationNamesAtLeastOneLifeDomain() {
        for (BatTrachRelation relation : BatTrachRelation.values()) {
            var meaning = BatTrachRelationMeanings.of(relation);
            assertThat(meaning.domainsVi())
                    .as("Quan hệ %s phải nêu khía cạnh đời sống", relation)
                    .isNotEmpty();
            assertThat(meaning.domainsVi())
                    .allSatisfy(d -> assertThat(d).doesNotMatch("^[A-Z_]+$"));
        }
    }

    @Test
    @DisplayName("Không nói gì về phòng, giường, bàn hay cửa — đó là mục nghiên cứu R26")
    void saysNothingAboutRoomsOrFurniture() {
        // Master Spec §20 xếp phòng/cửa/giường/bàn vào mục "Nâng cao" và trong
        // repo không có nguồn nào cho phần đó. Viết ra là vi phạm Rule C. Bài
        // test này giữ cho một lần "cải thiện nội dung" sau này không lặng lẽ
        // thêm vào.
        List<String> outOfScope = List.of(
                "giường", "ngủ", "bàn làm việc", "cửa chính", "bếp", "phòng thờ", "kê ");

        for (BatTrachRelation relation : BatTrachRelation.values()) {
            var meaning = BatTrachRelationMeanings.of(relation);
            String all = (meaning.natureVi() + " " + meaning.tendencyVi() + " "
                    + String.join(" ", meaning.domainsVi())).toLowerCase(Locale.ROOT);
            for (String phrase : outOfScope) {
                assertThat(all)
                        .as("Diễn giải %s chưa được phép nói về '%s' (R26 còn mở)", relation, phrase)
                        .doesNotContain(phrase);
            }
        }
    }
}
