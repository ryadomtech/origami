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
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import co.touchlab.kermit.Logger
import tech.ryadom.origami.style.OrigamiAspectRatio
import tech.ryadom.origami.util.extensions.crop
import tech.ryadom.origami.util.extensions.findEdgeContaining
import tech.ryadom.origami.util.extensions.offset

/**
 * Origami cropping utils
 * @param aspectRatio [OrigamiAspectRatio]
 */
internal class OrigamiCroppingUtils(
    private val aspectRatio: OrigamiAspectRatio
) {

    internal val origamiCropRect = mutableStateOf(Rect.Zero)

    private var sourceSize: Size = Size.Zero
    private var touchRect: Rect = Rect.Zero

    private var isTouchedInsideRect: Boolean = false
    private var edgeTouched: Edge? = null
    private var origamiRectTopLeft: Offset = Offset.Zero

    private var lastPointTouched: Offset? = null

    private var maxSquareLimit: Float = 0f
    private var minSquareLimit: Float = 0f

    internal fun onGloballyPositioned(topLeft: Offset, size: IntSize) {
        sourceSize = Size(
            width = size.width.toFloat(),
            height = size.height.toFloat()
        )

        resetCropRect(topLeft)
    }

    internal fun onDragStart(touchPoint: Offset) {
        isTouchedInsideRect = touchRect.contains(touchPoint)

        if (!isTouchedInsideRect) {
            edgeTouched = origamiCropRect.value.findEdgeContaining(
                point = touchPoint,
                tolerance = MIN_PADDING_FOR_TOUCH_RECT
            )
        }

        lastPointTouched = touchPoint

        Logger.i { "Drag started $touchPoint $isTouchedInsideRect $edgeTouched" }
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

        Logger.i { "On drag  $dragPoint $isTouchedInsideRect $edgeTouched" }
    }

    internal fun onDragEnd() {
        isTouchedInsideRect = false
        edgeTouched = null
        lastPointTouched = null

        Logger.i { "On drag end" }
    }

    private fun resetCropRect(topLeft: Offset) {
        with(sourceSize) {
            if (aspectRatio.isVariable) {
                setupFreeStyleRect(topLeft, width, height)
            } else {
                setupSquareRect(topLeft, width, height)
            }
        }

        updateTouchRect()
    }

    private fun setupSquareRect(topLeft: Offset, width: Float, height: Float) {
        val minRectSide = minOf(width, height)

        maxSquareLimit = minRectSide
        minSquareLimit = maxSquareLimit * 0.3f

        val squareSize = minRectSide * 0.8f

        origamiRectTopLeft = calculateSquarePosition(topLeft, width, height, squareSize)
        origamiCropRect.value = Rect(
            offset = origamiRectTopLeft,
            size = Size(squareSize, squareSize)
        )
    }

    private fun setupFreeStyleRect(topLeft: Offset, width: Float, height: Float) {
        origamiRectTopLeft = topLeft
        origamiCropRect.value = Rect(
            offset = origamiRectTopLeft,
            size = Size(width, height)
        )
    }

    private fun calculateSquarePosition(
        topLeft: Offset,
        width: Float,
        height: Float,
        squareSize: Float
    ): Offset {
        return Offset(
            x = topLeft.x + ((width - squareSize) / SQUARE_POSITION_CALCULATION_FACTOR),
            y = topLeft.y + ((height - squareSize) / SQUARE_POSITION_CALCULATION_FACTOR)
        )
    }

    private fun updateTouchRect() {
        val insidePadding = PADDING_FOR_TOUCH_RECT * 2
        touchRect = origamiCropRect.value.let {
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
            val newOffset = origamiRectTopLeft + diff
            val clampedOffset = clampOffsetToCanvas(newOffset)
            updateIRectTopLeft(clampedOffset)
        }
    }

    private fun handleEdgeDrag(dragPoint: Offset, lastPoint: Offset) {
        val diff = dragPoint - lastPoint
        when (edgeTouched) {
            Edge.TopLeft -> handleTopLeftDrag(diff)
            Edge.TopRight -> handleTopRightDrag(diff)
            Edge.BottomLeft -> handleBottomLeftDrag(diff)
            Edge.BottomRight -> handleBottomRightDrag(diff)
            else -> Unit
        }
    }

    private fun handleTopLeftDrag(diff: Offset) {
        val x = (0f.coerceAtLeast(origamiRectTopLeft.x + diff.x))
            .coerceAtMost(sourceSize.width - MIN_PADDING_FOR_TOUCH_RECT)

        val y = (0f.coerceAtLeast(origamiRectTopLeft.y + diff.y))
            .coerceAtMost(sourceSize.height - MIN_PADDING_FOR_TOUCH_RECT)

        val newWidth = calculateNewDimension(origamiCropRect.value.size.width, -diff.x)
        val newHeight = calculateNewDimension(origamiCropRect.value.size.height, -diff.y)

        updateRectDimensions(x, y, newWidth, newHeight)
    }

    private fun handleTopRightDrag(diff: Offset) {
        val currentSize = origamiCropRect.value.size
        val newWidth =
            (currentSize.width + diff.x).coerceIn(
                MIN_PADDING_FOR_TOUCH_RECT,
                sourceSize.width - origamiRectTopLeft.x
            )
        val newHeight =
            (currentSize.height - diff.y).coerceIn(
                MIN_PADDING_FOR_TOUCH_RECT,
                sourceSize.height - origamiRectTopLeft.y
            )

        if (aspectRatio.isVariable) {
            origamiCropRect.value = Rect(
                offset = origamiCropRect.value.offset,
                size = Size(newWidth, newHeight)
            )


        } else {
            val squareSize = minOf(newWidth, newHeight).coerceAtLeast(MIN_PADDING_FOR_TOUCH_RECT)
            adjustSquareVerticalPosition(squareSize)
            origamiCropRect.value = Rect(
                offset = origamiCropRect.value.offset,
                size = Size(squareSize, squareSize)
            )
        }
        updateTouchRect()
    }

    // norm
    private fun handleBottomLeftDrag(diff: Offset) {
        val newWidth =
            (origamiCropRect.value.size.width - diff.x).coerceAtLeast(MIN_PADDING_FOR_TOUCH_RECT)
        val newHeight = (origamiCropRect.value.size.height + diff.y).coerceIn(
            MIN_PADDING_FOR_TOUCH_RECT,
            sourceSize.height - origamiRectTopLeft.y
        )

        if (aspectRatio.isVariable) {
            origamiCropRect.value = Rect(
                offset = origamiRectTopLeft,
                size = Size(newWidth, newHeight)
            )
        } else {
            val squareSize = minOf(newWidth, newHeight).coerceAtLeast(MIN_PADDING_FOR_TOUCH_RECT)
            adjustSquareVerticalPosition(squareSize)
            origamiCropRect.value = Rect(
                offset = origamiRectTopLeft,
                size = Size(squareSize, squareSize)
            )
        }
        updateTouchRect()
    }

    private fun handleBottomRightDrag(diff: Offset) {
        val newWidth = (origamiCropRect.value.size.width + diff.x).coerceIn(
            MIN_PADDING_FOR_TOUCH_RECT,
            sourceSize.width - origamiRectTopLeft.x
        )
        val newHeight = (origamiCropRect.value.size.height + diff.y).coerceIn(
            MIN_PADDING_FOR_TOUCH_RECT,
            sourceSize.height - origamiRectTopLeft.y
        )

        if (aspectRatio.isVariable) {
            origamiCropRect.value = Rect(
                offset = origamiCropRect.value.offset,
                size = Size(newWidth, newHeight)
            )
        } else {
            val squareSize = minOf(newWidth, newHeight).coerceAtLeast(MIN_PADDING_FOR_TOUCH_RECT)
            adjustSquareVerticalPosition(squareSize)
            origamiCropRect.value = Rect(
                offset = origamiCropRect.value.offset,
                size = Size(squareSize, squareSize)
            )
        }
        updateTouchRect()
    }

    private fun updateRectDimensions(x: Float, y: Float, width: Float, height: Float) {
        origamiRectTopLeft = Offset(x, y)
        val newSize = if (aspectRatio.isVariable) {
            Size(
                width.coerceAtLeast(MIN_PADDING_FOR_TOUCH_RECT),
                height.coerceAtLeast(MIN_PADDING_FOR_TOUCH_RECT)
            )
        } else {
            val squareSize = minOf(width, height).coerceAtLeast(MIN_PADDING_FOR_TOUCH_RECT)
            adjustSquareVerticalPosition(squareSize)
            Size(squareSize, squareSize)
        }
        origamiCropRect.value = Rect(
            offset = origamiRectTopLeft,
            size = newSize
        )

        updateTouchRect()
    }

    private fun adjustSquareVerticalPosition(squareSize: Float) {
        val totalHeight = origamiRectTopLeft.y + squareSize
        val heightDiff = sourceSize.height - totalHeight
        if (heightDiff < 0) {
            origamiRectTopLeft = origamiRectTopLeft.copy(y = origamiRectTopLeft.y + heightDiff)
        }
    }

    private fun calculateNewDimension(current: Float, diff: Float): Float {
        return (current + diff).coerceIn(MIN_PADDING_FOR_TOUCH_RECT, sourceSize.width)
    }

    private fun calculateDragOffset(newPoint: Offset): Offset? {
        return lastPointTouched?.let {
            Offset(newPoint.x - it.x, newPoint.y - it.y)
        }
    }

    private fun clampOffsetToCanvas(offset: Offset): Offset {
        val maxX = sourceSize.width - origamiCropRect.value.size.width
        val maxY = sourceSize.height - origamiCropRect.value.size.height
        return Offset(
            offset.x.coerceIn(0f, maxX),
            offset.y.coerceIn(0f, maxY)
        )
    }

    private fun updateIRectTopLeft(newOffset: Offset) {
        origamiRectTopLeft = newOffset
        origamiCropRect.value = Rect(offset = newOffset, size = origamiCropRect.value.size)
        updateTouchRect()
    }

    fun cropImage(bitmapImage: ImageBitmap): ImageBitmap {
        return bitmapImage
            .cropToRect(getCropRect())
        // .scaleToFinalSize()
    }

    private fun ImageBitmap.scaleToCanvas() = this

    private fun getCropRect(): IntRect {
        with(origamiCropRect.value) {
            Logger.i("ree $topLeft $origamiRectTopLeft ${size.width} ${size.height}")
            return IntRect(
                left = topLeft.x.toInt() - origamiRectTopLeft.x.toInt(),
                top = topLeft.y.toInt() - origamiRectTopLeft.y.toInt(),
                right = (topLeft.x + size.width).toInt() - origamiRectTopLeft.x.toInt(),
                bottom = (topLeft.y + size.height).toInt() - origamiRectTopLeft.y.toInt()
            ).coerceInCanvasBounds()
        }
    }

    private fun IntRect.coerceInCanvasBounds(): IntRect {
        Logger.i("before corcing $this")
        val clampedLeft = left.coerceAtLeast(0)
        val clampedTop = top.coerceAtLeast(0)
        val clampedRight = right.coerceAtMost(sourceSize.width.toInt())
        val clampedBottom = bottom.coerceAtMost(sourceSize.height.toInt())
        return IntRect(clampedLeft, clampedTop, clampedRight, clampedBottom)
    }

    private fun ImageBitmap.cropToRect(rect: IntRect): ImageBitmap {
        Logger.i("crop rect $rect")
        return if (rect.width > 0 && rect.height > 0) crop(rect) else this
    }

    companion object {
        private const val PADDING_FOR_TOUCH_RECT = 70f
        private const val MIN_LIMIT_MULTIPLIER = 3f
        private const val SIZE_REDUCTION = 100f
        private const val SQUARE_POSITION_CALCULATION_FACTOR = 2f

        private const val MIN_PADDING_FOR_TOUCH_RECT = PADDING_FOR_TOUCH_RECT * MIN_LIMIT_MULTIPLIER
    }
}