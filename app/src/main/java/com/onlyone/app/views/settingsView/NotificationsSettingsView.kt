package com.onlyone.app.views.settingsView

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onlyone.app.R
import com.onlyone.app.theme.ThemeTokens
import com.onlyone.app.viewModels.userViewModel.UserViewModel

@Composable
fun NotificationsSettingsView(
    userViewModel: UserViewModel,
    theme: ThemeTokens,
) {
    val user = userViewModel.user.value ?: return
    val uid = user.uid

    data class NotificationToggleItem(@StringRes val labelRes: Int, val key: String)

    // Stable, locale-independent keys.
    val items = remember {
        listOf(
            NotificationToggleItem(R.string.settings_notifications_message, "message"),
            NotificationToggleItem(R.string.settings_notifications_feedback, "feedback"),
            NotificationToggleItem(R.string.settings_notifications_friend_request, "friendRequest") // 🆕
        )
    }

    // State by key
    val toggles = remember { mutableStateMapOf<String, Boolean>() }

    // Load initial state (now expects 3 values)
    LaunchedEffect(uid) {
        userViewModel.getNotificationToggles { msgEnabled, fbEnabled, frEnabled ->
            toggles["message"] = msgEnabled
            toggles["feedback"] = fbEnabled
            toggles["friendRequest"] = frEnabled // 🆕
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.settings_notifications_header),
            color = theme.textColor,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        items.forEach { item ->
            val isChecked = toggles[item.key] ?: true

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(item.labelRes),
                    color = theme.textColor,
                    style = MaterialTheme.typography.body1
                )

                Switch(
                    checked = isChecked,
                    onCheckedChange = { checked ->
                        toggles[item.key] = checked

                        // Save locally (all three together)
                        val msg = toggles["message"] ?: true
                        val fb  = toggles["feedback"] ?: true
                        val fr  = toggles["friendRequest"] ?: true
                        userViewModel.saveNotificationToggles(msg, fb, fr) // 🆕

                        // Save remotely (individual)
                        userViewModel.updateNotificationPreference(item.key, checked)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Green,
                        uncheckedThumbColor = Color.Gray
                    )
                )
            }
        }
    }
}
