package com.example.onlyone.cloudMessaging

import kotlinx.coroutines.flow.MutableSharedFlow

object MessageNotifier {
    val newMessageFlow = MutableSharedFlow<Pair<String?, String?>>(
        replay = 1, // ✅ keep last message
        extraBufferCapacity = 1 // ✅ allow one uncollected emit
    )
}

