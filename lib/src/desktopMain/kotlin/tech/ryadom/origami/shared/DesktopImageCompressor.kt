package tech.ryadom.origami.shared

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import tech.ryadom.origami.style.OrigamiCompression
import java.io.ByteArrayOutputStream
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import javax.imageio.stream.MemoryCacheImageOutputStream

private class DesktopImageCompressor : ImageCompressor {
    override fun scaleToPlatformLimits(image: ImageBitmap): ImageBitmap {
        return image
    }

    override suspend fun compress(
        image: ImageBitmap,
        compression: OrigamiCompression
    ): ImageBitmap = withContext(Dispatchers.IO) {
        val awtImage = scaleToPlatformLimits(image).toAwtImage()
        var quality = compression.startQuality / 100f
        var compressed: ByteArray

        val writer = ImageIO.getImageWritersByFormatName("jpeg")
            .next()

        val param = writer.defaultWriteParam
        param.compressionMode = ImageWriteParam.MODE_EXPLICIT

        ensureActive()

        do {
            ByteArrayOutputStream().use { outputStream ->
                param.compressionQuality = quality

                writer.output = MemoryCacheImageOutputStream(outputStream)
                writer.write(null, IIOImage(awtImage, null, null), param)

                compressed = outputStream.toByteArray()
                quality -= compression.qualityDowngradeStep
            }
        } while (
            compressed.size > compression.maxSize
            && isActive
            && quality > 0.1
        )

        writer.dispose()
        compressed.decodeToImageBitmap()
    }
}

actual fun createImageCompressor(): ImageCompressor {
    return DesktopImageCompressor()
}