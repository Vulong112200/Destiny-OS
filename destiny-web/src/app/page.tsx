import Link from "next/link";
import { listMethodologies } from "@/lib/api";
import { MethodologyTable } from "@/components/MethodologyTable";
import type { MethodologyDto } from "@/lib/types";

export default async function OverviewPage() {
  let methodologies: MethodologyDto[] | undefined;
  let loadError: string | null = null;
  try {
    methodologies = await listMethodologies();
  } catch {
    loadError = "Không thể kết nối tới API. Kiểm tra destiny-app có đang chạy tại NEXT_PUBLIC_API_BASE_URL không.";
  }

  const calculableCount = methodologies?.filter((m) => m.calculable).length ?? 0;
  const total = methodologies?.length ?? 0;

  return (
    <div className="space-y-8">
      <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm lg:p-8">
        <div className="flex flex-col gap-6 lg:flex-row lg:items-center lg:justify-between">
          <div className="max-w-2xl">
            <h1 className="text-2xl font-bold lg:text-3xl">Tổng quan hệ thống</h1>
            <p className="mt-2 text-slate-600">
              Mọi phương pháp được đăng ký ở đây với trạng thái thật — kể cả phương pháp đang chờ
              nghiên cứu. Không có phương pháp nào bị ẩn đi, và không phương pháp nào được phép
              trả kết quả khi thuật toán của nó chưa được xác minh.
            </p>
            <Link
              href="/trung-tam-quyet-dinh"
              className="mt-5 inline-block rounded-md bg-slate-900 px-5 py-2.5 text-sm font-semibold text-white hover:bg-slate-700"
            >
              Bắt đầu — vào Trung tâm quyết định
            </Link>
          </div>

          {methodologies && (
            <dl className="grid shrink-0 grid-cols-2 gap-3 lg:gap-4">
              <StatTile label="Tính toán được" value={calculableCount} tone="emerald" />
              <StatTile label="Đã đăng ký" value={total} tone="slate" />
            </dl>
          )}
        </div>
      </section>

      {loadError && (
        <p className="rounded-md bg-rose-50 px-4 py-3 text-sm text-rose-800">{loadError}</p>
      )}

      {methodologies && (
        <section className="space-y-3">
          <div className="flex items-baseline justify-between gap-4">
            <h2 className="text-lg font-semibold text-slate-900">Sổ đăng ký phương pháp</h2>
            <p className="text-sm text-slate-500">
              {calculableCount}/{total} phương pháp hiện có thể tính toán.
            </p>
          </div>
          <MethodologyTable methodologies={methodologies} />
        </section>
      )}
    </div>
  );
}

function StatTile({
  label,
  value,
  tone,
}: {
  label: string;
  value: number;
  tone: "emerald" | "slate";
}) {
  const toneClass =
    tone === "emerald"
      ? "border-emerald-200 bg-emerald-50 text-emerald-900"
      : "border-slate-200 bg-slate-50 text-slate-900";
  return (
    <div className={`rounded-xl border px-6 py-4 ${toneClass}`}>
      <dt className="text-xs font-medium uppercase tracking-wide opacity-70">{label}</dt>
      <dd className="mt-1 text-3xl font-bold tabular-nums">{value}</dd>
    </div>
  );
}
