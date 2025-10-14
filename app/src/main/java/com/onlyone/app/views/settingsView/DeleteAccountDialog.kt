package com.onlyone.app.views.settingsView

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
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.onlyone.app.R
import com.onlyone.app.theme.ThemeTokens

@Composable
fun DeleteAccountDialog(
    theme: ThemeTokens,
    isPasswordUser: Boolean,
    onDismiss: () -> Unit,
    onConfirmWithPassword: (String) -> Unit,
    onRequestGoogleReauth: () -> Unit,
    isProcessing: Boolean = false   // <-- NEW, with default
) {
    val context = LocalContext.current
    var pwd by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // capture strings in composition
    val title = stringResource(R.string.settings_delete_title)
    val body = stringResource(R.string.settings_delete_body)
    val cancelTxt = stringResource(R.string.settings_delete_cancel)
    val confirmTxt = stringResource(R.string.settings_delete_confirm)
    val currentPwdLabel = stringResource(R.string.auth_current_password_label)
    val enterPwdError = stringResource(R.string.auth_error_enter_current_password)
    val googleNoPwdMsg = stringResource(R.string.auth_toast_google_no_password)

    CustomAlertDialog(theme = theme, onDismiss = if (isProcessing) ({}) else onDismiss) {
        Column(horizontalAlignment = Alignment.Start) {
            Text(title, color = theme.textColor, style = MaterialTheme.typography.h6)
            Spacer(Modifier.height(8.dp))
            Text(body, color = theme.textColor.copy(alpha = 0.85f))

            if (isPasswordUser) {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = pwd,
                    onValueChange = { if (!isProcessing) { error = null; pwd = it } },
                    enabled = !isProcessing, // <-- disable while processing
                    label = { Text(currentPwdLabel, color = theme.textColor.copy(0.9f)) },
                    visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { if (!isProcessing) visible = !visible }) {
                            Icon(
                                if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = theme.textColor
                            )
                        }
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = theme.textColor,
                        cursorColor = theme.borderColor,
                        focusedBorderColor = theme.borderColor,
                        unfocusedBorderColor = theme.textColor.copy(alpha = 0.75f),
                        focusedLabelColor = theme.textColor,
                        unfocusedLabelColor = theme.textColor.copy(alpha = 0.8f),
                        trailingIconColor = theme.textColor,
                        disabledTextColor = theme.textColor.copy(alpha = 0.7f),
                        disabledBorderColor = theme.textColor.copy(alpha = 0.4f),
                        disabledLabelColor = theme.textColor.copy(alpha = 0.5f)
                    )
                )
                if (!error.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(error!!, color = MaterialTheme.colors.error, style = MaterialTheme.typography.body2)
                }
            } else {
                Spacer(Modifier.height(12.dp))
                Text(googleNoPwdMsg, color = theme.textColor.copy(alpha = 0.85f), style = MaterialTheme.typography.body2)
            }

            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(
                    onClick = onDismiss,
                    enabled = !isProcessing // <-- disable while processing
                ) {
                    Text(cancelTxt, color = theme.textColor)
                }
                Spacer(Modifier.width(8.dp))
                TextButton(
                    onClick = {
                        if (isProcessing) return@TextButton
                        if (isPasswordUser) {
                            if (pwd.isBlank()) {
                                error = enterPwdError
                            } else {
                                onConfirmWithPassword(pwd)
                            }
                        } else {
                            onRequestGoogleReauth()
                        }
                    },
                    enabled = !isProcessing // <-- disable while processing
                ) {
                    if (isProcessing) {
                        // Spinner instead of text
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(confirmTxt, color = MaterialTheme.colors.error)
                    }
                }
            }
        }
    }
}
