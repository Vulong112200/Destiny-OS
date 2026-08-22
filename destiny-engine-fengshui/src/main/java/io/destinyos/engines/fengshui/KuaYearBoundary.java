package io.destinyos.engines.fengshui;

/**
 * Which convention decides the year a Kua number is computed from — the one
 * part of research item R7 that this engine does <strong>not</strong> resolve.
 *
 * <p>The two camps both state their position plainly, and no source was found
 * that arbitrates between them:
 * <ul>
 *   <li>{@link #LUNAR_NEW_YEAR} — Vietnamese practice computes from the lunar
 *       birth year ({@code năm sinh âm lịch}), e.g. {@code hoc.kabala.vn}.</li>
 *   <li>{@link #LAP_XUAN} — classical practice computes from the solar year,
 *       which turns at Lập Xuân around 4 February: <em>"if your birthday is in
 *       January or before February 4th, you must use the previous calendar
 *       year"</em>.</li>
 * </ul>
 *
 * <p><strong>Bát Tự's R18 decision does not transfer.</strong> R18 selected Lập
 * Xuân for Bát Tự on the strength of published Four Pillars tables that
 * demonstrably follow it. That evidence says nothing about Kua practice, and
 * adopting Lập Xuân here merely because a neighbouring engine did would be the
 * silent school selection Rule D exists to prevent.
 *
 * <p>So this engine computes <strong>both</strong>. The two agree for every
 * birth outside the Tết-to-Lập-Xuân window, which is the overwhelming majority;
 * inside it, the engine reports both answers and declines to emit a signal
 * rather than choosing on the user's behalf (Rule E: conflict is a valid
 * result).
 */
public enum KuaYearBoundary {
    /** Year turns at Lập Xuân (~4 February) — classical practice. */
    LAP_XUAN,
    /** Year turns at Tết — Vietnamese popular practice. */
    LUNAR_NEW_YEAR
}
