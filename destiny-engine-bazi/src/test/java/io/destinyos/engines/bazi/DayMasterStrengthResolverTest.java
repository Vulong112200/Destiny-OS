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
 * Golden tests against Thiệu Vĩ Hoa's own worked examples — Ví dụ 5, 6 and 7
 * only (Ví dụ 1, 2 and 3 were verified and rejected as fixtures, see
 * {@code docs/research_drafts/VERIFICATION_OPUS_R3.md} §C1/§C4/§C5).
 *
 * <p>Table locations, since the book's own page layout splits each example's
 * steps and its result table across a page boundary: Ví dụ 5's table is at
 * the top of p.350; Ví dụ 6's at the top of p.351; Ví dụ 7's at the bottom
 * of p.351.
 */
class DayMasterStrengthResolverTest {

    private static BaziPillar pillar(PillarPosition pos, HeavenlyStem stem, EarthlyBranch branch) {
        return new BaziPillar(pos, stem, branch, HiddenStems.of(branch), null, List.of());
    }

    @Test
    @DisplayName("Vi du 5 (p.349-350): Tan Mao / Mau Tuat / Quy Mao / Ky Mui -> matches the book except Moc "
            + "(this project deliberately uses -6 degrees for the branch-controlled-by-truc-dinh-stem penalty, "
            + "not the book's own -8 here; see DECISION_LOG.md's R3 decision #1 - Vi du 6 and 7 both demonstrate "
            + "-6 twice each, contradicting this example's -8, and -6 is what this implementation picked)")
    void viDu5MatchesBookExceptWoodPenaltyChoice() {
        var year = pillar(PillarPosition.YEAR, HeavenlyStem.TAN, EarthlyBranch.RABBIT);
        var month = pillar(PillarPosition.MONTH, HeavenlyStem.MAU, EarthlyBranch.DOG);
        var day = pillar(PillarPosition.DAY, HeavenlyStem.QUY, EarthlyBranch.RABBIT);
        var hour = pillar(PillarPosition.HOUR, HeavenlyStem.KY, EarthlyBranch.GOAT);

        Optional<DayMasterStrength> result = DayMasterStrengthResolver.resolve(year, month, day, hour);
        assertThat(result).isPresent();
        DayMasterStrength s = result.get();

        // Book's own arithmetic: Moc=52, Tho=137 (Moc's nam-truc-dinh-Mao
        // loses 8, not 6). This implementation's -6 choice makes Moc=54
        // instead - the only figure this penalty choice touches, since it
        // only ever fires on the one branch (nam pillar's Mao) that a
        // same-pillar controlling stem (Tan Kim) actually reaches.
        assertThat(s.elementDegrees())
                .containsEntry(FiveElement.WOOD, 54)
                .containsEntry(FiveElement.FIRE, 0)
                .containsEntry(FiveElement.EARTH, 137)
                .containsEntry(FiveElement.METAL, 24)
                .containsEntry(FiveElement.WATER, 0);
        // Day Master Quy (Thuy): phe minh = Kim (generates Thuy) + Thuy = 24+0=24; T=54+0+137+24+0=215
        assertThat(s.ownSideDegrees()).isEqualTo(24);
        assertThat(s.totalDegrees()).isEqualTo(215);
        assertThat(s.vuong()).isFalse(); // 24/215 ~= 11.2% < 40%, same verdict as the book's own 24/213
    }

    @Test
    @DisplayName("Ví dụ 6 (p.350-351): Nhâm Thân / Đinh Mùi / Bính Thân / Tân Mão -> element totals match the book exactly")
    void viDu6MatchesBookExactly() {
        var year = pillar(PillarPosition.YEAR, HeavenlyStem.NHAM, EarthlyBranch.MONKEY);
        var month = pillar(PillarPosition.MONTH, HeavenlyStem.DINH, EarthlyBranch.GOAT);
        var day = pillar(PillarPosition.DAY, HeavenlyStem.BINH, EarthlyBranch.MONKEY);
        var hour = pillar(PillarPosition.HOUR, HeavenlyStem.TAN, EarthlyBranch.RABBIT);

        Optional<DayMasterStrength> result = DayMasterStrengthResolver.resolve(year, month, day, hour);
        assertThat(result).isPresent();
        DayMasterStrength s = result.get();

        assertThat(s.elementDegrees())
                .containsEntry(FiveElement.WOOD, 27)
                .containsEntry(FiveElement.FIRE, 51)
                .containsEntry(FiveElement.EARTH, 43)
                .containsEntry(FiveElement.METAL, 42)
                .containsEntry(FiveElement.WATER, 34);
        // Day Master Binh (Hoa): phe minh = Moc (generates Hoa) + Hoa = 27+51=78; T=27+51+43+42+34=197
        assertThat(s.ownSideDegrees()).isEqualTo(78);
        assertThat(s.totalDegrees()).isEqualTo(197);
        assertThat(s.vuong()).isFalse(); // 78/197 ~= 39.6%, just under 40%
    }

    @Test
    @DisplayName("Ví dụ 7 (p.351): Ất Dậu / Canh Thìn / Quý Dậu / Mậu Ngọ -> element totals match the book exactly")
    void viDu7MatchesBookExactly() {
        var year = pillar(PillarPosition.YEAR, HeavenlyStem.AT, EarthlyBranch.ROOSTER);
        var month = pillar(PillarPosition.MONTH, HeavenlyStem.CANH, EarthlyBranch.DRAGON);
        var day = pillar(PillarPosition.DAY, HeavenlyStem.QUY, EarthlyBranch.ROOSTER);
        var hour = pillar(PillarPosition.HOUR, HeavenlyStem.MAU, EarthlyBranch.HORSE);

        Optional<DayMasterStrength> result = DayMasterStrengthResolver.resolve(year, month, day, hour);
        assertThat(result).isPresent();
        DayMasterStrength s = result.get();

        assertThat(s.elementDegrees())
                .containsEntry(FiveElement.WOOD, 6)
                .containsEntry(FiveElement.FIRE, 21)
                .containsEntry(FiveElement.EARTH, 76)
                .containsEntry(FiveElement.METAL, 96)
                .containsEntry(FiveElement.WATER, 19);
        // Day Master Quy (Thuy): phe minh = Kim (generates Thuy) + Thuy = 96+19=115; T=6+21+76+96+19=218
        assertThat(s.ownSideDegrees()).isEqualTo(115);
        assertThat(s.totalDegrees()).isEqualTo(218);
        assertThat(s.vuong()).isTrue(); // 115/218 ~= 52.8% >= 40%
    }
}
