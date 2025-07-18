package com.example.onlyone.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun MoodStatusCardContent(
    moodStatus: String,
    avatarResId: Int,
    onMoodSubmit: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var editedMood by remember { mutableStateOf(moodStatus) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Image(
            painter = painterResource(id = avatarResId),
            contentDescription = "Current Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .clickable { showDialog = true }
        )

        Text(
            text = "Mood: $moodStatus",
            color = Color.White.copy(alpha = 0.8f),
            style = MaterialTheme.typography.caption
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Update Mood Status") },
            text = {
                OutlinedTextField(
                    value = editedMood,
                    onValueChange = { editedMood = it },
                    placeholder = { Text("Enter mood...") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onMoodSubmit(editedMood)
                    showDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                editedMood = moodStatus
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
