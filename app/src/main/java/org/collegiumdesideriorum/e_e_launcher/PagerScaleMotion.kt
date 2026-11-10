package org.collegiumdesideriorum.e_e_launcher

/**
 * Concurrent stationary zoom for two Pager pages.
 *
 * Each page is scaled from its own distance to the viewport centre:
 * outgoing page distance 0 -> 1 gives 100% -> 0%, while the incoming
 * page distance 1 -> 0 gives 0% -> 100%, at the SAME time.
 * Pages never slide; the corresponding graphicsLayer cancels Pager's
 * native per-page translation. No opacity interpolation is used.
 */
internal fun pagerSimultaneousZoom(pageDistance: Float): Float =
    1f - pageDistance.coerceIn(0f, 1f)

/**
 * The incoming page for the new "Zoom out" effect starts at 400% scale,
 * fully transparent, and returns to 100% while fading in.
 * The outgoing page keeps its size and fades out at the same time.
 * Both pages are centred by the graphicsLayer Pager displacement correction.
 */
internal fun pagerZoomOutScale(pageDistance: Float, isOutgoingPage: Boolean): Float =
    if (isOutgoingPage) 1f else 1f + 3f * pageDistance.coerceIn(0f, 1f)

internal fun pagerZoomOutAlpha(pageDistance: Float): Float =
    1f - pageDistance.coerceIn(0f, 1f)
