@file:Suppress("unused")

package com.onlyone.ui.onboarding

import CustomAlertDialog
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.onlyone.app.R
import com.onlyone.app.theme.ThemeTokens

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingView(
    theme: ThemeTokens,
    onDismiss: () -> Unit
) {
    // 🚫 Block system back (gesture / nav bar)
    BackHandler(enabled = true) { /* consume back */ }

    // strings from resources (composable-safe)
    val step1 = stringResource(R.string.onboarding_step1)
    val step2 = stringResource(R.string.onboarding_step2)
    val step3 = stringResource(R.string.onboarding_step3)
    val title = stringResource(R.string.onboarding_getting_started_title)
    val nextLabel = stringResource(R.string.onboarding_next)
    val finishLabel = stringResource(R.string.onboarding_finish)

    val steps = listOf(step1, step2, step3)

    var stepIndex by rememberSaveable { mutableStateOf(0) }
    val total = steps.size
    val isLast = stepIndex == total - 1
    val progress = (stepIndex + 1f) / total.toFloat()

    CustomAlertDialog(
        theme = theme,
        borderColor = theme.borderColor,
        onDismiss = { /* no-op: only our button may finish */ }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = theme.textColor
                )
                Text(
                    text = "${stepIndex + 1} / $total",
                    style = MaterialTheme.typography.labelLarge,
                    color = theme.textColor
                )
            }

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth().height(6.dp)
            )

            AnimatedContent(
                targetState = stepIndex,
                transitionSpec = { (fadeIn(animationSpec = spring()) togetherWith fadeOut(animationSpec = spring())) },
                label = "OnboardingStep"
            ) { idx ->
                Text(
                    text = steps[idx],
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth(),
                    color = theme.textColor
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = {
                    if (isLast) onDismiss() else stepIndex++
                }) {
                    Text(text = if (isLast) finishLabel else nextLabel, color = theme.textColor)
                }
            }
        }
    }
}
