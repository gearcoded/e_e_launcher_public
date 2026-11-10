package org.collegiumdesideriorum.e_e_launcher

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.hypot
import kotlin.math.sqrt
import kotlin.math.absoluteValue
import kotlin.math.sin

/** Stable API-26 implementations retained as alternatives to the main Mesh effects. */
@Composable
internal fun OreoAlternativePageSurface(
    effect: PageTransitionEffect,
    signedPageOffset: Float,
    verticalPager: Boolean,
    isOutgoingPage: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (effect == PageTransitionEffect.CUBE_CANVAS) {
        CanvasCubePageSurface(signedPageOffset, verticalPager, isOutgoingPage, modifier, content)
        return
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .oreoAlternativeTransform(effect, signedPageOffset, verticalPager, isOutgoingPage)
            .oreoAlternativeMask(effect, signedPageOffset, isOutgoingPage, verticalPager)
    ) {
        content()
    }
}

private fun Modifier.oreoAlternativeTransform(
    effect: PageTransitionEffect,
    signedPageOffset: Float,
    vertical: Boolean,
    isOutgoingPage: Boolean
): Modifier {
    val offset = signedPageOffset.coerceIn(-1f, 1f)
    val distance = offset.absoluteValue
    return graphicsLayer {
        when (effect) {
            PageTransitionEffect.CUBE_OUT_HORIZONTAL_OREO -> {
                rotationY = -88f * offset
                transformOrigin = if (offset < 0f) TransformOrigin(1f, 0.5f) else TransformOrigin(0f, 0.5f)
                cameraDistance = 30f
                alpha = (1f - distance).coerceIn(0f, 1f)
            }
            PageTransitionEffect.CUBE_IN_HORIZONTAL_OREO -> {
                rotationY = 88f * offset
                transformOrigin = if (offset < 0f) TransformOrigin(0f, 0.5f) else TransformOrigin(1f, 0.5f)
                cameraDistance = 30f
                scaleX = 1f - distance * 0.05f
                alpha = (1f - distance).coerceIn(0f, 1f)
            }
            PageTransitionEffect.CUBE_OUT_VERTICAL_OREO -> {
                rotationX = 88f * offset
                transformOrigin = if (offset < 0f) TransformOrigin(0.5f, 1f) else TransformOrigin(0.5f, 0f)
                cameraDistance = 30f
                alpha = (1f - distance).coerceIn(0f, 1f)
            }
            PageTransitionEffect.CUBE_IN_VERTICAL_OREO -> {
                rotationX = -88f * offset
                transformOrigin = if (offset < 0f) TransformOrigin(0.5f, 0f) else TransformOrigin(0.5f, 1f)
                cameraDistance = 30f
                scaleY = 1f - distance * 0.05f
                alpha = (1f - distance).coerceIn(0f, 1f)
            }
            PageTransitionEffect.CARD_OREO -> {
                // Both page faces meet at the edge-on position at the swipe midpoint.
                // A full-page perspective rotation needs neither a Canvas mask nor Mesh.
                val frame = pagerCardFrame(offset, isOutgoingPage)
                // Cancel the default Pager slide: both faces rotate in the same
                // screen position, rather than disappearing at opposite edges.
                if (vertical) {
                    translationY = offset * size.height
                    rotationX = frame.rotationY
                } else {
                    translationX = offset * size.width
                    rotationY = frame.rotationY
                }
                alpha = frame.alpha
                transformOrigin = TransformOrigin.Center
                cameraDistance = (if (vertical) size.height else size.width) * 4f
            }
            PageTransitionEffect.VORTEX_OREO -> {
                rotationZ = -190f * offset
                rotationY = -24f * offset
                scaleX = 1f - distance * 0.52f
                scaleY = scaleX
                alpha = (1f - distance).coerceIn(0f, 1f)
                cameraDistance = 46f
            }
            PageTransitionEffect.RETRO_FLY_TILES_OREO -> {
                rotationZ = 9f * offset
                scaleX = 0.82f + (1f - distance) * 0.18f
                scaleY = scaleX
                translationY = distance * size.height * 0.08f
            }
            PageTransitionEffect.RETRO_FLY_TILES_STATIC,
            PageTransitionEffect.FIRE_REVEAL_OREO,
            PageTransitionEffect.BLINDS_VERTICAL_OREO,
            PageTransitionEffect.BLINDS_HORIZONTAL_OREO,
            PageTransitionEffect.CHECKERBOARD_OREO,
            PageTransitionEffect.CHECKERBOARD_CANVAS,
            PageTransitionEffect.CHECKERBOARD_VERTICAL_CANVAS,
            PageTransitionEffect.CHECKERBOARD_HORIZONTAL_CANVAS,
            PageTransitionEffect.TRIANGLE_WIPE_CANVAS,
            PageTransitionEffect.CIRCLE_REVEAL_CANVAS,
            PageTransitionEffect.CIRCLE_WIPE_OREO,
            PageTransitionEffect.OVAL_REVEAL_CANVAS,
            PageTransitionEffect.OVAL_WIPE_OREO,
            PageTransitionEffect.TRIANGLE_REVEAL_CANVAS,
            PageTransitionEffect.DIAMOND_REVEAL_CANVAS,
            PageTransitionEffect.RANDOM_BARS_OREO,
            PageTransitionEffect.RANDOM_BARS_VERTICAL_CANVAS,
            PageTransitionEffect.THREAD_WIPE_OREO,
            PageTransitionEffect.THREAD_CROSSED_CANVAS,
            PageTransitionEffect.DIAGONAL_WIPE_OREO,
            PageTransitionEffect.ARC_WIPE_CANVAS,
            PageTransitionEffect.GLITTER_OREO,
            PageTransitionEffect.INTERLACED_BLINDS_VERTICAL_OREO,
            PageTransitionEffect.INTERLACED_BLINDS_HORIZONTAL_OREO -> {
                if (vertical) translationY = offset * size.height else translationX = offset * size.width
                if (effect == PageTransitionEffect.GLITTER_OREO) {
                    alpha = (1f - distance).coerceIn(0f, 1f)
                }
            }
            else -> Unit
        }
    }
}

/**
 * Compose-only page effects. Stationary ports use complementary clipping masks:
 * the incoming page fills the revealed region and the outgoing page fills its
 * exact complement. Never draw two unrelated wipe boundaries for the two pages.
 */
private fun Modifier.oreoAlternativeMask(
    effect: PageTransitionEffect,
    signedPageOffset: Float,
    isOutgoingPage: Boolean,
    verticalPager: Boolean
): Modifier {
    val offset = signedPageOffset.coerceIn(-1f, 1f)
    val visibility = (1f - offset.absoluteValue).coerceIn(0f, 1f)
    val progress = if (isOutgoingPage) 1f - visibility else visibility
    // Pager's outgoing / incoming offsets have opposite signs. Normalize them
    // into the same swipe direction before building the shared clipping mask.
    val direction = when {
        isOutgoingPage -> if (offset < 0f) -1f else 1f
        offset > 0f -> -1f
        else -> 1f
    }

    return drawWithContent {
        val contentScope = this
        when (effect) {
            PageTransitionEffect.RETRO_FLY_TILES_OREO,
            PageTransitionEffect.RETRO_FLY_TILES_STATIC,
            PageTransitionEffect.CHECKERBOARD_OREO -> {
                val flying = effect != PageTransitionEffect.CHECKERBOARD_OREO
                drawGridReveal(
                    columns = if (flying) 5 else 8,
                    rows = if (flying) 7 else 10,
                    visibility = visibility,
                    flying = flying,
                    direction = if (offset < 0f) -1f else 1f,
                    verticalPager = verticalPager && effect != PageTransitionEffect.CHECKERBOARD_OREO
                )
            }
            PageTransitionEffect.FIRE_REVEAL_OREO -> {
                // Mirror the old version: the new page now enters from the
                // right on forward swipes (and from the left on reverse swipes).
                val oldDirection = if (offset < 0f) -1f else 1f
                val extent = if (verticalPager) size.height else size.width
                val boundary = if (oldDirection < 0f) {
                    extent * (1f - visibility)
                } else {
                    extent * visibility
                }.coerceIn(0f, extent)
                if (verticalPager) {
                    if (oldDirection < 0f) clipRect(top = boundary) { contentScope.drawContent() }
                    else clipRect(bottom = boundary) { contentScope.drawContent() }
                } else {
                    if (oldDirection < 0f) clipRect(left = boundary) { contentScope.drawContent() }
                    else clipRect(right = boundary) { contentScope.drawContent() }
                }
                val transitionActive = offset.absoluteValue > 0.001f && offset.absoluteValue < 0.999f
                if (transitionActive && isOutgoingPage) drawFireBand(boundary, verticalPager)
            }
            PageTransitionEffect.BLINDS_VERTICAL_OREO ->
                drawBlinds(vertical = true, visibility = visibility, interlaced = false)
            PageTransitionEffect.BLINDS_HORIZONTAL_OREO ->
                drawBlinds(vertical = false, visibility = visibility, interlaced = false)
            PageTransitionEffect.INTERLACED_BLINDS_VERTICAL_OREO ->
                drawComplementaryReveal(progress, isOutgoingPage, interlacedBarsPath(vertical = true, progress = progress, direction = direction))
            PageTransitionEffect.INTERLACED_BLINDS_HORIZONTAL_OREO ->
                drawComplementaryReveal(progress, isOutgoingPage, interlacedBarsPath(vertical = false, progress = progress, direction = direction))
            PageTransitionEffect.CIRCLE_REVEAL_CANVAS ->
                drawComplementaryReveal(progress, isOutgoingPage, circleRevealPath(progress))
            PageTransitionEffect.OVAL_REVEAL_CANVAS ->
                drawComplementaryReveal(progress, isOutgoingPage, ovalRevealPath(progress))
            PageTransitionEffect.TRIANGLE_REVEAL_CANVAS ->
                drawComplementaryReveal(progress, isOutgoingPage, triangleRevealPath(progress))
            PageTransitionEffect.DIAMOND_REVEAL_CANVAS ->
                drawComplementaryReveal(progress, isOutgoingPage, diamondRevealPath(progress))
            PageTransitionEffect.CIRCLE_WIPE_OREO -> {
                // Deliberately retain the old cross-clipped variant as its own effect.
                val radius = visibility * hypot(size.width, size.height) * 0.58f
                val path = Path().apply {
                    addOval(Rect(center - Offset(radius, radius), center + Offset(radius, radius)))
                }
                clipPath(path) { contentScope.drawContent() }
            }
            PageTransitionEffect.OVAL_WIPE_OREO -> {
                val radiusX = size.width * visibility * 0.72f
                val radiusY = size.height * visibility * 0.92f
                val path = Path().apply {
                    addOval(Rect(center.x - radiusX, center.y - radiusY, center.x + radiusX, center.y + radiusY))
                }
                clipPath(path) { contentScope.drawContent() }
            }
            PageTransitionEffect.CHECKERBOARD_CANVAS ->
                drawComplementaryReveal(progress, isOutgoingPage, orientedRevealPath(meshCheckerboardPath(progress, direction), verticalPager))
            PageTransitionEffect.CHECKERBOARD_VERTICAL_CANVAS ->
                drawComplementaryReveal(progress, isOutgoingPage, verticalCheckerboardPath(progress))
            PageTransitionEffect.CHECKERBOARD_HORIZONTAL_CANVAS ->
                drawComplementaryReveal(progress, isOutgoingPage, horizontalCheckerboardPath(progress))
            PageTransitionEffect.RANDOM_BARS_OREO ->
                drawComplementaryReveal(progress, isOutgoingPage, randomSnapBarsPath(progress))
            PageTransitionEffect.RANDOM_BARS_VERTICAL_CANVAS ->
                drawComplementaryReveal(progress, isOutgoingPage, randomSnapColumnsPath(progress))
            PageTransitionEffect.THREAD_WIPE_OREO ->
                drawComplementaryReveal(progress, isOutgoingPage, orientedRevealPath(meshThreadPath(progress, direction), verticalPager))
            PageTransitionEffect.THREAD_CROSSED_CANVAS ->
                drawCrossThreads(visibility, verticalPager)
            PageTransitionEffect.DIAGONAL_WIPE_OREO ->
                drawComplementaryReveal(progress, isOutgoingPage, orientedRevealPath(meshDiagonalPath(progress, direction), verticalPager))
            PageTransitionEffect.TRIANGLE_WIPE_CANVAS ->
                drawComplementaryReveal(progress, isOutgoingPage, orientedRevealPath(meshTrianglePath(progress, direction), verticalPager))
            PageTransitionEffect.ARC_WIPE_CANVAS ->
                drawComplementaryReveal(progress, isOutgoingPage, orientedRevealPath(meshArcPath(progress, direction), verticalPager))
            PageTransitionEffect.GLITTER_OREO -> drawGlitter(visibility)
            else -> contentScope.drawContent()
        }
    }
}

/** Rotate the wipe boundary, not the page bitmap or individual widgets. */
private fun ContentDrawScope.orientedRevealPath(original: Path, verticalPager: Boolean): Path {
    if (!verticalPager || size.width <= 0f || size.height <= 0f) return original
    val matrix = android.graphics.Matrix()
    matrix.setValues(floatArrayOf(
        0f, size.width / size.height, 0f,
        size.height / size.width, 0f, 0f,
        0f, 0f, 1f
    ))
    val native = android.graphics.Path()
    original.asAndroidPath().transform(matrix, native)
    return native.asComposePath()
}

/** No duplicate outlines: draw each complete page only once through the same border. */
private fun ContentDrawScope.drawComplementaryReveal(
    progress: Float,
    isOutgoingPage: Boolean,
    revealedRegion: Path
) {
    if (progress <= 0f) {
        if (isOutgoingPage) drawContent()
        return
    }
    if (progress >= 1f) {
        if (!isOutgoingPage) drawContent()
        return
    }
    val contentScope = this
    clipPath(revealedRegion, clipOp = if (isOutgoingPage) ClipOp.Difference else ClipOp.Intersect) {
        contentScope.drawContent()
    }
}

private fun ContentDrawScope.circleRevealPath(progress: Float): Path {
    val halfDiagonal = hypot(size.width / 2f, size.height / 2f)
    val radius = (halfDiagonal + 2f) * smoothCanvas(progress)
    return Path().apply { addOval(Rect(center.x - radius, center.y - radius, center.x + radius, center.y + radius)) }
}

private fun ContentDrawScope.ovalRevealPath(progress: Float): Path {
    // Physical proportions remain a vertical oval also on landscape displays.
    val base = minOf(size.width, size.height).coerceAtLeast(1f)
    val unitX = base * 0.34f
    val unitY = base * 0.62f
    val cornerNorm = hypot(size.width / (2f * unitX), size.height / (2f * unitY))
    val scale = cornerNorm * smoothCanvas(progress) * 1.002f
    return Path().apply {
        addOval(Rect(center.x - unitX * scale, center.y - unitY * scale,
            center.x + unitX * scale, center.y + unitY * scale))
    }
}

/**
 * Centre-growing equilateral triangle, distinct from the Mesh half-diamond
 * wipe now named Angle. The apex points upward; the final scale is chosen so
 * the whole viewport (including its top two corners) fits in the triangle.
 */
private fun ContentDrawScope.triangleRevealPath(progress: Float): Path {
    val scale = smoothCanvas(progress)
    val radius = maxOf(size.height, size.height / 2f + size.width * sqrt(3f) / 2f) * 1.015f * scale
    val halfBase = sqrt(3f) * radius / 2f
    return Path().apply {
        moveTo(center.x, center.y - radius)
        lineTo(center.x + halfBase, center.y + radius / 2f)
        lineTo(center.x - halfBase, center.y + radius / 2f)
        close()
    }
}

/** A centre-growing four-point diamond with a true 45-degree corner profile. */
private fun ContentDrawScope.diamondRevealPath(progress: Float): Path {
    // For a diamond |x| + |y| <= r, r >= (width+height)/2 covers all corners.
    val radius = (size.width + size.height) / 2f * 1.015f * smoothCanvas(progress)
    return Path().apply {
        moveTo(center.x, center.y - radius)
        lineTo(center.x + radius, center.y)
        lineTo(center.x, center.y + radius)
        lineTo(center.x - radius, center.y)
        close()
    }
}

private fun ContentDrawScope.meshCheckerboardPath(progress: Float, direction: Float): Path {
    val columns = 10
    val rows = 14
    val cellWidth = size.width / columns
    val cellHeight = size.height / rows
    return Path().apply {
        repeat(rows) { row ->
            repeat(columns) { column ->
                val directedColumn = if (direction > 0f) columns - 1 - column else column
                val diagonalRank = (directedColumn + row).toFloat() / (columns + rows - 2f)
                val parityDelay = ((column + row) and 1) * 0.16f
                if (progress >= diagonalRank * 0.72f + parityDelay) {
                    addRect(Rect(column * cellWidth, row * cellHeight,
                        (column + 1) * cellWidth + 0.1f, (row + 1) * cellHeight + 0.1f))
                }
            }
        }
    }
}

/**
 * Two-phase square checkerboard. The existing vertical effect reveals each
 * even-parity cell top -> bottom during 0..0.5, then each odd cell. The new
 * horizontal variant reveals inside each cell left -> right, with identical
 * grid geometry and timing. Both pages share this path, so their clip masks
 * are exact complements during forward, backward and cancelled swipes.
 */
private fun ContentDrawScope.verticalCheckerboardPath(progress: Float): Path =
    checkerboardCellRevealPath(progress, verticalReveal = true)

private fun ContentDrawScope.horizontalCheckerboardPath(progress: Float): Path =
    checkerboardCellRevealPath(progress, verticalReveal = false)

private fun ContentDrawScope.checkerboardCellRevealPath(
    progress: Float,
    verticalReveal: Boolean
): Path {
    if (size.width <= 0f || size.height <= 0f) return Path()
    val cellSide = minOf(size.width, size.height) / 8f
    val columns = ceil(size.width / cellSide).toInt()
    val rows = ceil(size.height / cellSide).toInt()
    return Path().apply {
        repeat(rows) { row ->
            repeat(columns) { column ->
                val phaseStart = if ((row + column) % 2 == 0) 0f else 0.5f
                val local = ((progress - phaseStart) * 2f).coerceIn(0f, 1f)
                if (local > 0f) {
                    val left = column * cellSide
                    val top = row * cellSide
                    val right = minOf(left + cellSide, size.width)
                    val bottom = minOf(top + cellSide, size.height)
                    if (verticalReveal) {
                        addRect(Rect(left, top, right, top + (bottom - top) * local))
                    } else {
                        addRect(Rect(left, top, left + (right - left) * local, bottom))
                    }
                }
            }
        }
    }
}

private const val CANVAS_RANDOM_BAR_COUNT = 96

/**
 * Stable shuffled strip order. A fresh random permutation on each page would
 * make the incoming/outgoing complementary masks disagree; one shared order
 * keeps the image complete and flicker-free for forward, reverse and undo.
 */
private val canvasRandomBarRanks: IntArray = run {
    val order = IntArray(CANVAS_RANDOM_BAR_COUNT) { it }
    var state = 0x4B19F32
    for (i in order.lastIndex downTo 1) {
        state = state * 1664525 + 1013904223 // Intentional 32-bit overflow.
        val j = (state ushr 1) % (i + 1)
        val temp = order[i]
        order[i] = order[j]
        order[j] = temp
    }
    IntArray(CANVAS_RANDOM_BAR_COUNT).also { ranks ->
        for (rank in order.indices) ranks[order[rank]] = rank
    }
}

/**
 * Preserve 0.44.10's instant horizontal snap; add its vertical transpose.
 * Each strip switches as a complete rectangle, with no sliding, fading or
 * partial wipe. Shared deterministic ranks guarantee that the incoming and
 * outgoing pages use complementary masks in either swipe direction.
 */
private fun ContentDrawScope.randomSnapBarsPath(progress: Float): Path =
    randomSnapStripsPath(progress, horizontal = true)

private fun ContentDrawScope.randomSnapColumnsPath(progress: Float): Path =
    randomSnapStripsPath(progress, horizontal = false)

private fun ContentDrawScope.randomSnapStripsPath(progress: Float, horizontal: Boolean): Path {
    if (size.width <= 0f || size.height <= 0f) return Path()
    val revealed = (progress.coerceIn(0f, 1f) * CANVAS_RANDOM_BAR_COUNT)
        .toInt().coerceIn(0, CANVAS_RANDOM_BAR_COUNT)
    val extent = if (horizontal) size.height else size.width
    val stripExtent = extent / CANVAS_RANDOM_BAR_COUNT
    return Path().apply {
        repeat(CANVAS_RANDOM_BAR_COUNT) { index ->
            if (canvasRandomBarRanks[index] < revealed) {
                val start = index * stripExtent
                val end = if (index == CANVAS_RANDOM_BAR_COUNT - 1) extent
                    else (index + 1) * stripExtent + 0.1f
                if (horizontal) {
                    addRect(Rect(0f, start, size.width, end))
                } else {
                    addRect(Rect(start, 0f, end, size.height))
                }
            }
        }
    }
}

/**
 * Legacy 0.44.7 Canvas thread-wipe geometry, intentionally kept independent for
 * each of the two pages so the waves meet/cross. Unlike the old variant it draws
 * no straight white boundary lines (those appeared twice during a transition).
 * The 0.44.8 shared-boundary THREAD_WIPE_OREO is retained unchanged.
 */
private fun ContentDrawScope.drawCrossThreads(visibility: Float, verticalPager: Boolean) {
    val contentScope = this
    val boundary = size.width * visibility
    val path = Path().apply {
        moveTo(0f, 0f)
        repeat(25) { index ->
            val y = size.height * index / 24f
            val wave = sin(index * 0.86f + visibility * 9f) * size.width * 0.035f
            lineTo((boundary + wave).coerceIn(0f, size.width), y)
        }
        lineTo(0f, size.height)
        close()
    }
    clipPath(orientedRevealPath(path, verticalPager)) { contentScope.drawContent() }
}

private fun ContentDrawScope.meshThreadPath(progress: Float, direction: Float): Path {
    val waveStrength = size.width * 0.034f * sin(progress * PI.toFloat())
    val samples = 64
    return Path().apply {
        if (direction > 0f) {
            moveTo(size.width, 0f)
            lineTo(size.width, size.height)
        } else {
            moveTo(0f, 0f)
            lineTo(0f, size.height)
        }
        for (index in samples downTo 0) {
            val v = index / samples.toFloat()
            val x = (if (direction > 0f) size.width * (1f - progress) else size.width * progress) +
                waveStrength * sin(v * PI.toFloat() * 15f + progress * 4f)
            lineTo(x.coerceIn(0f, size.width), v * size.height)
        }
        close()
    }
}

private fun ContentDrawScope.meshDiagonalPath(progress: Float, direction: Float): Path {
    // Match MeshTransitionGeometry.DIAGONAL_WIPE, including its swipe direction.
    val first = if (direction > 0f) 1f - progress * 1.58f else progress * 1.58f - 0.58f
    val last = first + 0.58f
    return Path().apply {
        if (direction > 0f) {
            moveTo(size.width, 0f)
            lineTo(size.width, size.height)
            lineTo(last * size.width, size.height)
            lineTo(first * size.width, 0f)
        } else {
            moveTo(0f, 0f)
            lineTo(first * size.width, 0f)
            lineTo(last * size.width, size.height)
            lineTo(0f, size.height)
        }
        close()
    }
}

/**
 * Port of MeshTransitionGeometry.TRIANGLE_WIPE:
 * edgeDistance(u, direction) + abs(v - 0.5) * 0.88 <= progress * 1.44.
 * This is a half-diamond extending from the entry edge; the broken-line
 * boundary has its tip at mid-height.  The same path clips both pages as
 * exact complements, including reverse and cancelled swipes.
 */
private fun ContentDrawScope.meshTrianglePath(progress: Float, direction: Float): Path {
    val reach = progress.coerceIn(0f, 1f) * 1.44f
    val edgeX = if (direction > 0f) size.width else 0f
    // The Mesh condition is piecewise linear in v, but its 0..1 clamping
    // adds two corners when the tip first emerges and two more when its
    // centre reaches the far edge. Include those breakpoints explicitly:
    // simply joining top/middle/bottom would open the triangle too early.
    val heights = mutableListOf(0f, 0.5f, 1f)
    if (reach < 0.44f) {
        val half = reach / 0.88f
        heights.add(0.5f - half)
        heights.add(0.5f + half)
    }
    if (reach > 1f) {
        val half = (reach - 1f) / 0.88f
        heights.add(0.5f - half)
        heights.add(0.5f + half)
    }
    heights.sort()
    return Path().apply {
        moveTo(edgeX, 0f)
        lineTo(edgeX, size.height)
        for (v in heights.asReversed()) {
            val edge = (reach - (v - 0.5f).absoluteValue * 0.88f).coerceIn(0f, 1f)
            val x = if (direction > 0f) (1f - edge) * size.width else edge * size.width
            lineTo(x, v * size.height)
        }
        close()
    }
}

private fun ContentDrawScope.meshArcPath(progress: Float, direction: Float): Path {
    // Mesh counterpart: sqrt(edgeDistance(u)^2 + (v - 0.5)^2 * 0.58)
    // <= progress * 1.12. Sample the curved boundary in normalized space.
    val steps = 80
    val radiusSq = progress * progress * 1.12f * 1.12f
    return Path().apply {
        if (direction > 0f) {
            moveTo(size.width, 0f)
            lineTo(size.width, size.height)
        } else {
            moveTo(0f, 0f)
            lineTo(0f, size.height)
        }
        for (index in steps downTo 0) {
            val v = index / steps.toFloat()
            val horizontalSq = radiusSq - (v - 0.5f) * (v - 0.5f) * 0.58f
            val edge = if (horizontalSq > 0f) sqrt(horizontalSq) else 0f
            val x = (if (direction > 0f) 1f - edge else edge).coerceIn(0f, 1f)
            lineTo(x * size.width, v * size.height)
        }
        close()
    }
}

private fun smoothCanvas(value: Float): Float {
    val safe = value.coerceIn(0f, 1f)
    return safe * safe * (3f - 2f * safe)
}


private fun ContentDrawScope.drawFireBand(boundary: Float, verticalPager: Boolean) {
    if (verticalPager) {
        val band = (size.height * 0.055f).coerceAtLeast(18f * density)
        val stripeTop = (boundary - band / 2f).coerceIn(-band, size.height)
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color(0xCCB32612), Color(0xFFFF7A1A),
                    Color(0xFFFFE082), Color(0xFFFF7A1A), Color(0xCCB32612), Color.Transparent),
                startY = stripeTop,
                endY = stripeTop + band
            ),
            topLeft = Offset(0f, stripeTop),
            size = Size(size.width, band)
        )
        drawLine(
            color = Color(0xFFFFF2B0).copy(alpha = 0.85f),
            start = Offset(0f, boundary.coerceIn(0f, size.height)),
            end = Offset(size.width, boundary.coerceIn(0f, size.height)),
            strokeWidth = 1.5f * density
        )
        return
    }
    val band = (size.width * 0.055f).coerceAtLeast(18f * density)
    val stripeLeft = (boundary - band / 2f).coerceIn(-band, size.width)
    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(
                Color.Transparent,
                Color(0xCCB32612),
                Color(0xFFFF7A1A),
                Color(0xFFFFE082),
                Color(0xFFFF7A1A),
                Color(0xCCB32612),
                Color.Transparent
            ),
            startX = stripeLeft,
            endX = stripeLeft + band
        ),
        topLeft = Offset(stripeLeft, 0f),
        size = Size(band, size.height)
    )
    drawLine(
        color = Color(0xFFFFF2B0).copy(alpha = 0.85f),
        start = Offset(boundary.coerceIn(0f, size.width), 0f),
        end = Offset(boundary.coerceIn(0f, size.width), size.height),
        strokeWidth = 1.5f * density
    )
}

private fun ContentDrawScope.drawBlinds(
    vertical: Boolean,
    visibility: Float,
    interlaced: Boolean
) {
    val contentScope = this
    val sections = if (vertical) 12 else 16
    val sectionSize = if (vertical) size.width / sections else size.height / sections
    repeat(sections) { index ->
        val phase = if (interlaced && index % 2 != 0) 0.34f else 0f
        val local = ((visibility - phase) / (1f - phase).coerceAtLeast(0.01f)).coerceIn(0f, 1f)
        if (local > 0f) {
            if (vertical) {
                val left = index * sectionSize
                clipRect(left = left, right = left + sectionSize * local + 1f) { contentScope.drawContent() }
            } else {
                val top = index * sectionSize
                clipRect(top = top, bottom = top + sectionSize * local + 1f) { contentScope.drawContent() }
            }
        }
    }
}

private fun ContentDrawScope.drawGridReveal(
    columns: Int,
    rows: Int,
    visibility: Float,
    flying: Boolean,
    direction: Float,
    verticalPager: Boolean
) {
    val contentScope = this
    val cellWidth = size.width / columns
    val cellHeight = size.height / rows
    repeat(rows) { row ->
        repeat(columns) { column ->
            val index = row * columns + column
            val order = ((index * 17 + row * 5 + column * 11) % (columns * rows)) /
                (columns * rows - 1f)
            val local = ((visibility - order * 0.54f) / 0.46f).coerceIn(0f, 1f)
            if (local > 0f) {
                val left = column * cellWidth
                val top = row * cellHeight
                val pivot = Offset(left + cellWidth / 2f, top + cellHeight / 2f)
                clipRect(left, top, left + cellWidth + 1f, top + cellHeight + 1f) {
                    if (flying) {
                        val shift = direction * (1f - local) *
                            (if (verticalPager) size.height else size.width) *
                            (0.16f + (row % 3) * 0.04f)
                        withTransform({
                            if (verticalPager) translate(left = (1f - local) * (column - columns / 2f) * 3f, top = shift)
                            else translate(left = shift, top = (1f - local) * (row - rows / 2f) * 3f)
                            scale(local.coerceAtLeast(0.35f), local.coerceAtLeast(0.35f), pivot)
                        }) { contentScope.drawContent() }
                    } else {
                        contentScope.drawContent()
                    }
                }
            }
        }
    }
}

/**
 * The working, randomized vertical-strip Canvas implementation formerly used
 * by RANDOM_BARS_OREO, now reused as the interlaced-blinds starting point.
 * The horizontal version is the transposition of exactly the same algorithm.
 */
private fun ContentDrawScope.interlacedBarsPath(vertical: Boolean, progress: Float, direction: Float): Path {
    if (size.width <= 0f || size.height <= 0f) return Path()
    // Match physical thickness for the two orientations, including portrait:
    // horizontal strips should not be much taller than vertical strips are wide.
    val extent = if (vertical) size.width else size.height
    val thickness = minOf(size.width, size.height) / 14f
    val bars = ceil(extent / thickness).toInt().coerceAtLeast(1)
    val strip = extent / bars
    return Path().apply {
        repeat(bars) { index ->
            // Keep the previous Canvas RANDOM_BARS ordering and reveal timings.
            val order = ((index * 9 + 5) % bars) / (bars - 1f)
            val local = ((progress - order * 0.42f) / 0.58f).coerceIn(0f, 1f)
            if (local > 0f) {
                if (vertical) {
                    val left = index * strip
                    addRect(Rect(left, 0f, left + strip + 0.1f, size.height * local))
                } else {
                    val top = index * strip
                    val revealedWidth = size.width * local
                    // Pager offset > 0 is a left swipe: wipe from right to left.
                    val left = if (direction > 0f) size.width - revealedWidth else 0f
                    addRect(Rect(left, top, left + revealedWidth, top + strip + 0.1f))
                }
            }
        }
    }
}

private fun ContentDrawScope.drawGlitter(visibility: Float) {
    drawContent()
    repeat(34) { index ->
        val seedX = ((index * 47) % 101) / 100f
        val seedY = ((index * 71 + 13) % 103) / 102f
        val threshold = ((index * 29) % 34) / 33f
        val local = ((visibility - threshold * 0.55f) / 0.45f).coerceIn(0f, 1f)
        if (local in 0.01f..0.96f) {
            val pulse = sin((local * PI).toFloat()).coerceAtLeast(0f)
            drawCircle(
                color = if (index % 2 == 0) Color.White.copy(alpha = pulse * 0.82f)
                else Color(0xFFFFD86B).copy(alpha = pulse * 0.78f),
                radius = (2f + index % 4) * density,
                center = Offset(seedX * size.width, seedY * size.height)
            )
        }
    }
}
