package com.example.onlyone.utils

import com.example.onlyone.data.LocalFriend
import com.example.onlyone.data.PublicUser

fun LocalFriend.toPublicUser(): PublicUser = PublicUser(
    uid = uid,
    username = username,
    moodStatus = moodStatus,
    avatarId = avatarId,
    points = points
)
