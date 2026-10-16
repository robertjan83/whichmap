package app.whichmap

/**
 * Zelfde rotatie als in index.html. Houd REF gelijk aan REF in index.html
 * (20:10 UTC = 22:10 Amsterdamse tijd op vr 16 okt 2026).
 */
object Rotation {
    const val SLOT_MS = 10 * 60 * 1000L
    const val REF = 1792181400000L

    // Op REF is order[0] actief; daarna elke 10 minuten de volgende.
    val ORDER = mapOf(
        "solo" to listOf("rebirth", "haunted", "fortune"),
        "duos" to listOf("haunted", "fortune", "rebirth"),
        "quads" to listOf("fortune", "rebirth", "haunted"),
    )
    val MODE_LABEL = mapOf("solo" to "Solo", "duos" to "Duos", "quads" to "Quads")
    val MAP_NAME = mapOf("haunted" to "Haunted Hollow", "fortune" to "Fortune's Keep", "rebirth" to "Rebirth Island")
    val MAP_RES = mapOf("haunted" to R.drawable.map_haunted, "fortune" to R.drawable.map_fortune, "rebirth" to R.drawable.map_rebirth)

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
