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
 * [display] is the primary value the user sees — either the current operand being entered or the
 * evaluated result after pressing equals.
 *
 * [activeOperator] is the currently selected operator character (÷, ×, -, +), or null if no
 * operator is pending. Used by the UI to highlight the active operator key.
 */
data class State(
    val expression: String = "",
    val display: String = "0",
    val activeOperator: Char? = null,
)
