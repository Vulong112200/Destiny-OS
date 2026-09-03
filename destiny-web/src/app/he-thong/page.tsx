import type { Metadata } from "next";
import Link from "next/link";
import { listMethodologies } from "@/lib/api";
import type { MethodologyDto } from "@/lib/types";
import {
  RUNNABLE_SYSTEMS,
  SYSTEM_INVENTORY,
  UNBUILT_SYSTEMS,
  type SystemEntry,
} from "@/lib/systemInventory";
import { LabeledBadge } from "@/components/LabeledBadge";

export const metadata: Metadata = {
  title: "Hệ thống · Destiny OS",
  description: "Chín hệ thống trong đặc tả, hệ nào chạy được, hệ nào còn chờ, và vì sao.",
};

/**
 * Trang trả lời một câu hỏi cụ thể: hệ thống này có bao nhiêu môn, và vì sao
 * form chỉ hiện một phần.
 *
 * <p>Ba con số cùng đúng và trước đây không chỗ nào nói ra: đặc tả có 9 hệ,
 * registry phương pháp có 18 mục (vì mỗi hệ được tách phần lập lá số và phần
 * luận giải), còn code có 6 engine. Người dùng đếm ô chọn trên form và kết luận
 * đó là tất cả.
 */
export default async function HeThongPage() {
  let methodologies: MethodologyDto[] = [];
  let apiError: string | null = null;
  try {
    methodologies = await listMethodologies();
  } catch {
    apiError =
      "Không đọc được danh sách phương pháp từ máy chủ. Phần dưới vẫn hiển thị từ dữ liệu tĩnh của giao diện.";
  }

  const byStatus = groupByStatus(methodologies);

  return (
    <div className="mx-auto w-full max-w-5xl space-y-8 px-4 py-8">
      <header className="space-y-2">
        <h1 className="text-2xl font-semibold text-slate-900">Hệ thống</h1>
        <p className="max-w-3xl text-sm leading-relaxed text-slate-600">
          Đặc tả của Destiny OS nêu <strong>9 hệ thống</strong>. Sáu hệ đã có engine và chạy được;
          ba hệ chưa. Trang này nói rõ hệ nào là hệ nào, và hệ chưa chạy thì còn vướng gì — thay vì
          để chúng biến mất khỏi giao diện như thể không tồn tại.
        </p>
      </header>

      <section className="rounded-xl border border-slate-200 bg-white p-5">
        <h2 className="text-sm font-semibold text-slate-900">Ba con số, đều đúng</h2>
        <dl className="mt-3 grid gap-4 sm:grid-cols-3">
          <div>
            <dt className="text-xs text-slate-500">Trong đặc tả</dt>
            <dd className="text-2xl font-semibold text-slate-900">{SYSTEM_INVENTORY.length}</dd>
            <p className="mt-1 text-xs text-slate-500">hệ thống huyền học</p>
          </div>
          <div>
            <dt className="text-xs text-slate-500">Có engine trong code</dt>
            <dd className="text-2xl font-semibold text-slate-900">{RUNNABLE_SYSTEMS.length}</dd>
            <p className="mt-1 text-xs text-slate-500">
              đều có ô chọn ở Trung tâm quyết định
            </p>
          </div>
          <div>
            <dt className="text-xs text-slate-500">Mục trong registry phương pháp</dt>
            <dd className="text-2xl font-semibold text-slate-900">
              {methodologies.length > 0 ? methodologies.length : "—"}
            </dd>
            <p className="mt-1 text-xs text-slate-500">
              nhiều hơn số hệ, vì mỗi hệ tách riêng phần lập lá số và phần luận giải
            </p>
          </div>
        </dl>
      </section>

      <section className="space-y-3">
        <h2 className="text-lg font-semibold text-slate-900">
          Sáu hệ đang chạy được
        </h2>
        <div className="grid gap-3 md:grid-cols-2">
          {RUNNABLE_SYSTEMS.map((sys) => (
            <SystemCard key={sys.id} system={sys} available />
          ))}
        </div>
      </section>

      <section className="space-y-3">
        <h2 className="text-lg font-semibold text-slate-900">Ba hệ chưa chạy được</h2>
        <div className="grid gap-3 md:grid-cols-2">
          {UNBUILT_SYSTEMS.map((sys) => (
            <SystemCard key={sys.id} system={sys} available={false} />
          ))}
        </div>
      </section>

      <section className="space-y-3">
        <h2 className="text-lg font-semibold text-slate-900">Registry phương pháp</h2>
        <p className="max-w-3xl text-sm text-slate-600">
          Mỗi hệ được đăng ký thành một hoặc nhiều <em>phương pháp</em>, mỗi phương pháp có trường
          phái, nguồn và trạng thái riêng. Một phương pháp bị chặn vẫn nằm trong danh sách với lý
          do rõ ràng — không bị giấu đi.
        </p>

        {apiError && (
          <p className="rounded-md bg-amber-50 px-4 py-3 text-sm text-amber-900">{apiError}</p>
        )}

        {byStatus.map(({ title, note, items }) =>
          items.length === 0 ? null : (
            <div key={title} className="rounded-xl border border-slate-200 bg-white p-4">
              <h3 className="text-sm font-semibold text-slate-900">
                {title} ({items.length})
              </h3>
              <p className="mb-2 text-xs text-slate-500">{note}</p>
              <ul className="divide-y divide-slate-100">
                {items.map((m) => (
                  <li key={m.methodologyId} className="py-2">
                    <div className="flex flex-wrap items-center gap-2">
                      <span className="text-sm font-medium text-slate-800">{m.displayNameVi}</span>
                      {m.status && <LabeledBadge value={m.status} />}
                      {m.version && (
                        <span className="text-[11px] text-slate-400">phiên bản {m.version}</span>
                      )}
                    </div>
                    {m.school && <p className="mt-0.5 text-xs text-slate-600">Trường phái: {m.school}</p>}
                    {m.notes && <p className="mt-0.5 text-xs text-slate-500">{m.notes}</p>}
                    {m.researchIds.length > 0 && (
                      <p className="mt-0.5 text-[11px] text-slate-400">
                        Mã mục nghiên cứu đang chặn: {m.researchIds.join(", ")}
                      </p>
                    )}
                  </li>
                ))}
              </ul>
            </div>
          ),
        )}
      </section>

      <p className="text-sm text-slate-600">
        Muốn chạy thử thì sang{" "}
        <Link href="/trung-tam-quyet-dinh" className="font-medium underline underline-offset-2">
          Trung tâm quyết định
        </Link>
        .
      </p>
    </div>
  );
}

function SystemCard({ system, available }: { system: SystemEntry; available: boolean }) {
  return (
    <article
      className={`rounded-xl border p-4 ${
        available ? "border-slate-200 bg-white" : "border-dashed border-slate-300 bg-slate-50"
      }`}
    >
      <div className="flex flex-wrap items-center gap-2">
        <h3 className="text-sm font-semibold text-slate-900">{system.nameVi}</h3>
        <span
          className={`rounded-full px-2 py-0.5 text-[11px] font-medium ${
            available ? "bg-emerald-100 text-emerald-800" : "bg-slate-200 text-slate-600"
          }`}
        >
          {available ? "Chạy được" : "Chưa có engine"}
        </span>
        {system.formToggle && (
          <span className="rounded-full bg-indigo-100 px-2 py-0.5 text-[11px] font-medium text-indigo-800">
            Có ô chọn ở form
          </span>
        )}
      </div>
      <p className="mt-1.5 text-xs leading-relaxed text-slate-600">{system.stateVi}</p>
    </article>
  );
}

/**
 * Gộp registry theo trạng thái.
 *
 * <p>Bốn nhóm thay vì một bảng dài, vì "đang chạy được" và "chưa xác minh
 * nguồn" là hai câu chuyện khác nhau và trộn chung thì không đọc ra được cái
 * nào.
 */
function groupByStatus(items: MethodologyDto[]) {
  const of = (...codes: string[]) =>
    items.filter((m) => m.status !== null && codes.includes(m.status.technical));

  return [
    {
      title: "Đang chạy được",
      note: "Thuật toán đã xác minh, có nguồn, và được phép cho ra kết quả thật.",
      items: of("PRODUCTION_READY", "CONTENT_REQUIRED"),
    },
    {
      title: "Đang chờ xác minh nguồn",
      note: "Không được phép tính, vì chưa xác minh được cách làm hoặc chưa chốt trường phái. Bỏ trống là có chủ đích.",
      items: of("RESEARCH_REQUIRED", "DECISION_REQUIRED"),
    },
    {
      title: "Đã đặc tả, chưa dựng",
      note: "Đã mô tả trong tài liệu nhưng chưa có mã nguồn.",
      items: of("NOT_IMPLEMENTED"),
    },
    {
      title: "Ngoài phạm vi",
      note: "Cố ý không làm. Đây là một quyết định đã ghi lại, không phải một việc bị quên.",
      items: of("OUT_OF_SCOPE"),
    },
  ];
}
