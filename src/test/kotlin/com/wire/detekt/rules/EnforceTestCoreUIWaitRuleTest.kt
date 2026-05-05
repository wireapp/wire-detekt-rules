package com.wire.detekt.rules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.rules.KotlinCoreEnvironmentTest
import io.gitlab.arturbosch.detekt.test.compileAndLintWithContext
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@KotlinCoreEnvironmentTest
class EnforceTestCoreUIWaitRuleTest(private val env: KotlinCoreEnvironment) {

    // region Thread.sleep

    @Test
    fun `reports Thread sleep in test core package class`() {
        val code = """
            package com.wire.android.tests.core

            class LoginScreenSteps {
                fun waitForLoginScreen() {
                    Thread.sleep(1000)
                }
            }
        """.trimIndent()

        val findings = EnforceTestCoreUIWaitRule(Config.empty).compileAndLintWithContext(env, code)

        assertEquals(1, findings.size)
        assertTrue(findings[0].message.contains("Thread.sleep"))
    }

    @Test
    fun `reports Thread sleep in class annotated with Test`() {
        val code = """
            annotation class Test

            @Test
            class LoginScreenSteps {
                fun waitForLoginScreen() {
                    Thread.sleep(500)
                }
            }
        """.trimIndent()

        val findings = EnforceTestCoreUIWaitRule(Config.empty).compileAndLintWithContext(env, code)

        assertEquals(1, findings.size)
        assertTrue(findings[0].message.contains("Thread.sleep"))
    }

    @Test
    fun `does not report Thread sleep in non-test-core class`() {
        val code = """
            package com.wire.android.feature.login

            class LoginViewModel {
                fun someMethod() {
                    Thread.sleep(500)
                }
            }
        """.trimIndent()

        val findings = EnforceTestCoreUIWaitRule(Config.empty).compileAndLintWithContext(env, code)

        assertTrue(findings.isEmpty())
    }

    // endregion

    // region SystemClock.sleep

    @Test
    fun `reports SystemClock sleep in test core package class`() {
        val code = """
            package com.wire.android.tests.core

            class HomeScreenSteps {
                fun waitForAnimation() {
                    SystemClock.sleep(300)
                }
            }
        """.trimIndent()

        val findings = EnforceTestCoreUIWaitRule(Config.empty).compileAndLintWithContext(env, code)

        assertEquals(1, findings.size)
        assertTrue(findings[0].message.contains("SystemClock.sleep"))
    }

    @Test
    fun `does not report SystemClock sleep outside test core package`() {
        val code = """
            package com.wire.android.ui.home

            class HomeScreen {
                fun animateSomething() {
                    SystemClock.sleep(300)
                }
            }
        """.trimIndent()

        val findings = EnforceTestCoreUIWaitRule(Config.empty).compileAndLintWithContext(env, code)

        assertTrue(findings.isEmpty())
    }

    // endregion

    // region waitForExists

    @Test
    fun `reports waitForExists in test core package class`() {
        val code = """
            package com.wire.android.tests.core

            class ConversationScreenSteps {
                fun waitForElement() {
                    uiObject.waitForExists(5000)
                }
            }
        """.trimIndent()

        val findings = EnforceTestCoreUIWaitRule(Config.empty).compileAndLintWithContext(env, code)

        assertEquals(1, findings.size)
        assertTrue(findings[0].message.contains("waitForExists"))
    }

    @Test
    fun `does not report waitForExists outside test core package`() {
        val code = """
            package com.wire.android.ui

            class SomeUiHelper {
                fun checkElement() {
                    uiObject.waitForExists(5000)
                }
            }
        """.trimIndent()

        val findings = EnforceTestCoreUIWaitRule(Config.empty).compileAndLintWithContext(env, code)

        assertTrue(findings.isEmpty())
    }

    // endregion

    // region UiWaitUtils.WaitUtils.waitFor

    @Test
    fun `reports UiWaitUtils WaitUtils waitFor in test core package class`() {
        val code = """
            package com.wire.android.tests.core

            class SearchScreenSteps {
                fun waitForResults() {
                    UiWaitUtils.WaitUtils.waitFor(5000) { resultsVisible() }
                }
            }
        """.trimIndent()

        val findings = EnforceTestCoreUIWaitRule(Config.empty).compileAndLintWithContext(env, code)

        assertEquals(1, findings.size)
        assertTrue(findings[0].message.contains("WaitUtils.waitFor"))
    }

    @Test
    fun `does not report UiWaitUtils waitFor called directly (correct usage)`() {
        val code = """
            package com.wire.android.tests.core

            class SearchScreenSteps {
                fun waitForResults() {
                    UiWaitUtils.waitFor(5000) { resultsVisible() }
                }
            }
        """.trimIndent()

        val findings = EnforceTestCoreUIWaitRule(Config.empty).compileAndLintWithContext(env, code)

        assertTrue(findings.isEmpty())
    }

    // endregion

    // region import directive

    @Test
    fun `reports import of nested WaitUtils waitFor in test core file`() {
        val code = """
            package com.wire.android.tests.core

            import uiautomatorutils.UiWaitUtils.WaitUtils.waitFor

            class LoginScreenSteps {
                fun waitForLoginScreen() {
                    waitFor(5000) { loginVisible() }
                }
            }
        """.trimIndent()

        val findings = EnforceTestCoreUIWaitRule(Config.empty).compileAndLintWithContext(env, code)

        assertEquals(1, findings.size)
        assertTrue(findings[0].message.contains("WaitUtils"))
    }

    @Test
    fun `does not report WaitUtils import outside test core package`() {
        val code = """
            package com.wire.android.ui

            import uiautomatorutils.UiWaitUtils.WaitUtils.waitFor

            class SomeHelper {
                fun doSomething() {
                    waitFor(5000) { visible() }
                }
            }
        """.trimIndent()

        val findings = EnforceTestCoreUIWaitRule(Config.empty).compileAndLintWithContext(env, code)

        assertTrue(findings.isEmpty())
    }

    // endregion

    // region multiple violations

    @Test
    fun `reports all violations independently when multiple are present in the same class`() {
        val code = """
            package com.wire.android.tests.core

            class LoginScreenSteps {
                fun slowWait() {
                    Thread.sleep(500)
                }
                fun anotherSlowWait() {
                    SystemClock.sleep(1000)
                }
                fun waitForElement() {
                    uiObject.waitForExists(3000)
                }
            }
        """.trimIndent()

        val findings = EnforceTestCoreUIWaitRule(Config.empty).compileAndLintWithContext(env, code)

        assertEquals(3, findings.size)
    }

    // endregion

    // region no violations

    @Test
    fun `does not report when UiWaitUtils is used correctly`() {
        val code = """
            package com.wire.android.tests.core

            class LoginScreenSteps {
                fun waitForLoginScreen() {
                    UiWaitUtils.waitFor(5000) { loginVisible() }
                    UiWaitUtils.waitForMillis(500)
                    UiWaitUtils.waitUntilVisibleOrThrow(element)
                }
            }
        """.trimIndent()

        val findings = EnforceTestCoreUIWaitRule(Config.empty).compileAndLintWithContext(env, code)

        assertTrue(findings.isEmpty())
    }

    // endregion
}
