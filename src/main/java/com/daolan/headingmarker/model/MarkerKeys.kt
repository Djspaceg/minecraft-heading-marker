package com.daolan.headingmarker.model

import kotlin.random.Random

/** Rules for the short keys that identify waypoints in commands and saves. */
object MarkerKeys {
    const val LENGTH = 8
    private const val ALPHABET = "abcdefghijklmnopqrstuvwxyz0123456789"
    private val FORMAT = Regex("^[a-z0-9]{1,$LENGTH}$")
    private const val MAX_ATTEMPTS = 1024

    /** True if [key] can be kept as-is: short, lowercase alphanumeric, and not a color word. */
    fun isUsable(key: String): Boolean =
        FORMAT.matches(key) && key !in WaypointColor.RESERVED_WORDS

    /** Generates a fresh key for which [isTaken] returns false. */
    fun generate(random: Random = Random.Default, isTaken: (String) -> Boolean): String {
        repeat(MAX_ATTEMPTS) {
            val candidate = buildString(LENGTH) {
                repeat(LENGTH) { append(ALPHABET[random.nextInt(ALPHABET.length)]) }
            }
            if (isUsable(candidate) && !isTaken(candidate)) return candidate
        }
        throw IllegalStateException("Unable to generate a unique $LENGTH-character marker key.")
    }
}
