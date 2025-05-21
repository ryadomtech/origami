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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import tech.ryadom.origami.style.OrigamiCropArea
import tech.ryadom.origami.util.BitmapSource
import tech.ryadom.origami.util.OrigamiCroppingUtils
import tech.ryadom.origami.util.OrigamiSource
import tech.ryadom.origami.util.PainterSource

/**
 * Origami [ImageBitmap] and [OrigamiCroppingUtils] state holder
 *
 * @param source image [OrigamiSource]
 */
class Origami(
    internal val source: OrigamiSource
) {

    /**
     * [OrigamiCroppingUtils] for manage state of crop area
     */
    private lateinit var origamiCroppingUtils: OrigamiCroppingUtils

    /**
     * Cropping [source] to crop area
     * @return cropped [source]
     */
    fun crop(): ImageBitmap {
        return origamiCroppingUtils.cropImage(
            source.getImageBitmap()
        )
    }

    /**
     * Function to prepare origami instance to work
     * @param cropArea [OrigamiCropArea]
     * @return origami crop rectangle [Rect]
     */
    internal fun prepare(cropArea: OrigamiCropArea): State<Rect> {
        origamiCroppingUtils = OrigamiCroppingUtils(cropArea.aspectRatio)
        return origamiCroppingUtils.origamiCropRect
    }

    /**
     * Image globally positioned callback
     * @param size new size
     * @param topLeft top left [Offset]
     */
    internal fun onGloballyPositioned(topLeft: Offset, size: IntSize) {
        origamiCroppingUtils.onGloballyPositioned(topLeft, size)
    }

    /**
     * Dragging start callback
     * @param touchPoint initial point
     */
    internal fun onDragStart(touchPoint: Offset) {
        origamiCroppingUtils.onDragStart(touchPoint)
    }

    /**
     * Dragging callback
     * @param dragPoint drag point
     */
    internal fun onDrag(dragPoint: Offset) {
        origamiCroppingUtils.onDrag(dragPoint)
    }

    /**
     * Dragging end callback
     */
    internal fun onDragEnd() {
        origamiCroppingUtils.onDragEnd()
    }

    companion object {

        /**
         * Creates instance of [Origami] with [BitmapSource]
         * @param imageBitmap initial bitmap
         * @see [OrigamiSource]
         */
        fun of(imageBitmap: ImageBitmap): Origami {
            return Origami(
                source = BitmapSource(imageBitmap)
            )
        }

        /**
         * Creates instance of [Origami] with [PainterSource]
         * @param painter your [Painter]
         * @param density your [Density]
         * @param layoutDirection your [LayoutDirection]
         * @see [OrigamiSource]
         */
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