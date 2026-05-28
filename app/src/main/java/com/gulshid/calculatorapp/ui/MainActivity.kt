package com.gulshid.calculatorapp.ui


import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.gulshid.calculatorapp.R
import com.gulshid.calculatorapp.model.CalculatorState
import com.gulshid.calculatorapp.viewModel.CalculatorViewModel

/**
 * MainActivity — Pure View layer in MVVM.
 *
 * Responsibilities:
 *  - Observe [CalculatorViewModel.state] and update UI
 *  - Forward user button clicks to ViewModel as actions
 *  - Zero business logic
 */
class MainActivity : AppCompatActivity() {

    // ViewModel injected via KTX delegate (survives config changes)
    private val viewModel: CalculatorViewModel by viewModels()

    // Views
    private lateinit var tvExpression: TextView
    private lateinit var tvDisplay: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()
        observeState()
        setupClickListeners()
    }

    // ── View Binding ───────────────────────────────────────────────────────────

    private fun bindViews() {
        tvExpression = findViewById(R.id.tvExpression)
        tvDisplay    = findViewById(R.id.tvDisplay)
    }

    // ── State Observer ─────────────────────────────────────────────────────────

    private fun observeState() {
        viewModel.state.observe(this) { state : CalculatorState->
            tvExpression.text = state.expression
            tvDisplay.text    = state.displayValue

            // Auto-resize text for long numbers
            autoResizeDisplay(state.displayValue.length)

            // Tint display red on error
            val color = if (state.isError)
                ContextCompat.getColor(this, R.color.error_red)
            else
                ContextCompat.getColor(this, R.color.display_text)
            tvDisplay.setTextColor(color)
        }
    }

    private fun autoResizeDisplay(length: Int) {
        tvDisplay.textSize = when {
            length <= 6  -> 64f
            length <= 9  -> 48f
            length <= 12 -> 36f
            else         -> 28f
        }
    }

    // ── Click Listeners ────────────────────────────────────────────────────────

    private fun setupClickListeners() {
        // Digits
        val digits = mapOf(
            R.id.btn0 to "0", R.id.btn1 to "1", R.id.btn2 to "2",
            R.id.btn3 to "3", R.id.btn4 to "4", R.id.btn5 to "5",
            R.id.btn6 to "6", R.id.btn7 to "7", R.id.btn8 to "8",
            R.id.btn9 to "9"
        )
        digits.forEach { (id, digit) ->
            findViewById<Button>(id).setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                viewModel.onDigit(digit)
            }
        }

        // Operators
        val operators = mapOf(
            R.id.btnAdd      to "+",
            R.id.btnSubtract to "-",
            R.id.btnMultiply to "×",
            R.id.btnDivide   to "÷"
        )
        operators.forEach { (id, op) ->
            findViewById<Button>(id).setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                viewModel.onOperator(op)
            }
        }

        // Special actions
        findViewById<Button>(R.id.btnEquals).setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            viewModel.onEquals()
        }
        findViewById<Button>(R.id.btnClear).setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            viewModel.onClear()
        }
        findViewById<Button>(R.id.btnBackspace).setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            viewModel.onBackspace()
        }
        findViewById<Button>(R.id.btnDecimal).setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            viewModel.onDecimal()
        }
        findViewById<Button>(R.id.btnPercent).setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            viewModel.onPercent()
        }
        findViewById<Button>(R.id.btnToggleSign).setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            viewModel.onToggleSign()
        }
    }
}