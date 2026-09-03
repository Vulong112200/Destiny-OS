/**
 * Những gì cả bảng Du Niên lẫn la bàn Bát Quái đều cần biết.
 *
 * <p>Tách ra vì hai chỗ đó phải nói cùng một điều. Trước đây tập hợp "hướng
 * nào là cát" nằm trong `BatTrachCard`; thêm một la bàn với bản sao thứ hai
 * thì hai hình có thể tô màu ngược nhau cho cùng một hướng mà từng cái vẫn
 * trông hợp lý.
 */

export const COMPASS_ORDER = [
  "NORTH",
  "NORTHEAST",
  "EAST",
  "SOUTHEAST",
  "SOUTH",
  "SOUTHWEST",
  "WEST",
  "NORTHWEST",
] as const;

export type CompassDirection = (typeof COMPASS_ORDER)[number];

/**
 * Bốn quan hệ truyền thống xếp vào nhóm cát.
 *
 * <p>Chỉ dùng để chọn tông màu, không bao giờ để xếp lại thứ hạng — thứ hạng
 * (thượng/trung/tiểu cát, đại/thứ/tiểu hung) đã nằm trong nhãn tiếng Việt do
 * backend trả về.
 */
export const AUSPICIOUS_RELATIONS = new Set([
  "SINH_KHI",
  "DIEN_NIEN",
  "THIEN_Y",
  "PHUC_VI",
]);

export function isAuspicious(relation: unknown): boolean {
  return typeof relation === "string" && AUSPICIOUS_RELATIONS.has(relation);
}

/**
 * Góc của mỗi hướng trên la bàn, độ, 0 = hướng lên trên màn hình.
 *
 * <p>**Bắc ở trên.** Sơ đồ Bát Trạch cổ truyền thường vẽ Nam ở trên, nhưng
 * một chiếc la bàn và điện thoại của người dùng đều để Bắc ở trên. Đây là một
 * lựa chọn quy ước, không phải mặc định hiển nhiên, nên hình vẽ phải ghi rõ nó
 * ra — đọc nhầm hướng trên đúng cái hình này là loại nhầm lẫn tốn kém.
 */
export const DIRECTION_ANGLE: Record<CompassDirection, number> = {
  NORTH: 0,
  NORTHEAST: 45,
  EAST: 90,
  SOUTHEAST: 135,
  SOUTH: 180,
  SOUTHWEST: 225,
  WEST: 270,
  NORTHWEST: 315,
};

/** Viết tắt tiếng Việt để in trong một cung 45 độ. */
export const DIRECTION_SHORT_VI: Record<CompassDirection, string> = {
  NORTH: "Bắc",
  NORTHEAST: "Đông Bắc",
  EAST: "Đông",
  SOUTHEAST: "Đông Nam",
  SOUTH: "Nam",
  SOUTHWEST: "Tây Nam",
  WEST: "Tây",
  NORTHWEST: "Tây Bắc",
};
