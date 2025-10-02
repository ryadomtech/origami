/*
   Copyright 2025 Ryadom Tech

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package tech.ryadom.origami.shared

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.decodeToImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import tech.ryadom.origami.style.OrigamiCompression
import java.io.ByteArrayOutputStream
import kotlin.math.min

private class AndroidImageCompressor : ImageCompressor {
    // Prevents potentials OOM
    override fun scaleToPlatformLimits(image: ImageBitmap): ImageBitmap {
        val maxBitmapSize = when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.N -> 2048
            else -> 4096
        }

        if (image.width <= maxBitmapSize && image.height <= maxBitmapSize) {
            return image
        }

        val bitmap = image.asAndroidBitmap()
        val scale = min(
            a = maxBitmapSize.toFloat() / bitmap.width,
            b = maxBitmapSize.toFloat() / bitmap.height
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

            ensureActive()

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