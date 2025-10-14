package com.onlyone.app.views.chainMessage

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.onlyone.app.R
import com.onlyone.app.composables.CustomColorOverlay
import com.onlyone.app.composables.FriendDropdown
import com.onlyone.app.data.chainMessage.Chain
import com.onlyone.app.data.chainMessage.ChainStep
import com.onlyone.app.theme.ThemeTokens
import com.onlyone.app.viewModels.userViewModel.UserViewModel

@Composable
fun ChainDetailView(
    theme: ThemeTokens,
    userViewModel: UserViewModel,
    chain: Chain,
    steps: List<ChainStep>,
    myUid: String,
    onSend: (text: String, nextAssignee: String?) -> Unit,
    onReroute: (newAssignee: String) -> Unit = {},
    onSaveLocal: () -> Unit = {}      // <-- we'll expose a Save action always
) {
    val isOpen = chain.state == "open"
    val isAssignee = chain.currentAssignee == myUid
    val isLastTurn = chain.lastStepIndex + 1 == chain.targetLength - 1

    // friends (allow repeats; exclude only self)
    val localFriends by userViewModel.observeLocalFriends().collectAsState(initial = emptyList())
    val candidateFriends = remember(localFriends, myUid) {
        localFriends.filter { f -> f.uid != myUid }
    }
    val uidToName = remember(candidateFriends) { candidateFriends.associate { f -> f.uid to (f.username ?: f.uid) } }
    val nameToUid = remember(candidateFriends) { candidateFriends.associate { f -> (f.username ?: f.uid) to f.uid } }
    val displayNames = remember(candidateFriends) { candidateFriends.map { it.username ?: it.uid } }

    // Reroute permission & timing (5 min grace)
    val canHoldReroute = (myUid == chain.ownerUid || myUid == chain.lastContributorUid)
    val lastAssignAtMs = remember(chain.lastAssignAt) { chain.lastAssignAt?.toDate()?.time ?: 0L }
    val elapsedMin = remember(lastAssignAtMs, chain.state) {
        if (lastAssignAtMs == 0L) 0L else ((System.currentTimeMillis() - lastAssignAtMs) / 60000L)
    }
    val rerouteGraceMin = 5L
    val canRerouteNow = isOpen && !isAssignee && canHoldReroute && elapsedMin >= rerouteGraceMin

    CustomColorOverlay(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp),
        theme = theme,
        onDismiss = {}
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Text(
                text = when {
                    !isOpen && chain.state == "complete" -> stringResource(R.string.chain_header_complete)
                    !isOpen && chain.state == "expired"  -> stringResource(R.string.chain_header_expired)
                    isAssignee && isLastTurn             -> stringResource(R.string.chain_header_finish)
                    isAssignee                            -> stringResource(R.string.chain_header_your_turn)
                    else                                  -> stringResource(R.string.chain_header_waiting)
                },
                style = MaterialTheme.typography.h6,
                color = theme.textColor
            )

            // Title (if present)
            chain.title?.takeIf { it.isNotBlank() }?.let { t ->
                Text(
                    text = t,
                    style = MaterialTheme.typography.subtitle1,
                    color = theme.textColor
                )
            }

            // Progress (based on actual steps)
            LinearProgressIndicator(
                progress = (steps.size.toFloat() / chain.targetLength.coerceAtLeast(1)),
                color = theme.textColor,
                backgroundColor = theme.textColor.copy(alpha = 0.2f),
                modifier = Modifier.fillMaxWidth()
            )

            // Steps
            steps.forEach { s ->
                val who = s.authorName?.takeIf { it.isNotBlank() } ?: s.authorUid
                Text("• $who: ${s.text}", color = theme.textColor, style = MaterialTheme.typography.body2)
            }

            Spacer(Modifier.height(8.dp))

            // Contributor UI
            if (isOpen && isAssignee) {
                var text by remember { mutableStateOf(TextFieldValue("")) }
                val maxChars = 100

                OutlinedTextField(
                    value = text,
                    onValueChange = { nv ->
                        text = if (nv.text.length <= maxChars) nv else nv.copy(text = nv.text.take(maxChars))
                    },
                    label = { Text(stringResource(R.string.chain_seed_label), color = theme.textColor) },
                    minLines = 3,
                    maxLines = 6,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = theme.textColor,
                        cursorColor = theme.textColor,
                        focusedBorderColor = theme.textColor,
                        unfocusedBorderColor = theme.textColor.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    val countColor =
                        if (text.text.length >= maxChars) Color.Red else theme.textColor.copy(alpha = 0.7f)
                    Text("${text.text.length}/$maxChars", color = countColor, style = MaterialTheme.typography.caption)
                }

                var nextAssignee by remember { mutableStateOf("") }
                if (!isLastTurn) {
                    FriendDropdown(
                        theme = theme,
                        labelText = stringResource(R.string.chain_select_friend_label),
                        currentUid = nextAssignee,
                        displayNames = displayNames,
                        uidToName = uidToName,
                        nameToUid = nameToUid,
                        enabled = candidateFriends.isNotEmpty(),
                        keyReset = "detail_friend_picker_${candidateFriends.size}",
                        onFriendSelected = { uid -> nextAssignee = uid }
                    )
                }

                Button(
                    onClick = { onSend(text.text.trim(), if (isLastTurn) null else nextAssignee) },
                    enabled = text.text.isNotBlank() && (isLastTurn || nextAssignee.isNotBlank()),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        if (isLastTurn) stringResource(R.string.chain_finish_button)
                        else stringResource(R.string.chain_add_and_pass_button),
                        color = theme.textColor
                    )
                }
            }

            // Reroute UI (owner or last contributor; not current assignee; chain OPEN)
            if (isOpen && !isAssignee && canHoldReroute) {
                Divider(color = theme.textColor.copy(alpha = 0.25f))
                Text(
                    text = stringResource(R.string.chain_reroute_header),
                    style = MaterialTheme.typography.subtitle1,
                    color = theme.textColor
                )

                var rerouteUid by remember { mutableStateOf("") }

                FriendDropdown(
                    theme = theme,
                    labelText = stringResource(R.string.chain_reroute_pick),
                    currentUid = rerouteUid,
                    displayNames = displayNames,
                    uidToName = uidToName,
                    nameToUid = nameToUid,
                    enabled = candidateFriends.isNotEmpty(),
                    keyReset = "reroute_friend_picker_${candidateFriends.size}",
                    onFriendSelected = { uid -> rerouteUid = uid }
                )

                val waitLeft = (rerouteGraceMin - elapsedMin).coerceAtLeast(0L)
                val helper = if (canRerouteNow)
                    stringResource(R.string.chain_reroute_now)
                else
                    stringResource(R.string.chain_reroute_later, waitLeft.toInt())

                Text(helper, color = theme.textColor.copy(alpha = 0.75f), style = MaterialTheme.typography.caption)

                Button(
                    onClick = { onReroute(rerouteUid) },
                    enabled = canRerouteNow && rerouteUid.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text(stringResource(R.string.chain_reroute_button), color = theme.textColor)
                }
            }

            // Show Save / Share only when the chain is complete
            if (!isOpen && chain.state == "complete") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onSaveLocal,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.chain_save_locally), color = theme.textColor)
                    }
                    Button(
                        onClick = { /* TODO: share sheet */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.chain_share), color = theme.textColor)
                    }
                }
            }

        }
    }
}
