package org.collegiumdesideriorum.e_e_launcher

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import kotlin.math.PI
import kotlin.math.floor
import kotlin.math.sin

/** Draws the hot boundary; the page reveal itself remains an Android Mesh. */
@Composable
internal fun FireTransitionBand(
    progress: Float,
    direction: Float,
    modifier: Modifier = Modifier,
    vertical: Boolean = false
) {
    Canvas(modifier) {
        // The particles, glowing band and Mesh reveal must use the SAME axis.
        // For vertical page changes the strip becomes horizontal, with its
        // uneven edge running along the viewport width.
        val axisExtent = if (vertical) size.height else size.width
        val crossExtent = if (vertical) size.width else size.height
        val halfThickness = axisExtent * 0.027f
        val center = (if (direction > 0f) 1f - progress else progress) * axisExtent
        fun position(cross: Float, along: Float): Offset =
            if (vertical) Offset(cross, along) else Offset(along, cross)

        val steps = 48
        val band = Path().apply {
            for (index in 0..steps) {
                val fraction = index / steps.toFloat()
                val along = fireBoundaryFraction(fraction, progress, direction) * axisExtent
                val p = position(fraction * crossExtent, along - halfThickness)
                if (index == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
            }
            for (index in steps downTo 0) {
                val fraction = index / steps.toFloat()
                val along = fireBoundaryFraction(fraction, progress, direction) * axisExtent
                val p = position(fraction * crossExtent, along + halfThickness)
                lineTo(p.x, p.y)
            }
            close()
        }
        val colors = arrayOf(
            0f to Color.Transparent,
            0.28f to Color(0x99FF3D00),
            0.50f to Color(0xFFFFF176),
            0.72f to Color(0xCCFF6D00),
            1f to Color.Transparent
        )
        drawPath(
            band,
            brush = if (vertical) Brush.verticalGradient(
                *colors, startY = center - halfThickness, endY = center + halfThickness
            ) else Brush.horizontalGradient(
                *colors, startX = center - halfThickness, endX = center + halfThickness
            )
        )
        val hotEdge = Path().apply {
            for (index in 0..steps) {
                val fraction = index / steps.toFloat()
                val along = fireBoundaryFraction(fraction, progress, direction) * axisExtent
                val p = position(fraction * crossExtent, along)
                if (index == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
            }
        }
        drawPath(hotEdge, Color(0xFFFFF8B0), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.2f))

        repeat(512) { index ->
            val seedX = fireHash(index * 71 + 13)
            val seedY = fireHash(index * 43 + 29)
            val life = fractional(progress * 1.35f + fireHash(index * 97 + 7))
            val cross = fractional(seedY - life * (0.10f + fireHash(index * 31) * 0.22f))
            val edge = fireBoundaryFraction(cross, progress, direction) * axisExtent
            val drift = (seedX - 0.5f) * halfThickness * (1.2f + life * 3.8f)
            val backward = -direction * life * halfThickness * 1.4f
            val alpha = (sin(life * PI.toFloat()) * (0.48f + seedX * 0.52f)).coerceIn(0f, 1f)
            val radius = (2.2f + fireHash(index * 59 + 3) * 5.8f) * density
            drawCircle(
                color = if (index % 5 == 0) Color(0xFFFFF59D) else Color(0xFFFF6D00),
                radius = radius,
                center = position(cross * crossExtent, edge + drift + backward),
                alpha = alpha
            )
        }
    }
}

internal fun fireBoundaryFraction(normalizedY: Float, progress: Float, direction: Float): Float {
    val boundary = if (direction > 0f) 1f - progress else progress
    return (
        boundary +
            sin(normalizedY * PI.toFloat() * 9f + progress * 15f) * 0.012f +
            sin(normalizedY * PI.toFloat() * 23f - progress * 9f) * 0.006f
        ).coerceIn(-0.04f, 1.04f)
}

private fun fireHash(seed: Int): Float {
    val value = sin(seed * 12.9898f + 78.233f) * 43758.547f
    return value - floor(value)
}

private fun fractional(value: Float): Float = value - floor(value)
