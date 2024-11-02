package net.primal.android.premium.info.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun WhyPremiumTabContent(modifier: Modifier = Modifier) {
    QAColumn(
        modifier = modifier,
        question = "Why Get Primal Premium?",
        answer = """
            Become a Nostr power user and help shape the future! Open protocols like Nostr give us the opportunity to regain control over our online lives. 
            
            At Primal, we don’t rely on advertising. We don’t monetize user data. Our users are our customers. Our sole focus is to make the best possible product for our users. We open source all our work to help the Nostr ecosystem flourish. By signing up for Primal Premium, you are enabling us to continue building for Nostr. 
            
            Be the change you want to see in the world. If you don’t want to be the product, consider being the customer. 
            """.trimIndent(),
    )
}

