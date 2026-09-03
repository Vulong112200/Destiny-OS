import type { LabelRegistries } from "@/lib/types";
import { engineName, engineNames } from "@/lib/labels";

/**
 * Tên tiếng Việt của một engine.
 *
 * <p>Tồn tại để không chỗ nào còn viết `{something.engine}` thẳng vào JSX.
 * Đó chính xác là cách người dùng đọc được `TAROT`, `NUMEROLOGY_PYTHAGOREAN`,
 * `WESTERN_ASTROLOGY`, `FENGSHUI_KUA` và `ICHING` trên trang kết quả, trong khi
 * bảng nhãn tiếng Việt cho cả sáu đã nằm sẵn trong phản hồi của
 * `GET /api/v1/labels`.
 *
 * <p>Tên kỹ thuật không bị giấu — nó nằm ở `title`, nên vẫn tra được khi cần
 * đối chiếu với log hay tài liệu, chỉ là không đập vào mắt người đọc.
 */
export function EngineName({
  id,
  labels,
  className,
}: {
  id: string;
  labels?: LabelRegistries;
  className?: string;
}) {
  return (
    <span className={className} title={id}>
      {engineName(id, labels)}
    </span>
  );
}

/** Nhiều engine, nối bằng dấu phẩy. Thay cho `ids.join(", ")`. */
export function EngineNameList({
  ids,
  labels,
  empty = "—",
  className,
}: {
  ids: readonly string[];
  labels?: LabelRegistries;
  empty?: string;
  className?: string;
}) {
  return (
    <span className={className} title={ids.join(", ")}>
      {engineNames(ids, labels, empty)}
    </span>
  );
}
