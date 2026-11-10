package org.collegiumdesideriorum.e_e_launcher

import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.layer.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

/**
 * App-drawer icon animations are Compose graphicsLayer/Canvas effects, not
 * Android Mesh effects. The source cell remains a live Composable. We record
 * its current contents once per draw and reuse the GPU layer for fragments.
 */
internal enum class AdvancedDrawerIconMotion { SPIRAL, FIREWORK, BLACK_HOLE, BIG_BANG }

private class FragmentLayerCache { var captured = false }

/** Simple deterministic pseudo-random values; every fragment keeps its direction during a swipe. */
private fun fragmentRandom(seed: Int): Float {
    var n = seed * 1103515245 + 12345
    n = n xor (n ushr 16)
    n *= 0x45d9f3b
    n = n xor (n ushr 16)
    return (n and 0x7fffffff).toFloat() / 2147483647f
}

/** Distance from a point to the first viewport edge along a normalized ray. */
private fun distanceToViewportEdge(
    point: Offset, directionX: Float, directionY: Float, viewport: Rect
): Float {
    val xDistance = when {
        directionX > 0.0001f -> (viewport.right - point.x) / directionX
        directionX < -0.0001f -> (viewport.left - point.x) / directionX
        else -> Float.POSITIVE_INFINITY
    }
    val yDistance = when {
        directionY > 0.0001f -> (viewport.bottom - point.y) / directionY
        directionY < -0.0001f -> (viewport.top - point.y) / directionY
        else -> Float.POSITIVE_INFINITY
    }
    return min(xDistance, yDistance).coerceAtLeast(0f)
}


/**
 * Pager lays neighboring pages one viewport apart. During the transition their
 * visual translation is cancelled at the page surface, but layout callbacks
 * for an incoming icon can still report the neighboring (off-screen) position.
 * Wrap only along the pager axis so every particle uses a source position on
 * the same *visible* page as viewport.center. Already-correct coordinates are
 * left unchanged.
 */
private fun pointOnVisiblePage(point: Offset, viewport: Rect, verticalPager: Boolean): Offset {
    if (verticalPager) {
        val span = viewport.height
        if (span <= 0f || point.y in viewport.top..viewport.bottom) return point
        val y = viewport.top + ((point.y - viewport.top) % span + span) % span
        return Offset(point.x, y)
    }
    val span = viewport.width
    if (span <= 0f || point.x in viewport.left..viewport.right) return point
    val x = viewport.left + ((point.x - viewport.left) % span + span) % span
    return Offset(x, point.y)
}

/**
 * The whole drawer page is kept at its settled position for the advanced
 * effects; only individual live icons/tiles move. In particular, no Pager
 * compensation is applied again at icon level (which doubled the movement).
 * Outgoing and incoming pages can intentionally have different animations:
 * spiral uses centre-based flight paths, Firework and Black Hole give their
 * incoming icons separate animations, and Big Bang assembles new fragments
 * at their final cells after flying from the viewport centre.
 */
internal fun Modifier.advancedAppDrawerIconTransform(
    effect: AdvancedDrawerIconMotion,
    signedPageOffset: Float,
    itemIndex: Int,
    density: Float,
    verticalPager: Boolean,
    pageBoundsInRoot: Rect?,
    isOutgoingPage: Boolean
): Modifier = composed {
    val offset = signedPageOffset.coerceIn(-1f, 1f)
    val progress = abs(offset)
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val viewportWidth = configuration.screenWidthDp * density
    val viewportHeight = configuration.screenHeightDp * density
    val viewport = pageBoundsInRoot ?: Rect(0f, 0f, viewportWidth, viewportHeight)
    val viewportCenter = viewport.center
    var iconCentreOnSettledPage by remember { mutableStateOf(Offset.Unspecified) }
    val layer = rememberGraphicsLayer()
    val fragmentCache = remember(effect, itemIndex) { FragmentLayerCache() }
    SideEffect { layer.compositingStrategy = CompositingStrategy.Offscreen }
    val phase = progress * progress * (3f - 2f * progress) // smoothstep, reversible
    val arrival = (1f - progress).coerceIn(0f, 1f)
    val fadeIn = arrival * arrival * (3f - 2f * arrival)

    // Only update the rest centre when motion has stopped. The page itself is
    // stationary (see PagedAppDrawer), so no screen-sized Pager offset belongs
    // in this calculation or in the icon's graphicsLayer.
    this.onGloballyPositioned { coordinates ->
        if (progress < 0.001f || !iconCentreOnSettledPage.isSpecified) {
            val centre = coordinates.positionInRoot() + Offset(
                coordinates.size.width * 0.5f,
                coordinates.size.height * 0.5f
            )
            if (centre != iconCentreOnSettledPage) iconCentreOnSettledPage = centre
        }
    }.graphicsLayer {
        when (effect) {
            AdvancedDrawerIconMotion.SPIRAL -> {
                // Neighboring Pager pages can be one full viewport offscreen
                // before the first frame. Use the icon's position on the
                // *visible* stationary page, not its raw layout position.
                val restCentre = pointOnVisiblePage(
                    if (iconCentreOnSettledPage.isSpecified) iconCentreOnSettledPage
                    else viewportCenter,
                    viewport,
                    verticalPager
                )
                val dx = restCentre.x - viewportCenter.x
                val dy = restCentre.y - viewportCenter.y
                val restRadius = hypot(dx, dy)
                val restAngle = if (restRadius > 2f) atan2(dy, dx)
                    else fragmentRandom(itemIndex * 71 + 5) * 2f * PI.toFloat()
                // Both page halves use the same swipe sign, including when
                // flipping in the opposite direction or across a loop edge.
                val swipeSign = if (isOutgoingPage) offset else -offset
                val orbitDirection = if (swipeSign >= 0f) -1f else 1f
                val fullTurn = 2f * PI.toFloat()
                val drawnCentre: Offset
                if (isOutgoingPage) {
                    // Old icons immediately spiral away AROUND THE PAGE CENTRE,
                    // starting at their exact settled positions. Never scale
                    // them down into a stationary translucent cluster.
                    val exitRadius = hypot(viewport.width, viewport.height) * 0.55f +
                        max(size.width, size.height) * 1.8f
                    val radius = restRadius +
                        (exitRadius - restRadius).coerceAtLeast(0f) * phase.pow(1.08f)
                    val angle = restAngle + orbitDirection * fullTurn * 0.88f * phase
                    drawnCentre = viewportCenter + Offset(cos(angle) * radius, sin(angle) * radius)
                    // Fade only at the end, after the spiral has taken the
                    // departing icons towards/outside the page boundary.
                    alpha = ((1f - phase) / 0.17f).coerceIn(0f, 1f)
                } else {
                    // Incoming icons start precisely at the page centre. Most
                    // of the turn happens early; the last part settles directly
                    // onto the cell with no extra two revolutions.
                    val travel = arrival
                    val radialProgress = 1f - (1f - travel).pow(1.35f)
                    val angularRemainder = (1f - travel).pow(2.4f)
                    val radius = restRadius * radialProgress
                    val angle = restAngle - orbitDirection * fullTurn * 0.82f * angularRemainder
                    drawnCentre = viewportCenter + Offset(cos(angle) * radius, sin(angle) * radius)
                    alpha = (travel / 0.16f).coerceIn(0f, 1f)
                }
                translationX = drawnCentre.x - restCentre.x
                translationY = drawnCentre.y - restCentre.y
                rotationZ = 0f
                scaleX = 1f
                scaleY = 1f
            }
            AdvancedDrawerIconMotion.FIREWORK -> {
                alpha = if (isOutgoingPage) 1f else fadeIn
            }
            AdvancedDrawerIconMotion.BLACK_HOLE -> {
                if (isOutgoingPage) {
                    alpha = 1f
                } else {
                    val sourceCentre = if (iconCentreOnSettledPage.isSpecified)
                        pointOnVisiblePage(iconCentreOnSettledPage, viewport, verticalPager)
                    else viewportCenter
                    val dx = sourceCentre.x - viewportCenter.x
                    val dy = sourceCentre.y - viewportCenter.y
                    val farScale = 4f + fragmentRandom(itemIndex * 67 + 11) * 2f
                    translationX = dx * (farScale - 1f) * progress
                    translationY = dy * (farScale - 1f) * progress
                    alpha = 0.15f + 0.85f * fadeIn
                }
            }
            AdvancedDrawerIconMotion.BIG_BANG -> {
                if (isOutgoingPage) {
                    // Keep 0.46.8: existing whole icons fly beyond the screen
                    // as though the page grows 4–6 times larger.
                    val sourceCentre = if (iconCentreOnSettledPage.isSpecified)
                        pointOnVisiblePage(iconCentreOnSettledPage, viewport, verticalPager)
                    else viewportCenter
                    val dx = sourceCentre.x - viewportCenter.x
                    val dy = sourceCentre.y - viewportCenter.y
                    val farScale = 6f + fragmentRandom(itemIndex * 79 + 19) * 2f
                    // Keep the approved 1.5x flight speed but extend the
                    // virtual outgoing page to 600–800% (previously 400–600%).
                    val flightProgress = (phase * 1.5f).coerceIn(0f, 1f)
                    translationX = dx * (farScale - 1f) * flightProgress
                    translationY = dy * (farScale - 1f) * flightProgress
                    alpha = (1f - progress * 0.45f).coerceIn(0f, 1f)
                } else {
                    // Fragments of the entering icons are rendered in Canvas
                    // well outside their cell. Fractional layer alpha clips
                    // them there, so opacity must remain 1 throughout flight.
                    alpha = 1f
                }
            }
        }
    }.drawWithContent {
        val fragmentAnimation = when (effect) {
            AdvancedDrawerIconMotion.SPIRAL -> false
            AdvancedDrawerIconMotion.FIREWORK, AdvancedDrawerIconMotion.BLACK_HOLE -> isOutgoingPage
            AdvancedDrawerIconMotion.BIG_BANG -> !isOutgoingPage
        }
        if (progress <= 0.001f) fragmentCache.captured = false
        if (!fragmentAnimation || progress <= 0.001f) {
            // Preserve the ordinary live cell for the nonfragmented half of
            // each transition, including the outgoing Big Bang flight.
            drawContent()
            return@drawWithContent
        }
        if (progress >= 0.999f || size.width <= 0f || size.height <= 0f) return@drawWithContent

        // App drawer icon contents are static during a swipe. Capture each
        // cell once on the first movement frame, then reuse its GPU surface.
        // 4x4 fragments cost 16 draws instead of 25 on modest GPUs.
        if (!fragmentCache.captured) {
            layer.record { this@drawWithContent.drawContent() }
            fragmentCache.captured = true
        }
        val fragments = 4
        val stepX = size.width / fragments
        val stepY = size.height / fragments
        val fragmentPaint = Paint()
        val centreAtRest = pointOnVisiblePage(
            if (iconCentreOnSettledPage.isSpecified) iconCentreOnSettledPage else viewportCenter,
            viewport,
            verticalPager
        )
        for (row in 0 until fragments) {
            for (col in 0 until fragments) {
                val seed = itemIndex * 149 + row * 53 + col * 97
                val midX = (col + 0.5f) * stepX
                val midY = (row + 0.5f) * stepY
                val left = col * stepX
                val top = row * stepY
                val right = if (col == fragments - 1) size.width else left + stepX
                val bottom = if (row == fragments - 1) size.height else top + stepY
                val tx: Float
                val ty: Float
                val fragmentScale: Float
                val turn: Float
                val fragmentAlpha: Float
                val fragmentCentre = centreAtRest + Offset(
                    midX - size.width * 0.5f,
                    midY - size.height * 0.5f
                )
                when (effect) {
                    AdvancedDrawerIconMotion.FIREWORK -> {
                        val angle = fragmentRandom(seed) * 2f * PI.toFloat()
                        val rayX = cos(angle)
                        val rayY = sin(angle)
                        val exitDistance = distanceToViewportEdge(
                            fragmentCentre, rayX, rayY, viewport
                        ) + max(stepX, stepY) * 1.6f
                        val flight = phase.pow(1.15f) * exitDistance
                        tx = rayX * flight
                        ty = rayY * flight
                        turn = (fragmentRandom(seed + 29) - 0.5f) * phase * 500f
                        fragmentScale = 1f
                        // Opacity starts to decrease immediately after the
                        // fragments separate, smoothly across the whole flight.
                        // Midway between the original and 2x fade speeds:
                        // fully transparent at 2/3 of the swipe (1.5x rate).
                        fragmentAlpha = (1f - 1.5f * progress).coerceIn(0f, 1f)
                    }
                    AdvancedDrawerIconMotion.BLACK_HOLE -> {
                        val distanceNorm = (hypot(
                            fragmentCentre.x - viewportCenter.x,
                            fragmentCentre.y - viewportCenter.y
                        ) / hypot(viewport.width, viewport.height)).coerceIn(0f, 1f)
                        val converge = phase.pow(0.55f + distanceNorm * 1.25f)
                        tx = (viewportCenter.x - fragmentCentre.x) * converge
                        ty = (viewportCenter.y - fragmentCentre.y) * converge
                        turn = (fragmentRandom(seed + 7) - 0.5f) * phase * 90f
                        fragmentScale = 1f
                        fragmentAlpha = 1f
                    }
                    AdvancedDrawerIconMotion.BIG_BANG -> {
                        // Restore the approved 0.46.7 entering animation:
                        // all 25 fragments start at the COMMON page centre,
                        // then fly to their own cells without changing size.
                        tx = (viewportCenter.x - fragmentCentre.x) * phase
                        ty = (viewportCenter.y - fragmentCentre.y) * phase
                        turn = (fragmentRandom(seed + 37) - 0.5f) * phase * 170f
                        fragmentScale = 1f
                        fragmentAlpha = 1f
                    }
                    AdvancedDrawerIconMotion.SPIRAL -> error("Spiral has no fragments")
                }
                withTransform({
                    translate(left = tx, top = ty)
                    rotate(degrees = turn, pivot = Offset(midX, midY))
                    scale(scaleX = fragmentScale, scaleY = fragmentScale, pivot = Offset(midX, midY))
                }) {
                    fragmentPaint.alpha = fragmentAlpha
                    drawContext.canvas.saveLayer(Rect(left, top, right, bottom), fragmentPaint)
                    clipRect(left = left, top = top, right = right, bottom = bottom) {
                        drawLayer(layer)
                    }
                    drawContext.canvas.restore()
                }
            }
        }
    }
}
