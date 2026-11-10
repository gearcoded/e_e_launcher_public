package org.collegiumdesideriorum.e_e_launcher

import android.content.Intent
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.graphics.RuntimeShader
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan
import kotlin.random.Random

internal enum class EnvironmentEffectType(override val titleRes: Int, val symbol: String) : LocalizedTitle {
    RAIN(R.string.ui_rain, "☂"),
    SNOW(R.string.ui_snow, "❄"),
    BUBBLES(R.string.ui_bubbles, "○"),
    GREEN_LEAVES(R.string.ui_green_leaves, "♧"),
    MAPLE_LEAVES(R.string.ui_maple_leaves, "🍁"),
    CLOUDS(R.string.ui_clouds, "☁"),
    TUMBLEWEED(R.string.ui_tumbleweed, "◉"),
    FIRE(R.string.ui_fire, "♨"),
    GEOMETRIC(R.string.ui_geometric_shapes, "◇"),
    SUN_RAYS(R.string.ui_sun_rays, "☀"),
    FISH(R.string.ui_fish, "🐟"),
    DISTORTION(R.string.ui_distortion, "◌")
}

/** Distortion is guarded by an independently drawn normal workspace layer. */
internal const val DISTORTION_EFFECT_AVAILABLE = true

private const val DEFAULT_SUN_RAYS_PER_SOURCE = 20
private const val DEFAULT_SUN_RAY_HEIGHT_PERCENT = 20

internal enum class EnvironmentDistortionType(
    override val titleRes: Int,
    val available: Boolean = true
) : LocalizedTitle {
    MAGNIFIER(R.string.ui_distortion_magnifier),
    FISH_EYE(R.string.ui_distortion_fish_eye),
    MINIFY(R.string.ui_distortion_minify),
    WOBBLE(R.string.ui_distortion_wobble),
    WATER(R.string.ui_distortion_water),
    BLACK_HOLE(R.string.ui_distortion_black_hole),
    SPIRAL(R.string.ui_distortion_spiral),
    INSIDE_OUT(R.string.ui_distortion_inside_out)
}

internal enum class EnvironmentDirection(override val titleRes: Int) : LocalizedTitle {
    UP(R.string.ui_up),
    DOWN(R.string.ui_down),
    LEFT(R.string.ui_left),
    RIGHT(R.string.ui_right),
    UP_OR_DOWN(R.string.ui_up_or_down),
    LEFT_OR_RIGHT(R.string.ui_left_or_right),
    CHAOTIC(R.string.ui_chaotic),
    ALL_DIRECTIONS(R.string.ui_all_directions),
    ZOOM_IN_3D(R.string.ui_text_3d_zoom_in),
    ZOOM_OUT_3D(R.string.ui_text_3d_zoom_out),
    SPIRAL(R.string.ui_spiral),
    BOUNCE(R.string.ui_bounce_off_screen_edges)
}

internal enum class EnvironmentRotation(override val titleRes: Int) : LocalizedTitle {
    OFF(R.string.ui_disabled),
    CLOCKWISE(R.string.ui_clockwise),
    COUNTER_CLOCKWISE(R.string.ui_counterclockwise),
    ROTATE_3D_RIGHT(R.string.ui_text_3d_right),
    ROTATE_3D_LEFT(R.string.ui_text_3d_left),
    ROTATE_3D_DIAGONAL(R.string.ui_text_3d_diagonal),
    RANDOM(R.string.ui_random)
}

internal enum class EnvironmentFillMode(override val titleRes: Int) : LocalizedTitle {
    COLOR(R.string.ui_colour),
    GRADIENT(R.string.ui_gradient),
    IMAGE(R.string.ui_picture),
    ICON_PACK(R.string.ui_icon_pack)
}

internal enum class EnvironmentGradientType(override val titleRes: Int) : LocalizedTitle {
    HORIZONTAL(R.string.ui_horizontal),
    VERTICAL(R.string.ui_vertical),
    DIAGONAL(R.string.ui_diagonal_0101),
    RADIAL(R.string.ui_radial)
}

internal data class EnvironmentEffectOptions(
    val enabled: Boolean = false,
    val direction: EnvironmentDirection = EnvironmentDirection.DOWN,
    val rotation: EnvironmentRotation = EnvironmentRotation.OFF,
    val colorHex: String = "#FFFFFF",
    val secondaryColorHex: String = "#7EC8FF",
    val fillMode: EnvironmentFillMode = EnvironmentFillMode.COLOR,
    val gradientType: EnvironmentGradientType = EnvironmentGradientType.VERTICAL,
    val imageUri: String? = null,
    val imageUris: List<String> = emptyList(),
    val iconPackPackage: String? = null,
    val iconPackDrawable: String? = null,
    val opacityPercent: Int = 70,
    val blurDp: Int = 0,
    val speedPercent: Int = 100,
    val count: Int = 36,
    val sunRayHeightPercent: Int = DEFAULT_SUN_RAY_HEIGHT_PERCENT,
    val sunRaysPerSource: Int = DEFAULT_SUN_RAYS_PER_SOURCE,
    val sunRayThicknessPx: Int = 24,
    val sunRayMaxAngleDegrees: Int = 22,
    val distortionType: EnvironmentDistortionType = EnvironmentDistortionType.MAGNIFIER,
    val distortionShape: FolderShape = FolderShape.CIRCLE,
    val distortionSizePercent: Int = 24,
    val distortionStrengthPercent: Int = 100
)

internal data class EnvironmentEffectsConfig(
    val effects: Map<EnvironmentEffectType, EnvironmentEffectOptions> = defaultEnvironmentEffects(),
    val layerMode: EnvironmentEffectsLayerMode = EnvironmentEffectsLayerMode.BEHIND
) {
    fun options(type: EnvironmentEffectType): EnvironmentEffectOptions =
        effects[type] ?: defaultOptions(type)

    fun update(type: EnvironmentEffectType, value: EnvironmentEffectOptions): EnvironmentEffectsConfig =
        copy(effects = effects + (type to value))
}

internal enum class EnvironmentEffectsLayerMode(override val titleRes: Int) : LocalizedTitle {
    BEHIND(R.string.ui_behind_elements),
    ABOVE(R.string.ui_above_everything)
}

internal fun defaultEnvironmentEffects(): Map<EnvironmentEffectType, EnvironmentEffectOptions> =
    EnvironmentEffectType.values().associateWith(::defaultOptions)

private fun defaultCount(type: EnvironmentEffectType): Int = when (type) {
    EnvironmentEffectType.RAIN -> 90
    EnvironmentEffectType.SNOW -> 55
    EnvironmentEffectType.BUBBLES -> 32
    EnvironmentEffectType.GREEN_LEAVES, EnvironmentEffectType.MAPLE_LEAVES -> 34
    EnvironmentEffectType.CLOUDS -> 8
    EnvironmentEffectType.TUMBLEWEED -> 10
    EnvironmentEffectType.FIRE -> 52
    EnvironmentEffectType.GEOMETRIC -> 36
    EnvironmentEffectType.SUN_RAYS -> 2
    EnvironmentEffectType.FISH -> 8
    EnvironmentEffectType.DISTORTION -> 2
}

private fun defaultOptions(type: EnvironmentEffectType): EnvironmentEffectOptions = when (type) {
    EnvironmentEffectType.RAIN -> EnvironmentEffectOptions(direction = EnvironmentDirection.DOWN, colorHex = "#A9D8FF", secondaryColorHex = "#5EA9E6", opacityPercent = 50, count = defaultCount(type))
    EnvironmentEffectType.SNOW -> EnvironmentEffectOptions(direction = EnvironmentDirection.DOWN, colorHex = "#FFFFFF", secondaryColorHex = "#BBDFFF", opacityPercent = 62, blurDp = 5, count = defaultCount(type))
    EnvironmentEffectType.BUBBLES -> EnvironmentEffectOptions(direction = EnvironmentDirection.UP, colorHex = "#B9E9FF", secondaryColorHex = "#8FB7FF", opacityPercent = 77, blurDp = 1, count = defaultCount(type))
    EnvironmentEffectType.GREEN_LEAVES -> EnvironmentEffectOptions(direction = EnvironmentDirection.DOWN, colorHex = "#62C96B", secondaryColorHex = "#247A3A", opacityPercent = 78, count = defaultCount(type))
    EnvironmentEffectType.MAPLE_LEAVES -> EnvironmentEffectOptions(direction = EnvironmentDirection.DOWN, colorHex = "#E97A35", secondaryColorHex = "#9D291F", opacityPercent = 82, count = defaultCount(type))
    EnvironmentEffectType.CLOUDS -> EnvironmentEffectOptions(direction = EnvironmentDirection.RIGHT, colorHex = "#E8EEF7", secondaryColorHex = "#AFC4DD", opacityPercent = 75, blurDp = 10, speedPercent = 55, count = defaultCount(type))
    EnvironmentEffectType.TUMBLEWEED -> EnvironmentEffectOptions(direction = EnvironmentDirection.LEFT_OR_RIGHT, rotation = EnvironmentRotation.CLOCKWISE, colorHex = "#B88954", secondaryColorHex = "#77502F", opacityPercent = 80, speedPercent = 75, count = defaultCount(type))
    EnvironmentEffectType.FIRE -> EnvironmentEffectOptions(direction = EnvironmentDirection.UP, colorHex = "#FF7A2F", secondaryColorHex = "#FFD060", opacityPercent = 75, blurDp = 1, count = defaultCount(type))
    EnvironmentEffectType.GEOMETRIC -> EnvironmentEffectOptions(direction = EnvironmentDirection.CHAOTIC, colorHex = "#9BB2FF", secondaryColorHex = "#DD8CFF", opacityPercent = 58, count = defaultCount(type))
    EnvironmentEffectType.SUN_RAYS -> EnvironmentEffectOptions(
        colorHex = "#FFF1B8",
        secondaryColorHex = "#BDEBFF",
        opacityPercent = 72,
        blurDp = 2,
        speedPercent = 162,
        count = defaultCount(type),
        sunRaysPerSource = 12,
        sunRayThicknessPx = 60,
        sunRayMaxAngleDegrees = 45
    )
    EnvironmentEffectType.FISH -> EnvironmentEffectOptions(colorHex = "#5FD6E8", secondaryColorHex = "#FFB85C", opacityPercent = 82, speedPercent = 100, count = defaultCount(type))
    EnvironmentEffectType.DISTORTION -> EnvironmentEffectOptions(
        direction = EnvironmentDirection.BOUNCE,
        speedPercent = 70,
        count = defaultCount(type),
        distortionType = EnvironmentDistortionType.MAGNIFIER,
        distortionShape = FolderShape.CIRCLE,
        distortionSizePercent = 24,
        distortionStrengthPercent = 100
    )
}

private data class EnvironmentParticle(
    val x: Float,
    val y: Float,
    val size: Float,
    val speed: Float,
    val phase: Float,
    val variant: Int
)

@Composable
internal fun EnvironmentEffectsLayer(
    config: EnvironmentEffectsConfig,
    fishTapEvents: SharedFlow<Offset>? = null,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        EnvironmentEffectType.values().forEach { type ->
            val options = config.options(type)
            if (options.enabled && type != EnvironmentEffectType.DISTORTION) {
                EnvironmentEffectCanvas(
                    type = type,
                    options = options,
                    fishTapEvents = fishTapEvents,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun rememberEnvironmentImage(options: EnvironmentEffectOptions): ImageBitmap? {
    val context = LocalContext.current
    var bitmap by remember(
        options.fillMode,
        options.imageUri,
        options.iconPackPackage,
        options.iconPackDrawable
    ) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(
        options.fillMode,
        options.imageUri,
        options.iconPackPackage,
        options.iconPackDrawable
    ) {
        bitmap = withContext(Dispatchers.IO) {
            when (options.fillMode) {
                EnvironmentFillMode.IMAGE -> options.imageUri?.let { raw ->
                    runCatching {
                        val source = ImageDecoder.createSource(context.contentResolver, Uri.parse(raw))
                        ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                            decoder.setTargetSize(192, 192)
                            decoder.isMutableRequired = false
                        }.asImageBitmap()
                    }.getOrNull()
                }
                EnvironmentFillMode.ICON_PACK -> {
                    val pack = options.iconPackPackage
                    val drawable = options.iconPackDrawable
                    if (!pack.isNullOrBlank() && !drawable.isNullOrBlank()) {
                        IconPackRepository.loadIcon(context.applicationContext, pack, drawable)
                    } else null
                }
                else -> null
            }
        }
    }
    return bitmap
}

@Composable
private fun rememberEnvironmentImages(uris: List<String>): List<ImageBitmap> {
    val context = LocalContext.current
    var bitmaps by remember(uris) { mutableStateOf<List<ImageBitmap>>(emptyList()) }
    LaunchedEffect(uris) {
        bitmaps = withContext(Dispatchers.IO) {
            uris.take(10).mapNotNull { raw ->
                runCatching {
                    val source = ImageDecoder.createSource(context.contentResolver, Uri.parse(raw))
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.setTargetSize(256, 256)
                        decoder.isMutableRequired = false
                    }.asImageBitmap()
                }.getOrNull()
            }
        }
    }
    return bitmaps
}

private data class FishMotionState(
    var x: Float,
    val y: Float,
    val size: Float,
    val speedFactor: Float,
    var direction: Int,
    var secondsUntilTurn: Float,
    val variant: Int,
    var boosted: Boolean = false,
    var boostMultiplier: Float = 1f
)

/**
 * Project-owned sun-ray renderer using a distance-to-axis AGSL beam model.
 * Each ray has its own moving top-edge origin and a normalized transverse
 * colour/opacity profile. Controls include speed, height, count, two colours,
 * opacity, blur, physical thickness in pixels and maximum angular deviation.
 */
@Composable
private fun IndependentSunRaysEffectCanvas(
    options: EnvironmentEffectOptions,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var running by remember {
        mutableStateOf(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, _ ->
            running = lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var elapsedSeconds by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(running, options.speedPercent) {
        if (!running || options.speedPercent <= 0) return@LaunchedEffect
        var previousNanos = 0L
        while (isActive) {
            withFrameNanos { now ->
                if (previousNanos != 0L) {
                    val delta = ((now - previousNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
                    elapsedSeconds += delta * (options.speedPercent.coerceIn(0, 300) / 100f)
                }
                previousNanos = now
            }
        }
    }

    val primary = environmentColor(options.colorHex)
    val secondary = environmentColor(options.secondaryColorHex)
    val alphaBase = options.opacityPercent.coerceIn(0, 100) / 100f

    // Ray count means exactly the number of independent visible beams.  It is
    // deliberately excluded from every width/gradient calculation.
    val rayCount = options.sunRaysPerSource.coerceIn(1, 20)
    val raySeeds = remember(rayCount) {
        List(rayCount) { index ->
            val seed = (index * 1103515245L + 12345L) and 0x7fffffff
            fun part(shift: Int): Float = ((seed ushr shift) and 0x3FF).toFloat() / 1023f
            floatArrayOf(
                part(0),
                part(7),
                part(13),
                part(17),
                part(3)
            )
        }
    }

    // A shader is used instead of nested polygons/bands so the same normalized
    // transverse profile is preserved at every thickness.  A 100 px beam is
    // therefore a geometrically enlarged version of a 10 px beam rather than a
    // wide flat polygon containing a narrow fixed-size colour transition.
    val runtimeShader = remember { RuntimeShader(INDEPENDENT_RAY_SHADER) }
    val paint = remember(runtimeShader) { Paint().apply { shader = runtimeShader } }

    Canvas(modifier = modifier.blur(options.blurDp.coerceIn(0, 18).dp)) {
        val rayHeight = size.height * options.sunRayHeightPercent.coerceIn(10, 100) / 100f
        val rayThicknessPx = options.sunRayThicknessPx.coerceIn(2, 160).toFloat()
        val maxAngleRadians = Math.toRadians(
            options.sunRayMaxAngleDegrees.coerceIn(0, 75).toDouble()
        ).toFloat()
        val sourceY = -size.height * 0.06f
        val verticalSpan = (rayHeight - sourceY).coerceAtLeast(1f)

        runtimeShader.setFloatUniform("colorCenter", primary.red, primary.green, primary.blue)
        runtimeShader.setFloatUniform("colorEdge", secondary.red, secondary.green, secondary.blue)

        raySeeds.forEachIndexed { index, seed ->
            val randomA = seed[0]
            val randomB = seed[1]
            val randomC = seed[2]
            val randomD = seed[3]

            val baseSourceX = size.width * (0.08f + randomA * 0.84f)
            val sourceWander = sin(
                elapsedSeconds * (0.16f + randomB * 0.18f) + randomC * PI.toFloat() * 2f
            ) * size.width * 0.035f
            val sourceX = baseSourceX + sourceWander

            val baseDirection = if (rayCount <= 1) {
                0f
            } else {
                (index.toFloat() / (rayCount - 1) * 2f - 1f) * 0.72f
            }
            val angleWander = sin(
                elapsedSeconds * (0.26f + randomC * 0.22f) + randomD * 9.7f
            ) * 0.18f
            val directionSlot = (baseDirection + angleWander).coerceIn(-1f, 1f)
            val rayAngle = directionSlot * maxAngleRadians
            val targetX = sourceX + tan(rayAngle) * verticalSpan

            // Thickness is the actual full diameter in pixels.  Variation is
            // intentionally small and does not change the gradient proportions.
            val widthVariation = 0.92f + randomB * 0.16f
            val halfWidth = rayThicknessPx * widthVariation * 0.5f

            // Visibility is intentionally much faster than the geometric
            // wandering of the beam.  The previous 0.40..0.68 rad/s pulse made
            // a ray take many seconds to appear/fade even at maximum speed.
            // A full 0..1 envelope also makes this a real appearance/fade cycle
            // instead of a small brightness wobble around an always-visible ray.
            val visibilityWave = 0.5f + 0.5f * sin(
                elapsedSeconds * (1.60f + randomD * 1.12f) + randomC * 11f
            )
            val visibility = visibilityWave * visibilityWave * (3f - 2f * visibilityWave)
            val rayAlpha = alphaBase * (0.38f + 0.42f * randomC) *
                visibility.coerceIn(0f, 1f)

            runtimeShader.setFloatUniform("rayStart", sourceX, sourceY)
            runtimeShader.setFloatUniform("rayEnd", targetX, rayHeight)
            runtimeShader.setFloatUniform("halfWidth", halfWidth.coerceAtLeast(1f))
            runtimeShader.setFloatUniform("rayOpacity", rayAlpha.coerceIn(0f, 1f))

            val margin = halfWidth + with(this) { options.blurDp.coerceIn(0, 18).dp.toPx() } * 2f + 4f
            val left = (minOf(sourceX, targetX) - margin).coerceAtLeast(0f)
            val right = (maxOf(sourceX, targetX) + margin).coerceAtMost(size.width)
            if (right > left) {
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawRect(left, 0f, right, rayHeight.coerceAtMost(size.height), paint)
                }
            }
        }
    }
}

/**
 * Project-owned AGSL sun-ray beam profile.
 *
 * The transverse coordinate is always normalized by halfWidth.  This is the
 * important part of the GIMP-style reference behaviour: changing thickness
 * scales the complete colour/opacity profile instead of stretching a flat
 * centre while leaving a fixed-size edge gradient.
 */
private const val INDEPENDENT_RAY_SHADER = """
    uniform float2 rayStart;
    uniform float2 rayEnd;
    uniform float halfWidth;
    uniform float rayOpacity;
    uniform float3 colorCenter;
    uniform float3 colorEdge;

    half4 main(float2 fragmentCoordinate) {
        float2 axis = rayEnd - rayStart;
        float axisLengthSquared = max(dot(axis, axis), 1.0);

        // Position along the beam, 0 at the source and 1 at the configured end.
        float tRaw = dot(fragmentCoordinate - rayStart, axis) / axisLengthSquared;
        float t = clamp(tRaw, 0.0, 1.0);
        float2 closest = rayStart + axis * t;

        // 0 at the centre line, 1 at the selected physical beam edge.
        float transverse = length(fragmentCoordinate - closest) / max(halfWidth, 1.0);
        float side = clamp(1.0 - transverse, 0.0, 1.0);

        // The reference profile is close to a triangular light distribution:
        // the centre is bright and the entire colour/opacity transition expands
        // proportionally as the beam gets wider.  The slight power keeps the
        // centre readable without creating a narrow fixed-width "tube".
        float profile = pow(side, 1.28);
        float3 rayColor = mix(colorEdge, colorCenter, pow(profile, 0.72));

        // Fade before both longitudinal ends so the configured height never
        // looks like a hard polygon cut.  These values are relative to length,
        // therefore they also remain stable if ray height changes.
        float startFade = smoothstep(0.00, 0.075, tRaw);
        float endFade = 1.0 - smoothstep(0.74, 1.00, tRaw);
        float longitudinal = startFade * endFade;

        // Explicitly suppress pixels past the segment.  The fades above make
        // this transition invisible rather than clipped.
        float segmentMask = step(0.0, tRaw) * step(tRaw, 1.0);
        float alpha = clamp(rayOpacity * profile * longitudinal * segmentMask, 0.0, 1.0);

        // Android RuntimeShader output must be premultiplied.
        return half4(half3(rayColor * alpha), half(alpha));
    }
"""

@Composable
private fun FishEffectCanvas(
    options: EnvironmentEffectOptions,
    tapEvents: SharedFlow<Offset>?,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var running by remember {
        mutableStateOf(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, _ ->
            running = lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val fish = remember(options.count) {
        MutableList(options.count.coerceIn(1, 220)) { index ->
            val seed = index * 1103515245L + 17431L
            fun part(shift: Int): Float = ((seed ushr shift) and 0xFFFF).toFloat() / 65535f
            FishMotionState(
                x = -0.18f + part(0) * 1.36f,
                y = 0.10f + part(8) * 0.80f,
                size = 0.72f + part(16) * 0.72f,
                speedFactor = 0.28f + part(24) * 0.72f,
                direction = if (index % 2 == 0) 1 else -1,
                secondsUntilTurn = 3.5f + part(4) * 8f,
                variant = index
            )
        }
    }
    val images = rememberEnvironmentImages(options.imageUris)
    val localDensity = LocalDensity.current
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var frameSerial by remember { mutableIntStateOf(0) }

    LaunchedEffect(tapEvents, canvasSize, fish) {
        tapEvents?.collect { tap ->
            val width = canvasSize.width.coerceAtLeast(1).toFloat()
            val height = canvasSize.height.coerceAtLeast(1).toFloat()
            val hitIndex = fish.indices.lastOrNull { index ->
                val item = fish[index]
                val halfWidth = with(localDensity) { 46.dp.toPx() } * item.size
                val halfHeight = with(localDensity) { 30.dp.toPx() } * item.size
                abs(tap.x - item.x * width) <= halfWidth &&
                    abs(tap.y - item.y * height) <= halfHeight
            }
            if (hitIndex != null) {
                fish[hitIndex].apply {
                    boosted = true
                    boostMultiplier = Random.nextFloat() * 6f + 4f
                }
                frameSerial += 1
            }
        }
    }
    LaunchedEffect(running, options.speedPercent, fish) {
        if (!running || options.speedPercent <= 0) return@LaunchedEffect
        var previousNanos = 0L
        while (isActive) {
            withFrameNanos { now ->
                if (previousNanos != 0L) {
                    val delta = ((now - previousNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
                    val maximumSpeed = 0.20f * (options.speedPercent.coerceIn(0, 300) / 100f)
                    fish.forEach { item ->
                        val boost = if (item.boosted) item.boostMultiplier else 1f
                        item.x += item.direction * maximumSpeed * item.speedFactor * boost * delta
                        if (item.boosted) {
                            val leftScreen = item.direction < 0 && item.x < -0.24f
                            val rightScreen = item.direction > 0 && item.x > 1.24f
                            if (leftScreen || rightScreen) {
                                item.x = if (leftScreen) 1.24f else -0.24f
                                item.boosted = false
                                item.boostMultiplier = 1f
                                item.secondsUntilTurn = 4f +
                                    fraction(item.y * 11.7f + item.variant * 0.37f) * 8f
                            }
                        } else {
                            item.secondsUntilTurn -= delta
                            if (item.secondsUntilTurn <= 0f) {
                                item.direction *= -1
                                item.secondsUntilTurn = 4f +
                                    fraction(item.x * 7.31f + item.y * 11.7f) * 8f
                            }
                            if (item.x < -0.24f) item.x = 1.24f
                            if (item.x > 1.24f) item.x = -0.24f
                        }
                    }
                    frameSerial += 1
                }
                previousNanos = now
            }
        }
    }
    val colorA = environmentColor(options.colorHex)
    val colorB = environmentColor(options.secondaryColorHex)
    val alpha = options.opacityPercent.coerceIn(0, 100) / 100f
    Canvas(
        modifier = modifier
            .onSizeChanged { canvasSize = it }
            .blur(options.blurDp.coerceIn(0, 18).dp)
    ) {
        frameSerial
        fish.forEach { item ->
            val position = Offset(item.x * size.width, item.y * size.height)
            val halfWidth = 42f * density * item.size
            val halfHeight = 25f * density * item.size
            val customImage = images.getOrNull(Math.floorMod(item.variant, images.size.coerceAtLeast(1)))
            withTransform({
                if (item.direction < 0) scale(-1f, 1f, position)
            }) {
                if (customImage != null) {
                    drawImage(
                        image = customImage,
                        srcOffset = IntOffset.Zero,
                        srcSize = IntSize(customImage.width, customImage.height),
                        dstOffset = IntOffset(
                            (position.x - halfWidth).toInt(),
                            (position.y - halfHeight).toInt()
                        ),
                        dstSize = IntSize((halfWidth * 2f).toInt(), (halfHeight * 2f).toInt()),
                        alpha = alpha
                    )
                } else {
                    val variantColor = lerp(colorA, colorB, Math.floorMod(item.variant, 10) / 9f)
                        .copy(alpha = alpha)
                    drawOval(
                        variantColor,
                        topLeft = position - Offset(halfWidth * 0.72f, halfHeight * 0.72f),
                        size = Size(halfWidth * 1.44f, halfHeight * 1.44f)
                    )
                    val tail = Path().apply {
                        moveTo(position.x - halfWidth * 0.60f, position.y)
                        lineTo(position.x - halfWidth, position.y - halfHeight * 0.72f)
                        lineTo(position.x - halfWidth, position.y + halfHeight * 0.72f)
                        close()
                    }
                    drawPath(tail, variantColor)
                    drawCircle(
                        Color.White.copy(alpha = alpha),
                        radius = halfHeight * 0.13f,
                        center = position + Offset(halfWidth * 0.40f, -halfHeight * 0.16f)
                    )
                    drawCircle(
                        Color.Black.copy(alpha = alpha),
                        radius = halfHeight * 0.065f,
                        center = position + Offset(halfWidth * 0.43f, -halfHeight * 0.16f)
                    )
                }
            }
        }
    }
}

@Composable
private fun EnvironmentEffectCanvas(
    type: EnvironmentEffectType,
    options: EnvironmentEffectOptions,
    fishTapEvents: SharedFlow<Offset>?,
    modifier: Modifier = Modifier
) {
    if (type == EnvironmentEffectType.DISTORTION) return
    if (type == EnvironmentEffectType.SUN_RAYS) {
        IndependentSunRaysEffectCanvas(options, modifier)
        return
    }
    if (type == EnvironmentEffectType.FISH) {
        FishEffectCanvas(options, fishTapEvents, modifier)
        return
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    var running by remember {
        mutableStateOf(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, _ ->
            running = lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var elapsedSeconds by remember(type) { mutableFloatStateOf(0f) }
    LaunchedEffect(running, options.speedPercent) {
        if (!running || options.speedPercent <= 0) return@LaunchedEffect
        var previousNanos = 0L
        while (isActive) {
            withFrameNanos { now ->
                if (previousNanos != 0L) {
                    val delta = ((now - previousNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
                    elapsedSeconds += delta * (options.speedPercent.coerceIn(0, 300) / 100f)
                }
                previousNanos = now
            }
        }
    }

    val particles = remember(type, options.count) {
        List(options.count.coerceIn(1, 220)) { index ->
            // Four highly correlated slices of one linear seed previously
            // produced parallel rows / staircase patterns after a few cycles.
            // Generate independent coordinates and velocities for each item.
            val rng = Random(index * 0x45D9F3B + type.ordinal * 0x119DE1F3 + 0x1F123BB5)
            EnvironmentParticle(
                x = rng.nextFloat(),
                y = rng.nextFloat(),
                size = 0.45f + rng.nextFloat() * 1.15f,
                speed = 0.45f + rng.nextFloat() * 1.1f,
                phase = rng.nextFloat() * (PI * 2).toFloat(),
                variant = rng.nextInt()
            )
        }
    }
    val customBitmap = rememberEnvironmentImage(options)
    val colorA = environmentColor(options.colorHex)
    val colorB = environmentColor(options.secondaryColorHex)
    val alpha = options.opacityPercent.coerceIn(0, 100) / 100f

    Canvas(modifier = modifier.blur(options.blurDp.coerceIn(0, 18).dp)) {
        particles.forEachIndexed { index, particle ->
            val motion = particleMotion(
                direction = options.direction,
                particle = particle,
                index = index,
                elapsedSeconds = elapsedSeconds,
                canvasSize = size,
                edgeMarginPx = when (type) {
                    EnvironmentEffectType.CLOUDS -> 34f * particle.size * density * 2.5f
                    EnvironmentEffectType.TUMBLEWEED -> 22f * particle.size * density * 1.6f
                    EnvironmentEffectType.FIRE -> 9f * particle.size * density * 2.2f
                    else -> 9f * particle.size * density * 2f
                }
            )
            val position = motion.position
            val particleColor = effectColorAt(
                colorA = colorA,
                colorB = colorB,
                type = options.gradientType,
                fillMode = options.fillMode,
                position = position,
                canvasSize = size
            ).copy(alpha = alpha)
            val px = when (type) {
                EnvironmentEffectType.CLOUDS -> 34f * particle.size
                EnvironmentEffectType.TUMBLEWEED -> 22f * particle.size
                else -> 9f * particle.size
            } * density * motion.scale
            val transform = particleRotation(options.rotation, particle, index, elapsedSeconds)

            withTransform({
                rotate(transform.degrees, pivot = position)
                scale(transform.scaleX, transform.scaleY, pivot = position)
            }) {
                if (customBitmap != null && (options.fillMode == EnvironmentFillMode.IMAGE || options.fillMode == EnvironmentFillMode.ICON_PACK)) {
                    val imageSize = (px * 2.5f).coerceAtLeast(10f).toInt()
                    drawImage(
                        image = customBitmap,
                        srcOffset = IntOffset.Zero,
                        srcSize = IntSize(customBitmap.width, customBitmap.height),
                        dstOffset = IntOffset((position.x - imageSize / 2f).toInt(), (position.y - imageSize / 2f).toInt()),
                        dstSize = IntSize(imageSize, imageSize),
                        alpha = alpha
                    )
                } else {
                    drawEnvironmentParticle(type, particle, index, position, px, particleColor, colorB, alpha, elapsedSeconds)
                }
            }
        }
    }
}

private data class ParticleMotion(val position: Offset, val scale: Float = 1f)
private data class ParticleRotation(val degrees: Float, val scaleX: Float = 1f, val scaleY: Float = 1f)

private fun particleMotion(
    direction: EnvironmentDirection,
    particle: EnvironmentParticle,
    index: Int,
    elapsedSeconds: Float,
    canvasSize: Size,
    edgeMarginPx: Float
): ParticleMotion {
    val width = canvasSize.width.coerceAtLeast(1f)
    val height = canvasSize.height.coerceAtLeast(1f)
    val base = Offset(particle.x * width, particle.y * height)
    val travel = maxOf(width, height) * 1.35f
    val amount = elapsedSeconds * (0.055f + particle.speed * 0.045f) + particle.phase / (PI * 2f).toFloat()
    val center = Offset(width / 2f, height / 2f)
    return when (direction) {
        EnvironmentDirection.ZOOM_IN_3D,
        EnvironmentDirection.ZOOM_OUT_3D -> {
            val cycle = fraction(amount * 0.42f)
            val progress = if (direction == EnvironmentDirection.ZOOM_IN_3D) cycle else 1f - cycle
            val scale = 0.16f + progress * 1.55f
            val radial = base - center
            ParticleMotion(center + radial * scale, scale.coerceIn(0.16f, 1.7f))
        }
        EnvironmentDirection.SPIRAL -> {
            val cycle = fraction(amount * 0.34f)
            val radiusMax = minOf(width, height) * (0.12f + particle.x * 0.43f)
            val radius = radiusMax * (0.20f + cycle * 0.80f)
            val angle = particle.phase + elapsedSeconds * (0.65f + particle.speed * 0.72f) + index * 0.19f
            ParticleMotion(
                Offset(
                    center.x + cos(angle.toDouble()).toFloat() * radius,
                    center.y + sin(angle.toDouble()).toFloat() * radius
                ),
                0.70f + cycle * 0.45f
            )
        }
        EnvironmentDirection.BOUNCE -> {
            val angle = particle.phase + index * 0.41f
            val vx = cos(angle.toDouble()).toFloat() * travel * (0.065f + particle.speed * 0.035f)
            val vy = sin(angle.toDouble()).toFloat() * travel * (0.065f + particle.speed * 0.035f)
            ParticleMotion(Offset(reflect(base.x + vx * elapsedSeconds, width), reflect(base.y + vy * elapsedSeconds, height)))
        }
        else -> {
            val vector = movementVector(direction, particle.phase, elapsedSeconds, index, particle.variant)
            val speed = travel * (0.055f + particle.speed * 0.045f)
            // A small, asynchronous crosswind means even particles sharing a
            // cardinal direction never travel on identical straight rails.
            val drift = sin((elapsedSeconds * (0.27f + particle.speed * 0.33f) +
                particle.phase * 2.7f).toDouble()).toFloat() *
                minOf(width, height) * (0.006f + particle.size * 0.012f)
            val perpendicular = Offset(-vector.y, vector.x)
            val raw = base + vector * (elapsedSeconds * speed) + perpendicular * drift
            // Wrap after the entire object has left the visible area. Clouds
            // especially must not teleport while their silhouette is visible.
            ParticleMotion(Offset(
                wrapBeyondEdge(raw.x, width, edgeMarginPx),
                wrapBeyondEdge(raw.y, height, edgeMarginPx)
            ))
        }
    }
}

private fun particleRotation(
    requested: EnvironmentRotation,
    particle: EnvironmentParticle,
    index: Int,
    elapsedSeconds: Float
): ParticleRotation {
    val mode = if (requested == EnvironmentRotation.RANDOM) {
        when (Math.floorMod(particle.variant, 6)) {
            0 -> EnvironmentRotation.OFF
            1 -> EnvironmentRotation.CLOCKWISE
            2 -> EnvironmentRotation.COUNTER_CLOCKWISE
            3 -> EnvironmentRotation.ROTATE_3D_RIGHT
            4 -> EnvironmentRotation.ROTATE_3D_LEFT
            else -> EnvironmentRotation.ROTATE_3D_DIAGONAL
        }
    } else requested
    val angle = elapsedSeconds * (70f + particle.speed * 75f) + particle.phase * 57.2958f + index * 11f
    fun axisScale(value: Float): Float {
        val raw = cos(Math.toRadians(value.toDouble())).toFloat()
        return if (kotlin.math.abs(raw) < 0.12f) 0.12f * if (raw < 0f) -1f else 1f else raw
    }
    return when (mode) {
        EnvironmentRotation.OFF -> ParticleRotation(0f)
        EnvironmentRotation.CLOCKWISE -> ParticleRotation(angle)
        EnvironmentRotation.COUNTER_CLOCKWISE -> ParticleRotation(-angle)
        EnvironmentRotation.ROTATE_3D_RIGHT -> ParticleRotation(0f, scaleX = axisScale(angle), scaleY = 1f)
        EnvironmentRotation.ROTATE_3D_LEFT -> ParticleRotation(0f, scaleX = axisScale(-angle), scaleY = 1f)
        EnvironmentRotation.ROTATE_3D_DIAGONAL -> ParticleRotation(angle * 0.18f, scaleX = axisScale(angle), scaleY = axisScale(angle * 0.72f))
        EnvironmentRotation.RANDOM -> ParticleRotation(0f)
    }
}

private fun DrawScope.drawEnvironmentParticle(
    type: EnvironmentEffectType,
    particle: EnvironmentParticle,
    index: Int,
    position: Offset,
    px: Float,
    particleColor: Color,
    secondaryColor: Color,
    alpha: Float,
    elapsedSeconds: Float
) {
    when (type) {
        EnvironmentEffectType.RAIN -> {
            drawLine(particleColor, position - Offset(0f, px * 3.4f), position, strokeWidth = (1.2f + particle.size) * density)
        }
        EnvironmentEffectType.SNOW -> {
            drawCircle(particleColor, px * 0.46f, position)
            drawLine(particleColor.copy(alpha = alpha * 0.75f), position - Offset(px, 0f), position + Offset(px, 0f), strokeWidth = 0.7f * density)
            drawLine(particleColor.copy(alpha = alpha * 0.75f), position - Offset(0f, px), position + Offset(0f, px), strokeWidth = 0.7f * density)
        }
        EnvironmentEffectType.BUBBLES -> {
            drawCircle(particleColor, px, position, style = Stroke(width = 1.5f * density))
            drawCircle(Color.White.copy(alpha = alpha * 0.5f), px * 0.14f, position - Offset(px * 0.35f, px * 0.35f))
        }
        EnvironmentEffectType.GREEN_LEAVES -> {
            // Two pointed tips with curved sides and a visible main vein.
            val path = Path().apply {
                moveTo(position.x - px, position.y + px * 0.35f)
                quadraticBezierTo(position.x - px * 0.42f, position.y - px * 1.08f,
                    position.x + px, position.y - px * 0.45f)
                quadraticBezierTo(position.x + px * 0.44f, position.y + px * 1.03f,
                    position.x - px, position.y + px * 0.35f)
                close()
            }
            drawPath(path, particleColor)
            drawLine(secondaryColor.copy(alpha = alpha * 0.65f),
                position + Offset(-px * 0.85f, px * 0.27f),
                position + Offset(px * 0.80f, -px * 0.36f),
                strokeWidth = 0.9f * density)
        }
        EnvironmentEffectType.MAPLE_LEAVES -> {
            // Three major lobes form the familiar maple silhouette: a long
            // central blade and two broad, pointed side half-leaves. The two
            // smaller lower lobes and short stalk keep it readable at particle
            // size. Original geometric path, not a borrowed icon asset.
            val p = Path().apply {
                fun vertex(x: Float, y: Float) =
                    lineTo(position.x + x * px, position.y + y * px)
                moveTo(position.x, position.y - 1.24f * px)
                // Central blade, right edge with one small secondary point.
                vertex(0.17f, -0.87f)
                vertex(0.30f, -0.97f)
                vertex(0.32f, -0.66f)
                vertex(0.48f, -0.70f)
                vertex(0.42f, -0.46f)
                // Large right half-leaf, clearly distinct from the centre.
                vertex(0.67f, -0.60f)
                vertex(0.74f, -0.42f)
                vertex(1.13f, -0.43f)
                vertex(0.92f, -0.19f)
                vertex(1.01f, -0.03f)
                vertex(0.68f, 0.00f)
                // Short basal lobe and deep notch at the stalk.
                vertex(0.81f, 0.30f)
                vertex(0.45f, 0.20f)
                vertex(0.33f, 0.45f)
                vertex(0.13f, 0.33f)
                vertex(0.06f, 0.63f)
                vertex(0.06f, 1.12f)
                vertex(-0.06f, 1.12f)
                vertex(-0.06f, 0.63f)
                vertex(-0.13f, 0.33f)
                vertex(-0.33f, 0.45f)
                vertex(-0.45f, 0.20f)
                vertex(-0.81f, 0.30f)
                vertex(-0.68f, 0.00f)
                vertex(-1.01f, -0.03f)
                vertex(-0.92f, -0.19f)
                vertex(-1.13f, -0.43f)
                vertex(-0.74f, -0.42f)
                vertex(-0.67f, -0.60f)
                vertex(-0.42f, -0.46f)
                vertex(-0.48f, -0.70f)
                vertex(-0.32f, -0.66f)
                vertex(-0.30f, -0.97f)
                vertex(-0.17f, -0.87f)
                close()
            }
            drawPath(p, particleColor)
            val vein = secondaryColor.copy(alpha = alpha * 0.63f)
            fun veinTo(x: Float, y: Float, strength: Float = 0.8f) {
                drawLine(
                    vein, position + Offset(0f, px * 0.45f),
                    position + Offset(px * x, px * y),
                    strokeWidth = strength * density
                )
            }
            veinTo(0f, -1.06f, 0.95f)
            veinTo(0.88f, -0.33f)
            veinTo(-0.88f, -0.33f)
            veinTo(0.62f, 0.18f, 0.55f)
            veinTo(-0.62f, 0.18f, 0.55f)
        }
        EnvironmentEffectType.CLOUDS -> {
            val cloud = particleColor.copy(alpha = alpha * (0.72f + particle.size * 0.12f))
            drawOval(cloud, position - Offset(px * 1.3f, px * 0.45f), Size(px * 2.6f, px * 0.9f))
            drawCircle(cloud, px * 0.62f, position - Offset(px * 0.42f, px * 0.36f))
            drawCircle(cloud, px * 0.78f, position + Offset(px * 0.28f, -px * 0.34f))
        }
        EnvironmentEffectType.TUMBLEWEED -> {
            drawCircle(particleColor, px, position, style = Stroke(width = 1.4f * density))
            repeat(5) { spoke ->
                val angle = spoke * (PI * 2f / 5f).toFloat()
                val endpoint = position + Offset(cos(angle.toDouble()).toFloat() * px, sin(angle.toDouble()).toFloat() * px)
                drawLine(particleColor, position, endpoint, strokeWidth = 0.9f * density)
            }
        }
        EnvironmentEffectType.FIRE -> {
            val flicker = (0.78f + 0.22f * sin((elapsedSeconds * 5.3f + particle.phase).toDouble()).toFloat())
            val tip = px * (1.18f + flicker * 0.56f)
            val outer = Path().apply {
                moveTo(position.x, position.y + px)
                cubicTo(position.x - px * 1.05f, position.y + px * 0.58f,
                    position.x - px * 0.70f, position.y - px * 0.36f,
                    position.x - px * 0.20f, position.y - px * 0.73f)
                quadraticBezierTo(position.x - px * 0.29f, position.y - tip * 0.10f,
                    position.x + px * 0.11f, position.y - tip)
                cubicTo(position.x + px * 0.33f, position.y - tip * 0.44f,
                    position.x + px * 1.02f, position.y - px * 0.06f,
                    position.x + px * 0.62f, position.y + px * 0.62f)
                quadraticBezierTo(position.x + px * 0.20f, position.y + px,
                    position.x, position.y + px)
                close()
            }
            drawPath(outer, particleColor.copy(alpha = alpha * flicker))
            val inner = Path().apply {
                moveTo(position.x, position.y + px * 0.80f)
                quadraticBezierTo(position.x - px * 0.42f, position.y + px * 0.15f,
                    position.x + px * 0.08f, position.y - px * 0.56f)
                quadraticBezierTo(position.x + px * 0.52f, position.y + px * 0.34f,
                    position.x, position.y + px * 0.80f)
                close()
            }
            drawPath(inner, secondaryColor.copy(alpha = alpha * 0.86f))
        }
        EnvironmentEffectType.GEOMETRIC -> {
            when (Math.floorMod(particle.variant, 3)) {
                0 -> drawCircle(particleColor, px, position, style = Stroke(width = 1.5f * density))
                1 -> drawRect(particleColor, topLeft = position - Offset(px, px), size = Size(px * 2f, px * 2f), style = Stroke(width = 1.5f * density))
                else -> {
                    val path = Path().apply {
                        moveTo(position.x, position.y - px)
                        lineTo(position.x + px, position.y + px)
                        lineTo(position.x - px, position.y + px)
                        close()
                    }
                    drawPath(path, particleColor, style = Stroke(width = 1.5f * density))
                }
            }
        }
        EnvironmentEffectType.SUN_RAYS,
        EnvironmentEffectType.FISH,
        EnvironmentEffectType.DISTORTION -> Unit
    }
}

private fun fraction(value: Float): Float = value - kotlin.math.floor(value)

private fun reflect(value: Float, extent: Float): Float {
    if (extent <= 0f) return 0f
    val period = extent * 2f
    val wrapped = wrap(value, period)
    return if (wrapped <= extent) wrapped else period - wrapped
}

private fun effectColorAt(
    colorA: Color,
    colorB: Color,
    type: EnvironmentGradientType,
    fillMode: EnvironmentFillMode,
    position: Offset,
    canvasSize: Size
): Color {
    if (fillMode != EnvironmentFillMode.GRADIENT) return colorA
    val width = canvasSize.width.coerceAtLeast(1f)
    val height = canvasSize.height.coerceAtLeast(1f)
    val fraction = when (type) {
        EnvironmentGradientType.HORIZONTAL -> position.x / width
        EnvironmentGradientType.VERTICAL -> position.y / height
        EnvironmentGradientType.DIAGONAL -> (position.x / width + position.y / height) / 2f
        EnvironmentGradientType.RADIAL -> {
            val dx = position.x - width / 2f
            val dy = position.y - height / 2f
            hypot(dx.toDouble(), dy.toDouble()).toFloat() / hypot((width / 2f).toDouble(), (height / 2f).toDouble()).toFloat().coerceAtLeast(1f)
        }
    }.coerceIn(0f, 1f)
    return lerp(colorA, colorB, fraction)
}

private fun movementVector(
    direction: EnvironmentDirection,
    phase: Float,
    elapsedSeconds: Float,
    index: Int,
    variant: Int
): Offset = when (direction) {
    EnvironmentDirection.UP -> Offset(0f, -1f)
    EnvironmentDirection.DOWN -> Offset(0f, 1f)
    EnvironmentDirection.LEFT -> Offset(-1f, 0f)
    EnvironmentDirection.RIGHT -> Offset(1f, 0f)
    EnvironmentDirection.UP_OR_DOWN -> if ((variant + index) % 2 == 0) Offset(0f, -1f) else Offset(0f, 1f)
    EnvironmentDirection.LEFT_OR_RIGHT -> if ((variant + index) % 2 == 0) Offset(-1f, 0f) else Offset(1f, 0f)
    EnvironmentDirection.CHAOTIC -> Offset(
        cos((phase + elapsedSeconds * 0.9f).toDouble()).toFloat(),
        sin((phase * 1.3f + elapsedSeconds * 1.1f).toDouble()).toFloat()
    ).normalized()
    EnvironmentDirection.ALL_DIRECTIONS -> {
        val angle = phase + index * 0.73f
        Offset(cos(angle.toDouble()).toFloat(), sin(angle.toDouble()).toFloat()).normalized()
    }
    EnvironmentDirection.ZOOM_IN_3D,
    EnvironmentDirection.ZOOM_OUT_3D,
    EnvironmentDirection.SPIRAL,
    EnvironmentDirection.BOUNCE -> Offset.Zero
}

private fun Offset.normalized(): Offset {
    val length = sqrt(x * x + y * y).coerceAtLeast(0.0001f)
    return Offset(x / length, y / length)
}

/** Coordinate that re-enters only after the complete particle moves off-screen. */
private fun wrapBeyondEdge(value: Float, extent: Float, margin: Float): Float {
    val safeExtent = extent.coerceAtLeast(1f)
    val overhang = margin.coerceIn(0f, safeExtent * 0.45f)
    val period = safeExtent + overhang * 2f
    return ((value + overhang) % period + period) % period - overhang
}

private fun wrap(value: Float, extent: Float): Float {
    if (extent <= 0f) return 0f
    val wrapped = value % extent
    return if (wrapped < 0f) wrapped + extent else wrapped
}

private fun environmentColor(hex: String): Color {
    val clean = hex.trim().removePrefix("#").takeLast(6)
    return runCatching { Color(0xFF000000L or clean.toLong(16)) }.getOrDefault(Color.White)
}

@Composable
internal fun EnvironmentEffectsSettingsPanel(
    config: EnvironmentEffectsConfig,
    onConfigChange: (EnvironmentEffectsConfig) -> Unit
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf<EnvironmentEffectType?>(null) }
    var imageTarget by remember { mutableStateOf<EnvironmentEffectType?>(null) }
    var imageSlotTarget by remember { mutableStateOf<Int?>(null) }
    var iconPackTarget by remember { mutableStateOf<EnvironmentEffectType?>(null) }
    val imagePicker = rememberLauncherForActivityResult(OpenDocument()) { uri ->
        val type = imageTarget
        val slot = imageSlotTarget
        imageTarget = null
        imageSlotTarget = null
        if (uri != null && type != null) {
            runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            val old = config.options(type)
            if (type == EnvironmentEffectType.FISH && slot != null) {
                val images = old.imageUris.take(10).toMutableList()
                if (slot < images.size) images[slot] = uri.toString()
                else if (images.size < 10) images.add(uri.toString())
                onConfigChange(config.update(type, old.copy(imageUris = images.distinct().take(10))))
            } else {
                onConfigChange(config.update(type, old.copy(
                    fillMode = EnvironmentFillMode.IMAGE,
                    imageUri = uri.toString(),
                    iconPackPackage = null,
                    iconPackDrawable = null
                )))
            }
        }
    }

    SettingsSectionTitle(l10n(R.string.ui_display_layer))
    EnvironmentEffectsLayerMode.values().forEach { mode ->
        SimpleRadioOption(mode.title, mode == config.layerMode) {
            onConfigChange(config.copy(layerMode = mode))
        }
    }
    Spacer(Modifier.height(8.dp))

    Text(
        l10n(R.string.ui_multiple_effects_can_be_enabled_at_once_animation_pauses),
        color = Color.White.copy(alpha = 0.62f)
    )
    EnvironmentEffectType.values()
        .filter { type -> type != EnvironmentEffectType.DISTORTION || DISTORTION_EFFECT_AVAILABLE }
        .forEach { type ->
        val options = config.options(type)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.055f))
        ) {
            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { expanded = if (expanded == type) null else type },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(42.dp).background(
                            if (type == EnvironmentEffectType.DISTORTION) {
                                Color.White.copy(alpha = 0.12f)
                            } else {
                                environmentColor(options.colorHex).copy(alpha = 0.24f)
                            },
                            CircleShape
                        ),
                        contentAlignment = Alignment.Center
                    ) { Text(type.symbol) }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(type.title, fontWeight = FontWeight.Bold)
                        Text(
                            when (type) {
                                EnvironmentEffectType.SUN_RAYS -> l10n(
                                    R.string.ui_sun_rays_summary,
                                    options.speedPercent,
                                    options.sunRaysPerSource.coerceIn(4, 80)
                                )
                                EnvironmentEffectType.FISH -> l10n(
                                    R.string.ui_fish_summary,
                                    options.speedPercent,
                                    options.count,
                                    options.imageUris.size
                                )
                                EnvironmentEffectType.DISTORTION -> l10n(
                                    R.string.ui_distortion_summary,
                                    options.distortionType.title,
                                    options.direction.title,
                                    options.count.coerceIn(1, 5)
                                )
                                else -> l10n(
                                    R.string.ui_speed_items,
                                    options.direction.title,
                                    options.rotation.title,
                                    options.speedPercent,
                                    options.count
                                )
                            },
                            color = Color.White.copy(alpha = 0.56f)
                        )
                    }
                    Switch(
                        checked = options.enabled,
                        onCheckedChange = { onConfigChange(config.update(type, options.copy(enabled = it))) }
                    )
                }
                if (expanded == type) {
                    val fixedMotion = type == EnvironmentEffectType.SUN_RAYS ||
                        type == EnvironmentEffectType.FISH
                    if (!fixedMotion) {
                        Text(l10n(R.string.ui_direction), fontWeight = FontWeight.Bold)
                        EnvironmentDirection.values().forEach { direction ->
                            SimpleRadioOption(direction.title, direction == options.direction) {
                                onConfigChange(config.update(type, options.copy(direction = direction)))
                            }
                        }
                        if (type != EnvironmentEffectType.DISTORTION) {
                            Text(l10n(R.string.ui_rotation), fontWeight = FontWeight.Bold)
                            EnvironmentRotation.values().forEach { rotation ->
                                SimpleRadioOption(rotation.title, rotation == options.rotation) {
                                    onConfigChange(config.update(type, options.copy(rotation = rotation)))
                                }
                            }
                        }
                    }
                    IntegerSliderSetting(
                        if (type == EnvironmentEffectType.FISH) {
                            l10n(R.string.ui_maximum_speed)
                        } else l10n(R.string.ui_speed),
                        options.speedPercent,
                        0,
                        300
                    ) {
                        onConfigChange(config.update(type, options.copy(speedPercent = it)))
                    }
                    if (type == EnvironmentEffectType.SUN_RAYS) {
                        IntegerSliderSetting(
                            l10n(R.string.ui_sun_ray_height),
                            options.sunRayHeightPercent,
                            10,
                            100
                        ) {
                            onConfigChange(config.update(type, options.copy(sunRayHeightPercent = it)))
                        }
                        IntegerSliderSetting(
                            l10n(R.string.ui_sun_ray_count),
                            options.sunRaysPerSource.coerceIn(1, 20),
                            1,
                            20
                        ) {
                            onConfigChange(config.update(type, options.copy(sunRaysPerSource = it)))
                        }
                        IntegerSliderSetting(
                            l10n(R.string.ui_sun_ray_thickness),
                            options.sunRayThicknessPx.coerceIn(2, 160),
                            2,
                            160
                        ) {
                            onConfigChange(config.update(type, options.copy(sunRayThicknessPx = it)))
                        }
                        IntegerSliderSetting(
                            l10n(R.string.ui_sun_ray_max_angle),
                            options.sunRayMaxAngleDegrees.coerceIn(0, 75),
                            0,
                            75
                        ) {
                            onConfigChange(config.update(type, options.copy(sunRayMaxAngleDegrees = it)))
                        }
                    } else if (type == EnvironmentEffectType.DISTORTION) {
                        IntegerSliderSetting(
                            l10n(R.string.ui_item_count),
                            options.count.coerceIn(1, 5),
                            1,
                            5
                        ) {
                            onConfigChange(config.update(type, options.copy(count = it)))
                        }
                        Text(l10n(R.string.ui_distortion_type), fontWeight = FontWeight.Bold)
                        EnvironmentDistortionType.values().forEach { distortion ->
                            SimpleRadioOption(
                                title = distortion.title,
                                selected = distortion == options.distortionType,
                                enabled = distortion.available
                            ) {
                                onConfigChange(config.update(type, options.copy(distortionType = distortion)))
                            }
                        }
                        Text(l10n(R.string.ui_distortion_shape), fontWeight = FontWeight.Bold)
                        FolderShape.values().forEach { shape ->
                            SimpleRadioOption(shape.title, shape == options.distortionShape) {
                                onConfigChange(config.update(type, options.copy(distortionShape = shape)))
                            }
                        }
                        IntegerSliderSetting(
                            l10n(R.string.ui_distortion_size),
                            options.distortionSizePercent,
                            8,
                            60
                        ) {
                            onConfigChange(config.update(type, options.copy(distortionSizePercent = it)))
                        }
                        IntegerSliderSetting(
                            l10n(R.string.ui_distortion_strength),
                            options.distortionStrengthPercent,
                            10,
                            250
                        ) {
                            onConfigChange(config.update(type, options.copy(distortionStrengthPercent = it)))
                        }
                    } else {
                        IntegerSliderSetting(
                            l10n(R.string.ui_item_count),
                            options.count,
                            1,
                            220
                        ) {
                            onConfigChange(config.update(type, options.copy(count = it)))
                        }
                    }
                    if (type != EnvironmentEffectType.DISTORTION) {
                    Text(l10n(R.string.ui_item_appearance), fontWeight = FontWeight.Bold)
                    if (type == EnvironmentEffectType.FISH) {
                        Text(
                            l10n(R.string.ui_fish_picture_slots_hint),
                            color = Color.White.copy(alpha = 0.62f)
                        )
                        repeat(10) { slot ->
                            val selectedImage = options.imageUris.getOrNull(slot)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        imageTarget = type
                                        imageSlotTarget = slot
                                        imagePicker.launch(arrayOf("image/*"))
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        if (selectedImage == null) {
                                            l10n(R.string.ui_choose_fish_picture, slot + 1)
                                        } else l10n(R.string.ui_replace_fish_picture, slot + 1)
                                    )
                                }
                                if (selectedImage != null) {
                                    OutlinedButton(
                                        onClick = {
                                            val images = options.imageUris.toMutableList()
                                            images.removeAt(slot)
                                            onConfigChange(config.update(type, options.copy(imageUris = images)))
                                        },
                                        modifier = Modifier.width(52.dp)
                                    ) { Text("×") }
                                }
                            }
                        }
                        VisualColorSetting(l10n(R.string.ui_fallback_primary_colour), options.colorHex) {
                            onConfigChange(config.update(type, options.copy(colorHex = it)))
                        }
                        VisualColorSetting(l10n(R.string.ui_fallback_secondary_colour), options.secondaryColorHex) {
                            onConfigChange(config.update(type, options.copy(secondaryColorHex = it)))
                        }
                    } else if (type == EnvironmentEffectType.SUN_RAYS) {
                        VisualColorSetting(l10n(R.string.ui_primary_colour_0275), options.colorHex) {
                            onConfigChange(config.update(type, options.copy(colorHex = it)))
                        }
                        VisualColorSetting(l10n(R.string.ui_secondary_colour), options.secondaryColorHex) {
                            onConfigChange(config.update(type, options.copy(secondaryColorHex = it)))
                        }
                    } else {
                        EnvironmentFillMode.values().forEach { mode ->
                            SimpleRadioOption(mode.title, mode == options.fillMode) {
                                onConfigChange(config.update(type, options.copy(fillMode = mode)))
                            }
                        }
                        when (options.fillMode) {
                            EnvironmentFillMode.COLOR -> {
                                VisualColorSetting(l10n(R.string.ui_colour), options.colorHex) {
                                    onConfigChange(config.update(type, options.copy(colorHex = it)))
                                }
                            }
                            EnvironmentFillMode.GRADIENT -> {
                                VisualColorSetting(l10n(R.string.ui_primary_colour_0275), options.colorHex) {
                                    onConfigChange(config.update(type, options.copy(colorHex = it)))
                                }
                                VisualColorSetting(l10n(R.string.ui_secondary_colour), options.secondaryColorHex) {
                                    onConfigChange(config.update(type, options.copy(secondaryColorHex = it)))
                                }
                                Text(l10n(R.string.ui_gradient_type), fontWeight = FontWeight.Bold)
                                EnvironmentGradientType.values().forEach { gradient ->
                                    SimpleRadioOption(gradient.title, gradient == options.gradientType) {
                                        onConfigChange(config.update(type, options.copy(gradientType = gradient)))
                                    }
                                }
                            }
                            EnvironmentFillMode.IMAGE -> {
                                OutlinedButton(onClick = {
                                    imageTarget = type
                                    imagePicker.launch(arrayOf("image/*"))
                                }, modifier = Modifier.fillMaxWidth()) {
                                    Text(if (options.imageUri == null) l10n(R.string.ui_choose_picture) else l10n(R.string.ui_replace_picture))
                                }
                            }
                            EnvironmentFillMode.ICON_PACK -> {
                                OutlinedButton(onClick = { iconPackTarget = type }, modifier = Modifier.fillMaxWidth()) {
                                    Text(if (options.iconPackDrawable == null) l10n(R.string.ui_choose_icon_from_icon_pack) else l10n(R.string.ui_replace_icon))
                                }
                            }
                        }
                    }
                    IntegerSliderSetting(l10n(R.string.ui_opacity), options.opacityPercent, 0, 100) {
                        onConfigChange(config.update(type, options.copy(opacityPercent = it)))
                    }
                    IntegerSliderSetting(l10n(R.string.ui_blur_dp), options.blurDp, 0, 18) {
                        onConfigChange(config.update(type, options.copy(blurDp = it)))
                    }
                    }
                }
            }
        }
    }

    iconPackTarget?.let { type ->
        IconPackAssetPickerDialog(
            title = l10n(R.string.ui_icon_for_effect, type.title),
            onDismiss = { iconPackTarget = null },
            onSelect = { packPackage, drawableName ->
                val old = config.options(type)
                onConfigChange(config.update(type, old.copy(
                    fillMode = EnvironmentFillMode.ICON_PACK,
                    imageUri = null,
                    iconPackPackage = packPackage,
                    iconPackDrawable = drawableName
                )))
                iconPackTarget = null
            }
        )
    }
}
