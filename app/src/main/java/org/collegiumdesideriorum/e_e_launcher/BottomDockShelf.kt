package org.collegiumdesideriorum.e_e_launcher

import androidx.compose.foundation.background
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * The platform's actual outline. The former graphicsLayer used a rounded-rectangle shadow
 * behind this curved silhouette; that rectangle showed through on translucent/light shelves.
 */
private object PlatformShelfOutline : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val h = size.height
        val shoulder = (h * 0.85f).coerceAtMost(size.width * 0.09f)
        return Outline.Generic(Path().apply {
            moveTo(0f, h)
            quadraticTo(0f, h * 0.78f, shoulder, h * 0.18f)
            quadraticTo(shoulder * 1.35f, 0f, shoulder * 2.1f, 0f)
            lineTo(size.width - shoulder * 2.1f, 0f)
            quadraticTo(size.width - shoulder * 1.35f, 0f, size.width - shoulder, h * 0.18f)
            quadraticTo(size.width, h * 0.78f, size.width, h)
            close()
        })
    }
}

private fun dockShelfOutline(shelfShape: DockShelfShape): Shape = when (shelfShape) {
    DockShelfShape.NONE -> RectangleShape
    DockShelfShape.PLATFORM -> PlatformShelfOutline
    DockShelfShape.ROUNDED -> RoundedCornerShape(10.dp)
    DockShelfShape.PILL -> RoundedCornerShape(50)
    DockShelfShape.RECTANGLE -> RoundedCornerShape(1.dp)
}

/**
 * Draw the outline shadows OUTSIDE the clipped HorizontalPager, but still behind the dock pages.
 * Neither this host nor the parent dock clips its children: the shadow can soften across
 * the existing 12 dp side margins instead of being cut at the ends of the shelf.
 * During a swipe, the current and entering page shadows track their moving shelves.
 * This modifier has no pointer input; drag/drop and icon hit areas remain unchanged.
 */
@Composable
internal fun DockPageShelfShadows(
    shelfHeightDp: Int,
    shelfVerticalOffsetDp: Int,
    shelfShape: DockShelfShape,
    shelfOpacityPercent: Int,
    pageWidthDp: Dp,
    pagerState: PagerState,
    modifier: Modifier = Modifier
) {
    if (shelfHeightDp <= 0 || shelfShape == DockShelfShape.NONE || shelfOpacityPercent <= 0) return

    // Read the animated offset here, not in PagedBottomDock: only the lightweight shadow
    // layer should recompose during swipes, not all of the dock's icon and drag UI.
    val pageOffsetFraction = pagerState.currentPageOffsetFraction
    val density = LocalDensity.current
    val shelfHeight = shelfHeightDp.coerceIn(1, 92)
    val topDp = 92 - shelfHeight + shelfVerticalOffsetDp.coerceIn(-32, 32)
    // Match the page's original exact width; shadow blur is allowed beyond those bounds.
    val widthPx = with(density) { pageWidthDp.toPx() }
    val topPx = with(density) { topDp.dp.toPx() }
    val outline = dockShelfOutline(shelfShape)

    // At rest, don't let invisible neighbor pages cast shadows into the visible page.
    // During a drag only the entering neighbor needs its own moving shadow.
    val visiblePages = when {
        pageOffsetFraction > 0.02f -> 0..1
        pageOffsetFraction < -0.02f -> -1..0
        else -> 0..0
    }

    // Android draws an elevated layer's shadow THROUGH its own translucent surface.
    // On PLATFORM this looks like a smaller, second shelf (most visible at 1..99% alpha).
    // Mask only the INSIDE of the shelf silhouettes out of the shadow pass. The actual
    // semi-transparent colored shelf is still painted by DockPageBackground, unchanged.
    // Keep this mask outside HorizontalPager so the outer blur is not side-clipped.
    Box(
        modifier
            .width(pageWidthDp)
            .height(92.dp)
            .drawWithContent {
                val shelfHeightPx = with(density) { shelfHeight.dp.toPx() }
                val shelfOutline = outline.createOutline(
                    Size(widthPx, shelfHeightPx), layoutDirection, this
                )
                val contour = when (shelfOutline) {
                    is Outline.Generic -> shelfOutline.path
                    is Outline.Rounded -> Path().apply { addRoundRect(shelfOutline.roundRect) }
                    is Outline.Rectangle -> Path().apply { addRect(shelfOutline.rect) }
                }
                val silhouettes = Path().apply {
                    for (relativePage in visiblePages) {
                        // Use the same integer-pixel displacement as Modifier.offset below.
                        addPath(
                            contour,
                            Offset(
                                ((relativePage - pageOffsetFraction) * widthPx).roundToInt().toFloat(),
                                topPx.roundToInt().toFloat()
                            )
                        )
                    }
                }
                clipPath(silhouettes, clipOp = ClipOp.Difference) {
                    this@drawWithContent.drawContent()
                }
            }
    ) {
        for (relativePage in visiblePages) {
            Box(
                Modifier
                    .offset {
                        IntOffset(
                            ((relativePage - pageOffsetFraction) * widthPx).roundToInt(),
                            topPx.roundToInt()
                        )
                    }
                    .width(pageWidthDp)
                    .height(shelfHeight.dp)
                    .graphicsLayer {
                        shape = outline
                        clip = false
                        shadowElevation = 8.dp.toPx()
                    }
            )
        }
    }
}

/** Background owned by one dock page, so the shelf surface travels with that page. */
@Composable
internal fun DockPageBackground(
    shelfHeightDp: Int,
    shelfVerticalOffsetDp: Int,
    shelfShape: DockShelfShape,
    shelfColorHex: String,
    shelfOpacityPercent: Int,
    backgroundColorHex: String,
    backgroundOpacityPercent: Int,
    modifier: Modifier = Modifier
) {
    val shelfVisible = shelfHeightDp > 0 && shelfShape != DockShelfShape.NONE
    Box(modifier.fillMaxSize()) {
        if (!shelfVisible) {
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(
                    containerColor = parseAppFrameColor(backgroundColorHex).copy(
                        alpha = backgroundOpacityPercent.coerceIn(0, 100) / 100f
                    )
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (backgroundOpacityPercent > 0) 8.dp else 0.dp
                )
            ) {}
            return@Box
        }

        val shelfColor = parseAppFrameColor(shelfColorHex).copy(
            alpha = shelfOpacityPercent.coerceIn(0, 100) / 100f
        )
        // No rectangular graphicsLayer shadow here: that used to create a second, unrelated
        // silhouette under the translucent platform and caused the horizontal color band.
        // Shadow is now drawn in the wider, unclipped dock host by DockPageShelfShadows.
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(shelfHeightDp.coerceIn(1, 92).dp)
                .offset(y = shelfVerticalOffsetDp.coerceIn(-32, 32).dp)
                .background(shelfColor, dockShelfOutline(shelfShape))
        )
    }
}
