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
 * Arithmetic evaluation uses standard precedence (× ÷ before + −).
 */
class Engine {

    var state by mutableStateOf(State())
        private set

    /** True when the last action was `=`, so the next digit input starts fresh. */
    private var evaluated = false

    /** True when the last action was an operator, so the next digit replaces "0". */
    private var operatorJustPressed = false

    companion object {
        /** Maximum number of digit characters allowed in a single operand. */
        const val MAX_DIGITS = 9
    }

    /** Counts only digit characters in the display (excludes minus, comma, dots). */
    private fun digitCount(display: String): Int = display.count { it.isDigit() }

    // Digit & Decimal
    fun onDigit(digit: Char) {
        if (evaluated) {
            state = State(display = digit.toString())
            evaluated = false
            return
        }

        // After pressing an operator, the first digit starts a new operand.
        if (operatorJustPressed) {
            state = state.copy(display = digit.toString(), activeOperator = null)
            operatorJustPressed = false
            return
        }

        val current = state.display
        // Guard: max 9 digits per operand.
        if (digitCount(current) >= MAX_DIGITS) return
        val next = if (current == "0") digit.toString() else current + digit
        state = state.copy(display = next, activeOperator = null)
    }

    fun onDecimal() {
        if (evaluated) {
            state = State(display = "0,")
            evaluated = false
            return
        }

        if (operatorJustPressed) {
            state = state.copy(display = "0,", activeOperator = null)
            operatorJustPressed = false
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
        // If "-" is pressed at the very start (no expression, display is "0"),
        // treat it as starting a negative number.
        if (op == '-' && state.expression.isEmpty() && state.display == "0" && !evaluated) {
            state = state.copy(display = "-")
            return
        }

        // After evaluation, chain: use the result as left-hand side.
        if (evaluated) {
            val display = state.display
            state =
                State(
                    expression = displayToInternal(display) + op,
                    display = display,
                    activeOperator = op,
                )
            evaluated = false
            operatorJustPressed = true
            return
        }

        val expr = state.expression
        val display = state.display

        // If an operator was just pressed (user is switching operators),
        // replace the last operator instead of appending a new operand.
        if (operatorJustPressed && expr.isNotEmpty() && expr.last() in "+-×÷") {
            state = state.copy(expression = expr.dropLast(1) + op, activeOperator = op)
            return
        }

        state =
            state.copy(
                expression = expr + displayToInternal(display) + op,
                display = "0",
                activeOperator = op,
            )
        operatorJustPressed = true
    }

    // Percent
    fun onPercent() {
        val value = displayToInternal(state.display).toDoubleOrNull() ?: return
        val result = value / 100.0
        state = state.copy(display = formatResult(result), activeOperator = null)
        evaluated = false
    }

    // Equals
    fun onEquals() {
        // Guard: don't evaluate if there's nothing to evaluate.
        if (state.expression.isEmpty()) return
        // Guard: if already evaluated, don't re-evaluate (prevents crash).
        if (evaluated) return

        val fullExpr = state.expression + displayToInternal(state.display)

        val result = evaluate(fullExpr)
        state = State(expression = "$fullExpr=", display = formatResult(result))
        evaluated = true
        operatorJustPressed = false
    }

    /** Full clear — resets everything to initial state. */
    fun onClear() {
        state = State()
        evaluated = false
        operatorJustPressed = false
    }

    /** Clear entry — resets display and active operator, keeping nothing. */
    fun onClearEntry() {
        state = State()
        evaluated = false
        operatorJustPressed = false
    }

    fun onBackspace() {
        if (evaluated) {
            onClear()
            return
        }
        val current = state.display
        val next = current.dropLast(1)
        state =
            state.copy(
                display = if (next.isEmpty() || next == "-") "0" else next,
                activeOperator = null,
            )
        operatorJustPressed = false
    }

    /**
     * Converts display string to internal format for arithmetic. Strips thousand-separator dots and
     * converts decimal comma to dot.
     */
    private fun displayToInternal(display: String): String =
        display.replace(".", "").replace(',', '.')

    /**
     * Tokenizes the expression into numbers and operators, then evaluates with standard arithmetic
     * precedence (× ÷ before + −).
     */
    private fun evaluate(expr: String): Double {
        val tokens = tokenize(expr)
        if (tokens.isEmpty()) return 0.0

        val first = tokens[0].toDoubleOrNull() ?: return 0.0

        // First pass: resolve × and ÷
        val afterMulDiv = mutableListOf(first)
        var i = 1
        while (i < tokens.size - 1) {
            val op = tokens[i]
            val next = tokens[i + 1].toDoubleOrNull() ?: break
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

        for ((index, ch) in expr.withIndex()) {
            when (ch) {
                in "+-×÷" if buffer.isNotEmpty() -> {
                    result.add(buffer.toString())
                    result.add(ch.toString())
                    buffer.clear()
                }
                // Leading minus at index 0 is part of the number, not an operator
                '-' if buffer.isEmpty() &&
                        (index == 0 || result.lastOrNull() in listOf("+", "-", "×", "÷")) -> {
                    buffer.append(ch)
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
