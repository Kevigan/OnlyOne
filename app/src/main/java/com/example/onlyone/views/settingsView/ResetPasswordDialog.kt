package com.example.onlyone.views.settingsView

import CustomAlertDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.onlyone.R
import com.example.onlyone.theme.ThemeTokens

@Composable
fun ResetPasswordDialog(
    theme: ThemeTokens,
    prefillEmail: String,
    onDismiss: () -> Unit,
    onConfirm: (email: String) -> Unit,
    isProcessing: Boolean = false // <-- NEW
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf(prefillEmail) }
    var error by remember { mutableStateOf<String?>(null) }

    val title = stringResource(R.string.auth_reset_dialog_title)
    val lblEmail = stringResource(R.string.auth_email_label)
    val txtCancel = stringResource(R.string.auth_dialog_cancel)
    val txtSend = stringResource(R.string.auth_reset_dialog_send)
    val errInvalid = stringResource(R.string.auth_error_invalid_email)

    val tfColors = TextFieldDefaults.outlinedTextFieldColors(
        textColor = theme.textColor,
        cursorColor = theme.borderColor,
        focusedBorderColor = theme.borderColor,
        unfocusedBorderColor = theme.textColor.copy(alpha = 0.75f),
        focusedLabelColor = theme.textColor,
        unfocusedLabelColor = theme.textColor.copy(alpha = 0.8f),
        disabledTextColor = theme.textColor.copy(alpha = 0.7f),
        disabledBorderColor = theme.textColor.copy(alpha = 0.4f),
        disabledLabelColor = theme.textColor.copy(alpha = 0.5f)
    )

    CustomAlertDialog(
        theme = theme,
        onDismiss = if (isProcessing) ({}) else onDismiss // lock while processing
    ) {
        Column {
            Text(title, color = theme.textColor, style = MaterialTheme.typography.h6)
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { if (!isProcessing) { error = null; email = it } },
                enabled = !isProcessing,
                label = { Text(lblEmail, color = theme.textColor.copy(0.9f)) },
                colors = tfColors
            )

            if (!error.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(error!!, color = MaterialTheme.colors.error, style = MaterialTheme.typography.body2)
            }

            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss, enabled = !isProcessing) {
                    Text(txtCancel, color = theme.textColor)
                }
                Spacer(Modifier.width(8.dp))
                TextButton(
                    onClick = {
                        if (isProcessing) return@TextButton
                        if (email.isBlank() || !email.contains('@')) {
                            error = errInvalid
                        } else {
                            onConfirm(email.trim())
                        }
                    },
                    enabled = !isProcessing
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text(txtSend, color = theme.textColor)
                    }
                }
            }
        }
    }
}
