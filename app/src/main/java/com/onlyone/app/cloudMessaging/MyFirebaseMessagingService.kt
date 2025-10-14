package com.onlyone.app.cloudMessaging

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.onlyone.app.R
import com.onlyone.app.dao.UserSettingsDao
import com.onlyone.app.repos.ChatRepository
import com.onlyone.app.repos.UserInventoryRepo
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject lateinit var userInventoryRepo: UserInventoryRepo
    @Inject lateinit var userSettingsDao: UserSettingsDao
    @Inject lateinit var firestore: FirebaseFirestore
    @Inject lateinit var chatRepository: ChatRepository

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        Log.d("FCM_OO", "📨 onMessageReceived data=${remoteMessage.data} notif=${remoteMessage.notification != null}")

        val rawType = remoteMessage.data["type"]
        val type = when (rawType) {
            "friend_request", "friendRequest" -> "friend_request"
            else -> rawType
        }

        when (type) {
            "friend_request" -> {
                Log.d("FCM_OO", "➡️ handleFriendRequest()")
                handleFriendRequest(remoteMessage)
            }
            else -> {
                if (remoteMessage.notification != null) {
                    Log.d("FCM_OO", "➡️ handleMessageNotification() (no friend_request type)")
                    handleMessageNotification(remoteMessage, uid)
                } else {
                    Log.w("FCM_OO", "❓ Unknown payload (no type + no notification). data=${remoteMessage.data}")
                }
            }
        }
    }


    // -------------------------
    // Message notification path
    // -------------------------
    private fun handleMessageNotification(remoteMessage: RemoteMessage, uid: String) {
        val notif = remoteMessage.notification ?: return

        CoroutineScope(Dispatchers.IO).launch {
            val settings = userSettingsDao.getSettings()
            val messageAllowed = settings?.notifyMessages ?: true
            if (!messageAllowed) {
                Log.d("FCM", "🔕 message notifications disabled — skipping UI")
                return@launch
            }

            // Optional side work (kept from your original code)
            val inventory = userInventoryRepo.fetchInventory(uid)
            val publicDoc = firestore.collection("users_public").document(uid).get().await()
            val points = (publicDoc.get("points") as? Number)?.toInt() ?: 0
            Log.d("FCM", "📥 Inventory updated — gold=${inventory?.gold}, points=$points")

            withContext(Dispatchers.Main) {
                if (isAppInForeground(this@MyFirebaseMessagingService)) {
                    // In-app banner/snackbar flow for messages
                    MessageNotifier.newMessageFlow.tryEmit(notif.title to notif.body)
                } else {
                    showNotification(notif.title, notif.body)
                }
            }
        }
    }

    // ----------------------------
    // Friend request notification
    // ----------------------------
    private fun handleFriendRequest(remoteMessage: RemoteMessage) {
        val fromUid = remoteMessage.data["fromUid"].orEmpty()
        val fromUsername = remoteMessage.data["fromUsername"]

        CoroutineScope(Dispatchers.IO).launch {
            val settings = userSettingsDao.getSettings()
            val friendReqAllowed = settings?.notifyFriendRequests ?: true
            if (!friendReqAllowed) {
                Log.d("FCM_OO", "🔕 friend request notifications disabled — skipping UI")
                return@launch
            }

            // Build localized strings ONCE here
            val title = getString(R.string.friend_request_title)
            val body = if (!fromUsername.isNullOrBlank())
                getString(R.string.friend_request_body_named, fromUsername)
            else
                getString(R.string.friend_request_generic)

            withContext(Dispatchers.Main) {
                val inFg = isAppInForeground(this@MyFirebaseMessagingService)
                Log.d(
                    "FCM_OO",
                    "👀 FriendRequest: inForeground=$inFg, fromUid=$fromUid, fromUsername=$fromUsername"
                )

                if (inFg) {
                    // In-app path: emit already-localized message for direct display in UI
                    FriendRequestNotifier.newFriendRequestFlow.tryEmit(
                        FriendRequestEvent(
                            fromUid = fromUid,
                            fromUsername = fromUsername,
                            message = body // <- NEW: ready-to-show localized text
                        )
                    )
                    Log.d("FCM_OO", "📣 Emitted in-app friend request event (no system notification in foreground).")
                } else {
                    // Background path: show tray notification
                    Log.d("FCM_OO", "🔔 Background: about to showNotification title=\"$title\" body=\"$body\"")
                    showNotification(title, body)
                }
            }
        }
    }


    // -------------------------
    // Helpers
    // -------------------------
    private fun isAppInForeground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        val packageName = context.packageName
        for (appProcess in appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                appProcess.processName == packageName
            ) {
                return true
            }
        }
        return false
    }

    private fun showNotification(title: String?, body: String?) {
        val notificationManager = NotificationManagerCompat.from(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                Log.w("FCM_OO", "Notification permission not granted — skipping notification")
                return
            }
        }

        // 🧭 Debug log showing what will be displayed
        Log.d("FCM_OO", "🔔 showNotification() -> title=\"$title\" body=\"$body\"")

        val builder = NotificationCompat.Builder(this, "onlyone_channel")
            .setSmallIcon(R.drawable.ghosthead_happy) // ensure this icon exists
            .setContentTitle(title ?: "OnlyOne")
            .setContentText(body ?: "")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    override fun onNewToken(token: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users_private")
            .document(uid)
            .update("fcmToken", token)
            .addOnFailureListener { e -> Log.e("FCM_OO", "Failed to update fcmToken", e) }
    }
}
