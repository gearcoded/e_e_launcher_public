package org.collegiumdesideriorum.e_e_launcher

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.layer.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import kotlin.math.absoluteValue

/**
 * Canvas approximation of the ordinary shared Mesh cube. The two pages use
 * the same global turn and camera projection, rendered through thin vertical
 * Canvas strips instead of the Android 14 Mesh API. The Pager's slide is
 * cancelled so the faces meet at the same screen-space cube edge.
 */
@Composable
internal fun CanvasCubePageSurface(
    signedPageOffset: Float,
    verticalPager: Boolean,
    isOutgoingPage: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val offset = signedPageOffset.coerceIn(-1f, 1f)
    val visibility = 1f - offset.absoluteValue
    val progress = (if (isOutgoingPage) 1f - visibility else visibility).coerceIn(0f, 1f)
    val direction = when {
        isOutgoingPage -> if (offset < 0f) -1f else 1f
        offset > 0f -> -1f
        else -> 1f
    }
    val faceVisible = if (isOutgoingPage) progress < 0.82f else progress >= 0.18f
    // Keep the full, live Compose page in one reusable GPU display layer.
    // Previously drawContent() ran for each of the 32 cube strips, which
    // re-issued every icon, folder and widget draw 32 times per frame.
    val pageLayer = rememberGraphicsLayer()
    // Explicit offscreen composition: without this hint a recorded layer may
    // replay its child draw commands for every strip instead of compositing
    // one cached GPU surface. Refresh that surface each frame for live content.
    SideEffect { pageLayer.compositingStrategy = CompositingStrategy.Offscreen }

    Box(
        modifier = modifier.fillMaxSize()
            .graphicsLayer {
                if (verticalPager) translationY = offset * size.height
                else translationX = offset * size.width
                alpha = if (faceVisible) 1f else 0f
            }
            .drawWithContent {
                if (!faceVisible) return@drawWithContent
                if ((isOutgoingPage && progress <= 0.001f) ||
                    (!isOutgoingPage && progress >= 0.999f)) {
                    drawContent()
                    return@drawWithContent
                }
                val width = size.width
                val height = size.height
                if (width <= 1f || height <= 1f) return@drawWithContent

                // A moderate strip count approximates the varying 3D
                // perspective while remaining cheaper than a full per-pixel
                // deformation. No graphicsLayer rotation or Mesh drawing.
                // Record the current content ONCE per draw. Replaying the
                // recorded layer per clipped strip preserves live animations
                // (including widget snapshots) without re-drawing all children
                // separately for each strip.
                pageLayer.record { this@drawWithContent.drawContent() }
                val projection = CanvasCubeProjection.frame(
                    progress, direction, isOutgoingPage, width, height, verticalPager
                )
                val sections = 32
                val extent = if (verticalPager) height else width
                val sectionSize = extent / sections
                val shade = (1f - CanvasCubeProjection.shading(progress, direction, isOutgoingPage))
                    .coerceIn(0f, 1f) * 0.45f
                repeat(sections) { index ->
                    val sourceStart = index * sectionSize
                    val sourceEnd = if (index == sections - 1) extent else sourceStart + sectionSize
                    val a: CanvasCubeProjection.Position
                    val b: CanvasCubeProjection.Position
                    val topLeft: CanvasCubeProjection.Position
                    val bottomRight: CanvasCubeProjection.Position
                    if (!verticalPager) {
                        val u0 = sourceStart / width
                        val u1 = sourceEnd / width
                        val midU = (u0 + u1) * 0.5f
                        a = projection.point(u0, 0.5f)
                        b = projection.point(u1, 0.5f)
                        topLeft = projection.point(midU, 0f)
                        bottomRight = projection.point(midU, 1f)
                    } else {
                        val v0 = sourceStart / height
                        val v1 = sourceEnd / height
                        val midV = (v0 + v1) * 0.5f
                        a = projection.point(0.5f, v0)
                        b = projection.point(0.5f, v1)
                        topLeft = projection.point(0f, midV)
                        bottomRight = projection.point(1f, midV)
                    }
                    val scaleX = if (verticalPager) (bottomRight.x - topLeft.x) / width
                        else (b.x - a.x) / (sourceEnd - sourceStart)
                    val scaleY = if (verticalPager) (b.y - a.y) / (sourceEnd - sourceStart)
                        else (bottomRight.y - topLeft.y) / height
                    val destX = if (verticalPager) topLeft.x else a.x
                    val destY = if (verticalPager) a.y else topLeft.y
                    val srcX = if (verticalPager) 0f else sourceStart
                    val srcY = if (verticalPager) sourceStart else 0f
                    if (scaleX > 0.00001f && scaleY > 0.00001f) {
                        withTransform({
                            translate(left = destX - srcX * scaleX, top = destY - srcY * scaleY)
                            scale(scaleX = scaleX, scaleY = scaleY, pivot = Offset.Zero)
                        }) {
                            // A slight overlap reduces seams between the
                            // affine approximations of adjacent cube strips.
                            val left = if (verticalPager) 0f else sourceStart
                            val top = if (verticalPager) sourceStart else 0f
                            val right = if (verticalPager) width else sourceEnd + 0.4f
                            val bottom = if (verticalPager) sourceEnd + 0.4f else height
                            clipRect(left = left, top = top, right = right, bottom = bottom) {
                                drawLayer(pageLayer)
                                if (shade > 0f) {
                                    drawRect(Color.Black.copy(alpha = shade),
                                        topLeft = Offset(left, top),
                                        size = Size(right - left, bottom - top))
                                }
                            }
                        }
                    }
                }
            }
    ) { content() }
}
