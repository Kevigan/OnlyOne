package com.example.onlyone.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.onlyone.R
import com.example.onlyone.data.LocalMessage
import com.example.onlyone.viewModels.ChatViewModel

@Composable
fun ReceivedMessageItemBig(
    chatViewModel: ChatViewModel,
    message: LocalMessage,
    onFeedbackSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    shape: Int = 45
) {
    LaunchedEffect(message.id) {
        chatViewModel.markMessageAsRead(message)
    }

    CustomColorOverlay(
        modifier = modifier,
        shape = RoundedCornerShape(shape),
        overlayColor = Color.Gray,
        onDismiss = {},
        paddingBox1 = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
        paddingBox2 = PaddingValues(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message.content,
                style = MaterialTheme.typography.body1,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .align(Alignment.CenterHorizontally)
            )
            if (message.feedback == -10) {
                Text(
                    text = "Give Feedback",
                    style = MaterialTheme.typography.subtitle1,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_thumb_up_off_alt_24),
                        contentDescription = "Thumb Up",
                        tint = Color.Green,
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { onFeedbackSelected(1) }
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_sentiment_neutral_24),
                        contentDescription = "Neutral",
                        tint = Color.Gray,
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { onFeedbackSelected(0) }
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_thumb_down_off_alt_24),
                        contentDescription = "Thumb Down",
                        tint = Color.Red,
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { onFeedbackSelected(-1) }
                    )
                }
            } else {
                // ✅ Show selected feedback
                val (iconId, tint, description) = when (message.feedback) {
                    1 -> Triple(R.drawable.baseline_thumb_up_off_alt_24, Color.Green, "You gave thumbs up")
                    0 -> Triple(R.drawable.baseline_sentiment_neutral_24, Color.Gray, "You gave neutral")
                    -1 -> Triple(R.drawable.baseline_thumb_down_off_alt_24, Color.Red, "You gave thumbs down")
                    else -> Triple(R.drawable.baseline_sentiment_neutral_24, Color.LightGray, "No Feedback given")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Icon(
                    painter = painterResource(id = iconId),
                    contentDescription = description,
                    tint = tint,
                    modifier = Modifier.size(36.dp)
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.body2,
                    color = Color.White,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}


