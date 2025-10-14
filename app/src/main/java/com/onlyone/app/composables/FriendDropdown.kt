package com.onlyone.app.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.onlyone.app.theme.ThemeTokens

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FriendDropdown(
    theme: ThemeTokens,
    labelText: String,                    // ← custom label you control
    currentUid: String,                   // ← currently selected friend UID
    displayNames: List<String>,           // e.g., usernames list
    uidToName: Map<String, String>,       // uid -> username
    nameToUid: Map<String, String>,       // username -> uid
    enabled: Boolean = true,
    keyReset: String,
    onFriendSelected: (String) -> Unit    // returns the selected UID
) {
    var expanded by remember(keyReset) { mutableStateOf(false) }

    // show mapped name if we have it; otherwise show the raw UID
    val selectedText = uidToName[currentUid] ?: currentUid

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded }
    ) {
        OutlinedTextField(
            readOnly = true,
            value = if (selectedText.isNotBlank()) selectedText else "",
            onValueChange = {},
            label = {
                Text(
                    text = labelText,     // ← your custom label (e.g., "Select friend")
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
            displayNames.forEachIndexed { idx, display ->
                DropdownMenuItem(onClick = {
                    expanded = false
                    // map display -> uid and return it
                    nameToUid[display]?.let(onFriendSelected)
                }) {
                    Text(
                        text = display,
                        color = theme.textColor,
                        style = MaterialTheme.typography.body1
                    )
                }
                if (idx < displayNames.lastIndex) {
                    Divider(color = theme.textColor.copy(alpha = 0.15f), thickness = 0.5.dp)
                }
            }
        }
    }
}
