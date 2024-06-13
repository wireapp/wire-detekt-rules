package com.wire.detekt.rules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.psiUtil.isPublic

class DocumentedPublicUseCases(config: Config = Config.empty) : Rule(
    config,
    description = "Public Use Cases should have documentation for the clients"
) {

    override fun visitClassOrObject(kClass: KtClassOrObject) {
        if (isClassAPublicUseCase(kClass)) {
            if (!hasDocumentation(kClass)) {
                report(kClass, "The class '${kClass.name}' is a Public Use Case and doesn't provide documentation.")
            }
        }
    }

    private fun hasDocumentation(kClass: KtClassOrObject) =
        kClass.docComment?.getAllSections()?.firstOrNull()?.getContent() != null ||
                kClass.body?.functions?.firstOrNull { it.name == "invoke" }?.docComment?.getAllSections()?.firstOrNull()
                    ?.getContent() != null

    private fun isClassAPublicUseCase(kClass: KtClassOrObject) =
        kClass.fqName?.asString().orEmpty().contains("com.wire.kalium.logic.feature") &&
                kClass.fqName?.shortName()?.asString().orEmpty().endsWith("UseCase", ignoreCase = true) &&
                kClass.isPublic

}
