package com.wire.detekt.rules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.isPublic

/**
 * In public UseCase classes, Flow-like return types must not be exposed via suspend functions.
 */
class PublicUseCaseSuspendFlowReturnRule(config: Config = Config.empty) : Rule(config) {

    override val issue: Issue = Issue(
        id = javaClass.simpleName,
        severity = Severity.Defect,
        description = "Public UseCase classes must not declare Flow-returning functions as suspend",
        debt = Debt.FIVE_MINS
    )

    private val useCaseContext = ArrayDeque<Pair<String?, Boolean>>()

    override fun visitClassOrObject(classOrObject: KtClassOrObject) {
        val isPublicUseCase = isClassAPublicUseCase(classOrObject)
        useCaseContext.addLast(classOrObject.name to isPublicUseCase)
        super.visitClassOrObject(classOrObject)
        useCaseContext.removeLast()
    }

    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)

        val (className, isPublicUseCase) = useCaseContext.lastOrNull() ?: return
        if (!isPublicUseCase) return
        if (!function.hasModifier(KtTokens.SUSPEND_KEYWORD)) return
        if (!returnsFlowType(function)) return

        report(
            function,
            "In ${className ?: "public UseCase class"}, function '${function.name}' returns a Flow and must not be declared as suspend"
        )
    }

    private fun isClassAPublicUseCase(kClass: KtClassOrObject) =
        kClass.fqName?.asString().orEmpty().contains("com.wire.kalium.logic.feature") &&
                kClass.fqName?.shortName()?.asString().orEmpty().endsWith("UseCase", ignoreCase = true) &&
                kClass.isPublic

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
