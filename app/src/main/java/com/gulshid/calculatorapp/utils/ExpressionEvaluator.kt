package com.gulshid.calculatorapp.utils


/**
 * Utility object that evaluates a mathematical expression string.
 * Supports: +, -, ×, ÷, % and decimal numbers.
 * Uses a recursive descent / operator-precedence parser (no eval hacks).
 */
object ExpressionEvaluator {

    private const val DIVIDE_BY_ZERO = "Error: ÷ by 0"
    private const val INVALID_EXPR   = "Error: Invalid"

    /**
     * Public entry point.
     * @param expression e.g. "12+3×4÷2"
     * @return result as formatted String, or an error string
     */
    fun evaluate(expression: String): String {
        return try {
            val sanitized = expression
                .replace("×", "*")
                .replace("÷", "/")
                .replace("%", "/100")
                .trim()

            if (sanitized.isEmpty()) return "0"

            val result = parseExpression(sanitized.toCharArray(), intArrayOf(0))

            if (result.isInfinite() || result.isNaN()) {
                DIVIDE_BY_ZERO
            } else {
                formatResult(result)
            }
        } catch (e: ArithmeticException) {
            DIVIDE_BY_ZERO
        } catch (e: Exception) {
            INVALID_EXPR
        }
    }

    /** Format: remove trailing ".0" for whole numbers, limit decimals to 8 places */
    private fun formatResult(value: Double): String {
        return if (value == kotlin.math.floor(value) && !value.isInfinite()) {
            value.toLong().toString()
        } else {
            // Up to 8 decimal places, strip trailing zeros
            "%.8f".format(value).trimEnd('0').trimEnd('.')
        }
    }

    // ── Recursive Descent Parser ───────────────────────────────────────────────

    /** expression := term (('+' | '-') term)* */
    private fun parseExpression(chars: CharArray, pos: IntArray): Double{
        var result = parseTerm(chars, pos)
        while (pos[0] < chars.size) {
            when (chars[pos[0]]) {
                '+' -> { pos[0]++; result += parseTerm(chars, pos) }
                '-' -> { pos[0]++; result -= parseTerm(chars, pos) }
                else -> break
            }
        }
        return result
    }

    /** term := factor (('*' | '/') factor)* */
    private fun parseTerm(chars: CharArray, pos: IntArray): Double{
        var result = parseFactor(chars, pos)
        while (pos[0] < chars.size) {
            when (chars[pos[0]]) {
                '*' -> { pos[0]++; result *= parseFactor(chars, pos) }
                '/' -> {
                    pos[0]++
                    val divisor = parseFactor(chars, pos)
                    if (divisor == 0.0) throw ArithmeticException("Division by zero")
                    result /= divisor
                }
                else -> break
            }
        }
        return result
    }

    /** factor := number | '(' expression ')' | unary minus */
    private fun parseFactor(chars: CharArray, pos: IntArray): Double {
        skipSpaces(chars, pos)

        if (pos[0] >= chars.size) throw Exception("Unexpected end")

        // Unary minus
        if (chars[pos[0]] == '-') {
            pos[0]++
            return -parseFactor(chars, pos)
        }

        // Parenthesised sub-expression
        if (chars[pos[0]] == '(') {
            pos[0]++ // consume '('
            val result = parseExpression(chars, pos)
            if (pos[0] < chars.size && chars[pos[0]] == ')') pos[0]++ // consume ')'
            return result
        }

        // Number
        return parseNumber(chars, pos)
    }

    /** Parse a decimal number literal */
    private fun parseNumber(chars: CharArray, pos: IntArray): Double {
        val start = pos[0]
        while (pos[0] < chars.size && (chars[pos[0]].isDigit() || chars[pos[0]] == '.')) {
            pos[0]++
        }
        if (start == pos[0]) throw Exception("Expected number at pos ${pos[0]}")
        return chars.concatToString(start, pos[0]).toDouble()
    }

    private fun skipSpaces(chars: CharArray, pos: IntArray) {
        while (pos[0] < chars.size && chars[pos[0]] == ' ') pos[0]++
    }
}