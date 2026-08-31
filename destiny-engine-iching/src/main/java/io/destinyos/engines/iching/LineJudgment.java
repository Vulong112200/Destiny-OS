package io.destinyos.engines.iching;

import java.util.Objects;
import java.util.Optional;

/**
 * Vietnamese interpretive content for one line's hào từ (爻辭) of one
 * hexagram - research item R25, same source as {@link HexagramJudgment}.
 *
 * @param hexagramNumber King Wen number this line belongs to, 1-64
 * @param position       1-6 counting from the bottom line upward, matching
 *                        {@code Hexagram}'s own bit order; 0 for the two
 *                        special "dụng cửu"/"dụng lục" lines that exist only
 *                        for hexagram 1 (Kiền, all-yang) and 2 (Khôn,
 *                        all-yin) - see R12's note on this classical special
 *                        case, still {@code BlockedSection} for which line to
 *                        read when six lines move at once
 * @param label          the line's own name as printed ("Sơ Cửu", "Cửu Nhị",
 *                        "Lục Tam", ... "Thượng Lục", "Dụng Cửu"/"Dụng Lục")
 * @param hanTu          classical Chinese text
 * @param hanViet        Hán-Việt transliteration
 * @param nghia          Vietnamese meaning, Ngô Tất Tố's literal gloss
 * @param sourcePage     page number in the source edition, for audit
 * @param note           non-null only where extraction required a documented
 *                        manual correction or carries a known data-quality
 *                        caveat (see R25)
 */
public record LineJudgment(
        int hexagramNumber,
        int position,
        String label,
        String hanTu,
        String hanViet,
        String nghia,
        Integer sourcePage,
        String note
) {
    public LineJudgment {
        Objects.requireNonNull(label, "label");
        Objects.requireNonNull(hanTu, "hanTu");
        Objects.requireNonNull(hanViet, "hanViet");
        Objects.requireNonNull(nghia, "nghia");
        if (hexagramNumber < 1 || hexagramNumber > 64) {
            throw new IllegalArgumentException("King Wen number must be 1-64, got " + hexagramNumber);
        }
        if (position < 0 || position > 6) {
            throw new IllegalArgumentException("position must be 0-6, got " + position);
        }
    }

    public Optional<String> noteIfPresent() {
        return Optional.ofNullable(note);
    }
}
