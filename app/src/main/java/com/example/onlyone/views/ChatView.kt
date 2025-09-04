package com.example.onlyone.views

import NativeAdGateCard
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.onlyone.R
import com.example.onlyone.composables.CustomColorOverlay
import com.example.onlyone.composables.mapAvatarIdToDrawable
import com.example.onlyone.data.PublicUser
import com.example.onlyone.data.Message
import com.example.onlyone.data.MessageResult
import com.example.onlyone.data.UserComposite
import com.example.onlyone.theme.ThemeTokens
import com.example.onlyone.viewModels.ChatViewModel
import com.example.onlyone.utils.buildMessageId
import com.example.onlyone.viewModels.userViewModel.UserViewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun ChatView(
    user: UserComposite,
    targetUser: PublicUser?,
    isRandom: Boolean,
    onNextUser: () -> Unit,
    chatViewModel: ChatViewModel,
    userViewModel: UserViewModel,
    navController: NavController,
    theme: ThemeTokens
) {
    var messageText by remember { mutableStateOf("") }
    val context = LocalContext.current
    val isSending by chatViewModel.isSending.collectAsState()
    val maxLength = user.maxMessageLength
    val coroutineScope = rememberCoroutineScope()
    val engagementStatus by userViewModel.engagementStatus.collectAsState()

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }

    val anyLabel = stringResource(R.string.chat_language_any)

    val displayName: (String) -> String = { code ->
        when (code) {
            "any" -> anyLabel
            "en"  -> "EN"
            "de"  -> "DE"
            "fr"  -> "FR"
            "es"  -> "ES"
            "it"  -> "IT"
            else  -> code.uppercase()
        }
    }

    val offsetX = remember { Animatable(0f) }
    var isAnimating by remember { mutableStateOf(false) }

    // --- AD STATE (new) ---
    // Persisted swipe count (today) from AdCounter
    val swipeCount by userViewModel.adCountToday.collectAsState(initial = 0)
    // Local "interstitial" state: showing ad instead of a user
    var adMode by rememberSaveable { mutableStateOf(false) }
    // Prevent showing the same gate repeatedly at the same threshold (10, 20, 30…)
    var lastAdGateCount by rememberSaveable { mutableStateOf(-1) }

    var selectedLanguage by remember { mutableStateOf("any") }
    LaunchedEffect(user.uid) {
        userViewModel.getSearchUserLanguage { savedLang -> selectedLanguage = savedLang }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 36.dp, bottom = 16.dp, start = 4.dp, end = 4.dp)
    ) {
        // Header
        CustomColorOverlay(
            modifier = Modifier.fillMaxWidth(),
            paddingBox1 = PaddingValues(1.dp),
            paddingBox2 = PaddingValues(1.dp),
            gradientColor1 = Color(0xFF001F54).copy(alpha = 0.95f),
            gradientColor2 = Color(0xFF003366).copy(alpha = 0.95f),
            borderWidth = 1.dp,
            shape = RoundedCornerShape(24.dp),
            theme = theme,
            onDismiss = {}
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.chat_title),
                    style = MaterialTheme.typography.h6,
                    color = theme.textColor,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (isRandom && engagementStatus != null) {
                    val swipesUsed = engagementStatus!!.swipesUsed
                    val maxSwipes = user.maxSwipes
                    Text(
                        text = stringResource(R.string.chat_swipes, swipesUsed, maxSwipes),
                        style = MaterialTheme.typography.subtitle1,
                        color = theme.textColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Main card (either USER content or AD-only content)
        CustomColorOverlay(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .offset { IntOffset(offsetX.value.roundToInt(), 0) },
            paddingBox1 = PaddingValues(1.dp),
            paddingBox2 = PaddingValues(1.dp),
            gradientColor1 = Color(0xFFF5DEB3).copy(alpha = 0.95f),
            gradientColor2 = Color(0xFFDAA520).copy(alpha = 0.95f),
            borderWidth = 1.dp,
            shape = RoundedCornerShape(24.dp),
            theme = theme,
            onDismiss = {}
        ) {
            if (adMode) {
                // ---------------- AD-ONLY CONTENT ----------------
                NativeAdGateCard(
                    theme = theme,
                    onContinue = { adMode = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                )
            } else {
                // ---------------- NORMAL USER CONTENT ----------------
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    // User header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = mapAvatarIdToDrawable(targetUser?.avatarId ?: 0)),
                            contentDescription = stringResource(R.string.chat_cd_avatar),
                            modifier = Modifier
                                .size(48.dp)
                                .padding(end = 12.dp)
                        )
                        Column {
                            Text(
                                text = targetUser?.username ?: stringResource(R.string.chat_username_placeholder),
                                style = MaterialTheme.typography.subtitle1,
                                color = theme.textColor
                            )
                            Text(
                                text = targetUser?.moodStatus ?: "",
                                style = MaterialTheme.typography.body2,
                                color = theme.textColor
                            )
                            val achievementCount = targetUser?.achievementCount ?: 0
                            Text(
                                text = "${stringResource(R.string.achv_title)}: $achievementCount",
                                style = MaterialTheme.typography.caption,
                                color = theme.textColor.copy(alpha = 0.8f)
                            )
                            val favMsg = targetUser?.favouriteMessage?.text
                                ?.takeIf { it.isNotBlank() }
                                ?:stringResource(R.string.chat_msg_no_fav)
                            Text(
                                text = "${stringResource(R.string.chat_msg_title)}: $favMsg",
                                style = MaterialTheme.typography.caption,
                                color = theme.textColor.copy(alpha = 0.8f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Message area
                    if (targetUser != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            OutlinedTextField(
                                value = messageText,
                                onValueChange = { if (it.length <= maxLength) messageText = it },
                                label = null,
                                placeholder = {
                                    Text(
                                        text = stringResource(
                                            R.string.chat_input_label,
                                            messageText.length,
                                            maxLength
                                        ),
                                        color = theme.textColor.copy(alpha = 0.6f)
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    backgroundColor = Color(0xCC1C1C1C),
                                    focusedBorderColor = theme.textColor,
                                    unfocusedBorderColor = theme.textColor.copy(alpha = 0.6f),
                                    cursorColor = theme.textColor,
                                    textColor = theme.textColor
                                ),
                                maxLines = Int.MAX_VALUE
                            )

                            Text(
                                text = stringResource(
                                    R.string.chat_input_label,
                                    messageText.length,
                                    maxLength
                                ),
                                style = MaterialTheme.typography.caption,
                                color = theme.textColor.copy(alpha = 0.75f),
                                modifier = Modifier
                                    .padding(top = 6.dp)
                                    .align(Alignment.End)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Image(
                                    painter = painterResource(id = R.drawable.ghosthead_sad),
                                    contentDescription = stringResource(R.string.chat_cd_waiting),
                                    modifier = Modifier
                                        .size(160.dp)
                                        .padding(bottom = 12.dp)
                                )
                                Text(
                                    stringResource(R.string.chat_searching),
                                    style = MaterialTheme.typography.body1,
                                    color = theme.textColor
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Send button
                    Button(
                        onClick = {
                            if (messageText.trim().length > user.maxMessageLength) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.chat_toast_too_long),
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }

                            val messageId = buildMessageId(user.uid, targetUser?.uid ?: "")
                            val msg = Message(
                                id = messageId,
                                senderUsername = user.username,
                                senderId = user.uid,
                                receiverId = targetUser?.uid ?: "",
                                content = messageText.trim(),
                                timestamp = Timestamp.now(),
                                senderAvatarId = user.avatarId,
                                senderMood = user.moodStatus,
                                feedback = -10
                            )

                            chatViewModel.sendMessage(msg, userViewModel = userViewModel) { result ->
                                when (result) {
                                    is MessageResult.Success -> {
                                        messageText = ""
                                        val rewardText = context.getString(
                                            R.string.chat_reward_text,
                                            result.gold,
                                            result.points
                                        )
                                        val runeText = result.rune?.let {
                                            "\n" + context.getString(R.string.chat_rune_drop, it)
                                        } ?: ""
                                        Toast.makeText(context, rewardText + runeText, Toast.LENGTH_LONG).show()
                                        navController.popBackStack()
                                    }
                                    is MessageResult.AlreadySent -> {
                                        Toast.makeText(context, context.getString(R.string.chat_already_sent), Toast.LENGTH_LONG).show()
                                    }
                                    is MessageResult.Error -> {
                                        Toast.makeText(context, context.getString(R.string.chat_send_error), Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        },
                        enabled = targetUser != null && messageText.isNotBlank() && !isSending && !isAnimating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(
                                color = theme.textColor,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(stringResource(R.string.chat_send_button), fontSize = 18.sp, color = theme.textColor)
                        }
                    }
                }
            }
        }

        // Bottom controls
        if (isRandom) {
            if (adMode) {
                // While ad is shown: no language/next; show a single "Continue" action
                Button(
                    onClick = { adMode = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .height(48.dp)
                ) {
                    Text(stringResource(R.string.ad_btn_continue), color = theme.textColor, fontSize = 16.sp)
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    var expanded by remember { mutableStateOf(false) }
                    val languageOptions = listOf("any", "en", "de", "fr", "es", "it")

                    Box {
                        OutlinedButton(onClick = { expanded = true }) {
                            Text(
                                stringResource(R.string.chat_language_prefix, displayName(selectedLanguage)),
                                color = theme.textColor
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            languageOptions.forEach { lang ->
                                DropdownMenuItem(
                                    onClick = {
                                        selectedLanguage = lang
                                        expanded = false
                                        userViewModel.saveSearchUserLanguage(lang)
                                        coroutineScope.launch {
                                            userViewModel.loadRandomUserBatch(showToasts = true)
                                        }
                                    }
                                ) {
                                    Text(displayName(lang), color = theme.textColor)
                                }
                            }
                        }
                    }


                    IconButton(
                        onClick = {
                            if (isAnimating) return@IconButton
                            // Will the NEXT swipe be an ad slot?
                            val nextCount = swipeCount + 1
                            if (nextCount % 10 == 0 && lastAdGateCount != nextCount) {
                                // Show ad now, do NOT consume a user or increment counter
                                adMode = true
                                lastAdGateCount = nextCount
                                return@IconButton
                            }

                            // Otherwise proceed to next user and increment swipe counter
                            coroutineScope.launch {
                                try {
                                    isAnimating = true
                                    offsetX.animateTo(
                                        targetValue = screenWidthPx,
                                        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
                                    )

                                    messageText = ""

                                    onNextUser() // consume exactly one random user
                                    userViewModel.onRandomCardVisible() // increment persisted swipe count

                                    offsetX.snapTo(-screenWidthPx)
                                    offsetX.animateTo(
                                        targetValue = 0f,
                                        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
                                    )
                                } finally {
                                    isAnimating = false
                                }
                            }
                        },
                        enabled = !isAnimating && targetUser != null,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = stringResource(R.string.chat_cd_next_user),
                            tint = theme.textColor,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}

/** Full-card ad placeholder (no user shown). */
@Composable
fun AdInterstitialCard(
    theme: ThemeTokens,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(
            text = "Sponsored",
            style = MaterialTheme.typography.caption,
            color = theme.textColor.copy(alpha = 0.85f)
        )
        Spacer(Modifier.height(8.dp))

        // Big media (16:9) to mirror native ad requirements
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(Color(0xFF1A1A1A), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("Ad Image 16:9", color = Color.White.copy(alpha = 0.8f))
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Make your one message legendary.",
            style = MaterialTheme.typography.h6,
            color = theme.textColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "Sponsored • example.com",
            style = MaterialTheme.typography.caption,
            color = theme.textColor.copy(alpha = 0.75f)
        )

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = { /* TODO: open link */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Learn more", color = theme.textColor)
        }
    }
}
