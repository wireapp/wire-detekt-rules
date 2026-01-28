package com.wire.detekt.rules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import io.gitlab.arturbosch.detekt.api.config
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression

/**
 * This rule enforces that in DAO classes, any call to `.asFlow()` must be followed
 * by `.flowOn()` as the last operation in the chain.
 *
 * This ensures proper dispatcher handling for database flow operations.
 *
 * Examples:
 * - Correct: `query.asFlow().mapToList().flowOn(dispatcher)`
 * - Wrong: `query.asFlow().mapToList()` (missing flowOn)
 * - Wrong: `query.asFlow().flowOn(dispatcher).mapToList()` (flowOn is not last)
 *
 * The class name suffixes to check can be configured via the `classSuffixes` property.
 * Default suffixes: "DAO", "DAOImpl", "Dao", "DaoImpl"
 */
class DaoFlowOnRule(config: Config = Config.empty) : Rule(config) {

    override val issue: Issue = Issue(
        id = javaClass.simpleName,
        severity = Severity.Defect,
        description = "DAO classes must call .flowOn() as the last operation after .asFlow()",
        debt = Debt.FIVE_MINS
    )

    private val classSuffixes: List<String> by config(listOf("DAO", "DAOImpl", "Dao", "DaoImpl"))

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

        // Check if this expression contains .asFlow()
        if (containsAsFlow(expression)) {
            // Check if the last call in the chain is .flowOn()
            if (!endsWithFlowOn(expression)) {
                val message = "In $currentClassName, calls to .asFlow() must be followed by " +
                        ".flowOn() as the last operation in the chain"
                report(expression, message)
            }
        }
    }

    /**
     * Checks if the expression tree contains a call to .asFlow()
     */
    private fun containsAsFlow(expression: KtDotQualifiedExpression): Boolean {
        val text = expression.text
        return text.contains(".asFlow()")
    }

    /**
     * Checks if the expression ends with .flowOn()
     */
    private fun endsWithFlowOn(expression: KtDotQualifiedExpression): Boolean {
        val selectorText = expression.selectorExpression?.text ?: return false
        return selectorText.startsWith("flowOn(")
    }
}
