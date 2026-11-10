package org.collegiumdesideriorum.e_e_launcher

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

/** Geometry returned by the reusable presentation-style Mesh transition family. */
internal data class MeshTransitionVertex(
    val x: Float,
    val y: Float,
    val u: Float,
    val v: Float,
    val shade: Float = 1f,
    val opacity: Float = 1f
)

internal object MeshTransitionGeometry {
    /**
     * [direction] is the shared visual direction of the transition pair. The
     * caller normalizes the opposite pager-offset signs before entering here.
     */
    fun deform(
        effect: PageTransitionEffect,
        u: Float,
        v: Float,
        progress: Float,
        direction: Float,
        width: Float,
        height: Float,
        outgoingPage: Boolean,
        verticalPager: Boolean
    ): MeshTransitionVertex? = when (effect) {
        PageTransitionEffect.BLINDS_VERTICAL -> stationaryReveal(
            u, v, width, height, outgoingPage,
            revealed = blindReveal(u, progress, direction, 12)
        )
        PageTransitionEffect.BLINDS_HORIZONTAL -> stationaryReveal(
            u, v, width, height, outgoingPage,
            revealed = blindReveal(v, progress, direction, 10)
        )
        PageTransitionEffect.CHECKERBOARD -> stationaryReveal(
            u, v, width, height, outgoingPage,
            revealed = checkerReveal(u, v, progress, direction)
        )
        PageTransitionEffect.TRIANGLE_WIPE -> stationaryReveal(
            u, v, width, height, outgoingPage,
            revealed = edgeDistance(u, direction) + abs(v - 0.5f) * 0.88f <= progress * 1.44f
        )
        PageTransitionEffect.ARC_WIPE -> stationaryReveal(
            u, v, width, height, outgoingPage,
            revealed = sqrt(
                edgeDistance(u, direction) * edgeDistance(u, direction) +
                    (v - 0.5f) * (v - 0.5f) * 0.58f
            ) <= progress * 1.12f
        )
        PageTransitionEffect.CIRCLE_WIPE -> stationaryReveal(
            u, v, width, height, outgoingPage,
            revealed = radialReveal(u, v, progress, width, height, oval = false)
        )
        PageTransitionEffect.OVAL_WIPE -> stationaryReveal(
            u, v, width, height, outgoingPage,
            revealed = radialReveal(u, v, progress, width, height, oval = true)
        )
        PageTransitionEffect.RANDOM_BARS -> stationaryReveal(
            u, v, width, height, outgoingPage,
            revealed = randomBarReveal(u, v, progress, direction, horizontalBars = true)
        )
        PageTransitionEffect.STRIPS_VERTICAL_MESH -> stationaryReveal(
            u, v, width, height, outgoingPage,
            revealed = randomBarReveal(u, v, progress, direction, horizontalBars = false)
        )
        PageTransitionEffect.THREAD_WIPE -> stationaryReveal(
            u, v, width, height, outgoingPage,
            revealed = edgeDistance(u, direction) <= (
                progress + sin(v * PI.toFloat() * 15f + progress * 4f) * 0.034f
                ).coerceIn(0f, 1f)
        )
        PageTransitionEffect.DIAGONAL_WIPE -> stationaryReveal(
            u, v, width, height, outgoingPage,
            revealed = edgeDistance(u, direction) +
                (if (direction > 0f) v else 1f - v) * 0.58f <= progress * 1.58f
        )
        PageTransitionEffect.GLITTER -> glitter(u, v, progress, width, height, outgoingPage)
        PageTransitionEffect.VORTEX -> vortex(
            u, v, progress, direction, width, height, outgoingPage
        )
        PageTransitionEffect.INTERLACED_BLINDS_VERTICAL -> interlacedBlind(
            u, v, progress, direction, width, height, outgoingPage, verticalAxis = true
        )
        PageTransitionEffect.INTERLACED_BLINDS_HORIZONTAL -> interlacedBlind(
            u, v, progress, direction, width, height, outgoingPage, verticalAxis = false
        )
        else -> null
    }

    private fun stationaryReveal(
        u: Float,
        v: Float,
        width: Float,
        height: Float,
        outgoingPage: Boolean,
        revealed: Boolean,
        shade: Float = 1f
    ): MeshTransitionVertex {
        val opacity = if (outgoingPage == revealed) 0f else 1f
        return MeshTransitionVertex(u * width, v * height, u, v, shade, opacity)
    }

    private fun blindReveal(coordinate: Float, progress: Float, direction: Float, count: Int): Boolean {
        val scaled = coordinate.coerceIn(0f, 0.999999f) * count
        val local = scaled - floor(scaled)
        val opening = if (direction > 0f) 1f - local else local
        return opening <= smoothStep(progress)
    }

    private fun checkerReveal(u: Float, v: Float, progress: Float, direction: Float): Boolean {
        val columns = 10
        val rows = 14
        val column = minOf((u.coerceIn(0f, 0.999999f) * columns).toInt(), columns - 1)
        val row = minOf((v.coerceIn(0f, 0.999999f) * rows).toInt(), rows - 1)
        val directedColumn = if (direction > 0f) columns - 1 - column else column
        val diagonalRank = (directedColumn + row).toFloat() / (columns + rows - 2f)
        val parityDelay = ((column + row) and 1) * 0.16f
        return progress >= diagonalRank * 0.72f + parityDelay
    }

    private fun randomBarReveal(
        u: Float,
        v: Float,
        progress: Float,
        direction: Float,
        horizontalBars: Boolean
    ): Boolean {
        val barCount = if (horizontalBars) 30 else 20
        val barCoordinate = if (horizontalBars) v else u
        val bar = minOf((barCoordinate.coerceIn(0f, 0.999999f) * barCount).toInt(), barCount - 1)
        val delay = hash(bar * 37 + 11) * 0.72f
        val localProgress = ((progress - delay) / 0.28f).coerceIn(0f, 1f)
        val travelCoordinate = if (horizontalBars) u else v
        // A forward/left swipe gives the outgoing page a POSITIVE Pager offset.
        // edgeDistance(direction > 0) starts at the right edge and moves left;
        // reversing it made this effect travel opposite to the gesture.
        val travelDirection = direction
        return edgeDistance(travelCoordinate, travelDirection) <= smoothStep(localProgress)
    }

    private fun glitter(
        u: Float,
        v: Float,
        progress: Float,
        width: Float,
        height: Float,
        outgoingPage: Boolean
    ): MeshTransitionVertex {
        val columns = 22
        val rows = 30
        val column = minOf((u.coerceIn(0f, 0.999999f) * columns).toInt(), columns - 1)
        val row = minOf((v.coerceIn(0f, 0.999999f) * rows).toInt(), rows - 1)
        val threshold = hash(column * 101 + row * 47 + 19) * 0.88f
        val revealed = progress >= threshold
        val sparkle = (1f - abs(progress - threshold) / 0.075f).coerceIn(0f, 1f)
        return stationaryReveal(
            u, v, width, height, outgoingPage, revealed,
            shade = 1f + sparkle * 0.35f
        )
    }

    private fun vortex(
        u: Float,
        v: Float,
        progress: Float,
        direction: Float,
        width: Float,
        height: Float,
        outgoingPage: Boolean
    ): MeshTransitionVertex {
        val dx = u - 0.5f
        val dy = (v - 0.5f) * height / width.coerceAtLeast(1f)
        val radius = sqrt(dx * dx + dy * dy)
        val maxRadius = sqrt(0.25f + 0.25f * height * height / (width * width).coerceAtLeast(1f))
        val twist = direction * sin(progress * PI.toFloat()) *
            (1f - radius.coerceIn(0f, maxRadius) / maxRadius.coerceAtLeast(0.01f)) * 2.2f
        val rotatedX = dx * cos(twist) - dy * sin(twist)
        val rotatedY = dx * sin(twist) + dy * cos(twist)
        val revealed = radius <= smoothStep(progress) * maxRadius
        return MeshTransitionVertex(
            (0.5f + rotatedX) * width,
            (0.5f + rotatedY * width / height.coerceAtLeast(1f)) * height,
            u,
            v,
            0.72f + 0.28f * abs(cos(twist)),
            if (outgoingPage == revealed) 0f else 1f
        )
    }

    private fun radialReveal(
        u: Float,
        v: Float,
        progress: Float,
        width: Float,
        height: Float,
        oval: Boolean
    ): Boolean {
        val dx = (u - 0.5f) * width
        val dy = (v - 0.5f) * height
        val distance: Float
        val cornerDistance: Float
        if (oval) {
            // Fixed physical axes keep this visibly oval on both portrait and landscape pages.
            val base = minOf(width, height).coerceAtLeast(1f)
            val radiusX = base * 0.34f
            val radiusY = base * 0.62f
            distance = sqrt(dx * dx / (radiusX * radiusX) + dy * dy / (radiusY * radiusY))
            cornerDistance = sqrt(
                width * width * 0.25f / (radiusX * radiusX) +
                    height * height * 0.25f / (radiusY * radiusY)
            )
        } else {
            distance = sqrt(dx * dx + dy * dy)
            cornerDistance = sqrt(width * width * 0.25f + height * height * 0.25f)
        }
        return distance <= smoothStep(progress) * cornerDistance
    }

    private fun interlacedBlind(
        u: Float,
        v: Float,
        progress: Float,
        direction: Float,
        width: Float,
        height: Float,
        outgoingPage: Boolean,
        verticalAxis: Boolean
    ): MeshTransitionVertex {
        val columns = 8
        val rows = 12
        val safeU = u.coerceIn(0f, 0.999999f)
        val safeV = v.coerceIn(0f, 0.999999f)
        val column = minOf((safeU * columns).toInt(), columns - 1)
        val row = minOf((safeV * rows).toInt(), rows - 1)
        // The horizontal-labelled Mesh variant turns around vertical hinges.
        // Its visual procession should follow the swipe instead of opposing it.
        val travelColumn = if (verticalAxis && direction > 0f) columns - 1 - column else column
        val index = if (verticalAxis) travelColumn * rows + row else row * columns + column
        val total = columns * rows
        val parity = index and 1
        val rank = index / 2
        val parityCount = (total + 1) / 2
        val start = parity * 0.5f + rank.toFloat() / parityCount * 0.43f
        val local = ((progress - start) / 0.07f).coerceIn(0f, 1f)
        val orientedDirection = if (verticalAxis) -direction else direction
        val angle = smoothStep(local) * PI.toFloat() * orientedDirection
        val cellLeft = column / columns.toFloat()
        val cellTop = row / rows.toFloat()
        val localU = (safeU * columns - column).coerceIn(0f, 1f)
        val localV = (safeV * rows - row).coerceIn(0f, 1f)
        val cellCenterX = (cellLeft + 0.5f / columns) * width
        val cellCenterY = (cellTop + 0.5f / rows) * height
        val x: Float
        val y: Float
        val z: Float
        if (verticalAxis) {
            val dx = (localU - 0.5f) * width / columns
            x = cellCenterX + dx * cos(angle)
            y = v * height
            z = dx * sin(angle)
        } else {
            val dy = (localV - 0.5f) * height / rows
            x = u * width
            y = cellCenterY + dy * cos(angle)
            z = dy * sin(angle)
        }
        val projected = perspective(x, y, z, width, height)
        val turned = local >= 0.5f
        val textureU = if (verticalAxis && turned) cellLeft + (1f - localU) / columns else u
        val textureV = if (!verticalAxis && turned) cellTop + (1f - localV) / rows else v
        return MeshTransitionVertex(
            projected.first,
            projected.second,
            textureU,
            textureV,
            0.48f + 0.52f * abs(cos(angle)),
            if (outgoingPage == turned) 0f else 1f
        )
    }

    private fun edgeDistance(coordinate: Float, direction: Float): Float =
        if (direction > 0f) 1f - coordinate else coordinate

    private fun perspective(
        x: Float,
        y: Float,
        z: Float,
        width: Float,
        height: Float
    ): Pair<Float, Float> {
        val camera = max(width, height) * 1.75f
        val scale = camera / (camera - z.coerceAtMost(camera * 0.78f))
        return Pair(
            width * 0.5f + (x - width * 0.5f) * scale,
            height * 0.5f + (y - height * 0.5f) * scale
        )
    }

    private fun hash(seed: Int): Float {
        val value = sin(seed * 12.9898f + 78.233f) * 43758.547f
        return value - floor(value)
    }

    private fun smoothStep(value: Float): Float {
        val safe = value.coerceIn(0f, 1f)
        return safe * safe * (3f - 2f * safe)
    }
}
