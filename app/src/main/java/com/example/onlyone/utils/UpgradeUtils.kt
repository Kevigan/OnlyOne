package com.example.onlyone.utils

// com/example/onlyone/utils/UpgradeUtils.kt

data class UpgradeCost(
    val gold: Int = 0,
    val runesRare: Int = 0,
    val runesSuperRare: Int = 0,
    val runesMegaRare: Int = 0
)

/** ---- Server-matching definitions ---- */
object UpgradeDefs {
    // Bases (level 0 → absolute value)
    val base = mapOf(
        "maxMoments" to 0,
        "maxSwipes" to 25,
        "maxMessageLength" to 75,
        "maxMoodLength" to 60,
        "maxAdsPerDay" to 0
    )

    // ✅ Step per level — MUST match Cloud Function
    val step = mapOf(
        "maxMoments" to 25,
        "maxSwipes" to 2,   // << fix: was 5, must be 2
        "maxMessageLength" to 5,
        "maxMoodLength" to 5,
        "maxAdsPerDay" to 1
    )

    private fun makeFifteen(): List<UpgradeCost> =
        (1..15).map { i ->
            val gold = 250 + (i - 1) * 25  // 250..600 (+25)
            val rare = when {
                i <= 8  -> 0
                i <= 10 -> 1
                i <= 12 -> 2
                i == 13 -> 3
                i == 14 -> 4
                else    -> 5
            }
            UpgradeCost(gold = gold, runesRare = rare)
        }

    // Per-level costs: index 0 = cost from level 0 → 1
    val costs: Map<String, List<UpgradeCost>> = mapOf(
        "maxMoments" to listOf(
            UpgradeCost(gold = 40),
            UpgradeCost(gold = 40),
            UpgradeCost(gold = 60, runesRare = 1),
            UpgradeCost(gold = 60, runesRare = 1),
            UpgradeCost(gold = 80, runesSuperRare = 1),
            UpgradeCost(gold = 100, runesMegaRare = 1)
        ),
        "maxSwipes" to makeFifteen(),
        "maxMessageLength" to makeFifteen(),
        "maxMoodLength" to makeFifteen(),
        "maxAdsPerDay" to listOf(
            UpgradeCost(gold = 100),
            UpgradeCost(gold = 120, runesRare = 1),
            UpgradeCost(gold = 150, runesSuperRare = 1)
        )
    )
}

/** ---- Helper functions (unchanged) ---- */
fun maxLevels(feature: String): Int = UpgradeDefs.costs[feature]?.size ?: 0
fun deriveLevelFromAbsolute(feature: String, absolute: Int): Int {
    val b = UpgradeDefs.base[feature] ?: 0
    val s = UpgradeDefs.step[feature] ?: 1
    val rel = (absolute - b).coerceAtLeast(0)
    val lvl = rel / s
    val max = maxLevels(feature)
    return lvl.coerceIn(0, max)
}
fun nextCostByLevel(feature: String, currentLevel: Int): UpgradeCost? =
    UpgradeDefs.costs[feature]?.getOrNull(currentLevel)
fun sumCostByLevels(feature: String, startLevel: Int, levels: Int): UpgradeCost {
    val list = UpgradeDefs.costs[feature].orEmpty()
    val end = (startLevel + levels).coerceAtMost(list.size)
    var g = 0; var r = 0; var sr = 0; var mr = 0
    for (i in startLevel until end) {
        val c = list[i]
        g += c.gold; r += c.runesRare; sr += c.runesSuperRare; mr += c.runesMegaRare
    }
    return UpgradeCost(g, r, sr, mr)
}
fun calculateUpgradeCost(feature: String, currentAbsolute: Int, levels: Int): UpgradeCost {
    val currentLevel = deriveLevelFromAbsolute(feature, currentAbsolute)
    return sumCostByLevels(feature, currentLevel, levels)
}
fun nextUpgradeCostOrNull(feature: String, currentAbsolute: Int): UpgradeCost? {
    val level = deriveLevelFromAbsolute(feature, currentAbsolute)
    return nextCostByLevel(feature, level)
}


