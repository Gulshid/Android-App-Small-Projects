package com.gulshid.calculatorapp.viewModel


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gulshid.calculatorapp.model.CalculatorState
import com.gulshid.calculatorapp.utils.ExpressionEvaluator

/**
 * CalculatorViewModel — the single source of truth for calculator logic.
 *
 * Exposes [state] as LiveData<CalculatorState> so the UI (MainActivity)
 * only observes and dispatches user actions — no business logic in the View.
 *
 * Responsibilities:
 *  - Maintain expression & display value
 *  - Handle digit, operator, decimal, equals, backspace, clear actions
 *  - Delegate evaluation to ExpressionEvaluator
 */
class CalculatorViewModel : ViewModel() {

    private val _state = MutableLiveData(CalculatorState())
    val state: LiveData<CalculatorState> = _state

    private val current get() = _state.value ?: CalculatorState()

    // ── Public Action Handlers ─────────────────────────────────────────────────

    /** Called when a digit button (0-9) is pressed */
    fun onDigit(digit: String) {
        val s = current
        val newExpression = when {
            s.isError -> digit                                    // reset after error
            s.justEvaluated -> digit                             // start fresh after '='
            s.expression == "0" -> digit                         // replace leading zero
            else -> s.expression + digit
        }
        _state.value = s.copy(
            expression = newExpression,
            displayValue = getLastOperand(newExpression),
            isError = false,
            justEvaluated = false
        )
    }

    /** Called when an operator (+, -, ×, ÷) is pressed */
    fun onOperator(op: String) {
        val s = current
        if (s.isError) return

        var expr = if (s.justEvaluated) s.displayValue else s.expression

        // Replace trailing operator if present (e.g. "12+" → "12×")
        expr = if (expr.isNotEmpty() && isOperator(expr.last())) {
            expr.dropLast(1) + op
        } else {
            expr + op
        }

        _state.value = s.copy(
            expression = expr,
            displayValue = op,
            justEvaluated = false
        )
    }

    /** Called when '.' decimal button is pressed */
    fun onDecimal() {
        val s = current
        val lastOperand = getLastOperand(s.expression)

        // Only add decimal if the current operand doesn't already have one
        if (!lastOperand.contains('.')) {
            val prefix = if (s.justEvaluated || s.isError) "0" else s.expression
            val newExpr = if (lastOperand.isEmpty()) "$prefix." else s.expression + "."
            _state.value = s.copy(
                expression = newExpr,
                displayValue = getLastOperand(newExpr),
                isError = false,
                justEvaluated = false
            )
        }
    }

    /** Called when '=' is pressed */
    fun onEquals() {
        val s = current
        if (s.isError || s.expression.isEmpty()) return

        // Avoid double evaluation
        if (s.justEvaluated) return

        val result = ExpressionEvaluator.evaluate(s.expression)
        val isError = result.startsWith("Error")

        _state.value = s.copy(
            expression = if (isError) s.expression else s.expression + "=",
            displayValue = result,
            isError = isError,
            justEvaluated = !isError
        )
    }

    /** Called when '+/-' toggle is pressed */
    fun onToggleSign() {
        val s = current
        if (s.isError) return

        val lastOperand = getLastOperand(s.expression)
        if (lastOperand.isEmpty() || lastOperand == "0") return

        val newExpr = if (lastOperand.startsWith("-")) {
            s.expression.dropLast(lastOperand.length) + lastOperand.drop(1)
        } else {
            s.expression.dropLast(lastOperand.length) + "-$lastOperand"
        }

        _state.value = s.copy(
            expression = newExpr,
            displayValue = getLastOperand(newExpr)
        )
    }

    /** Called when '%' percent button is pressed */
    fun onPercent() {
        val s = current
        if (s.isError) return

        val lastOperand = getLastOperand(s.expression)
        if (lastOperand.isEmpty()) return

        val percentValue = (lastOperand.toDoubleOrNull() ?: return) / 100
        val formatted = if (percentValue == kotlin.math.floor(percentValue))
            percentValue.toLong().toString()
        else
            "%.8f".format(percentValue).trimEnd('0').trimEnd('.')

        val newExpr = s.expression.dropLast(lastOperand.length) + formatted

        _state.value = s.copy(
            expression = newExpr,
            displayValue = formatted
        )
    }

    /** Called when backspace (⌫) is pressed */
    fun onBackspace() {
        val s = current
        if (s.isError || s.justEvaluated) {
            onClear(); return
        }
        val newExpr = if (s.expression.length <= 1) "0" else s.expression.dropLast(1)
        _state.value = s.copy(
            expression = newExpr,
            displayValue = getLastOperand(newExpr).ifEmpty { "0" }
        )
    }

    /** Called when 'C' (clear) is pressed */
    fun onClear() {
        _state.value = CalculatorState() // reset to initial state
    }

    // ── Private Helpers ────────────────────────────────────────────────────────

    /** Extract the rightmost operand from an expression string */
    private fun getLastOperand(expr: String): String {
        if (expr.isEmpty()) return "0"
        val sb = StringBuilder()
        for (i in expr.indices.reversed()) {
            val c = expr[i]
            if (isOperator(c) && i != expr.lastIndex) break
            sb.insert(0, c)
        }
        return sb.toString().trimStart('+', '×', '÷')
    }

    private fun isOperator(c: Char) = c in listOf('+', '-', '×', '÷')
}