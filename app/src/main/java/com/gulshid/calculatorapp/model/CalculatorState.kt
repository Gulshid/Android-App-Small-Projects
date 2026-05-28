package com.gulshid.calculatorapp.model


/**
 * Immutable data class representing the full state of the calculator.
 * Used by ViewModel to expose state to the UI via LiveData.
 */
data class CalculatorState(
    val expression: String = "",       // Full expression shown in secondary display
    val displayValue: String = "0",    // Primary/main display value
    val isError: Boolean = false,      // Whether current state is an error
    val justEvaluated: Boolean = false // Whether last action was '=' evaluation
)