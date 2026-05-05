package com.wire.detekt.rules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import io.gitlab.arturbosch.detekt.rules.hasAnnotation
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtImportDirective

/**
 * This rule checks for direct usage of Thread.sleep, SystemClock.sleep, waitForExists, and the nested
 * WaitUtils.waitFor in test core code. It encourages using UiWaitUtils retry/wait helpers instead
 * for better reliability and maintainability.
 */
class EnforceTestCoreUIWaitRule(config: Config = Config.empty) : Rule(config) {
    override val issue: Issue = Issue(
        id = javaClass.simpleName,
        severity = Severity.Maintainability,
        description = "Tests core code should not use Thread.sleep or SystemClock.sleep directly. " +
                "Use UiWaitUtils instead for better reliability and maintainability.",
        debt = Debt.TEN_MINS
    )

    private var isInTestCore = false

    override fun visitClassOrObject(classOrObject: KtClassOrObject) {
        val previous = isInTestCore
        isInTestCore = classOrObject.isInTestCoreScope()
        super.visitClassOrObject(classOrObject)
        isInTestCore = previous
    }

    override fun visitImportDirective(importDirective: KtImportDirective) {
        val filePackage = importDirective.containingKtFile.packageFqName.asString()
        if (filePackage.contains("com.wire.android.tests.core")) {
            val importPath = importDirective.importedFqName?.asString().orEmpty()
            if (importPath.contains("UiWaitUtils.WaitUtils.waitFor")) {
                report(CodeSmell(
                    issue,
                    Entity.from(importDirective),
                    "Import UiWaitUtils and call UiWaitUtils.waitFor(...) instead of importing the nested WaitUtils class"
                ))
            }
        }
        super.visitImportDirective(importDirective)
    }

    override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
        if (isInTestCore) {
            val receiver = expression.receiverExpression.text
            val selector = expression.selectorExpression
            if (selector is KtCallExpression) {
                val callName = selector.calleeExpression?.text
                when {
                    callName == "sleep" && receiver == "Thread" ->
                        report(expression, "Use UiWaitUtils.waitFor(...) or UiWaitUtils.waitForMillis(...) instead of Thread.sleep(...)")

                    callName == "sleep" && receiver == "SystemClock" ->
                        report(expression, "Use UiWaitUtils retry/wait helpers instead of direct sleeps like SystemClock.sleep(...)")

                    callName == "waitForExists" ->
                        report(expression, "Use UiWaitUtils.waitUntilVisibleOrThrow(...) or waitUntilGoneOrThrow(...) instead of waitForExists(...)")

                    callName == "waitFor" && receiver.endsWith(".WaitUtils") ->
                        report(expression, "Use UiWaitUtils.waitFor(...) instead of UiWaitUtils.WaitUtils.waitFor(...)")
                }
            }
        }
        super.visitDotQualifiedExpression(expression)
    }

    private fun KtClassOrObject.isInTestCoreScope(): Boolean {
        val packageName = containingKtFile.packageFqName.asString()
        return hasAnnotation("Test") ||
                packageName.contains("com.wire.android.tests.core")
    }
}
