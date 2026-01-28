package com.wire.detekt.rules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtExpression

fun Rule.report(classOrObject: KtClassOrObject, message: String) {
    report(CodeSmell(issue, Entity.atName(classOrObject), message))
}

fun Rule.report(expression: KtExpression, message: String) {
    report(CodeSmell(issue, Entity.from(expression), message))
}
