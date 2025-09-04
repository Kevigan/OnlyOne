package com.example.onlyone.views.settingsView

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.example.onlyone.R
import com.example.onlyone.theme.ThemeTokens
import com.example.onlyone.viewModels.userViewModel.UserViewModel

@Composable
fun NotificationsSettingsView(userViewModel: UserViewModel, theme: ThemeTokens,) {
    val user = userViewModel.user.value ?: return
    val uid = user.uid

    data class NotificationToggleItem(@StringRes val labelRes: Int, val key: String)

    // Stable items: key is locale-independent
    val items = remember {
        listOf(
            NotificationToggleItem(R.string.settings_notifications_message, "message"),
            NotificationToggleItem(R.string.settings_notifications_feedback, "feedback")
        )
    }

    // State keyed by stable keys
    val toggles = remember { mutableStateMapOf<String, Boolean>() }

    // Load initial state
    LaunchedEffect(uid) {
        userViewModel.getNotificationToggles { msgEnabled, fbEnabled ->
            toggles["message"] = msgEnabled
            toggles["feedback"] = fbEnabled
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column {
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

                            // Save locally (both toggles together)
                            val msgToggle = toggles["message"] ?: true
                            val fbToggle  = toggles["feedback"] ?: true
                            userViewModel.saveNotificationToggles(msgToggle, fbToggle)

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
}
