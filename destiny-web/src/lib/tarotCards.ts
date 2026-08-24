/**
 * Vietnamese display names for the 78 Rider-Waite-Smith cards, and the path
 * to each card's public-domain scan (1909 deck, via Wikimedia Commons —
 * `docs/` has no note against this: `TarotCard.java`'s own Javadoc calls the
 * card identity/naming "structural fact about a well-documented, public
 * domain deck design", not gated content).
 *
 * This is a name TRANSLATION (standard suit/rank vocabulary already
 * converged in Vietnamese Tarot writing), not interpretive content — unlike
 * `TarotCardMeaning`'s career/finance/relationship text, which is authored,
 * versioned reference data gated by research item R11. No such gate applies
 * to what a card is called.
 */

const MAJOR_NAMES_VI: Record<string, string> = {
  MAJOR_00_THE_FOOL: "Kẻ Ngốc",
  MAJOR_01_THE_MAGICIAN: "Nhà Ảo Thuật",
  MAJOR_02_THE_HIGH_PRIESTESS: "Nữ Tu",
  MAJOR_03_THE_EMPRESS: "Hoàng Hậu",
  MAJOR_04_THE_EMPEROR: "Hoàng Đế",
  MAJOR_05_THE_HIEROPHANT: "Giáo Hoàng",
  MAJOR_06_THE_LOVERS: "Cặp Tình Nhân",
  MAJOR_07_THE_CHARIOT: "Cỗ Xe",
  MAJOR_08_STRENGTH: "Sức Mạnh",
  MAJOR_09_THE_HERMIT: "Ẩn Sĩ",
  MAJOR_10_WHEEL_OF_FORTUNE: "Vòng Xoay Số Phận",
  MAJOR_11_JUSTICE: "Công Lý",
  MAJOR_12_THE_HANGED_MAN: "Người Treo Ngược",
  MAJOR_13_DEATH: "Tử Thần",
  MAJOR_14_TEMPERANCE: "Điều Độ",
  MAJOR_15_THE_DEVIL: "Ác Quỷ",
  MAJOR_16_THE_TOWER: "Tòa Tháp",
  MAJOR_17_THE_STAR: "Ngôi Sao",
  MAJOR_18_THE_MOON: "Mặt Trăng",
  MAJOR_19_THE_SUN: "Mặt Trời",
  MAJOR_20_JUDGEMENT: "Sự Phán Xét",
  MAJOR_21_THE_WORLD: "Thế Giới",
};

const SUIT_NAME_VI: Record<string, string> = {
  WANDS: "Gậy",
  CUPS: "Cốc",
  SWORDS: "Kiếm",
  PENTACLES: "Tiền",
};

const RANK_NAME_VI: Record<string, string> = {
  "01": "Ách",
  "02": "Hai",
  "03": "Ba",
  "04": "Bốn",
  "05": "Năm",
  "06": "Sáu",
  "07": "Bảy",
  "08": "Tám",
  "09": "Chín",
  "10": "Mười",
  "11": "Thị Đồng",
  "12": "Kỵ Sĩ",
  "13": "Hoàng Hậu",
  "14": "Vua",
};

const POSITION_LABEL_VI: Record<string, string> = {
  PAST: "Quá khứ",
  PRESENT: "Hiện tại",
  FUTURE: "Tương lai",
  CHOICE_A: "Lựa chọn A",
  CHOICE_B: "Lựa chọn B",
  SITUATION: "Tình huống",
  CHALLENGE: "Thử thách",
  ADVICE: "Lời khuyên",
};

/** e.g. "MINOR_WANDS_01_ACE" -> "Ách Gậy". Falls back to the raw id if it doesn't parse. */
export function tarotCardNameVi(cardId: string, englishName: string): string {
  if (MAJOR_NAMES_VI[cardId]) return MAJOR_NAMES_VI[cardId];
  const minorMatch = /^MINOR_([A-Z]+)_(\d{2})_/.exec(cardId);
  if (minorMatch) {
    const [, suit, rank] = minorMatch;
    const suitVi = SUIT_NAME_VI[suit];
    const rankVi = RANK_NAME_VI[rank];
    if (suitVi && rankVi) return `${rankVi} ${suitVi}`;
  }
  return englishName;
}

export function tarotPositionLabelVi(position: string): string {
  return POSITION_LABEL_VI[position] ?? position;
}

/** Local public-domain scan for this card, or null if none was bundled. */
export function tarotCardImagePath(cardId: string): string {
  return `/tarot/${cardId}.jpg`;
}
