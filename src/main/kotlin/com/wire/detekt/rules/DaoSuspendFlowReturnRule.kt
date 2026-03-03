package com.wire.detekt.rules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import io.gitlab.arturbosch.detekt.api.config
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * In DAO classes, functions returning Flow-like types must not be suspend.
 */
class DaoSuspendFlowReturnRule(config: Config = Config.empty) : Rule(config) {

    override val issue: Issue = Issue(
        id = javaClass.simpleName,
        severity = Severity.Defect,
        description = "DAO classes must not declare Flow-returning functions as suspend",
        debt = Debt.FIVE_MINS
    )

    private val classSuffixes: List<String> by config(listOf("DAO", "DAOImpl", "Dao", "DaoImpl"))

    private val daoContext = ArrayDeque<Pair<String?, Boolean>>()

    override fun visitClassOrObject(classOrObject: KtClassOrObject) {
        val className = classOrObject.name
        val isDao = className?.let { name -> classSuffixes.any { suffix -> name.endsWith(suffix) } } ?: false
        daoContext.addLast(className to isDao)
        super.visitClassOrObject(classOrObject)
        daoContext.removeLast()
    }

    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)

        val (className, isDao) = daoContext.lastOrNull() ?: return
        if (!isDao) return
        if (!function.hasModifier(KtTokens.SUSPEND_KEYWORD)) return
        if (!returnsFlowType(function)) return

        report(
            function,
            "In ${className ?: "DAO class"}, function '${function.name}' returns a Flow and must not be declared as suspend"
        )
    }

    private fun returnsFlowType(function: KtNamedFunction): Boolean {
        val returnType = function.typeReference?.text?.replace(" ", "").orEmpty()
        if (returnType.isEmpty()) return false
        return FLOW_TYPE_REGEX.containsMatchIn(returnType)
    }

    companion object {
        private val FLOW_TYPE_REGEX = Regex(
            pattern = """(?:^|[.<])(?:Flow|SharedFlow|StateFlow|MutableSharedFlow|MutableStateFlow)(?:<|\?|$)"""
        )
    }
}
