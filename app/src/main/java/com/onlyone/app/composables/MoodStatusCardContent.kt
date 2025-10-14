package com.onlyone.app.composables

import CustomAlertDialog
import MoodPickerDialog
import com.onlyone.app.data.UserComposite
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
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
import com.onlyone.app.R
import com.onlyone.app.composables.mapMoodIdToDrawable
import com.onlyone.app.theme.ThemeTokens

@Composable
fun MoodStatusCardContent(
    theme: ThemeTokens,
    user: UserComposite?,
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

    var showEditDialog by remember { mutableStateOf(false) }
    var showPickerDialog by remember { mutableStateOf(false) }
    var editedMood by remember(moodStatus, maxMoodLength) { mutableStateOf(moodStatus.take(maxMoodLength)) }

    // === Main Row: Column (Mood text + icon) | Mood text area ===
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        // 1) Left: Column with header ("Mood" + edit icon) and the mood icon
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .clickable(enabled = ownedMoodIds.isNotEmpty()) { showPickerDialog = true }
                .padding(horizontal = 1.dp, vertical = 1.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.common_mood),
                        style = MaterialTheme.typography.caption,
                        color = theme.textColor.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_edit_24),
                        contentDescription = null,
                        tint = theme.textColor.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(Modifier.height(6.dp))

                Image(
                    painter = painterResource(id = moodAvatarResId),
                    contentDescription = stringResource(R.string.profile_cd_current_avatar),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        // 2) Right: Tappable area for mood text (expands to fill remaining width)
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp)) // rounded box instead of circle
                .background(Color.White.copy(alpha = 0.08f))
                .clickable { showEditDialog = true } // whole card is tappable
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End // puts the icon to the top-right
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_edit_24),
                    contentDescription = null,
                    tint = theme.textColor.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )

                Spacer(Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 80.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    val isEmpty = moodStatus.isBlank()
                    Text(
                        text = if (isEmpty)
                            stringResource(R.string.profile_mood_empty_hint)
                        else
                            moodStatus,
                        color = if (isEmpty)
                            theme.textColor.copy(alpha = 0.5f)
                        else
                            theme.textColor.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.caption,
                        maxLines = 2
                    )
                }
            }
        }
    }


    // === Edit Mood Dialog ===
    if (showEditDialog) {
        CustomAlertDialog(
            theme = theme,
            onDismiss = { showEditDialog = false }
        ) {
            Column(Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.profile_mood_dialog_title),
                    style = MaterialTheme.typography.h6,
                    color = theme.textColor
                )
                Spacer(Modifier.height(12.dp))
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
                Text(
                    text = "${editedMood.length} / $maxMoodLength",
                    style = MaterialTheme.typography.caption,
                    color = theme.textColor.copy(alpha = 0.75f),
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .align(Alignment.End)
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        editedMood = moodStatus.take(maxMoodLength)
                        showEditDialog = false
                    }) { Text(stringResource(R.string.profile_cancel), color = theme.textColor) }

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

    // === Mood Icon Picker ===
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
