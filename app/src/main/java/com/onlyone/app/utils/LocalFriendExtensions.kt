package com.onlyone.app.utils

import com.onlyone.app.data.LocalFriend
import com.onlyone.app.data.PublicUser

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

