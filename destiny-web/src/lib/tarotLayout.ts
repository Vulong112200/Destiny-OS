import type { TarotSpreadName } from "./types";

/**
 * Vị trí của từng lá trong một kiểu trải bài.
 *
 * <p>`x`/`y` là tỉ lệ trong khung chứa (0..1), không phải pixel — nên cùng một
 * bố cục dùng được ở mọi bề ngang mà không cần đo đạc gì ở phía component.
 */
export interface CardSlot {
  x: number;
  y: number;
  /** Độ nghiêng, chỉ dùng cho móng ngựa và lá giao cắt của Thập tự Celtic. */
  rotate: number;
  z: number;
}

const CENTER_ROW = (count: number): CardSlot[] =>
  Array.from({ length: count }, (_, i) => ({
    x: (i + 1) / (count + 1),
    y: 0.5,
    rotate: 0,
    z: 0,
  }));

/** Móng ngựa: hình cung ngửa, mỗi lá nghiêng theo tiếp tuyến. */
const HORSESHOE: CardSlot[] = [
  { x: 0.12, y: 0.68, rotate: -16, z: 0 },
  { x: 0.29, y: 0.36, rotate: -8, z: 0 },
  { x: 0.5, y: 0.22, rotate: 0, z: 0 },
  { x: 0.71, y: 0.36, rotate: 8, z: 0 },
  { x: 0.88, y: 0.68, rotate: 16, z: 0 },
];

/**
 * Thập tự Celtic: chữ thập bên trái, cây gậy bên phải.
 *
 * <p>Thứ tự khớp đúng thứ tự `TarotSpread.CELTIC_CROSS` khai báo ở backend —
 * lá 2 nằm chồng lên lá 1 và xoay ngang (nên gọi là "giao cắt"), lá 3 là nền
 * tảng nên nằm dưới, lá 5 là điều đang hướng tới nên nằm trên. Đổi thứ tự ở
 * đây mà không đổi ở `TarotSpread.java` sẽ khiến nhãn vị trí gắn sai lá.
 */
const CELTIC_CROSS: CardSlot[] = [
  { x: 0.29, y: 0.5, rotate: 0, z: 1 },
  { x: 0.29, y: 0.5, rotate: 90, z: 2 },
  { x: 0.29, y: 0.82, rotate: 0, z: 0 },
  { x: 0.1, y: 0.5, rotate: 0, z: 0 },
  { x: 0.29, y: 0.18, rotate: 0, z: 0 },
  { x: 0.48, y: 0.5, rotate: 0, z: 0 },
  { x: 0.74, y: 0.87, rotate: 0, z: 0 },
  { x: 0.74, y: 0.63, rotate: 0, z: 0 },
  { x: 0.74, y: 0.39, rotate: 0, z: 0 },
  { x: 0.74, y: 0.15, rotate: 0, z: 0 },
];

/**
 * Vị trí từng lá, hoặc `null` cho kiểu trải tự do.
 *
 * <p>`null` **không phải** là thiếu sót. Kiểu trải tự do tồn tại để từ chối gán
 * ý nghĩa cho vị trí; xếp nó thành hình gì đó sẽ ngầm nói ngược lại. Người gọi
 * nhận `null` thì xếp thành một hàng cuộn bình thường.
 */
export function spreadLayout(spread: TarotSpreadName, cardCount: number): CardSlot[] | null {
  switch (spread) {
    case "PAST_PRESENT_FUTURE":
    case "SITUATION_CHALLENGE_ADVICE":
      return CENTER_ROW(3);
    case "CHOICE_A_B":
      return CENTER_ROW(2);
    case "HORSESHOE_FIVE":
      return HORSESHOE;
    case "CELTIC_CROSS":
      return CELTIC_CROSS;
    case "FREE_FORM":
      return null;
    default:
      return CENTER_ROW(cardCount);
  }
}

/** Tỉ lệ khung chứa, chọn sao cho bố cục không bị bóp méo. */
export function spreadAspect(spread: TarotSpreadName): number {
  switch (spread) {
    case "CELTIC_CROSS":
      return 1.35;
    case "HORSESHOE_FIVE":
      return 2.1;
    default:
      return 2.4;
  }
}

/**
 * Suy ra kiểu trải từ tên các vị trí trong evidence.
 *
 * <p>API không trả tên kiểu trải ở cấp lần chạy — nó chỉ trả từng lá kèm vị
 * trí. Tên vị trí là duy nhất cho từng kiểu trải (xem `TarotSpread.java`), nên
 * suy ngược lại được, và đây là chỗ duy nhất làm việc đó. Không nhận ra thì
 * trả `null` và người gọi xếp thành một hàng bình thường — đoán bừa một bố cục
 * sẽ đặt các lá vào những vị trí chúng không thuộc về.
 */
export function inferSpread(positions: readonly string[]): TarotSpreadName | null {
  const set = new Set(positions);
  if (set.has("SIGNIFICATOR") || set.has("HOPES_FEARS")) return "CELTIC_CROSS";
  if (set.has("OBSTACLE") || set.has("TENDENCY")) return "HORSESHOE_FIVE";
  if (set.has("SITUATION") || set.has("CHALLENGE")) return "SITUATION_CHALLENGE_ADVICE";
  if (set.has("CHOICE_A")) return "CHOICE_A_B";
  if (set.has("PAST") && set.has("FUTURE")) return "PAST_PRESENT_FUTURE";
  if (positions.every((p) => /^CARD_\d+$/.test(p))) return "FREE_FORM";
  return null;
}
