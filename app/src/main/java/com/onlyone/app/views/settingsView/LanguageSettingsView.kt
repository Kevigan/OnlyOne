// com.onlyone.app.views.settingsView/LanguageSettingsView.kt
package com.onlyone.app.views.settingsView

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onlyone.app.R
import com.onlyone.app.theme.ThemeTokens
import com.onlyone.app.utils.*
import com.onlyone.app.viewModels.userViewModel.UserViewModel
import com.onlyone.app.views.chat.ChatLanguageHelpDialog

@Composable
fun LanguageSettingsView(
    userViewModel: UserViewModel,
    theme: ThemeTokens,
) {
    val currentAppLang by userViewModel.appLanguage.collectAsState()
    var currentChatLang by remember { mutableStateOf("en") }


    var showChatLangInfo by remember { mutableStateOf(false) }
    var nextSwitchAtMs by remember { mutableStateOf(0L) }
    var pickerEnabled by remember { mutableStateOf(true) }

    // Re-enable picker after the cooldown
    LaunchedEffect(nextSwitchAtMs) {
        val wait = (nextSwitchAtMs - android.os.SystemClock.uptimeMillis()).coerceAtLeast(0L)
        if (wait > 0) {
            pickerEnabled = false
            kotlinx.coroutines.delay(wait)
        }
        pickerEnabled = true
    }

    val appLangs = LanguageCatalog.appOptions()   // Map<String, String> code -> label
    val chatLangs = LanguageCatalog.chatOptions()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = stringResource(R.string.settings_app_language_header),
            color = theme.textColor,
            fontSize = 18.sp
        )
        Spacer(Modifier.height(12.dp))

        LanguageDropdown(
            theme = theme,
            currentCode = currentAppLang,
            availableLanguages = appLangs.values.toList(),
            languageMap = appLangs,
            reverseMap = appLangs.entries.associate { (k, v) -> v to k },
            enabled = pickerEnabled,
            keyReset = currentAppLang
        ) { selectedCode ->
            if (!pickerEnabled || selectedCode == currentAppLang) return@LanguageDropdown
            userViewModel.setAppLanguageAndApply(selectedCode)   // 👈 single call
            nextSwitchAtMs = android.os.SystemClock.uptimeMillis() + 1100L
        }

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.settings_select_chat_language_label),
                color = theme.textColor,
                fontSize = 18.sp
            )
            IconButton(onClick = { showChatLangInfo = true }) {
                Icon(
                    imageVector = Icons.Outlined.HelpOutline,
                    contentDescription = "Chat language info",
                    tint = Color.Yellow
                )
            }
        }
        Spacer(Modifier.height(12.dp))

        LanguageDropdown(
            theme = theme,
            currentCode = currentChatLang,
            availableLanguages = chatLangs.values.toList(),
            languageMap = chatLangs,
            reverseMap = chatLangs.entries.associate { (k, v) -> v to k },
            keyReset = currentChatLang
        ) { selectedCode ->
            val normalized = normalizeLang(selectedCode)
            userViewModel.updatePublicProfile(
                updates = mapOf("chatLanguage" to normalized),
                onSuccess = { currentChatLang = normalized },
                onFailure = { /* toast/snackbar if desired */ }
            )
        }
    }

    if (showChatLangInfo) {
        ChatLanguageHelpDialog(theme = theme, onDismiss = { showChatLangInfo = false })
    }
}
