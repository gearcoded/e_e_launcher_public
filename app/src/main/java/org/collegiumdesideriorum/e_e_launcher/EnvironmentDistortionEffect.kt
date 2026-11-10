package org.collegiumdesideriorum.e_e_launcher

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RuntimeShader
import android.graphics.Shader
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * Draws the normal launcher independently and adds distortion as a second
 * recorded layer. Magnifier uses Compose drawing directly. The other modes feed
 * a recorded bitmap to AGSL explicitly instead of relying on
 * RenderEffect.createRuntimeShaderEffect(). Some Tecno renderers accepted that
 * API but supplied a transparent child shader, which made every non-magnifier
 * mode invisible and could leave a thin artifact under the status bar.
 */
@Composable
internal fun EnvironmentDistortionSurface(
    config: EnvironmentEffectsConfig,
    modifier: Modifier = Modifier,
    onShaderUnavailable: () -> Unit = {},
    content: @Composable BoxScope.() -> Unit
) {
    val options = config.options(EnvironmentEffectType.DISTORTION)
    if (!DISTORTION_EFFECT_AVAILABLE || !options.enabled) {
        Box(modifier = modifier, content = content)
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

    var elapsedSeconds by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(running, options.speedPercent) {
        if (!running || options.speedPercent <= 0) return@LaunchedEffect
        var previousNanos = 0L
        while (isActive) {
            withFrameNanos { now ->
                if (previousNanos != 0L) {
                    val deltaSeconds = ((now - previousNanos) / 1_000_000_000f)
                        .coerceIn(0f, 0.05f)
                    elapsedSeconds += deltaSeconds
                }
                previousNanos = now
            }
        }
    }

    val capturedContent = rememberGraphicsLayer()
    val rendererFailed = remember(options.distortionType) { AtomicBoolean(false) }
    val firstShaderFrameLogged = remember(options.distortionType) { AtomicBoolean(false) }
    val coroutineScope = rememberCoroutineScope()
    val needsShader = options.distortionType != EnvironmentDistortionType.MAGNIFIER
    var capturedBitmap by remember(needsShader) { mutableStateOf<Bitmap?>(null) }
    val shader = remember(needsShader) {
        if (needsShader) runCatching { RuntimeShader(DISTORTION_SHADER) }.getOrNull()
        else null
    }
    val shaderPaint = remember(shader) {
        shader?.let { runtimeShader -> Paint(Paint.ANTI_ALIAS_FLAG).apply { this.shader = runtimeShader } }
    }

    LaunchedEffect(options.distortionType, needsShader) {
        LauncherDiagnostics.info(
            "distortion selected type=${options.distortionType.name} " +
                "pipeline=${if (needsShader) "bitmap_shader" else "compose_magnifier"}"
        )
    }

    if (needsShader && (shader == null || shaderPaint == null)) {
        LaunchedEffect(options.distortionType) {
            LauncherDiagnostics.error("distortion shader construction failed type=${options.distortionType.name}")
            onShaderUnavailable()
        }
    }

    // A bitmap input avoids the OEM RenderEffect child-shader path. The shader
    // animates independently, so the desktop texture only needs a modest refresh
    // rate for widget and environment-effect changes.
    LaunchedEffect(running, needsShader, capturedContent) {
        if (!running || !needsShader || shader == null) return@LaunchedEffect
        var consecutiveFailures = 0
        while (isActive && !rendererFailed.get()) {
            withFrameNanos { }
            runCatching { capturedContent.toImageBitmap().asAndroidBitmap() }
                .onSuccess { bitmap ->
                    if (bitmap.width > 0 && bitmap.height > 0) {
                        capturedBitmap = bitmap
                        consecutiveFailures = 0
                    }
                }
                .onFailure { error ->
                    consecutiveFailures += 1
                    if (consecutiveFailures >= 3 && rendererFailed.compareAndSet(false, true)) {
                        LauncherDiagnostics.error(
                            "distortion capture failed type=${options.distortionType.name}",
                            error
                        )
                        onShaderUnavailable()
                    }
                }
            delay(200L)
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                // Always draw the ordinary launcher. Recording is a separate
                // operation used only by the overlay and cannot clear this draw.
                drawContent()

                if (rendererFailed.get() || size.width <= 0f || size.height <= 0f) {
                    return@drawWithContent
                }

                val contentRecordResult = runCatching {
                    capturedContent.record {
                        this@drawWithContent.drawContent()
                    }
                }

                if (contentRecordResult.isFailure && rendererFailed.compareAndSet(false, true)) {
                    LauncherDiagnostics.error(
                        "distortion layer record failed type=${options.distortionType.name}",
                        contentRecordResult.exceptionOrNull()
                    )
                    coroutineScope.launch { onShaderUnavailable() }
                }
            },
            content = content
        )

        if (!rendererFailed.get() && !needsShader) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                    if (size.width <= 0f || size.height <= 0f) return@drawWithContent

                    val overlayResult = runCatching {
                        val radius = min(size.width, size.height) *
                            options.distortionSizePercent.coerceIn(8, 60) / 200f
                        val magnification = (1f +
                            options.distortionStrengthPercent.coerceIn(10, 250) / 100f * 1.20f)
                            .coerceIn(1.12f, 4.0f)
                        val direction = options.direction
                        val speed = options.speedPercent.coerceIn(0, 300) / 100f
                        val time = elapsedSeconds
                        val count = options.count.coerceIn(1, 5)
                        val shape = folderShape(options.distortionShape)

                        repeat(count) { index ->
                            val centre = distortionCentre(
                                index = index,
                                direction = direction,
                                surface = size,
                                radius = radius,
                                elapsedSeconds = time,
                                speed = speed
                            )
                            val diameter = radius * 2f
                            val outline = shape.createOutline(
                                Size(diameter, diameter),
                                layoutDirection,
                                this
                            )
                            val maskPath = outline.toAbsolutePath(
                                Offset(centre.x - radius, centre.y - radius)
                            )

                            clipPath(maskPath) {
                                scale(magnification, magnification, centre) {
                                    drawLayer(capturedContent)
                                }
                            }
                        }
                    }

                    if (overlayResult.isFailure && rendererFailed.compareAndSet(false, true)) {
                        LauncherDiagnostics.error(
                            "distortion magnifier draw failed",
                            overlayResult.exceptionOrNull()
                        )
                        coroutineScope.launch { onShaderUnavailable() }
                    }
                }
            )
        } else if (!rendererFailed.get() && shader != null && shaderPaint != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                    if (size.width <= 0f || size.height <= 0f) return@drawWithContent
                    val bitmap = capturedBitmap ?: return@drawWithContent

                    val overlayDrawn = runCatching {
                        val inputShader = BitmapShader(
                            bitmap,
                            Shader.TileMode.CLAMP,
                            Shader.TileMode.CLAMP
                        ).apply {
                            setLocalMatrix(Matrix().apply {
                                setScale(
                                    size.width / bitmap.width.toFloat(),
                                    size.height / bitmap.height.toFloat()
                                )
                            })
                        }
                        shader.setInputShader("content", inputShader)
                        shader.setFloatUniform("resolution", size.width, size.height)
                        shader.setFloatUniform("time", elapsedSeconds)
                        shader.setFloatUniform(
                            "elementCount",
                            options.count.coerceIn(1, 5).toFloat()
                        )
                        shader.setFloatUniform(
                            "directionType",
                            options.direction.ordinal.toFloat()
                        )
                        shader.setFloatUniform(
                            "distortionType",
                            options.distortionType.ordinal.toFloat()
                        )
                        shader.setFloatUniform(
                            "shapeType",
                            options.distortionShape.ordinal.toFloat()
                        )
                        shader.setFloatUniform(
                            "speed",
                            options.speedPercent.coerceIn(0, 300) / 100f
                        )
                        shader.setFloatUniform(
                            "sizeFraction",
                            options.distortionSizePercent.coerceIn(8, 60) / 100f
                        )
                        shader.setFloatUniform(
                            "strength",
                            options.distortionStrengthPercent.coerceIn(10, 250) / 100f
                        )
                        drawIntoCanvas { canvas ->
                            canvas.nativeCanvas.drawRect(
                                0f,
                                0f,
                                size.width,
                                size.height,
                                shaderPaint
                            )
                        }
                    }.onSuccess {
                        if (firstShaderFrameLogged.compareAndSet(false, true)) {
                            LauncherDiagnostics.info(
                                "distortion first frame type=${options.distortionType.name} " +
                                    "surface=${size.width.toInt()}x${size.height.toInt()} " +
                                    "bitmap=${bitmap.width}x${bitmap.height}"
                            )
                        }
                    }.onFailure { error ->
                        LauncherDiagnostics.error(
                            "distortion draw failed type=${options.distortionType.name}",
                            error
                        )
                    }.isSuccess

                    if (!overlayDrawn && rendererFailed.compareAndSet(false, true)) {
                        coroutineScope.launch { onShaderUnavailable() }
                    }
                }
            )
        }
    }
}

private fun Outline.toAbsolutePath(offset: Offset): Path = Path().also { target ->
    when (this) {
        is Outline.Rectangle -> target.addRect(rect)
        is Outline.Rounded -> target.addRoundRect(roundRect)
        is Outline.Generic -> target.addPath(path)
    }
    target.translate(offset)
}

private fun distortionCentre(
    index: Int,
    direction: EnvironmentDirection,
    surface: Size,
    radius: Float,
    elapsedSeconds: Float,
    speed: Float
): Offset {
    val item = index.toFloat()
    val seedX = fract(0.173f + item * 0.371f)
    val seedY = fract(0.619f + item * 0.227f)
    val localSpeed = speed * (0.035f + item * 0.0045f)
    val usableX = max(surface.width - radius * 2f, 1f)
    val usableY = max(surface.height - radius * 2f, 1f)

    return when (direction) {
        EnvironmentDirection.BOUNCE -> Offset(
            radius + triangularWave(seedX + elapsedSeconds * localSpeed) * usableX,
            radius + triangularWave(
                seedY + elapsedSeconds * localSpeed * (0.73f + item * 0.09f)
            ) * usableY
        )

        EnvironmentDirection.SPIRAL -> {
            val angle = item * 1.37f + elapsedSeconds * speed * 0.32f
            val orbit = (0.13f + 0.31f * seedX) * min(surface.width, surface.height)
            Offset(
                surface.width * 0.5f + cos(angle) * orbit,
                surface.height * 0.5f + sin(angle) * orbit
            )
        }

        EnvironmentDirection.ZOOM_OUT_3D,
        EnvironmentDirection.ZOOM_IN_3D -> {
            val wave = triangularWave(seedX + elapsedSeconds * localSpeed * 0.55f)
            val phase = if (direction == EnvironmentDirection.ZOOM_IN_3D) wave else 1f - wave
            Offset(
                surface.width * 0.5f + (seedX * 2f - 1f) * usableX * 0.48f * phase,
                surface.height * 0.5f + (seedY * 2f - 1f) * usableY * 0.48f * phase
            )
        }

        EnvironmentDirection.ALL_DIRECTIONS -> {
            val angle = item * 2.39996323f
            Offset(
                fract(seedX + cos(angle) * elapsedSeconds * localSpeed) * surface.width,
                fract(seedY + sin(angle) * elapsedSeconds * localSpeed) * surface.height
            )
        }

        EnvironmentDirection.CHAOTIC -> Offset(
            radius + (0.5f + 0.5f * sin(elapsedSeconds * localSpeed * 5.1f + item * 1.7f)) * usableX,
            radius + (0.5f + 0.5f * sin(elapsedSeconds * localSpeed * 3.9f + item * 2.3f)) * usableY
        )

        else -> {
            val alternate = if (fract(index * 0.5f) < 0.25f) -1f else 1f
            val vector = when (direction) {
                EnvironmentDirection.UP -> Offset(0f, -1f)
                EnvironmentDirection.DOWN -> Offset(0f, 1f)
                EnvironmentDirection.LEFT -> Offset(-1f, 0f)
                EnvironmentDirection.RIGHT -> Offset(1f, 0f)
                EnvironmentDirection.UP_OR_DOWN -> Offset(0f, alternate)
                EnvironmentDirection.LEFT_OR_RIGHT -> Offset(alternate, 0f)
            }
            Offset(
                fract(seedX + vector.x * elapsedSeconds * localSpeed) * surface.width,
                fract(seedY + vector.y * elapsedSeconds * localSpeed) * surface.height
            )
        }
    }
}

private fun triangularWave(value: Float): Float = kotlin.math.abs(fract(value) * 2f - 1f)

private fun fract(value: Float): Float = value - floor(value)

@Suppress("unused")
private const val DISTORTION_SHADER = """
    uniform shader content;
    uniform float2 resolution;
    uniform float time;
    uniform float elementCount;
    uniform float directionType;
    uniform float distortionType;
    uniform float shapeType;
    uniform float speed;
    uniform float sizeFraction;
    uniform float strength;

    float triangularWave(float value) {
        return abs(fract(value) * 2.0 - 1.0);
    }

    float2 rotatePoint(float2 point, float angle) {
        float cosine = cos(angle);
        float sine = sin(angle);
        return float2(
            point.x * cosine - point.y * sine,
            point.x * sine + point.y * cosine
        );
    }

    float regularPolygonDistance(float2 point, float sides, float rotation) {
        float angle = atan(point.y, point.x) + rotation;
        float sector = 6.28318530718 / sides;
        return cos(floor(0.5 + angle / sector) * sector - angle) * length(point) - 0.82;
    }

    float roundedBoxDistance(float2 point, float2 bounds, float radius) {
        float2 q = abs(point) - bounds + radius;
        return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - radius;
    }

    float shapeDistance(float2 point, float shape) {
        float radial = length(point);
        float angle = atan(point.y, point.x);

        if (shape < 0.5) return roundedBoxDistance(point, float2(0.91, 0.91), 0.25); // Default
        if (shape < 1.5) return max(abs(point.x), abs(point.y)) - 0.94; // No background / rectangle
        if (shape < 2.5) return roundedBoxDistance(point, float2(0.92, 0.92), 0.36); // Rounded rectangle
        if (shape < 3.5) return roundedBoxDistance(point, float2(0.94, 0.94), 0.04); // Rectangle
        if (shape < 4.5) return radial - 0.94; // Circle
        if (shape < 5.5) return roundedBoxDistance(point, float2(0.94, 0.62), 0.60); // Capsule
        if (shape < 6.5) return regularPolygonDistance(point, 6.0, 0.5235987756); // Hexagon
        if (shape < 7.5) return regularPolygonDistance(point, 8.0, 0.3926990817); // Octagon
        if (shape < 8.5) return abs(point.x) + abs(point.y) - 1.10; // Diamond
        if (shape < 9.5) return regularPolygonDistance(point, 3.0, 1.5707963268); // Triangle
        if (shape < 10.5) { // Trapezoid
            float horizontal = abs(point.x) - (0.78 + point.y * 0.18);
            return max(horizontal, abs(point.y) - 0.88);
        }
        if (shape < 11.5) { // Parallelogram
            return max(abs(point.x + point.y * 0.24) - 0.82, abs(point.y) - 0.88);
        }
        if (shape < 12.5) return radial - (0.52 + 0.36 * max(cos(angle * 5.0), -0.35)); // Star
        if (shape < 13.5) return radial - (0.64 + 0.25 * max(cos(angle * 5.0), -0.25)); // Rounded star
        if (shape < 14.5) { // Shield
            float width = 0.88 - max(point.y, 0.0) * 0.36;
            return max(abs(point.x) - width, max(-point.y - 0.82, point.y - 0.96));
        }
        if (shape < 15.5) { // Drop
            float2 p = point - float2(0.0, 0.15);
            return length(p * float2(1.0, 0.82)) - (0.74 - p.y * 0.24);
        }
        if (shape < 16.5) { // Heart
            float2 p = float2(point.x, -point.y + 0.10);
            float x2 = p.x * p.x;
            float y2 = p.y * p.y;
            return pow(x2 + y2 - 0.48, 3.0) - x2 * p.y * p.y * p.y;
        }
        if (shape < 17.5) { // Cloud
            float distance = length(point - float2(-0.45, 0.18)) - 0.46;
            distance = min(distance, length(point - float2(0.00, -0.12)) - 0.62);
            distance = min(distance, length(point - float2(0.50, 0.18)) - 0.43);
            return min(distance, roundedBoxDistance(point - float2(0.0, 0.32), float2(0.78, 0.30), 0.25));
        }
        if (shape < 18.5) return radial - (0.68 + 0.19 * cos(angle * 8.0)); // Flower
        if (shape < 19.5) { // Leaf
            float2 p = rotatePoint(point, -0.72);
            return max(length(p - float2(0.30, 0.0)) - 0.83, length(p + float2(0.30, 0.0)) - 0.83);
        }
        if (shape < 20.5) { // Crescent
            float outer = radial - 0.92;
            float cutout = 0.70 - length(point - float2(0.38, -0.22));
            return max(outer, cutout);
        }
        if (shape < 21.5) { // Cross
            float vertical = max(abs(point.x) - 0.34, abs(point.y) - 0.94);
            float horizontal = max(abs(point.x) - 0.94, abs(point.y) - 0.34);
            return min(vertical, horizontal);
        }
        if (shape < 22.5) { // Ticket
            float outer = roundedBoxDistance(point, float2(0.94, 0.78), 0.12);
            float notches = min(length(point - float2(-0.94, 0.0)), length(point - float2(0.94, 0.0)));
            return max(outer, 0.18 - notches);
        }
        if (shape < 23.5) { // Tag
            float horizontal = max(abs(point.y) - 0.68, -point.x - 0.92);
            float angledEnd = point.x + abs(point.y) * 0.55 - 1.03;
            return max(horizontal, angledEnd);
        }
        if (shape < 24.5) { // Squircle
            return pow(abs(point.x), 4.2) + pow(abs(point.y), 4.2) - 0.78;
        }
        if (shape < 25.5) { // Superellipse
            return pow(abs(point.x), 2.8) + pow(abs(point.y), 2.8) - 0.82;
        }
        // Folder with tab
        float body = roundedBoxDistance(point - float2(0.0, 0.14), float2(0.94, 0.72), 0.10);
        float tab = roundedBoxDistance(point - float2(0.28, -0.65), float2(0.35, 0.22), 0.08);
        return min(body, tab);
    }

    float2 movingCentre(int index, float radius) {
        float item = float(index);
        float2 seed = fract(float2(0.173 + item * 0.371, 0.619 + item * 0.227));
        float localSpeed = speed * (0.035 + item * 0.0045);
        float2 usable = max(resolution - float2(radius * 2.0), float2(1.0));

        if (directionType > 10.5) { // Bounce off screen edges (default)
            float2 bounce = float2(
                triangularWave(seed.x + time * localSpeed),
                triangularWave(seed.y + time * localSpeed * (0.73 + item * 0.09))
            );
            return float2(radius) + bounce * usable;
        }

        if (directionType > 9.5) { // Spiral
            float angle = item * 1.37 + time * speed * 0.32;
            float orbit = (0.13 + 0.31 * seed.x) * min(resolution.x, resolution.y);
            return resolution * 0.5 + float2(cos(angle), sin(angle)) * orbit;
        }

        if (directionType > 8.5) { // 3D zoom out
            float phase = 1.0 - triangularWave(seed.x + time * localSpeed * 0.55);
            return resolution * 0.5 + (seed * 2.0 - 1.0) * usable * 0.48 * phase;
        }

        if (directionType > 7.5) { // 3D zoom in
            float phase = triangularWave(seed.x + time * localSpeed * 0.55);
            return resolution * 0.5 + (seed * 2.0 - 1.0) * usable * 0.48 * phase;
        }

        if (directionType > 6.5) { // All directions
            float angle = item * 2.39996323;
            float2 vector = float2(cos(angle), sin(angle));
            return fract(seed + vector * time * localSpeed) * resolution;
        }

        if (directionType > 5.5) { // Chaotic
            return float2(radius) + float2(
                0.5 + 0.5 * sin(time * localSpeed * 5.1 + item * 1.7),
                0.5 + 0.5 * sin(time * localSpeed * 3.9 + item * 2.3)
            ) * usable;
        }

        float2 vector = float2(0.0);
        if (directionType < 0.5) vector = float2(0.0, -1.0); // Up
        else if (directionType < 1.5) vector = float2(0.0, 1.0); // Down
        else if (directionType < 2.5) vector = float2(-1.0, 0.0); // Left
        else if (directionType < 3.5) vector = float2(1.0, 0.0); // Right
        float alternate = fract(float(index) * 0.5) < 0.25 ? -1.0 : 1.0;
        if (directionType < 4.5) vector = float2(0.0, alternate); // Up or down
        else vector = float2(alternate, 0.0); // Left or right
        return fract(seed + vector * time * localSpeed) * resolution;
    }

    float2 distortedCoordinate(float2 coordinate, float2 centre, float radius, float localTime) {
        float2 local = coordinate - centre;
        float normalizedRadius = length(local) / max(radius, 1.0);
        float influence = clamp(1.0 - normalizedRadius, 0.0, 1.0);

        if (distortionType < 0.5) { // Magnifier
            return centre + local * mix(1.0, 0.42, influence * strength);
        }
        if (distortionType < 1.5) { // Fish eye
            float scale = mix(1.0, 0.30 + 0.70 * normalizedRadius * normalizedRadius, influence * strength);
            return centre + local * scale;
        }
        if (distortionType < 2.5) { // Minify
            return centre + local * (1.0 + influence * strength * 0.88);
        }
        if (distortionType < 3.5) { // Wobble
            float2 wave = float2(
                sin(local.y * 0.055 + localTime * 2.4),
                cos(local.x * 0.049 - localTime * 2.0)
            );
            return coordinate + wave * radius * 0.10 * influence * strength;
        }
        if (distortionType < 4.5) { // Water
            float ripple = sin(normalizedRadius * 34.0 - localTime * 4.2);
            float2 radialDirection = local / max(length(local), 1.0);
            return coordinate - radialDirection * ripple * radius * 0.075 * influence * strength;
        }
        if (distortionType < 5.5) { // Black hole
            float2 spiral = rotatePoint(local, influence * strength * 3.8);
            return centre + spiral * mix(1.0, 0.24, influence * min(strength, 1.5));
        }
        if (distortionType < 6.5) { // Spiral distortion
            return centre + rotatePoint(local, influence * influence * strength * 6.2);
        }
        // Radial inside-out distortion
        return centre - local * mix(0.35, 1.0, normalizedRadius);
    }

    half4 main(float2 fragmentCoordinate) {
        float radius = min(resolution.x, resolution.y) * sizeFraction * 0.5;
        float edgeSoftness = max(1.5 / max(radius, 1.0), 0.008);
        float2 sampleCoordinate = fragmentCoordinate;
        float darkness = 1.0;
        float coverage = 0.0;

        for (int index = 0; index < 5; index++) {
            if (float(index) >= elementCount) break;
            float2 centre = movingCentre(index, radius);
            float2 normalizedPoint = (fragmentCoordinate - centre) / max(radius, 1.0);
            float mask = 1.0 - smoothstep(-edgeSoftness, edgeSoftness, shapeDistance(normalizedPoint, shapeType));
            coverage = max(coverage, mask);
            float2 candidate = distortedCoordinate(sampleCoordinate, centre, radius, time + float(index) * 1.31);
            sampleCoordinate = mix(sampleCoordinate, candidate, mask);

            if (distortionType > 4.5 && distortionType < 5.5) {
                float radial = clamp(length(normalizedPoint), 0.0, 1.0);
                darkness *= mix(1.0, 0.20 + radial * 0.80, mask * min(strength, 1.0));
            }
        }

        sampleCoordinate = clamp(sampleCoordinate, float2(0.0), resolution - float2(1.0));
        half4 colour = content.eval(sampleCoordinate);
        colour *= half(coverage);
        colour.rgb *= half(darkness);
        return colour;
    }
"""
