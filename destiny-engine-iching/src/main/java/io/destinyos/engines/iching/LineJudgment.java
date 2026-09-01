package io.destinyos.engines.iching;

import java.util.Objects;
import java.util.Optional;

/**
 * Interpretive content for one line's hào từ (爻辭) of one hexagram —
 * research item R25, same two-source split as {@link HexagramJudgment}:
 * canonical Chinese from zh.wikisource, Vietnamese from Ngô Tất Tố.
 *
 * <p><strong>Every label here was re-derived, not trusted.</strong> A line's
 * name encodes its own yin/yang — 九 (Cửu) is a yang line, 六 (Lục) a yin one
 * — so it is fully determined by the hexagram's trigram composition and can
 * be checked rather than transcribed. Doing that across all 384 ordinary
 * lines caught two errors the source book had shipped, both since corrected
 * with the reason recorded in {@link #note()}:
 * <ul>
 *   <li>Quẻ 45 (Tụy) line 1 — the book prints 初九/"Sơ Cửu", but Tụy is Đoài
 *       over Khôn, whose first line is yin. The book is wrong consistently
 *       here, in its label and its gloss alike, so only the machine-readable
 *       label was corrected and Ngô Tất Tố's prose left verbatim.</li>
 *   <li>Quẻ 51 (Chấn) line 4 — the book prints 六四/"Lục Tứ" while its own
 *       gloss two lines below reads "Hào <em>Chín</em> Tư". Chấn's fourth
 *       line is yang, so the gloss was right and the label wrong; correcting
 *       it resolved the book's self-contradiction.</li>
 * </ul>
 *
 * <p>Three further defects in the book needed a documented repair, listed
 * here because this Javadoc previously named only the two above while the
 * data already carried five — the class doc was behind the table it
 * describes, which is the same class of drift the repairs exist to prevent:
 * <ul>
 *   <li>Quẻ 2 (Khôn) Thượng Lục — the book prints "Dịch nghĩa" twice in place
 *       of "Dịch âm" then "Dịch nghĩa", so the automated extractor skipped
 *       the entry entirely; restored from p.129.</li>
 *   <li>Quẻ 48 (Tỉnh) line 1 — the book prints the transliteration twice, and
 *       the second copy also misreads "mê" for "nê"; the duplicate dropped.</li>
 *   <li>Quẻ 53 (Tiệm) line 2 — the book omits the line label altogether. This
 *       one is <em>reconstructed by position</em> (between Sơ Lục and Cửu Tam,
 *       matching 六二 in the canonical text) rather than read off the page, so
 *       it is the least directly-attested entry in the table and says so in
 *       its own note.</li>
 *   <li>Quẻ 45 (Tụy) line 6 — the book prints "Háo" for "Hào" at the start of
 *       the gloss. Corrected because the label is independently derivable
 *       (Thượng Lục is line 6 and yin, so "Hào Sáu Trên"); no other word of
 *       Ngô Tất Tố's prose was touched.</li>
 * </ul>
 *
 * <p><strong>Four entries legitimately carry no position label</strong> — 2/6,
 * 38/5, 47/5 and 53/2 — because the book simply does not reprint one there.
 * That is a fact about the edition, not a defect, and the translation is a
 * named translator's text, so nothing was prepended to make the shape uniform.
 * {@code HexagramJudgmentTableTest} pins that set of four explicitly, so a
 * fifth cannot appear unnoticed and so nobody "fixes" one of these by editing
 * his wording.
 *
 * @param hexagramNumber  King Wen number this line belongs to, 1-64
 * @param position        1-6 counting from the bottom line upward, matching
 *                        {@link Hexagram}'s own line order; 0 for the two
 *                        classical "dụng cửu"/"dụng lục" lines that exist
 *                        only for hexagram 1 (Kiền, all-yang) and 2 (Khôn,
 *                        all-yin)
 * @param label           the line's Vietnamese name ("Sơ Cửu", "Cửu Nhị",
 *                        "Lục Tam", … "Thượng Lục", "Dụng Cửu"/"Dụng Lục")
 * @param hanTu           canonical classical Chinese (爻辭), from wikisource,
 *                        with the position label prefix stripped
 * @param hanViet         Hán-Việt transliteration ("dịch âm"), Ngô Tất Tố
 * @param nghia           Vietnamese meaning ("dịch nghĩa"), Ngô Tất Tố
 * @param sourcePage      page in the Ngô Tất Tố edition, for audit
 * @param bookHanDiffered whether the book's own (OCR-damaged) Chinese
 *                        disagreed with the canonical text at this entry
 * @param note            non-null only where extraction required a documented
 *                        repair of a defect in the source book
 */
public record LineJudgment(
        int hexagramNumber,
        int position,
        String label,
        String hanTu,
        String hanViet,
        String nghia,
        Integer sourcePage,
        boolean bookHanDiffered,
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
