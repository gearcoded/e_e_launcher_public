package org.collegiumdesideriorum.e_e_launcher

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal enum class LauncherInterfaceBackgroundMode(override val titleRes: Int) : LocalizedTitle {
    NONE(R.string.ui_no_background),
    COLOR(R.string.ui_colour),
    GRADIENT(R.string.ui_gradient),
    IMAGE(R.string.ui_picture)
}

internal data class LauncherInterfaceStyle(
    val backgroundMode: LauncherInterfaceBackgroundMode = LauncherInterfaceBackgroundMode.NONE,
    val colorAHex: String = "#15151C",
    val colorBHex: String = "#34345A",
    val imageUri: String? = null,
    val opacityPercent: Int = 0,
    val blurDp: Int = 0,
    val textColorHex: String = "#FFFFFF"
)

@Composable
internal fun LauncherInterfaceBackground(style: LauncherInterfaceStyle, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var image by remember(style.imageUri) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(style.imageUri) {
        image = withContext(Dispatchers.IO) {
            style.imageUri?.let { raw ->
                runCatching {
                    decodeLauncherBackground(context, raw).asImageBitmap()
                }.getOrNull()
            }
        }
    }
    val alpha = style.opacityPercent.coerceIn(0, 100) / 100f
    val layerModifier = modifier.fillMaxSize().blur(style.blurDp.coerceIn(0, 48).dp)
    when (style.backgroundMode) {
        LauncherInterfaceBackgroundMode.NONE -> Unit
        LauncherInterfaceBackgroundMode.COLOR -> Box(
            layerModifier.background(parseInterfaceColor(style.colorAHex).copy(alpha = alpha))
        )
        LauncherInterfaceBackgroundMode.GRADIENT -> Box(
            layerModifier.background(
                Brush.verticalGradient(
                    listOf(
                        parseInterfaceColor(style.colorAHex).copy(alpha = alpha),
                        parseInterfaceColor(style.colorBHex).copy(alpha = alpha)
                    )
                )
            )
        )
        LauncherInterfaceBackgroundMode.IMAGE -> image?.let { bitmap ->
            Image(bitmap, null, layerModifier, contentScale = ContentScale.Crop, alpha = alpha)
        }
    }
}

internal fun parseInterfaceColor(raw: String): Color = runCatching {
    Color(android.graphics.Color.parseColor(raw))
}.getOrDefault(Color.White)
