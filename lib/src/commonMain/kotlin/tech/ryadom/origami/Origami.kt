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

package tech.ryadom.origami

import androidx.compose.runtime.State
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import tech.ryadom.origami.style.OrigamiCropArea
import tech.ryadom.origami.util.BitmapSource
import tech.ryadom.origami.util.OrigamiCropRect
import tech.ryadom.origami.util.OrigamiCroppingUtils
import tech.ryadom.origami.util.OrigamiSource
import tech.ryadom.origami.util.PainterSource

/**
 * Origami [ImageBitmap] and [OrigamiCroppingUtils] state holder
 *
 * @param source image source
 * @see [OrigamiSource]
 */
class Origami(
    val source: OrigamiSource
) {

    /**
     * [OrigamiCroppingUtils] for manage state of crop area
     */
    private var origamiCroppingUtils: OrigamiCroppingUtils? = null

    /**
     * Function to prepare origami instance to work
     * @param cropArea [OrigamiCropArea]
     */
    fun prepare(cropArea: OrigamiCropArea): State<OrigamiCropRect> {
        origamiCroppingUtils = OrigamiCroppingUtils(cropArea.aspectRatio)
        return origamiCroppingUtils!!.origamiCropRect
    }

    /**
     * Cropping [source] to crop area
     * @return cropped [source]
     */
    fun crop(): ImageBitmap {
        requireNotNull(origamiCroppingUtils) { "Origami.prepare() not called!" }
        return origamiCroppingUtils!!.cropImage(
            source.getImageBitmap()
        )
    }

    fun onCanvasSizeChanged(intSize: IntSize) {
        origamiCroppingUtils?.onCanvasSizeChanged(intSize)
    }

    fun onDragStart(touchPoint: Offset) {
        origamiCroppingUtils?.onDragStart(touchPoint)
    }

    fun onDrag(dragPoint: Offset) {
        origamiCroppingUtils?.onDrag(dragPoint)
    }

    fun onDragEnd() {
        origamiCroppingUtils?.onDragEnd()
    }

    companion object {

        fun of(imageBitmap: ImageBitmap): Origami {
            return Origami(
                source = BitmapSource(imageBitmap)
            )
        }

        fun of(painter: Painter, density: Density, layoutDirection: LayoutDirection): Origami {
            return Origami(
                source = PainterSource(
                    painter = painter,
                    density = density,
                    layoutDirection = layoutDirection
                )
            )
        }
    }
}