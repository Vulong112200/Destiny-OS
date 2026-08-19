"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";

/**
 * Scoped honestly to "look up a calculation by id you already have" - no
 * user/session system exists yet, so there is no real "list every
 * calculation I've ever run" to offer instead of pretending to browse one.
 */
export default function HistoryPage() {
  const router = useRouter();
  const [calculationId, setCalculationId] = useState("");

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    const trimmed = calculationId.trim();
    if (trimmed) {
      router.push(`/ket-qua/${encodeURIComponent(trimmed)}`);
    }
  }

  return (
    <div className="mx-auto max-w-xl space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Lịch sử</h1>
        <p className="mt-1 text-slate-600">
          Nhập mã lần tính đã lưu từ một kết quả trước đó để xem lại. Hệ thống hiện chưa có tài
          khoản người dùng nên chưa thể liệt kê toàn bộ lịch sử của bạn — chỉ tra cứu được theo mã
          cụ thể.
        </p>
      </div>
      <form onSubmit={handleSubmit} className="flex gap-2">
        <input
          type="text"
          value={calculationId}
          onChange={(e) => setCalculationId(e.target.value)}
          placeholder="ví dụ: calc-a38e8f45-5735-4507-b043-b49b91d336b0"
          className="flex-1 rounded-md border border-slate-300 px-3 py-2 font-mono text-sm text-slate-900"
        />
        <button
          type="submit"
          className="rounded-md bg-slate-900 px-4 py-2 text-sm font-semibold text-white hover:bg-slate-700"
        >
          Tra cứu
        </button>
      </form>
    </div>
  );
}
