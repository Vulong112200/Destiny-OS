package io.destinyos.engines.numerology;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Verifies the adopted R8 normalization policy (docs/RESEARCH_BLOCKERS.md):
 * NFD decompose + strip combining marks, with an explicit separate
 * substitution for {@code đ}/{@code Đ} since Unicode gives it no canonical
 * decomposition.
 */
class VietnameseNameNormalizerTest {

    @Test
    @DisplayName("Standard diacritics are stripped via NFD decomposition")
    void diacriticsAreStripped() {
        var result = VietnameseNameNormalizer.normalize("Nguyễn Văn Tuấn");

        assertThat(result.displayForm()).isEqualTo("Nguyen Van Tuan");
        assertThat(result.lettersOnly()).isEqualTo("NGUYENVANTUAN");
    }

    @Test
    @DisplayName("dd with stroke requires its own substitution - NFD alone does not touch it")
    void dStrokeIsFoldedExplicitly() {
        // This is the specific Unicode fact R8's research established: U+0111
        // has no canonical decomposition, unlike e.g. e-with-circumflex.
        var result = VietnameseNameNormalizer.normalize("Đặng Đình Đức");

        assertThat(result.displayForm()).isEqualTo("Dang Dinh Duc");
        assertThat(result.lettersOnly()).isEqualTo("DANGDINHDUC");
    }

    @Test
    @DisplayName("The original name is always preserved, never silently discarded")
    void originalIsPreserved() {
        var result = VietnameseNameNormalizer.normalize("Lê Thị Đẹp");

        assertThat(result.original()).isEqualTo("Lê Thị Đẹp");
        assertThat(result.displayForm()).isEqualTo("Le Thi Dep");
    }

    @Test
    @DisplayName("Hyphens and apostrophes in the display form are accepted but excluded from lettersOnly")
    void punctuationHandling() {
        var result = VietnameseNameNormalizer.normalize("Nguyễn-Anh O'Brien");

        assertThat(result.displayForm()).contains("-").contains("'");
        assertThat(result.lettersOnly()).doesNotContain("-").doesNotContain("'");
        assertThat(result.lettersOnly()).isEqualTo("NGUYENANHOBRIEN");
    }

    @Test
    @DisplayName("lettersOnly is upper-cased regardless of input casing")
    void lettersOnlyIsUppercased() {
        var result = VietnameseNameNormalizer.normalize("nguyễn văn an");
        assertThat(result.lettersOnly()).isEqualTo("NGUYENVANAN");
    }

    @Test
    @DisplayName("A digit or symbol after normalization is rejected, not silently dropped")
    void unrecognisedCharacterIsRejected() {
        assertThatThrownBy(() -> VietnameseNameNormalizer.normalize("Nguyễn Văn 123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("R8");
    }

    @Test
    @DisplayName("A blank name is rejected")
    void blankNameIsRejected() {
        assertThatThrownBy(() -> VietnameseNameNormalizer.normalize("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("tryNormalize returns empty rather than throwing")
    void tryNormalizeIsNonThrowing() {
        assertThat(VietnameseNameNormalizer.tryNormalize("Nguyễn Văn An")).isPresent();
        assertThat(VietnameseNameNormalizer.tryNormalize("123")).isEmpty();
    }
}
