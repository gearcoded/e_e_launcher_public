package org.collegiumdesideriorum.e_e_launcher

import kotlin.math.abs

/** Half-turn of a two-sided card: outgoing face first, incoming face second. */
internal data class PagerCardFrame(val rotationY: Float, val alpha: Float)

internal fun pagerCardFrame(signedPageOffset: Float, outgoing: Boolean): PagerCardFrame {
    val offset = signedPageOffset.coerceIn(-1f, 1f)
    val distance = abs(offset)
    val progress = if (outgoing) distance else 1f - distance
    // In a horizontal swipe the faces must rotate toward opposite sides of the
    // camera, otherwise the second face would flip in from the wrong edge.
    val direction = if (offset < 0f) -1f else 1f
    return if (outgoing) {
        val rotation = direction * 90f * (progress * 2f).coerceIn(0f, 1f)
        val visibility = ((0.55f - progress) / 0.10f).coerceIn(0f, 1f)
        PagerCardFrame(rotation, visibility)
    } else {
        val rotation = direction * 90f * ((1f - progress) * 2f).coerceIn(0f, 1f)
        val visibility = ((progress - 0.45f) / 0.10f).coerceIn(0f, 1f)
        PagerCardFrame(rotation, visibility)
    }
}
