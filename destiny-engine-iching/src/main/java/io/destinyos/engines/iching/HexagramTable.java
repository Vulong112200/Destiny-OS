package io.destinyos.engines.iching;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The full 64-hexagram Văn Vương (King Wen) lookup table.
 *
 * <p><strong>Verification, not just transcription.</strong> This exact table
 * was checked three independent ways in
 * {@code docs/research_drafts/VERIFICATION_OPUS_R12.md} §A1: it is a
 * bijection (all 64 (upper, lower) pairs appear exactly once); every one of
 * the 32 King Wen pairs (2k-1, 2k) is either a 180° rotation (綜卦) or a full
 * yin/yang complement (錯卦) of its partner, a deep structural property a
 * single transposed row would break; and 56 of the 64 traditional two-element
 * names (e.g. 水火既濟, "Water over Fire") encode their (upper, lower) order
 * exactly.
 *
 * <p><strong>One pair the structural check cannot settle, recorded so a
 * future re-check does not rely on it alone.</strong> #63 (既濟, Water/Fire)
 * and #64 (未濟, Fire/Water) are simultaneously a 綜卦 and a 錯卦 of each
 * other, so both orientations satisfy the pair rule — only the naming
 * convention decides it. A first fetch of Chinese Wikipedia during research
 * had these two rows swapped; a second, independent source (and the naming
 * check) corrected it. See §A2 of the verification file.
 */
public final class HexagramTable {

    private static final List<Hexagram> BY_NUMBER = List.of(
        new Hexagram(1, IChingTrigram.HEAVEN, IChingTrigram.HEAVEN),
        new Hexagram(2, IChingTrigram.EARTH, IChingTrigram.EARTH),
        new Hexagram(3, IChingTrigram.WATER, IChingTrigram.THUNDER),
        new Hexagram(4, IChingTrigram.MOUNTAIN, IChingTrigram.WATER),
        new Hexagram(5, IChingTrigram.WATER, IChingTrigram.HEAVEN),
        new Hexagram(6, IChingTrigram.HEAVEN, IChingTrigram.WATER),
        new Hexagram(7, IChingTrigram.EARTH, IChingTrigram.WATER),
        new Hexagram(8, IChingTrigram.WATER, IChingTrigram.EARTH),
        new Hexagram(9, IChingTrigram.WIND, IChingTrigram.HEAVEN),
        new Hexagram(10, IChingTrigram.HEAVEN, IChingTrigram.LAKE),
        new Hexagram(11, IChingTrigram.EARTH, IChingTrigram.HEAVEN),
        new Hexagram(12, IChingTrigram.HEAVEN, IChingTrigram.EARTH),
        new Hexagram(13, IChingTrigram.HEAVEN, IChingTrigram.FIRE),
        new Hexagram(14, IChingTrigram.FIRE, IChingTrigram.HEAVEN),
        new Hexagram(15, IChingTrigram.EARTH, IChingTrigram.MOUNTAIN),
        new Hexagram(16, IChingTrigram.THUNDER, IChingTrigram.EARTH),
        new Hexagram(17, IChingTrigram.LAKE, IChingTrigram.THUNDER),
        new Hexagram(18, IChingTrigram.MOUNTAIN, IChingTrigram.WIND),
        new Hexagram(19, IChingTrigram.EARTH, IChingTrigram.LAKE),
        new Hexagram(20, IChingTrigram.WIND, IChingTrigram.EARTH),
        new Hexagram(21, IChingTrigram.FIRE, IChingTrigram.THUNDER),
        new Hexagram(22, IChingTrigram.MOUNTAIN, IChingTrigram.FIRE),
        new Hexagram(23, IChingTrigram.MOUNTAIN, IChingTrigram.EARTH),
        new Hexagram(24, IChingTrigram.EARTH, IChingTrigram.THUNDER),
        new Hexagram(25, IChingTrigram.HEAVEN, IChingTrigram.THUNDER),
        new Hexagram(26, IChingTrigram.MOUNTAIN, IChingTrigram.HEAVEN),
        new Hexagram(27, IChingTrigram.MOUNTAIN, IChingTrigram.THUNDER),
        new Hexagram(28, IChingTrigram.LAKE, IChingTrigram.WIND),
        new Hexagram(29, IChingTrigram.WATER, IChingTrigram.WATER),
        new Hexagram(30, IChingTrigram.FIRE, IChingTrigram.FIRE),
        new Hexagram(31, IChingTrigram.LAKE, IChingTrigram.MOUNTAIN),
        new Hexagram(32, IChingTrigram.THUNDER, IChingTrigram.WIND),
        new Hexagram(33, IChingTrigram.HEAVEN, IChingTrigram.MOUNTAIN),
        new Hexagram(34, IChingTrigram.THUNDER, IChingTrigram.HEAVEN),
        new Hexagram(35, IChingTrigram.FIRE, IChingTrigram.EARTH),
        new Hexagram(36, IChingTrigram.EARTH, IChingTrigram.FIRE),
        new Hexagram(37, IChingTrigram.WIND, IChingTrigram.FIRE),
        new Hexagram(38, IChingTrigram.FIRE, IChingTrigram.LAKE),
        new Hexagram(39, IChingTrigram.WATER, IChingTrigram.MOUNTAIN),
        new Hexagram(40, IChingTrigram.THUNDER, IChingTrigram.WATER),
        new Hexagram(41, IChingTrigram.MOUNTAIN, IChingTrigram.LAKE),
        new Hexagram(42, IChingTrigram.WIND, IChingTrigram.THUNDER),
        new Hexagram(43, IChingTrigram.LAKE, IChingTrigram.HEAVEN),
        new Hexagram(44, IChingTrigram.HEAVEN, IChingTrigram.WIND),
        new Hexagram(45, IChingTrigram.LAKE, IChingTrigram.EARTH),
        new Hexagram(46, IChingTrigram.EARTH, IChingTrigram.WIND),
        new Hexagram(47, IChingTrigram.LAKE, IChingTrigram.WATER),
        new Hexagram(48, IChingTrigram.WATER, IChingTrigram.WIND),
        new Hexagram(49, IChingTrigram.LAKE, IChingTrigram.FIRE),
        new Hexagram(50, IChingTrigram.FIRE, IChingTrigram.WIND),
        new Hexagram(51, IChingTrigram.THUNDER, IChingTrigram.THUNDER),
        new Hexagram(52, IChingTrigram.MOUNTAIN, IChingTrigram.MOUNTAIN),
        new Hexagram(53, IChingTrigram.WIND, IChingTrigram.MOUNTAIN),
        new Hexagram(54, IChingTrigram.THUNDER, IChingTrigram.LAKE),
        new Hexagram(55, IChingTrigram.THUNDER, IChingTrigram.FIRE),
        new Hexagram(56, IChingTrigram.FIRE, IChingTrigram.MOUNTAIN),
        new Hexagram(57, IChingTrigram.WIND, IChingTrigram.WIND),
        new Hexagram(58, IChingTrigram.LAKE, IChingTrigram.LAKE),
        new Hexagram(59, IChingTrigram.WIND, IChingTrigram.WATER),
        new Hexagram(60, IChingTrigram.WATER, IChingTrigram.LAKE),
        new Hexagram(61, IChingTrigram.WIND, IChingTrigram.LAKE),
        new Hexagram(62, IChingTrigram.THUNDER, IChingTrigram.MOUNTAIN),
        new Hexagram(63, IChingTrigram.WATER, IChingTrigram.FIRE),
        new Hexagram(64, IChingTrigram.FIRE, IChingTrigram.WATER)
    );

    private static final Map<IChingTrigram, Map<IChingTrigram, Hexagram>> BY_TRIGRAMS = buildIndex();

    private HexagramTable() {
    }

    private static Map<IChingTrigram, Map<IChingTrigram, Hexagram>> buildIndex() {
        Map<IChingTrigram, Map<IChingTrigram, Hexagram>> index = new EnumMap<>(IChingTrigram.class);
        for (IChingTrigram upper : IChingTrigram.values()) {
            index.put(upper, new EnumMap<>(IChingTrigram.class));
        }
        for (Hexagram h : BY_NUMBER) {
            index.get(h.upper()).put(h.lower(), h);
        }
        return index;
    }

    public static Hexagram of(IChingTrigram upper, IChingTrigram lower) {
        return BY_TRIGRAMS.get(upper).get(lower);
    }

    public static Hexagram byNumber(int number) {
        return BY_NUMBER.get(number - 1);
    }

    public static List<Hexagram> all() {
        return BY_NUMBER;
    }
}
