package io.destinyos.calendar;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The Tàng Can table, checked against both cited sources.
 *
 * <p>The assertions below are the <em>intersection</em> of what
 * 4thuman.com (VN) and imperialharvest.com (EN) state — the stem sets and the
 * principal stem, on which they agree for all twelve branches. The two branches
 * where they disagree on central-vs-residual ordering are asserted to be
 * <em>flagged</em> as disputed, not asserted to have one particular ordering:
 * a test that pinned one source's ordering would quietly convert an open
 * question into a project decision nobody made.
 */
class HiddenStemsTest {

    @Test
    @DisplayName("Every branch conceals its own element as the principal stem")
    void principalStemSharesTheBranchElement() {
        // Both sources state this as the defining property of the 主氣, and it
        // is the one structural check that catches a transposition anywhere in
        // the table without needing to trust the transcription twice.
        for (EarthlyBranch branch : EarthlyBranch.values()) {
            assertThat(HiddenStems.of(branch).principal().element())
                    .as("principal hidden stem of %s", branch)
                    .isEqualTo(branch.element());
        }
    }

    @Test
    @DisplayName("The stem sets match both cited sources, branch by branch")
    void stemSetsMatchBothSources() {
        assertStems(EarthlyBranch.RAT, HeavenlyStem.QUY);
        assertStems(EarthlyBranch.OX, HeavenlyStem.KY, HeavenlyStem.TAN, HeavenlyStem.QUY);
        assertStems(EarthlyBranch.TIGER, HeavenlyStem.GIAP, HeavenlyStem.BINH, HeavenlyStem.MAU);
        assertStems(EarthlyBranch.RABBIT, HeavenlyStem.AT);
        assertStems(EarthlyBranch.DRAGON, HeavenlyStem.MAU, HeavenlyStem.AT, HeavenlyStem.QUY);
        assertStems(EarthlyBranch.SNAKE, HeavenlyStem.BINH, HeavenlyStem.MAU, HeavenlyStem.CANH);
        assertStems(EarthlyBranch.HORSE, HeavenlyStem.DINH, HeavenlyStem.KY);
        assertStems(EarthlyBranch.GOAT, HeavenlyStem.KY, HeavenlyStem.DINH, HeavenlyStem.AT);
        assertStems(EarthlyBranch.MONKEY, HeavenlyStem.CANH, HeavenlyStem.NHAM, HeavenlyStem.MAU);
        assertStems(EarthlyBranch.ROOSTER, HeavenlyStem.TAN);
        assertStems(EarthlyBranch.DOG, HeavenlyStem.MAU, HeavenlyStem.TAN, HeavenlyStem.DINH);
        assertStems(EarthlyBranch.PIG, HeavenlyStem.NHAM, HeavenlyStem.GIAP);
    }

    @Test
    @DisplayName("Tý, Mão and Dậu conceal only their principal stem")
    void pureBranchesHaveNoAdditionalStems() {
        assertThat(HiddenStems.of(EarthlyBranch.RAT).additional()).isEmpty();
        assertThat(HiddenStems.of(EarthlyBranch.RABBIT).additional()).isEmpty();
        assertThat(HiddenStems.of(EarthlyBranch.ROOSTER).additional()).isEmpty();
    }

    @Test
    @DisplayName("Exactly Sửu and Tỵ are flagged as having a disputed role ordering")
    void onlyTheTwoDisputedBranchesAreFlagged() {
        for (EarthlyBranch branch : EarthlyBranch.values()) {
            boolean expected = branch == EarthlyBranch.OX || branch == EarthlyBranch.SNAKE;
            assertThat(HiddenStems.of(branch).roleOrderingDisputed())
                    .as("role ordering dispute flag for %s", branch)
                    .isEqualTo(expected);
        }
    }

    @Test
    @DisplayName("No branch conceals more than three stems, and none conceals none")
    void stemCountsStayWithinTheDocumentedRange() {
        for (EarthlyBranch branch : EarthlyBranch.values()) {
            assertThat(HiddenStems.of(branch).all())
                    .as("hidden stems of %s", branch)
                    .hasSizeBetween(1, 3)
                    .doesNotHaveDuplicates();
        }
    }

    private static void assertStems(EarthlyBranch branch, HeavenlyStem... expected) {
        assertThat(HiddenStems.of(branch).all())
                .as("hidden stems of %s", branch)
                .containsExactlyInAnyOrderElementsOf(List.of(expected));
        assertThat(HiddenStems.of(branch).principal())
                .as("principal hidden stem of %s", branch)
                .isEqualTo(expected[0]);
    }
}
