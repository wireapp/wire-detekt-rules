package com.wire.detekt

import com.wire.detekt.rules.DocumentedPublicUseCases
import com.wire.detekt.rules.EnforceSerializableFields
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

/**
 * Wire's [RuleSetProvider] can be extended to add more rules to this [RuleSet]
 */
class WireRuleSetProvider : RuleSetProvider {

    override val ruleSetId: RuleSet.Id = RuleSet.Id("WireRuleSet")
    override fun instance() = RuleSet(
        id = ruleSetId,
        rules = listOf(
            { config -> EnforceSerializableFields(config) },
            { config -> DocumentedPublicUseCases(config) },
        )
    )
}
