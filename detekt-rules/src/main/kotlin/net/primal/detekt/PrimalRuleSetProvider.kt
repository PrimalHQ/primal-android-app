package net.primal.detekt

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

class PrimalRuleSetProvider : RuleSetProvider {

    override val ruleSetId: String = "PrimalRuleSet"

    override fun instance(config: Config): RuleSet =
        RuleSet(
            id = ruleSetId,
            rules = listOf(
                RequireCustomRunCatching(config),
                RequireCustomResult(config),
            ),
        )
}
