package net.primal.detekt

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtUserType
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

private const val CUSTOM_RESULT_FQN = "net.primal.core.utils.Result"

class RequireCustomResult(config: Config = Config.empty) : Rule(config) {

    override val issue = Issue(
        id = "RequireCustomResult",
        severity = Severity.Defect,
        description = "Use net.primal.core.utils.Result, not stdlib kotlin.Result. " +
            "Add `import net.primal.core.utils.Result` to this file.",
        debt = Debt.FIVE_MINS,
    )

    override fun visitKtFile(file: KtFile) {
        super.visitKtFile(file)
        if (file.isCustomResultDefinitionFile()) return
        if (file.hasCustomResultImport()) return

        file.collectDescendantsOfType<KtUserType> { type ->
            type.referencedName == "Result" && type.qualifier == null
        }.forEach { type ->
            report(CodeSmell(issue, Entity.from(type), issue.description))
        }

        file.collectDescendantsOfType<KtDotQualifiedExpression> { expr ->
            expr.receiverExpression.text == "Result" &&
                expr.selectorExpression?.text?.let { it.startsWith("success") || it.startsWith("failure") } == true
        }.forEach { expr ->
            report(CodeSmell(issue, Entity.from(expr), issue.description))
        }
    }

    private fun KtFile.hasCustomResultImport(): Boolean =
        importDirectives.any {
            it.importedFqName?.asString() == CUSTOM_RESULT_FQN
        }

    private fun KtFile.isCustomResultDefinitionFile(): Boolean = packageFqName.asString() == "net.primal.core.utils"
}
