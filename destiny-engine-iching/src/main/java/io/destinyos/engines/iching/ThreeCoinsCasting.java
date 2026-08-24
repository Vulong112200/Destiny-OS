package io.destinyos.engines.iching;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 三錢起卦 — three coins tossed six times, one toss per line, bottom to top.
 *
 * <p>Value convention (verified across independent secondary sources, see
 * {@code docs/research_drafts/R12_iching_maihoa.md} §3): mặt Dương = 3, mặt
 * Âm = 2. Summing three tosses gives 6, 7, 8 or 9 with probability 1:3:3:1 —
 * derived by exhaustive enumeration in
 * {@code docs/research_drafts/VERIFICATION_OPUS_R12.md} §A6 and reproduced
 * here structurally: this class draws three independent fair coins rather
 * than sampling from a hardcoded 1:3:3:1 table, so the distribution is a
 * consequence of the code, not an assertion about it.
 *
 * <p>The one genuine dispute found (which physical coin face counts as Dương)
 * is a labelling convention for a real coin a user tosses themselves, not an
 * arithmetic one — it has no effect here, since both faces of a simulated
 * coin are exchangeable.
 */
final class ThreeCoinsCasting {

    private ThreeCoinsCasting() {
    }

    static List<LineValue> cast(Random random) {
        List<LineValue> lines = new ArrayList<>(6);
        for (int i = 0; i < 6; i++) {
            int total = tossThreeCoins(random);
            lines.add(LineValue.fromTotal(total));
        }
        return lines;
    }

    private static int tossThreeCoins(Random random) {
        return coinTotal(random.nextBoolean(), random.nextBoolean(), random.nextBoolean());
    }

    /** Exposed for exhaustive-enumeration testing only — see {@code ThreeCoinsCastingTest}. */
    static int coinTotal(boolean coin1Yang, boolean coin2Yang, boolean coin3Yang) {
        return value(coin1Yang) + value(coin2Yang) + value(coin3Yang);
    }

    private static int value(boolean yang) {
        return yang ? 3 : 2;
    }
}
