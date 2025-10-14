package com.onlyone.app.composables

import androidx.annotation.DrawableRes
import androidx.annotation.IntRange
import androidx.compose.runtime.Immutable
import com.onlyone.app.R


enum class AvatarCategory(val labelRes: Int) {
    ALL(R.string.shop_avatars_category_all),
    GHOSTS(R.string.shop_avatars_category_ghosts),
    ALIENS(R.string.shop_avatars_category_aliens),
    Humans(R.string.shop_avatars_category_humans),
}

@Immutable
data class AvatarItem(
    val id: Int,
    @DrawableRes val imageRes: Int,
    val cost: Int,
    val category: AvatarCategory
)

object AvatarCatalog {
    val avatars = listOf(
        // Ghosts
        AvatarItem(id = 1, imageRes = R.drawable.ghosthead_happy, cost = 0,   category = AvatarCategory.GHOSTS),
        AvatarItem(id = 2, imageRes = R.drawable.ghosthead_angry, cost = 0,   category = AvatarCategory.GHOSTS),

        // Aliens (placeholder category for the rest; re-tag as you add real alien assets)
        AvatarItem(id = 3, imageRes = R.drawable.female_avatar_1, cost = 500, category = AvatarCategory.ALIENS),
        AvatarItem(id = 4, imageRes = R.drawable.male_avatar_1,   cost = 500, category = AvatarCategory.ALIENS),
        AvatarItem(id = 5, imageRes = R.drawable.female_avatar_2, cost = 500, category = AvatarCategory.ALIENS),
        AvatarItem(id = 6, imageRes = R.drawable.male_avatar_2,   cost = 500, category = AvatarCategory.ALIENS),
        AvatarItem(id = 7, imageRes = R.drawable.female_avatar_3, cost = 500, category = AvatarCategory.ALIENS),
        AvatarItem(id = 8, imageRes = R.drawable.male_avatar_3,   cost = 500, category = AvatarCategory.ALIENS),
        AvatarItem(id = 9,  imageRes = R.drawable.female_avatar_4, cost = 500, category = AvatarCategory.ALIENS),
        AvatarItem(id = 10, imageRes = R.drawable.male_avatar_4,   cost = 500, category = AvatarCategory.ALIENS),
        AvatarItem(id = 11, imageRes = R.drawable.female_avatar_5, cost = 500, category = AvatarCategory.ALIENS),
        AvatarItem(id = 12, imageRes = R.drawable.male_avatar_5,   cost = 500, category = AvatarCategory.ALIENS),
        AvatarItem(id = 13, imageRes = R.drawable.female_avatar_6, cost = 500, category = AvatarCategory.ALIENS),
        AvatarItem(id = 14, imageRes = R.drawable.male_avatar_6,   cost = 500, category = AvatarCategory.ALIENS),
        AvatarItem(id = 15, imageRes = R.drawable.female_avatar_7, cost = 500, category = AvatarCategory.ALIENS),
        AvatarItem(id = 16, imageRes = R.drawable.male_avatar_7,   cost = 500, category = AvatarCategory.ALIENS),
        AvatarItem(id = 17, imageRes = R.drawable.female_avatar_8, cost = 500, category = AvatarCategory.ALIENS),
        AvatarItem(id = 18, imageRes = R.drawable.male_avatar_8,   cost = 500, category = AvatarCategory.ALIENS),
        AvatarItem(id = 19, imageRes = R.drawable.female_avatar_9, cost = 500, category = AvatarCategory.ALIENS),
        AvatarItem(id = 20, imageRes = R.drawable.male_avatar_9,   cost = 500, category = AvatarCategory.ALIENS),
        AvatarItem(id = 21, imageRes = R.drawable.female_avatar_10, cost = 500, category = AvatarCategory.ALIENS),
        AvatarItem(id = 22, imageRes = R.drawable.male_avatar_10,   cost = 500, category = AvatarCategory.ALIENS),

        //Humans
        AvatarItem(id = 23, imageRes = R.drawable.human_avatar_1, cost = 500, category = AvatarCategory.Humans),
        AvatarItem(id = 24, imageRes = R.drawable.human_avatar_2,   cost = 500, category = AvatarCategory.Humans),
        AvatarItem(id = 25, imageRes = R.drawable.human_avatar_3, cost = 500, category = AvatarCategory.Humans),
        AvatarItem(id = 26, imageRes = R.drawable.human_avatar_4,   cost = 500, category = AvatarCategory.Humans),
        AvatarItem(id = 27, imageRes = R.drawable.human_avatar_5, cost = 500, category = AvatarCategory.Humans),
        AvatarItem(id = 28, imageRes = R.drawable.human_avatar_6,   cost = 500, category = AvatarCategory.Humans),
        AvatarItem(id = 29, imageRes = R.drawable.human_avatar_7, cost = 500, category = AvatarCategory.Humans),
        AvatarItem(id = 30, imageRes = R.drawable.human_avatar_8,   cost = 500, category = AvatarCategory.Humans),
        AvatarItem(id = 31, imageRes = R.drawable.human_avatar_9, cost = 500, category = AvatarCategory.Humans),
        AvatarItem(id = 32, imageRes = R.drawable.human_avatar_10,   cost = 500, category = AvatarCategory.Humans),
        AvatarItem(id = 33, imageRes = R.drawable.human_avatar_11, cost = 500, category = AvatarCategory.Humans),
        AvatarItem(id = 34, imageRes = R.drawable.human_avatar_12,   cost = 500, category = AvatarCategory.Humans),
        AvatarItem(id = 35, imageRes = R.drawable.human_avatar_13, cost = 500, category = AvatarCategory.Humans),
        AvatarItem(id = 36, imageRes = R.drawable.human_avatar_14,   cost = 500, category = AvatarCategory.Humans),
        AvatarItem(id = 37, imageRes = R.drawable.human_avatar_15, cost = 500, category = AvatarCategory.Humans),
        AvatarItem(id = 38, imageRes = R.drawable.human_avatar_16,   cost = 500, category = AvatarCategory.Humans),
        AvatarItem(id = 39, imageRes = R.drawable.human_avatar_17, cost = 500, category = AvatarCategory.Humans),
        AvatarItem(id = 40, imageRes = R.drawable.human_avatar_18,   cost = 500, category = AvatarCategory.Humans),
        AvatarItem(id = 41, imageRes = R.drawable.human_avatar_19, cost = 500, category = AvatarCategory.Humans),
        AvatarItem(id = 42, imageRes = R.drawable.human_avatar_20,   cost = 500, category = AvatarCategory.Humans),
    )

    val idToRes: Map<Int, Int> = avatars.associate { it.id to it.imageRes }
}

@DrawableRes
fun mapAvatarIdToDrawable(@IntRange(from = 0) avatarId: Int): Int {
    return AvatarCatalog.idToRes[avatarId] ?: R.drawable.ghosthead
}

@DrawableRes
fun mapAvatarIdToDrawable(avatarId: Int?): Int {
    return AvatarCatalog.idToRes[avatarId] ?: R.drawable.ghosthead
}


// ===== Add to ShopUtiles.kt =====

enum class MoodCategory(val labelRes: Int) {
    ALL(R.string.shop_moods_category_all),
    ACTIVITIES(R.string.shop_moods_category_activities),
    LOVE(R.string.shop_moods_category_love)
}

@Immutable
data class MoodItem(
    val id: Int,
    @DrawableRes val imageRes: Int,
    val cost: Int,
    val category: MoodCategory
)

object MoodCatalog {
    val moods = listOf(
        // Activities
        MoodItem(id = 3,  imageRes = R.drawable.mood_icon_activities_3, cost = 500, category = MoodCategory.ACTIVITIES),
        MoodItem(id = 20, imageRes = R.drawable.new_mood_icons_2,  cost = 500, category = MoodCategory.ACTIVITIES),
        MoodItem(id = 21, imageRes = R.drawable.new_mood_icons_3,  cost = 500, category = MoodCategory.ACTIVITIES),
        MoodItem(id = 22, imageRes = R.drawable.new_mood_icons_4,  cost = 500, category = MoodCategory.ACTIVITIES),
        MoodItem(id = 23, imageRes = R.drawable.new_mood_icons_5,  cost = 500, category = MoodCategory.ACTIVITIES),
        MoodItem(id = 24, imageRes = R.drawable.new_mood_icons_6,  cost = 500, category = MoodCategory.ACTIVITIES),
        MoodItem(id = 25, imageRes = R.drawable.new_mood_icons_7,  cost = 500, category = MoodCategory.ACTIVITIES),
        MoodItem(id = 26, imageRes = R.drawable.new_mood_icons_8,  cost = 500, category = MoodCategory.ACTIVITIES),
        MoodItem(id = 27, imageRes = R.drawable.new_mood_icons_9,  cost = 500, category = MoodCategory.ACTIVITIES),
        MoodItem(id = 28, imageRes = R.drawable.new_mood_icons_10, cost = 500, category = MoodCategory.ACTIVITIES),
        MoodItem(id = 29, imageRes = R.drawable.new_mood_icons_11, cost = 500, category = MoodCategory.ACTIVITIES),
        MoodItem(id = 30, imageRes = R.drawable.new_mood_icons_12, cost = 500, category = MoodCategory.ACTIVITIES),
        MoodItem(id = 31, imageRes = R.drawable.new_mood_icons_13, cost = 500, category = MoodCategory.ACTIVITIES),
        MoodItem(id = 32, imageRes = R.drawable.new_mood_icons_14, cost = 500, category = MoodCategory.ACTIVITIES),
        MoodItem(id = 33, imageRes = R.drawable.new_mood_icons_15, cost = 500, category = MoodCategory.ACTIVITIES),
        MoodItem(id = 34, imageRes = R.drawable.new_mood_icons_16, cost = 500, category = MoodCategory.ACTIVITIES),
        MoodItem(id = 35, imageRes = R.drawable.new_mood_icons_17, cost = 500, category = MoodCategory.ACTIVITIES),
        MoodItem(id = 36, imageRes = R.drawable.new_mood_icons_18, cost = 500, category = MoodCategory.ACTIVITIES),
        MoodItem(id = 37, imageRes = R.drawable.new_mood_icons_19, cost = 500, category = MoodCategory.ACTIVITIES),

        // Love & Affection
        MoodItem(id = 4,  imageRes = R.drawable.mood_icon_love_affection_1,  cost = 500, category = MoodCategory.LOVE),
        MoodItem(id = 6,  imageRes = R.drawable.mood_icon_love_affection_2,  cost = 500, category = MoodCategory.LOVE),
        MoodItem(id = 7,  imageRes = R.drawable.mood_icon_love_affection_3,  cost = 500, category = MoodCategory.LOVE),
        MoodItem(id = 8,  imageRes = R.drawable.mood_icon_love_affection_4,  cost = 500, category = MoodCategory.LOVE),
        MoodItem(id = 9,  imageRes = R.drawable.mood_icon_love_affection_5,  cost = 500, category = MoodCategory.LOVE),
        MoodItem(id = 10, imageRes = R.drawable.mood_icon_love_affection_6,  cost = 500, category = MoodCategory.LOVE),
        MoodItem(id = 11, imageRes = R.drawable.mood_icon_love_affection_7,  cost = 500, category = MoodCategory.LOVE),
        MoodItem(id = 12, imageRes = R.drawable.mood_icon_love_affection_8,  cost = 500, category = MoodCategory.LOVE),
        MoodItem(id = 13, imageRes = R.drawable.mood_icon_love_affection_9,  cost = 500, category = MoodCategory.LOVE),
        MoodItem(id = 14, imageRes = R.drawable.mood_icon_love_affection_10, cost = 500, category = MoodCategory.LOVE),
        MoodItem(id = 15, imageRes = R.drawable.mood_icon_love_affection_11, cost = 500, category = MoodCategory.LOVE),
        MoodItem(id = 16, imageRes = R.drawable.mood_icon_love_affection_12, cost = 500, category = MoodCategory.LOVE),
        MoodItem(id = 17, imageRes = R.drawable.mood_icon_love_affection_13, cost = 500, category = MoodCategory.LOVE),
        MoodItem(id = 18, imageRes = R.drawable.mood_icon_love_affection_14, cost = 500, category = MoodCategory.LOVE),
        MoodItem(id = 19, imageRes = R.drawable.mood_icon_love_affection_15, cost = 500, category = MoodCategory.LOVE),
    )

    // Fast lookup map
    val idToRes: Map<Int, Int> = moods.associate { it.id to it.imageRes }
}


/** Safe mapping with fallback. */
@DrawableRes
fun mapMoodIdToDrawable(moodId: Int): Int {
    return MoodCatalog.idToRes[moodId] ?: R.drawable.ic_launcher_foreground
}

/** Owned-only helper (unchanged API) */
fun ownedMoodItems(ownedIds: Collection<Int>): List<MoodItem> {
    if (ownedIds.isEmpty()) return emptyList()
    val ownedSet = ownedIds.toSet()
    return MoodCatalog.moods.filter { it.id in ownedSet }
}



