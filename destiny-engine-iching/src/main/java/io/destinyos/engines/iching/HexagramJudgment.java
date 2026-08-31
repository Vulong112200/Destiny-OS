package io.destinyos.engines.iching;

import java.util.Objects;
import java.util.Optional;

/**
 * Vietnamese interpretive content for one hexagram's quẻ từ (卦辭) - research
 * items R24/R25.
 *
 * <p>Source: Ngô Tất Tố (dịch và chú giải), "Kinh Dịch Trọn Bộ", NXB Văn Học -
 * digitized edition (khoahoctamlinh.vn), supplied by the project owner
 * 2026-08-31. Ngô Tất Tố died 1954; the translation entered the public domain
 * in Vietnam in 2005 under Article 27 of the Law on Intellectual Property
 * (life of the author + 50 years) - see docs/research_drafts/
 * R24_iching_hexagram_judgments.md section 2 for the full copyright analysis.
 *
 * <p><strong>Verification status, stated honestly.</strong> Hexagrams 1-8's
 * Chinese text was cross-checked against >= 2 independent hosts
 * (zh.wikisource.org, ctext.org) during the R24 pilot before this book was
 * supplied ({@code hanTuCrossChecked}). Hexagrams 9-64 come from this single
 * book only - not yet independently cross-checked. None of this content has
 * had the project's standard Opus verification pass (compare every other
 * research item in docs/RESEARCH_BLOCKERS.md) - it should not be treated as
 * fully settled until that happens.
 *
 * @param number             King Wen sequence number, 1-64
 * @param hanTu               classical Chinese text (quẻ từ / 卦辭)
 * @param hanViet             Hán-Việt transliteration ("dịch âm")
 * @param nghia               Vietnamese meaning ("dịch nghĩa"), Ngô Tất Tố's
 *                            literal gloss - not a modern paraphrase. Meant as
 *                            hard-data source material for the AI Narrative
 *                            layer (Rule B) to restate in contemporary
 *                            Vietnamese, not to be shown to end users verbatim
 * @param sourcePage          page number in the source edition, for audit
 * @param hanTuCrossChecked   whether {@code hanTu} was independently
 *                            cross-checked against a second host (true only
 *                            for hexagrams 1-8, the R24 pilot)
 * @param note                non-null only where extraction required a
 *                            documented manual correction or carries a known
 *                            data-quality caveat (see R24/R25)
 */
public record HexagramJudgment(
        int number,
        String hanTu,
        String hanViet,
        String nghia,
        int sourcePage,
        boolean hanTuCrossChecked,
        String note
) {
    public HexagramJudgment {
        Objects.requireNonNull(hanTu, "hanTu");
        Objects.requireNonNull(hanViet, "hanViet");
        Objects.requireNonNull(nghia, "nghia");
        if (number < 1 || number > 64) {
            throw new IllegalArgumentException("King Wen number must be 1-64, got " + number);
        }
    }

    public Optional<String> noteIfPresent() {
        return Optional.ofNullable(note);
    }
}
