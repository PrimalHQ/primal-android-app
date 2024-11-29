package net.primal.android.premium.manage.nameChange

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.premium.legend.LegendaryCustomization
import net.primal.android.premium.ui.PremiumPrimalNameTable
import net.primal.android.theme.AppTheme

@Composable
fun ConfirmNameChangeStage(
    modifier: Modifier = Modifier,
    primalName: String,
    profileAvatarCdnImage: CdnImage?,
    profileLegendaryCustomization: LegendaryCustomization?,
    contentPadding: PaddingValues,
) {
    Column(
        modifier = modifier.padding(contentPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        UniversalAvatarThumbnail(
            avatarCdnImage = profileAvatarCdnImage,
            avatarSize = 80.dp,
            legendaryCustomization = profileLegendaryCustomization,
        )
        NostrUserText(
            displayName = primalName,
            internetIdentifier = "$primalName@primal.net",
            internetIdentifierBadgeSize = 24.dp,
            fontSize = 20.sp,
            customBadgeStyle = if (profileLegendaryCustomization?.customBadge == true) {
                profileLegendaryCustomization.legendaryStyle
            } else {
                null
            },
        )
        Text(
            modifier = Modifier.padding(horizontal = 12.dp),
            text = stringResource(id = R.string.premium_purchase_primal_name_available),
            style = AppTheme.typography.bodyLarge,
            color = AppTheme.extraColorScheme.successBright,
        )
        PremiumPrimalNameTable(
            primalName = primalName,
        )
    }
}
