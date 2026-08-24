package io.destinyos.engines.iching;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Mai Hoa's two deterministic methods, against the primary-sourced worked example and the decided mod-6 convention. */
class MaiHoaCastingTest {

    @Test
    @DisplayName("The quoted worked example (3, 6) reproduces exactly: Ly/Khảm, Hỏa Thủy Vị Tế (hexagram 64)")
    void quotedWorkedExampleReproduces() {
        // R12_iching_maihoa.md §5: "數字三作為上卦，數字六作為下卦，數字三就是
        // 離卦，數字六就是坎卦，兩卦重疊就是上離下坎，組合火水未濟卦。"
        var cast = MaiHoaCasting.fromNumbers(3, 6);
        assertThat(cast.upper()).isEqualTo(IChingTrigram.FIRE);
        assertThat(cast.lower()).isEqualTo(IChingTrigram.WATER);
        assertThat(HexagramTable.of(cast.upper(), cast.lower()).number()).isEqualTo(64);
    }

    @Test
    @DisplayName("Numbers reduce via mod 8 with remainder 0 read as 8 (Khôn), per the primary text")
    void numbersReduceModEightWithZeroAsEight() {
        var cast = MaiHoaCasting.fromNumbers(8, 16);
        assertThat(cast.upper()).isEqualTo(IChingTrigram.EARTH);
        assertThat(cast.lower()).isEqualTo(IChingTrigram.EARTH);
    }

    @Test
    @DisplayName("Moving line uses the un-reduced grand total mod 6, remainder 0 decided as line 6")
    void movingLineUsesDecidedModSixConvention() {
        // total = 3+6 = 9 -> 9 mod 6 = 3
        assertThat(MaiHoaCasting.fromNumbers(3, 6).movingLinePosition()).isEqualTo(3);
        // total = 4+8 = 12 -> exactly divisible by 6 -> decided as line 6 (DECISION_LOG.md 2026-08-24)
        assertThat(MaiHoaCasting.fromNumbers(4, 8).movingLinePosition()).isEqualTo(6);
        // total = 1+5 = 6 -> exactly divisible -> line 6
        assertThat(MaiHoaCasting.fromNumbers(1, 5).movingLinePosition()).isEqualTo(6);
    }

    @Test
    @DisplayName("Year-Month-Day-Hour: upper = year+month+day mod 8, lower adds the hour, moving line mod 6 of the full total")
    void dateTimeFormulaMatchesThePrimaryText() {
        // Made-up but arithmetically self-consistent inputs: year branch 5,
        // month 6, day 7 -> upperRaw = 18 -> mod 8 = 2 (Đoài/LAKE).
        // + hour branch 9 -> lowerRaw = 27 -> mod 8 = 3 (Ly/FIRE).
        // moving line = 27 mod 6 = 3.
        var cast = MaiHoaCasting.fromDateTime(5, 6, 7, 9);
        assertThat(cast.upper()).isEqualTo(IChingTrigram.LAKE);
        assertThat(cast.lower()).isEqualTo(IChingTrigram.FIRE);
        assertThat(cast.movingLinePosition()).isEqualTo(3);
    }
}
