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

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.decodeToImageBitmap
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.get
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import tech.ryadom.origami.style.OrigamiCompression

private class AppleImageCompressor : ImageCompressor {
    override fun scaleToPlatformLimits(image: ImageBitmap): ImageBitmap {
        return image
    }

    @BetaInteropApi
    @ExperimentalForeignApi
    override suspend fun compress(
        image: ImageBitmap,
        compression: OrigamiCompression
    ): ImageBitmap = withContext(Dispatchers.Default) {
        val bytes = scaleToPlatformLimits(image)
            .asSkiaBitmap()
            .readPixels()

        val nsData = bytes?.toNSData()
        val uiImage = nsData?.let { UIImage(it) }

        var quality = compression.startQuality / 100.0
        var compressed: NSData?

        ensureActive()
        do {
            compressed = uiImage?.let {
                UIImageJPEGRepresentation(it, quality)
            }

            quality -= compression.qualityDowngradeStep / 100.0
        } while (
            compressed != null
            && compressed.length > compression.maxSize.toULong()
            && isActive
            && quality > 0.1
        )

        compressed?.toByteArray()?.decodeToImageBitmap()
            ?: image
    }
}

actual fun createImageCompressor(): ImageCompressor {
    return AppleImageCompressor()
}

@ExperimentalForeignApi
private fun NSData.toByteArray(): ByteArray? = bytes?.let { bytes ->
    val pointer: CPointer<ByteVar> = bytes.reinterpret()
    ByteArray(length.toInt()) { pointer[it] }
}

@BetaInteropApi
@ExperimentalForeignApi
private fun ByteArray.toNSData(): NSData = usePinned { pinned ->
    NSData.create(
        bytes = pinned.addressOf(0),
        length = size.toULong()
    )
}