package com.onlyone.app.views.chainMessage

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.onlyone.app.R
import com.onlyone.app.Screen
import com.onlyone.app.composables.CustomColorOverlay
import com.onlyone.app.data.chainMessage.Chain
import com.onlyone.app.theme.ThemeTokens
import com.onlyone.app.viewModels.userViewModel.UserViewModel
import java.util.concurrent.TimeUnit

@Composable
fun ChainsView(
    theme: ThemeTokens,
    userViewModel: UserViewModel,
    navController: NavHostController
) {
    val myChains by userViewModel.chainManager.myChains.collectAsState(emptyList())
    val me by userViewModel.user.observeAsState()

    if (me == null && myChains.isEmpty()) {
        LaunchedEffect(Unit) {
            Log.d("ChainsView", "⏳ Loading — me=${me?.uid ?: "null"}, myChains=${myChains.size}")
        }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val nowMs = System.currentTimeMillis()
    val ttlMs = TimeUnit.HOURS.toMillis(24)

    // Helper: compute the effective end time for a chain
    fun effectiveEndMs(c: Chain): Long {
        val createdMs = c.createdAt?.toDate()?.time ?: 0L
        val deleteMs = c.deleteAt?.toDate()?.time ?: (if (createdMs > 0) createdMs + ttlMs else Long.MAX_VALUE)
        val expireMs = c.expiresAt?.toDate()?.time ?: Long.MAX_VALUE
        return minOf(deleteMs, expireMs)
    }

    // Show chains created within last 24h
    val cutoffMs = remember { nowMs - ttlMs }
    val recentChains = remember(myChains, nowMs) {
        myChains
            .filter { c -> (c.createdAt?.toDate()?.time ?: Long.MIN_VALUE) >= cutoffMs }
            .sortedByDescending { it.createdAt?.toDate()?.time ?: 0L }
    }

    CustomColorOverlay(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 32.dp, bottom = 32.dp),
        paddingBox1 = PaddingValues(12.dp),
        paddingBox2 = PaddingValues(12.dp),
        gradientColor1 = theme.gradientColor1.copy(alpha = 0.95f),
        gradientColor2 = theme.gradientColor2.copy(alpha = 0.95f),
        borderWidth = 1.dp,
        shape = RoundedCornerShape(24.dp),
        theme = theme,
        onDismiss = {}
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (recentChains.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.chains_recent_header),
                        style = MaterialTheme.typography.subtitle1,
                        color = theme.textColor
                    )
                }
                items(recentChains, key = { it.id }) { chain ->
                    val currentStep = (chain.lastStepIndex + 1).coerceAtLeast(0)
                    val endMs = effectiveEndMs(chain)
                    val leftMs = (endMs - nowMs).coerceAtLeast(0L)

                    val stateLabel = when (chain.state) {
                        "open" -> stringResource(R.string.chain_state_open)
                        "complete" -> stringResource(R.string.chain_state_complete)
                        "expired" -> stringResource(R.string.chain_state_expired)
                        else -> chain.state
                    }

                    val timeLeftText = if (leftMs > 0L) {
                        val h = TimeUnit.MILLISECONDS.toHours(leftMs)
                        val m = TimeUnit.MILLISECONDS.toMinutes(leftMs) % 60
                        if (h > 0) {
                            stringResource(R.string.chain_time_left_hm, h.toInt(), m.toInt())
                        } else {
                            stringResource(R.string.chain_time_left_m, m.toInt())
                        }
                    } else {
                        stringResource(R.string.chain_time_left_zero)
                    }

                    val subtitle = stringResource(
                        R.string.chain_subtitle_format,
                        currentStep,
                        chain.targetLength,
                        stateLabel,
                        timeLeftText
                    )


                    ChainRowItem(
                        theme = theme,
                        chainTitle = chain.title ?: stringResource(R.string.common_untitled),
                        subtitle = subtitle,
                        chainState = chain.state, // pass state down
                        onClick = { navController.navigate(Screen.ChainDetailScreen.createRoute(chain.id)) }
                    )
                }
            } else {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = stringResource(R.string.chains_empty_title), color = theme.textColor)
                        Spacer(Modifier.height(8.dp))
                        Text(text = stringResource(R.string.chains_empty_hint), color = theme.textColor)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChainRowItem(
    theme: ThemeTokens,
    chainTitle: String,
    subtitle: String,
    chainState: String,
    onClick: () -> Unit
) {
    // state-based color (only gradientColor1 changes)
    val startColor = remember(chainState) {
        when (chainState) {
            "open" -> Color(0xFF66BB6A)      // green 400
            "complete" -> Color(0xFF42A5F5)  // blue 400
            "expired" -> Color(0xFFFFB74D)   // orange 300
            else -> Color(0xFF9E9E9E)        // grey fallback
        }.copy(alpha = 0.9f)
    }

    ChainRowOverlay(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        paddingBox1 = PaddingValues(0.dp),
        paddingBox2 = PaddingValues(0.dp),
        gradientColor1 = startColor,                     // only this varies by state
        gradientColor2 = theme.gradientColor2.copy(alpha = 0.9f),
        borderWidth = 1.dp,
        shape = RoundedCornerShape(16.dp),
        theme = theme,
        onDismiss = {}
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(chainTitle, style = MaterialTheme.typography.body1, color = theme.textColor, maxLines = 1)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, style = MaterialTheme.typography.caption, color = theme.textColor)
        }
    }
}

@Composable
fun ChainRowOverlay(
    modifier: Modifier = Modifier,
    theme: ThemeTokens,
    overlayColor: Color = Color.Transparent,
    borderColor: Color =  Color(0xFF80DFFF),
    gradientColor1: Color = Color(0xFF353535).copy(alpha = 0.95f),
    gradientColor2: Color = Color(0xFF1F1F1F).copy(alpha = 0.95f),
    borderWidth : Dp = 0.1.dp,
    shape: Shape = RoundedCornerShape(32.dp),
    paddingBox1: PaddingValues = PaddingValues(horizontal = 4.dp),
    paddingBox2: PaddingValues = PaddingValues(12.dp),
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(paddingBox1)
            .border(
                width = borderWidth,
                color = theme.borderColor.copy(alpha = 0.6f),
                shape = shape
            )
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = shape,
            elevation = 8.dp,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(gradientColor1, gradientColor2)
                        ),
                        shape = shape
                    )
                    .padding(paddingBox2)
            ) {
                content()
            }
        }
    }
}
