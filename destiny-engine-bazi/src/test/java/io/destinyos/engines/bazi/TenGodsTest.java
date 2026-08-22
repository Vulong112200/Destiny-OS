package io.destinyos.engines.bazi;

import static org.assertj.core.api.Assertions.assertThat;

import io.destinyos.calendar.HeavenlyStem;
import java.util.EnumSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The Thập Thần derivation rule.
 *
 * <p>The Giáp Day Master row below is asserted stem by stem because two cited
 * sources state it explicitly — bazi-web.com works through exactly this case
 * ("if you're a Yang Wood 甲木 Day Master, Wood controls Earth, and Yin Earth
 * 己土 has the opposite polarity" → Chính Tài; Yang Earth 戊土 "shares your
 * polarity" → Thiên Tài), and phongthuykhaitoan.com states the same
 * same-polarity/opposite-polarity pairing for the 食/傷 and 印 pairs. The
 * bijection property is then asserted for all ten Day Masters, which is what
 * makes a transposition anywhere in the switch impossible to hide.
 */
class TenGodsTest {

    @Test
    @DisplayName("Giáp Day Master: all ten roles match the cited worked example")
    void giapDayMasterRow() {
        HeavenlyStem dm = HeavenlyStem.GIAP;

        assertThat(TenGods.of(dm, HeavenlyStem.GIAP)).isEqualTo(TenGod.TY_KIEN);
        assertThat(TenGods.of(dm, HeavenlyStem.AT)).isEqualTo(TenGod.KIEP_TAI);
        assertThat(TenGods.of(dm, HeavenlyStem.BINH)).isEqualTo(TenGod.THUC_THAN);
        assertThat(TenGods.of(dm, HeavenlyStem.DINH)).isEqualTo(TenGod.THUONG_QUAN);
        assertThat(TenGods.of(dm, HeavenlyStem.MAU)).isEqualTo(TenGod.THIEN_TAI);
        assertThat(TenGods.of(dm, HeavenlyStem.KY)).isEqualTo(TenGod.CHINH_TAI);
        assertThat(TenGods.of(dm, HeavenlyStem.CANH)).isEqualTo(TenGod.THAT_SAT);
        assertThat(TenGods.of(dm, HeavenlyStem.TAN)).isEqualTo(TenGod.CHINH_QUAN);
        assertThat(TenGods.of(dm, HeavenlyStem.NHAM)).isEqualTo(TenGod.THIEN_AN);
        assertThat(TenGods.of(dm, HeavenlyStem.QUY)).isEqualTo(TenGod.CHINH_AN);
    }

    @Test
    @DisplayName("Kỷ Day Master (Âm Thổ) exercises the opposite polarity side of every pair")
    void kyDayMasterRow() {
        // Giáp alone would leave the Âm Day Master half of every pair untested,
        // and polarity is precisely the axis that is easy to invert.
        HeavenlyStem dm = HeavenlyStem.KY;

        assertThat(TenGods.of(dm, HeavenlyStem.KY)).isEqualTo(TenGod.TY_KIEN);
        assertThat(TenGods.of(dm, HeavenlyStem.MAU)).isEqualTo(TenGod.KIEP_TAI);
        // Thổ sinh Kim: Tân is Âm, same polarity as Kỷ -> Thực Thần.
        assertThat(TenGods.of(dm, HeavenlyStem.TAN)).isEqualTo(TenGod.THUC_THAN);
        assertThat(TenGods.of(dm, HeavenlyStem.CANH)).isEqualTo(TenGod.THUONG_QUAN);
        // Thổ khắc Thủy: Quý is Âm -> Thiên Tài; Nhâm is Dương -> Chính Tài.
        assertThat(TenGods.of(dm, HeavenlyStem.QUY)).isEqualTo(TenGod.THIEN_TAI);
        assertThat(TenGods.of(dm, HeavenlyStem.NHAM)).isEqualTo(TenGod.CHINH_TAI);
        // Mộc khắc Thổ: Ất is Âm -> Thất Sát; Giáp is Dương -> Chính Quan.
        assertThat(TenGods.of(dm, HeavenlyStem.AT)).isEqualTo(TenGod.THAT_SAT);
        assertThat(TenGods.of(dm, HeavenlyStem.GIAP)).isEqualTo(TenGod.CHINH_QUAN);
        // Hỏa sinh Thổ: Đinh is Âm -> Thiên Ấn; Bính is Dương -> Chính Ấn.
        assertThat(TenGods.of(dm, HeavenlyStem.DINH)).isEqualTo(TenGod.THIEN_AN);
        assertThat(TenGods.of(dm, HeavenlyStem.BINH)).isEqualTo(TenGod.CHINH_AN);
    }

    @Test
    @DisplayName("For every Day Master the ten stems map onto the ten roles bijectively")
    void everyDayMasterRowIsABijection() {
        // This is the structural invariant a transcribed 10x10 table cannot
        // guarantee and the rule-based derivation does. If any relation or
        // polarity branch were duplicated, some role would appear twice and
        // another not at all - caught here for all 100 pairs at once.
        for (HeavenlyStem dayMaster : HeavenlyStem.values()) {
            var roles = EnumSet.noneOf(TenGod.class);
            for (HeavenlyStem target : HeavenlyStem.values()) {
                assertThat(roles.add(TenGods.of(dayMaster, target)))
                        .as("Day Master %s produced a duplicate role at target %s",
                                dayMaster, target)
                        .isTrue();
            }
            assertThat(roles)
                    .as("roles reachable from Day Master %s", dayMaster)
                    .hasSize(TenGod.values().length);
        }
    }

    @Test
    @DisplayName("A stem of the same element and polarity is always Tỷ Kiên — i.e. itself")
    void selfIsAlwaysTyKien() {
        for (HeavenlyStem stem : HeavenlyStem.values()) {
            assertThat(TenGods.of(stem, stem)).isEqualTo(TenGod.TY_KIEN);
        }
    }
}
