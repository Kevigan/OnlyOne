package com.example.onlyone.views.settingsView

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
import com.example.onlyone.R
import com.example.onlyone.theme.ThemeTokens
import com.example.onlyone.utils.LanguageCatalog
import com.example.onlyone.utils.applyAppLocale
import com.example.onlyone.viewModels.userViewModel.UserViewModel
import com.example.onlyone.views.chat.ChatLanguageHelpDialog

@Composable
fun LanguageSettingsView(
    userViewModel: UserViewModel,
    theme: ThemeTokens,
) {
    var currentAppLang by remember { mutableStateOf("en") }
    var currentChatLang by remember { mutableStateOf("en") }
    var showChatLangInfo by remember { mutableStateOf(false) }

    // App language (en/de)
    val appLangs = LanguageCatalog.appOptions()
    // Chat language (full EU set)
    val chatLangs = LanguageCatalog.chatOptions()

    LaunchedEffect(Unit) {
        userViewModel.getAppLanguage { saved ->
            currentAppLang = if (saved in LanguageCatalog.APP) saved else "en"
        }
        currentChatLang = userViewModel.user.value?.chatLanguage
            ?.takeIf { it in LanguageCatalog.CHAT } ?: "en"
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        // --- App language header
        Text(
            text = stringResource(R.string.settings_app_language_header),
            color = theme.textColor,
            fontSize = 18.sp
        )
        Spacer(Modifier.height(12.dp))
        LanguageDropdown(
            theme = theme,
            currentCode = currentAppLang,
            options = appLangs,
            onSelected = { code ->
                userViewModel.saveAppLanguage(code)
                currentAppLang = code
                applyAppLocale(code)
            }
        )

        Spacer(Modifier.height(24.dp))

        // --- Chat language header + help icon
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
            options = chatLangs,
            onSelected = { code ->
                userViewModel.updatePublicProfile(
                    updates = mapOf("chatLanguage" to code),
                    onSuccess = { currentChatLang = code },
                    onFailure = { /* optionally show a toast/snackbar */ }
                )
            }
        )
    }

    if (showChatLangInfo) {
        ChatLanguageHelpDialog(
            theme = theme,
            onDismiss = { showChatLangInfo = false }
        )
    }
}

@Composable
private fun LanguageDropdown(
    theme: ThemeTokens,
    currentCode: String,
    options: Map<String, String>, // code -> displayName
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val display = options[currentCode] ?: currentCode.uppercase()

    Box(
        Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
    ) {
        OutlinedTextField(
            value = display,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = theme.textColor,
                cursorColor = theme.textColor,
                focusedBorderColor = theme.textColor,
                unfocusedBorderColor = theme.textColor.copy(alpha = 0.6f),
                disabledTextColor = theme.textColor
            )
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (code, label) ->
                DropdownMenuItem(onClick = {
                    expanded = false
                    onSelected(code)
                }) {
                    Text(label)
                }
            }
        }
    }
}

