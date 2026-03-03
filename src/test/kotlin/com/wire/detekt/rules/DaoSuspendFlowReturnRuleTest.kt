package com.wire.detekt.rules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.rules.KotlinCoreEnvironmentTest
import io.gitlab.arturbosch.detekt.test.compileAndLintWithContext
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@KotlinCoreEnvironmentTest
class DaoSuspendFlowReturnRuleTest(private val env: KotlinCoreEnvironment) {

    @Test
    fun `reports when suspend function returns Flow in DAO class`() {
        val code = """
            class MessageDAOImpl {
                suspend fun observeMessages(): Flow<List<String>> = TODO()
            }
        """.trimIndent()

        val findings = DaoSuspendFlowReturnRule(Config.empty).compileAndLintWithContext(env, code)

        assertEquals(1, findings.size)
        assertTrue(findings[0].message.contains("must not be declared as suspend"))
    }

    @Test
    fun `does not report when non-suspend function returns Flow in DAO class`() {
        val code = """
            class MessageDAOImpl {
                fun observeMessages(): Flow<List<String>> = TODO()
            }
        """.trimIndent()

        val findings = DaoSuspendFlowReturnRule(Config.empty).compileAndLintWithContext(env, code)

        assertTrue(findings.isEmpty())
    }

    @Test
    fun `does not report when suspend function does not return Flow in DAO class`() {
        val code = """
            class MessageDAOImpl {
                suspend fun observeMessages(): List<String> = TODO()
            }
        """.trimIndent()

        val findings = DaoSuspendFlowReturnRule(Config.empty).compileAndLintWithContext(env, code)

        assertTrue(findings.isEmpty())
    }

    @Test
    fun `does not report when suspend function returns Flow in non-DAO class`() {
        val code = """
            class MessageRepository {
                suspend fun observeMessages(): Flow<List<String>> = TODO()
            }
        """.trimIndent()

        val findings = DaoSuspendFlowReturnRule(Config.empty).compileAndLintWithContext(env, code)

        assertTrue(findings.isEmpty())
    }

    @Test
    fun `reports when suspend function returns StateFlow in DAO class`() {
        val code = """
            class MessageDao {
                suspend fun observeState(): StateFlow<String?> = TODO()
            }
        """.trimIndent()

        val findings = DaoSuspendFlowReturnRule(Config.empty).compileAndLintWithContext(env, code)

        assertEquals(1, findings.size)
    }
}
