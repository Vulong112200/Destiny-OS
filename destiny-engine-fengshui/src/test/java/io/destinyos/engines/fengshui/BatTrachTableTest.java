package io.destinyos.engines.fengshui;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * The derived Bát Trạch table, checked against published tables and against the
 * structural properties the derivation guarantees.
 *
 * <p>The published rows below were read off pages nobody here wrote
 * (CLAUDE.md §32). The structural tests are not a substitute for them — they are
 * what makes a <em>single</em> disagreeing source diagnosable instead of
 * paralysing, which is exactly what happened while building this: the only
 * complete English-language table found disagrees in four cells, and these
 * invariants plus a Vietnamese source identified those four as its error.
 */
class BatTrachTableTest {

    @Nested
    @DisplayName("Golden rows from published sources")
    class Published {

        @Test
        @DisplayName("Cấn (Kua 8) matches nguyenthehoa.com on all eight directions")
        void canRowMatchesVietnameseSource() {
            // The decisive row. It contains both cells where the English table
            // disagrees, and this Vietnamese source sides with the derivation:
            // "Đông Nam – Tuyệt Mệnh" and "Đông – Lục Sát".
            assertRow(Trigram.CAN, Map.of(
                    CompassDirection.SOUTHWEST, BatTrachRelation.SINH_KHI,
                    CompassDirection.WEST, BatTrachRelation.DIEN_NIEN,
                    CompassDirection.NORTHWEST, BatTrachRelation.THIEN_Y,
                    CompassDirection.NORTHEAST, BatTrachRelation.PHUC_VI,
                    CompassDirection.SOUTHEAST, BatTrachRelation.TUYET_MENH,
                    CompassDirection.EAST, BatTrachRelation.LUC_SAT,
                    CompassDirection.SOUTH, BatTrachRelation.HOA_HAI,
                    CompassDirection.NORTH, BatTrachRelation.NGU_QUY));
        }

        @Test
        @DisplayName("Khôn (Kua 2) matches masterseanchan.com's dedicated Kua 2 page")
        void khonRowMatchesEnglishSource() {
            // A row both sources agree on, from a page dedicated to Kua 2 rather
            // than from the matrix - so it is an independent statement, not the
            // same table read twice.
            assertRow(Trigram.KHON, Map.of(
                    CompassDirection.NORTHEAST, BatTrachRelation.SINH_KHI,
                    CompassDirection.WEST, BatTrachRelation.THIEN_Y,
                    CompassDirection.NORTHWEST, BatTrachRelation.DIEN_NIEN,
                    CompassDirection.SOUTHWEST, BatTrachRelation.PHUC_VI,
                    CompassDirection.EAST, BatTrachRelation.HOA_HAI,
                    CompassDirection.SOUTHEAST, BatTrachRelation.NGU_QUY,
                    CompassDirection.SOUTH, BatTrachRelation.LUC_SAT,
                    CompassDirection.NORTH, BatTrachRelation.TUYET_MENH));
        }

        @Test
        @DisplayName("Chấn (Kua 3) matches both sources, which agree on this row")
        void chanRowMatchesBothSources() {
            assertRow(Trigram.CHAN, Map.of(
                    CompassDirection.SOUTH, BatTrachRelation.SINH_KHI,
                    CompassDirection.NORTH, BatTrachRelation.THIEN_Y,
                    CompassDirection.SOUTHEAST, BatTrachRelation.DIEN_NIEN,
                    CompassDirection.EAST, BatTrachRelation.PHUC_VI,
                    CompassDirection.SOUTHWEST, BatTrachRelation.HOA_HAI,
                    CompassDirection.NORTHWEST, BatTrachRelation.NGU_QUY,
                    CompassDirection.NORTHEAST, BatTrachRelation.LUC_SAT,
                    CompassDirection.WEST, BatTrachRelation.TUYET_MENH));
        }

        @Test
        @DisplayName("Ly (Kua 9) matches masterseanchan.com's Kua 9 row")
        void lyRowMatchesEnglishSource() {
            assertRow(Trigram.LY, Map.of(
                    CompassDirection.EAST, BatTrachRelation.SINH_KHI,
                    CompassDirection.SOUTHEAST, BatTrachRelation.THIEN_Y,
                    CompassDirection.NORTH, BatTrachRelation.DIEN_NIEN,
                    CompassDirection.SOUTH, BatTrachRelation.PHUC_VI,
                    CompassDirection.NORTHEAST, BatTrachRelation.HOA_HAI,
                    CompassDirection.WEST, BatTrachRelation.NGU_QUY,
                    CompassDirection.SOUTHWEST, BatTrachRelation.LUC_SAT,
                    CompassDirection.NORTHWEST, BatTrachRelation.TUYET_MENH));
        }

        @Test
        @DisplayName("The Tuyệt Mệnh pairs are exactly the four the Vietnamese source names")
        void tuyetMenhPairsMatchTheNamedList()  {
            // "Tuyệt Mệnh: Càn-Ly, Khôn-Khảm, Cấn-Tốn, Đoài-Chấn" - quoted
            // directly, and the single most useful sentence found for this item:
            // it is what settles the four disputed cells.
            assertThat(BatTrachTable.relation(Trigram.KIEN, Trigram.LY))
                    .isEqualTo(BatTrachRelation.TUYET_MENH);
            assertThat(BatTrachTable.relation(Trigram.KHON, Trigram.KHAM))
                    .isEqualTo(BatTrachRelation.TUYET_MENH);
            assertThat(BatTrachTable.relation(Trigram.CAN, Trigram.TON))
                    .isEqualTo(BatTrachRelation.TUYET_MENH);
            assertThat(BatTrachTable.relation(Trigram.DOAI, Trigram.CHAN))
                    .isEqualTo(BatTrachRelation.TUYET_MENH);

            // And nothing else is: four unordered pairs, eight ordered cells.
            long cells = 0;
            for (Trigram a : Trigram.values()) {
                for (Trigram b : Trigram.values()) {
                    if (BatTrachTable.relation(a, b) == BatTrachRelation.TUYET_MENH) {
                        cells++;
                    }
                }
            }
            assertThat(cells).isEqualTo(8);
        }

        private static void assertRow(Trigram lifeTrigram,
                                      Map<CompassDirection, BatTrachRelation> expected) {
            assertThat(expected)
                    .as("a published row must state all eight directions")
                    .hasSize(8);
            expected.forEach((direction, relation) ->
                    assertThat(BatTrachTable.relation(lifeTrigram, direction))
                            .as("%s at %s", lifeTrigram, direction)
                            .isEqualTo(relation));
        }
    }

    @Nested
    @DisplayName("Structural invariants the derivation guarantees")
    class Invariants {

        @Test
        @DisplayName("Every row is a permutation of the eight relations")
        void eachRowIsAPermutation() {
            for (Trigram lifeTrigram : Trigram.values()) {
                var seen = EnumSet.noneOf(BatTrachRelation.class);
                for (CompassDirection direction : CompassDirection.values()) {
                    assertThat(seen.add(BatTrachTable.relation(lifeTrigram, direction)))
                            .as("%s produced a duplicate relation at %s", lifeTrigram, direction)
                            .isTrue();
                }
                assertThat(seen).hasSize(BatTrachRelation.values().length);
            }
        }

        @Test
        @DisplayName("Phục Vị sits on the trigram's own direction, and only there")
        void phucViIsSelf() {
            for (Trigram lifeTrigram : Trigram.values()) {
                for (CompassDirection direction : CompassDirection.values()) {
                    boolean own = lifeTrigram.direction() == direction;
                    assertThat(BatTrachTable.relation(lifeTrigram, direction)
                            == BatTrachRelation.PHUC_VI)
                            .as("%s at %s", lifeTrigram, direction)
                            .isEqualTo(own);
                }
            }
        }

        @Test
        @DisplayName("The relation is symmetric — a difference does not care which side computes it")
        void relationIsSymmetric() {
            // The invariant that identified the four wrong cells in the one
            // complete English table. A line-difference rule is symmetric by
            // construction, so an asymmetric table has an error in it.
            for (Trigram a : Trigram.values()) {
                for (Trigram b : Trigram.values()) {
                    assertThat(BatTrachTable.relation(a, b))
                            .as("rel(%s,%s) vs rel(%s,%s)", a, b, b, a)
                            .isEqualTo(BatTrachTable.relation(b, a));
                }
            }
        }

        @Test
        @DisplayName("All four auspicious directions fall inside the trigram's own group")
        void auspiciousDirectionsStayInGroup() {
            // This is what Đông tứ trạch / Tây tứ trạch *is*, and it is a
            // consequence of the derivation rather than a separate rule - so if
            // it ever failed, either the group assignments or the line values
            // would be wrong.
            for (Trigram lifeTrigram : Trigram.values()) {
                var directions = BatTrachTable.allDirections(lifeTrigram);
                var auspicious = directions.entrySet().stream()
                        .filter(e -> e.getValue().auspicious())
                        .map(Map.Entry::getKey)
                        .toList();

                assertThat(auspicious).as("%s auspicious count", lifeTrigram).hasSize(4);
                assertThat(auspicious).allSatisfy(direction ->
                        assertThat(Trigram.ofDirection(direction).group())
                                .as("%s: %s should be in its own group", lifeTrigram, direction)
                                .isEqualTo(lifeTrigram.group()));
            }
        }

        @Test
        @DisplayName("The four inauspicious relations form a Latin square on East x West")
        void inauspiciousRelationsFormALatinSquare() {
            // Follows from symmetry plus group containment, and is what makes a
            // partially-known table reconstructible. Asserted because it is the
            // strongest single statement about the table's shape.
            var east = EnumSet.noneOf(Trigram.class);
            var west = EnumSet.noneOf(Trigram.class);
            for (Trigram trigram : Trigram.values()) {
                (trigram.group() == TrigramGroup.EAST ? east : west).add(trigram);
            }
            assertThat(east).hasSize(4);
            assertThat(west).hasSize(4);

            for (Trigram e : east) {
                var inRow = EnumSet.noneOf(BatTrachRelation.class);
                for (Trigram w : west) {
                    assertThat(inRow.add(BatTrachTable.relation(e, w)))
                            .as("%s repeats a relation across the West group", e)
                            .isTrue();
                }
                assertThat(inRow).allSatisfy(r -> assertThat(r.auspicious()).isFalse());
            }
            for (Trigram w : west) {
                var inColumn = EnumSet.noneOf(BatTrachRelation.class);
                for (Trigram e : east) {
                    assertThat(inColumn.add(BatTrachTable.relation(w, e)))
                            .as("%s repeats a relation across the East group", w)
                            .isTrue();
                }
            }
        }

        @Test
        @DisplayName("Every relation appears exactly eight times across the whole table")
        void relationsAreEvenlyDistributed() {
            Map<BatTrachRelation, Integer> counts = new EnumMap<>(BatTrachRelation.class);
            for (Trigram a : Trigram.values()) {
                for (Trigram b : Trigram.values()) {
                    counts.merge(BatTrachTable.relation(a, b), 1, Integer::sum);
                }
            }

            assertThat(counts).hasSize(BatTrachRelation.values().length);
            assertThat(counts.values()).allMatch(count -> count == 8);
        }
    }
}
