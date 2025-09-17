package com.example.onlyone.composables

import CustomAlertDialog
import MoodPickerDialog
import com.example.onlyone.data.UserComposite
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.onlyone.R
import com.example.onlyone.theme.ThemeTokens

@Composable
fun MoodStatusCardContent(
    theme: ThemeTokens,
    user: UserComposite?,                 // <- nullable now
    onMoodSubmit: (String) -> Unit,
    onMoodIconSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val maxMoodLength = user?.maxMoodLength ?: 25
    val moodStatus = user?.moodStatus.orEmpty()
    val currentMoodId = user?.moodId
    val ownedMoodIds = user?.ownedMoods ?: emptyList()

    val moodAvatarResId = remember(currentMoodId) {
        currentMoodId?.let { mapMoodIdToDrawable(it) } ?: R.drawable.baseline_tag_faces_24
    }

    var showEditDialog by remember { mutableStateOf(false) }   // edit mood text
    var showPickerDialog by remember { mutableStateOf(false) } // pick owned icon
    var editedMood by remember(moodStatus, maxMoodLength) { mutableStateOf(moodStatus.take(maxMoodLength)) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Image(
            painter = painterResource(id = moodAvatarResId),
            contentDescription = stringResource(R.string.profile_cd_current_avatar),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .clickable(enabled = ownedMoodIds.isNotEmpty()) { showPickerDialog = true }
        )

        Text(
            text = moodStatus,
            color = theme.textColor.copy(alpha = 0.8f),
            style = MaterialTheme.typography.caption,
            modifier = Modifier.clickable { showEditDialog = true }
        )
    }

    // Edit mood text
    if (showEditDialog) {
        CustomAlertDialog(
            theme = theme,
            onDismiss = { showEditDialog = false }
        ) {
            Column(Modifier.fillMaxWidth()) {
                // Title
                Text(
                    text = stringResource(R.string.profile_mood_dialog_title),
                    style = MaterialTheme.typography.h6,
                    color = theme.textColor
                )

                Spacer(Modifier.height(12.dp))

                // Input
                OutlinedTextField(
                    value = editedMood,
                    onValueChange = { editedMood = it.take(maxMoodLength) },
                    placeholder = { Text(stringResource(R.string.profile_mood_placeholder)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = theme.textColor,
                        cursorColor = theme.borderColor,
                        focusedBorderColor = theme.borderColor,
                        unfocusedBorderColor = theme.cardContentColor.copy(alpha = 0.5f),
                        focusedLabelColor = theme.textColor,
                        unfocusedLabelColor = theme.textColor.copy(alpha = 0.8f),
                        placeholderColor = theme.cardContentColor.copy(alpha = 0.6f)
                    )
                )

                // Counter
                Text(
                    text = "${editedMood.length} / $maxMoodLength",
                    style = MaterialTheme.typography.caption,
                    color = theme.textColor.copy(alpha = 0.75f),
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .align(Alignment.End)
                )

                Spacer(Modifier.height(16.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        editedMood = moodStatus.take(maxMoodLength)
                        showEditDialog = false
                    }) {
                        Text(stringResource(R.string.profile_cancel), color = theme.textColor)
                    }

                    Spacer(Modifier.width(8.dp))

                    val trimmed = editedMood.trim()
                    val canSave = trimmed.isNotEmpty() && trimmed != moodStatus
                    TextButton(
                        onClick = {
                            onMoodSubmit(trimmed)
                            showEditDialog = false
                        },
                        enabled = canSave
                    ) {
                        Text(stringResource(R.string.profile_save), color = theme.textColor)
                    }
                }
            }
        }
    }

    // Owned-only picker
    if (showPickerDialog) {
        MoodPickerDialog(
            ownedMoodIds = ownedMoodIds,
            currentMoodId = currentMoodId,
            onSelect = { id -> onMoodIconSelected(id) },
            onDismiss = { showPickerDialog = false },
            theme = theme
        )
    }
}
