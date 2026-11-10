package org.collegiumdesideriorum.e_e_launcher

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.BlendMode
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Mesh
import android.graphics.MeshSpecification
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.AttributeSet
import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Shares captured textures between virtual copies of the same logical page.
 *
 * Infinite pagers create a fresh virtual item whenever they pass the logical
 * first/last-page boundary. Without this cache that item can be swiped before
 * its asynchronous texture capture completes, leaving only half of a paired
 * effect or its cross-fade fallback visible.
 */
@Stable
internal class MeshPageSnapshotCache(private val maxEntries: Int) {
    private val bitmaps = mutableStateMapOf<Any, Bitmap>()
    private val accessOrder = java.util.ArrayDeque<Any>()

    operator fun get(key: Any): Bitmap? = bitmaps[key]

    fun remove(key: Any) {
        bitmaps.remove(key)
        accessOrder.remove(key)
    }

    fun put(key: Any, bitmap: Bitmap) {
        bitmaps[key] = bitmap
        accessOrder.remove(key)
        accessOrder.addLast(key)
        while (accessOrder.size > maxEntries.coerceAtLeast(2)) {
            bitmaps.remove(accessOrder.removeFirst())
        }
    }
}

@Composable
internal fun rememberMeshPageSnapshotCache(maxEntries: Int = 64): MeshPageSnapshotCache =
    remember(maxEntries) { MeshPageSnapshotCache(maxEntries) }

@Stable
internal class PageGestureTuningState {
    var travelFraction by mutableFloatStateOf(1f)
        private set
    var released by mutableStateOf(false)
        private set

    fun begin(startFraction: Float) {
        // A gesture starting at an edge has a longer path than one starting near the centre.
        travelFraction = (0.48f + kotlin.math.abs(startFraction - 0.5f) * 1.04f).coerceIn(0.48f, 1f)
        released = false
    }

    fun release() {
        released = true
    }

    fun reset() {
        travelFraction = 1f
        released = false
    }
}

@Composable
internal fun rememberPageGestureTuningState(): PageGestureTuningState =
    remember { PageGestureTuningState() }

internal fun Modifier.trackPageGestureTuning(
    state: PageGestureTuningState,
    vertical: Boolean,
    onSwipeStart: (() -> Unit)? = null
): Modifier = pointerInput(state, vertical) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        val extent = if (vertical) size.height.toFloat() else size.width.toFloat()
        val coordinate = if (vertical) down.position.y else down.position.x
        state.begin(if (extent > 0f) coordinate / extent else 0.5f)
        var swipeStarted = false
        while (true) {
            val event = awaitPointerEvent(pass = androidx.compose.ui.input.pointer.PointerEventPass.Initial)
            val change = event.changes.firstOrNull { it.id == down.id } ?: break
            if (!swipeStarted && change.pressed) {
                val primary = if (vertical) change.position.y - down.position.y
                    else change.position.x - down.position.x
                val secondary = if (vertical) change.position.x - down.position.x
                    else change.position.y - down.position.y
                if (primary.absoluteValue > viewConfiguration.touchSlop &&
                    primary.absoluteValue > secondary.absoluteValue * 1.15f) {
                    swipeStarted = true
                    onSwipeStart?.invoke()
                }
            }
            if (!change.pressed) break
        }
        state.release()
    }
}

internal fun PageTransitionEffect.isMeshCube(): Boolean = when (this) {
    PageTransitionEffect.CUBE_OUT_HORIZONTAL,
    PageTransitionEffect.CUBE_IN_HORIZONTAL,
    PageTransitionEffect.CUBE_OUT_VERTICAL,
    PageTransitionEffect.CUBE_IN_VERTICAL -> true
    else -> false
}

internal fun PageTransitionEffect.usesSharedMeshSurface(): Boolean =
    this == PageTransitionEffect.SHARED_CUBE ||
        this == PageTransitionEffect.CYLINDER ||
        this == PageTransitionEffect.SPHERE

internal fun PageTransitionEffect.usesPairedMeshTextures(): Boolean =
    isMeshCube() || usesSharedMeshSurface() ||
        this == PageTransitionEffect.CARD || this == PageTransitionEffect.DNA ||
        this == PageTransitionEffect.DNA_OUT ||
        this == PageTransitionEffect.RETRO_FLY_TILES || this == PageTransitionEffect.FIRE_REVEAL ||
        this == PageTransitionEffect.BLINDS_VERTICAL || this == PageTransitionEffect.BLINDS_HORIZONTAL ||
        this == PageTransitionEffect.CHECKERBOARD || this == PageTransitionEffect.TRIANGLE_WIPE ||
        this == PageTransitionEffect.ARC_WIPE || this == PageTransitionEffect.CIRCLE_WIPE ||
        this == PageTransitionEffect.OVAL_WIPE || this == PageTransitionEffect.RANDOM_BARS ||
        this == PageTransitionEffect.THREAD_WIPE ||
        this == PageTransitionEffect.DIAGONAL_WIPE || this == PageTransitionEffect.GLITTER ||
        this == PageTransitionEffect.VORTEX ||
        this == PageTransitionEffect.CHECKERBOARD_VERTICAL_MESH ||
        this == PageTransitionEffect.CHECKERBOARD_HORIZONTAL_MESH ||
        this == PageTransitionEffect.TRIANGLE_REVEAL_MESH ||
        this == PageTransitionEffect.DIAMOND_REVEAL_MESH ||
        this == PageTransitionEffect.RANDOM_BARS_SNAP_HORIZONTAL_MESH ||
        this == PageTransitionEffect.RANDOM_BARS_SNAP_VERTICAL_MESH ||
        this == PageTransitionEffect.STRIPS_VERTICAL_MESH ||
        this == PageTransitionEffect.PAGE_CURL_SOFT ||
        this == PageTransitionEffect.PAGE_CURL_OUTWARD ||
        this == PageTransitionEffect.CUBE_FROM_PAGER_INSIDE ||
        this == PageTransitionEffect.INTERLACED_BLINDS_VERTICAL ||
        this == PageTransitionEffect.INTERLACED_BLINDS_HORIZONTAL

internal val LocalMeshLiveContentVisible = staticCompositionLocalOf { true }
internal val LocalMeshCaptureWidgetSnapshot = staticCompositionLocalOf { false }
internal val LocalMeshSnapshotInvalidator = staticCompositionLocalOf<() -> Unit> { {} }

/**
 * Page effects shared by workspace pages and both paged app-drawer modes.
 * Mesh effects are rendered by the API 34 android.graphics.Mesh pipeline.
 */
internal enum class PageEffectDimension(val label: String) {
    TWO_D("2D"),
    THREE_D("3D")
}

/** Rendering families shown as collapsible sections in the effect picker. */
internal enum class PageEffectTechnologyGroup(val titleRes: Int) {
    SIMPLE(R.string.ui_simple_effects_pager_graphicslayer),
    COMPOSE_CANVAS(R.string.ui_compose_canvas_effects),
    ANDROID_MESH(R.string.ui_android_mesh_effects)
}

internal enum class PageTransitionEffect(
    override val titleRes: Int,
    override val descriptionRes: Int,
    val dimension: PageEffectDimension,
    val appDrawerOnly: Boolean = false,
    val mesh: Boolean = false,
    val oreoAlternative: Boolean = false
) : LocalizedTitle, LocalizedDescription {
    DEFAULT(R.string.ui_default, R.string.ui_standard_page_transition, PageEffectDimension.TWO_D),
    INSTANT(R.string.ui_none, R.string.ui_the_page_changes_after_the_gesture_passes_the_midpoint, PageEffectDimension.TWO_D, appDrawerOnly = true),
    CROSSFADE(R.string.ui_crossfade, R.string.ui_the_old_page_fades_out_while_the_new_page, PageEffectDimension.TWO_D),
    SCALE(R.string.ui_scale, R.string.ui_the_page_shrinks_as_it_moves_away_from_the, PageEffectDimension.TWO_D),
    ZOOM_OUT(R.string.ui_zoom_out, R.string.ui_the_incoming_page_shrinks_from_400_percent, PageEffectDimension.TWO_D),
    ROTATE(R.string.ui_rotate, R.string.ui_slight_page_rotation_and_lift, PageEffectDimension.TWO_D),
    WAVE(R.string.ui_diagonal_page_swipe, R.string.ui_vertical_movement_with_a_slight_tilt, PageEffectDimension.TWO_D),
    RETRO_FLY_TILES(R.string.ui_mesh_cutting, R.string.ui_sections_fly_out_and_arrive_sequentially_from_the_adjacent, PageEffectDimension.TWO_D, mesh = true),
    RETRO_FLY_TILES_STATIC(R.string.ui_space_shards, R.string.ui_sections_fly_without_moving_the_pages, PageEffectDimension.TWO_D, oreoAlternative = true),
    RETRO_FLY_TILES_OREO(R.string.ui_space_shards_scrolling, R.string.ui_sections_fly_out_and_arrive_sequentially_from_the_adjacent, PageEffectDimension.TWO_D, oreoAlternative = true),
    FIRE_REVEAL(R.string.ui_burning, R.string.ui_a_single_synchronised_fire_boundary_reveals_the_stationary_next, PageEffectDimension.TWO_D, mesh = true),
    FIRE_REVEAL_OREO(R.string.ui_canvas_burn_away, R.string.ui_a_single_synchronised_fire_boundary_reveals_the_stationary_next, PageEffectDimension.TWO_D, oreoAlternative = true),
    BLINDS_VERTICAL(R.string.ui_blinds_vertical, R.string.ui_vertical_slats_reveal_the_adjacent_page_simultaneously, PageEffectDimension.TWO_D, mesh = true),
    BLINDS_VERTICAL_OREO(R.string.ui_blinds_vertical, R.string.ui_vertical_slats_reveal_the_adjacent_page_simultaneously, PageEffectDimension.TWO_D, oreoAlternative = true),
    BLINDS_HORIZONTAL(R.string.ui_blinds_horizontal, R.string.ui_horizontal_slats_reveal_the_adjacent_page_simultaneously, PageEffectDimension.TWO_D, mesh = true),
    BLINDS_HORIZONTAL_OREO(R.string.ui_blinds_horizontal, R.string.ui_horizontal_slats_reveal_the_adjacent_page_simultaneously, PageEffectDimension.TWO_D, oreoAlternative = true),
    CHECKERBOARD(R.string.ui_mesh_checkerboard_diagonal, R.string.ui_checkerboard_cells_reveal_the_adjacent_page_in_sequence, PageEffectDimension.TWO_D, mesh = true),
    CHECKERBOARD_VERTICAL_MESH(R.string.ui_mesh_checkerboard_vertical, R.string.ui_mesh_checkerboard_vertical_desc, PageEffectDimension.TWO_D, mesh = true),
    CHECKERBOARD_HORIZONTAL_MESH(R.string.ui_mesh_checkerboard_horizontal, R.string.ui_mesh_checkerboard_horizontal_desc, PageEffectDimension.TWO_D, mesh = true),
    CHECKERBOARD_CANVAS(R.string.ui_canvas_checkerboard_diagonal, R.string.ui_canvas_checkerboard_diagonal_desc, PageEffectDimension.TWO_D, oreoAlternative = true),
    CHECKERBOARD_VERTICAL_CANVAS(R.string.ui_canvas_checkerboard_vertical, R.string.ui_canvas_checkerboard_vertical_desc, PageEffectDimension.TWO_D, oreoAlternative = true),
    CHECKERBOARD_HORIZONTAL_CANVAS(R.string.ui_canvas_checkerboard_horizontal, R.string.ui_canvas_checkerboard_horizontal_desc, PageEffectDimension.TWO_D, oreoAlternative = true),
    CHECKERBOARD_OREO(R.string.ui_canvas_dissolve, R.string.ui_canvas_dissolve_desc, PageEffectDimension.TWO_D, oreoAlternative = true),
    TRIANGLE_WIPE(R.string.ui_mesh_angle, R.string.ui_a_triangular_half_diamond_opens_from_the_edge, PageEffectDimension.TWO_D, mesh = true),
    ARC_WIPE(R.string.ui_arc, R.string.ui_a_semi_oval_moves_from_the_edge_to_reveal, PageEffectDimension.TWO_D, mesh = true),
    CIRCLE_WIPE(R.string.ui_circle, R.string.ui_a_circle_expands_from_the_centre_to_reveal_the, PageEffectDimension.TWO_D, mesh = true),
    CIRCLE_REVEAL_CANVAS(R.string.ui_circle, R.string.ui_a_circle_expands_from_the_centre_to_reveal_the, PageEffectDimension.TWO_D, oreoAlternative = true),
    OVAL_WIPE(R.string.ui_oval, R.string.ui_a_vertical_oval_expands_from_the_centre_to_reveal, PageEffectDimension.TWO_D, mesh = true),
    TRIANGLE_REVEAL_MESH(R.string.ui_mesh_triangle, R.string.ui_canvas_triangle_reveal_desc, PageEffectDimension.TWO_D, mesh = true),
    DIAMOND_REVEAL_MESH(R.string.ui_mesh_diamond, R.string.ui_canvas_diamond_reveal_desc, PageEffectDimension.TWO_D, mesh = true),
    OVAL_REVEAL_CANVAS(R.string.ui_oval, R.string.ui_a_vertical_oval_expands_from_the_centre_to_reveal, PageEffectDimension.TWO_D, oreoAlternative = true),
    TRIANGLE_REVEAL_CANVAS(R.string.ui_triangle, R.string.ui_canvas_triangle_reveal_desc, PageEffectDimension.TWO_D, oreoAlternative = true),
    DIAMOND_REVEAL_CANVAS(R.string.ui_canvas_diamond, R.string.ui_canvas_diamond_reveal_desc, PageEffectDimension.TWO_D, oreoAlternative = true),
    CUBE_CANVAS(R.string.ui_cube, R.string.ui_canvas_cube_desc, PageEffectDimension.THREE_D, oreoAlternative = true),
    CIRCLE_WIPE_OREO(R.string.ui_canvas_cross_circle, R.string.ui_canvas_cross_circle_desc, PageEffectDimension.TWO_D, oreoAlternative = true),
    OVAL_WIPE_OREO(R.string.ui_canvas_cross_oval, R.string.ui_canvas_cross_oval_desc, PageEffectDimension.TWO_D, oreoAlternative = true),
    RANDOM_BARS(R.string.ui_mesh_horizontal_strips, R.string.ui_mesh_progressive_horizontal_strips_desc, PageEffectDimension.TWO_D, mesh = true),
    STRIPS_VERTICAL_MESH(R.string.ui_mesh_vertical_strips, R.string.ui_mesh_progressive_vertical_strips_desc, PageEffectDimension.TWO_D, mesh = true),
    RANDOM_BARS_SNAP_HORIZONTAL_MESH(R.string.ui_mesh_random_bars_horizontal, R.string.ui_mesh_random_bars_horizontal_desc, PageEffectDimension.TWO_D, mesh = true),
    RANDOM_BARS_SNAP_VERTICAL_MESH(R.string.ui_mesh_random_bars_vertical, R.string.ui_mesh_random_bars_vertical_desc, PageEffectDimension.TWO_D, mesh = true),
    RANDOM_BARS_OREO(R.string.ui_canvas_random_bars_horizontal, R.string.ui_canvas_random_bars_desc, PageEffectDimension.TWO_D, oreoAlternative = true),
    RANDOM_BARS_VERTICAL_CANVAS(R.string.ui_canvas_random_bars_vertical, R.string.ui_canvas_random_bars_vertical_desc, PageEffectDimension.TWO_D, oreoAlternative = true),
    THREAD_WIPE(R.string.ui_thread, R.string.ui_a_thin_wavy_boundary_draws_in_the_new_page, PageEffectDimension.TWO_D, mesh = true),
    THREAD_WIPE_OREO(R.string.ui_thread, R.string.ui_a_thin_wavy_boundary_draws_in_the_new_page, PageEffectDimension.TWO_D, oreoAlternative = true),
    THREAD_CROSSED_CANVAS(R.string.ui_canvas_cross_threads, R.string.ui_canvas_cross_threads_desc, PageEffectDimension.TWO_D, oreoAlternative = true),
    DIAGONAL_WIPE(R.string.ui_diagonal, R.string.ui_a_diagonal_boundary_reveals_the_next_page, PageEffectDimension.TWO_D, mesh = true),
    DIAGONAL_WIPE_OREO(R.string.ui_diagonal, R.string.ui_a_diagonal_boundary_reveals_the_next_page, PageEffectDimension.TWO_D, oreoAlternative = true),
    TRIANGLE_WIPE_CANVAS(R.string.ui_canvas_angle, R.string.ui_a_triangular_half_diamond_opens_from_the_edge, PageEffectDimension.TWO_D, oreoAlternative = true),
    ARC_WIPE_CANVAS(R.string.ui_arc, R.string.ui_a_semi_oval_moves_from_the_edge_to_reveal, PageEffectDimension.TWO_D, oreoAlternative = true),
    GLITTER(R.string.ui_mesh_dissolution, R.string.ui_small_glowing_fragments_assemble_the_next_page, PageEffectDimension.TWO_D, mesh = true),
    GLITTER_OREO(R.string.ui_glitter, R.string.ui_small_glowing_fragments_assemble_the_next_page, PageEffectDimension.TWO_D, oreoAlternative = true),
    INTERLACED_BLINDS_VERTICAL(R.string.ui_interlaced_blinds_horizontal, R.string.ui_odd_sections_rotate_sequentially_first_followed_by_even_sections, PageEffectDimension.TWO_D, mesh = true),
    INTERLACED_BLINDS_VERTICAL_OREO(R.string.ui_canvas_vertical_strips, R.string.ui_canvas_interlaced_vertical_desc, PageEffectDimension.TWO_D, oreoAlternative = true),
    INTERLACED_BLINDS_HORIZONTAL(R.string.ui_interlaced_blinds_vertical, R.string.ui_odd_rows_rotate_sequentially_first_followed_by_even_rows, PageEffectDimension.TWO_D, mesh = true),
    INTERLACED_BLINDS_HORIZONTAL_OREO(R.string.ui_canvas_horizontal_strips, R.string.ui_canvas_interlaced_horizontal_desc, PageEffectDimension.TWO_D, oreoAlternative = true),

    FLIP(R.string.ui_flip, R.string.ui_rotate_the_page_around_its_vertical_axis, PageEffectDimension.THREE_D),
    PAGE_CURL_SOFT(R.string.ui_page_curl_soft, R.string.ui_a_softer_version_of_the_true_mesh_curl, PageEffectDimension.THREE_D, mesh = true),
    PAGE_CURL_OUTWARD(R.string.ui_page_curl_outward, R.string.ui_page_curl_outward_desc, PageEffectDimension.THREE_D, mesh = true),
    CUBE_OUT_HORIZONTAL(R.string.ui_cube_outside_horizontal, R.string.ui_mesh_faces_rotate_outward_horizontally, PageEffectDimension.THREE_D, mesh = true),
    CUBE_OUT_HORIZONTAL_OREO(R.string.ui_cube_outside_horizontal, R.string.ui_mesh_faces_rotate_outward_horizontally, PageEffectDimension.THREE_D, oreoAlternative = true),
    CUBE_IN_HORIZONTAL(R.string.ui_cube_inside_horizontal, R.string.ui_mesh_faces_rotate_inward_horizontally, PageEffectDimension.THREE_D, mesh = true),
    CUBE_IN_HORIZONTAL_OREO(R.string.ui_cube_inside_horizontal, R.string.ui_mesh_faces_rotate_inward_horizontally, PageEffectDimension.THREE_D, oreoAlternative = true),
    CUBE_OUT_VERTICAL(R.string.ui_cube_outside_vertical, R.string.ui_mesh_faces_rotate_outward_vertically, PageEffectDimension.THREE_D, mesh = true),
    CUBE_OUT_VERTICAL_OREO(R.string.ui_cube_outside_vertical, R.string.ui_mesh_faces_rotate_outward_vertically, PageEffectDimension.THREE_D, oreoAlternative = true),
    CUBE_IN_VERTICAL(R.string.ui_cube_inside_vertical, R.string.ui_mesh_faces_rotate_inward_vertically, PageEffectDimension.THREE_D, mesh = true),
    CUBE_IN_VERTICAL_OREO(R.string.ui_cube_inside_vertical, R.string.ui_mesh_faces_rotate_inward_vertically, PageEffectDimension.THREE_D, oreoAlternative = true),
    SHARED_CUBE(R.string.ui_cube, R.string.ui_desktops_form_a_shared_cube_and_rotate, PageEffectDimension.THREE_D, mesh = true),
    CUBE_FROM_PAGER_INSIDE(R.string.ui_mesh_cube_inside, R.string.ui_mesh_cube_inside_desc, PageEffectDimension.THREE_D, mesh = true),
    CYLINDER(R.string.ui_cylinder, R.string.ui_desktops_move_away_form_a_shared_cylinder_and_move, PageEffectDimension.THREE_D, mesh = true),
    SPHERE(R.string.ui_sphere, R.string.ui_desktops_move_away_form_a_shared_sphere_and_move, PageEffectDimension.THREE_D, mesh = true),
    CARD(R.string.ui_plane_card, R.string.ui_the_card_rotates_and_the_next_page_appears_on, PageEffectDimension.THREE_D, mesh = true),
    CARD_OREO(R.string.ui_pager_compression, R.string.ui_the_card_rotates_and_the_next_page_appears_on, PageEffectDimension.THREE_D, oreoAlternative = true),
    DNA_OUT(R.string.ui_mesh_cascade_twist, R.string.ui_horizontal_sections_twist_outward_from_top_to_bottom_in, PageEffectDimension.THREE_D, mesh = true),
    DNA(R.string.ui_mesh_cascade_twist_reverse, R.string.ui_horizontal_sections_twist_inward_from_top_to_bottom_in, PageEffectDimension.THREE_D, mesh = true),
    VORTEX(R.string.ui_mesh_vortex, R.string.ui_pages_twist_around_the_centre_and_replace_one_another, PageEffectDimension.THREE_D, mesh = true),
    VORTEX_OREO(R.string.ui_vortex, R.string.ui_pages_twist_around_the_centre_and_replace_one_another, PageEffectDimension.THREE_D, oreoAlternative = true);

    val technologyGroup: PageEffectTechnologyGroup
        get() = when {
            mesh -> PageEffectTechnologyGroup.ANDROID_MESH
            this == CUBE_OUT_HORIZONTAL_OREO ||
                this == CUBE_IN_HORIZONTAL_OREO ||
                this == CUBE_OUT_VERTICAL_OREO ||
                this == CUBE_IN_VERTICAL_OREO ||
                this == CARD_OREO ||
                this == VORTEX_OREO -> PageEffectTechnologyGroup.SIMPLE
            oreoAlternative -> PageEffectTechnologyGroup.COMPOSE_CANVAS
            else -> PageEffectTechnologyGroup.SIMPLE
        }

    val pickerTitle: String
        get() = l10n(titleRes)

    val rendererLabel: String
        get() = when {
            this == DEFAULT -> "Pager"
            technologyGroup == PageEffectTechnologyGroup.SIMPLE -> "Compose graphicsLayer"
            technologyGroup == PageEffectTechnologyGroup.COMPOSE_CANVAS -> "Compose Canvas"
            else -> "Android Mesh"
        }

    override val title: String
        get() = "$pickerTitle ($rendererLabel)"
}

internal fun Modifier.pageTransitionTransform(
    effect: PageTransitionEffect,
    signedPageOffset: Float,
    vertical: Boolean = false,
    isOutgoingPage: Boolean = true
): Modifier {
    val offset = signedPageOffset.coerceIn(-1f, 1f)
    val distance = offset.absoluteValue
    return graphicsLayer {
        when (effect) {
            PageTransitionEffect.DEFAULT -> Unit
            PageTransitionEffect.INSTANT -> {
                if (vertical) translationY = offset * size.height else translationX = offset * size.width
                alpha = if (distance < 0.5f) 1f else 0f
            }
            PageTransitionEffect.CROSSFADE -> {
                if (vertical) translationY = offset * size.height else translationX = offset * size.width
                alpha = (1f - distance).coerceIn(0f, 1f)
                scaleX = 1f - distance * 0.05f
                scaleY = scaleX
            }
            PageTransitionEffect.SCALE -> {
                // Counteract Pager's page displacement. Both pages stay
                // centred while the old one shrinks and the new one grows.
                if (vertical) translationY = offset * size.height
                else translationX = offset * size.width

                // Continuous concurrent zoom: outgoing 1 -> 0 and incoming
                // 0 -> 1 across the same full gesture (no halfway pause).
                val zoom = pagerSimultaneousZoom(distance)
                scaleX = zoom
                scaleY = zoom
                // Avoid translucent ghost pages: fully opaque until 0%.
                alpha = if (zoom <= 0f) 0f else 1f
            }
            PageTransitionEffect.ZOOM_OUT -> {
                // Opposite of Zoom in: the incoming page zooms down from
                // 400% to 100% while fading in; the outgoing page stays at
                // 100% and fades out. Both stay centred, without Pager slide.
                if (vertical) translationY = offset * size.height
                else translationX = offset * size.width
                val zoom = pagerZoomOutScale(distance, isOutgoingPage)
                scaleX = zoom
                scaleY = zoom
                alpha = pagerZoomOutAlpha(distance)
            }
            PageTransitionEffect.ROTATE -> {
                rotationZ = -12f * offset
                translationY = distance * size.height * 0.09f
                scaleX = 1f - distance * 0.08f
                scaleY = scaleX
            }
            PageTransitionEffect.FLIP -> {
                rotationY = -92f * offset
                cameraDistance = 34f
                alpha = (1f - distance * 0.82f).coerceAtLeast(0.18f)
            }
            PageTransitionEffect.WAVE -> {
                if (vertical) {
                    // Keep the Pager's natural vertical translation and add
                    // a lateral component. Scaling/rotation exposed slivers
                    // of the neighboring page in portrait mode.
                    translationY = 0f
                    translationX = -offset * size.width * 0.28f
                    rotationZ = 0f
                    scaleX = 1f
                    scaleY = 1f
                    alpha = 1f
                } else {
                    translationY = offset * size.height * 0.24f
                    rotationZ = 7f * offset
                    scaleX = 1f - distance * 0.10f
                    scaleY = scaleX
                    alpha = 1f - distance * 0.25f
                }
            }
            else -> {
                // A failed Mesh page uses this exact, low-risk fallback.
                if (vertical) translationY = offset * size.height else translationX = offset * size.width
                alpha = (1f - distance).coerceIn(0f, 1f)
            }
        }
    }
}

/** Captures the page once composed and uses that snapshot as the Mesh texture. */
@Composable
internal fun MeshPageSurface(
    effect: PageTransitionEffect,
    signedPageOffset: Float,
    verticalPager: Boolean,
    captureKey: Any,
    snapshotCache: MeshPageSnapshotCache? = null,
    meshDeform: Boolean = true,
    isOutgoingPage: Boolean = true,
    liveContentInteractive: Boolean = false,
    transitionInProgress: Boolean = false,
    refreshOutgoingOnSwipe: Boolean = false,
    deferSettledRefresh: Boolean = false,
    captureRequestId: Int? = null,
    outgoingPageHasWidgets: Boolean = false,
    gestureTravelFraction: Float = 1f,
    gestureReleased: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (effect.oreoAlternative) {
        OreoAlternativePageSurface(effect, signedPageOffset, verticalPager, isOutgoingPage, modifier, content)
        return
    }
    if (!effect.mesh) {
        Box(
            modifier
                .fillMaxSize()
                // Keep the arriving page above the outgoing one while zooming.
                // ZOOM_OUT has a 400%-sized, fully transparent incoming page
                // when idle. Never lift that idle page above the active one:
                // invisible graphicsLayer content may still intercept taps on
                // workspace icons, folders and AndroidView widgets.
                // Preserve the already-tested SCALE layering unchanged.
                .then(
                    if (effect == PageTransitionEffect.SCALE ||
                        (effect == PageTransitionEffect.ZOOM_OUT && transitionInProgress))
                        Modifier.zIndex(if (isOutgoingPage) 0f else 5f)
                    else Modifier
                )
                .pageTransitionTransform(effect, signedPageOffset, verticalPager, isOutgoingPage)
        ) { content() }
        return
    }

    val graphicsLayer = rememberGraphicsLayer()
    var pageBitmap by remember(captureKey) { mutableStateOf<Bitmap?>(null) }
    val cachedPageBitmap = snapshotCache?.get(captureKey)
    var freshSourceCaptured by remember(captureKey) { mutableStateOf(false) }
    val freshCapturePending = refreshOutgoingOnSwipe && isOutgoingPage &&
        transitionInProgress && !freshSourceCaptured
    val availablePageBitmap = if (freshCapturePending) null else pageBitmap ?: cachedPageBitmap
    var size by remember(captureKey) { mutableStateOf(IntSize.Zero) }
    var failed by remember(captureKey, effect) { mutableStateOf(false) }
    var captureRevision by remember(captureKey) { mutableStateOf(0) }
    var lastCapturedRevision by remember(captureKey) { mutableStateOf(0) }
    var lastCaptureRequestId by remember(captureKey) { mutableStateOf<Int?>(null) }
    var captureFailures by remember(captureKey) { mutableStateOf(0) }
    val invalidateSnapshot = remember(captureKey) { { captureRevision += 1 } }
    LaunchedEffect(transitionInProgress, captureKey, size, refreshOutgoingOnSwipe,
        isOutgoingPage, outgoingPageHasWidgets) {
        if (transitionInProgress && size.width > 0 && size.height > 0) {
            if (refreshOutgoingOnSwipe && isOutgoingPage) {
                // The resting layer can predate the latest widget frame. Draw
                // the outgoing page and its widget snapshots first, then take
                // the texture before revealing the Mesh transition.
                freshSourceCaptured = false
                pageBitmap = null
                snapshotCache?.remove(captureKey)
                val revisionAtStart = captureRevision
                if (outgoingPageHasWidgets) {
                    // Widgets draw in an AndroidView, outside the Compose
                    // graphics layer. Wait briefly for the newly requested
                    // widget snapshot to be published before capturing it.
                    withTimeoutOrNull(120L) {
                        snapshotFlow { captureRevision }.first { it > revisionAtStart }
                    }
                }
                awaitFrame()
                awaitFrame()
            }
            runCatching { graphicsLayer.toImageBitmap().asAndroidBitmap() }
                .onSuccess { bitmap ->
                    failed = false
                    pageBitmap = bitmap
                    lastCapturedRevision = captureRevision
                    snapshotCache?.put(captureKey, bitmap)
                    if (refreshOutgoingOnSwipe && isOutgoingPage) freshSourceCaptured = true
                }
                .onFailure { if (refreshOutgoingOnSwipe && isOutgoingPage) failed = true }
        } else if (!transitionInProgress) {
            // A settled page is drawn live; the old texture must not survive
            // into a later swipe (including a swipe through a looped page).
            snapshotCache?.remove(captureKey)
            freshSourceCaptured = false
        }
    }
    val rawOffset = signedPageOffset.coerceIn(-1f, 1f)
    val rawDistance = rawOffset.absoluteValue
    val retainTextureRole = effect.usesPairedMeshTextures()
    var retainedOutgoingRole by remember(captureKey) { mutableStateOf(isOutgoingPage) }
    LaunchedEffect(isOutgoingPage, retainTextureRole, transitionInProgress) {
        // Pager can replace settledPage just before live content returns. Retaining
        // roles during motion prevents reversal. At rest every precomposed page
        // explicitly receives its role for the next transition.
        if (retainTextureRole && !transitionInProgress) retainedOutgoingRole = isOutgoingPage
    }
    val textureOutgoingPage = if (retainTextureRole && transitionInProgress) {
        retainedOutgoingRole
    } else {
        isOutgoingPage
    }
    val rawProgress = if (textureOutgoingPage) rawDistance else 1f - rawDistance
    val travelFraction = gestureTravelFraction.coerceIn(0.48f, 1f)
    val acceleratedProgress = (rawProgress / travelFraction).coerceIn(0f, 1f)
    // The four face-rotation effects used to reach progress=1 while Pager was
    // still settling, leaving the almost-hidden old page frozen at the end.
    // During the drag reserve the final 6%; after release, Pager's remaining
    // distance drives that last part continuously up to the settled frame.
    val progress = if (effect.isMeshCube() && travelFraction < 0.999f) {
        if (!gestureReleased) {
            acceleratedProgress.coerceAtMost(0.94f)
        } else if (rawProgress <= travelFraction) {
            acceleratedProgress * 0.94f
        } else {
            0.94f + 0.06f *
                ((rawProgress - travelFraction) / (1f - travelFraction).coerceAtLeast(0.001f))
                    .coerceIn(0f, 1f)
        }
    } else {
        acceleratedProgress
    }
    val visualProgress = progress
    val direction = when {
        rawOffset < 0f -> -1f
        rawOffset > 0f -> 1f
        else -> 1f
    }
    val meshOffset = direction * if (textureOutgoingPage) progress else 1f - progress
    val pairedTextures = effect.usesPairedMeshTextures()
    // Pager precomposes a page at each side. Paired effects must render only
    // the settled source and the neighbour that is actually entering.
    val transitionParticipant = textureOutgoingPage || rawDistance < 0.995f
    // The live page is handed back only after the pager reports that scrolling
    // has finished. A role change at the final offset can happen one frame earlier.
    val showLiveContent = liveContentInteractive
    val pageAlpha = when {
        (effect == PageTransitionEffect.PAGE_CURL_SOFT || effect == PageTransitionEffect.PAGE_CURL_OUTWARD) &&
            textureOutgoingPage -> if (progress < 0.995f) 1f else 0f
        effect == PageTransitionEffect.PAGE_CURL_SOFT || effect == PageTransitionEffect.PAGE_CURL_OUTWARD -> 1f
        effect == PageTransitionEffect.CARD && textureOutgoingPage -> ((0.54f - progress) / 0.08f).coerceIn(0f, 1f)
        effect == PageTransitionEffect.CARD -> ((progress - 0.46f) / 0.08f).coerceIn(0f, 1f)
        pairedTextures || textureOutgoingPage -> 1f
        else -> ((progress - 0.5f) / 0.12f).coerceIn(0f, 1f)
    }

    val captureUncachedPage = availablePageBitmap == null
    val explicitCaptureRequested = captureRequestId != null &&
        captureRequestId != lastCaptureRequestId
    val captureAllowed = captureUncachedPage || explicitCaptureRequested ||
        (!deferSettledRefresh && captureRevision != lastCapturedRevision && !transitionInProgress)
    LaunchedEffect(
        captureKey,
        size,
        captureRevision,
        captureAllowed,
        captureFailures,
        transitionInProgress,
        deferSettledRefresh,
        captureRequestId
    ) {
        if (failed) return@LaunchedEffect
        if (size.width <= 0 || size.height <= 0) return@LaunchedEffect
        if (!captureAllowed || (refreshOutgoingOnSwipe && isOutgoingPage && transitionInProgress))
            return@LaunchedEffect
        // Widget-backed AndroidViews publish a Compose-drawable frame first.
        // A cached neighbour is never replaced at the release boundary: swapping a
        // widget-backed texture there caused a one-frame page-curl flash.
        if (captureRevision == 0) awaitFrame() else delay(50)
        if (transitionInProgress && pageBitmap != null) return@LaunchedEffect
        runCatching { graphicsLayer.toImageBitmap().asAndroidBitmap() }
            .onSuccess {
                pageBitmap = it
                lastCapturedRevision = captureRevision
                if (captureRequestId != null) lastCaptureRequestId = captureRequestId
                if (transitionInProgress) snapshotCache?.put(captureKey, it)
                captureFailures = 0
            }
            .onFailure {
                captureFailures += 1
                if (captureFailures >= 2) failed = true
            }
    }

    val awaitingCapture = availablePageBitmap == null
    val fallback = failed && awaitingCapture
    val showUncapturedOutgoingPage = awaitingCapture && textureOutgoingPage
    Box(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (transitionInProgress &&
                    (effect == PageTransitionEffect.PAGE_CURL_SOFT || effect == PageTransitionEffect.PAGE_CURL_OUTWARD ||
                     effect == PageTransitionEffect.TRIANGLE_REVEAL_MESH || effect == PageTransitionEffect.DIAMOND_REVEAL_MESH)) {
                    // Curl lies over its revealed background; shape reveals lie over the old page.
                    val incomingOnTop = effect == PageTransitionEffect.TRIANGLE_REVEAL_MESH ||
                        effect == PageTransitionEffect.DIAMOND_REVEAL_MESH
                    Modifier.zIndex(if (textureOutgoingPage == incomingOnTop) 0f else 1f)
                } else Modifier
            )
            .onSizeChanged { size = it }
            .then(
                if (fallback) Modifier.pageTransitionTransform(PageTransitionEffect.CROSSFADE, rawOffset, verticalPager)
                else if (!meshDeform) Modifier.graphicsLayer {
                    if (verticalPager) translationY = rawOffset * this.size.height
                    else translationX = rawOffset * this.size.width
                    alpha = when {
                        showLiveContent -> 1f
                        !transitionParticipant -> 0f
                        rawDistance >= 0.999f && !pairedTextures -> 0f
                        else -> pageAlpha
                    }
                }
                else Modifier.graphicsLayer {
                    // Pager lays each page out at the opposite signed offset. This
                    // translation cancels that layout motion for Mesh textures, just
                    // as it does for Arc, so source and destination stay screen-fixed.
                    if (verticalPager) translationY = rawOffset * this.size.height
                    else translationX = rawOffset * this.size.width
                    alpha = when {
                        showLiveContent -> 1f
                        !transitionParticipant -> 0f
                        rawDistance >= 0.999f && !pairedTextures -> 0f
                        else -> pageAlpha
                    }
                }
            )
    ) {
        if (!fallback && !awaitingCapture && !showLiveContent && transitionParticipant) {
            val bitmap = requireNotNull(availablePageBitmap)
            AndroidView(
                factory = { MeshRenderView(it) },
                update = { view ->
                    view.update(
                        bitmap,
                        if (meshDeform) effect else PageTransitionEffect.CROSSFADE,
                        if (meshDeform) meshOffset else 0f,
                        visualProgress,
                        textureOutgoingPage,
                        verticalPager
                    ) { failed = true }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        CompositionLocalProvider(
            LocalMeshLiveContentVisible provides (fallback || showUncapturedOutgoingPage || showLiveContent),
            LocalMeshCaptureWidgetSnapshot provides (freshCapturePending || explicitCaptureRequested),
            LocalMeshSnapshotInvalidator provides invalidateSnapshot
        ) {
            Box(
                Modifier.fillMaxSize().drawWithContent {
                    graphicsLayer.record { this@drawWithContent.drawContent() }
                    if (fallback || showUncapturedOutgoingPage || showLiveContent) drawLayer(graphicsLayer)
                }
            ) { content() }
        }

        if (!fallback && !awaitingCapture && !showLiveContent && transitionParticipant && textureOutgoingPage &&
            effect == PageTransitionEffect.FIRE_REVEAL && visualProgress in 0.015f..0.985f
        ) {
            FireTransitionBand(
                progress = visualProgress, direction = direction,
                vertical = verticalPager, modifier = Modifier.fillMaxSize()
            )
        }
    }
}

private class MeshRenderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    private val diagnosticViewId = nextViewId.getAndIncrement()
    private var bitmap: Bitmap? = null
    private var effect: PageTransitionEffect = PageTransitionEffect.CROSSFADE
    private var offset: Float = 0f
    private var transitionProgress: Float = 0f
    private var outgoingPage: Boolean = true
    private var verticalPager: Boolean = false
    private var failure: (() -> Unit)? = null
    private var failurePosted = false
    private var currentDisplayMesh: DisplayMesh? = null
    private var sheetSourceBitmap: Bitmap? = null
    private var sheetTextureBitmap: Bitmap? = null

    /**
     * Add a visible translucent sheet *under* the complete captured page.
     * The captured texture already contains shortcuts, folders, notes and the
     * widget snapshots. Combining them once per capture means all of those
     * items follow exactly the existing Mesh curl without changing its math.
     *
     * Only the outgoing curl asks for this texture. Incoming pages retain
     * their original bitmap, and the sheet is never drawn when content is live
     * (the Mesh view is removed outside a page transition).
     */
    private fun pageCurlSheetTexture(source: Bitmap): Bitmap {
        if (sheetSourceBitmap === source) return sheetTextureBitmap ?: source
        sheetSourceBitmap = source
        val sheet = runCatching {
            val result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
            val target = Canvas(result)
            // Frosted page, visible even on an otherwise empty desktop.
            // The low opacity also lets the wallpaper/next page show through.
            target.drawColor(Color.argb(50, 180, 180, 180))
            // Preserve the whole snapshot as one textured surface: icons,
            // folders and (already captured) AndroidView widget contents.
            // Make the snapshot slightly translucent, not just its background.
            val drawable = if (source.config == Bitmap.Config.HARDWARE) {
                source.copy(Bitmap.Config.ARGB_8888, false) ?: source
            } else source
            target.drawBitmap(drawable, 0f, 0f, Paint(Paint.FILTER_BITMAP_FLAG).apply {
                alpha = 222
            })
            if (drawable !== source) drawable.recycle()
            val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(112, 255, 255, 255)
                style = Paint.Style.STROKE
                strokeWidth = 2f
            }
            target.drawRect(1f, 1f, source.width - 1f, source.height - 1f, border)
            result
        }.getOrNull()
        sheetTextureBitmap = sheet
        return sheet ?: source
    }

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
        isClickable = false
        isFocusable = false
    }

    fun update(
        bitmap: Bitmap,
        effect: PageTransitionEffect,
        offset: Float,
        transitionProgress: Float,
        outgoingPage: Boolean,
        verticalPager: Boolean,
        failure: () -> Unit
    ) {
        this.bitmap = bitmap
        this.effect = effect
        this.offset = offset
        this.transitionProgress = transitionProgress
        this.outgoingPage = outgoingPage
        this.verticalPager = verticalPager
        this.failure = failure
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val texture = bitmap ?: return
        try {
            // The page-curl geometry and shading stay unchanged. Only the
            // outgoing page's sampled texture gains a translucent carrier.
            val curlSheet = outgoingPage &&
                (effect == PageTransitionEffect.PAGE_CURL_SOFT ||
                    effect == PageTransitionEffect.PAGE_CURL_OUTWARD)
            val renderedTexture = if (curlSheet) pageCurlSheetTexture(texture) else texture
            val mesh = MeshGeometry.create(
                effect = effect,
                offset = offset,
                transitionProgress = transitionProgress,
                outgoingPage = outgoingPage,
                width = width.toFloat(),
                height = height.toFloat(),
                verticalPager = verticalPager,
                bitmap = renderedTexture
            ) ?: return
            val displayMesh = DisplayMesh(
                id = nextMeshId.getAndIncrement(),
                effect = effect,
                progress = transitionProgress,
                drawable = mesh
            )
            canvas.drawMesh(mesh.mesh, BlendMode.MODULATE, mesh.paint)
            val previous = currentDisplayMesh
            currentDisplayMesh = displayMesh
            if (previous != null) {
                MeshObjectRetainer.retainReplaced(diagnosticViewId, previous)
            }
        } catch (_: Throwable) {
            if (!failurePosted) {
                failurePosted = true
                post { failure?.invoke() }
            }
        }
    }

    override fun onDetachedFromWindow() {
        currentDisplayMesh?.let {
            MeshObjectRetainer.retainDetached(diagnosticViewId, it)
            currentDisplayMesh = null
        }
        // MeshObjectRetainer owns any in-flight textures until RenderThread
        // finishes with them. The view no longer needs its own sheet cache.
        sheetSourceBitmap = null
        sheetTextureBitmap = null
        super.onDetachedFromWindow()
    }

    companion object {
        private val nextViewId = AtomicLong(1L)
        private val nextMeshId = AtomicLong(1L)
    }
}

private data class DrawableMesh(val mesh: Mesh, val paint: Paint)

private data class DisplayMesh(
    val id: Long,
    val effect: PageTransitionEffect,
    val progress: Float,
    val drawable: DrawableMesh
)

/**
 * A MeshRenderView strongly owns the Mesh in its current display list. This
 * process-wide tail keeps replaced frames briefly and the final frame of a
 * detached AndroidView longer, while RenderThread finishes queued/cached lists.
 */
private object MeshObjectRetainer {
    private const val REPLACED_HOLD_MS = 1_000L
    private const val DETACHED_HOLD_MS = 10_000L
    private const val LOG_INTERVAL_MS = 1_000L

    private data class RetainedMesh(
        val displayMesh: DisplayMesh,
        val viewId: Long,
        val reason: String,
        val releaseAtUptimeMs: Long
    )

    private val mainHandler = Handler(Looper.getMainLooper())
    private val retained = java.util.ArrayDeque<RetainedMesh>()
    private var cleanupScheduled = false
    private var lastLoggedEffect: PageTransitionEffect? = null
    private var lastLogUptimeMs = Long.MIN_VALUE

    private val cleanupRunnable = Runnable {
        synchronized(this) {
            cleanupScheduled = false
            pruneAndSchedule(SystemClock.uptimeMillis())
        }
    }

    fun retainReplaced(viewId: Long, displayMesh: DisplayMesh) {
        retain(viewId, displayMesh, "replaced", REPLACED_HOLD_MS)
    }

    fun retainDetached(viewId: Long, displayMesh: DisplayMesh) {
        retain(viewId, displayMesh, "detached", DETACHED_HOLD_MS)
    }

    private fun retain(viewId: Long, displayMesh: DisplayMesh, reason: String, holdMs: Long) {
        val now = SystemClock.uptimeMillis()
        synchronized(this) {
            retained.addLast(RetainedMesh(displayMesh, viewId, reason, now + holdMs))
            pruneAndSchedule(now)

            if (reason == "detached" || displayMesh.effect != lastLoggedEffect ||
                now - lastLogUptimeMs >= LOG_INTERVAL_MS
            ) {
                LauncherDiagnostics.info(
                    "mesh retained effect=${displayMesh.effect.name} view=$viewId " +
                        "mesh=${displayMesh.id} progress=${"%.3f".format(displayMesh.progress)} " +
                        "reason=$reason queue=${retained.size} holdMs=$holdMs"
                )
                lastLoggedEffect = displayMesh.effect
                lastLogUptimeMs = now
            }
        }
    }

    private fun pruneAndSchedule(now: Long) {
        while (retained.peekFirst()?.releaseAtUptimeMs?.let { it <= now } == true) {
            retained.removeFirst()
        }
        val nextRetained = retained.peekFirst()
        if (!cleanupScheduled && nextRetained != null) {
            cleanupScheduled = true
            mainHandler.postDelayed(
                cleanupRunnable,
                (nextRetained.releaseAtUptimeMs - now).coerceAtLeast(1L)
            )
        }
    }
}

private object MeshGeometry {
    private const val STRIDE = 24
    private const val X_SEGMENTS = 64
    private const val Y_SEGMENTS = 72

    private val specification: MeshSpecification by lazy {
        MeshSpecification.make(
            arrayOf(
                MeshSpecification.Attribute(MeshSpecification.TYPE_FLOAT2, 0, "position"),
                MeshSpecification.Attribute(MeshSpecification.TYPE_FLOAT2, 8, "texCoord"),
                MeshSpecification.Attribute(MeshSpecification.TYPE_FLOAT, 16, "shade"),
                MeshSpecification.Attribute(MeshSpecification.TYPE_FLOAT, 20, "opacity")
            ),
            STRIDE,
            arrayOf(
                MeshSpecification.Varying(MeshSpecification.TYPE_FLOAT2, "texCoord"),
                MeshSpecification.Varying(MeshSpecification.TYPE_FLOAT, "shade"),
                MeshSpecification.Varying(MeshSpecification.TYPE_FLOAT, "opacity")
            ),
            """
                Varyings main(const Attributes attributes) {
                    Varyings varyings;
                    varyings.position = attributes.position;
                    varyings.texCoord = attributes.texCoord;
                    varyings.shade = attributes.shade;
                    varyings.opacity = attributes.opacity;
                    return varyings;
                }
            """.trimIndent(),
            """
                float2 main(const Varyings varyings, out float4 color) {
                    float light = clamp(varyings.shade, 0.08, 1.35);
                    float alpha = step(0.5, clamp(varyings.opacity, 0.0, 1.0));
                    color = float4(light, light, light, alpha);
                    return varyings.texCoord;
                }
            """.trimIndent()
        )
    }

    fun create(
        effect: PageTransitionEffect,
        offset: Float,
        transitionProgress: Float,
        outgoingPage: Boolean,
        width: Float,
        height: Float,
        verticalPager: Boolean,
        bitmap: Bitmap
    ): DrawableMesh? {
        require(width > 0f && height > 0f)
        if (effect == PageTransitionEffect.RANDOM_BARS_SNAP_HORIZONTAL_MESH ||
            effect == PageTransitionEffect.RANDOM_BARS_SNAP_VERTICAL_MESH
        ) {
            // Angular seams are used only for RANDOM strips. Keep the ordinary
            // horizontal/vertical strips on their original 0.45.2 flat Mesh
            // path via MeshTransitionGeometry.deform() below.
            return createAngularStripsMesh(
                effect, transitionProgress, if (offset < 0f) -1f else 1f,
                outgoingPage, width, height, bitmap
            )
        }
        if (effect == PageTransitionEffect.CHECKERBOARD_VERTICAL_MESH ||
            effect == PageTransitionEffect.CHECKERBOARD_HORIZONTAL_MESH ||
            effect == PageTransitionEffect.TRIANGLE_REVEAL_MESH ||
            effect == PageTransitionEffect.DIAMOND_REVEAL_MESH
        ) {
            // True Mesh surface with angular, mutually complementary triangular
            // facets. Unlike the old flat rectangles/polygons, the irregular
            // triangulation is visible in the growing/replaced page boundary.
            return createAngularRevealMesh(effect, transitionProgress, outgoingPage, width, height, bitmap)
        }
        if (!outgoingPage &&
            (effect == PageTransitionEffect.PAGE_CURL_SOFT || effect == PageTransitionEffect.PAGE_CURL_OUTWARD)
        ) {
            // Draw the incoming page only in the area uncovered by the old curl.
            // Both textures receive the same normalized transitionProgress.
            val sharedDirection = if (offset < 0f) 1f else -1f
            return createCurlIncomingMesh(
                transitionProgress, sharedDirection, width, height, bitmap,
                softness = 1.0f,
                outward = effect == PageTransitionEffect.PAGE_CURL_OUTWARD,
                verticalPager = verticalPager
            )
        }
        if (effect == PageTransitionEffect.RETRO_FLY_TILES ||
            effect == PageTransitionEffect.INTERLACED_BLINDS_VERTICAL ||
            effect == PageTransitionEffect.INTERLACED_BLINDS_HORIZONTAL
        ) {
            return createIndependentTileMesh(
                effect, offset, transitionProgress, outgoingPage,
                width, height, verticalPager, bitmap
            )
        }
        val xSegments = effect.meshXSegments()
        val ySegments = effect.meshYSegments()
        val vertexCount = (xSegments + 1) * (ySegments + 1)
        val vertices = ByteBuffer.allocateDirect(vertexCount * STRIDE).order(ByteOrder.nativeOrder())
        val opacities = FloatArray(vertexCount)
        val progress = offset.absoluteValue.coerceIn(0f, 1f)
        val direction = if (offset < 0f) -1f else 1f
        var vertexIndex = 0

        for (row in 0..ySegments) {
            val v = row / ySegments.toFloat()
            for (column in 0..xSegments) {
                val u = column / xSegments.toFloat()
                val vertex = deform(
                    effect, u, v, progress, transitionProgress, outgoingPage,
                    direction, width, height, verticalPager
                )
                vertices.putFloat(vertex.x)
                vertices.putFloat(vertex.y)
                vertices.putFloat(vertex.u * bitmap.width)
                vertices.putFloat(vertex.v * bitmap.height)
                vertices.putFloat(vertex.shade)
                vertices.putFloat(vertex.opacity)
                opacities[vertexIndex++] = vertex.opacity
            }
        }
        vertices.flip()

        val indexCount = xSegments * ySegments * 6
        val indices = ByteBuffer.allocateDirect(indexCount * 2).order(ByteOrder.nativeOrder()).asShortBuffer()
        val rowWidth = xSegments + 1
        for (row in 0 until ySegments) {
            for (indexColumn in 0 until xSegments) {
                // When folding outward the flap must be rendered last over the
                // sheet regardless of swipe direction. The original right-to-
                // left sweep hid the flap behind the flat page.
                val column = if (effect == PageTransitionEffect.PAGE_CURL_OUTWARD &&
                    outgoingPage && direction < 0f) xSegments - 1 - indexColumn
                    else indexColumn
                val a = row * rowWidth + column
                val b = a + 1
                val c = a + rowWidth
                val d = c + 1
                putVisibleTriangle(indices, opacities, a, c, b)
                putVisibleTriangle(indices, opacities, b, c, d)
            }
        }
        if (indices.position() == 0) return null
        indices.flip()

        val mesh = Mesh(
            specification,
            Mesh.TRIANGLES,
            vertices,
            vertexCount,
            indices,
            RectF(-width, -height, width * 2f, height * 2f)
        )
        val shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP).apply {
            setLocalMatrix(Matrix())
        }
        val paint = Paint().apply {
            this.shader = shader
            isFilterBitmap = true
            alpha = 255
        }
        return DrawableMesh(mesh, paint)
    }

    // The Canvas random-strips permutation is reproduced exactly: no movement and
    // no alpha tween, only whole-line replacement in a stable shuffled order.
    private val snapBarRanks: IntArray by lazy {
        val count = 96
        val order = IntArray(count) { it }
        var state = 0x4B19F32
        for (i in order.lastIndex downTo 1) {
            state = state * 1664525 + 1013904223
            val j = (state ushr 1) % (i + 1)
            val old = order[i]
            order[i] = order[j]
            order[j] = old
        }
        IntArray(count).also { ranks ->
            for (rank in order.indices) ranks[order[rank]] = rank
        }
    }

    /**
     * Mesh strips are thin bands with a subtle, repeatable angular silhouette.
     * Each strip boundary is one continuous triangular-wave polyline, so the
     * outgoing/incoming textures share exactly the same vertices and triangles.
     * Vertex UVs follow screen positions: an intact page is never distorted.
     * The random pair snaps entire bands at once; the progressive pair reveals
     * a band along its length, retaining its per-band delay and direction.
     */
    private fun createAngularStripsMesh(
        effect: PageTransitionEffect, progress: Float, direction: Float,
        outgoingPage: Boolean, width: Float, height: Float, bitmap: Bitmap
    ): DrawableMesh? {
        val randomHorizontal = effect == PageTransitionEffect.RANDOM_BARS_SNAP_HORIZONTAL_MESH
        val randomVertical = effect == PageTransitionEffect.RANDOM_BARS_SNAP_VERTICAL_MESH
        val horizontal = randomHorizontal || effect == PageTransitionEffect.RANDOM_BARS
        val count = when {
            randomHorizontal || randomVertical -> 96
            horizontal -> 30
            else -> 20
        }
        // Preserve 64x72 sampling along the length of the strips. Only their
        // transverse edges become angular; the page itself does not warp.
        val xs = if (horizontal) X_SEGMENTS else count
        val ys = if (horizontal) count else Y_SEGMENTS
        val stepX = width / xs
        val stepY = height / ys
        val vertexCount = (xs + 1) * (ys + 1)
        val vertices = ByteBuffer.allocateDirect(vertexCount * STRIDE).order(ByteOrder.nativeOrder())
        val indices = ByteBuffer.allocateDirect(xs * ys * 12)
            .order(ByteOrder.nativeOrder()).asShortBuffer()
        val stripeThickness = if (horizontal) stepY else stepX
        // 22% of band thickness is enough to give each edge two or three
        // clearly angled sections without producing jagged, noisy tearing.
        val amplitude = stripeThickness * 0.22f
        fun angularWave(position: Int, boundary: Int): Float {
            // Piecewise linear triangular wave: alternate rising/falling facets.
            val phase = (position + boundary * 5) % 16
            return 1f - kotlin.math.abs(phase - 8) / 4f
        }
        for (row in 0..ys) for (col in 0..xs) {
            val x = if (!horizontal && col in 1 until xs)
                col * stepX + amplitude * angularWave(row, col) else col * stepX
            val y = if (horizontal && row in 1 until ys)
                row * stepY + amplitude * angularWave(col, row) else row * stepY
            putFlatVertex(vertices, x, y, width, height, bitmap)
        }
        vertices.flip()
        val p = progress.coerceIn(0f, 1f)
        val visibleCount = (p * 96f).toInt().coerceIn(0, 96)
        fun revealed(col: Int, row: Int, lowerLeft: Boolean): Boolean {
            if (p <= 0f) return false
            if (p >= 1f) return true
            if (randomHorizontal) return snapBarRanks[row] < visibleCount
            if (randomVertical) return snapBarRanks[col] < visibleCount
            val u = (col + if (lowerLeft) 1f / 3f else 2f / 3f) / xs
            val v = (row + if (lowerLeft) 2f / 3f else 1f / 3f) / ys
            // The original progressive-strip timing and edge direction are
            // reused, sampled at each triangle's centroid.
            return MeshTransitionGeometry.deform(
                effect, u, v, p, direction, width, height,
                outgoingPage = false, verticalPager = false
            )!!.opacity > 0.5f
        }
        val rowWidth = xs + 1
        for (row in 0 until ys) for (col in 0 until xs) {
            val a = row * rowWidth + col
            val b = a + 1
            val c = a + rowWidth
            val d = c + 1
            if (revealed(col, row, true) != outgoingPage)
                indices.put(a.toShort()).put(c.toShort()).put(b.toShort())
            if (revealed(col, row, false) != outgoingPage)
                indices.put(b.toShort()).put(c.toShort()).put(d.toShort())
        }
        if (indices.position() == 0) return null
        indices.flip()
        return texturedMesh(vertices, vertexCount, indices, width, height, bitmap)
    }

    /**
     * Mesh-native reveals on the SAME regular triangle lattice used by the
     * older Mesh effects (Circle, Arc, Thread, Checkerboard diagonal, etc.).
     * Keep the projected vertices at their original positions: displacing them
     * creates irregular torn edges unrelated to the effect and was the cause
     * of the unpleasant geometry in 0.45.1.
     *
     * Each triangle is assigned to exactly one of the complementary textures.
     * The two pages use the same indices and UV map, so neither overlaps nor
     * leaves transparent seams at a fragment boundary.
     */
    private fun createAngularRevealMesh(
        effect: PageTransitionEffect, progress: Float, outgoingPage: Boolean,
        width: Float, height: Float, bitmap: Bitmap
    ): DrawableMesh? {
        val randomHorizontal = effect == PageTransitionEffect.RANDOM_BARS_SNAP_HORIZONTAL_MESH
        val randomVertical = effect == PageTransitionEffect.RANDOM_BARS_SNAP_VERTICAL_MESH
        val checkerVertical = effect == PageTransitionEffect.CHECKERBOARD_VERTICAL_MESH
        val checkerHorizontal = effect == PageTransitionEffect.CHECKERBOARD_HORIZONTAL_MESH
        val checker = checkerVertical || checkerHorizontal
        val triangle = effect == PageTransitionEffect.TRIANGLE_REVEAL_MESH
        val diamond = effect == PageTransitionEffect.DIAMOND_REVEAL_MESH
        // Squares are subdivided into triangles with a shared diagonal; there
        // are no random displacements and no separate Canvas clip rectangles.
        val squareSize = minOf(width, height) / 8f
        val tileColumns = if (checker) kotlin.math.ceil(width / squareSize).toInt().coerceAtLeast(1) else 0
        val tileRows = if (checker) kotlin.math.ceil(height / squareSize).toInt().coerceAtLeast(1) else 0
        val subdivisions = 6
        val xs = when {
            randomVertical -> 96
            randomHorizontal -> X_SEGMENTS
            checker -> tileColumns * subdivisions
            else -> X_SEGMENTS
        }
        val ys = when {
            randomHorizontal -> 96
            randomVertical -> Y_SEGMENTS
            checker -> tileRows * subdivisions
            else -> Y_SEGMENTS
        }
        val vertexCount = (xs + 1) * (ys + 1)
        val vertices = ByteBuffer.allocateDirect(vertexCount * STRIDE).order(ByteOrder.nativeOrder())
        val stepX = width / xs
        val stepY = height / ys
        for (r in 0..ys) for (c in 0..xs) {
            putFlatVertex(vertices, c * stepX, r * stepY, width, height, bitmap)
        }
        vertices.flip()
        val indices = ByteBuffer.allocateDirect(xs * ys * 12)
            .order(ByteOrder.nativeOrder()).asShortBuffer()
        val p = progress.coerceIn(0f, 1f)
        val eased = p * p * (3f - 2f * p)
        val cx = width / 2f
        val cy = height / 2f
        val triangleRadius = maxOf(height, height * 0.5f + width * sqrt(3f) * 0.5f) * 1.015f
        val diamondRadius = (width + height) * 0.5f * 1.015f
        // A triangle pointing upward, centred on the page; it grows until it
        // encloses the entire viewport, regardless of screen orientation.
        val apexX = cx
        val apexY = cy - triangleRadius * eased
        val rightX = cx + triangleRadius * sqrt(3f) * 0.5f * eased
        val bottomY = cy + triangleRadius * 0.5f * eased
        val leftX = cx - triangleRadius * sqrt(3f) * 0.5f * eased

        fun revealed(col: Int, row: Int, isLowerLeft: Boolean): Boolean {
            if (p <= 0f) return false
            if (p >= 1f) return true
            // The two triangles of a cell are sampled at their actual centres.
            val du = if (isLowerLeft) 1f / 3f else 2f / 3f
            val dv = if (isLowerLeft) 2f / 3f else 1f / 3f
            val x = (col + du) * stepX
            val y = (row + dv) * stepY
            return when {
                randomHorizontal -> snapBarRanks[row] < (p * 96f).toInt().coerceIn(0, 96)
                randomVertical -> snapBarRanks[col] < (p * 96f).toInt().coerceIn(0, 96)
                checker -> {
                    val blockCol = col / subdivisions
                    val blockRow = row / subdivisions
                    val phase = if (((blockCol + blockRow) and 1) == 0) 0f else 0.5f
                    val local = ((p - phase) * 2f).coerceIn(0f, 1f)
                    val portion = if (checkerVertical) (row % subdivisions + dv) / subdivisions
                        else (col % subdivisions + du) / subdivisions
                    local >= portion
                }
                triangle -> {
                    fun cross(x1: Float, y1: Float, x2: Float, y2: Float) = x1 * y2 - y1 * x2
                    cross(rightX - apexX, bottomY - apexY, x - apexX, y - apexY) >= 0f &&
                        cross(leftX - rightX, bottomY - bottomY, x - rightX, y - bottomY) >= 0f &&
                        cross(apexX - leftX, apexY - bottomY, x - leftX, y - bottomY) >= 0f
                }
                diamond -> kotlin.math.abs(x - cx) + kotlin.math.abs(y - cy) <= diamondRadius * eased
                else -> false
            }
        }
        val gridWidth = xs + 1
        for (row in 0 until ys) for (col in 0 until xs) {
            val a = row * gridWidth + col
            val b = a + 1
            val c = a + gridWidth
            val d = c + 1
            if (revealed(col, row, true) != outgoingPage)
                indices.put(a.toShort()).put(c.toShort()).put(b.toShort())
            if (revealed(col, row, false) != outgoingPage)
                indices.put(b.toShort()).put(c.toShort()).put(d.toShort())
        }
        if (indices.position() == 0) return null
        indices.flip()
        return texturedMesh(vertices, vertexCount, indices, width, height, bitmap)
    }

    private fun createCurlIncomingMesh(
        progress: Float, direction: Float, width: Float, height: Float,
        bitmap: Bitmap, softness: Float, outward: Boolean,
        verticalPager: Boolean
    ): DrawableMesh? {
        val p = progress.coerceIn(0f, 1f)
        if (p <= 0.002f) return null
        if (p >= 0.995f) return createRectangleMesh(listOf(RectF(0f, 0f, width, height)), width, height, bitmap)
        // The scroll's flat/UV foldStart is NOT its visible screen edge. The
        // rolled front projects beyond that edge until the page physically
        // uncurls. Find the utmost projected point of the outgoing sheet and
        // reveal the next page only beyond that visible occluding silhouette.
        val axisExtent = if (verticalPager) height else width
        var front = if (direction > 0f) 0f else axisExtent
        val samples = 96
        for (i in 0..samples) {
            val u = i.toFloat() / samples
            val visibleAxis = if (verticalPager) {
                pageCurl(u, 0.5f, p, direction, height, width, softness, outward).x
            } else {
                pageCurl(u, 0.5f, p, direction, width, height, softness, outward).x
            }
            front = if (direction > 0f) maxOf(front, visibleAxis) else minOf(front, visibleAxis)
        }
        val edge = front.coerceIn(0f, axisExtent)
        val rect = if (verticalPager) {
            if (direction > 0f) RectF(0f, edge, width, height)
            else RectF(0f, 0f, width, edge)
        } else {
            if (direction > 0f) RectF(edge, 0f, width, height)
            else RectF(0f, 0f, edge, height)
        }
        return createRectangleMesh(listOf(rect), width, height, bitmap)
    }

    private fun createRectangleMesh(
        rectangles: List<RectF>, width: Float, height: Float, bitmap: Bitmap
    ): DrawableMesh? {
        val valid = rectangles.filter { it.right - it.left > 0.0001f && it.bottom - it.top > 0.0001f }
        if (valid.isEmpty()) return null
        val vertices = ByteBuffer.allocateDirect(valid.size * 4 * STRIDE).order(ByteOrder.nativeOrder())
        val indices = ByteBuffer.allocateDirect(valid.size * 6 * 2).order(ByteOrder.nativeOrder()).asShortBuffer()
        var base = 0
        for (r in valid) {
            putFlatVertex(vertices, r.left, r.top, width, height, bitmap)
            putFlatVertex(vertices, r.right, r.top, width, height, bitmap)
            putFlatVertex(vertices, r.left, r.bottom, width, height, bitmap)
            putFlatVertex(vertices, r.right, r.bottom, width, height, bitmap)
            indices.put(base.toShort()).put((base + 2).toShort()).put((base + 1).toShort())
            indices.put((base + 1).toShort()).put((base + 2).toShort()).put((base + 3).toShort())
            base += 4
        }
        vertices.flip()
        indices.flip()
        return texturedMesh(vertices, base, indices, width, height, bitmap)
    }

    private fun putFlatVertex(
        vertices: ByteBuffer, x: Float, y: Float, width: Float, height: Float, bitmap: Bitmap
    ) {
        vertices.putFloat(x)
        vertices.putFloat(y)
        vertices.putFloat(x / width * bitmap.width)
        vertices.putFloat(y / height * bitmap.height)
        vertices.putFloat(1f)
        vertices.putFloat(1f)
    }

    private fun texturedMesh(
        vertices: ByteBuffer, vertexCount: Int, indices: java.nio.ShortBuffer,
        width: Float, height: Float, bitmap: Bitmap
    ): DrawableMesh {
        val mesh = Mesh(
            specification, Mesh.TRIANGLES, vertices, vertexCount, indices,
            RectF(-width, -height, width * 2f, height * 2f)
        )
        val paint = Paint().apply {
            shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            isFilterBitmap = true
            alpha = 255
        }
        return DrawableMesh(mesh, paint)
    }

    private fun createIndependentTileMesh(
        effect: PageTransitionEffect,
        offset: Float,
        transitionProgress: Float,
        outgoingPage: Boolean,
        width: Float,
        height: Float,
        verticalPager: Boolean,
        bitmap: Bitmap
    ): DrawableMesh? {
        val verticalSlices = verticalPager && effect == PageTransitionEffect.RETRO_FLY_TILES
        // Independent slices must lie ACROSS the direction of motion.
        // Horizontal pager: 12 horizontal strips; vertical pager: 12 vertical strips.
        val columns = if (verticalSlices) 12 else if (effect == PageTransitionEffect.RETRO_FLY_TILES) 1 else 8
        val rows = if (verticalSlices) 1 else 12
        val tileCount = columns * rows
        val vertexCount = tileCount * 4
        val vertices = ByteBuffer.allocateDirect(vertexCount * STRIDE).order(ByteOrder.nativeOrder())
        val indices = ByteBuffer.allocateDirect(tileCount * 6 * 2)
            .order(ByteOrder.nativeOrder()).asShortBuffer()
        val progress = offset.absoluteValue.coerceIn(0f, 1f)
        val direction = if (offset < 0f) -1f else 1f
        var vertexBase = 0
        for (row in 0 until rows) {
            for (column in 0 until columns) {
                val left = column / columns.toFloat()
                val right = (column + 1) / columns.toFloat()
                val top = row / rows.toFloat()
                val bottom = (row + 1) / rows.toFloat()
                val corners = arrayOf(
                    Pair(left, top), Pair(right, top),
                    Pair(left, bottom), Pair(right, bottom)
                )
                var tileOpacity = 0f
                corners.forEachIndexed { corner, (exactU, exactV) ->
                    val sampleU = when {
                        corner % 2 == 0 && column > 0 -> exactU + 0.000001f
                        corner % 2 == 1 && column < columns - 1 -> exactU - 0.000001f
                        else -> exactU
                    }
                    val sampleV = when {
                        corner < 2 && row > 0 -> exactV + 0.000001f
                        corner >= 2 && row < rows - 1 -> exactV - 0.000001f
                        else -> exactV
                    }
                    val vertex = deform(
                        effect, sampleU, sampleV, progress, transitionProgress,
                        outgoingPage, direction, width, height, verticalPager
                    )
                    vertices.putFloat(vertex.x)
                    vertices.putFloat(vertex.y)
                    vertices.putFloat(vertex.u * bitmap.width)
                    vertices.putFloat(vertex.v * bitmap.height)
                    vertices.putFloat(vertex.shade)
                    vertices.putFloat(vertex.opacity)
                    tileOpacity += vertex.opacity
                }
                if (tileOpacity >= 2f) {
                    indices.put(vertexBase.toShort())
                        .put((vertexBase + 2).toShort())
                        .put((vertexBase + 1).toShort())
                        .put((vertexBase + 1).toShort())
                        .put((vertexBase + 2).toShort())
                        .put((vertexBase + 3).toShort())
                }
                vertexBase += 4
            }
        }
        vertices.flip()
        if (indices.position() == 0) return null
        indices.flip()
        val mesh = Mesh(
            specification,
            Mesh.TRIANGLES,
            vertices,
            vertexCount,
            indices,
            RectF(-width, -height, width * 2f, height * 2f)
        )
        val paint = Paint().apply {
            shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            isFilterBitmap = true
            alpha = 255
        }
        return DrawableMesh(mesh, paint)
    }

    private fun putVisibleTriangle(
        indices: java.nio.ShortBuffer,
        opacities: FloatArray,
        first: Int,
        second: Int,
        third: Int
    ) {
        if (opacities[first] + opacities[second] + opacities[third] >= 1.5f) {
            indices.put(first.toShort()).put(second.toShort()).put(third.toShort())
        }
    }

    private val VERTICAL_AXIS_MESH_EFFECTS = setOf(
        PageTransitionEffect.DNA,
        PageTransitionEffect.DNA_OUT,
        PageTransitionEffect.PAGE_CURL_SOFT,
        PageTransitionEffect.PAGE_CURL_OUTWARD,
        PageTransitionEffect.SPHERE,
        PageTransitionEffect.RETRO_FLY_TILES,
        PageTransitionEffect.FIRE_REVEAL,
        PageTransitionEffect.THREAD_WIPE,
        PageTransitionEffect.DIAGONAL_WIPE,
        PageTransitionEffect.ARC_WIPE,
        PageTransitionEffect.TRIANGLE_WIPE
    )

    private fun deform(
        effect: PageTransitionEffect,
        u: Float,
        v: Float,
        progress: Float,
        transitionProgress: Float,
        outgoingPage: Boolean,
        direction: Float,
        width: Float,
        height: Float,
        verticalPager: Boolean
    ): Vertex {
        if (verticalPager && effect == PageTransitionEffect.SPHERE) {
            // Portrait pages are much taller than they are wide. Blindly
            // transposing the horizontal sphere bends its poles into an
            // elongated ring. Keep the established vertical travel direction,
            // but use a nearly circular projected envelope in physical pixels.
            val pairedDirection = if (outgoingPage) direction else -direction
            val rotated = sphere(
                v, u, transitionProgress, -pairedDirection,
                height, width, outgoingPage, compactVerticalSphere = true
            )
            return Vertex(
                rotated.y, rotated.x, rotated.v, rotated.u,
                rotated.shade, rotated.opacity
            )
        }
        if (verticalPager && effect in VERTICAL_AXIS_MESH_EFFECTS) {
            // Compute the established horizontal effect in transposed page
            // coordinates, then transpose geometry and UV back. This rotates
            // the motion axis without rotating app icons or the page bitmap.
            val transposed = deform(
                effect, v, u, progress, transitionProgress, outgoingPage,
                direction, height, width, verticalPager = false
            )
            return Vertex(
                transposed.y, transposed.x, transposed.v, transposed.u,
                transposed.shade, transposed.opacity
            )
        }
        // Each pager page reports its own offset sign, so the incoming sign is the
        // opposite of the outgoing sign. Paired effects use one visual direction
        // for both textures to keep their masks and motion exactly complementary.
        val pairedDirection = if (outgoingPage) direction else -direction
        return when (effect) {
        PageTransitionEffect.PAGE_CURL_SOFT,
        PageTransitionEffect.PAGE_CURL_OUTWARD -> pageCurl(u, v, progress, direction, width, height,
            softness = 1.0f, outward = effect == PageTransitionEffect.PAGE_CURL_OUTWARD)
        PageTransitionEffect.CUBE_FROM_PAGER_INSIDE -> sharedCube(
            u, v, transitionProgress, pairedDirection, width, height,
            verticalPager, outgoingPage, inside = true
        )
        PageTransitionEffect.CUBE_OUT_HORIZONTAL -> cube(u, v, progress, direction, width, height, false, false)
        PageTransitionEffect.CUBE_IN_HORIZONTAL -> cube(u, v, progress, direction, width, height, false, true)
        PageTransitionEffect.CUBE_OUT_VERTICAL -> cube(u, v, progress, direction, width, height, true, false)
        PageTransitionEffect.CUBE_IN_VERTICAL -> cube(u, v, progress, direction, width, height, true, true)
        PageTransitionEffect.SHARED_CUBE -> sharedCube(
            u, v, transitionProgress, pairedDirection, width, height, verticalPager, outgoingPage
        )
        PageTransitionEffect.CYLINDER -> cylinder(
            u, v, transitionProgress, -pairedDirection, width, height, verticalPager, outgoingPage
        )
        PageTransitionEffect.SPHERE -> sphere(
            u, v, transitionProgress, -pairedDirection, width, height, outgoingPage
        )
        PageTransitionEffect.CARD -> card(
            u, v, transitionProgress, pairedDirection, width, height, verticalPager
        )
        PageTransitionEffect.DNA -> dna(
            u, v, transitionProgress, -pairedDirection, width, height, outgoingPage
        )
        PageTransitionEffect.DNA_OUT -> dna(
            u, v, transitionProgress, pairedDirection, width, height, outgoingPage
        )
        PageTransitionEffect.RETRO_FLY_TILES -> retroFlyTiles(
            u, v, transitionProgress, pairedDirection, width, height, outgoingPage
        )
        PageTransitionEffect.FIRE_REVEAL -> fireReveal(
            u, v, transitionProgress, pairedDirection, width, height, outgoingPage
        )
        else -> MeshTransitionGeometry.deform(
            effect, u, v, transitionProgress, pairedDirection, width, height,
            outgoingPage, verticalPager
        )?.let { Vertex(it.x, it.y, it.u, it.v, it.shade, it.opacity) }
            ?: Vertex(u * width, v * height, u, v, 1f)
        }
    }

    private fun PageTransitionEffect.meshXSegments(): Int =
        if (this == PageTransitionEffect.SPHERE) 32 else X_SEGMENTS

    private fun PageTransitionEffect.meshYSegments(): Int =
        if (this == PageTransitionEffect.SPHERE) 36 else Y_SEGMENTS

    private fun pageCurl(
        u: Float, v: Float, progress: Float, direction: Float, width: Float, height: Float,
        softness: Float, outward: Boolean
    ): Vertex {
        if (progress <= 0.002f) return Vertex(u * width, v * height, u, v, 1f)
        val mirroredU = if (direction > 0f) u else 1f - u
        val start = (1f - progress * 1.18f).coerceIn(-0.18f, 0.995f)
        if (mirroredU <= start) {
            val x = if (direction > 0f) mirroredU * width else (1f - mirroredU) * width
            // The outward flap is on top of the remaining sheet. Its fold
            // casts a soft crease shadow over the flat portion of that sheet.
            val creaseShade = if (outward) {
                val span = (1f - start).coerceAtLeast(0.005f)
                val proximity = ((start - mirroredU) / (span * 0.16f)).coerceIn(0f, 1f)
                0.64f + 0.36f * proximity
            } else 1f
            return Vertex(x, v * height, u, v, creaseShade)
        }
        val span = (1f - start).coerceAtLeast(0.005f)
        val local = ((mirroredU - start) / span).coerceIn(0f, 1f)
        val angleMax = (0.38f + progress * 1.62f) * PI.toFloat() * softness
        val angle = local * angleMax
        val radius = width * span / angleMax.coerceAtLeast(0.18f)
        // Inward: the roll projects into the area being uncovered (under the
        // main sheet). Outward: the folded flap returns OVER the still-visible
        // sheet, on the other side of the crease. Merely negating depth left
        // the two scrolls visually almost indistinguishable.
        val curlOffset = sin(angle) * radius
        val rawX = if (outward) start * width - kotlin.math.abs(curlOffset)
            else start * width + curlOffset
        val depth = (1f - cos(angle)) * radius * if (outward) 1.4f else 1f
        val perspective = 1f + depth / (width * 1.65f)
        val projectedX = width * 0.5f + (rawX - width * 0.5f) * perspective
        val yWave = (v - 0.5f) * height * (perspective - 1f) * 0.32f
        val x = if (direction > 0f) projectedX else width - projectedX
        val back = cos(angle) < 0f
        val texMirror = mirroredU
        val textureU = if (direction > 0f) texMirror else 1f - texMirror
        val foldHighlight = (1f - ((local - 0.48f) / 0.18f).let { it * it }).coerceIn(0f, 1f)
        val shade = if (outward) {
            // Raised front flap: a bright curved highlight against the dark
            // crease. The underside is cooler/darker than the front face.
            ((if (back) 0.62f else 0.82f) + foldHighlight * 0.38f -
                local * 0.10f).coerceIn(0.45f, 1.25f)
        } else {
            (if (back) 0.58f else 0.84f) + foldHighlight * 0.42f - local * 0.12f
        }
        return Vertex(x, v * height - yWave, textureU, v, shade)
    }

    private fun cube(
        u: Float, v: Float, progress: Float, direction: Float, width: Float, height: Float,
        vertical: Boolean, inside: Boolean
    ): Vertex {
        // Compiz-style inside/outside variants share the hinge direction but use
        // opposite depth, so one face recedes while the other projects forward.
        val angle = progress * (PI.toFloat() / 2f) * direction
        val sign = if (direction > 0f) 1f else -1f
        val depthSign = if (inside) -1f else 1f
        return if (!vertical) {
            val pivot = if (sign > 0f) 0f else width
            val dx = u * width - pivot
            val x3 = pivot + dx * cos(angle)
            val z = dx * sin(angle) * depthSign
            val p = perspective(x3, v * height, z, width, height)
            Vertex(p.first, p.second, u, v, (0.48f + 0.52f * cos(angle).absoluteValue))
        } else {
            val pivot = if (sign > 0f) 0f else height
            val dy = v * height - pivot
            val y3 = pivot + dy * cos(angle)
            val z = dy * sin(angle) * depthSign
            val p = perspective(u * width, y3, z, width, height)
            Vertex(p.first, p.second, u, v, (0.48f + 0.52f * cos(angle).absoluteValue))
        }
    }

    private fun cylinder(
        u: Float, v: Float, progress: Float, direction: Float, width: Float, height: Float,
        vertical: Boolean, outgoingPage: Boolean
    ): Vertex {
        val wrap = surfaceWrap(progress)
        val pageTurn = PI.toFloat() / 2f
        val baseAngle = direction * if (outgoingPage) progress * pageTurn else (progress - 1f) * pageTurn
        val zoom = 1f - 0.18f * wrap
        val flatX = u * width
        val flatY = v * height
        val x: Float
        val y: Float
        val z: Float
        val light: Float
        if (!vertical) {
            val localAngle = (u - 0.5f) * (PI.toFloat() / 2f)
            val angle = baseAngle + localAngle
            val radius = width / (2f * sin(PI.toFloat() / 4f))
            val curvedX = width * 0.5f + sin(angle) * radius
            x = mix(flatX, curvedX, wrap)
            y = flatY
            z = (cos(angle) - 1f) * radius * wrap
            light = 0.58f + 0.42f * cos(angle).absoluteValue
        } else {
            val localAngle = (v - 0.5f) * (PI.toFloat() / 2f)
            val angle = baseAngle + localAngle
            val radius = height / (2f * sin(PI.toFloat() / 4f))
            x = flatX
            val curvedY = height * 0.5f + sin(angle) * radius
            y = mix(flatY, curvedY, wrap)
            z = (cos(angle) - 1f) * radius * wrap
            light = 0.58f + 0.42f * cos(angle).absoluteValue
        }
        val projected = perspective(x, y, z, width, height)
        val opacity = sharedSurfacePageOpacity(progress, outgoingPage)
        return Vertex(
            width * 0.5f + (projected.first - width * 0.5f) * zoom,
            height * 0.5f + (projected.second - height * 0.5f) * zoom,
            u, v, light, opacity
        )
    }

    private fun sharedCube(
        u: Float,
        v: Float,
        progress: Float,
        direction: Float,
        width: Float,
        height: Float,
        vertical: Boolean,
        outgoingPage: Boolean,
        inside: Boolean = false
    ): Vertex {
        val wrap = surfaceWrap(progress)
        val turn = -direction * progress * (PI.toFloat() / 2f)
        val flatX = u * width
        val flatY = v * height
        val cubeX: Float
        val cubeY: Float
        val cubeZ: Float
        val faceLight: Float

        if (!vertical) {
            val half = width * 0.5f
            val localX: Float
            val localZ: Float
            if (outgoingPage) {
                localX = (u - 0.5f) * width
                localZ = half
                faceLight = 0.52f + 0.48f * cos(turn).absoluteValue
            } else {
                val orientedU = if (direction > 0f) u else 1f - u
                localX = direction * half
                localZ = half - orientedU * width
                faceLight = 0.52f + 0.48f * (-direction * sin(turn)).absoluteValue
            }
            cubeX = width * 0.5f + localX * cos(turn) + localZ * sin(turn)
            cubeY = flatY
            cubeZ = -localX * sin(turn) + localZ * cos(turn) - half
        } else {
            val half = height * 0.5f
            val localY: Float
            val localZ: Float
            if (outgoingPage) {
                localY = (v - 0.5f) * height
                localZ = half
                faceLight = 0.52f + 0.48f * cos(turn).absoluteValue
            } else {
                val orientedV = if (direction > 0f) v else 1f - v
                localY = direction * half
                localZ = half - orientedV * height
                faceLight = 0.52f + 0.48f * (-direction * sin(turn)).absoluteValue
            }
            cubeX = flatX
            cubeY = height * 0.5f + localY * cos(turn) + localZ * sin(turn)
            cubeZ = -localY * sin(turn) + localZ * cos(turn) - half
        }

        val projected = perspective(
            mix(flatX, cubeX, wrap),
            mix(flatY, cubeY, wrap),
            (if (inside) -cubeZ else cubeZ) * wrap,
            width,
            height
        )
        // Outside view zooms out to show cube edges; the inside view moves
        // toward the walls so they surround the viewer.
        val zoom = if (inside) 1f + 0.10f * wrap else 1f - 0.16f * wrap
        return Vertex(
            width * 0.5f + (projected.first - width * 0.5f) * zoom,
            height * 0.5f + (projected.second - height * 0.5f) * zoom,
            u,
            v,
            mix(1f, faceLight, wrap),
            sharedSurfacePageOpacity(progress, outgoingPage)
        )
    }

    private fun sphere(
        u: Float, v: Float, progress: Float, direction: Float, width: Float, height: Float,
        outgoingPage: Boolean, compactVerticalSphere: Boolean = false
    ): Vertex {
        val wrap = surfaceWrap(progress)
        val pageTurn = PI.toFloat() / 2f
        val baseLongitude = direction * if (outgoingPage) progress * pageTurn else (progress - 1f) * pageTurn
        val longitude = baseLongitude + (u - 0.5f) * (PI.toFloat() / 2f)
        // Stronger two-axis curvature so this reads as a sphere rather than
        // a cylinder. The longitudinal edge pulls back at the poles.
        val latitudeExtent = if (compactVerticalSphere) 1.78f else 2.12f
        val latitude = (v - 0.5f) * latitudeExtent
        val radiusX = if (compactVerticalSphere) {
            // Bound the sphere to the shorter side of the real screen.
            minOf(width, height) / (2f * sin(PI.toFloat() / 4f))
        } else width / (2f * sin(PI.toFloat() / 4f))
        val radiusY = height / (2f * sin(latitudeExtent * 0.5f))
        val curvedX = width * 0.5f + sin(longitude) * cos(latitude) * radiusX
        val curvedY = height * 0.5f + sin(latitude) * radiusY
        val depthRadius = minOf(radiusX, radiusY)
        val z = (cos(longitude) * cos(latitude) - 1f) * depthRadius * wrap *
            (if (compactVerticalSphere) 1.06f else 1.38f)
        val projected = perspective(
            mix(u * width, curvedX, wrap),
            mix(v * height, curvedY, wrap),
            z, width, height
        )
        val zoom = 1f - (if (compactVerticalSphere) 0.10f else 0.24f) * wrap
        val light = 0.40f + 0.60f * (cos(longitude) * cos(latitude)).absoluteValue
        val opacity = sharedSurfacePageOpacity(progress, outgoingPage)
        return Vertex(
            width * 0.5f + (projected.first - width * 0.5f) * zoom,
            height * 0.5f + (projected.second - height * 0.5f) * zoom,
            u, v, light, opacity
        )
    }

    private fun card(
        u: Float, v: Float, progress: Float, direction: Float, width: Float, height: Float, vertical: Boolean
    ): Vertex {
        val angle = progress * PI.toFloat() * direction
        val dx = (u - 0.5f) * width
        val dy = (v - 0.5f) * height
        val x: Float
        val y: Float
        val z: Float
        if (vertical) {
            x = width * 0.5f + dx
            y = height * 0.5f + dy * cos(angle)
            z = dy * sin(angle)
        } else {
            x = width * 0.5f + dx * cos(angle)
            y = height * 0.5f + dy
            z = dx * sin(angle)
        }
        val p = perspective(x, y, z, width, height)
        return Vertex(p.first, p.second, if (cos(angle) < 0f) 1f - u else u, v, 0.48f + 0.52f * cos(angle).absoluteValue)
    }

    private fun dna(
        u: Float, v: Float, progress: Float, direction: Float, width: Float, height: Float,
        outgoingPage: Boolean
    ): Vertex {
        // Every horizontal strip performs exactly one half-turn. Lower strips
        // start later, while completed strips stay at PI and show the next page.
        val localProgress = ((progress - v * 0.5f) / 0.5f).coerceIn(0f, 1f)
        val eased = smoothStep(localProgress)
        val angle = direction * eased * PI.toFloat()
        val dx = (u - 0.5f) * width
        val x = width * 0.5f + dx * cos(angle)
        val z = dx * sin(angle)
        val turnOffset = direction * sin(eased * PI.toFloat()) * width * 0.055f
        val stripCurl = direction * sin(eased * PI.toFloat()) * width * 0.028f *
            sin(v * PI.toFloat() * 10f)
        val p = perspective(x + turnOffset + stripCurl, v * height, z, width, height)
        val mirrored = cos(angle) < 0f
        val textureOpacity = if (outgoingPage == (localProgress >= 0.5f)) 0f else 1f
        return Vertex(
            p.first, p.second,
            if (mirrored) 1f - u else u,
            v,
            0.46f + 0.54f * cos(angle).absoluteValue,
            textureOpacity
        )
    }

    private fun retroFlyTiles(
        u: Float, v: Float, progress: Float, direction: Float, width: Float, height: Float,
        outgoingPage: Boolean
    ): Vertex {
        val stripCount = 12
        val strip = minOf((v * stripCount).toInt(), stripCount - 1)
        val delay = strip / (stripCount - 1f) * 0.58f
        val local = ((progress - delay) / 0.42f).coerceIn(0f, 1f)
        val eased = smoothStep(local)
        val translation = if (outgoingPage) -direction * eased * width
        else direction * (1f - eased) * width
        return Vertex(
            u * width + translation,
            v * height,
            u,
            v,
            1f,
            1f
        )
    }

    private fun fireReveal(
        u: Float, v: Float, progress: Float, direction: Float, width: Float, height: Float,
        outgoingPage: Boolean
    ): Vertex {
        val flameEdge = fireBoundaryFraction(v, progress, direction)
        val revealed = if (direction > 0f) u >= flameEdge else u <= flameEdge
        val opacity = if (outgoingPage == revealed) 0f else 1f
        return Vertex(u * width, v * height, u, v, 1f, opacity)
    }

    private fun smoothStep(value: Float): Float = value * value * (3f - 2f * value)

    private fun surfaceWrap(progress: Float): Float {
        val enter = smoothStep((progress / 0.18f).coerceIn(0f, 1f))
        val exit = smoothStep(((1f - progress) / 0.18f).coerceIn(0f, 1f))
        return minOf(enter, exit)
    }

    private fun sharedSurfacePageOpacity(progress: Float, outgoingPage: Boolean): Float =
        if (outgoingPage) {
            if (progress < 0.82f) 1f else 0f
        } else {
            if (progress >= 0.18f) 1f else 0f
        }

    private fun mix(start: Float, end: Float, amount: Float): Float =
        start + (end - start) * amount.coerceIn(0f, 1f)

    private fun perspective(x: Float, y: Float, z: Float, width: Float, height: Float): Pair<Float, Float> {
        val camera = max(width, height) * 1.75f
        val scale = camera / (camera - z.coerceAtMost(camera * 0.78f))
        return Pair(
            width * 0.5f + (x - width * 0.5f) * scale,
            height * 0.5f + (y - height * 0.5f) * scale
        )
    }

    private data class Vertex(
        val x: Float,
        val y: Float,
        val u: Float,
        val v: Float,
        val shade: Float,
        val opacity: Float = 1f
    )
}
