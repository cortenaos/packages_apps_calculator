/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026-present The CortenaOS Project
 */
package app.cortena.calculator.engine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Pure state-machine that drives the calculator logic.
 *
 * Compose observes [state] (backed by `mutableStateOf`) so any mutation triggers a recomposition in
 * the UI layer. The engine itself has zero knowledge of layout or components — it only knows how to
 * transform [State].
 *
 * Arithmetic evaluation is intentionally simple (left-to-right, no operator precedence beyond × ÷
 * before + −). A proper expression parser is out of scope for Phase 1; the goal here is to validate
 * CortenaUI interaction patterns, not to ship a production calculator.
 */
class Engine {

    var state by mutableStateOf(State())
        private set

    /** True when the last action was `=`, so the next digit input starts fresh. */
    private var evaluated = false

    // Digit & Decimal
    fun onDigit(digit: Char) {
        if (evaluated) {
            // After pressing "=", a new digit starts a brand-new expression.
            state = State(display = digit.toString())
            evaluated = false
            return
        }

        val current = state.display
        // Prevent leading zeros: "0" → "5", not "05".
        val next = if (current == "0") digit.toString() else current + digit
        state = state.copy(display = next)
    }

    fun onDecimal() {
        if (evaluated) {
            state = State(display = "0,")
            evaluated = false
            return
        }
        if (',' !in state.display) {
            state = state.copy(display = state.display + ",")
        }
    }

    // Negate (+/-)
    fun onNegate() {
        val current = state.display
        if (current == "0") return
        val next = if (current.startsWith("-")) current.drop(1) else "-$current"
        state = state.copy(display = next)
    }

    /** Whether the user has entered any input (display ≠ initial "0" or expression is active). */
    val hasInput: Boolean
        get() = state.display != "0" || state.expression.isNotEmpty()

    // Operators
    fun onOperator(op: Char) {
        evaluated = false
        val expr = state.expression
        val display = state.display

        // If the expression already ends with an operator (user is switching),
        // replace the last operator instead of appending a new operand.
        if (expr.isNotEmpty() && expr.last() in "+-×÷") {
            state = state.copy(expression = expr.dropLast(1) + op)
            return
        }

        state = state.copy(expression = expr + display + op, display = "0")
    }

    // Percent
    fun onPercent() {
        val value = state.display.replace(',', '.').toDoubleOrNull() ?: return
        val result = value / 100.0
        state = state.copy(display = formatResult(result))
        evaluated = false
    }

    // Equals
    fun onEquals() {
        val fullExpr = state.expression + state.display.replace(',', '.')
        if (fullExpr.isEmpty()) return

        val result = evaluate(fullExpr)
        state = State(expression = "$fullExpr=", display = formatResult(result))
        evaluated = true
    }

    // Clear & Backspace
    fun onClear() {
        state = State()
        evaluated = false
    }

    fun onBackspace() {
        if (evaluated) {
            // After "=" backspace clears everything.
            onClear()
            return
        }
        val current = state.display
        val next = current.dropLast(1)
        state = state.copy(display = if (next.isEmpty() || next == "-") "0" else next)
    }

    /** Clears only the current entry (display), keeping the expression. */
    fun onClearEntry() {
        state = state.copy(display = "0")
        evaluated = false
    }

    /**
     * Tokenizes the expression into numbers and operators, then evaluates with standard arithmetic
     * precedence (× ÷ before + −).
     */
    private fun evaluate(expr: String): Double {
        val tokens = tokenize(expr)
        if (tokens.isEmpty()) return 0.0

        // First pass: resolve × and ÷
        val afterMulDiv = mutableListOf(tokens[0].toDouble())
        var i = 1
        while (i < tokens.size - 1) {
            val op = tokens[i]
            val next = tokens[i + 1].toDouble()
            when (op) {
                "×" -> afterMulDiv[afterMulDiv.lastIndex] = afterMulDiv.last() * next

                "÷" ->
                    afterMulDiv[afterMulDiv.lastIndex] =
                        if (next != 0.0) afterMulDiv.last() / next else Double.NaN

                else -> {
                    afterMulDiv.add(if (op == "-") -next else next)
                }
            }
            i += 2
        }

        // Second pass: sum everything (+ − already folded into sign).
        return afterMulDiv.sum()
    }

    /**
     * Splits an expression string like "12.5+3×7" into ["12.5", "+", "3", "×", "7"]. Handles
     * negative leading numbers and the Unicode operators ×÷.
     */
    private fun tokenize(expr: String): List<String> {
        val result = mutableListOf<String>()
        val buffer = StringBuilder()

        for (ch in expr) {
            when (ch) {
                in "+-×÷" if buffer.isNotEmpty() -> {
                    result.add(buffer.toString())
                    result.add(ch.toString())
                    buffer.clear()
                }
                '=' -> {
                    // Trailing "=" from the expression — ignore.
                }
                else -> buffer.append(ch)
            }
        }
        if (buffer.isNotEmpty()) result.add(buffer.toString())
        return result
    }

    /** Formats a Double for display — strips trailing ".0" for clean integers. */
    private fun formatResult(value: Double): String {
        if (value.isNaN()) return "Error"
        if (value.isInfinite()) return "Error"
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            // Cap to 10 decimal places to avoid floating-point noise.
            "%.10g".format(value).trimEnd('0').trimEnd('.').replace('.', ',')
        }
    }
}
