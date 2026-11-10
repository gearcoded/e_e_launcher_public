package org.collegiumdesideriorum.e_e_launcher

import android.graphics.ImageDecoder
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun rememberLauncherCustomIcon(
    imageUri: String?,
    iconPackPackage: String?,
    iconPackDrawable: String?
): ImageBitmap? {
    val context = LocalContext.current
    var bitmap by remember(imageUri, iconPackPackage, iconPackDrawable) {
        mutableStateOf<ImageBitmap?>(null)
    }
    LaunchedEffect(imageUri, iconPackPackage, iconPackDrawable) {
        bitmap = withContext(Dispatchers.IO) {
            when {
                !imageUri.isNullOrBlank() -> runCatching {
                    val source = ImageDecoder.createSource(context.contentResolver, Uri.parse(imageUri))
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.setTargetSize(192, 192)
                        decoder.isMutableRequired = false
                    }.asImageBitmap()
                }.getOrNull()
                !iconPackPackage.isNullOrBlank() && !iconPackDrawable.isNullOrBlank() ->
                    IconPackRepository.loadIcon(context.applicationContext, iconPackPackage, iconPackDrawable)
                else -> null
            }
        }
    }
    return bitmap
}
