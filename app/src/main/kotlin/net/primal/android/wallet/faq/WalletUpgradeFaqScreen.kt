package net.primal.android.wallet.faq

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.PrimalScaffold
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.QAList
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletUpgradeFaqScreen(onClose: () -> Unit) {
    val questionAndAnswerPairs = listOf(
        Pair(
            stringResource(id = R.string.wallet_upgrade_faq_first_question),
            stringResource(id = R.string.wallet_upgrade_faq_first_answer),
        ),
        Pair(
            stringResource(id = R.string.wallet_upgrade_faq_second_question),
            stringResource(id = R.string.wallet_upgrade_faq_second_answer),
        ),
        Pair(
            stringResource(id = R.string.wallet_upgrade_faq_third_question),
            stringResource(id = R.string.wallet_upgrade_faq_third_answer),
        ),
        Pair(
            stringResource(id = R.string.wallet_upgrade_faq_fourth_question),
            stringResource(id = R.string.wallet_upgrade_faq_fourth_answer),
        ),
        Pair(
            stringResource(id = R.string.wallet_upgrade_faq_fifth_question),
            stringResource(id = R.string.wallet_upgrade_faq_fifth_answer),
        ),
        Pair(
            stringResource(id = R.string.wallet_upgrade_faq_sixth_question),
            stringResource(id = R.string.wallet_upgrade_faq_sixth_answer),
        ),
        Pair(
            stringResource(id = R.string.wallet_upgrade_faq_seventh_question),
            stringResource(id = R.string.wallet_upgrade_faq_seventh_answer),
        ),
        Pair(
            stringResource(id = R.string.wallet_upgrade_faq_eighth_question),
            stringResource(id = R.string.wallet_upgrade_faq_eighth_answer),
        ),
        Pair(
            stringResource(id = R.string.wallet_upgrade_faq_ninth_question),
            stringResource(id = R.string.wallet_upgrade_faq_ninth_answer),
        ),
        Pair(
            stringResource(id = R.string.wallet_upgrade_faq_tenth_question),
            stringResource(id = R.string.wallet_upgrade_faq_tenth_answer),
        ),
    )

    PrimalScaffold(
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.wallet_upgrade_faq_title),
                navigationIcon = PrimalIcons.ArrowBack,
                navigationIconContentDescription = stringResource(id = R.string.accessibility_back_button),
                onNavigationIconClick = onClose,
                showDivider = false,
            )
        },
        content = { paddingValues ->
            QAList(
                questionAndAnswerPairs = questionAndAnswerPairs,
                modifier = Modifier.padding(horizontal = 16.dp),
                contentPadding = paddingValues,
            )
        },
    )
}
