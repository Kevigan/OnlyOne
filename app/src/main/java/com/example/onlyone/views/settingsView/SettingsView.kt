package com.example.onlyone.views.settingsView

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.onlyone.R
import com.example.onlyone.composables.CustomColorOverlay
import com.example.onlyone.composables.settings.AboutItemBig
import com.example.onlyone.composables.settings.AccountItemBig
import com.example.onlyone.composables.settings.AppLanguageBig
import com.example.onlyone.composables.settings.AppearanceItemBig
import com.example.onlyone.composables.settings.NotificationsItemBig
import com.example.onlyone.composables.settings.PrivacyItemBig
import com.example.onlyone.viewModels.userViewModel.UserViewModel

@Composable
fun SettingsView(userViewModel: UserViewModel) {
    val sections = remember { SettingSection.values().toList() }
    var selectedSection by remember { mutableStateOf<SettingSection?>(null) }
    val countFakeUsers = 20

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp)
    ) {
        Column(modifier = Modifier.padding(top = 20.dp)) {
            Text(
                text = stringResource(R.string.settings_header),
                style = MaterialTheme.typography.h5,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyColumn {
                items(sections) { section ->
                    SettingsItemRow(
                        title = stringResource(section.titleRes),
                        onClick = { selectedSection = section }
                    )
                }
            }

            Button(
                onClick = { userViewModel.createFakeUsers(count = countFakeUsers) },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_create_fake_users, countFakeUsers),
                    color = Color.White
                )
            }

            Button(
                onClick = {
                    userViewModel.seedAchievementDefinitions(
                        onSuccess = { count ->
                            Log.d("Seeder", "✅ Seeded $count definitions.")
                        },
                        onFailure = { error ->
                            Log.e("Seeder", "❌ Failed to seed: ${error.message}")
                        }
                    )
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_seed_achievements),
                    color = Color.White
                )
            }
        }

        // Overlay
        selectedSection?.let { section ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.15f))
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { selectedSection = null })
                    }
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(0.9f)
                        .fillMaxHeight(0.66f)
                        .clickable(enabled = false) {}
                ) {
                    CustomColorOverlay(
                        modifier = Modifier.fillMaxSize(),
                        overlayColor = Color.DarkGray.copy(alpha = 0.95f),
                        borderColor = Color.White,
                        borderWidth = 0.dp,
                        shape = RoundedCornerShape(24.dp),
                        paddingBox1 = PaddingValues(0.dp),
                        paddingBox2 = PaddingValues(24.dp),
                        onDismiss = {}
                    ) {
                        when (section) {
                            SettingSection.ACCOUNT -> AccountSettingsView()
                            SettingSection.NOTIFICATIONS -> NotificationsSettingsView(userViewModel)
                            SettingSection.LANGUAGE -> LanguageSettingsView(userViewModel)
                            SettingSection.PRIVACY -> PrivacySettingsView()
                            SettingSection.APPEARANCE -> AppearanceSettingsView()
                            SettingSection.ABOUT -> AboutSettingsView()
                        }
                    }
                }
            }
        }
    }
}


enum class SettingSection(@StringRes val titleRes: Int) {
    ACCOUNT(R.string.settings_section_account),
    NOTIFICATIONS(R.string.settings_section_notifications),
    LANGUAGE(R.string.settings_section_language),
    PRIVACY(R.string.settings_section_privacy),
    APPEARANCE(R.string.settings_section_appearance),
    ABOUT(R.string.settings_section_about)
}



