package com.onlyone.app.views.chainMessage

import CustomAlertDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.Button
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.onlyone.app.R
import com.onlyone.app.composables.CustomColorOverlay
import com.onlyone.app.theme.ThemeTokens
import com.onlyone.app.viewModels.userViewModel.UserViewModel
import com.onlyone.app.data.SavedChainEntity

@Composable
fun SavedChainsScreen(
    theme: ThemeTokens,
    userViewModel: UserViewModel,
    navController: NavHostController
) {
    val saved by userViewModel.chainManager.observeSavedChains()
        .collectAsState(initial = emptyList())

    var preview: SavedChainEntity? by remember { mutableStateOf(null) }

    CustomColorOverlay(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 32.dp, bottom = 32.dp, start = 16.dp, end = 16.dp),
        paddingBox1 = PaddingValues(12.dp),
        paddingBox2 = PaddingValues(12.dp),
        theme = theme,
        onDismiss = {}
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.saved_chains_title),
                style = MaterialTheme.typography.h6,
                color = theme.textColor
            )

            if (saved.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.saved_chains_empty), color = theme.textColor)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(saved, key = { it.chainId }) { item ->           // ← key by chainId
                        SavedRowCard(
                            theme = theme,
                            item = item,
                            onClick = { preview = item }
                        )
                    }
                }
            }
        }
    }

    // Detail dialog
    if (preview != null) {
        CustomAlertDialog(
            theme = theme,
            onDismiss = { preview = null }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = preview?.title?.takeIf { it.isNotBlank() }
                        ?: stringResource(R.string.common_untitled),
                    style = MaterialTheme.typography.h6,
                    color = theme.textColor
                )
                Text(
                    text = stringResource(R.string.saved_chains_created, preview!!.createdDay),
                    color = theme.textColor
                )
                Text(
                    text = stringResource(R.string.saved_chains_contributors, preview!!.contributorsCsv),
                    color = theme.textColor
                )
                Text(
                    text = preview!!.message,
                    color = theme.textColor,
                    style = MaterialTheme.typography.body2
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { preview = null },
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Text(stringResource(R.string.common_ok), color = theme.textColor)
                }
            }
        }
    }
}

@Composable
private fun SavedRowCard(
    theme: ThemeTokens,
    item: SavedChainEntity,
    onClick: () -> Unit
) {
    CustomColorOverlay(
        modifier = Modifier.fillMaxWidth(),
        paddingBox1 = PaddingValues(6.dp),
        paddingBox2 = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        theme = theme,
        onDismiss = {}
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
        ) {
            Text(
                text = item.title?.takeIf { it.isNotBlank() }
                    ?: stringResource(R.string.common_untitled),
                style = MaterialTheme.typography.body1,
                color = theme.textColor,
                maxLines = 1
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = item.createdDay,
                style = MaterialTheme.typography.caption,
                color = theme.textColor
            )
        }
    }
}
