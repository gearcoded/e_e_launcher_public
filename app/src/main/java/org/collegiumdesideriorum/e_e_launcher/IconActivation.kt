package org.collegiumdesideriorum.e_e_launcher

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.sin

/** One activation implementation shared by docks, folders and pulled-out folder icons. */
@Composable
internal fun Modifier.iconActivationEffect(
    activationSerial: Int,
    shape: Shape,
    kind: IconActivationKind = IconActivationKind.LAUNCH,
    effectOverride: AppLaunchAnimationEffect? = null,
    durationOverride: Int? = null,
    onlyEffect: AppLaunchAnimationEffect? = null,
    excludedEffect: AppLaunchAnimationEffect? = null
): Modifier {
    val context = LocalContext.current
    val density = LocalDensity.current.density
    val configuredRaw = effectOverride ?: loadConfiguredIconActivation(context, kind)
    val configured = when {
        onlyEffect != null && configuredRaw != onlyEffect -> AppLaunchAnimationEffect.NONE
        excludedEffect != null && configuredRaw == excludedEffect -> AppLaunchAnimationEffect.NONE
        else -> configuredRaw
    }
    val duration = durationOverride ?: loadConfiguredIconActivationDuration(context, kind)
    val progress = remember { Animatable(0f) }
    val wireframeRenderEffect = if (configured == AppLaunchAnimationEffect.ICON_THEN_WIREFRAME) {
        sharedIconWireframeRenderEffect
    } else null
    val resolved = remember(activationSerial, configured) {
        if (configured != AppLaunchAnimationEffect.ROTATE_RANDOM) configured
        else listOf(
            AppLaunchAnimationEffect.ROTATE_2D_CW,
            AppLaunchAnimationEffect.ROTATE_2D_CCW,
            AppLaunchAnimationEffect.ROTATE_3D_RIGHT,
            AppLaunchAnimationEffect.ROTATE_3D_LEFT,
            AppLaunchAnimationEffect.ROTATE_3D_DIAGONAL
        )[abs(activationSerial) % 5]
    }

    LaunchedEffect(activationSerial, resolved, duration) {
        if (activationSerial <= 0 || resolved == AppLaunchAnimationEffect.NONE) return@LaunchedEffect
        progress.snapTo(0f)
        progress.animateTo(1f, tween(duration))
        progress.snapTo(0f)
    }

    val p = progress.value
    val wireProgress = ((p - 0.5f) * 2f).coerceIn(0f, 1f)
    // The wireframe must be visibly present at 400% instead of becoming fully
    // transparent exactly when it reaches that size. It grows from 200% to
    // 400% during the first 70% of the wireframe phase, then remains at 400%
    // while fading during the final 30%.
    val wireGrowthProgress = (wireProgress / 0.70f).coerceIn(0f, 1f)
    val wireFadeProgress = ((wireProgress - 0.70f) / 0.30f).coerceIn(0f, 1f)
    val effectScale = when (resolved) {
        AppLaunchAnimationEffect.ICON_EXPAND -> 1f + p * 0.75f
        AppLaunchAnimationEffect.FRAME -> 1f + p * 0.28f
        AppLaunchAnimationEffect.ICON_THEN_WIREFRAME -> {
            if (p < 0.5f) 1f + p * 2f else 2f + wireGrowthProgress * 2f
        }
        else -> 1f
    }
    val opacity = when (resolved) {
        AppLaunchAnimationEffect.ICON_EXPAND, AppLaunchAnimationEffect.FRAME -> 1f - p * 0.72f
        AppLaunchAnimationEffect.ICON_THEN_WIREFRAME -> if (p < 0.5f) 1f else 1f - wireFadeProgress
        AppLaunchAnimationEffect.BLINK -> if (p == 0f) 1f else 0.30f + 0.70f * abs(sin(p * Math.PI.toFloat() * 6f))
        else -> 1f
    }

    return this
        .graphicsLayer {
            scaleX = effectScale
            scaleY = effectScale
            alpha = opacity
            renderEffect = if (
                resolved == AppLaunchAnimationEffect.ICON_THEN_WIREFRAME &&
                p >= 0.5f &&
                wireframeRenderEffect != null
            ) wireframeRenderEffect else null
            translationY = if (resolved == AppLaunchAnimationEffect.BOUNCE && p > 0f) {
                -abs(sin(p * Math.PI.toFloat() * 2f)) * 22f * density
            } else 0f
            rotationZ = when (resolved) {
                AppLaunchAnimationEffect.ROTATE_2D_CW -> 360f * p
                AppLaunchAnimationEffect.ROTATE_2D_CCW -> -360f * p
                AppLaunchAnimationEffect.ROTATE_3D_DIAGONAL -> 36f * p
                else -> 0f
            }
            rotationY = when (resolved) {
                AppLaunchAnimationEffect.ROTATE_3D_RIGHT -> 360f * p
                AppLaunchAnimationEffect.ROTATE_3D_LEFT -> -360f * p
                AppLaunchAnimationEffect.ROTATE_3D_DIAGONAL -> 300f * p
                else -> 0f
            }
            rotationX = if (resolved == AppLaunchAnimationEffect.ROTATE_3D_DIAGONAL) 220f * p else 0f
            cameraDistance = 32f * density
        }
        .then(
            if (resolved == AppLaunchAnimationEffect.FRAME && p > 0f) {
                Modifier.border((1f + p * 3f).dp, androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 1f - p), shape)
            } else Modifier
        )
}

/*
 * Project-created edge extraction for the "Icon, then wireframe" animation.
 * It receives the already rendered icon as a child shader and estimates local
 * luminance/alpha gradients. The second half of the animation therefore shows
 * lines derived from the icon image itself instead of a generic rounded frame.
 */
private const val ICON_WIREFRAME_SHADER = """
    uniform shader content;

    half luminance(half4 c) {
        if (c.a <= 0.001) return 0.0;
        half3 rgb = c.rgb / max(c.a, 0.001);
        return dot(rgb, half3(0.2126, 0.7152, 0.0722));
    }

    half4 main(float2 p) {
        const float stepPx = 1.35;
        half4 center = content.eval(p);
        half4 left = content.eval(p + float2(-stepPx, 0.0));
        half4 right = content.eval(p + float2(stepPx, 0.0));
        half4 up = content.eval(p + float2(0.0, -stepPx));
        half4 down = content.eval(p + float2(0.0, stepPx));

        half gx = abs(luminance(right) - luminance(left));
        half gy = abs(luminance(down) - luminance(up));
        half ax = abs(right.a - left.a);
        half ay = abs(down.a - up.a);
        half edge = clamp((gx + gy) * 2.6 + (ax + ay) * 1.8, 0.0, 1.0);

        // Keep faint internal colour boundaries visible while suppressing the
        // original filled image. Output is premultiplied for Android rendering.
        half alpha = max(edge, edge * center.a);
        return half4(alpha, alpha, alpha, alpha);
    }
"""

private val sharedIconWireframeRenderEffect by lazy {
    runCatching {
        RenderEffect.createRuntimeShaderEffect(
            RuntimeShader(ICON_WIREFRAME_SHADER),
            "content"
        ).asComposeRenderEffect()
    }.getOrNull()
}
