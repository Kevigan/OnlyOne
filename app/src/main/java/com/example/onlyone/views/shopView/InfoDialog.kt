package com.example.onlyone.views.shopView

import CustomAlertDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.onlyone.R
import com.example.onlyone.theme.ThemeTokens

@Composable
fun InfoDialog(
    title: String,
    message: String,
    theme: ThemeTokens,
    onDismiss: () -> Unit
) {
    CustomAlertDialog(
        theme = theme,
        borderColor = theme.borderColor,
        onDismiss = onDismiss
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.h6,
                color = theme.textColor
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.body1,
                color = theme.textColor
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                androidx.compose.material.TextButton(onClick = onDismiss) {
                    Text(
                        text = stringResource(R.string.shop_info_ok),
                        color = theme.textColor
                    )
                }
            }
        }
    }
}
