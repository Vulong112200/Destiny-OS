package io.destinyos.engines.astrology;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WholeSignHousesTest {

    @Test
    @DisplayName("House 1 is always the Ascendant's own sign")
    void houseOneIsAscendantSign() {
        for (ZodiacSign ascendant : ZodiacSign.values()) {
            assertThat(WholeSignHouses.cusps(ascendant).get(AstrologicalHouse.HOUSE_1))
                    .as("Ascendant = %s", ascendant).isEqualTo(ascendant);
        }
    }

    @Test
    @DisplayName("Houses follow zodiac order and wrap after Pisces back to Aries")
    void housesWrapInZodiacOrder() {
        var cusps = WholeSignHouses.cusps(ZodiacSign.SAGITTARIUS);

        assertThat(cusps.get(AstrologicalHouse.HOUSE_1)).isEqualTo(ZodiacSign.SAGITTARIUS);
        assertThat(cusps.get(AstrologicalHouse.HOUSE_2)).isEqualTo(ZodiacSign.CAPRICORN);
        assertThat(cusps.get(AstrologicalHouse.HOUSE_3)).isEqualTo(ZodiacSign.AQUARIUS);
        assertThat(cusps.get(AstrologicalHouse.HOUSE_4)).isEqualTo(ZodiacSign.PISCES);
        // Wraps past Pisces back to Aries for house 5.
        assertThat(cusps.get(AstrologicalHouse.HOUSE_5)).isEqualTo(ZodiacSign.ARIES);
        assertThat(cusps.get(AstrologicalHouse.HOUSE_12)).isEqualTo(ZodiacSign.SCORPIO);
    }

    @Test
    @DisplayName("Every sign appears in exactly one house")
    void everySignAppearsExactlyOnce() {
        var cusps = WholeSignHouses.cusps(ZodiacSign.GEMINI);
        assertThat(cusps.values()).containsExactlyInAnyOrder(ZodiacSign.values());
    }

    @Test
    @DisplayName("houseOf is the inverse of cusps: looking up a sign returns its house")
    void houseOfInvertsCusps() {
        ZodiacSign ascendant = ZodiacSign.LEO;
        var cusps = WholeSignHouses.cusps(ascendant);
        cusps.forEach((house, sign) ->
                assertThat(WholeSignHouses.houseOf(ascendant, sign))
                        .as("sign %s", sign).isEqualTo(house));
    }
}
