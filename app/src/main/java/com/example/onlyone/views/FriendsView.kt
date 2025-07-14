package com.example.onlyone.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.onlyone.R
import com.example.onlyone.composables.FriendItem

@Composable
fun FriendsView() {
    val fakeFriends = listOf(
        Triple("Alice Johnson", "Online", R.drawable.baseline_tag_faces_24),
        Triple("Bob Smith", "Away", R.drawable.baseline_tag_faces_24),
        Triple("Charlie Adams", "Busy", R.drawable.baseline_tag_faces_24),
        Triple("Diana Lee", "Online", R.drawable.baseline_tag_faces_24),
        Triple("Ethan Carter", "Offline", R.drawable.baseline_tag_faces_24),
        Triple("Alice Johnson", "Online", R.drawable.baseline_tag_faces_24),
        Triple("Bob Smith", "Away", R.drawable.baseline_tag_faces_24),
        Triple("Charlie Adams", "Busy", R.drawable.baseline_tag_faces_24),
        Triple("Diana Lee", "Online", R.drawable.baseline_tag_faces_24),
        Triple("Ethan Carter", "Offline", R.drawable.baseline_tag_faces_24)
        // add more...
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 32.dp, bottom = 16.dp)
    ) {
        Text(
            text = "Your Friends",
            style = MaterialTheme.typography.h5,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            items(fakeFriends) { (name, status, avatarResId) ->
                FriendItem(
                    name = name,
                    status = status,
                    avatarResId = avatarResId
                )
            }
        }
    }
}
