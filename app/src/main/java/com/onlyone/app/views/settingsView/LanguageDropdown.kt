package com.onlyone.app.views.settingsView

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.onlyone.app.R
import com.onlyone.app.theme.ThemeTokens
import com.onlyone.app.utils.normalizeLang

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LanguageDropdown(
    theme: ThemeTokens,
    currentCode: String,
    availableLanguages: List<String>,
    languageMap: Map<String, String>,
    reverseMap: Map<String, String>,
    enabled: Boolean = true,
    keyReset: String,
    onLanguageSelected: (String) -> Unit
) {
    var expanded by remember(keyReset) { mutableStateOf(false) }

    val selectedText = languageMap[currentCode]
        ?: languageMap[normalizeLang(currentCode)]
        ?: currentCode.uppercase()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded }
    ) {
        OutlinedTextField(
            readOnly = true,
            value = selectedText,
            onValueChange = {},
            label = {
                Text(
                    stringResource(R.string.settings_select_language_label),
                    color = theme.textColor
                )
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            enabled = enabled,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = theme.textColor,
                disabledTextColor = theme.textColor,
                disabledLabelColor = theme.textColor.copy(alpha = 0.5f),
                disabledBorderColor = theme.textColor.copy(alpha = 0.3f),
                disabledTrailingIconColor = theme.textColor,
                focusedBorderColor = theme.textColor,
                unfocusedBorderColor = theme.textColor.copy(alpha = 0.6f)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(theme.gradientColor2)
        ) {
            availableLanguages.forEach { display ->
                DropdownMenuItem(onClick = {
                    expanded = false
                    reverseMap[display]?.let(onLanguageSelected)
                }) {
                    Text(text = display, color = theme.textColor, style = MaterialTheme.typography.body1)
                }
                Divider(color = theme.textColor.copy(alpha = 0.15f), thickness = 0.5.dp)
            }
        }
    }
}

