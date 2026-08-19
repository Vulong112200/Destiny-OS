import Link from "next/link";
import { listMethodologies } from "@/lib/api";
import { MethodologyTable } from "@/components/MethodologyTable";

export default async function OverviewPage() {
  let methodologies;
  let loadError: string | null = null;
  try {
    methodologies = await listMethodologies();
  } catch {
    loadError = "Không thể kết nối tới API. Kiểm tra destiny-app có đang chạy tại NEXT_PUBLIC_API_BASE_URL không.";
  }

  const calculableCount = methodologies?.filter((m) => m.calculable).length ?? 0;

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Tổng quan hệ thống</h1>
        <p className="mt-1 text-slate-600">
          Mọi phương pháp được đăng ký ở đây với trạng thái thật — kể cả phương pháp đang chờ
          nghiên cứu. Không có phương pháp nào bị ẩn đi.
        </p>
      </div>

      {loadError && (
        <p className="rounded-md bg-rose-50 px-4 py-3 text-sm text-rose-800">{loadError}</p>
      )}

      {methodologies && (
        <>
          <p className="text-sm text-slate-500">
            {calculableCount}/{methodologies.length} phương pháp hiện có thể tính toán.
          </p>
          <MethodologyTable methodologies={methodologies} />
        </>
      )}

      <div className="rounded-lg border border-slate-200 bg-white p-6">
        <h2 className="text-lg font-semibold">Bắt đầu</h2>
        <p className="mt-1 text-sm text-slate-600">
          Vào <Link href="/trung-tam-quyet-dinh" className="font-medium underline">Trung tâm quyết định</Link>{" "}
          để chạy một kịch bản với các phương pháp đang khả dụng.
        </p>
      </div>
    </div>
  );
}
