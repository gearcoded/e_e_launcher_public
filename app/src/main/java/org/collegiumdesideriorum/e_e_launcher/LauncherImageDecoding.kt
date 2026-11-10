package org.collegiumdesideriorum.e_e_launcher

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import kotlin.math.max
import kotlin.math.roundToInt

/** Decode user-selected backgrounds without first allocating their full-resolution bitmap. */
internal fun decodeLauncherBackground(
    context: Context,
    rawUri: String,
    maxSidePx: Int = 2560,
    softwareBitmap: Boolean = false
): Bitmap {
    require(maxSidePx > 0)
    val source = ImageDecoder.createSource(context.contentResolver, Uri.parse(rawUri))
    return ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
        val width = info.size.width.coerceAtLeast(1)
        val height = info.size.height.coerceAtLeast(1)
        val largest = max(width, height)
        if (largest > maxSidePx) {
            val scale = maxSidePx.toDouble() / largest
            decoder.setTargetSize(
                (width * scale).roundToInt().coerceAtLeast(1),
                (height * scale).roundToInt().coerceAtLeast(1)
            )
        }
        decoder.isMutableRequired = false
        if (softwareBitmap) decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
    }
}
