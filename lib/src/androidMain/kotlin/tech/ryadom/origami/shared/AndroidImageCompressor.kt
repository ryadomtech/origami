package tech.ryadom.origami.shared

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.decodeToImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import tech.ryadom.origami.style.OrigamiCompression
import java.io.ByteArrayOutputStream
import kotlin.math.min

private class AndroidImageCompressor : ImageCompressor {
    override fun scaleToPlatformLimits(image: ImageBitmap): ImageBitmap {
        val canvas = Canvas()
        val bitmap = image.asAndroidBitmap()

        val maxWidth = canvas.maximumBitmapWidth
        val maxHeight = canvas.maximumBitmapHeight

        val scale = min(
            a = maxWidth.toFloat() / bitmap.width,
            b = maxHeight.toFloat() / bitmap.height
        )

        val newWidth = (bitmap.width * scale).toInt()
        val newHeight = (bitmap.height * scale).toInt()

        @SuppressLint("UseKtx")
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
            .asImageBitmap()
    }

    override suspend fun compress(
        image: ImageBitmap,
        compression: OrigamiCompression
    ): ImageBitmap {
        return withContext(Dispatchers.IO) {
            val bitmap = scaleToPlatformLimits(image)
                .asAndroidBitmap()

            var quality = compression.startQuality
            var compressed: ByteArray

            do {
                ByteArrayOutputStream().use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                    compressed = outputStream.toByteArray()
                    quality -= compression.qualityDowngradeStep
                }
            } while (
                compressed.size > compression.maxSize
                && isActive
                && quality > 10
            )

            compressed.decodeToImageBitmap()
        }
    }
}

actual fun createImageCompressor(): ImageCompressor {
    return AndroidImageCompressor()
}