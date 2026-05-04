package net.primal.detekt

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

private const val CUSTOM_RUN_CATCHING_FQN = "net.primal.core.utils.runCatching"

class RequireCustomRunCatching(config: Config = Config.empty) : Rule(config) {

    override val issue = Issue(
        id = "RequireCustomRunCatching",
        severity = Severity.Defect,
        description = "Use net.primal.core.utils.runCatching (cancellation-safe), " +
            "not stdlib kotlin.runCatching which silently swallows CancellationException. " +
            "Add `import net.primal.core.utils.runCatching` to this file.",
        debt = Debt.FIVE_MINS,
    )

    override fun visitKtFile(file: KtFile) {
        super.visitKtFile(file)
        if (file.isCustomRunCatchingDefinitionFile()) return
        if (file.hasCustomRunCatchingImport()) return

        file.collectDescendantsOfType<KtCallExpression> { call ->
            call.calleeExpression?.text == "runCatching"
        }.forEach { call ->
            report(CodeSmell(issue, Entity.from(call), issue.description))
        }
    }

    private fun KtFile.hasCustomRunCatchingImport(): Boolean =
        importDirectives.any {
            it.importedFqName?.asString() == CUSTOM_RUN_CATCHING_FQN
        }

    private fun KtFile.isCustomRunCatchingDefinitionFile(): Boolean =
        packageFqName.asString() == "net.primal.core.utils"
}
