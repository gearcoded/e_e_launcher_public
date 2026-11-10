package org.collegiumdesideriorum.e_e_launcher

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog as MaterialAlertDialog
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

internal data class LauncherDialogStyle(
    val backgroundMode: LauncherInterfaceBackgroundMode = LauncherInterfaceBackgroundMode.COLOR,
    val colorAHex: String = "#202028",
    val colorBHex: String = "#34344A",
    val imageUri: String? = null,
    val opacityPercent: Int = 50,
    val blurDp: Int = 0,
    val textColorHex: String = "#FFFFFF",
    val textShadowEnabled: Boolean = true,
    val textShadowOffsetDp: Int = 2,
    val textShadowBlurDp: Int = 4,
    val textShadowColorHex: String = "#000000",
    val textShadowOpacityPercent: Int = 65
)

internal val LocalLauncherDialogStyle = staticCompositionLocalOf { LauncherDialogStyle() }

internal data class LauncherDialogStyleBinding(
    val value: LauncherDialogStyle,
    val onValueChange: (LauncherDialogStyle) -> Unit,
    val onChooseImage: () -> Unit
)

@Composable
internal fun rememberLauncherDialogStyleBinding(): LauncherDialogStyleBinding {
    val context = LocalContext.current
    var value by remember {
        mutableStateOf(loadLauncherDialogStyle(context.applicationContext))
    }
    fun update(proposed: LauncherDialogStyle) {
        value = proposed.copy(
            opacityPercent = proposed.opacityPercent.coerceIn(0, 100),
            blurDp = proposed.blurDp.coerceIn(0, 48),
            textShadowOffsetDp = proposed.textShadowOffsetDp.coerceIn(0, 12),
            textShadowBlurDp = proposed.textShadowBlurDp.coerceIn(0, 24),
            textShadowOpacityPercent = proposed.textShadowOpacityPercent.coerceIn(0, 100)
        )
        saveLauncherDialogStyle(context.applicationContext, value)
    }
    val imagePicker = rememberLauncherForActivityResult(OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            update(value.copy(
                backgroundMode = LauncherInterfaceBackgroundMode.IMAGE,
                imageUri = uri.toString(),
                opacityPercent = value.opacityPercent.coerceAtLeast(35)
            ))
        }
    }
    return LauncherDialogStyleBinding(
        value = value,
        onValueChange = ::update,
        onChooseImage = { imagePicker.launch(arrayOf("image/*")) }
    )
}

private fun loadLauncherDialogStyle(context: Context): LauncherDialogStyle {
    val raw = context.getSharedPreferences(DIALOG_PREFS_NAME, Context.MODE_PRIVATE)
        .getString(DIALOG_STYLE_KEY, null) ?: return LauncherDialogStyle()
    return runCatching {
        val json = JSONObject(raw)
        LauncherDialogStyle(
            backgroundMode = runCatching {
                LauncherInterfaceBackgroundMode.valueOf(json.optString("backgroundMode"))
            }.getOrDefault(LauncherInterfaceBackgroundMode.COLOR),
            colorAHex = json.optString("colorAHex", "#202028"),
            colorBHex = json.optString("colorBHex", "#34344A"),
            imageUri = json.optString("imageUri").takeIf { it.isNotBlank() },
            opacityPercent = json.optInt("opacityPercent", 50).coerceIn(0, 100),
            blurDp = json.optInt("blurDp", 0).coerceIn(0, 48),
            textColorHex = json.optString("textColorHex", "#FFFFFF"),
            textShadowEnabled = json.optBoolean("textShadowEnabled", true),
            textShadowOffsetDp = json.optInt("textShadowOffsetDp", 2).coerceIn(0, 12),
            textShadowBlurDp = json.optInt("textShadowBlurDp", 4).coerceIn(0, 24),
            textShadowColorHex = json.optString("textShadowColorHex", "#000000"),
            textShadowOpacityPercent = json.optInt("textShadowOpacityPercent", 65).coerceIn(0, 100)
        )
    }.getOrDefault(LauncherDialogStyle())
}

private fun saveLauncherDialogStyle(context: Context, style: LauncherDialogStyle) {
    val json = JSONObject().apply {
        put("backgroundMode", style.backgroundMode.name)
        put("colorAHex", style.colorAHex)
        put("colorBHex", style.colorBHex)
        style.imageUri?.let { put("imageUri", it) }
        put("opacityPercent", style.opacityPercent.coerceIn(0, 100))
        put("blurDp", style.blurDp.coerceIn(0, 48))
        put("textColorHex", style.textColorHex)
        put("textShadowEnabled", style.textShadowEnabled)
        put("textShadowOffsetDp", style.textShadowOffsetDp.coerceIn(0, 12))
        put("textShadowBlurDp", style.textShadowBlurDp.coerceIn(0, 24))
        put("textShadowColorHex", style.textShadowColorHex)
        put("textShadowOpacityPercent", style.textShadowOpacityPercent.coerceIn(0, 100))
    }
    context.getSharedPreferences(DIALOG_PREFS_NAME, Context.MODE_PRIVATE).edit()
        .putString(DIALOG_STYLE_KEY, json.toString()).apply()
}

private const val DIALOG_PREFS_NAME = "e_e_launcher_workspace"
private const val DIALOG_STYLE_KEY = "launcher_dialog_style"

@Composable
internal fun AlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
    styleOverride: LauncherDialogStyle? = null
) {
    val inheritedStyle = LocalLauncherDialogStyle.current
    val style = styleOverride ?: inheritedStyle
    val backgroundModifier = rememberDialogBackgroundModifier(style)
    val textColor = parseInterfaceColor(style.textColorHex)
    MaterialAlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = { DialogTextSlot(style, confirmButton) },
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .then(backgroundModifier),
        dismissButton = dismissButton?.let { slot -> { DialogTextSlot(style, slot) } },
        icon = icon?.let { slot -> { DialogTextSlot(style, slot) } },
        title = title?.let { slot -> { DialogTextSlot(style, slot) } },
        text = text?.let { slot -> { DialogTextSlot(style, slot) } },
        containerColor = Color.Transparent,
        iconContentColor = textColor,
        titleContentColor = textColor,
        textContentColor = textColor,
        tonalElevation = 0.dp
    )
}

@Composable
private fun DialogTextSlot(style: LauncherDialogStyle, content: @Composable () -> Unit) {
    val density = LocalDensity.current
    val color = parseInterfaceColor(style.textColorHex)
    val shadow = if (style.textShadowEnabled) {
        Shadow(
            color = parseInterfaceColor(style.textShadowColorHex).copy(
                alpha = style.textShadowOpacityPercent.coerceIn(0, 100) / 100f
            ),
            offset = Offset(
                with(density) { style.textShadowOffsetDp.coerceIn(0, 12).dp.toPx() },
                with(density) { style.textShadowOffsetDp.coerceIn(0, 12).dp.toPx() }
            ),
            blurRadius = with(density) { style.textShadowBlurDp.coerceIn(0, 24).dp.toPx() }
        )
    } else null
    CompositionLocalProvider(
        LocalContentColor provides color,
        LocalTextStyle provides LocalTextStyle.current.merge(TextStyle(color = color, shadow = shadow))
    ) { content() }
}

@Composable
private fun rememberDialogBackgroundModifier(style: LauncherDialogStyle): Modifier {
    val context = LocalContext.current
    val density = context.resources.displayMetrics.density
    var image by remember(style.imageUri, style.blurDp) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(style.imageUri, style.blurDp) {
        image = withContext(Dispatchers.IO) {
            style.imageUri?.let { raw ->
                runCatching {
                    val decoded = decodeLauncherBackground(
                        context, raw, maxSidePx = MAX_DIALOG_IMAGE_SIDE_PX.toInt(),
                        softwareBitmap = true
                    )
                    prepareDialogBitmap(
                        source = decoded,
                        blurRadiusPx = (style.blurDp.coerceIn(0, 48) * density).roundToInt()
                    )
                }.getOrNull()
            }
        }
    }
    val alpha = style.opacityPercent.coerceIn(0, 100) / 100f
    val colorA = parseInterfaceColor(style.colorAHex).copy(alpha = alpha)
    val colorB = parseInterfaceColor(style.colorBHex).copy(alpha = alpha)
    return Modifier.drawWithContent {
        when (style.backgroundMode) {
            LauncherInterfaceBackgroundMode.NONE -> Unit
            LauncherInterfaceBackgroundMode.COLOR -> drawRect(colorA)
            LauncherInterfaceBackgroundMode.GRADIENT -> drawRect(
                Brush.verticalGradient(listOf(colorA, colorB))
            )
            LauncherInterfaceBackgroundMode.IMAGE -> {
                val bitmap = image
                if (bitmap == null) {
                    drawRect(colorA)
                } else {
                    drawIntoCanvas { composeCanvas ->
                        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                            this.alpha = (alpha * 255f).toInt().coerceIn(0, 255)
                        }
                        composeCanvas.nativeCanvas.drawBitmap(
                            bitmap,
                            null,
                            RectF(0f, 0f, size.width, size.height),
                            paint
                        )
                    }
                }
            }
        }
        drawContent()
    }
}

private fun prepareDialogBitmap(source: Bitmap, blurRadiusPx: Int): Bitmap {
    val largestSide = max(source.width, source.height).coerceAtLeast(1)
    val scale = min(1f, MAX_DIALOG_IMAGE_SIDE_PX / largestSide.toFloat())
    val workingBitmap = if (scale < 1f) {
        Bitmap.createScaledBitmap(
            source,
            (source.width * scale).roundToInt().coerceAtLeast(1),
            (source.height * scale).roundToInt().coerceAtLeast(1),
            true
        )
    } else {
        source
    }
    if (blurRadiusPx <= 0) return workingBitmap
    val scaledRadius = (blurRadiusPx * scale).roundToInt().coerceIn(1, MAX_DIALOG_BLUR_RADIUS_PX)
    return boxBlurBitmap(workingBitmap, scaledRadius)
}

/** Applies a separable box blur without RenderScript, native code, or unsupported Paint APIs. */
private fun boxBlurBitmap(source: Bitmap, radius: Int): Bitmap {
    val width = source.width
    val height = source.height
    if (width <= 0 || height <= 0 || radius <= 0) return source

    val sourcePixels = IntArray(width * height)
    val horizontalPixels = IntArray(sourcePixels.size)
    val resultPixels = IntArray(sourcePixels.size)
    source.getPixels(sourcePixels, 0, width, 0, 0, width, height)
    val diameter = radius * 2 + 1

    for (y in 0 until height) {
        val rowStart = y * width
        var alpha = 0
        var red = 0
        var green = 0
        var blue = 0
        for (offset in -radius..radius) {
            val color = sourcePixels[rowStart + offset.coerceIn(0, width - 1)]
            alpha += color ushr 24
            red += color ushr 16 and 0xFF
            green += color ushr 8 and 0xFF
            blue += color and 0xFF
        }
        for (x in 0 until width) {
            horizontalPixels[rowStart + x] = packBlurColor(alpha, red, green, blue, diameter)
            val removed = sourcePixels[rowStart + (x - radius).coerceIn(0, width - 1)]
            val added = sourcePixels[rowStart + (x + radius + 1).coerceIn(0, width - 1)]
            alpha += (added ushr 24) - (removed ushr 24)
            red += (added ushr 16 and 0xFF) - (removed ushr 16 and 0xFF)
            green += (added ushr 8 and 0xFF) - (removed ushr 8 and 0xFF)
            blue += (added and 0xFF) - (removed and 0xFF)
        }
    }

    for (x in 0 until width) {
        var alpha = 0
        var red = 0
        var green = 0
        var blue = 0
        for (offset in -radius..radius) {
            val color = horizontalPixels[offset.coerceIn(0, height - 1) * width + x]
            alpha += color ushr 24
            red += color ushr 16 and 0xFF
            green += color ushr 8 and 0xFF
            blue += color and 0xFF
        }
        for (y in 0 until height) {
            resultPixels[y * width + x] = packBlurColor(alpha, red, green, blue, diameter)
            val removed = horizontalPixels[(y - radius).coerceIn(0, height - 1) * width + x]
            val added = horizontalPixels[(y + radius + 1).coerceIn(0, height - 1) * width + x]
            alpha += (added ushr 24) - (removed ushr 24)
            red += (added ushr 16 and 0xFF) - (removed ushr 16 and 0xFF)
            green += (added ushr 8 and 0xFF) - (removed ushr 8 and 0xFF)
            blue += (added and 0xFF) - (removed and 0xFF)
        }
    }

    return Bitmap.createBitmap(resultPixels, width, height, Bitmap.Config.ARGB_8888)
}

private fun packBlurColor(alpha: Int, red: Int, green: Int, blue: Int, divisor: Int): Int =
    (alpha / divisor shl 24) or
        (red / divisor shl 16) or
        (green / divisor shl 8) or
        (blue / divisor)

private const val MAX_DIALOG_IMAGE_SIDE_PX = 1200
private const val MAX_DIALOG_BLUR_RADIUS_PX = 64
