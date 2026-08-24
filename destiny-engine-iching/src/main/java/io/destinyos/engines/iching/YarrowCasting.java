package io.destinyos.engines.iching;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 大衍筮法 / 蓍草筮法 — the 50-stalk yarrow procedure, from 繫辭上傳:
 * 「大衍之數五十，其用四十有九。分而為二以象兩，掛一以象三，揲之以四以象四時，
 * 歸奇於扐以象閏」, and 「十有八變而成卦」 (18 changes make one hexagram — 3
 * changes per line × 6 lines), cross-checked across 3 independent hosts in
 * {@code docs/research_drafts/R12_iching_maihoa.md} §4.
 *
 * <p><strong>This produces a different distribution over {@link LineValue}
 * than {@link ThreeCoinsCasting} — a real methodological difference, not an
 * implementation detail (the project's own R12 problem statement asked this
 * to be confirmed).</strong> The closed-form distribution
 * (P(6)=1/16, P(7)=5/16, P(8)=7/16, P(9)=3/16, i.e. ratio 1:5:7:3) is derived
 * from first principles — not merely cited from secondary sources — in
 * {@code docs/research_drafts/VERIFICATION_OPUS_R12.md} §B, and is
 * reproduced here as a direct implementation of that derivation's per-change
 * removal tables, rather than as a hardcoded sampling table, for the same
 * reason {@link ThreeCoinsCasting} draws real coins instead of sampling
 * 1:3:3:1 directly: the distribution should be a consequence of the code.
 *
 * <p><strong>The one assumption this implementation makes, stated plainly.</strong>
 * Each of the three changes-per-line splits the current pile into two random
 * heaps; what matters for the removal count is only the left heap's size
 * modulo 4 (see the verification file for why). This class draws that
 * residue directly and uniformly (via {@code random.nextInt(4)}) rather than
 * simulating an actual pile split and reducing it — for a human counting
 * real stalks, "the residues are uniform" is an idealisation; for this
 * software implementation, it is not an approximation but the literal
 * definition of how the residue is chosen.
 */
final class YarrowCasting {

    private YarrowCasting() {
    }

    static List<LineValue> cast(Random random) {
        List<LineValue> lines = new ArrayList<>(6);
        for (int i = 0; i < 6; i++) {
            int removedInChangeOne = changeOneRemoval(random.nextInt(4));
            int removedInChangeTwo = changeTwoOrThreeRemoval(random.nextInt(4));
            int removedInChangeThree = changeTwoOrThreeRemoval(random.nextInt(4));
            int remaining = 49 - removedInChangeOne - removedInChangeTwo - removedInChangeThree;
            lines.add(LineValue.fromTotal(remaining / 4));
        }
        return lines;
    }

    /**
     * First change, starting from 49 stalks. After setting aside the 掛一
     * stalk, the two heaps' sizes sum to 48 ≡ 0 (mod 4), so their remainders
     * (each read as 4, not 0, when evenly divisible — 揲之以四, the same
     * "remainder zero reads as the divisor" convention already primary-sourced
     * for the mod-8 trigram rule) sum to 4 or 8, giving a removal of 5 or 9.
     * Residue 0 (of the left heap mod 4) is the only one landing on 9.
     */
    static int changeOneRemoval(int leftHeapResidueMod4) {
        return leftHeapResidueMod4 == 0 ? 9 : 5;
    }

    /**
     * Second or third change, starting from a pile that is itself ≡ 0 (mod 4)
     * (guaranteed by every possible outcome of the prior change). The two
     * heaps' remainders sum to 3 or 7, giving a removal of 4 or 8, split
     * evenly across the four residue classes (0 and 3 -> 8; 1 and 2 -> 4).
     */
    static int changeTwoOrThreeRemoval(int leftHeapResidueMod4) {
        return (leftHeapResidueMod4 == 0 || leftHeapResidueMod4 == 3) ? 8 : 4;
    }

    /** Exposed for exhaustive-enumeration testing only — see {@code YarrowCastingTest}. */
    static int remainingStalksFor(int r1, int r2, int r3) {
        return 49 - changeOneRemoval(r1) - changeTwoOrThreeRemoval(r2) - changeTwoOrThreeRemoval(r3);
    }
}
