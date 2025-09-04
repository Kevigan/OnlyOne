package com.example.onlyone.views.settingsView

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlyone.R
import com.example.onlyone.theme.ThemeTokens
import com.example.onlyone.utils.applyAppLocale
import com.example.onlyone.viewModels.userViewModel.UserViewModel

@Composable
fun LanguageSettingsView(userViewModel: UserViewModel, theme: ThemeTokens,) {
    var currentAppLang by remember { mutableStateOf("en") }
    var currentChatLang by remember { mutableStateOf("en") }

    // Localized display names (from string resources below)
    val langNames = mapOf(
        "en" to stringResource(R.string.lang_english),
        "de" to stringResource(R.string.lang_german),
        "fr" to stringResource(R.string.lang_french),
        "es" to stringResource(R.string.lang_spanish),
        "pt" to stringResource(R.string.lang_portuguese)
    )
    val availableLanguages = langNames.values.toList()
    val languageMap = langNames                    // code -> display name
    val reverseMap = langNames.entries.associate { it.value to it.key } // display -> code

    LaunchedEffect(Unit) {
        userViewModel.getAppLanguage { saved ->
            currentAppLang = saved
            // remove this now that you apply at startup to prevent flicker:
            // applyAppLocale(saved)
        }
        currentChatLang = userViewModel.user.value?.chatLanguage ?: "en"
    }

    Box(Modifier.fillMaxSize()) {
        Column {
            Text(
                text = stringResource(R.string.settings_app_language_header),
                color = theme.textColor,
                fontSize = 18.sp
            )
            Spacer(Modifier.height(12.dp))

            LanguageDropdown(
                theme = theme,
                currentCode = currentAppLang,
                availableLanguages = availableLanguages,
                languageMap = languageMap,
                reverseMap = reverseMap
            ) { code ->
                userViewModel.saveAppLanguage(code)
                currentAppLang = code
                applyAppLocale(code) // apply instantly on user change
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.settings_chat_language_header),
                color = theme.textColor,
                fontSize = 18.sp
            )
            Spacer(Modifier.height(12.dp))

            LanguageDropdown(
                theme = theme,
                currentCode = currentChatLang,
                availableLanguages = availableLanguages,
                languageMap = languageMap,
                reverseMap = reverseMap
            ) { code ->
                userViewModel.updatePublicProfile(
                    updates = mapOf("chatLanguage" to code),
                    onSuccess = { currentChatLang = code },
                    onFailure = { /* show error */ }
                )
            }
        }
    }
}

