package com.example.onlyone.views

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
    navController: NavController
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

    val offsetX = remember { Animatable(0f) }
    var isAnimating by remember { mutableStateOf(false) }


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
            modifier = Modifier
                .fillMaxWidth(),
            paddingBox1 = PaddingValues(1.dp),
            paddingBox2 = PaddingValues(1.dp),
            gradientColor1 =  Color(0xFF001F54).copy(alpha = 0.95f),
            gradientColor2 = Color(0xFF003366).copy(alpha = 0.95f),
            borderWidth = 1.dp,
            shape = RoundedCornerShape(24.dp),
            onDismiss = {}
        )
        {
            Column(
                modifier = Modifier
                    .fillMaxWidth().padding(top = 4.dp, bottom = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.chat_title),
                    style = MaterialTheme.typography.h6,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (isRandom && engagementStatus != null) {
                    val swipesUsed = engagementStatus!!.swipesUsed
                    val maxSwipes = user.maxSwipes
                    Text(
                        text = stringResource(R.string.chat_swipes, swipesUsed, maxSwipes),
                        style = MaterialTheme.typography.subtitle1,
                        color = Color.White
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        // Public Info Card
        // Public Info Card + Input + Send (all in one overlay and swipeable)
        CustomColorOverlay(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // this card grows to fill the screen area
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }, // hook for your swipe animation
            paddingBox1 = PaddingValues(1.dp),
            paddingBox2 = PaddingValues(1.dp),
            gradientColor1 = Color(0xFFF5DEB3).copy(alpha = 0.95f),
            gradientColor2 = Color(0xFFDAA520).copy(alpha = 0.95f),
            borderWidth = 1.dp,
            shape = RoundedCornerShape(24.dp),
            onDismiss = {}
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                // Top: target user header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = mapAvatarIdToDrawable(targetUser?.avatarId ?: 0)),
                        contentDescription = stringResource(R.string.chat_cd_avatar),
                        modifier = Modifier.size(48.dp).padding(end = 12.dp)
                    )
                    Column {
                        Text(
                            text = targetUser?.username ?: stringResource(R.string.chat_username_placeholder),
                            style = MaterialTheme.typography.subtitle1,
                            color = Color.White
                        )
                        Text(
                            text = targetUser?.moodStatus ?: "",
                            style = MaterialTheme.typography.body2,
                            color = Color.White
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Middle: message area (expands)
                // Middle: message area (expands)
                if (targetUser != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { if (it.length <= maxLength) messageText = it },
                            // no built-in label – we’ll draw it below instead
                            label = null,
                            placeholder = {
                                Text(
                                    text = stringResource(R.string.chat_input_label, messageText.length, maxLength),
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                backgroundColor = Color(0xCC1C1C1C),
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
                                cursorColor = Color.White,
                                textColor = Color.White
                            ),
                            maxLines = Int.MAX_VALUE
                        )

                        // Counter / helper text under the field
                        Text(
                            text = stringResource(R.string.chat_input_label, messageText.length, maxLength),
                            style = MaterialTheme.typography.caption,
                            color = Color.White.copy(alpha = 0.75f),
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .align(Alignment.End) // right-align under the box; change to Start/Center as you like
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
                                modifier = Modifier.size(160.dp).padding(bottom = 12.dp)
                            )
                            Text(stringResource(R.string.chat_searching), style = MaterialTheme.typography.body1)
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Bottom: send button
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
                            color = Color.White,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(stringResource(R.string.chat_send_button), fontSize = 18.sp)
                    }
                }
            }
        }

        // Language filter + next user
        if (isRandom) {
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
                            stringResource(
                                R.string.chat_language_prefix,
                                selectedLanguage.uppercase()
                            )
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
                                Text(lang.uppercase())
                            }
                        }
                    }
                }

                IconButton(
                    onClick = {
                        if (isAnimating) return@IconButton
                        coroutineScope.launch {
                            try {
                                isAnimating = true
                                // slide current card out to the right
                                offsetX.animateTo(
                                    targetValue = screenWidthPx,
                                    animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
                                )

                                // optional: clear input when moving to the next user
                                messageText = ""

                                // swap to next user
                                onNextUser()

                                // position new card off-screen left, then animate in
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
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}
