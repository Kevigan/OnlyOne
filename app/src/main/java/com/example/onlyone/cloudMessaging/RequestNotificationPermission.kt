package com.example.onlyone.cloudMessaging

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun RequestNotificationPermission() {
    val context = LocalContext.current
    Log.d("NotificationPermission", "Composable entered")
    Log.d("NotificationPermission", "SDK = ${Build.VERSION.SDK_INT}")

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                if (isGranted) {
                    Toast.makeText(context, "Notifications enabled. Have fun.", Toast.LENGTH_LONG).show()
                    // Permission granted
                } else {
                    // Permission denied
                    Toast.makeText(context, "Notifications disabled. You can enable them in settings.", Toast.LENGTH_LONG).show()
                }
            }
        )

        LaunchedEffect(Unit) {
            Log.d("NotificationPermission", "LaunchedEffect running")
            val permissionStatus = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
