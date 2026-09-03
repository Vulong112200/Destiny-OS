package io.destinyos.engines.fengshui;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Vietnamese prose for each of the eight Bát Trạch relations, for display.
 *
 * <p>Transcribed from {@code docs/content/NARRATIVE_CONTENT_DRAFT.md} §C2, a
 * reviewed content draft that had been sitting unwired since 2026-08-25. Until
 * it landed, {@code BatTrachCard} rendered a two-column table of direction →
 * relation badge and <strong>not one word</strong> explaining what Sinh Khí
 * means — which is exactly the complaint that "Đại cát / Đại hung" says no more
 * than a five-minute web search.
 *
 * <p><strong>Why this is a separate class from {@link BatTrachMeanings}.</strong>
 * That one feeds {@code polarityOf}, {@code strengthOf} and
 * {@code dimensionsOf}, which is to say it feeds <em>Signals</em>. Display
 * wording and signal-bearing data must not live in the same file, or a future
 * edit to a sentence could quietly change a polarity. Nothing here is ever read
 * by the signal path.
 *
 * <p><strong>Bounds, from the draft's own authoring rules.</strong> Every
 * sentence is a general tendency of the relation, never a prediction about the
 * reader: "hướng này thuộc nhóm xu hướng…", never "hướng này sẽ khiến bạn…".
 * The health-leaning relations get no diagnosis and no prognosis — CLAUDE.md
 * §10 forbids both, and this engine emits a polarity, never a prognosis.
 *
 * <p><strong>Not authored here:</strong> which room, door, bed or desk should
 * face which direction. Master Spec §20 puts that under "Advanced", no source
 * in this repository covers it, and inventing it would be Rule C. It is
 * registered as research item R26 and reported as a blocked section rather than
 * quietly omitted.
 *
 * <p>Source for §C1–§C2: the Cung Phi formula and the Bát Biến Du Niên
 * direction table are golden-tested against cited sources (research item R7).
 * The wording here only puts into words the meaning of a relation the UI
 * already shows as a coloured badge; it adds no new judgement.
 */
final class BatTrachRelationMeanings {

    /** Bumped whenever the wording below changes, independently of code version. */
    static final String CONTENT_VERSION = "1.0";

    static final String SOURCE_NOTE_VI =
            "Bát Trạch (Bát Biến Du Niên). Bảng tám hướng đã đối chiếu nguồn (mục nghiên cứu R7); "
                    + "phần diễn giải dưới đây là ý nghĩa khái quát của mỗi quan hệ, không phải "
                    + "phán đoán cho riêng một người. Truyền thống, không phải kết luận khoa học.";

    /**
     * @param natureVi   quan hệ này thuộc nhóm nào, nói ngắn
     * @param tendencyVi xu hướng chung của quan hệ này
     * @param domainsVi  những khía cạnh đời sống mà truyền thống gắn với quan hệ này
     */
    record Meaning(String natureVi, String tendencyVi, List<String> domainsVi) {
    }

    private static final Map<BatTrachRelation, Meaning> MEANINGS = build();

    private BatTrachRelationMeanings() {
    }

    static Meaning of(BatTrachRelation relation) {
        return MEANINGS.get(relation);
    }

    static Map<BatTrachRelation, Meaning> all() {
        return MEANINGS;
    }

    private static Map<BatTrachRelation, Meaning> build() {
        var map = new EnumMap<BatTrachRelation, Meaning>(BatTrachRelation.class);

        map.put(BatTrachRelation.SINH_KHI, new Meaning(
                "Tốt nhất trong nhóm hợp",
                "Thuận lợi tổng thể, được xem là hướng mang nhiều sinh khí nhất trong bốn hướng "
                        + "hợp của bạn — nâng đỡ tinh thần và sức sống nói chung.",
                List.of("Tài lộc", "Sự nghiệp")));

        map.put(BatTrachRelation.DIEN_NIEN, new Meaning(
                "Tốt, thiên về quan hệ",
                "Hỗ trợ sự hòa hợp: quan hệ trong nhà êm thuận hơn, hợp tác bên ngoài bớt trục "
                        + "trặc hơn.",
                List.of("Quan hệ", "Sự nghiệp")));

        map.put(BatTrachRelation.THIEN_Y, new Meaning(
                "Tốt, thiên về sức khỏe",
                "Hỗ trợ nghỉ ngơi và hồi phục, thiên về sự ổn định của thể chất. Đây là một xu "
                        + "hướng theo truyền thống, không phải một nhận định y tế.",
                List.of("Sức khỏe", "Tài lộc")));

        map.put(BatTrachRelation.PHUC_VI, new Meaning(
                "Trung tính, ổn định",
                "Giữ nguyên trạng: ít biến động, phù hợp khi điều bạn cần là sự bền vững chứ "
                        + "không phải một thay đổi.",
                List.of("Nhà cửa")));

        map.put(BatTrachRelation.HOA_HAI, new Meaning(
                "Không thuận, mức nhẹ",
                "Dễ phát sinh những trục trặc nhỏ và vụn vặt hơn là chuyện lớn.",
                List.of("Quan hệ", "Quyết định")));

        map.put(BatTrachRelation.LUC_SAT, new Meaning(
                "Không thuận",
                "Dễ phát sinh trì trệ, mọi việc kém suôn sẻ và tốn công hơn bình thường.",
                List.of("Quan hệ", "Sự nghiệp")));

        map.put(BatTrachRelation.NGU_QUY, new Meaning(
                "Không thuận",
                "Dễ phát sinh xáo trộn và mâu thuẫn, cả trong nhà lẫn trong công việc.",
                List.of("Tài lộc", "Quan hệ")));

        map.put(BatTrachRelation.TUYET_MENH, new Meaning(
                "Không thuận nhất trong nhóm kỵ",
                "Là hướng cần lưu ý nhiều nhất trong bốn hướng không hợp của bạn. Truyền thống "
                        + "xếp nó ở mức nặng nhất; đây là một xu hướng, không phải một lời cảnh "
                        + "báo về sức khỏe hay an toàn.",
                List.of("Sức khỏe", "Tài lộc")));

        // Mọi hằng số của enum phải có mặt. Thiếu một mục nghĩa là người dùng
        // thấy một hướng không có lời giải thích nào bên cạnh bảy hướng có -
        // và không có gì trong kiểu dữ liệu bắt được chuyện đó.
        if (map.size() != BatTrachRelation.values().length) {
            throw new IllegalStateException(
                    "Thiếu diễn giải cho " + (BatTrachRelation.values().length - map.size())
                            + " quan hệ Bát Trạch.");
        }
        return Map.copyOf(map);
    }
}
