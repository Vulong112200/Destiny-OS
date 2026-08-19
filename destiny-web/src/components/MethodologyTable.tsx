import type { MethodologyDto } from "@/lib/types";
import { LabeledBadge } from "./LabeledBadge";

/**
 * ADR D7 made visible: a research-blocked methodology is a normal row here
 * with an honest status, never hidden because it isn't finished.
 */
export function MethodologyTable({ methodologies }: { methodologies: MethodologyDto[] }) {
  return (
    <div className="overflow-x-auto rounded-lg border border-slate-200">
      <table className="min-w-full divide-y divide-slate-200 text-sm">
        <thead className="bg-slate-50">
          <tr>
            <th className="px-4 py-3 text-left font-semibold text-slate-700">Phương pháp</th>
            <th className="px-4 py-3 text-left font-semibold text-slate-700">Lĩnh vực</th>
            <th className="px-4 py-3 text-left font-semibold text-slate-700">Trạng thái</th>
            <th className="px-4 py-3 text-left font-semibold text-slate-700">Trường phái / Nguồn</th>
            <th className="px-4 py-3 text-left font-semibold text-slate-700">Ghi chú</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-100">
          {methodologies.map((m) => (
            <tr key={m.methodologyId}>
              <td className="px-4 py-3 font-medium text-slate-900" title={m.methodologyId}>
                {m.displayNameVi}
              </td>
              <td className="px-4 py-3 text-slate-600">{m.domain ?? "—"}</td>
              <td className="px-4 py-3">{m.status ? <LabeledBadge value={m.status} /> : "—"}</td>
              <td className="px-4 py-3 text-slate-600">
                {m.school ? (
                  <span title={m.source ?? undefined}>{m.school}</span>
                ) : (
                  <span className="text-slate-400">Chưa xác định</span>
                )}
              </td>
              <td className="max-w-md px-4 py-3 text-slate-500">{m.notes ?? "—"}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
