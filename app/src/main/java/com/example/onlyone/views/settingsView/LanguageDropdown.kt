package com.example.onlyone.views.settingsView

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
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
import com.example.onlyone.R
import com.example.onlyone.theme.ThemeTokens

@Composable
fun LanguageDropdown(
    theme: ThemeTokens,
    currentCode: String,
    availableLanguages: List<String>,
    languageMap: Map<String, String>,     // code -> display
    reverseMap: Map<String, String>,      // display -> code
    onLanguageSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf(languageMap[currentCode] ?: languageMap["en"] ?: "English") }

    LaunchedEffect(currentCode, languageMap) {
        selectedText = languageMap[currentCode] ?: languageMap["en"] ?: "English"
    }

    Box {
        OutlinedTextField(
            value = selectedText,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            enabled = false,
            label = { Text(stringResource(R.string.settings_select_language_label), color = theme.textColor) },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                disabledTextColor = theme.textColor,
                disabledLabelColor = Color.LightGray,
                disabledBorderColor = theme.textColor,
                disabledTrailingIconColor = theme.textColor
            ),
            trailingIcon = {
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            availableLanguages.forEach { display ->
                DropdownMenuItem(onClick = {
                    selectedText = display
                    expanded = false
                    onLanguageSelected(reverseMap[display] ?: "en")
                }) {
                    Text(display, color = theme.textColor)
                }
            }
        }
    }
}