// com.onlyone.app.cloudMessaging/FriendRequestNotifier.kt
package com.onlyone.app.cloudMessaging

import kotlinx.coroutines.flow.MutableSharedFlow

data class FriendRequestEvent(
    val fromUid: String,
    val fromUsername: String?,
    val message: String?
)

object FriendRequestNotifier {
    val newFriendRequestFlow = MutableSharedFlow<FriendRequestEvent>(
        replay = 1,
        extraBufferCapacity = 1
    )
}
