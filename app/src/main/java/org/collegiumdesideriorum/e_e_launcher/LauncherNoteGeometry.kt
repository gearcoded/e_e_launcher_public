package org.collegiumdesideriorum.e_e_launcher

/** Shared coordinate conversion for free-floating note cards (same convention as hosted widgets).
 * Fractions describe the position inside the FREE extent, not the full screen extent.
 * Keeping both conversions symmetric prevents drift after repeated drag/resize cycles.
 */
internal object LauncherNoteGeometry {
    fun absolute(fraction: Float, containerPx: Float, objectPx: Float): Float =
        fraction.coerceIn(0f, 1f) * (containerPx - objectPx).coerceAtLeast(0f)

    fun fraction(positionPx: Float, containerPx: Float, objectPx: Float): Float {
        val extent = (containerPx - objectPx).coerceAtLeast(0f)
        return if (extent > 0f) (positionPx / extent).coerceIn(0f, 1f) else 0f
    }
}
