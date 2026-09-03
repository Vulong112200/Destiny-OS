import type { Metadata } from "next";
import Link from "next/link";
import { Be_Vietnam_Pro, Geist_Mono } from "next/font/google";
import { Disclaimer } from "@/components/Disclaimer";
import { NavLink } from "@/components/NavLink";
import "./globals.css";

/**
 * Be Vietnam Pro, not Geist.
 *
 * Geist was loaded with `subsets: ["latin"]`, which does not cover Vietnamese
 * diacritics - so every ế, ữ and ọ on a Vietnamese-first product fell back to
 * a system font mid-sentence. (It never actually rendered at all, because
 * globals.css also set `font-family: Arial` on body and won; fixing only that
 * would have surfaced the subset problem instead of the Arial one.) Be Vietnam
 * Pro is drawn for this language and covers it properly.
 */
const sans = Be_Vietnam_Pro({
  variable: "--font-be-vietnam",
  subsets: ["latin", "vietnamese"],
  weight: ["400", "500", "600", "700"],
  display: "swap",
});

/** Kept for calculation ids and hashes, which are hex and never Vietnamese. */
const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
  display: "swap",
});

export const metadata: Metadata = {
  title: "Destiny OS",
  description: "Nền tảng tính toán huyền học tất định và hỗ trợ ra quyết định theo kịch bản.",
};

// UI_UX_VIETNAMESE_SPEC section 2's 13-item nav. Only 3 are backed by a
// real API today (see docs/UI_UX_VIETNAMESE_SPEC.md and this session's
// frontend plan) - the rest are named honestly as "Sắp ra mắt" rather than
// linked to a page with nothing real behind it.
const NAV_ITEMS: { href: string; label: string }[] = [
  { href: "/", label: "Tổng quan" },
  { href: "/trung-tam-quyet-dinh", label: "Trung tâm quyết định" },
  { href: "/he-thong", label: "Hệ thống" },
  { href: "/lich-su", label: "Lịch sử" },
  { href: "/nhat-ky", label: "Nhật ký" },
];

const COMING_SOON_ITEMS = [
  "Hồ sơ",
  "Phương Đông",
  "Phương Tây",
  "Chu kỳ",
  "Dòng thời gian",
  "Tương hợp",
  "Tarot",
  "Kinh Dịch",
  "Báo cáo",
  "Cài đặt",
];

export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    <html
      lang="vi"
      className={`${sans.variable} ${geistMono.variable} h-full antialiased`}
    >
      <body className="flex min-h-full flex-col bg-slate-50 font-sans text-slate-900">
        {/*
          Sticky, because the result page is long by nature - it carries a
          chart, an evidence trail and a conflict list that a user is meant to
          be able to compare against each other. Losing the nav at the top of
          that is what made "kéo xuống liên tục" feel like being stranded.
        */}
        <header className="sticky top-0 z-30 border-b border-slate-200 bg-white/90 backdrop-blur">
          <div className="mx-auto flex w-full max-w-[1600px] items-center justify-between gap-4 px-4 py-3 sm:px-6 lg:px-10">
            <Link href="/" className="flex shrink-0 items-center gap-2 text-lg font-bold text-slate-900">
              <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-slate-900 text-sm font-bold text-white">
                洛
              </span>
              Destiny OS
            </Link>
            <nav className="flex items-center gap-1">
              {NAV_ITEMS.map((item) => (
                <NavLink key={item.href} href={item.href} label={item.label} />
              ))}
              <details className="relative">
                <summary className="list-none cursor-pointer rounded-md px-3 py-2 text-sm font-medium text-slate-400 marker:content-none [&::-webkit-details-marker]:hidden">
                  Sắp ra mắt ({COMING_SOON_ITEMS.length})
                </summary>
                <div className="absolute right-0 z-10 mt-1 w-48 rounded-lg border border-slate-200 bg-white p-2 shadow-lg">
                  <p className="mb-1 px-2 text-xs text-slate-400">
                    Chưa có hệ thống hỗ trợ phía sau
                  </p>
                  {COMING_SOON_ITEMS.map((label) => (
                    <span key={label} className="block rounded-md px-2 py-1.5 text-sm text-slate-400">
                      {label}
                    </span>
                  ))}
                </div>
              </details>
            </nav>
          </div>
        </header>
        {/*
          The shell is deliberately wide (1600px) and each page narrows itself
          to what its own content needs, rather than the shell capping every
          page at a reading width. A result page is a dashboard - a chart, a
          sidebar and an evidence table meant to be seen together - while an
          article-shaped page still sets its own `max-w-*`. Capping here at
          max-w-5xl forced the dashboard into a column and produced the
          endless scroll this replaces.
        */}
        <main className="mx-auto w-full max-w-[1600px] flex-1 px-4 py-6 sm:px-6 lg:px-10">
          {children}
        </main>
        <footer className="border-t border-slate-200 bg-white">
          <Disclaimer />
        </footer>
      </body>
    </html>
  );
}
