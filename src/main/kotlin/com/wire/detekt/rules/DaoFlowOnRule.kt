package com.wire.detekt.rules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import io.gitlab.arturbosch.detekt.api.config
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtLambdaArgument

/**
 * This rule enforces that in DAO classes, any call to `.asFlow()` must be followed
 * by a context-controlling operator as the last operation in the chain.
 *
 * Valid terminators:
 * - `.flowOn(dispatcher)` - explicitly sets the upstream dispatcher
 * - `.shareIn(scope, ...)` - converts to SharedFlow with scope-defined context
 * - `.stateIn(scope, ...)` - converts to StateFlow with scope-defined context
 *
 * This ensures proper dispatcher handling for database flow operations.
 *
 * Examples:
 * - Correct: `query.asFlow().mapToList().flowOn(dispatcher)`
 * - Correct: `query.asFlow().mapToList().shareIn(scope, SharingStarted.Lazily, 1)`
 * - Correct: `query.asFlow().mapToList().stateIn(scope, SharingStarted.Lazily, null)`
 * - Wrong: `query.asFlow().mapToList()` (missing context control)
 * - Wrong: `query.asFlow().flowOn(dispatcher).mapToList()` (flowOn is not last)
 *
 * The class name suffixes to check can be configured via the `classSuffixes` property.
 * Default suffixes: "DAO", "DAOImpl", "Dao", "DaoImpl"
 */
class DaoFlowOnRule(config: Config = Config.empty) : Rule(config) {

    override val issue: Issue = Issue(
        id = javaClass.simpleName,
        severity = Severity.Defect,
        description = "DAO classes must call .flowOn(), .shareIn(), or .stateIn() as the last operation after .asFlow()",
        debt = Debt.FIVE_MINS
    )

    private val classSuffixes: List<String> by config(listOf("DAO", "DAOImpl", "Dao", "DaoImpl"))
    
    private val validTerminators = listOf("flowOn", "shareIn", "stateIn")

    private var currentClassName: String? = null
    private var isInDao = false

    override fun visitClass(klass: KtClass) {
        currentClassName = klass.name
        isInDao = currentClassName?.let { name ->
            classSuffixes.any { suffix -> name.endsWith(suffix) }
        } ?: false
        super.visitClass(klass)
        isInDao = false
        currentClassName = null
    }

    override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
        super.visitDotQualifiedExpression(expression)

        if (!isInDao) return

        // Only check root expressions (those that don't have a parent DotQualifiedExpression)
        if (expression.parent is KtDotQualifiedExpression) return

        // Check if this expression directly contains .asFlow() (not inside a lambda)
        if (containsAsFlowDirectly(expression)) {
            // Check if the last call in the chain is a valid terminator
            if (!endsWithValidTerminator(expression)) {
                val message = "In $currentClassName, calls to .asFlow() must be followed by " +
                        ".flowOn(), .shareIn(), or .stateIn() as the last operation in the chain"
                report(expression, message)
            }
        }
    }

    /**
     * Checks if the expression tree contains a call to .asFlow() directly in the chain,
     * not inside a lambda argument.
     */
    private fun containsAsFlowDirectly(expression: KtDotQualifiedExpression): Boolean {
        // Walk the chain and check each call, excluding lambda arguments
        var current: KtDotQualifiedExpression? = expression
        
        while (current != null) {
            val selector = current.selectorExpression
            
            // Check if this selector is .asFlow()
            if (selector?.text == "asFlow()") {
                return true
            }
            
            // Check if the selector is a call expression with .asFlow() in its name (but not in lambdas)
            if (selector is KtCallExpression) {
                val callName = selector.calleeExpression?.text
                if (callName == "asFlow") {
                    return true
                }
            }
            
            // Move to the receiver if it's also a dot qualified expression
            val receiver = current.receiverExpression
            current = receiver as? KtDotQualifiedExpression
        }
        
        return false
    }

    /**
     * Checks if the expression ends with a valid terminator (.flowOn(), .shareIn(), or .stateIn())
     */
    private fun endsWithValidTerminator(expression: KtDotQualifiedExpression): Boolean {
        val selector = expression.selectorExpression ?: return false
        
        // Check if it's a call expression
        if (selector is KtCallExpression) {
            val callName = selector.calleeExpression?.text
            return callName in validTerminators
        }
        
        // Fallback: check the text starts with one of the valid terminators
        val selectorText = selector.text
        return validTerminators.any { selectorText.startsWith("$it(") }
    }
}
