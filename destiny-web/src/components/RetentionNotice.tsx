"use client";

import { useState } from "react";
import { ApiError, saveCalculation } from "@/lib/api";
import type { RetentionDto } from "@/lib/types";

/**
 * Tells the reader whether this result will be deleted, and lets them keep it
 * (CLAUDE.md §7).
 *
 * <p>Shown unconditionally rather than only when something is about to expire.
 * A notice that appears only near the deadline trains the reader to assume
 * results are permanent, which is precisely the assumption that makes automatic
 * deletion feel like data loss. The date is stated in full, in Vietnamese, in
 * the local calendar — not as "còn 12 ngày", which goes stale the moment the
 * page is cached.
 */
export function RetentionNotice({
  calculationId,
  retention,
}: {
  calculationId: string;
  retention: RetentionDto;
}) {
  const [current, setCurrent] = useState(retention);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleSave() {
    setError(null);
    setSaving(true);
    try {
      setCurrent(await saveCalculation(calculationId));
    } catch (err) {
      setError(
        err instanceof ApiError
          ? err.message
          : "Không lưu được kết quả này. Hãy thử lại sau.",
      );
    } finally {
      setSaving(false);
    }
  }

  const expiresLabel = current.expiresAt
    ? new Date(current.expiresAt).toLocaleDateString("vi-VN", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
      })
    : null;

  const tone = current.canBeSaved
    ? "border-amber-200 bg-amber-50 text-amber-900"
    : "border-emerald-200 bg-emerald-50 text-emerald-900";

  return (
    <section className={`rounded-lg border px-4 py-3 text-sm ${tone}`}>
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <p className="font-medium">{current.retentionClass.labelVi}</p>
          {expiresLabel ? (
            <p className="mt-0.5 text-xs">
              Kết quả này sẽ được tự động xóa sau ngày{" "}
              <span className="font-medium">{expiresLabel}</span>. Bấm{" "}
              <span className="font-medium">Lưu kết quả</span> để giữ lại lâu dài.
            </p>
          ) : (
            <p className="mt-0.5 text-xs">
              Kết quả này không nằm trong diện tự động xóa.
            </p>
          )}
        </div>

        {current.canBeSaved && (
          <button
            type="button"
            onClick={handleSave}
            disabled={saving}
            className="rounded-md bg-slate-900 px-3 py-2 text-xs font-semibold text-white hover:bg-slate-700 disabled:opacity-50"
          >
            {saving ? "Đang lưu…" : "Lưu kết quả"}
          </button>
        )}
      </div>

      {error && (
        <p role="alert" className="mt-2 text-xs text-rose-800">
          {error}
        </p>
      )}
    </section>
  );
}
