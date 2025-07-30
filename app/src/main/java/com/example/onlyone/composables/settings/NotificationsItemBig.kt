package com.example.onlyone.composables.settings

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlyone.viewModels.UserViewModel

@Composable
fun NotificationsItemBig(userViewModel: UserViewModel) {
    val context = LocalContext.current
    val user = userViewModel.user.value ?: return
    val uid = user.uid

    val toggleMap = mapOf(
        "Message notifications" to "message",
        "Feedback notifications" to "feedback"
    )

    val toggles = remember { mutableStateMapOf<String, Boolean>() }

    // 🔄 Load initial toggle states from Room
    LaunchedEffect(uid) {
        userViewModel.getNotificationToggles { msgEnabled, fbEnabled ->
            toggles["Message notifications"] = msgEnabled
            toggles["Feedback notifications"] = fbEnabled
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            Text("🔔 Notifications", color = Color.White, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(12.dp))

            toggleMap.forEach { (label, key) ->
                val isChecked = toggles[label] ?: true

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = label,
                        color = Color.White,
                        style = MaterialTheme.typography.body1
                    )

                    Switch(
                        checked = isChecked,
                        onCheckedChange = { checked ->
                            toggles[label] = checked

                            // ✅ Save locally to Room
                            val msgToggle = toggles["Message notifications"] ?: true
                            val fbToggle = toggles["Feedback notifications"] ?: true
                            userViewModel.saveNotificationToggles(msgToggle, fbToggle)

                            // ✅ Save to Firestore
                            userViewModel.updateNotificationPreference(key, checked)
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


