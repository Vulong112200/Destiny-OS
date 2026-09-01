package io.destinyos.engines.iching;

import java.util.Objects;
import java.util.Optional;

/**
 * Interpretive content for one hexagram's quẻ từ (卦辭) — research items
 * R24/R25, verified 2026-09-01.
 *
 * <p><strong>Two sources, deliberately, because they are two different kinds
 * of thing (Rule D).</strong>
 * <ul>
 *   <li>{@code hanTu} — the classical Chinese — comes from
 *       {@code zh.wikisource.org/wiki/周易/*}, fetched as raw wikitext so no
 *       summarising model could normalise a character. The 卦辭/爻辭 is the
 *       same canonical text in every edition; it is not a translation choice,
 *       so the best-attested witness wins.</li>
 *   <li>{@code hanViet} and {@code nghia} — come from Ngô Tất Tố (dịch và
 *       chú giải), "Kinh Dịch Trọn Bộ", NXB Văn Học, digitized edition
 *       (khoahoctamlinh.vn), supplied by the project owner 2026-08-31. This
 *       IS a translation choice, so a named translator is cited and his
 *       wording is never edited. Ngô Tất Tố died 1954; the translation
 *       entered the public domain in Vietnam in 2005 under Article 27 of the
 *       Law on Intellectual Property.</li>
 * </ul>
 *
 * <p><strong>Why not simply use the book's own Chinese.</strong> Verification
 * measured it: the book's text layer disagreed with the canonical Chinese in
 * 34 of 64 judgments and 280 of 386 line texts. Most are OCR damage rather
 * than editorial variants — the scan yields 九陳 for 九四, 六一 for 六二,
 * 軔六 for 初六, and in one place mixes Latin script into the Chinese
 * (六 tam). 15% of its Chinese characters were Kangxi Radical codepoints
 * (U+2F00–U+2FDF) rather than CJK Unified Ideographs. The book's Vietnamese,
 * by contrast, verified clean, which is the half only this book has.
 *
 * <p><strong>What verification did and did not establish.</strong> The book's
 * chapter order was confirmed to be King Wen order by cross-checking its
 * printed trigram compositions against {@link HexagramTable} (59/60 agree;
 * the single disagreement, #16 Dự, is the book printing "Cấn" for "Chấn").
 * Every line label was re-derived from the hexagram's own yin/yang structure,
 * which caught two real book errors (see {@link LineJudgment#note()}). The
 * Vietnamese-to-line pairing was checked by comparing Hán-Việt syllable
 * counts against canonical character counts. What has NOT happened: a
 * line-by-line reading of Ngô Tất Tố's Vietnamese prose for translation
 * accuracy — that would need a reader of classical Chinese, not a checksum.
 *
 * @param number            King Wen sequence number, 1-64
 * @param chineseName       the hexagram's Chinese name (乾, 坤, 屯, …)
 * @param hanTu             canonical classical Chinese (卦辭), from wikisource
 * @param hanViet           Hán-Việt transliteration ("dịch âm"), Ngô Tất Tố
 * @param nghia             Vietnamese meaning ("dịch nghĩa"), Ngô Tất Tố's
 *                          literal gloss — not a modern paraphrase. Intended
 *                          as hard-data source material for the AI Narrative
 *                          layer (Rule B) to restate in contemporary
 *                          Vietnamese, not to be shown to end users verbatim
 * @param sourcePage        page in the Ngô Tất Tố edition, for audit
 * @param bookHanDiffered   whether the book's own (OCR-damaged) Chinese
 *                          disagreed with the canonical text at this entry —
 *                          kept as an audit signal, not a quality score
 * @param note              non-null only where extraction required a
 *                          documented repair of a defect in the source book
 */
public record HexagramJudgment(
        int number,
        String chineseName,
        String hanTu,
        String hanViet,
        String nghia,
        int sourcePage,
        boolean bookHanDiffered,
        String note
) {
    public HexagramJudgment {
        Objects.requireNonNull(chineseName, "chineseName");
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
