package org.collegiumdesideriorum.e_e_launcher

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/**
 * Two faces of the Mesh shared Cube projected into the flat Compose Canvas.
 * Rendering uses column strips; unlike the native API-34 Mesh this operates
 * entirely through the Canvas drawing pipeline (no android.graphics.Mesh).
 */
internal object CanvasCubeProjection {
    data class Position(val x: Float, val y: Float)

    /** Prepare the trigonometry once per frame, not once for every strip end. */
    class Frame internal constructor(
        val progress: Float,
        val direction: Float,
        val outgoing: Boolean,
        val width: Float,
        val height: Float,
        val vertical: Boolean
    ) {
        private val t = progress.coerceIn(0f, 1f)
        private val wrap = smoothStep((t / 0.18f).coerceIn(0f, 1f)) *
            smoothStep(((1f - t) / 0.18f).coerceIn(0f, 1f))
        private val turn = -direction * t * (PI.toFloat() / 2f)
        private val cosTurn = cos(turn)
        private val sinTurn = sin(turn)
        private val camera = max(width, height) * 1.75f
        private val zoom = 1f - 0.16f * wrap

        fun point(u: Float, v: Float): Position {
            if (width <= 0f || height <= 0f) return Position(0f, 0f)
            val flatX = u * width
            val flatY = v * height
            val cubeX: Float
            val cubeY: Float
            val cubeZ: Float
            if (!vertical) {
                val half = width * 0.5f
                val localX: Float
                val localZ: Float
                if (outgoing) {
                    localX = (u - 0.5f) * width
                    localZ = half
                } else {
                    val orientedU = if (direction > 0f) u else 1f - u
                    localX = direction * half
                    localZ = half - orientedU * width
                }
                cubeX = width * 0.5f + localX * cosTurn + localZ * sinTurn
                cubeY = flatY
                cubeZ = -localX * sinTurn + localZ * cosTurn - half
            } else {
                val half = height * 0.5f
                val localY: Float
                val localZ: Float
                if (outgoing) {
                    localY = (v - 0.5f) * height
                    localZ = half
                } else {
                    val orientedV = if (direction > 0f) v else 1f - v
                    localY = direction * half
                    localZ = half - orientedV * height
                }
                cubeX = flatX
                cubeY = height * 0.5f + localY * cosTurn + localZ * sinTurn
                cubeZ = -localY * sinTurn + localZ * cosTurn - half
            }
            val x = flatX + (cubeX - flatX) * wrap
            val y = flatY + (cubeY - flatY) * wrap
            val z = cubeZ * wrap
            val perspective = camera / (camera - z.coerceAtMost(camera * 0.78f))
            return Position(
                width * 0.5f + (x - width * 0.5f) * perspective * zoom,
                height * 0.5f + (y - height * 0.5f) * perspective * zoom
            )
        }
    }

    fun frame(progress: Float, direction: Float, outgoing: Boolean,
              width: Float, height: Float, vertical: Boolean = false): Frame =
        Frame(progress, direction, outgoing, width, height, vertical)

    /** Retained to keep geometry tests and any other callers source-compatible. */
    fun point(
        u: Float, v: Float, progress: Float, direction: Float,
        outgoing: Boolean, width: Float, height: Float, vertical: Boolean = false
    ): Position = frame(progress, direction, outgoing, width, height, vertical).point(u, v)

    fun shading(progress: Float, direction: Float, outgoing: Boolean): Float {
        val turn = -direction * progress.coerceIn(0f, 1f) * PI.toFloat() / 2f
        val light = if (outgoing) abs(cos(turn)) else abs(-direction * sin(turn))
        val wrap = smoothStep((progress / 0.18f).coerceIn(0f, 1f)) *
            smoothStep(((1f - progress) / 0.18f).coerceIn(0f, 1f))
        return 1f + (0.52f + 0.48f * light - 1f) * wrap
    }

    private fun smoothStep(t: Float): Float = t * t * (3f - 2f * t)
}
