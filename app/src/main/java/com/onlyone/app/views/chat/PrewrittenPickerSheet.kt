package com.onlyone.app.views.chat.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.onlyone.app.R
import com.onlyone.app.prewritten.PreMsgCategory
import com.onlyone.app.theme.ThemeTokens
import com.onlyone.app.composables.CustomColorOverlay

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PrewrittenPickerSheet(
    theme: ThemeTokens,
    categories: List<PreMsgCategory>,
    includeAllOption: Boolean = true,
    selected: PreMsgCategory?,                // null => All
    onSelectCategory: (PreMsgCategory?) -> Unit,
    messages: List<String>,
    onInsert: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Header row with Title + Exposed dropdown (works in Dialog)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.pre_msg_title),
                style = MaterialTheme.typography.h6,
                color = theme.textColor,
                modifier = Modifier.padding(8.dp)
            )

            var expanded by remember { mutableStateOf(false) }
            val currentLabel = when (selected) {
                null -> stringResource(R.string.pre_msg_all_tab) // All
                else -> stringResource(selected.labelRes)        // Generic / Supportive / ...
            }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = currentLabel,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = theme.textColor,
                        focusedBorderColor = theme.textColor,
                        unfocusedBorderColor = theme.textColor.copy(alpha = 0.6f),
                        cursorColor = theme.textColor
                    ),
                    modifier = Modifier.widthIn(min = 160.dp) // no menuAnchor() needed
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    if (includeAllOption) {
                        DropdownMenuItem(onClick = {
                            onSelectCategory(null); expanded = false
                        }) {
                            Text(stringResource(R.string.pre_msg_all_tab), color = theme.textColor)
                        }
                    }
                    categories.forEach { cat ->
                        DropdownMenuItem(onClick = {
                            onSelectCategory(cat)
                            expanded = false
                        }) {
                            Text(text = stringResource(cat.labelRes), color = theme.textColor)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Scrollable list (All -> long lists)
        val listState = rememberLazyListState()
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 160.dp, max = 560.dp) // taller + scrollable
                .padding(bottom = 8.dp),
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.pre_msg_empty),
                        color = theme.textColor,
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else {
                items(messages) { msg ->
                    // Gradient wrapper per message
                    CustomColorOverlay(
                        modifier = Modifier.fillMaxWidth(),
                        paddingBox1 = PaddingValues(1.dp),
                        paddingBox2 = PaddingValues(12.dp),
                        gradientColor1 = theme.gradientColor1.copy(alpha = 0.95f),
                        gradientColor2 = theme.gradientColor2.copy(alpha = 0.95f),
                        borderWidth = 1.dp,
                        shape = RoundedCornerShape(20.dp),
                        theme = theme,
                        onDismiss = {}
                    ) {
                        // Row: left = message (wraps), right = button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = msg,
                                color = theme.textColor,
                                style = MaterialTheme.typography.body2,
                                modifier = Modifier
                                    .weight(1f)           // take remaining space
                                    .padding(end = 8.dp),  // breathing room from button
                                maxLines = Int.MAX_VALUE,
                                softWrap = true,
                                overflow = TextOverflow.Clip
                            )
                            TextButton(onClick = { onInsert(msg) }) {
                                Text(
                                    text = stringResource(R.string.pre_msg_insert),
                                    color = theme.textColor,
                                    style = MaterialTheme.typography.body2
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
