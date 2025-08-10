package com.example.onlyone.views.shopView

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.onlyone.R
import com.example.onlyone.composables.AvatarCatalog
import com.example.onlyone.composables.CustomColorOverlay
import com.example.onlyone.data.UserComposite

@Composable
fun AvatarsSection(
    user: UserComposite?,
    onBuyAvatar: (Int) -> Unit,
    onSelectAvatar: (Int) -> Unit
) {
    val avatarItems = AvatarCatalog.avatars

    Text(
        text = stringResource(R.string.common_avatars),
        style = MaterialTheme.typography.h6,
        color = Color.White,
        modifier = Modifier.padding(start = 6.dp, bottom = 4.dp)
    )

    CustomColorOverlay(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(percent = 21),
        overlayColor = Color.Gray,
        onDismiss = {},
        paddingBox1 = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
        paddingBox2 = PaddingValues(6.dp)
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(avatarItems) { avatar ->
                val alreadyOwned = user?.ownedAvatars?.contains(avatar.id) == true
                val isSelected = avatar.id == user?.avatarId
                val canAfford = (user?.gold ?: 0) >= avatar.cost

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = avatar.imageRes),
                        contentDescription = stringResource(R.string.common_avatar),
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .border(
                                width = 3.dp,
                                color = if (isSelected) Color(0xFF4CAF50) else Color.White.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    when {
                        !alreadyOwned -> {
                            Button(
                                onClick = { onBuyAvatar(avatar.id) },
                                enabled = canAfford
                            ) {
                                Text(stringResource(R.string.common_buy_with_cost, avatar.cost))
                            }
                        }
                        alreadyOwned && !isSelected -> {
                            Button(onClick = { onSelectAvatar(avatar.id) }) {
                                Text(stringResource(R.string.common_select))
                            }
                        }
                        isSelected -> {
                            Button(onClick = {}, enabled = false) {
                                Text(stringResource(R.string.common_selected))
                            }
                        }
                    }
                }
            }
        }
    }
}

