package com.onlyone.app.ads

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.onlyone.app.RewardedAds

@Composable
fun TestVideoAdButton() {
    val activity = LocalContext.current as android.app.Activity
    Button(onClick = {
        RewardedAds.show(
            activity,
            onReward = { /* optional: handle reward during testing */ },
            onClosed = { /* optional: after video closes */ }
        )
    }) {
        Text("Play test video ad")
    }
}
