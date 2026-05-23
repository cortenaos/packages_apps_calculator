/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026-present The CortenaOS Project
 */
package app.cortena.calculator.engine

/**
 * UI-facing snapshot of the calculator's current state.
 *
 * [expression] is the full arithmetic expression built so far (e.g. "12+34"). Shown as secondary
 * text above the main display.
 *
 * [display] is the raw primary value (no thousand separators). Use [formattedDisplay] for the
 * UI-facing string with thousand-separator dots.
 *
 * [activeOperator] is the currently selected operator character (÷, ×, -, +), or null if no
 * operator is pending. Used by the UI to highlight the active operator key.
 */
data class State(
    val expression: String = "",
    val display: String = "0",
    val activeOperator: Char? = null,
) {
    /**
     * Display value formatted with thousand-separator dots.
     *
     * Examples:
     * - "1234567" → "1.234.567"
     * - "-1234567,89" → "-1.234.567,89"
     * - "0,5" → "0,5"
     * - "-" → "-"
     */
    val formattedDisplay: String
        get() {
            if (display == "Error" || display == "-") return display

            val negative = display.startsWith("-")
            val raw = if (negative) display.drop(1) else display

            // Split integer and decimal parts at comma
            val parts = raw.split(",", limit = 2)
            val intPart = parts[0]
            val decPart = if (parts.size > 1) ",${parts[1]}" else ""

            // Add dots every 3 digits from the right
            val formatted =
                buildString {
                        for ((i, ch) in intPart.reversed().withIndex()) {
                            if (i > 0 && i % 3 == 0) append('.')
                            append(ch)
                        }
                    }
                    .reversed()

            return (if (negative) "-" else "") + formatted + decPart
        }
}
