package io.destinyos.calendar;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * The Ngũ Hành and Âm Dương attributes of the stems and branches, and the two
 * relation cycles.
 *
 * <p>These tables have no school variation to test <em>between</em>, so what
 * matters instead is that they are transcribed correctly and that the derived
 * cycles are algebraically consistent. A single transposed pair here would
 * silently corrupt every Thập Thần in every Bát Tự chart, and nothing else in
 * the system would notice.
 */
class CanChiAttributesTest {

    @Nested
    @DisplayName("Heavenly Stems")
    class Stems {

        @Test
        @DisplayName("Elements pair in cycle order: Mộc, Hỏa, Thổ, Kim, Thủy")
        void elementsPairInCycleOrder() {
            assertThat(HeavenlyStem.GIAP.element()).isEqualTo(FiveElement.WOOD);
            assertThat(HeavenlyStem.AT.element()).isEqualTo(FiveElement.WOOD);
            assertThat(HeavenlyStem.BINH.element()).isEqualTo(FiveElement.FIRE);
            assertThat(HeavenlyStem.DINH.element()).isEqualTo(FiveElement.FIRE);
            assertThat(HeavenlyStem.MAU.element()).isEqualTo(FiveElement.EARTH);
            assertThat(HeavenlyStem.KY.element()).isEqualTo(FiveElement.EARTH);
            assertThat(HeavenlyStem.CANH.element()).isEqualTo(FiveElement.METAL);
            assertThat(HeavenlyStem.TAN.element()).isEqualTo(FiveElement.METAL);
            assertThat(HeavenlyStem.NHAM.element()).isEqualTo(FiveElement.WATER);
            assertThat(HeavenlyStem.QUY.element()).isEqualTo(FiveElement.WATER);
        }

        @Test
        @DisplayName("Odd-position stems are Dương, even-position Âm")
        void polarityFollowsCyclePosition() {
            Set<HeavenlyStem> yang = EnumSet.of(HeavenlyStem.GIAP, HeavenlyStem.BINH,
                    HeavenlyStem.MAU, HeavenlyStem.CANH, HeavenlyStem.NHAM);

            for (HeavenlyStem stem : HeavenlyStem.values()) {
                assertThat(stem.polarity())
                        .as("polarity of %s", stem)
                        .isEqualTo(yang.contains(stem) ? YinYang.YANG : YinYang.YIN);
            }
        }

        @Test
        @DisplayName("Each element has exactly one Dương and one Âm stem")
        void everyElementHasBothPolarities() {
            for (FiveElement element : FiveElement.values()) {
                long yang = java.util.Arrays.stream(HeavenlyStem.values())
                        .filter(s -> s.element() == element && s.polarity() == YinYang.YANG)
                        .count();
                long yin = java.util.Arrays.stream(HeavenlyStem.values())
                        .filter(s -> s.element() == element && s.polarity() == YinYang.YIN)
                        .count();

                assertThat(yang).as("Dương stems of %s", element).isEqualTo(1);
                assertThat(yin).as("Âm stems of %s", element).isEqualTo(1);
            }
        }
    }

    @Nested
    @DisplayName("Earthly Branches")
    class Branches {

        @Test
        @DisplayName("Elements match the standard branch table")
        void elementsMatchTable() {
            assertThat(EarthlyBranch.RAT.element()).isEqualTo(FiveElement.WATER);
            assertThat(EarthlyBranch.OX.element()).isEqualTo(FiveElement.EARTH);
            assertThat(EarthlyBranch.TIGER.element()).isEqualTo(FiveElement.WOOD);
            assertThat(EarthlyBranch.RABBIT.element()).isEqualTo(FiveElement.WOOD);
            assertThat(EarthlyBranch.DRAGON.element()).isEqualTo(FiveElement.EARTH);
            assertThat(EarthlyBranch.SNAKE.element()).isEqualTo(FiveElement.FIRE);
            assertThat(EarthlyBranch.HORSE.element()).isEqualTo(FiveElement.FIRE);
            assertThat(EarthlyBranch.GOAT.element()).isEqualTo(FiveElement.EARTH);
            assertThat(EarthlyBranch.MONKEY.element()).isEqualTo(FiveElement.METAL);
            assertThat(EarthlyBranch.ROOSTER.element()).isEqualTo(FiveElement.METAL);
            assertThat(EarthlyBranch.DOG.element()).isEqualTo(FiveElement.EARTH);
            assertThat(EarthlyBranch.PIG.element()).isEqualTo(FiveElement.WATER);
        }

        @Test
        @DisplayName("Thổ appears four times — the branch table is not a 12/5 even split")
        void earthAppearsFourTimes() {
            // Worth asserting explicitly: someone "tidying" this table into an
            // even distribution would break the four Thổ branches (Sửu, Thìn,
            // Mùi, Tuất) that carry the seasonal transitions.
            long earth = java.util.Arrays.stream(EarthlyBranch.values())
                    .filter(b -> b.element() == FiveElement.EARTH)
                    .count();

            assertThat(earth).isEqualTo(4);
        }

        @Test
        @DisplayName("Odd-position branches are Dương, even-position Âm")
        void polarityFollowsCyclePosition() {
            for (EarthlyBranch branch : EarthlyBranch.values()) {
                YinYang expected = branch.index() % 2 == 1 ? YinYang.YANG : YinYang.YIN;
                assertThat(branch.polarity()).as("polarity of %s", branch).isEqualTo(expected);
            }
        }
    }

    @Nested
    @DisplayName("Five Element cycles")
    class Cycles {

        @Test
        @DisplayName("Tương sinh is the documented cycle and is a permutation")
        void generationCycle() {
            assertThat(FiveElement.WOOD.generates()).isEqualTo(FiveElement.FIRE);
            assertThat(FiveElement.FIRE.generates()).isEqualTo(FiveElement.EARTH);
            assertThat(FiveElement.EARTH.generates()).isEqualTo(FiveElement.METAL);
            assertThat(FiveElement.METAL.generates()).isEqualTo(FiveElement.WATER);
            assertThat(FiveElement.WATER.generates()).isEqualTo(FiveElement.WOOD);

            assertThat(java.util.Arrays.stream(FiveElement.values())
                    .map(FiveElement::generates).distinct().count()).isEqualTo(5);
        }

        @Test
        @DisplayName("Tương khắc is the documented cycle and is a permutation")
        void controlCycle() {
            assertThat(FiveElement.WOOD.controls()).isEqualTo(FiveElement.EARTH);
            assertThat(FiveElement.EARTH.controls()).isEqualTo(FiveElement.WATER);
            assertThat(FiveElement.WATER.controls()).isEqualTo(FiveElement.FIRE);
            assertThat(FiveElement.FIRE.controls()).isEqualTo(FiveElement.METAL);
            assertThat(FiveElement.METAL.controls()).isEqualTo(FiveElement.WOOD);

            assertThat(java.util.Arrays.stream(FiveElement.values())
                    .map(FiveElement::controls).distinct().count()).isEqualTo(5);
        }

        @Test
        @DisplayName("The inverses really are inverses")
        void inversesRoundTrip() {
            for (FiveElement element : FiveElement.values()) {
                assertThat(element.generates().generatedBy()).isEqualTo(element);
                assertThat(element.controls().controlledBy()).isEqualTo(element);
            }
        }

        @Test
        @DisplayName("relationTo partitions the five elements exactly once each")
        void relationToIsATotalPartition() {
            // This is the property the Ten Gods derivation depends on: for any
            // element, each of the five relations names exactly one element, so
            // the 10-way Thập Thần switch has no unreachable and no ambiguous case.
            for (FiveElement subject : FiveElement.values()) {
                var seen = EnumSet.noneOf(FiveElement.ElementRelation.class);
                for (FiveElement other : FiveElement.values()) {
                    assertThat(seen.add(subject.relationTo(other)))
                            .as("%s -> %s produced a duplicate relation", subject, other)
                            .isTrue();
                }
                assertThat(seen).hasSize(5);
            }
        }
    }
}
