package com.example.onlyone.views.achievements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.onlyone.R
import com.example.onlyone.viewModels.userViewModel.UserViewModel

@Composable
fun AchievementsView(
    viewModel: UserViewModel = hiltViewModel()
) {
    val isLoading = remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        viewModel.loadAchievementsWithStats()
        isLoading.value = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            stringResource(R.string.achv_title),
            style = MaterialTheme.typography.h4,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (isLoading.value) {
            CircularProgressIndicator()
        } else {
            val grouped by viewModel.groupedAchievements.observeAsState()
            if (grouped.isNullOrEmpty()) {
                Text(
                    stringResource(R.string.achv_empty),
                    color = Color.Red
                )
            }

            grouped?.forEach { (type, achievements) ->
                Text(
                    stringResource(R.string.achv_section, type),
                    style = MaterialTheme.typography.subtitle1
                )
                /*LazyRow {
                    items(achievements) { ach ->
                        AchievementCard(ach)
                    }
                }*/
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp), // space between items
                    contentPadding = PaddingValues(horizontal = 8.dp)    // optional: padding at start/end
                ) {
                    items(achievements) { a ->
                        AchievementCardGradient(
                            achievement = a,
                            width = 160.dp,
                            height = 200.dp,
                            shape = RoundedCornerShape(20) // tweak freely
                        )
                    }
                }
            }
        }
    }
}
