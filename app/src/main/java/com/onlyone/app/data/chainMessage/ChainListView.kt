package com.onlyone.app.data.chainMessage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.onlyone.app.R
import com.onlyone.app.composables.CustomColorOverlay
import com.onlyone.app.theme.ThemeTokens

@Composable
fun ChainListView(
    theme: ThemeTokens,
    chains: List<Chain>,
    myUid: String,
    onOpen: (String) -> Unit
) {
    CustomColorOverlay(
        modifier = Modifier.fillMaxWidth(),
        theme = theme,
        onDismiss = {}
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.friends_header), // or create "chain_list_header"
                style = MaterialTheme.typography.h6,
                color = theme.textColor
            )

            if (chains.isEmpty()) {
                Text(
                    text = stringResource(R.string.friends_empty_outgoing), // or create "chain_list_empty"
                    color = theme.textColor.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.body2
                )
            } else {
                chains.forEach { chain ->
                    ChainListItem(
                        theme = theme,
                        chain = chain,
                        myUid = myUid
                    ) { onOpen(chain.id) }
                }
            }
        }
    }
}

@Composable
private fun ChainListItem(
    theme: ThemeTokens,
    chain: Chain,
    myUid: String,
    onClick: () -> Unit
) {
    CustomColorOverlay(
        modifier = Modifier.fillMaxWidth(),
        theme = theme,
        onDismiss = {}
    ) {
        val progress =
            if (chain.targetLength <= 0) 0f
            else (chain.lastStepIndex + 1).coerceAtLeast(0) / chain.targetLength.toFloat()

        val stateText = when (chain.state) {
            "complete" -> stringResource(R.string.chain_header_complete)
            "expired"  -> stringResource(R.string.chain_header_expired)
            else       -> if (chain.currentAssignee == myUid)
                stringResource(R.string.chain_header_your_turn)
            else stringResource(R.string.chain_header_waiting)
        }

        val title = chain.title ?: stringResource(R.string.screen_title_create_chain)

        Surface(
            shape = MaterialTheme.shapes.medium,
            color = Color.Transparent,
            elevation = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
                .clickableNoRipple(onClick)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // Title + state
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(title, color = theme.textColor, style = MaterialTheme.typography.subtitle1)
                    Text(stateText, color = theme.textColor.copy(alpha = 0.8f), style = MaterialTheme.typography.caption)
                }

                // Progress bar
                LinearProgressIndicator(
                    progress = progress.coerceIn(0f, 1f),
                    color = theme.textColor,
                    backgroundColor = theme.textColor.copy(alpha = 0.2f),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/** Simple no-ripple clickable modifier */
fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier = composed {
    val interaction = remember { MutableInteractionSource() }
    this
        .padding(vertical = 6.dp)
        .clickable(
            interactionSource = interaction,
            indication = null
        ) { onClick() }
}
