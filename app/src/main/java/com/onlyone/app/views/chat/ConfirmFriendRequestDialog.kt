package com.onlyone.app.views.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.onlyone.app.R
import com.onlyone.app.composables.CustomColorOverlay
import com.onlyone.app.theme.ThemeTokens

@Composable
 fun ConfirmFriendRequestDialog(
    theme: ThemeTokens,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            Modifier
                .fillMaxWidth(0.98f)
                .widthIn(max = 480.dp)
        ) {
            CustomColorOverlay(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(percent = 4),
                overlayColor = Color.Gray,
                onDismiss = {},
                paddingBox1 = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                paddingBox2 = PaddingValues(10.dp),
                theme = theme
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.friends_dialog_title),
                        style = MaterialTheme.typography.h6,
                        color = theme.textColor
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.friends_dialog_send_confirm),
                        style = MaterialTheme.typography.body2,
                        color = theme.textColor,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text(stringResource(R.string.friends_dialog_cancel), color = theme.textColor)
                        }
                        TextButton(onClick = onConfirm) {
                            Text(stringResource(R.string.friends_dialog_send), color = theme.textColor)
                        }
                    }
                }
            }
        }
    }
}
