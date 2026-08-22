package io.destinyos.engines.fengshui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * The Kua formula, against worked examples nobody here produced.
 *
 * <p>Sources for the golden values (all retrieved 2026-08-22):
 * {@code nguyenthehoa.com/tinh-cung-menh-theo-cung-phi-bat-trach/} publishes two
 * fully worked examples for 1978; a separate page states that a male born 1990
 * is cung Khảm; and {@code hoc.kabala.vn} plus {@code wofs.com} both give the
 * "5" substitution.
 */
class KuaNumberTest {

    @Nested
    @DisplayName("Published worked examples")
    class Published {

        @Test
        @DisplayName("Male born 1978 is Tốn — 7+8=15, 1+5=6, 10-6=4")
        void male1978IsTon() {
            assertThat(KuaNumber.forYear(1978, Gender.MALE)).isEqualTo(Trigram.TON);
            assertThat(KuaNumber.forYear(1978, Gender.MALE).kuaNumber()).isEqualTo(4);
        }

        @Test
        @DisplayName("Female born 1978 is Khôn — 5+15=20, 2+0=2")
        void female1978IsKhon() {
            // The source adds 5 to the *unreduced* digit sum and then reduces,
            // while this implementation reduces first. Both are correct and
            // always agree: adding a constant preserves the digital root, so
            // reduce(5 + S) == reduce(5 + reduce(S)) for every S. Worth stating,
            // because the two orders look like they could diverge.
            assertThat(KuaNumber.forYear(1978, Gender.FEMALE)).isEqualTo(Trigram.KHON);
            assertThat(KuaNumber.forYear(1978, Gender.FEMALE).kuaNumber()).isEqualTo(2);
        }

        @Test
        @DisplayName("Male born 1990 is Khảm — 9+0=9, 10-9=1")
        void male1990IsKham() {
            assertThat(KuaNumber.forYear(1990, Gender.MALE)).isEqualTo(Trigram.KHAM);
        }

        @Test
        @DisplayName("The two genders genuinely differ for the same year")
        void formulasAreNotSymmetric() {
            // R7 flags the male/female asymmetry as something a plausible
            // simplification would smooth over, so it is asserted rather than
            // assumed. 1978 is the year both published examples cover.
            assertThat(KuaNumber.forYear(1978, Gender.MALE))
                    .isNotEqualTo(KuaNumber.forYear(1978, Gender.FEMALE));
        }
    }

    @Nested
    @DisplayName("The 2000 discontinuity")
    class Discontinuity {

        @Test
        @DisplayName("The formula changes at 2000, so 1999 and 2000 are not one sequence")
        void formulaChangesAt2000() {
            // Both cited sources give a different constant from 2000 onwards.
            // 1999: a = 9+9 = 18 -> 9. Male 10-9 = 1 (Khảm). Female 5+9 = 14 -> 5
            // -> substituted to Cấn.
            assertThat(KuaNumber.forYear(1999, Gender.MALE)).isEqualTo(Trigram.KHAM);
            assertThat(KuaNumber.forYear(1999, Gender.FEMALE)).isEqualTo(Trigram.CAN);

            // 2000: a = 0. Male 9-0 = 9 (Ly). Female 6+0 = 6 (Kiền).
            assertThat(KuaNumber.forYear(2000, Gender.MALE)).isEqualTo(Trigram.LY);
            assertThat(KuaNumber.forYear(2000, Gender.FEMALE)).isEqualTo(Trigram.KIEN);
        }

        @Test
        @DisplayName("A male result of 0 becomes Ly, per the Vietnamese source's own note")
        void maleZeroBecomesLy() {
            // "nếu b = 0 thì lấy cung Ly". Arises from the 2000-onwards formula
            // when a == 9, e.g. 2007: 0+7 = 7... 2018: 1+8 = 9, so 9-9 = 0.
            assertThat(KuaNumber.forYear(2018, Gender.MALE)).isEqualTo(Trigram.LY);
        }
    }

    @Nested
    @DisplayName("The centre (5) substitution")
    class CentreSubstitution {

        @Test
        @DisplayName("No year and gender ever yields Kua 5 — there is no Kua 5 trigram")
        void kuaFiveNeverEscapes() {
            // 5 is the centre of the Lạc Thư square and has no direction, which
            // is why the substitution exists at all. If it ever leaked,
            // Trigram.ofKuaNumber would throw - so this sweep is also a check
            // that the substitution is reached on every path.
            for (int year = 1900; year <= 2100; year++) {
                for (Gender gender : Gender.values()) {
                    var trigram = KuaNumber.forYear(year, gender);
                    assertThat(trigram.kuaNumber())
                            .as("Kua for %d %s", year, gender)
                            .isNotEqualTo(5)
                            .isBetween(1, 9);
                }
            }
        }

        @Test
        @DisplayName("Males take Khôn and females Cấn when the formula gives 5")
        void substitutionIsGendered() {
            // 1976: 7+6 = 13 -> 4. Male 10-4 = 6 (Kiền), no substitution.
            // 1977: 7+7 = 14 -> 5. Male 10-5 = 5 -> substituted to Khôn.
            assertThat(KuaNumber.forYear(1977, Gender.MALE)).isEqualTo(Trigram.KHON);

            // Female 5 + a == 5 requires a == 0 or a digital root of 5:
            // 1999 gives a = 9, 5+9 = 14 -> 5 -> substituted to Cấn.
            assertThat(KuaNumber.forYear(1999, Gender.FEMALE)).isEqualTo(Trigram.CAN);

            // Both substitutes are West group, which both sources note.
            assertThat(Trigram.KHON.group()).isEqualTo(TrigramGroup.WEST);
            assertThat(Trigram.CAN.group()).isEqualTo(TrigramGroup.WEST);
        }
    }

    @Nested
    @DisplayName("Contract")
    class Contract {

        @Test
        @DisplayName("Gender is required, with no neutral default")
        void genderIsRequired() {
            assertThatThrownBy(() -> KuaNumber.forYear(1990, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Kua 5 cannot be looked up as a trigram, and says why")
        void trigramFiveDoesNotExist() {
            assertThatThrownBy(() -> Trigram.ofKuaNumber(5))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("centre");
        }

        @Test
        @DisplayName("The eight trigrams cover the eight directions exactly once each")
        void trigramsCoverEveryDirection() {
            for (CompassDirection direction : CompassDirection.values()) {
                assertThat(Trigram.ofDirection(direction).direction()).isEqualTo(direction);
            }
            assertThat(Trigram.values()).hasSize(8);
            assertThat(CompassDirection.values()).hasSize(8);
        }
    }
}
