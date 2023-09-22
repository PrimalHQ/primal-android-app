package net.primal.android.auth.create.compose.steps

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import net.primal.android.R
import net.primal.android.auth.create.CreateContract
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.core.compose.button.PrimalOutlinedButton
import net.primal.android.core.utils.userNameUiFriendly

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FollowRecommendedAccountsStep(
    state: CreateContract.UiState, eventPublisher: (CreateContract.UiEvent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        if (state.loading && state.recommendedFollows.isEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    PrimalLoadingSpinner()
                }
            }
        } else {
            val follows = state.recommendedFollows.groupBy { it.groupName }
            follows.forEach { group ->
                stickyHeader {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF181818), Color(0xFF222222)
                                    )
                                )
                            )
                            .border(
                                width = 1.dp,
                                color = Color(0xFF222222),
                                shape = RoundedCornerShape(8.dp),
                            )
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(group.key)
                        PrimalOutlinedButton(onClick = {
                            eventPublisher(
                                CreateContract.UiEvent.ToggleGroupFollowEvent(
                                    groupName = group.key
                                )
                            )
                        }) {
                            val text =
                                if (state.recommendedFollows.filter { it.groupName == group.key }
                                        .any { !it.isCurrentUserFollowing }) stringResource(R.string.create_recommended_follow_all) else stringResource(
                                    id = R.string.create_recommended_unfollow_all
                                )
                            Text(text)
                        }
                    }
                }

                items(group.value) { suggestion ->
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 32.dp, vertical = 12.dp)
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(Color.Black),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val model = ImageRequest.Builder(LocalContext.current)
                            .data(suggestion.content.picture).build()
                        AsyncImage(
                            model = model,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .height(48.dp)
                                .width(48.dp)
                                .clip(CircleShape)
                        )
                        Column(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .fillMaxWidth(0.6f),
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = suggestion.content.userNameUiFriendly(suggestion.pubkey),
                                fontWeight = FontWeight.W700,
                                fontSize = 14.sp,
                                lineHeight = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = Color.White
                            )
                            Text(
                                text = suggestion.content.nip05 ?: "",
                                fontWeight = FontWeight.W400,
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = Color(0xFF666666)
                            )
                        }
                        PrimalOutlinedButton(
                            onClick = {
                                eventPublisher(
                                    CreateContract.UiEvent.ToggleFollowEvent(
                                        groupName = group.key, pubkey = suggestion.pubkey
                                    )
                                )
                            }, modifier = Modifier
                                .defaultMinSize(minWidth = 92.dp)
                                .height(36.dp)
                        ) {
                            val text =
                                if (state.recommendedFollows.first { it.pubkey == suggestion.pubkey && it.groupName == suggestion.groupName }.isCurrentUserFollowing) stringResource(
                                    id = R.string.create_recommended_unfollow
                                ) else stringResource(id = R.string.create_recommended_follow)
                            Text(text)
                        }
                    }
                }
            }
        }
    }
}