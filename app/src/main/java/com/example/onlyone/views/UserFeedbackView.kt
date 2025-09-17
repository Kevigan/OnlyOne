package com.example.onlyone.views.feedback

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.onlyone.R
import com.example.onlyone.composables.CustomColorOverlay
import com.example.onlyone.theme.ThemeTokens
import com.example.onlyone.viewModels.userViewModel.UserViewModel
import kotlinx.coroutines.launch

// --- Model kept in Kotlin (IDs only; labels via string resources) ---
private data class FbOption(val id: String, val labelRes: Int)
private data class FbQuestion(
    val id: String,
    val labelRes: Int,
    val options: List<FbOption>,
    val required: Boolean = true
)

// Plain (non-composable) function: returns IDs only.
private fun feedbackQuestions(): List<FbQuestion> = listOf(
    FbQuestion(
        id = "q1",
        labelRes = R.string.feedback_q1_label,
        options = listOf(
            FbOption("a", R.string.feedback_q1_opt_a),
            FbOption("b", R.string.feedback_q1_opt_b),
            FbOption("c", R.string.feedback_q1_opt_c),
        )
    ),
    FbQuestion(
        id = "q2",
        labelRes = R.string.feedback_q2_label,
        options = listOf(
            FbOption("a", R.string.feedback_q2_opt_a),
            FbOption("b", R.string.feedback_q2_opt_b),
            FbOption("c", R.string.feedback_q2_opt_c),
            FbOption("d", R.string.feedback_q2_opt_d),
        )
    ),
    FbQuestion(
        id = "q3",
        labelRes = R.string.feedback_q3_label,
        options = listOf(
            FbOption("a", R.string.feedback_q3_opt_a), // Yes
            FbOption("b", R.string.feedback_q3_opt_b), // No
        )
    )
)

@Composable
fun UserFeedbackView(
    navController: NavController,
    userViewModel: UserViewModel,
    theme: ThemeTokens
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Build questions once
    val questions = remember { feedbackQuestions() }

    // UI state
    var selections by remember { mutableStateOf(mutableMapOf<String, String>()) }
    var text by remember { mutableStateOf("") }
    val maxLen = 500

    val feedbackState by userViewModel.feedbackState.collectAsState()

    // Strings captured in composable scope
    val sentToast = stringResource(R.string.feedback_sent_toast)
    val alreadyToast = stringResource(R.string.feedback_already_submitted_toast)
    val errorDefaultToast = stringResource(R.string.feedback_error_toast)

    // React to terminal states
    LaunchedEffect(feedbackState) {
        when (feedbackState) {
            is UserViewModel.FeedbackUiState.Success -> {
                Toast.makeText(context, sentToast, Toast.LENGTH_LONG).show()
                userViewModel.clearFeedbackState()
                navController.popBackStack()
            }
            is UserViewModel.FeedbackUiState.AlreadySubmitted -> {
                Toast.makeText(context, alreadyToast, Toast.LENGTH_LONG).show()
                userViewModel.clearFeedbackState()
            }
            is UserViewModel.FeedbackUiState.Error -> {
                val msg = (feedbackState as UserViewModel.FeedbackUiState.Error).message
                Toast.makeText(context, if (msg.isBlank()) errorDefaultToast else msg, Toast.LENGTH_LONG).show()
                userViewModel.clearFeedbackState()
            }
            else -> Unit
        }
    }

    val isSending = feedbackState is UserViewModel.FeedbackUiState.Sending
    val scrollState = rememberScrollState()

    // Insets
    val navBars = WindowInsets.navigationBars.asPaddingValues()
    val statusBars = WindowInsets.statusBars.asPaddingValues()
    val bottomInset = navBars.calculateBottomPadding()
    val topInset = statusBars.calculateTopPadding()

    // Root column so weight() applies to the overlay
    Column(
        modifier = Modifier
            .fillMaxSize()
            // give extra space at the top so we don't overlap system status bar
            .padding(top = topInset + 16.dp, start = 12.dp, end = 12.dp, bottom = 0.dp)
    ) {
        CustomColorOverlay(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f),
            theme = theme,
            onDismiss = {}
        ) {
            // Overlay content box so we can anchor the button to bottom
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding() // keep content above keyboard
                    .padding(12.dp)
            ) {
                // SCROLLABLE CONTENT
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        // leave room so bottom button + system bar won't overlap content
                        .padding(bottom = 88.dp + bottomInset),
                    horizontalAlignment = Alignment.Start
                ) {
                    // Title (same style as LoginView)
                    Text(
                        text = stringResource(R.string.feedback_title),
                        style = MaterialTheme.typography.h4,
                        color = theme.textColor
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.feedback_subtitle),
                        style = MaterialTheme.typography.subtitle1,
                        color = theme.textColor.copy(alpha = 0.85f)
                    )

                    Spacer(Modifier.height(16.dp))

                    // Questions
                    questions.forEach { q ->
                        QuestionBlock(
                            question = q,
                            selected = selections[q.id],
                            onSelect = { choice ->
                                selections = selections.toMutableMap().apply { put(q.id, choice) }
                            },
                            theme = theme
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    // Free text
                    Text(
                        text = stringResource(R.string.feedback_free_text_label),
                        style = MaterialTheme.typography.h6,
                        color = theme.textColor
                    )
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = text,
                        onValueChange = { if (it.length <= maxLen) text = it },
                        label = { Text(stringResource(R.string.feedback_free_text_placeholder)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 160.dp)
                            .heightIn(min = 160.dp, max = 260.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = theme.textColor,
                            cursorColor = theme.textColor,
                            focusedBorderColor = theme.textColor,
                            unfocusedBorderColor = theme.textColor.copy(alpha = 0.6f)
                        ),
                        minLines = 5,
                        maxLines = 10
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Text(
                            "${text.length}/$maxLen",
                            style = MaterialTheme.typography.caption,
                            color = theme.textColor.copy(alpha = 0.7f)
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                }

                // FIXED BOTTOM SEND BUTTON
                Button(
                    onClick = {
                        val missing = questions.filter { it.required && selections[it.id].isNullOrBlank() }
                        if (missing.isNotEmpty()) {
                            Toast.makeText(context, context.getString(R.string.feedback_form_incomplete), Toast.LENGTH_LONG).show()
                            return@Button
                        }
                        scope.launch {
                            userViewModel.sendFeedback(
                                answers = selections.toMap(),
                                text = text
                            )
                        }
                    },
                    enabled = !isSending,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        // space for the nav bar + a little breathing room
                        .padding(bottom = bottomInset + 12.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isSending) {
                        CircularProgressIndicator(
                            color = theme.textColor,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.feedback_submit),
                            style = MaterialTheme.typography.button.copy(fontWeight = FontWeight.SemiBold),
                            color = theme.textColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestionBlock(
    question: FbQuestion,
    selected: String?,
    onSelect: (String) -> Unit,
    theme: ThemeTokens
) {
    Column {
        Text(
            text = stringResource(question.labelRes),
            style = MaterialTheme.typography.h6,
            color = theme.textColor
        )
        Spacer(Modifier.height(6.dp))
        question.options.forEach { opt ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            ) {
                RadioButton(
                    selected = selected == opt.id,
                    onClick = { onSelect(opt.id) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = theme.textColor,
                        unselectedColor = theme.textColor.copy(alpha = 0.6f),
                        disabledColor = theme.textColor.copy(alpha = 0.3f)
                    )
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(opt.labelRes),
                    style = MaterialTheme.typography.body1,
                    color = theme.textColor
                )
            }
        }
    }
}
