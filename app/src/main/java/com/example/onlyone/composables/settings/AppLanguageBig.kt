package com.example.onlyone.composables.settings

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppLanguageBig(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit
) {
    val availableLanguages = listOf("English", "Deutsch", "Français", "Español", "Português")
    val languageMap = mapOf(
        "en" to "English",
        "de" to "Deutsch",
        "fr" to "Français",
        "es" to "Español",
        "pt" to "Português"
    )
    val reverseMap = languageMap.entries.associate { it.value to it.key }

    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf(languageMap[currentLanguage] ?: "English") }

    LaunchedEffect(currentLanguage) {
        selectedText = languageMap[currentLanguage] ?: "English"
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            Text("🌐 App Language", color = Color.White, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(12.dp))

            Box {
                OutlinedTextField(
                    value = selectedText,
                    onValueChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = true },
                    enabled = false,
                    label = { Text("Select language") },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        disabledTextColor = Color.White,
                        disabledLabelColor = Color.LightGray,
                        disabledBorderColor = Color.White,
                        disabledTrailingIconColor = Color.White
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
                    availableLanguages.forEach { lang ->
                        DropdownMenuItem(onClick = {
                            selectedText = lang
                            expanded = false
                            onLanguageSelected(reverseMap[lang] ?: "en")
                        }) {
                            Text(lang)
                        }
                    }
                }
            }
        }
    }
}
