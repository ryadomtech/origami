package tech.ryadom.origami.shared

import androidx.compose.ui.graphics.ImageBitmap
import tech.ryadom.origami.style.OrigamiCompression

private class WasmJsImageCompressor : ImageCompressor {
    override fun scaleToPlatformLimits(image: ImageBitmap): ImageBitmap {
        return image
    }

    override suspend fun compress(
        image: ImageBitmap,
        compression: OrigamiCompression
    ): ImageBitmap {
        // Will done in 0.0.6
        return image
    }
}

actual fun createImageCompressor(): ImageCompressor {
    return WasmJsImageCompressor()
}
