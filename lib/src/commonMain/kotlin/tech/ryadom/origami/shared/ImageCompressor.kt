package tech.ryadom.origami.shared

import androidx.compose.ui.graphics.ImageBitmap
import tech.ryadom.origami.style.OrigamiCompression

interface ImageCompressor {

    fun scaleToPlatformLimits(image: ImageBitmap): ImageBitmap

    suspend fun compress(image: ImageBitmap, compression: OrigamiCompression): ImageBitmap

}

expect fun createImageCompressor(): ImageCompressor