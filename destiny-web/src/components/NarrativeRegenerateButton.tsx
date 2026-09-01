"use client";

import { useState, useTransition } from "react";
import { useRouter } from "next/navigation";
import { ApiError, regenerateNarrative } from "@/lib/api";

/**
 * Nút "Tạo lại phần diễn giải".
 *
 * <p>`NarrativePanel` là server component nên nó không thể tự có nút bấm; đây
 * là phần client nhỏ nhất có thể để làm được đúng một việc, theo cùng khuôn
 * mẫu `"use client"` mà `DecisionCenterForm` đang dùng.
 *
 * <p>Nút này chỉ được hiển thị khi nguồn diễn giải là bản dự phòng phi-AI.
 * Lý do: một bản diễn giải AI đã thành công thì không có gì để "thử lại", còn
 * một bản fallback thì hầu hết nguyên nhân đều là **tạm thời** — model đang bị
 * giới hạn tần suất, phản hồi sai định dạng, dịch vụ chậm. Với những nguyên
 * nhân đó, bấm lại sau vài giây là hành động đúng và người dùng không có cách
 * nào khác để làm điều đó.
 *
 * <p>Sau khi POST xong thì gọi `router.refresh()` để server component đọc lại
 * bản mới. Không tự vẽ lại nội dung ở client: dữ liệu cứng và phần diễn giải
 * phải đến từ cùng một lượt render của server, nếu không sẽ có lúc trang hiển
 * thị lời văn của lần tính này cạnh trạng thái của lần tính khác.
 */
export function NarrativeRegenerateButton({ calculationId }: { calculationId: string }) {
  const router = useRouter();
  const [dangGoi, setDangGoi] = useState(false);
  const [dangLamMoi, startTransition] = useTransition();
  const [loi, setLoi] = useState<string | null>(null);

  const dangChay = dangGoi || dangLamMoi;

  async function taoLai() {
    setDangGoi(true);
    setLoi(null);
    try {
      await regenerateNarrative(calculationId);
      startTransition(() => router.refresh());
    } catch (error) {
      // Người dùng vừa bấm một nút, nên họ xứng đáng được biết là nó không
      // thành công — nhưng đây vẫn không phải lỗi hệ thống: dữ liệu tính toán
      // bên dưới không phụ thuộc vào phần này (CLAUDE.md §8).
      setLoi(
        error instanceof ApiError
          ? error.message
          : "Không gọi được dịch vụ diễn giải. Dữ liệu tính toán bên dưới vẫn đầy đủ.",
      );
    } finally {
      setDangGoi(false);
    }
  }

  return (
    <div className="flex flex-wrap items-center gap-2">
      <button
        type="button"
        onClick={taoLai}
        disabled={dangChay}
        className="rounded-md border border-indigo-300 bg-white px-2.5 py-1 text-xs font-medium text-indigo-700 transition hover:bg-indigo-100 disabled:cursor-not-allowed disabled:opacity-60"
      >
        {dangChay ? "Đang tạo lại…" : "Tạo lại phần diễn giải"}
      </button>
      {loi && <span className="text-xs text-amber-700">{loi}</span>}
    </div>
  );
}
