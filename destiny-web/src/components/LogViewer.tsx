"use client";

import { useMemo, useState, useSyncExternalStore } from "react";
import { AlertTriangle, Check, Copy, Info, Trash2, XCircle } from "lucide-react";
import {
  clearLog,
  formatLogForChat,
  readLog,
  shortTime,
  subscribeLog,
  type LogEntry,
  type LogLevel,
} from "@/lib/logBuffer";

const EMPTY: LogEntry[] = [];

const LEVEL_META: Record<LogLevel, { labelVi: string; className: string }> = {
  error: { labelVi: "Lỗi", className: "bg-rose-100 text-rose-800" },
  warn: { labelVi: "Cảnh báo", className: "bg-amber-100 text-amber-800" },
  info: { labelVi: "Thông tin", className: "bg-slate-100 text-slate-700" },
};

export function LogViewer() {
  const entries = useSyncExternalStore(subscribeLog, readLog, () => EMPTY);
  const [level, setLevel] = useState<LogLevel | "all">("all");
  const [query, setQuery] = useState("");
  const [copied, setCopied] = useState(false);
  const [copyFallback, setCopyFallback] = useState<string | null>(null);

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase();
    return entries
      .filter((e) => (level === "all" ? true : e.level === level))
      .filter((e) =>
        q === ""
          ? true
          : [e.message, e.url, e.code, e.calculationId, e.method]
              .filter(Boolean)
              .some((v) => String(v).toLowerCase().includes(q)),
      )
      .slice()
      .reverse();
  }, [entries, level, query]);

  const latestCalculationId = useMemo(
    () => [...entries].reverse().find((e) => e.calculationId)?.calculationId ?? null,
    [entries],
  );

  async function copyAll() {
    const text = formatLogForChat(entries);
    try {
      await navigator.clipboard.writeText(text);
      setCopied(true);
      window.setTimeout(() => setCopied(false), 2000);
    } catch {
      // clipboard cần ngữ cảnh bảo mật (https hoặc localhost). Truy cập qua IP
      // trong mạng LAN thì không có, nên phải có đường thoát chứ không im lặng
      // không làm gì.
      setCopyFallback(text);
    }
  }

  const counts = useMemo(
    () => ({
      all: entries.length,
      error: entries.filter((e) => e.level === "error").length,
      warn: entries.filter((e) => e.level === "warn").length,
      info: entries.filter((e) => e.level === "info").length,
    }),
    [entries],
  );

  return (
    <div className="space-y-4">
      {latestCalculationId && (
        <section className="rounded-xl border border-slate-200 bg-white p-4">
          <h2 className="text-xs font-semibold uppercase tracking-wide text-slate-500">
            Mã lần tính gần nhất
          </h2>
          <p className="mt-1 break-all font-mono text-sm text-slate-800">{latestCalculationId}</p>
          <p className="mt-1 text-xs text-slate-500">
            Đây là thứ hữu ích nhất khi báo lỗi — nó tra được toàn bộ lần chạy ở phía máy chủ.
          </p>
        </section>
      )}

      <div className="flex flex-wrap items-center gap-2">
        {(["all", "error", "warn", "info"] as const).map((key) => (
          <button
            key={key}
            type="button"
            onClick={() => setLevel(key)}
            className={`rounded-full px-3 py-1 text-xs font-medium transition ${
              level === key
                ? "bg-slate-900 text-white"
                : "bg-white text-slate-600 ring-1 ring-slate-200 hover:bg-slate-50"
            }`}
          >
            {key === "all" ? "Tất cả" : LEVEL_META[key].labelVi} ({counts[key]})
          </button>
        ))}

        <input
          type="search"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="Lọc theo đường dẫn, mã lỗi…"
          className="min-w-[12rem] flex-1 rounded-md border border-slate-300 px-3 py-1.5 text-sm"
        />

        <button
          type="button"
          onClick={copyAll}
          disabled={entries.length === 0}
          className="inline-flex items-center gap-1.5 rounded-md bg-slate-900 px-3 py-1.5 text-xs font-medium text-white transition hover:bg-slate-700 disabled:opacity-40"
        >
          {copied ? <Check aria-hidden className="h-3.5 w-3.5" /> : <Copy aria-hidden className="h-3.5 w-3.5" />}
          {copied ? "Đã copy" : "Sao chép toàn bộ"}
        </button>

        <button
          type="button"
          onClick={clearLog}
          disabled={entries.length === 0}
          className="inline-flex items-center gap-1.5 rounded-md border border-slate-300 px-3 py-1.5 text-xs font-medium text-slate-700 transition hover:bg-slate-50 disabled:opacity-40"
        >
          <Trash2 aria-hidden className="h-3.5 w-3.5" />
          Xoá nhật ký
        </button>
      </div>

      {copyFallback !== null && (
        <div className="rounded-md border border-amber-300 bg-amber-50 p-3">
          <p className="mb-2 text-xs text-amber-900">
            Trình duyệt không cho copy tự động ở ngữ cảnh này. Nội dung đã được chọn sẵn bên dưới —
            bấm Ctrl+C để chép.
          </p>
          <textarea
            readOnly
            autoFocus
            onFocus={(e) => e.currentTarget.select()}
            value={copyFallback}
            className="h-48 w-full rounded border border-amber-200 bg-white p-2 font-mono text-[11px]"
          />
        </div>
      )}

      {entries.length === 0 ? (
        <p className="rounded-xl border border-slate-200 bg-white p-6 text-sm text-slate-600">
          Chưa có gì được ghi trong phiên này. Hãy chạy một lần tính ở Trung tâm quyết định rồi
          quay lại đây.
        </p>
      ) : filtered.length === 0 ? (
        <p className="rounded-xl border border-slate-200 bg-white p-6 text-sm text-slate-600">
          Không có mục nào khớp bộ lọc hiện tại.
        </p>
      ) : (
        <ul className="space-y-1.5">
          {filtered.map((e) => (
            <LogRow key={e.id} entry={e} />
          ))}
        </ul>
      )}
    </div>
  );
}

function LogRow({ entry }: { entry: LogEntry }) {
  const [open, setOpen] = useState(false);
  const meta = LEVEL_META[entry.level];
  const Icon = entry.level === "error" ? XCircle : entry.level === "warn" ? AlertTriangle : Info;
  const hasDetail = Boolean(entry.detail && Object.keys(entry.detail).length > 0);

  return (
    <li className="rounded-lg border border-slate-200 bg-white">
      <button
        type="button"
        onClick={() => setOpen((v) => !v)}
        className="flex w-full flex-wrap items-center gap-2 px-3 py-2 text-left text-xs"
      >
        <span className="font-mono text-slate-400">{shortTime(entry.at)}</span>
        <span className={`inline-flex items-center gap-1 rounded-full px-2 py-0.5 font-medium ${meta.className}`}>
          <Icon aria-hidden className="h-3 w-3" />
          {meta.labelVi}
        </span>
        <span className="rounded bg-slate-100 px-1.5 py-0.5 text-[10px] text-slate-600">
          {entry.origin === "server" ? "máy chủ" : "trình duyệt"}
        </span>
        {entry.method && <span className="font-mono text-slate-500">{entry.method}</span>}
        {entry.url && <span className="font-mono text-slate-700">{entry.url}</span>}
        {entry.status !== undefined && (
          <span className="font-mono text-slate-500">HTTP {entry.status}</span>
        )}
        {entry.durationMs !== undefined && (
          <span className="tabular-nums text-slate-500">{Math.round(entry.durationMs)}ms</span>
        )}
        {entry.code && (
          <span className="rounded bg-slate-100 px-1.5 py-0.5 font-mono text-[10px] text-slate-700">
            {entry.code}
          </span>
        )}
        <span className="min-w-0 flex-1 truncate text-slate-700">{entry.message}</span>
      </button>

      {open && (
        <div className="border-t border-slate-100 px-3 py-2 text-xs">
          <p className="text-slate-700">{entry.message}</p>
          {entry.calculationId && (
            <p className="mt-1 break-all font-mono text-[11px] text-slate-600">
              Mã lần tính: {entry.calculationId}
            </p>
          )}
          {hasDetail && (
            <pre className="mt-1 overflow-auto rounded bg-slate-50 p-2 font-mono text-[11px] text-slate-600">
              {JSON.stringify(entry.detail, null, 2)}
            </pre>
          )}
        </div>
      )}
    </li>
  );
}
