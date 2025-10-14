package com.onlyone.app.views.chain

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.onlyone.app.R
import com.onlyone.app.Screen
import com.onlyone.app.composables.CustomColorOverlay
import com.onlyone.app.composables.FriendDropdown
import com.onlyone.app.theme.ThemeTokens
import com.onlyone.app.viewModels.userViewModel.UserViewModel

@Composable
fun CreateChainMessageView(
    theme: ThemeTokens,
    userViewModel: UserViewModel,
    navController: NavController
) {
    val ctx = LocalContext.current
    val me = userViewModel.user.value
    val authorNameForSteps =
        me?.username?.takeIf { it.isNotBlank() }
            ?: me?.username?.takeIf { it.isNotBlank() }
            ?: me?.uid
            ?: ""
    // Friends
    val localFriends by userViewModel.observeLocalFriends().collectAsState(initial = emptyList())
    val uidToName = remember(localFriends) { localFriends.associate { lf -> lf.uid to (lf.username ?: lf.uid) } }
    val nameToUid = remember(localFriends) { localFriends.associate { lf -> (lf.username ?: lf.uid) to lf.uid } }
    val friendDisplayList = remember(localFriends) { localFriends.map { it.username ?: it.uid } }
    var selectedFriendUid by remember { mutableStateOf("") }

    // Inputs: Title + First message
    var title by remember { mutableStateOf(TextFieldValue("")) }
    var firstMessage by remember { mutableStateOf(TextFieldValue("")) }
    val titleMax = 60
    val msgMax = 200

    // Target length = TOTAL steps including the creator's first message (step 0)
    var targetLength by remember { mutableStateOf(5) }
    val lengths = remember { (3..10).toList() }

    val sendEnabled = title.text.isNotBlank() && firstMessage.text.isNotBlank() && selectedFriendUid.isNotBlank()

    // Help overlay state
    var showHelp by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 32.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Compact header with Help icon ────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.chain_create_header),
                style = MaterialTheme.typography.h6,
                color = theme.textColor
            )
            IconButton(onClick = { showHelp = true }) {
                Icon(Icons.Filled.Help, contentDescription = stringResource(R.string.cd_help), tint = theme.textColor)
            }
        }

        // ── Main card with inputs ────────────────────────────────────────────
        CustomColorOverlay(
            modifier = Modifier.fillMaxWidth(),
            theme = theme,
            onDismiss = {}
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    val c = if (title.text.length >= titleMax) Color.Red else theme.textColor.copy(alpha = 0.7f)
                    Text("${title.text.length}/$titleMax", color = c, style = MaterialTheme.typography.caption)
                }

                // Title (single line)
                OutlinedTextField(
                    value = title,
                    onValueChange = { new ->
                        title = if (new.text.length <= titleMax) new
                        else TextFieldValue(new.text.take(titleMax), TextRange(titleMax))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.body1,
                    label = { Text(text = stringResource(R.string.chain_title_label), color = theme.textColor) },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        backgroundColor = theme.textFieldColor,
                        focusedBorderColor = theme.textColor,
                        unfocusedBorderColor = theme.textColor.copy(alpha = 0.6f),
                        cursorColor = theme.textColor,
                        textColor = theme.textColor
                    ),
                    singleLine = true,
                    maxLines = 1
                )

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    val c = if (firstMessage.text.length >= msgMax) Color.Red else theme.textColor.copy(alpha = 0.7f)
                    Text("${firstMessage.text.length}/$msgMax", color = c, style = MaterialTheme.typography.caption)
                }

                // First message (multi-line)
                OutlinedTextField(
                    value = firstMessage,
                    onValueChange = { new ->
                        firstMessage = if (new.text.length <= msgMax) new
                        else TextFieldValue(new.text.take(msgMax), TextRange(msgMax))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.body1,
                    label = { Text(text = stringResource(id = R.string.chain_seed_label), color = theme.textColor) },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        backgroundColor = theme.textFieldColor,
                        focusedBorderColor = theme.textColor,
                        unfocusedBorderColor = theme.textColor.copy(alpha = 0.6f),
                        cursorColor = theme.textColor,
                        textColor = theme.textColor
                    ),
                    singleLine = false,
                    minLines = 4,
                    maxLines = 6
                )

                // Length quick picker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = {
                        val idx = lengths.indexOf(targetLength)
                        targetLength = lengths[(idx + 1) % lengths.size]
                    }) {
                        Text(
                            text = stringResource(R.string.chain_length_value, targetLength),
                            color = theme.textColor
                        )
                    }
                }

                // Friend selector
                FriendDropdown(
                    theme = theme,
                    labelText = stringResource(R.string.chain_select_friend_label),
                    currentUid = selectedFriendUid,
                    displayNames = friendDisplayList,
                    uidToName = uidToName,
                    nameToUid = nameToUid,
                    enabled = localFriends.isNotEmpty(),
                    keyReset = "friends_dropdown_${localFriends.size}",
                    onFriendSelected = { uid -> selectedFriendUid = uid }
                )

                // Create button
                Button(
                    onClick = {
                        val t = title.text.trim()
                        val m = firstMessage.text.trim()
                        val assignee = selectedFriendUid
                        if (t.isEmpty() || m.isEmpty() || assignee.isEmpty()) return@Button

                        userViewModel.chainManager.createChain(
                            title = t,
                            targetLength = targetLength,        // TOTAL steps incl. creator step
                            firstAssigneeUid = assignee,
                            hiddenHistory = false,
                            ttlHours = 72,
                            firstMessage = m,                   // send first message
                            authorName = authorNameForSteps,
                            onSuccess = { chainId ->
                                navController.navigate(Screen.ChainDetailScreen.createRoute(chainId))
                            },
                            onError = { e ->
                                Toast.makeText(
                                    ctx,
                                    e.message ?: ctx.getString(R.string.chain_toast_failed),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    },
                    enabled = sendEnabled,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFF81C784),
                        contentColor = theme.textColor,
                        disabledBackgroundColor = theme.disabledButtonBackground.copy(alpha = 0.4f),
                        disabledContentColor = theme.cardContentColor.copy(alpha = 0.6f)
                    )
                ) {
                    Text(text = stringResource(R.string.chain_send_button), color = theme.textColor)
                }
            }
        }
    }

    // ── Help dialog (overlay on top; does not push layout) ───────────────────
    if (showHelp) {
        CustomColorOverlay(
            modifier = Modifier
                .fillMaxWidth()
                .padding(64.dp),
            theme = theme,
            onDismiss = { showHelp = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = stringResource(R.string.chain_description_title),
                    style = MaterialTheme.typography.subtitle1,
                    color = theme.textColor
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
                ) {
                    val c = Color.Yellow.copy(alpha = 0.9f)
                    val b = Color.Green.copy(alpha = 0.9f)
                    val a = Color.Blue.copy(alpha = 0.9f)
                    Icon(Icons.Filled.Person, contentDescription = null, tint = c, modifier = Modifier.size(20.dp))
                    Icon(Icons.Filled.Message, contentDescription = null, tint = b, modifier = Modifier.size(18.dp))
                    Icon(Icons.Filled.Person, contentDescription = null, tint = c, modifier = Modifier.size(20.dp))
                    Icon(Icons.Filled.Message, contentDescription = null, tint = b, modifier = Modifier.size(18.dp))
                    Icon(Icons.Filled.MoreHoriz, contentDescription = null, tint = c, modifier = Modifier.size(22.dp))
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = a, modifier = Modifier.size(20.dp))
                }
                Text(
                    text = stringResource(R.string.chain_description_body),
                    style = MaterialTheme.typography.body2,
                    color = theme.textColor.copy(alpha = 0.9f)
                )
                Text(
                    text = stringResource(R.string.chain_description_hint),
                    style = MaterialTheme.typography.caption,
                    color = theme.textColor.copy(alpha = 0.8f)
                )

                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { showHelp = false },
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Text(text = stringResource(R.string.common_ok), color = theme.textColor)
                }
            }
        }
    }
}
