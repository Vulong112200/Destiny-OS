package io.destinyos.engines.fengshui;

import io.destinyos.core.signal.Dimension;
import io.destinyos.core.signal.Polarity;
import io.destinyos.core.signal.Strength;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Which life areas each Bát Trạch relation speaks to, and how strongly.
 *
 * <p><strong>Authored reference data, grounded in cited descriptions</strong> —
 * the same discipline {@code TarotCardMeanings} and
 * {@code NumerologyNumberMeanings} already follow (research items R11, R8):
 * written once as versioned static data, never generated at run time
 * (CLAUDE.md Rule B). What is authored here is only the mapping from prose to
 * {@link Dimension}; the polarity and the strength are <em>not</em> authored at
 * all — they are read off the tradition's own cát/hung classification and its
 * thượng/trung/tiểu ranking, which {@link BatTrachRelation} records.
 *
 * <p>Sources for the life-area descriptions (retrieved 2026-08-22):
 * {@code kasai.com.vn/sinh-khi-thien-y-dien-nien-phuc-vi-la-gi.html},
 * {@code xaydung365.com.vn/y-nghia-8-cung-trong-phong-thuy-bat-trach-n326.html}
 * and {@code nguyenthehoa.com/y-nghia-cung-can-tho/}. The Vietnamese wording
 * each mapping rests on is quoted inline below, so a reader can check the
 * mapping rather than take it.
 *
 * <p><strong>What is deliberately absent:</strong> any prose for the user.
 * Evidence carries structured findings, not narrative (see {@code Evidence}),
 * and the wording a reader sees is the narrative layer's job. The strings below
 * are provenance for the mapping, not display text.
 */
final class BatTrachMeanings {

    /** Bumped whenever a mapping below changes, independently of code version. */
    static final String CONTENT_VERSION = "1.0";

    private static final Map<BatTrachRelation, Set<Dimension>> DIMENSIONS = build();

    private BatTrachMeanings() {
    }

    private static Map<BatTrachRelation, Set<Dimension>> build() {
        Map<BatTrachRelation, Set<Dimension>> map = new EnumMap<>(BatTrachRelation.class);

        // "khí tốt ... mang lại danh tiếng, tạo ra sức sống dồi dào",
        // "thu hút tài lộc, thịnh vượng; tốt cho cửa chính, phòng làm việc"
        map.put(BatTrachRelation.SINH_KHI, Set.of(Dimension.FINANCE, Dimension.CAREER));

        // "tốt về sức khỏe, có lợi cho phụ nữ, vượng tài lộc, thường có quý
        // nhân phù trợ" - health first, wealth second.
        map.put(BatTrachRelation.THIEN_Y, Set.of(Dimension.HEALTH_REFLECTION, Dimension.FINANCE));

        // "mang lại sự hòa thuận, tốt cho sự nghiệp và các mối quan hệ vợ
        // chồng hòa thuận, tuổi thọ tăng lên"
        map.put(BatTrachRelation.DIEN_NIEN, Set.of(Dimension.RELATIONSHIP, Dimension.CAREER));

        // "mang lại sự bình yên, trấn tĩnh" - stability of the dwelling itself,
        // with no life-area claim beyond it. HOME alone, rather than padded out.
        map.put(BatTrachRelation.PHUC_VI, Set.of(Dimension.HOME));

        // "gây ra mâu thuẫn, tranh chấp và kiện tụng"
        map.put(BatTrachRelation.HOA_HAI, Set.of(Dimension.RELATIONSHIP, Dimension.DECISION));

        // "mang đến xung đột ... khiến chuyện tình cảm và phát triển sự nghiệp
        // trở nên khó khăn"
        map.put(BatTrachRelation.LUC_SAT, Set.of(Dimension.RELATIONSHIP, Dimension.CAREER));

        // "gây tai họa, kiện tụng, suy tài; sức khỏe phụ nữ kém"
        map.put(BatTrachRelation.NGU_QUY, Set.of(Dimension.FINANCE, Dimension.RELATIONSHIP));

        // "bệnh tật, tai nạn và tổn thất" - the strongest health-adjacent
        // reading in the set, which is why the label for HEALTH_REFLECTION says
        // "góc nhìn tham khảo": CLAUDE.md section 10 forbids diagnosis, and this
        // engine emits a polarity, never a prognosis.
        map.put(BatTrachRelation.TUYET_MENH, Set.of(Dimension.HEALTH_REFLECTION, Dimension.FINANCE));

        return Map.copyOf(map);
    }

    /** Never empty: every relation speaks to at least one life area. */
    static Set<Dimension> dimensionsOf(BatTrachRelation relation) {
        return DIMENSIONS.get(Objects.requireNonNull(relation, "relation"));
    }

    /**
     * Polarity read off the tradition's own classification, not assigned here.
     *
     * <p>The three-way split among the inauspicious relations matters: the
     * sources call Hoạ Hại "tiểu hung" and Lục Sát "thứ hung" but Ngũ Quỷ and
     * Tuyệt Mệnh "đại hung". Flattening all four to NEGATIVE would overstate
     * the first two, and flattening them to CAUTION would understate the last
     * two — {@code Polarity}'s own Javadoc is explicit that CAUTION is not the
     * same as NEGATIVE.
     */
    static Polarity polarityOf(BatTrachRelation relation) {
        if (relation.auspicious()) {
            return Polarity.SUPPORT;
        }
        return relation.rank() == BatTrachRelation.Rank.MAJOR
                ? Polarity.NEGATIVE
                : Polarity.CAUTION;
    }

    /** Magnitude from the tradition's thượng/trung/tiểu ranking. Never a score. */
    static Strength strengthOf(BatTrachRelation relation) {
        return switch (relation.rank()) {
            case MAJOR -> Strength.STRONG;
            case MEDIUM -> Strength.MEDIUM;
            case MINOR -> Strength.WEAK;
        };
    }

    /** All relations, so a coverage test can walk them. */
    static List<BatTrachRelation> covered() {
        return List.copyOf(DIMENSIONS.keySet());
    }
}
