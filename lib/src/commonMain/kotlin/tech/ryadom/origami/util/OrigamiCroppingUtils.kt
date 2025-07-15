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

package tech.ryadom.origami.util

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.roundToIntRect
import tech.ryadom.origami.style.OrigamiAspectRatio
import tech.ryadom.origami.util.extensions.copy
import tech.ryadom.origami.util.extensions.crop
import tech.ryadom.origami.util.extensions.findEdgeContaining
import tech.ryadom.origami.util.extensions.update
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sign

/**
 * Origami cropping utils
 * @param aspectRatio [OrigamiAspectRatio]
 */
internal class OrigamiCroppingUtils(
    private val aspectRatio: OrigamiAspectRatio
) {

    internal val origamiCropRect = mutableStateOf(Rect.Zero)

    private var sourceSize: Size = Size.Zero

    private var isTouchedInsideRect: Boolean = false
    private var edgeTouched: Edge? = null

    private var lastPointTouched: Offset? = null

    private var initialTopLeft = Offset.Zero

    internal fun crop(bitmapImage: ImageBitmap): ImageBitmap {
        val bitmapWidth = bitmapImage.width.toFloat()
        val bitmapHeight = bitmapImage.height.toFloat()

        if (bitmapWidth <= 0 || bitmapHeight <= 0) return bitmapImage

        val composeWidth = sourceSize.width
        val composeHeight = sourceSize.height

        val scale = min(composeWidth / bitmapWidth, composeHeight / bitmapHeight)
        val scaledWidth = bitmapWidth * scale
        val scaledHeight = bitmapHeight * scale

        val imageOffsetX = (composeWidth - scaledWidth) / 2
        val imageOffsetY = (composeHeight - scaledHeight) / 2

        val imageInBox = initialTopLeft + Offset(imageOffsetX, imageOffsetY)

        val cropRect = getRect()

        val imageRect = Rect(imageInBox, Size(scaledWidth, scaledHeight))
        val intersectedCrop = cropRect.intersect(imageRect)

        if (intersectedCrop.width <= 0 || intersectedCrop.height <= 0) {
            return bitmapImage
        }

        val bitmapCropRect = Rect(
            left = (intersectedCrop.left - imageInBox.x) / scale,
            top = (intersectedCrop.top - imageInBox.y) / scale,
            right = (intersectedCrop.right - imageInBox.x) / scale,
            bottom = (intersectedCrop.bottom - imageInBox.y) / scale
        )

        return bitmapImage.crop(
            bitmapCropRect.roundToIntRect()
        )
    }

    internal fun onGloballyPositioned(topLeft: Offset, size: IntSize) {
        sourceSize = Size(
            width = size.width.toFloat(),
            height = size.height.toFloat()
        )

        initialTopLeft = topLeft
        resetCropRect()
    }

    internal fun onDragStart(touchPoint: Offset) {
        isTouchedInsideRect = getTouchRect().contains(touchPoint)

        if (!isTouchedInsideRect) {
            edgeTouched = getRect().findEdgeContaining(
                point = touchPoint,
                tolerance = MIN_PADDING_FOR_TOUCH_RECT
            )
        }

        lastPointTouched = touchPoint
    }

    internal fun onDrag(dragPoint: Offset) {
        lastPointTouched?.let { lastPoint ->
            when {
                isTouchedInsideRect -> {
                    handleRectDrag(dragPoint)
                }

                edgeTouched != null -> {
                    handleEdgeDrag(dragPoint, lastPoint)
                }
            }

            lastPointTouched = dragPoint
        }
    }

    internal fun onDragEnd() {
        isTouchedInsideRect = false
        edgeTouched = null
        lastPointTouched = null
    }

    private fun resetCropRect() {
        if (aspectRatio.isVariable) {
            configureVariableRect()
        } else {
            configureFixedRect()
        }
    }

    private fun configureFixedRect() {
        val (width, height) = sourceSize.width to sourceSize.height
        val aspectRatio = aspectRatio.calculateAspectRatio()

        val maxWidth = min(width, height * aspectRatio)

        val targetWidth = maxWidth * 0.85f
        val targetHeight = targetWidth / aspectRatio

        val left = initialTopLeft.x + (width - targetWidth) / 2
        val top = initialTopLeft.y + (height - targetHeight) / 2

        updateRect(
            topLeft = Offset(left, top),
            size = Size(targetWidth, targetHeight)
        )
    }

    private fun configureVariableRect() {
        val (width, height) = sourceSize.width to sourceSize.height
        val ratio = aspectRatio.calculateAspectRatio()

        val maxWidth = min(width, height * ratio)

        val targetWidth = maxWidth * 0.85f
        val targetHeight = targetWidth / ratio

        val left = initialTopLeft.x + (width - targetWidth) / 2
        val top = initialTopLeft.y + (height - targetHeight) / 2

        updateRect(
            topLeft = Offset(left, top),
            size = Size(targetWidth, targetHeight)
        )
    }

    private fun getTouchRect(): Rect {
        val insidePadding = PADDING_FOR_TOUCH_RECT * 2
        return getRect().let {
            Rect(
                offset = Offset(
                    it.topLeft.x + PADDING_FOR_TOUCH_RECT,
                    it.topLeft.y + PADDING_FOR_TOUCH_RECT
                ),
                size = Size(
                    width = it.size.width - insidePadding,
                    height = it.size.height - insidePadding
                )
            )
        }
    }

    private fun handleRectDrag(dragPoint: Offset) {
        calculateDragOffset(dragPoint)?.let { diff ->
            val newOffset = getRect().topLeft + diff
            val clampedOffset = adjustTopLeftToSource(newOffset)
            updateRect(clampedOffset)
        }
    }

    private fun handleEdgeDrag(dragPoint: Offset, lastPoint: Offset) {
        var diff = dragPoint - lastPoint

        if (!aspectRatio.isVariable) {
            val minDiff = minOf(abs(diff.x), abs(diff.y))
            diff = diff.copy(x = diff.x.sign * minDiff, y = diff.y.sign * minDiff)
        }

        when (edgeTouched) {
            Edge.TopLeft -> handleTopLeftDrag(diff)
            Edge.TopRight -> handleTopRightDrag(diff)
            Edge.BottomLeft -> handleBottomLeftDrag(diff)
            Edge.BottomRight -> handleBottomRightDrag(diff)
            else -> {
                // Do nothing
            }
        }
    }

    private fun handleTopLeftDrag(diff: Offset) {
        val topLeftX = getRect().topLeft.x + diff.x
        val topLeftY = getRect().topLeft.y + diff.y

        val width = calculateNewDimension(getRect().size.width, -diff.x, sourceSize.width)
        val height = calculateNewDimension(getRect().size.height, -diff.y, sourceSize.height)

        updateRectDimensions(topLeftX, topLeftY, width, height)
    }

    private fun handleTopRightDrag(diff: Offset) {
        val topLeftX = getRect().topLeft.x
        val topLeftY = getRect().topLeft.y + diff.y

        val width = calculateNewDimension(getRect().size.width, diff.x, sourceSize.width)
        val height = calculateNewDimension(getRect().size.height, -diff.y, sourceSize.height)

        updateRectDimensions(topLeftX, topLeftY, width, height)
    }

    private fun handleBottomLeftDrag(diff: Offset) {
        val topLeftX = getRect().topLeft.x + diff.x
        val topLeftY = getRect().topLeft.y

        val width = calculateNewDimension(getRect().size.width, -diff.x, sourceSize.width)
        val height = calculateNewDimension(getRect().size.height, diff.y, sourceSize.height)

        updateRectDimensions(topLeftX, topLeftY, width, height)
    }

    private fun handleBottomRightDrag(diff: Offset) {
        val topLeftX = getRect().topLeft.x
        val topLeftY = getRect().topLeft.y

        val width = calculateNewDimension(getRect().size.width, diff.x, sourceSize.width)
        val height = calculateNewDimension(getRect().size.height, diff.y, sourceSize.height)
        updateRectDimensions(topLeftX, topLeftY, width, height)
    }

    private fun updateRectDimensions(x: Float, y: Float, width: Float, height: Float) {
        val newSize = if (aspectRatio.isVariable) {
            Size(width, height)
        } else {
            Size(width, width / aspectRatio.calculateAspectRatio())
        }

        updateRect(
            topLeft = adjustTopLeftToSource(
                topLeft = Offset(x, y)
            ),
            size = newSize
        )
    }

    private fun calculateNewDimension(current: Float, diff: Float, limit: Float): Float {
        return (current + diff).coerceIn(MIN_PADDING_FOR_TOUCH_RECT, limit)
    }

    private fun calculateDragOffset(newPoint: Offset): Offset? {
        return lastPointTouched?.let { newPoint - it }
    }

    private fun adjustTopLeftToSource(topLeft: Offset): Offset {
        val currentSize = getRect().size
        val maxX = sourceSize.width + initialTopLeft.x - currentSize.width
        val maxY = sourceSize.height + initialTopLeft.y - currentSize.height

        return Offset(
            topLeft.x.coerceIn(initialTopLeft.x, maxX),
            topLeft.y.coerceIn(initialTopLeft.y, maxY)
        )
    }

    private fun getRect() = origamiCropRect.value

    private fun updateRect(topLeft: Offset? = null, size: Size? = null) {
        origamiCropRect.update {
            it.copy(offset = topLeft ?: it.topLeft, size = size ?: it.size)
        }
    }

    companion object {
        private const val PADDING_FOR_TOUCH_RECT = 70f
        private const val MIN_LIMIT_MULTIPLIER = 3f

        private const val MIN_PADDING_FOR_TOUCH_RECT = PADDING_FOR_TOUCH_RECT * MIN_LIMIT_MULTIPLIER
    }
}