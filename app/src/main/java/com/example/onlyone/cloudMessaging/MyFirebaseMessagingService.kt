package com.example.onlyone.cloudMessaging

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.example.dao.UserSettingsDao
import com.example.onlyone.R
import com.example.onlyone.repos.AppDatabase
import com.example.onlyone.repos.UserInventoryRepo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {
    @Inject
    lateinit var userInventoryRepo: UserInventoryRepo
    @Inject lateinit var userSettingsDao: UserSettingsDao
    @Inject lateinit var firestore: FirebaseFirestore
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.let {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

            // 🔄 Check local Room settings before showing
            val db = Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java,
                "onlyone_db"
            ).build()

            val dao = db.userSettingsDao()

            CoroutineScope(Dispatchers.IO).launch {
                val settings = dao.getSettings(uid)

                val messageAllowed = settings?.notifyMessages ?: true
                if (!messageAllowed) {
                    Log.d("FCM", "🔕 Local setting: message notifications disabled — skipping")
                    return@launch
                }
                // ✅ Fetch inventory
                val inventory = userInventoryRepo.fetchInventory(uid)

                // ✅ Fetch points from users_public
                val publicDoc = firestore.collection("users_public").document(uid).get().await()
                val points = (publicDoc.get("points") as? Number)?.toInt() ?: 0

                Log.d("FCM", "📥 Updated inventory after feedback — gold=${inventory?.gold}, points=$points")


                withContext(Dispatchers.Main) {
                    if (isAppInForeground(this@MyFirebaseMessagingService)) {
                        MessageNotifier.newMessageFlow.tryEmit(it.title to it.body)
                    } else {
                        showNotification(it.title, it.body)
                    }
                }
            }
        }
    }


    // Helper to check foreground state
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
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                Log.w("FCM", "Notification permission not granted — skipping notification")
                return
            }
        }

        val builder = NotificationCompat.Builder(this, "onlyone_channel")
            .setSmallIcon(R.drawable.ghosthead_happy) // ensure this icon exists
            .setContentTitle(title ?: "OnlyOne")
            .setContentText(body ?: "")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    override fun onNewToken(token: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            FirebaseFirestore.getInstance()
                .collection("users_private")
                .document(uid)
                .update("fcmToken", token)
        }
    }
}
