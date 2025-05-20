package tech.ryadom.origami.util

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

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

/**
 * Bitmap source drawing painter
 * @param painter [Painter]
 * @param density [Density]
 * @param layoutDirection [LayoutDirection]
 */
internal data class PainterSource(
    private val painter: Painter,
    private val density: Density,
    private val layoutDirection: LayoutDirection
) : OrigamiSource {

    @Composable
    override fun Content(modifier: Modifier) {
        Image(
            modifier = modifier,
            painter = painter,
            contentDescription = null
        )
    }

    override fun getImageBitmap(): ImageBitmap {
        return painter.toImageBitmap(density, layoutDirection)
    }

    // Convert painter to image bitmap
    private fun Painter.toImageBitmap(
        density: Density,
        layoutDirection: LayoutDirection
    ): ImageBitmap {
        val bitmap = ImageBitmap(intrinsicSize.width.toInt(), intrinsicSize.height.toInt())
        val canvas = Canvas(bitmap)

        CanvasDrawScope().draw(density, layoutDirection, canvas, intrinsicSize) {
            draw(intrinsicSize)
        }

        return bitmap
    }
}