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

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import tech.ryadom.origami.style.OrigamiAspectRatio
import tech.ryadom.origami.util.BitmapSource
import tech.ryadom.origami.util.OrigamiCroppingUtils
import tech.ryadom.origami.util.OrigamiSource
import tech.ryadom.origami.util.PainterSource

/**
 * Origami [ImageBitmap] and [OrigamiCroppingUtils] state holder
 *
 * @param source image [OrigamiSource]
 * @param aspectRatio [OrigamiAspectRatio]
 */
class Origami(
    internal val source: OrigamiSource,
    private val aspectRatio: OrigamiAspectRatio
) {

    /**
     * [OrigamiCroppingUtils] for manage state of crop area
     */
    private val origamiCroppingUtils = OrigamiCroppingUtils(aspectRatio)

    /**
     * Origami crop rect.
     */
    val origamiRect = origamiCroppingUtils.origamiCropRect

    /**
     * Cropping [source] to crop area
     * @return cropped [source]
     */
    fun crop(): ImageBitmap {
        return origamiCroppingUtils.crop(
            source.getImageBitmap()
        )
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
         * @param aspectRatio [OrigamiAspectRatio]
         * @see [OrigamiSource]
         */
        fun of(
            imageBitmap: ImageBitmap,
            aspectRatio: OrigamiAspectRatio = OrigamiAspectRatio()
        ): Origami {
            return Origami(
                source = BitmapSource(imageBitmap),
                aspectRatio = aspectRatio
            )
        }

        /**
         * Creates instance of [Origami] with [PainterSource]
         * @param painter your [Painter]
         * @param density your [Density]
         * @param layoutDirection your [LayoutDirection]
         * @param aspectRatio [OrigamiAspectRatio]
         * @see [OrigamiSource]
         */
        fun of(
            painter: Painter,
            density: Density,
            layoutDirection: LayoutDirection,
            aspectRatio: OrigamiAspectRatio = OrigamiAspectRatio()
        ): Origami {
            return Origami(
                source = PainterSource(
                    painter = painter,
                    density = density,
                    layoutDirection = layoutDirection
                ),
                aspectRatio = aspectRatio
            )
        }
    }
}