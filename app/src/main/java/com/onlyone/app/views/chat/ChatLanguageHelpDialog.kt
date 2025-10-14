package com.onlyone.app.views.chat

import CustomAlertDialog
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.onlyone.app.R
import com.onlyone.app.theme.ThemeTokens

@Composable
fun ChatLanguageHelpDialog(
    theme: ThemeTokens,
    onDismiss: () -> Unit
) {
    CustomAlertDialog(
        borderColor = Color.Red,
        theme = theme,
        onDismiss = onDismiss
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.settings_chat_language_help_title),
                style = MaterialTheme.typography.h6,
                color = theme.textColor
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.settings_chat_language_help_body),
                style = MaterialTheme.typography.body1,
                color = theme.textColor
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(stringResource(R.string.common_ok))
            }
        }
    }
}
