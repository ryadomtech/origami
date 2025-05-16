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

package tech.ryadom.origami.util.extensions

import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize

/**
 * Cropping [ImageBitmap] to given [IntRect].
 *
 * @param rect [IntRect] representing crop area
 *
 * @return [ImageBitmap] copy, cropped to given rect
 */
fun ImageBitmap.crop(rect: IntRect): ImageBitmap {
    require(rect.left >= 0 && rect.top >= 0 && rect.right <= width && rect.bottom <= height) {
        "Crop rectangle must be within image bounds"
    }

    return ImageBitmap(rect.width, rect.height).apply {
        val canvas = Canvas(this)
        canvas.drawImageRect(
            image = this@crop,
            srcOffset = IntOffset(rect.left, rect.top),
            srcSize = IntSize(rect.width, rect.height),
            dstSize = IntSize(rect.width, rect.height),
            paint = Paint()
        )
    }
}

/**
 * Scaling [ImageBitmap] to given new width and new height.
 *
 * @param newWidth new width
 * @param newHeight new... height. Are you surprised?
 *
 * @return [ImageBitmap] copy, scaled to given new width and new height
 */
fun ImageBitmap.scale(newWidth: Int, newHeight: Int): ImageBitmap {
    return ImageBitmap(newWidth, newHeight).apply {
        val canvas = Canvas(this)
        canvas.drawImageRect(
            image = this@scale,
            srcSize = IntSize(this@scale.width, this@scale.height),
            dstSize = IntSize(newWidth, newHeight),
            paint = Paint()
        )
    }
}