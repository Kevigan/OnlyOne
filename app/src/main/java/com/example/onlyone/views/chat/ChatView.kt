package com.example.onlyone.views.chat

import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.example.onlyone.NativeAdGateCard
import com.example.onlyone.R
import com.example.onlyone.RewardedAds
import com.example.onlyone.composables.CustomColorOverlay
import com.example.onlyone.composables.mapAvatarIdToDrawable
import com.example.onlyone.data.Message
import com.example.onlyone.data.MessageResult
import com.example.onlyone.data.PublicUser
import com.example.onlyone.data.UserComposite
import com.example.onlyone.theme.ThemeTokens
import com.example.onlyone.utils.buildMessageId
import com.example.onlyone.viewModels.ChatViewModel
import com.example.onlyone.viewModels.userViewModel.UserViewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// Prewritten
import com.example.onlyone.prewritten.PrewrittenRepository
import com.example.onlyone.prewritten.PreMsgCategory
import com.example.onlyone.utils.LanguageCatalog
import com.example.onlyone.views.chat.components.PrewrittenPickerSheet
import com.example.onlyone.views.shopView.InfoDialog

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
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val buttonColors = ButtonDefaults.buttonColors(
        backgroundColor = theme.buttonBackgroundColor,
        contentColor = theme.textColor,
        disabledBackgroundColor = theme.disabledButtonBackground.copy(alpha = 0.4f),
        disabledContentColor = theme.cardContentColor.copy(alpha = 0.6f)
    )
    val buttonColorsSend = ButtonDefaults.buttonColors(
        backgroundColor = Color(0xFF81C784),
        contentColor = theme.textColor,
        disabledBackgroundColor = theme.disabledButtonBackground.copy(alpha = 0.4f),
        disabledContentColor = theme.cardContentColor.copy(alpha = 0.6f)
    )

    // ---- Suggestions Dialog State ----
    var showSuggestions by remember { mutableStateOf(false) }
    var selectedCat: PreMsgCategory? by rememberSaveable { mutableStateOf(null) } // null => All

    val messagesForDialog by remember(selectedCat, targetUser?.username, user.maxMessageLength) {
        mutableStateOf(
            if (selectedCat == null) {
                PrewrittenRepository.categories()
                    .flatMap {
                        PrewrittenRepository.messagesFor(
                            context = context,
                            category = it,
                            receiverName = targetUser?.username,
                            maxLength = Int.MAX_VALUE
                        )
                    }
                    .distinct()
            } else {
                PrewrittenRepository.messagesFor(
                    context = context,
                    category = selectedCat!!,
                    receiverName = targetUser?.username,
                    maxLength = Int.MAX_VALUE
                )
            }
        )
    }

    // ---- Chat State ----
    var messageText by rememberSaveable { mutableStateOf("") }
    val isSending by chatViewModel.isSending.collectAsState()
    val maxLength = user.maxMessageLength
    val engagementStatus by userViewModel.engagementStatus.collectAsState()

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }

    val offsetX = remember { Animatable(0f) }
    var isAnimating by remember { mutableStateOf(false) }

    // --- AD STATE ---
    val swipeCount by userViewModel.adCountToday.collectAsState(initial = 0)
    var adMode by rememberSaveable { mutableStateOf(false) }
    var lastAdGateCount by rememberSaveable { mutableStateOf(-1) }

    // --- Language filter state (uses shared catalog, includes "any")
    var selectedLanguage by remember { mutableStateOf("any") }
    LaunchedEffect(user.uid) {
        userViewModel.getSearchUserLanguage { savedLang -> selectedLanguage = savedLang }
    }
    val languageOptions = LanguageCatalog.chatOptionsWithAny()
    val displayName: @Composable (String) -> String = { code ->
        LanguageCatalog.fullName(code)
    }

    // --- Watch Ad to Reset Swipes Dialog ---
    var showResetDialog by rememberSaveable { mutableStateOf(false) }

    // If opening ChatView for random users and already at/over limit, show dialog.
    LaunchedEffect(isRandom, engagementStatus?.swipesUsed, user.maxSwipes) {
        val used = engagementStatus?.swipesUsed ?: 0
        if (isRandom && used >= user.maxSwipes) {
            showResetDialog = true
        }
    }

    // --- Info state ---
    var showSwipeInfoDialog by remember { mutableStateOf(false) }

    // ----------------- Screen -----------------
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
                engagementStatus?.let { es ->
                    if (isRandom) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.chat_swipes, es.swipesUsed, user.maxSwipes),
                                style = MaterialTheme.typography.subtitle1,
                                color = theme.textColor
                            )
                            Spacer(Modifier.width(8.dp))
                            IconButton(onClick = { showSwipeInfoDialog = true }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                                    contentDescription = stringResource(R.string.chat_swipe_info_cd),
                                    tint = Color(0xFFFFD700)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

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
                NativeAdGateCard(
                    theme = theme,
                    onContinue = { adMode = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                )
            } else {
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
                                ?: stringResource(R.string.chat_msg_no_fav)
                            Text(
                                text = "${stringResource(R.string.chat_msg_title)}: $favMsg",
                                style = MaterialTheme.typography.caption,
                                color = theme.textColor.copy(alpha = 0.8f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            val ageStr = targetUser?.age?.takeIf { it in 1..99 }?.toString() ?: "-"
                            val genderStr = when (targetUser?.gender?.trim()?.lowercase()) {
                                "m", "male" -> "M"
                                "f", "female" -> "F"
                                "d", "diverse", "other", "nonbinary", "non-binary", "x" -> "D"
                                else -> "-"
                            }

                            val cityStr = targetUser?.city?.takeIf { it.isNotBlank() } ?: "-"

                            val ageLabel = stringResource(R.string.onboarding_age_label)
                            val genderLabel = stringResource(R.string.onboarding_gender_label)
                            val cityLabel = stringResource(R.string.onboarding_city_label)

                            Text(
                                text = "$ageLabel: $ageStr   $genderLabel: $genderStr   $cityLabel: $cityStr",
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
                                onValueChange = { messageText = it },
                                label = null,
                                placeholder = { Text(stringResource(R.string.chat_placeholder), color = theme.textColor.copy(alpha = 0.6f)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    backgroundColor = theme.textFieldColor,
                                    focusedBorderColor = theme.textColor,
                                    unfocusedBorderColor = theme.textColor.copy(alpha = 0.6f),
                                    cursorColor = theme.textColor,
                                    textColor = theme.textColor
                                ),
                                maxLines = Int.MAX_VALUE
                            )

                            Text(
                                text = stringResource(R.string.chat_input_label, messageText.length, maxLength),
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
                            val trimmed = messageText.trim()
                            if (trimmed.isEmpty()) {
                                Toast.makeText(context, context.getString(R.string.chat_toast_empty), Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (trimmed.length > maxLength) {
                                Toast.makeText(context, context.getString(R.string.chat_toast_too_long), Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val receiverId = targetUser?.uid ?: return@Button
                            val messageId = buildMessageId(user.uid, receiverId)

                            val msg = Message(
                                id = messageId,
                                senderUsername = user.username,
                                senderId = user.uid,
                                receiverId = receiverId,
                                content = trimmed,
                                timestamp = Timestamp.now(),
                                senderAvatarId = user.avatarId,
                                senderMood = user.moodStatus,
                                feedback = -10
                            )

                            chatViewModel.sendMessage(msg, userViewModel = userViewModel) { result ->
                                when (result) {
                                    is MessageResult.Success -> {
                                        messageText = ""
                                        val rewardText = context.getString(R.string.chat_reward_text, result.gold)
                                        val runeText = result.rune?.let { "\n" + context.getString(R.string.chat_rune_drop, it) } ?: ""
                                        Toast.makeText(context, rewardText + runeText, Toast.LENGTH_LONG).show()
                                        navController.popBackStack()
                                    }
                                    is MessageResult.AlreadySent -> {
                                        Toast.makeText(context, context.getString(R.string.chat_already_sent), Toast.LENGTH_LONG).show()
                                    }
                                    is MessageResult.Error -> {
                                        Toast.makeText(context, context.getString(R.string.chat_send_error), Toast.LENGTH_LONG).show()
                                    }
                                    else -> {}
                                }
                            }
                        },
                        colors = buttonColorsSend,
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
        if (isRandom && !adMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                var expanded by remember { mutableStateOf(false) }

                // Language dropdown (shared catalog, + "any")
                Box {
                    OutlinedButton(onClick = { expanded = true }, colors = buttonColors) {
                        Text(
                            stringResource(R.string.chat_language_prefix, displayName(selectedLanguage)),
                            color = theme.textColor
                        )
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        languageOptions.forEach { (code, label) ->
                            DropdownMenuItem(
                                onClick = {
                                    selectedLanguage = code
                                    expanded = false
                                    userViewModel.saveSearchUserLanguage(code)
                                    coroutineScope.launch { userViewModel.loadRandomUserBatch(showToasts = true) }
                                }
                            ) { Text(label, color = theme.textColor) }
                        }
                    }
                }

                // Suggestions button -> opens dialog
                OutlinedButton(onClick = { showSuggestions = true }, colors = buttonColors) {
                    Text(text = stringResource(R.string.pre_msg_button), color = theme.textColor)
                }

                // Next user
                IconButton(
                    onClick = {
                        if (isAnimating) return@IconButton

                        val currentSwipes = engagementStatus?.swipesUsed ?: 0
                        val maxSwipes = user.maxSwipes
                        if (currentSwipes >= maxSwipes) {
                            // Show the reset dialog instead of just a toast
                            showResetDialog = true
                            return@IconButton
                        }

                        val nextCount = swipeCount + 1
                        if (nextCount % 10 == 0 && lastAdGateCount != nextCount) {
                            adMode = true
                            lastAdGateCount = nextCount
                            return@IconButton
                        }

                        coroutineScope.launch {
                            try {
                                isAnimating = true
                                offsetX.animateTo(
                                    targetValue = screenWidthPx,
                                    animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
                                )

                                messageText = ""

                                onNextUser()
                                userViewModel.onRandomCardVisible()

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
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp) // circle size
                            .background(color = theme.gradientColor1, shape = CircleShape), // round background
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = stringResource(R.string.chat_cd_next_user),
                            tint = Color.White, // contrast with background
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }

    // ---- Watch-ad-to-reset dialog ----
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.chat_out_of_swipes_title),
                    color = theme.textColor,
                    style = MaterialTheme.typography.h6
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.chat_out_of_swipes_body),
                    color = theme.textColor.copy(alpha = 0.9f)
                )
            },
            confirmButton = {
                val activity = context as? android.app.Activity
                Button(
                    onClick = {
                        if (activity == null) {
                            Toast.makeText(context, context.getString(R.string.chat_ad_unavailable), Toast.LENGTH_SHORT).show()
                            showResetDialog = false
                            return@Button
                        }
                        RewardedAds.show(
                            activity,
                            onReward = {
                                coroutineScope.launch {
                                    userViewModel.resetSwipesAfterAd(
                                        onSuccess = {
                                            Toast.makeText(context, context.getString(R.string.chat_swipes_reset_success), Toast.LENGTH_SHORT).show()
                                            showResetDialog = false
                                            coroutineScope.launch { userViewModel.loadRandomUserBatch(showToasts = true) }
                                        },
                                        onError = { msg ->
                                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                        }
                                    )
                                }
                            },
                            onClosed = { showResetDialog = false }
                        )
                    }
                ) {
                    Text(stringResource(R.string.chat_out_of_swipes_confirm), color = theme.textColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(stringResource(R.string.chat_out_of_swipes_later), color = theme.textColor)
                }
            },
            backgroundColor = Color(0xCC1C1C1C),
            contentColor = theme.textColor
        )
    }

    // ---- Prewritten suggestions dialog ----
    if (showSuggestions) {
        Dialog(
            onDismissRequest = { showSuggestions = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                Modifier
                    .fillMaxWidth(0.98f)
                    .widthIn(max = 720.dp)
            ) {
                CustomColorOverlay(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(percent = 4),
                    overlayColor = Color.Gray,
                    onDismiss = {},
                    paddingBox1 = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    paddingBox2 = PaddingValues(6.dp),
                    theme = theme
                ) {
                    PrewrittenPickerSheet(
                        theme = theme,
                        categories = PrewrittenRepository.categories(),
                        includeAllOption = true,
                        selected = selectedCat,                  // null => All
                        onSelectCategory = { selectedCat = it }, // set null for All
                        messages = messagesForDialog,
                        onInsert = { msg ->
                            messageText = msg
                            Toast.makeText(context, context.getString(R.string.pre_msg_copied), Toast.LENGTH_SHORT).show()
                            showSuggestions = false
                        }
                    )
                }
            }
        }
    }

    // ---- Info dialog ----
    if (showSwipeInfoDialog) {
        InfoDialog(
            title = stringResource(R.string.chat_swipe_info_title),
            message = stringResource(R.string.chat_swipe_info_body),
            theme = theme,
            onDismiss = { showSwipeInfoDialog = false }
        )
    }
}
