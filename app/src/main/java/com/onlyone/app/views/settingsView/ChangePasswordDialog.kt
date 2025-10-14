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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.onlyone.app.R
import com.onlyone.app.theme.ThemeTokens

@Composable
fun ChangePasswordDialog(
    theme: ThemeTokens,
    onDismiss: () -> Unit,
    onConfirm: (currentPassword: String, newPassword: String) -> Unit,
    isProcessing: Boolean = false // <-- NEW
) {
    val context = LocalContext.current
    var current by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var currentVisible by remember { mutableStateOf(false) }
    var newVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val title = stringResource(R.string.auth_change_dialog_title)
    val lblCurrent = stringResource(R.string.auth_current_password_label)
    val lblNew = stringResource(R.string.auth_new_password_label)
    val lblConfirm = stringResource(R.string.auth_confirm_password_label)
    val txtCancel = stringResource(R.string.auth_dialog_cancel)
    val txtSave = stringResource(R.string.auth_dialog_save)
    val errShort = stringResource(R.string.auth_error_password_short)
    val errMismatch = stringResource(R.string.auth_error_password_mismatch)
    val errEnter = stringResource(R.string.auth_error_enter_current_password)

    val tfColors = TextFieldDefaults.outlinedTextFieldColors(
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

    CustomAlertDialog(
        theme = theme,
        onDismiss = if (isProcessing) ({}) else onDismiss // lock while processing
    ) {
        Column {
            Text(title, color = theme.textColor, style = MaterialTheme.typography.h6)
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = current,
                onValueChange = { if (!isProcessing) { error = null; current = it } },
                enabled = !isProcessing,
                label = { Text(lblCurrent, color = theme.textColor.copy(0.9f)) },
                visualTransformation = if (currentVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { if (!isProcessing) currentVisible = !currentVisible }) {
                        Icon(
                            if (currentVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = theme.textColor
                        )
                    }
                },
                colors = tfColors
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = newPass,
                onValueChange = { if (!isProcessing) { error = null; newPass = it } },
                enabled = !isProcessing,
                label = { Text(lblNew, color = theme.textColor.copy(0.9f)) },
                visualTransformation = if (newVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { if (!isProcessing) newVisible = !newVisible }) {
                        Icon(
                            if (newVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = theme.textColor
                        )
                    }
                },
                colors = tfColors
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = confirm,
                onValueChange = { if (!isProcessing) { error = null; confirm = it } },
                enabled = !isProcessing,
                label = { Text(lblConfirm, color = theme.textColor.copy(0.9f)) },
                visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { if (!isProcessing) confirmVisible = !confirmVisible }) {
                        Icon(
                            if (confirmVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = theme.textColor
                        )
                    }
                },
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
                        error = when {
                            newPass.length < 6 -> errShort
                            newPass != confirm -> errMismatch
                            current.isBlank() -> errEnter
                            else -> null
                        }
                        if (error == null) onConfirm(current, newPass)
                    },
                    enabled = !isProcessing
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text(txtSave, color = theme.textColor)
                    }
                }
            }
        }
    }
}
