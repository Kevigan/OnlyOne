package com.example.onlyone.utils

import com.example.onlyone.data.LocalFriend
import com.example.onlyone.data.PublicUser

fun LocalFriend.toPublicUser(): PublicUser {
    return PublicUser(
        uid = uid,
        username = username,
        moodStatus = moodStatus,
        chatLanguage = "any",
        avatarId = avatarId,
        points = points
    )
}

