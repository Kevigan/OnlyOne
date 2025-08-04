package com.example.onlyone.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.unit.dp
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
    val settingsItems = listOf("Account", "Notifications", "AppLanguage", "Privacy", "Appearance", "About")
    var selectedSection by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(15.dp)
    ) {
        Column {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.h5,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Box {
                LazyColumn {
                    items(settingsItems) { item ->
                        CustomColorOverlay(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedSection = item },
                            shape = RoundedCornerShape(percent = 50),
                            overlayColor = Color.DarkGray.copy(alpha = 0.4f),
                            borderColor = Color.LightGray,
                            borderWidth = 1.dp,
                            onDismiss = {}, // not needed per item
                            paddingBox1 = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                            paddingBox2 = PaddingValues(6.dp)
                        ) {
                            Text(
                                text = item,
                                modifier = Modifier.padding(8.dp),
                                style = MaterialTheme.typography.body1,
                                color = Color.White
                            )
                        }
                    }
                }

                selectedSection?.let { section ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures(onTap = {
                                    selectedSection = null
                                })// dismiss when clicking outside
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(bottom = 125.dp)
                                .clickable(enabled = false) {} // absorb inside clicks
                        ) {
                            CustomColorOverlay(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(2.dp),
                                overlayColor = Color.DarkGray.copy(alpha = 0.95f),
                                borderColor = Color.White,
                                borderWidth = 0.dp,
                                shape = RoundedCornerShape(24.dp),
                                paddingBox1 = PaddingValues(0.dp),
                                paddingBox2 = PaddingValues(24.dp),
                                onDismiss = {}
                            ) {
                                when (section) {
                                    "Account" -> AccountItemBig()
                                    "Notifications" -> NotificationsItemBig(userViewModel = userViewModel)
                                    "AppLanguage" -> {
                                        var currentLang by remember { mutableStateOf("en") }

                                        LaunchedEffect(Unit) {
                                            userViewModel.getAppLanguage { saved ->
                                                currentLang = saved
                                            }
                                        }

                                        AppLanguageBig(
                                            currentLanguage = currentLang,
                                            onLanguageSelected = { newLang ->
                                                userViewModel.saveAppLanguage(newLang)
                                                currentLang = newLang
                                            }
                                        )
                                    }


                                    "Privacy" -> PrivacyItemBig()
                                    "Appearance" -> AppearanceItemBig()
                                    "About" -> AboutItemBig()
                                }
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    userViewModel.createFakeUsers(count = 20)
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Create 20 Fake Users", color = Color.White)
            }

        }
    }
}


