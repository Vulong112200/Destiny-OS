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

/**
 * Bố cục đầy đủ của một kiểu trải: vị trí các lá, tỉ lệ khung, và bề ngang lá.
 *
 * <p>Ba con số này phải đi cùng nhau. Trước đây `cardWidth` không tồn tại: kích
 * thước lá là một hằng số (`h-56 w-32`) rồi bị thu nhỏ bằng một hệ số cố định,
 * nên không có gì bảo đảm bốn lá của cây gậy Thập tự Celtic vừa chiều cao khung
 * — và thực tế là không vừa. Nay bề ngang lá được tính ngược từ bố cục chật
 * nhất của từng kiểu trải, nên khoảng cách giữa các lá là kết quả của phép tính
 * chứ không phải của việc thử số.
 */
export interface SpreadGeometry {
  /**
   * Vị trí từng lá, hoặc `null` cho kiểu trải tự do.
   *
   * <p>`null` **không phải** là thiếu sót. Kiểu trải tự do tồn tại để từ chối
   * gán ý nghĩa cho vị trí; xếp nó thành hình gì đó sẽ ngầm nói ngược lại.
   * Người gọi nhận `null` thì xếp thành một hàng bình thường.
   */
  slots: CardSlot[] | null;
  /** Tỉ lệ ngang/dọc của khung chứa bố cục. */
  aspect: number;
  /** Bề ngang một lá, theo tỉ lệ bề ngang khung chứa (0..1). */
  cardWidth: number;
}

/** Tỉ lệ cao/rộng của một lá, theo bản scan Rider-Waite-Smith đang dùng. */
export const CARD_RATIO = 224 / 128;

/**
 * Một hàng ngang, các lá cách đều nhau.
 *
 * <p>Bề ngang lá bị chặn hai đầu: `0.2` để lá không phình ra khi chỉ có hai ba
 * lá, và `0.85 / (count + 1)` để `count` lá cách đều nhau vẫn còn khe hở —
 * khoảng cách giữa hai tâm lá là `1 / (count + 1)`, nên lá rộng hơn 85% con số
 * đó là bắt đầu chạm nhau.
 */
function centerRow(count: number): SpreadGeometry {
  return {
    slots: Array.from({ length: count }, (_, i) => ({
      x: (i + 1) / (count + 1),
      y: 0.5,
      rotate: 0,
      z: 0,
    })),
    aspect: 2.4,
    cardWidth: Math.min(0.2, 0.85 / (count + 1)),
  };
}

/**
 * Móng ngựa: hình cung ngửa, mỗi lá nghiêng theo tiếp tuyến.
 *
 * <p>Bề ngang lá `0.14` và các mốc `x` ở đây tính theo hộp bao của lá **sau khi
 * nghiêng**, không phải theo bề ngang lá. Một lá nghiêng 16° rộng thêm
 * `H·sin16° ≈ 0.28·H`, tức gần bằng một nửa bề ngang lá — bỏ qua phần đó là hai
 * lá ngoài cùng chồm lên hai lá kế bên.
 */
const HORSESHOE: SpreadGeometry = {
  slots: [
    { x: 0.128, y: 0.622, rotate: -16, z: 0 },
    { x: 0.33, y: 0.483, rotate: -8, z: 0 },
    { x: 0.5, y: 0.314, rotate: 0, z: 0 },
    { x: 0.67, y: 0.483, rotate: 8, z: 0 },
    { x: 0.872, y: 0.622, rotate: 16, z: 0 },
  ],
  aspect: 2.2,
  cardWidth: 0.14,
};

/**
 * Thập tự Celtic: chữ thập bên trái, cây gậy bên phải.
 *
 * <p>Thứ tự khớp đúng thứ tự `TarotSpread.CELTIC_CROSS` khai báo ở backend —
 * lá 2 nằm chồng lên lá 1 và xoay ngang (nên gọi là "giao cắt"), lá 3 là nền
 * tảng nên nằm dưới, lá 5 là điều đang hướng tới nên nằm trên. Đổi thứ tự ở
 * đây mà không đổi ở `TarotSpread.java` sẽ khiến nhãn vị trí gắn sai lá.
 *
 * <p><strong>Vì sao khung này cao hơn rộng (`aspect` 0.95).</strong> Cây gậy là
 * bốn lá xếp dọc: chiều cao tối thiểu của bố cục là `4·H + 3·khe`, với
 * `H = 1.75·W`, tức khoảng `7.5·W`. Chữ thập cộng cây gậy chỉ cần khoảng `5.7·W`
 * bề ngang. Khung ngang (trước đây là 1.35) vì thế bắt buộc lá phải nhỏ tới mức
 * bốn lá cây gậy vẫn chạm nhau — bố cục này không thể nằm ngang.
 *
 * <p>Lá giao cắt xoay 90° nên chiếm `H` bề ngang quanh tâm lá 1, chứ không phải
 * `W`; hai mốc `x` của lá 4 và lá 6 chừa đúng khoảng đó.
 */
const CELTIC_CROSS: SpreadGeometry = {
  slots: [
    { x: 0.354, y: 0.5, rotate: 0, z: 1 },
    { x: 0.354, y: 0.5, rotate: 90, z: 2 },
    { x: 0.354, y: 0.741, rotate: 0, z: 0 },
    { x: 0.143, y: 0.5, rotate: 0, z: 0 },
    { x: 0.354, y: 0.259, rotate: 0, z: 0 },
    { x: 0.566, y: 0.5, rotate: 0, z: 0 },
    { x: 0.857, y: 0.862, rotate: 0, z: 0 },
    { x: 0.857, y: 0.621, rotate: 0, z: 0 },
    { x: 0.857, y: 0.379, rotate: 0, z: 0 },
    { x: 0.857, y: 0.138, rotate: 0, z: 0 },
  ],
  aspect: 0.95,
  cardWidth: 0.132,
};

const FREE_FORM: SpreadGeometry = { slots: null, aspect: 2.4, cardWidth: 0.2 };

/** Bố cục của một kiểu trải. Xem `SpreadGeometry`. */
export function spreadGeometry(spread: TarotSpreadName, cardCount: number): SpreadGeometry {
  switch (spread) {
    case "PAST_PRESENT_FUTURE":
    case "SITUATION_CHALLENGE_ADVICE":
      return centerRow(3);
    case "CHOICE_A_B":
      return centerRow(2);
    case "HORSESHOE_FIVE":
      return HORSESHOE;
    case "CELTIC_CROSS":
      return CELTIC_CROSS;
    case "FREE_FORM":
      return FREE_FORM;
    default:
      return centerRow(cardCount);
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
