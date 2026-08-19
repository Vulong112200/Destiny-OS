package io.destinyos.calendar;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * The R14a rule table (owner-adopted default, {@code docs/DECISION_LOG.md}).
 *
 * <p>Source: the Vietnamese Wikipedia article "Giờ ở Việt Nam", which cites
 * three specific gazette entries:
 * <ul>
 *   <li>Ngô Đình Diệm (2/7/1955). "Dụ số 46 ngày 25 tháng 6 năm 1955."
 *       <i>Công Báo Việt Nam</i>, Quyển 92, tr. 1780-1781. (South -&gt; UTC+7)
 *   <li>Ngô Đình Diệm (2/1/1960). "Sắc lệnh số 362-TTP ngày 30 tháng 12 năm
 *       1959." <i>Công Báo Việt Nam</i>, Quyển Đệ-nhất Tam-cá-nguyệt 1960.
 *       (South -&gt; UTC+8)
 *   <li>"Quyết định số 121-CP của Chính phủ Việt Nam Dân chủ Cộng hòa ngày
 *       8 tháng 8 năm 1967." (North -&gt; UTC+7; gazette volume not given)
 * </ul>
 * The reunification date (13 June 1975, resolving a round-1 12-vs-13 June
 * conflict via the article's own body text) and the pre-1955/pre-1968
 * "implied UTC+8" windows are the article's stated timeline but are not
 * independently gazette-cited the way the three changes above are — this
 * is recorded per-row, not smoothed over.
 *
 * <p><b>Mandatory confidence label</b> (research finding, round 2): "Dựa
 * trên trích dẫn Công Báo qua nguồn thứ cấp (Wikipedia); văn bản gốc chưa
 * được đối chiếu trực tiếp." Every resolution from this table carries this
 * as an informational (non-result-affecting) {@code Uncertainty} — this
 * table is an adopted default, not a primary-verified historical record.
 *
 * <p>R14b (which locations belong to which jurisdiction, 1955-1975) is
 * <b>not</b> addressed here — see {@link VietnameseRegion}'s Javadoc. A
 * query with {@code region = UNKNOWN} landing in a date range where North
 * and South diverge resolves to {@link Optional#empty()}, never to a
 * guessed side.
 */
public final class HistoricalTimezoneRuleTable {

    private static final List<HistoricalTimezoneRule> RULES = List.of(
            new HistoricalTimezoneRule(null, LocalDate.of(1955, 7, 1), null, 8.0,
                    "Implied pre-partition Indochina time; not independently gazette-cited "
                            + "(R14a research finding, round 2)."),
            new HistoricalTimezoneRule(LocalDate.of(1955, 7, 1), LocalDate.of(1960, 1, 1),
                    VietnameseRegion.SOUTH, 7.0,
                    "Dụ số 46 ngày 25/6/1955, Công Báo Việt Nam Quyển 92, tr. 1780-1781."),
            new HistoricalTimezoneRule(LocalDate.of(1960, 1, 1), LocalDate.of(1975, 6, 13),
                    VietnameseRegion.SOUTH, 8.0,
                    "Sắc lệnh số 362-TTP ngày 30/12/1959, Công Báo Việt Nam "
                            + "Quyển Đệ-nhất Tam-cá-nguyệt 1960."),
            new HistoricalTimezoneRule(LocalDate.of(1955, 7, 1), LocalDate.of(1968, 1, 1),
                    VietnameseRegion.NORTH, 8.0,
                    "Continuation of pre-partition offset for the North; not independently "
                            + "gazette-cited for this specific sub-period, only the 1968 change is."),
            new HistoricalTimezoneRule(LocalDate.of(1968, 1, 1), LocalDate.of(1975, 6, 13),
                    VietnameseRegion.NORTH, 7.0,
                    "Quyết định số 121-CP ngày 8/8/1967 (gazette volume not given in the "
                            + "secondary source)."),
            new HistoricalTimezoneRule(LocalDate.of(1975, 6, 13), null, null, 7.0,
                    "Reunification, 13/6/1975 (resolves a 12-vs-13-June conflict between "
                            + "round-1 sources via the Wikipedia article's own body text).")
    );

    private HistoricalTimezoneRuleTable() {
    }

    /**
     * @return the single rule covering {@code date} for {@code region}, or
     *         empty if none does (region-dependent window with
     *         {@code region == UNKNOWN}, or a genuine coverage gap) — the
     *         caller MUST treat empty as {@code RESEARCH_REQUIRED}, never
     *         fall back to a default offset (ADR D3)
     */
    public static Optional<HistoricalTimezoneRule> resolve(LocalDate date, VietnameseRegion region) {
        List<HistoricalTimezoneRule> matches = RULES.stream()
                .filter(r -> r.covers(date, region))
                .toList();
        if (matches.size() != 1) {
            // Zero matches: genuine gap, or a region-dependent window queried
            // with UNKNOWN region. More than one match would mean the table
            // itself has an overlap bug - surfacing uncertainty is still the
            // honest choice, never picking one arbitrarily.
            return Optional.empty();
        }
        return Optional.of(matches.get(0));
    }
}
