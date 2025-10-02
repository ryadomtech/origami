package tech.ryadom.origami.util

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import tech.ryadom.origami.Origami

/**
 * [Origami] image source
 */
interface OrigamiSource {

    /**
     * Composable content for displaying source
     * @param modifier [Modifier]
     */
    @Composable
    fun Content(modifier: Modifier)

    /**
     * Transform your source to [ImageBitmap]
     */
    fun getImageBitmap(): ImageBitmap
}

/**
 * Bitmap source drawing bitmap
 * @param imageBitmap [ImageBitmap]
 */
internal data class BitmapSource(
    private val imageBitmap: ImageBitmap
) : OrigamiSource {

    @Composable
    override fun Content(modifier: Modifier) {
        Image(
            modifier = modifier,
            bitmap = imageBitmap,
            contentDescription = null
        )
    }

    override fun getImageBitmap(): ImageBitmap {
        return imageBitmap
    }
}