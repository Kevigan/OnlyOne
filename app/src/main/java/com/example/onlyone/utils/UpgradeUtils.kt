package com.example.onlyone.utils

data class UpgradeCost(
    val gold: Int = 0,
    val runesRare: Int = 0,
    val runesSuperRare: Int = 0,
    val runesMegaRare: Int = 0
)

val upgradeSteps = mapOf(
    "maxMoments" to 25,
    "maxSwipes" to 25,
    "maxMessageLength" to 25,
    "maxMoodLength" to 25,   // ⭐ new
    "maxAdsPerDay" to 1
)

val upgradeCosts = mapOf(
    "maxMoments" to listOf(
        mapOf("gold" to 40),
        mapOf("gold" to 40),
        mapOf("gold" to 60, "runes_rare" to 1),
        mapOf("gold" to 60, "runes_rare" to 1),
        mapOf("gold" to 80, "runes_super_rare" to 1),
        mapOf("gold" to 100, "runes_mega_rare" to 1)
    ),
    "maxSwipes" to listOf(
        mapOf("gold" to 30),
        mapOf("gold" to 30),
        mapOf("gold" to 50, "runes_rare" to 1),
        mapOf("gold" to 50, "runes_rare" to 1),
        mapOf("gold" to 70, "runes_super_rare" to 1),
        mapOf("gold" to 90, "runes_mega_rare" to 1)
    ),
    "maxMessageLength" to listOf(
        mapOf("gold" to 50),
        mapOf("gold" to 50),
        mapOf("gold" to 70, "runes_rare" to 1),
        mapOf("gold" to 80, "runes_super_rare" to 1),
        mapOf("gold" to 100, "runes_mega_rare" to 1),
        mapOf("gold" to 120, "runes_mega_rare" to 2)
    ),
    "maxMoodLength" to listOf(              // ⭐ new
        mapOf("gold" to 50),
        mapOf("gold" to 50),
        mapOf("gold" to 70, "runes_rare" to 1),
        mapOf("gold" to 80, "runes_super_rare" to 1),
        mapOf("gold" to 100, "runes_mega_rare" to 1),
        mapOf("gold" to 120, "runes_mega_rare" to 2)
    ),
    "maxAdsPerDay" to listOf(
        mapOf("gold" to 100),
        mapOf("gold" to 120, "runes_rare" to 1),
        mapOf("gold" to 150, "runes_super_rare" to 1)
    )
)


fun calculateUpgradeCost(feature: String, currentValue: Int, levels: Int): UpgradeCost {
    val stepSize = upgradeSteps[feature] ?: return UpgradeCost()
    val costArray = upgradeCosts[feature] ?: return UpgradeCost()

    val currentLevel = currentValue / stepSize

    var gold = 0
    var runesRare = 0
    var runesSuperRare = 0
    var runesMegaRare = 0

    for (i in currentLevel until currentLevel + levels) {
        val cost = costArray.getOrNull(i) ?: continue
        gold += cost["gold"] ?: 0
        runesRare += cost["runes_rare"] ?: 0
        runesSuperRare += cost["runes_super_rare"] ?: 0
        runesMegaRare += cost["runes_mega_rare"] ?: 0
    }

    return UpgradeCost(gold, runesRare, runesSuperRare, runesMegaRare)
}

