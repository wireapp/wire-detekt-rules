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
        assertTrue(findings[0].message.contains("must be followed by"))
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
        assertTrue(findings[0].message.contains("must be followed by"))
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
        assertTrue(findings[0].message.contains("must be followed by"))
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
        assertTrue(findings[0].message.contains("must be followed by"))
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

    @Test
    fun `does not report when flowOn is last inside lambda passed to cache get`() {
        val code = """
            class MemberDAOImpl {
                suspend fun observeConversationMembers(qualifiedID: String): Flow<List<String>> = 
                    membersCache.get(qualifiedID) {
                        memberQueries.selectAllMembersByConversation(qualifiedID)
                            .asFlow()
                            .mapToList()
                            .map { it.map { item -> item.toString() } }
                            .flowOn(readDispatcher.value)
                    }
            }
        """.trimIndent()

        val findings = DaoFlowOnRule(Config.empty).compileAndLintWithContext(env, code)
        assertTrue(findings.isEmpty(), "Expected no findings but got: $findings")
    }

    @Test
    fun `reports when asFlow in lambda is missing flowOn`() {
        val code = """
            class MemberDAOImpl {
                suspend fun observeConversationMembers(qualifiedID: String): Flow<List<String>> = 
                    membersCache.get(qualifiedID) {
                        memberQueries.selectAllMembersByConversation(qualifiedID)
                            .asFlow()
                            .mapToList()
                            .map { it.map { item -> item.toString() } }
                    }
            }
        """.trimIndent()

        val findings = DaoFlowOnRule(Config.empty).compileAndLintWithContext(env, code)
        assertEquals(1, findings.size)
    }

    @Test
    fun `does not report when asFlow chain ends with shareIn`() {
        val code = """
            class MetadataDAOImpl {
                fun observeSerializable(key: String): Flow<String?> {
                    return metadataQueries.selectValueByKey(key)
                        .asFlow()
                        .mapToOneOrNull()
                        .map { jsonString -> jsonString?.uppercase() }
                        .distinctUntilChanged()
                        .shareIn(databaseScope, SharingStarted.Lazily, 1)
                }
            }
        """.trimIndent()

        val findings = DaoFlowOnRule(Config.empty).compileAndLintWithContext(env, code)
        assertTrue(findings.isEmpty(), "Expected no findings for shareIn but got: $findings")
    }

    @Test
    fun `does not report when asFlow chain ends with stateIn`() {
        val code = """
            class MetadataDAOImpl {
                fun observeValue(key: String): StateFlow<String?> {
                    return metadataQueries.selectValueByKey(key)
                        .asFlow()
                        .mapToOneOrNull()
                        .stateIn(databaseScope, SharingStarted.Lazily, null)
                }
            }
        """.trimIndent()

        val findings = DaoFlowOnRule(Config.empty).compileAndLintWithContext(env, code)
        assertTrue(findings.isEmpty(), "Expected no findings for stateIn but got: $findings")
    }
}
