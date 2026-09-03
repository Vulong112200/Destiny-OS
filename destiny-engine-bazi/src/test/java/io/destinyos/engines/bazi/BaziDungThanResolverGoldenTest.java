package io.destinyos.engines.bazi;

import static org.assertj.core.api.Assertions.assertThat;

import io.destinyos.calendar.EarthlyBranch;
import io.destinyos.calendar.FiveElement;
import io.destinyos.calendar.HeavenlyStem;
import io.destinyos.calendar.HiddenStems;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Golden tests for {@link BaziDungThanResolver} against the ten worked charts
 * in {@code docs/research_drafts/R1_chuong23_dung_than.md} (Thiệu Vĩ Hoa &amp;
 * Trần Viên, <em>Dự đoán theo Tứ trụ</em>, Chương 23, pp.583-629), plus a few
 * direct unit tests of the refusal paths.
 *
 * <p><strong>Honest scorecard (Rule C — mismatches are recorded, not
 * hidden or forced):</strong> 4 of the 10 charts match the book's own stated
 * Dụng Thần exactly (Ví dụ 3a, 5, 8, 9). The other 6 are asserted against
 * this resolver's <em>actual</em> output, each with a comment explaining why
 * it diverges from the book — every one traces to a scope boundary this
 * task deliberately drew, not to an arithmetic bug:
 * <ul>
 *   <li>Ví dụ 1, 2 — the month branch's chính khí is {@link TenGod#THIEN_AN}
 *       relative to the Day Master, and mục 1 has no "cách thiên ấn" —
 *       refuses by design.</li>
 *   <li>Ví dụ 3b, 6 — the book's own text names these "dụng thần điều hầu"
 *       (3b, tr.595) or "dùng ... giải hạn" (6, tr.605), i.e. climate
 *       adjustment, which this resolver never applies (out of scope, see
 *       class Javadoc).</li>
 *   <li>Ví dụ 7 — the book's own analysis states the Day Master "chuyển
 *       nguy thành an, nhật chủ từ nhược biến thành trung hòa" (tr.609) —
 *       a third, balanced state this resolver has no representation for
 *       (see class Javadoc, "trung hòa" gap); it is treated as Vượng
 *       ({@code ownSideDegrees/totalDegrees ~= 52%}), which the book's own
 *       narrative already flags as not quite the right description either.</li>
 *   <li>Ví dụ 10 — the book prefers Thực Thương (tiết, drain) over Tài
 *       (khắc, control) for handling an excess Ấn even though Tài is
 *       present (nonzero) in the chart, which contradicts mục 1.c's own
 *       literal primary-is-Tài wording as transcribed. A genuine tension
 *       between the source's stated rule and its own worked example, not
 *       resolved here — see class Javadoc.</li>
 * </ul>
 */
class BaziDungThanResolverGoldenTest {

    private static BaziPillar pillar(PillarPosition pos, HeavenlyStem stem, EarthlyBranch branch) {
        return new BaziPillar(pos, stem, branch, HiddenStems.of(branch), null, List.of());
    }

    private static BaziDungThanResolver.Outcome outcomeFor(HeavenlyStem ys, EarthlyBranch yb,
            HeavenlyStem ms, EarthlyBranch mb, HeavenlyStem ds, EarthlyBranch db, HeavenlyStem hs, EarthlyBranch hb) {
        var year = pillar(PillarPosition.YEAR, ys, yb);
        var month = pillar(PillarPosition.MONTH, ms, mb);
        var day = pillar(PillarPosition.DAY, ds, db);
        var hour = pillar(PillarPosition.HOUR, hs, hb);
        Optional<DayMasterStrength> strength = DayMasterStrengthResolver.resolve(year, month, day, hour);
        assertThat(strength).as("DayMasterStrength (R3) must resolve for every one of these 10 published charts")
                .isPresent();
        return BaziDungThanResolver.resolve(year, month, day, hour, strength.get());
    }

    // ------------------------------------------------------------------
    // Direct unit tests of the refusal paths.
    // ------------------------------------------------------------------

    @Test
    @DisplayName("No DayMasterStrength supplied -> refuses DAY_MASTER_STRENGTH_UNAVAILABLE")
    void refusesWithoutStrength() {
        var year = pillar(PillarPosition.YEAR, HeavenlyStem.GIAP, EarthlyBranch.RAT);
        var month = pillar(PillarPosition.MONTH, HeavenlyStem.GIAP, EarthlyBranch.RAT);
        var day = pillar(PillarPosition.DAY, HeavenlyStem.GIAP, EarthlyBranch.RAT);
        var hour = pillar(PillarPosition.HOUR, HeavenlyStem.GIAP, EarthlyBranch.RAT);

        var outcome = BaziDungThanResolver.resolve(year, month, day, hour, null);
        assertThat(outcome).isInstanceOfSatisfying(BaziDungThanResolver.Refused.class,
                r -> assertThat(r.reason()).isEqualTo(BaziDungThanResolver.RefuseReason.DAY_MASTER_STRENGTH_UNAVAILABLE));
    }

    @Test
    @DisplayName("Cach Chinh Tai + Nhat Chu Nhuoc -> refuses NO_RULE_FOR_PATTERN_BRANCH (Nhuoc branch never transcribed)")
    void refusesChinhTaiNhuoc() {
        // Thang Ky Suu -> chinh khi Ky; TenGods.of(Giap, Ky) = CHINH_TAI. Heavily Kim/Tho, Giap ends up Nhuoc.
        var outcome = outcomeFor(HeavenlyStem.CANH, EarthlyBranch.MONKEY, HeavenlyStem.KY, EarthlyBranch.OX,
                HeavenlyStem.GIAP, EarthlyBranch.HORSE, HeavenlyStem.CANH, EarthlyBranch.MONKEY);

        assertThat(outcome).isInstanceOfSatisfying(BaziDungThanResolver.Refused.class,
                r -> assertThat(r.reason()).isEqualTo(BaziDungThanResolver.RefuseReason.NO_RULE_FOR_PATTERN_BRANCH));
    }

    // ------------------------------------------------------------------
    // The 10 Chuong 23 golden charts.
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Vi du 1 (tr.583): Dinh Mui / Dinh Mui / Tan Mao (ND) / Canh Dan "
            + "-> refuses PATTERN_NOT_IDENTIFIED (thang chinh khi Ky = Thien An so voi Tan; muc 1 khong co cach do). "
            + "Book's own answer is Thuy (Thuc Thuong) - not reproducible in scope by design.")
    void viDu1RefusesThienAn() {
        var outcome = outcomeFor(HeavenlyStem.DINH, EarthlyBranch.GOAT, HeavenlyStem.DINH, EarthlyBranch.GOAT,
                HeavenlyStem.TAN, EarthlyBranch.RABBIT, HeavenlyStem.CANH, EarthlyBranch.TIGER);

        assertThat(outcome).isInstanceOfSatisfying(BaziDungThanResolver.Refused.class,
                r -> assertThat(r.reason()).isEqualTo(BaziDungThanResolver.RefuseReason.PATTERN_NOT_IDENTIFIED));
    }

    @Test
    @DisplayName("Vi du 2 (tr.588): Nham Dan / Binh Ngo / Ky Suu (ND) / Giap Tuat "
            + "-> refuses PATTERN_NOT_IDENTIFIED (thang chinh khi Dinh = Thien An so voi Ky). "
            + "Book's own answer is Thuy+Kim - not reproducible in scope by design.")
    void viDu2RefusesThienAn() {
        var outcome = outcomeFor(HeavenlyStem.NHAM, EarthlyBranch.TIGER, HeavenlyStem.BINH, EarthlyBranch.HORSE,
                HeavenlyStem.KY, EarthlyBranch.OX, HeavenlyStem.GIAP, EarthlyBranch.DOG);

        assertThat(outcome).isInstanceOfSatisfying(BaziDungThanResolver.Refused.class,
                r -> assertThat(r.reason()).isEqualTo(BaziDungThanResolver.RefuseReason.PATTERN_NOT_IDENTIFIED));
    }

    @Test
    @DisplayName("Vi du 3a (tr.591): Quy Mao / Giap Ti / Giap Thin (ND) / Giap Ti -> MATCH: Kim (Quan Sat), "
            + "same as the book (\"chon quan sat che than lam dung than\"), even though Kim's degree is 0 "
            + "in this chart - see class Javadoc for why presence is not required.")
    void viDu3aMatchesBook() {
        var outcome = outcomeFor(HeavenlyStem.QUY, EarthlyBranch.RABBIT, HeavenlyStem.GIAP, EarthlyBranch.RAT,
                HeavenlyStem.GIAP, EarthlyBranch.DRAGON, HeavenlyStem.GIAP, EarthlyBranch.RAT);

        assertThat(outcome).isInstanceOfSatisfying(BaziDungThanResolver.Resolved.class, r -> {
            assertThat(r.result().pattern()).isEqualTo(BaziDungThanResolver.Pattern.CHINH_AN);
            assertThat(r.result().dungThan()).isEqualTo(FiveElement.METAL);
        });
    }

    @Test
    @DisplayName("Vi du 3b (tr.595): Canh Tuat / Nham Ngo / Canh Than (ND) / Dinh Suu -> resolver gives Hoa "
            + "(Chinh Quan, Ti Kiep nhieu -> Quan Sat) via ordinary Phu Uc; book's own answer is Thuy, explicitly "
            + "named \"dung than dieu hau\" (tr.595) - Dieu Hau is out of scope, mismatch is expected and recorded.")
    void viDu3bDivergesDueToDieuHau() {
        var outcome = outcomeFor(HeavenlyStem.CANH, EarthlyBranch.DOG, HeavenlyStem.NHAM, EarthlyBranch.HORSE,
                HeavenlyStem.CANH, EarthlyBranch.MONKEY, HeavenlyStem.DINH, EarthlyBranch.OX);

        assertThat(outcome).isInstanceOfSatisfying(BaziDungThanResolver.Resolved.class, r -> {
            assertThat(r.result().pattern()).isEqualTo(BaziDungThanResolver.Pattern.CHINH_QUAN);
            assertThat(r.result().dungThan()).isEqualTo(FiveElement.FIRE);
        });
    }

    @Test
    @DisplayName("Vi du 5 (tr.600): Giap Ngo / Mau Thin / Quy Mao (ND) / At Mao (Nu) -> MATCH: Kim (An), same as "
            + "the book (\"lay an tinh kim than de khac che... lam dung than\"), even though Kim's degree is 0 "
            + "- Thuc Thuong (141) outranks Quan Sat (83) among simultaneously-nhieu triggers, matching the book's "
            + "own \"thuc thuong qua nang\" framing.")
    void viDu5MatchesBook() {
        var outcome = outcomeFor(HeavenlyStem.GIAP, EarthlyBranch.HORSE, HeavenlyStem.MAU, EarthlyBranch.DRAGON,
                HeavenlyStem.QUY, EarthlyBranch.RABBIT, HeavenlyStem.AT, EarthlyBranch.RABBIT);

        assertThat(outcome).isInstanceOfSatisfying(BaziDungThanResolver.Resolved.class, r -> {
            assertThat(r.result().pattern()).isEqualTo(BaziDungThanResolver.Pattern.CHINH_QUAN);
            assertThat(r.result().dungThan()).isEqualTo(FiveElement.METAL);
        });
    }

    @Test
    @DisplayName("Vi du 6 (tr.605): Canh Dan / Mau Ti / Quy Ty (ND) / Nham Ti -> resolver gives Tho (Kien Loc, "
            + "unconditional Tai/Quan branch); book's own answer is Hoa, described as \"phai dung binh hoa giai "
            + "han\" (tr.605, functional Dieu Hau without naming the term) - Dieu Hau out of scope, mismatch "
            + "expected and recorded.")
    void viDu6DivergesDueToDieuHau() {
        var outcome = outcomeFor(HeavenlyStem.CANH, EarthlyBranch.TIGER, HeavenlyStem.MAU, EarthlyBranch.RAT,
                HeavenlyStem.QUY, EarthlyBranch.SNAKE, HeavenlyStem.NHAM, EarthlyBranch.RAT);

        assertThat(outcome).isInstanceOfSatisfying(BaziDungThanResolver.Resolved.class, r -> {
            assertThat(r.result().pattern()).isEqualTo(BaziDungThanResolver.Pattern.KIEN_LOC);
            assertThat(r.result().dungThan()).isEqualTo(FiveElement.EARTH);
        });
    }

    @Test
    @DisplayName("Vi du 7 (tr.609): Giap Thin / At Hoi / Binh Tuat (ND) / Ky Hoi (Nu) -> resolver gives Kim "
            + "(That Sat, An nhieu -> Tai) treating the chart as Vuong (ownSide/total ~= 52%); book's own text "
            + "says the Day Master became \"trung hoa\" (tr.609), a third state this resolver has no "
            + "representation for (see class Javadoc). Book's answer is Moc (An) - mismatch expected and recorded.")
    void viDu7DivergesDueToTrungHoaGap() {
        var outcome = outcomeFor(HeavenlyStem.GIAP, EarthlyBranch.DRAGON, HeavenlyStem.AT, EarthlyBranch.PIG,
                HeavenlyStem.BINH, EarthlyBranch.DOG, HeavenlyStem.KY, EarthlyBranch.PIG);

        assertThat(outcome).isInstanceOfSatisfying(BaziDungThanResolver.Resolved.class, r -> {
            assertThat(r.result().pattern()).isEqualTo(BaziDungThanResolver.Pattern.THAT_SAT);
            assertThat(r.result().dungThan()).isEqualTo(FiveElement.METAL);
        });
    }

    @Test
    @DisplayName("Vi du 8 (tr.614): At Mui / Giap Than / Canh Tuat (ND) / Quy Mui -> MATCH: Moc (Tai), same as "
            + "the book's explicit three-part statement \"Dung than la moc, hi than la hoa, ki than la tho\" "
            + "(tr.614).")
    void viDu8MatchesBook() {
        var outcome = outcomeFor(HeavenlyStem.AT, EarthlyBranch.GOAT, HeavenlyStem.GIAP, EarthlyBranch.MONKEY,
                HeavenlyStem.CANH, EarthlyBranch.DOG, HeavenlyStem.QUY, EarthlyBranch.GOAT);

        assertThat(outcome).isInstanceOfSatisfying(BaziDungThanResolver.Resolved.class, r -> {
            assertThat(r.result().pattern()).isEqualTo(BaziDungThanResolver.Pattern.KIEN_LOC);
            assertThat(r.result().dungThan()).isEqualTo(FiveElement.WOOD);
        });
    }

    @Test
    @DisplayName("Vi du 9 (tr.618): Nham Dan / Nham Ti / Dinh Dau (ND) / Tan Suu (Nu) -> MATCH: Moc (An). Tai "
            + "(Kim, do 76) and Quan Sat (Thuy, do 122) are BOTH nhieu in this chart; the book's answer follows "
            + "the Quan Sat trigger (larger degree), not the Tai trigger that is listed first in the source's "
            + "prose - the evidence behind this class's \"largest degree wins\" tie-break, see class Javadoc.")
    void viDu9MatchesBook() {
        var outcome = outcomeFor(HeavenlyStem.NHAM, EarthlyBranch.TIGER, HeavenlyStem.NHAM, EarthlyBranch.RAT,
                HeavenlyStem.DINH, EarthlyBranch.ROOSTER, HeavenlyStem.TAN, EarthlyBranch.OX);

        assertThat(outcome).isInstanceOfSatisfying(BaziDungThanResolver.Resolved.class, r -> {
            assertThat(r.result().pattern()).isEqualTo(BaziDungThanResolver.Pattern.THAT_SAT);
            assertThat(r.result().dungThan()).isEqualTo(FiveElement.WOOD);
        });
    }

    @Test
    @DisplayName("Vi du 10 (tr.623): Binh Than / Dinh Dau / Nham Thin (ND) / Tan Hoi -> resolver gives Hoa "
            + "(Chinh An, An nhieu -> Tai, since Tai's degree 36 is present); book's own answer is Moc (Thuc "
            + "Thuong, \"de xi kim khi\") even though Tai is present in the chart, contradicting muc 1.c's own "
            + "literal primary-is-Tai wording as transcribed - a genuine tension between the source's stated "
            + "rule and its own worked example, not resolved here. Mismatch expected and recorded.")
    void viDu10DivergesFromSourcesOwnStatedRule() {
        var outcome = outcomeFor(HeavenlyStem.BINH, EarthlyBranch.MONKEY, HeavenlyStem.DINH, EarthlyBranch.ROOSTER,
                HeavenlyStem.NHAM, EarthlyBranch.DRAGON, HeavenlyStem.TAN, EarthlyBranch.PIG);

        assertThat(outcome).isInstanceOfSatisfying(BaziDungThanResolver.Resolved.class, r -> {
            assertThat(r.result().pattern()).isEqualTo(BaziDungThanResolver.Pattern.CHINH_AN);
            assertThat(r.result().dungThan()).isEqualTo(FiveElement.FIRE);
        });
    }
}
