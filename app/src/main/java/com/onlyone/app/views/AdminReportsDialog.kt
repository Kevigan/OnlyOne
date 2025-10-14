package com.onlyone.app.views

import CustomAlertDialog
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.onlyone.app.theme.ThemeTokens
import com.onlyone.app.viewModels.ChatViewModel

@Composable
fun AdminReportsDialog(
    chatViewModel: ChatViewModel,
    onDismiss: () -> Unit,
    theme: ThemeTokens
) {
    val ctx = LocalContext.current

    val reports by chatViewModel.reports.collectAsState()
    val loading by chatViewModel.reportsLoading.collectAsState()

    var index by remember { mutableStateOf(0) }
    var processing by remember { mutableStateOf(false) }
    var banHoursText by remember { mutableStateOf("24") }
    var noteText by remember { mutableStateOf("") }

    // Confirm dialogs
    var confirmDismiss by remember { mutableStateOf(false) }
    var confirmWarn by remember { mutableStateOf(false) }
    var confirmBan by remember { mutableStateOf(false) }

    // Keep index in bounds if list changes (e.g., after resolving)
    LaunchedEffect(reports.size) {
        if (reports.isEmpty()) index = 0 else if (index > reports.lastIndex) index = reports.lastIndex
    }

    CustomAlertDialog(
        borderColor = theme.borderColor,
        theme = theme,
        onDismiss = { if (!processing) onDismiss() }
    ) {
        Column(Modifier.fillMaxWidth()) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Open Reports",
                    style = MaterialTheme.typography.h6,
                    color = theme.textColor
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = {
                            index = 0
                            chatViewModel.loadReports("open", 50)
                        },
                        enabled = !loading && !processing,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) { Text("Reload") }

                    Spacer(Modifier.width(8.dp))

                    TextButton(
                        onClick = onDismiss,
                        enabled = !processing
                    ) { Text("Close", color = theme.textColor) }
                }
            }

            if (loading) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
            }

            if (reports.isEmpty() && !loading) {
                Spacer(Modifier.height(8.dp))
                Text("No open reports.", color = theme.textColor)
                Spacer(Modifier.height(8.dp))
            } else if (reports.isNotEmpty()) {
                val r = reports[index]
                val createdStr = remember(r.createdAt) {
                    r.createdAt?.toDate()?.let { java.text.DateFormat.getDateTimeInstance().format(it) } ?: "-"
                }

                Spacer(Modifier.height(8.dp))

                // Position controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { if (index > 0) index-- },
                        enabled = index > 0 && !processing,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) { Text("◀ Back") }

                    Text("${index + 1} / ${reports.size}", color = theme.textColor)

                    OutlinedButton(
                        onClick = { if (index < reports.lastIndex) index++ },
                        enabled = index < reports.lastIndex && !processing,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) { Text("Next ▶") }
                }

                Spacer(Modifier.height(12.dp))
                Divider(color = theme.textColor.copy(alpha = 0.2f))
                Spacer(Modifier.height(12.dp))

                // Report body
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Reason: ${r.reason ?: "-"}", color = theme.textColor)
                    Text("From: ${r.reporterUsername ?: r.reporterId ?: "-"}", color = theme.textColor)
                    Text("About: ${r.offenderUsername ?: r.offenderId ?: "-"}", color = theme.textColor)
                    Text("Created: $createdStr", color = theme.textColor)

                    // --- NEW: moderation history summary (if your callable returns these) ---
                    val lastWarnStr = r.offenderLastWarnedAt?.toDate()?.let {
                        java.text.DateFormat.getDateTimeInstance().format(it)
                    } ?: "-"
                    val lastBanStr = r.offenderLastBannedAt?.toDate()?.let {
                        java.text.DateFormat.getDateTimeInstance().format(it)
                    } ?: "-"
                    Text(
                        text = "History: warned ${r.offenderWarnCount ?: 0}× · banned ${r.offenderBanCount ?: 0}×",
                        color = theme.textColor
                    )
                    Text("Last warn: $lastWarnStr · Last ban: $lastBanStr", color = theme.textColor)
                    // -----------------------------------------------------------------------

                    Spacer(Modifier.height(8.dp))
                    Text("Message:", color = theme.textColor, style = MaterialTheme.typography.subtitle2)
                    Surface(
                        color = theme.cardContentColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = r.messagePreview ?: "(no preview)",
                            color = theme.textColor,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                Divider(color = theme.textColor.copy(alpha = 0.2f))
                Spacer(Modifier.height(12.dp))

                // Optional moderator note
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it.take(200) },
                    label = { Text("Moderator note (optional)") },
                    textStyle = LocalTextStyle.current.copy(color = theme.textColor),
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                // Actions row 1: Dismiss / Warn
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = { confirmDismiss = true },
                        enabled = !processing
                    ) { Text("Dismiss") }

                    OutlinedButton(
                        onClick = { confirmWarn = true },
                        enabled = !processing
                    ) { Text("Warn") }
                }

                Spacer(Modifier.height(12.dp))

                // Actions row 2: Ban section (Column so it won't get squeezed)
                Column(Modifier.fillMaxWidth()) {
                    Text("Ban user", color = theme.textColor, style = MaterialTheme.typography.subtitle2)
                    Spacer(Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = banHoursText,
                            onValueChange = { banHoursText = it.filter { ch -> ch.isDigit() }.take(4) },
                            label = { Text("Hours") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = { confirmBan = true },
                            enabled = !processing
                        ) { Text("Ban") }
                    }
                }

                // --- Confirmations ---

                if (confirmDismiss) {
                    ConfirmActionDialog(
                        theme = theme,
                        title = "Close report?",
                        message = "Are you sure you want to close this report with no action?",
                        onCancel = { confirmDismiss = false },
                        onConfirm = {
                            confirmDismiss = false
                            processing = true
                            chatViewModel.resolveReport(
                                reportId = r.id,
                                action = "none",
                                note = noteText.ifBlank { null }
                            ) { ok ->
                                processing = false
                                Toast.makeText(ctx, if (ok) "Closed (no action)" else "Failed", Toast.LENGTH_SHORT).show()
                                if (ok) {
                                    if (index < reports.lastIndex) index++ else onDismiss()
                                }
                            }
                        }
                    )
                }

                if (confirmWarn) {
                    ConfirmActionDialog(
                        theme = theme,
                        title = "Warn user?",
                        message = "Are you sure you want to warn this user and close the report?",
                        onCancel = { confirmWarn = false },
                        onConfirm = {
                            confirmWarn = false
                            processing = true
                            chatViewModel.resolveReport(
                                reportId = r.id,
                                action = "warn",
                                note = noteText.ifBlank { null }
                            ) { ok ->
                                processing = false
                                Toast.makeText(ctx, if (ok) "Warned" else "Failed", Toast.LENGTH_SHORT).show()
                                if (ok) {
                                    if (index < reports.lastIndex) index++ else onDismiss()
                                }
                            }
                        }
                    )
                }

                if (confirmBan) {
                    val hrs = banHoursText.toIntOrNull() ?: 0
                    ConfirmActionDialog(
                        theme = theme,
                        title = "Ban user?",
                        message = if (hrs > 0)
                            "Are you sure you want to ban this user for $hrs hour(s) and close the report?"
                        else
                            "Enter a ban duration (> 0 hours) before confirming.",
                        confirmEnabled = hrs > 0 && !processing,
                        onCancel = { confirmBan = false },
                        onConfirm = {
                            confirmBan = false
                            if (hrs <= 0) return@ConfirmActionDialog
                            processing = true
                            chatViewModel.resolveReport(
                                reportId = r.id,
                                action = "ban",
                                banHours = hrs,
                                note = noteText.ifBlank { null }
                            ) { ok ->
                                processing = false
                                Toast.makeText(ctx, if (ok) "Banned ${hrs}h" else "Failed", Toast.LENGTH_SHORT).show()
                                if (ok) {
                                    if (index < reports.lastIndex) index++ else onDismiss()
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ConfirmActionDialog(
    theme: ThemeTokens,
    title: String,
    message: String,
    confirmEnabled: Boolean = true,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    CustomAlertDialog(
        borderColor = theme.borderColor,
        theme = theme,
        onDismiss = onCancel
    ) {
        Column(Modifier.fillMaxWidth()) {
            Text(text = title, style = MaterialTheme.typography.h6, color = theme.textColor)
            Spacer(Modifier.height(8.dp))
            Text(text = message, color = theme.textColor)
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onCancel) { Text("No", color = theme.textColor) }
                Spacer(Modifier.width(8.dp))
                TextButton(
                    onClick = onConfirm,
                    enabled = confirmEnabled
                ) { Text("Yes") }
            }
        }
    }
}
