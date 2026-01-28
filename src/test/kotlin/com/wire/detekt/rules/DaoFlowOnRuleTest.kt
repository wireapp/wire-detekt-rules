package com.wire.detekt.rules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.rules.KotlinCoreEnvironmentTest
import io.gitlab.arturbosch.detekt.test.compileAndLintWithContext
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@KotlinCoreEnvironmentTest
class DaoFlowOnRuleTest(private val env: KotlinCoreEnvironment) {

    @Test
    fun `reports when asFlow is used without flowOn in DAOImpl`() {
        val code = """
            class MessageDAOImpl {
                fun observeMessages() {
                    return queries.selectAll()
                        .asFlow()
                        .mapToList()
                }
            }
        """.trimIndent()

        val findings = DaoFlowOnRule(Config.empty).compileAndLintWithContext(env, code)
        assertEquals(1, findings.size)
        assertTrue(findings[0].message.contains("must be followed by .flowOn() as the last operation"))
    }

    @Test
    fun `does not report when flowOn is called as last operation in DAOImpl`() {
        val code = """
            class MessageDAOImpl {
                fun observeMessages() {
                    return queries.selectAll()
                        .asFlow()
                        .mapToList()
                        .flowOn(dispatcher)
                }
            }
        """.trimIndent()

        val findings = DaoFlowOnRule(Config.empty).compileAndLintWithContext(env, code)
        assertTrue(findings.isEmpty())
    }

    @Test
    fun `reports when flowOn is not the last operation in DAOImpl`() {
        val code = """
            class MessageDAOImpl {
                fun observeMessages() {
                    return queries.selectAll()
                        .asFlow()
                        .flowOn(dispatcher)
                        .mapToList()
                }
            }
        """.trimIndent()

        val findings = DaoFlowOnRule(Config.empty).compileAndLintWithContext(env, code)
        assertEquals(1, findings.size)
    }

    @Test
    fun `does not report for non-DAOImpl classes`() {
        val code = """
            class MessageRepository {
                fun observeMessages() {
                    return queries.selectAll()
                        .asFlow()
                        .mapToList()
                }
            }
        """.trimIndent()

        val findings = DaoFlowOnRule(Config.empty).compileAndLintWithContext(env, code)
        assertTrue(findings.isEmpty())
    }

    @Test
    fun `does not report when asFlow is not used`() {
        val code = """
            class MessageDAOImpl {
                fun getMessages() {
                    return queries.selectAll()
                        .executeAsList()
                }
            }
        """.trimIndent()

        val findings = DaoFlowOnRule(Config.empty).compileAndLintWithContext(env, code)
        assertTrue(findings.isEmpty())
    }

    @Test
    fun `reports when asFlow is used without flowOn in DAO class`() {
        val code = """
            class MessageDAO {
                fun observeMessages() {
                    return queries.selectAll()
                        .asFlow()
                        .mapToList()
                }
            }
        """.trimIndent()

        val findings = DaoFlowOnRule(Config.empty).compileAndLintWithContext(env, code)
        assertEquals(1, findings.size)
        assertTrue(findings[0].message.contains("must be followed by .flowOn() as the last operation"))
    }

    @Test
    fun `reports when asFlow is used without flowOn in Dao class`() {
        val code = """
            class MessageDao {
                fun observeMessages() {
                    return queries.selectAll()
                        .asFlow()
                        .mapToList()
                }
            }
        """.trimIndent()

        val findings = DaoFlowOnRule(Config.empty).compileAndLintWithContext(env, code)
        assertEquals(1, findings.size)
        assertTrue(findings[0].message.contains("must be followed by .flowOn() as the last operation"))
    }

    @Test
    fun `reports when asFlow is used without flowOn in DaoImpl class`() {
        val code = """
            class MessageDaoImpl {
                fun observeMessages() {
                    return queries.selectAll()
                        .asFlow()
                        .mapToList()
                }
            }
        """.trimIndent()

        val findings = DaoFlowOnRule(Config.empty).compileAndLintWithContext(env, code)
        assertEquals(1, findings.size)
        assertTrue(findings[0].message.contains("must be followed by .flowOn() as the last operation"))
    }

    @Test
    fun `does not report for class with DAO in middle of name`() {
        val code = """
            class MessageDAOWrapper {
                fun observeMessages() {
                    return queries.selectAll()
                        .asFlow()
                        .mapToList()
                }
            }
        """.trimIndent()

        val findings = DaoFlowOnRule(Config.empty).compileAndLintWithContext(env, code)
        assertTrue(findings.isEmpty())
    }
}
