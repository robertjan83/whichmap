package app.whichmap

/** Zelfde tijdanker en volgorde als de website: 17 nov 2026, 23:50 CET = 22:50 UTC. */
object Rotation {
    const val SLOT_MS = 10 * 60 * 1000L
    const val REF = 1794955800000L

    // Op REF is order[0] actief; elke tien minuten wisselt de map.
    val ORDER = mapOf(
        "solo" to listOf("zodiac", "rebirth"),
        "duos" to listOf("rebirth", "zodiac"),
        "trios" to listOf("zodiac", "rebirth"),
        "quads" to listOf("rebirth", "zodiac"),
        "casuals" to listOf("rebirth", "zodiac"),
    )
    val MODE_LABEL = mapOf(
        "solo" to "Solo", "duos" to "Duos", "trios" to "Trio",
        "quads" to "Quads", "casuals" to "Quads"
    )
    val MAP_NAME = mapOf("zodiac" to "Zodiac", "rebirth" to "Rebirth Island")
    val MAP_RES = mapOf("zodiac" to R.drawable.map_zodiac, "rebirth" to R.drawable.map_rebirth)

    data class Slot(val current: String, val next: String, val start: Long, val end: Long)

    fun slotAt(mode: String, now: Long): Slot {
        val order = ORDER[mode] ?: ORDER.getValue("quads")
        val slot = Math.floorDiv(now - REF, SLOT_MS)
        val start = REF + slot * SLOT_MS
        val i = Math.floorMod(slot, order.size.toLong()).toInt()
        return Slot(order[i], order[(i + 1) % order.size], start, start + SLOT_MS)
    }

    fun nextBoundary(now: Long): Long = REF + (Math.floorDiv(now - REF, SLOT_MS) + 1) * SLOT_MS
}
