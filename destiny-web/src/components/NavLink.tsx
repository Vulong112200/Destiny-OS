"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

/**
 * A nav item that knows whether it is the current page.
 *
 * Split into its own client component so `layout.tsx` stays a server
 * component — only the active-state check needs the pathname, and marking the
 * whole shell `"use client"` to get it would push the header, the nav and the
 * footer into the client bundle for one boolean.
 */
export function NavLink({ href, label }: { href: string; label: string }) {
  const pathname = usePathname();
  // Every result page belongs to the Decision Center as far as the nav is
  // concerned - a user who lands on /ket-qua/... arrived from there and
  // should still see where they are.
  const active =
    href === "/"
      ? pathname === "/"
      : pathname.startsWith(href) ||
        (href === "/trung-tam-quyet-dinh" && pathname.startsWith("/ket-qua"));

  return (
    <Link
      href={href}
      aria-current={active ? "page" : undefined}
      className={`rounded-md px-3 py-2 text-sm font-medium transition-colors ${
        active
          ? "bg-slate-900 text-white"
          : "text-slate-700 hover:bg-slate-100"
      }`}
    >
      {label}
    </Link>
  );
}
