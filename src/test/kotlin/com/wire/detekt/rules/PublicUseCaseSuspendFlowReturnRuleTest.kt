package com.wire.detekt.rules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.rules.KotlinCoreEnvironmentTest
import io.gitlab.arturbosch.detekt.test.compileAndLintWithContext
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@KotlinCoreEnvironmentTest
class PublicUseCaseSuspendFlowReturnRuleTest(private val env: KotlinCoreEnvironment) {

    @Test
    fun `reports when suspend function returns Flow in public UseCase class`() {
        val code = """
            package com.wire.kalium.logic.feature.messaging

            class ObserveMessagesUseCase {
                suspend operator fun invoke(): Flow<List<String>> = TODO()
            }
        """.trimIndent()

        val findings = PublicUseCaseSuspendFlowReturnRule(Config.empty).compileAndLintWithContext(env, code)

        assertEquals(1, findings.size)
        assertTrue(findings[0].message.contains("must not be declared as suspend"))
    }

    @Test
    fun `does not report when non-suspend function returns Flow in public UseCase class`() {
        val code = """
            package com.wire.kalium.logic.feature.messaging

            class ObserveMessagesUseCase {
                operator fun invoke(): Flow<List<String>> = TODO()
            }
        """.trimIndent()

        val findings = PublicUseCaseSuspendFlowReturnRule(Config.empty).compileAndLintWithContext(env, code)

        assertTrue(findings.isEmpty())
    }

    @Test
    fun `does not report when suspend function returns non-Flow type in public UseCase class`() {
        val code = """
            package com.wire.kalium.logic.feature.messaging

            class ObserveMessagesUseCase {
                suspend operator fun invoke(): List<String> = TODO()
            }
        """.trimIndent()

        val findings = PublicUseCaseSuspendFlowReturnRule(Config.empty).compileAndLintWithContext(env, code)

        assertTrue(findings.isEmpty())
    }

    @Test
    fun `does not report when class is not in logic feature package`() {
        val code = """
            package com.wire.kalium.data.messaging

            class ObserveMessagesUseCase {
                suspend operator fun invoke(): Flow<List<String>> = TODO()
            }
        """.trimIndent()

        val findings = PublicUseCaseSuspendFlowReturnRule(Config.empty).compileAndLintWithContext(env, code)

        assertTrue(findings.isEmpty())
    }

    @Test
    fun `does not report when use case class is not public`() {
        val code = """
            package com.wire.kalium.logic.feature.messaging

            internal class ObserveMessagesUseCase {
                suspend operator fun invoke(): Flow<List<String>> = TODO()
            }
        """.trimIndent()

        val findings = PublicUseCaseSuspendFlowReturnRule(Config.empty).compileAndLintWithContext(env, code)

        assertTrue(findings.isEmpty())
    }
}
