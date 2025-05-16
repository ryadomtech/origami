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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import tech.ryadom.origami.style.OrigamiAspectRatio
import tech.ryadom.origami.util.extensions.crop
import tech.ryadom.origami.util.extensions.scale

class OrigamiCroppingUtils(
    private val aspectRatio: OrigamiAspectRatio
) {
    var canvasSize: Size by mutableStateOf(Size.Zero)
        private set

    var origamiCropRect: OrigamiCropRect by mutableStateOf(OrigamiCropRect())
        private set

    private var touchRect: OrigamiCropRect by mutableStateOf(OrigamiCropRect())
    private var isTouchedInsideRectMove: Boolean by mutableStateOf(false)
    private var edgesTouched: Edges? by mutableStateOf(null)
    private var iRectTopLeft: Offset by mutableStateOf(Offset.Zero)
    private var lastPointUpdated: Offset? by mutableStateOf(null)

    private val minLimit: Float = PADDING_FOR_TOUCH_RECT * MIN_LIMIT_MULTIPLIER
    private var maxSquareLimit: Float = 0f
    private var minSquareLimit: Float = 0f

    init {
        resetCropRect()
    }

    fun onCanvasSizeChanged(intSize: IntSize) {
        canvasSize = Size(intSize.width.toFloat(), intSize.height.toFloat())
        resetCropRect()
    }

    private fun resetCropRect() {
        with(canvasSize) {
            if (!aspectRatio.isVariable) {
                setupSquareRect(width, height)
            } else {
                setupFreeStyleRect(width, height)
            }
        }

        updateTouchRect()
    }

    private fun setupSquareRect(width: Float, height: Float) {
        val squareSize = minOf(width, height) - SIZE_REDUCTION
        maxSquareLimit = squareSize + SIZE_REDUCTION
        minSquareLimit = maxSquareLimit * 0.3f

        iRectTopLeft = calculateSquarePosition(width, height, squareSize)
        origamiCropRect = OrigamiCropRect(iRectTopLeft, Size(squareSize, squareSize))
    }

    private fun setupFreeStyleRect(width: Float, height: Float) {
        iRectTopLeft = Offset.Zero
        origamiCropRect = OrigamiCropRect(Offset.Zero, Size(width, height))
    }

    private fun calculateSquarePosition(width: Float, height: Float, squareSize: Float): Offset {
        val x = (width - squareSize) / SQUARE_POSITION_CALCULATION_FACTOR
        val y = (height - squareSize) / SQUARE_POSITION_CALCULATION_FACTOR
        return Offset(x, y)
    }

    private fun updateTouchRect() {
        val insidePadding = PADDING_FOR_TOUCH_RECT * 2
        touchRect = origamiCropRect.let {
            val newOffset = Offset(
                it.topLeft.x + PADDING_FOR_TOUCH_RECT,
                it.topLeft.y + PADDING_FOR_TOUCH_RECT
            )
            OrigamiCropRect(
                newOffset,
                Size(
                    it.size.width - insidePadding,
                    it.size.height - insidePadding
                )
            )
        }
    }

    fun onDragStart(touchPoint: Offset) {
        isTouchedInsideRectMove = touchRect.contains(touchPoint)
        edgesTouched = origamiCropRect.getEdgeContaining(touchPoint, minLimit)
        lastPointUpdated = touchPoint
    }

    fun onDrag(dragPoint: Offset) {
        lastPointUpdated?.let { lastPoint ->
            when {
                isTouchedInsideRectMove -> handleRectDrag(dragPoint)
                edgesTouched != null -> handleEdgeDrag(dragPoint, lastPoint)
            }
            lastPointUpdated = dragPoint
        }
    }

    fun onDragEnd() {
        isTouchedInsideRectMove = false
        edgesTouched = null
        lastPointUpdated = null
    }

    private fun handleRectDrag(dragPoint: Offset) {
        calculateDragOffset(dragPoint)?.let { diff ->
            val newOffset = iRectTopLeft + diff
            val clampedOffset = clampOffsetToCanvas(newOffset)
            updateIRectTopLeft(clampedOffset)
        }
    }

    private fun handleEdgeDrag(dragPoint: Offset, lastPoint: Offset) {
        val diff = dragPoint - lastPoint
        when (edgesTouched) {
            Edges.TopLeft -> handleTopLeftDrag(diff)
            Edges.TopRight -> handleTopRightDrag(diff)
            Edges.BottomLeft -> handleBottomLeftDrag(diff)
            Edges.BottomRight -> handleBottomRightDrag(diff)
            else -> Unit
        }
    }

    private fun handleTopLeftDrag(diff: Offset) {
        val newX = (iRectTopLeft.x + diff.x).coerceIn(0f, canvasSize.width - minLimit)
        val newY = (iRectTopLeft.y + diff.y).coerceIn(0f, canvasSize.height - minLimit)

        val newWidth = calculateNewDimension(origamiCropRect.size.width, -diff.x)
        val newHeight = calculateNewDimension(origamiCropRect.size.height, -diff.y)

        updateRectDimensions(newX, newY, newWidth, newHeight)
    }

    private fun handleTopRightDrag(diff: Offset) {
        val currentSize = origamiCropRect.size
        val newWidth =
            (currentSize.width + diff.x).coerceIn(minLimit, canvasSize.width - iRectTopLeft.x)
        val newHeight =
            (currentSize.height - diff.y).coerceIn(minLimit, canvasSize.height - iRectTopLeft.y)

        if (aspectRatio.isVariable) {
            origamiCropRect = origamiCropRect.copy(size = Size(newWidth, newHeight))
        } else {
            val squareSize = minOf(newWidth, newHeight).coerceAtLeast(minLimit)
            adjustSquareVerticalPosition(squareSize)
            origamiCropRect = origamiCropRect.copy(size = Size(squareSize, squareSize))
        }
        updateTouchRect()
    }

    private fun handleBottomLeftDrag(diff: Offset) {
        val newX = (iRectTopLeft.x + diff.x).coerceIn(0f, canvasSize.width - minLimit)
        val newWidth = (origamiCropRect.size.width - diff.x).coerceAtLeast(minLimit)
        val newHeight = (origamiCropRect.size.height + diff.y).coerceIn(
            minLimit,
            canvasSize.height - iRectTopLeft.y
        )

        iRectTopLeft = Offset(newX, iRectTopLeft.y)

        if (aspectRatio.isVariable) {
            origamiCropRect = origamiCropRect.copy(
                topLeft = iRectTopLeft,
                size = Size(newWidth, newHeight)
            )
        } else {
            val squareSize = minOf(newWidth, newHeight).coerceAtLeast(minLimit)
            adjustSquareVerticalPosition(squareSize)
            origamiCropRect = origamiCropRect.copy(
                topLeft = iRectTopLeft,
                size = Size(squareSize, squareSize)
            )
        }
        updateTouchRect()
    }

    private fun handleBottomRightDrag(diff: Offset) {
        val newWidth = (origamiCropRect.size.width + diff.x).coerceIn(
            minLimit,
            canvasSize.width - iRectTopLeft.x
        )
        val newHeight = (origamiCropRect.size.height + diff.y).coerceIn(
            minLimit,
            canvasSize.height - iRectTopLeft.y
        )

        if (aspectRatio.isVariable) {
            origamiCropRect = origamiCropRect.copy(size = Size(newWidth, newHeight))
        } else {
            val squareSize = minOf(newWidth, newHeight).coerceAtLeast(minLimit)
            adjustSquareVerticalPosition(squareSize)
            origamiCropRect = origamiCropRect.copy(size = Size(squareSize, squareSize))
        }
        updateTouchRect()
    }

    private fun updateRectDimensions(x: Float, y: Float, width: Float, height: Float) {
        iRectTopLeft = Offset(x, y)
        val newSize = if (aspectRatio.isVariable) {
            Size(width.coerceAtLeast(minLimit), height.coerceAtLeast(minLimit))
        } else {
            val squareSize = minOf(width, height).coerceAtLeast(minLimit)
            adjustSquareVerticalPosition(squareSize)
            Size(squareSize, squareSize)
        }
        origamiCropRect = origamiCropRect.copy(topLeft = iRectTopLeft, size = newSize)
        updateTouchRect()
    }

    private fun adjustSquareVerticalPosition(squareSize: Float) {
        val totalHeight = iRectTopLeft.y + squareSize
        val heightDiff = canvasSize.height - totalHeight
        if (heightDiff < 0) {
            iRectTopLeft = iRectTopLeft.copy(y = iRectTopLeft.y + heightDiff)
        }
    }

    private fun calculateNewDimension(current: Float, diff: Float): Float {
        return (current + diff).coerceIn(minLimit, canvasSize.width)
    }

    private fun calculateDragOffset(newPoint: Offset): Offset? {
        return lastPointUpdated?.let {
            Offset(newPoint.x - it.x, newPoint.y - it.y)
        }
    }

    private fun clampOffsetToCanvas(offset: Offset): Offset {
        val maxX = canvasSize.width - origamiCropRect.size.width
        val maxY = canvasSize.height - origamiCropRect.size.height
        return Offset(
            offset.x.coerceIn(0f, maxX),
            offset.y.coerceIn(0f, maxY)
        )
    }

    private fun updateIRectTopLeft(newOffset: Offset) {
        iRectTopLeft = newOffset
        origamiCropRect = origamiCropRect.copy(topLeft = newOffset)
        updateTouchRect()
    }

    fun cropImage(bitmapImage: ImageBitmap): ImageBitmap {
        if (canvasSize.isEmpty()) return bitmapImage

        return bitmapImage.scaleToCanvas()
            .cropToRect(getCropRect())
            .scaleToFinalSize()
    }

    private fun ImageBitmap.scaleToCanvas() =
        scale(canvasSize.width.toInt(), canvasSize.height.toInt())

    private fun getCropRect(): IntRect {
        with(origamiCropRect) {
            return IntRect(
                left = topLeft.x.toInt(),
                top = topLeft.y.toInt(),
                right = (topLeft.x + size.width).toInt(),
                bottom = (topLeft.y + size.height).toInt()
            ).coerceInCanvasBounds()
        }
    }

    private fun IntRect.coerceInCanvasBounds(): IntRect {
        val clampedLeft = left.coerceAtLeast(0)
        val clampedTop = top.coerceAtLeast(0)
        val clampedRight = right.coerceAtMost(canvasSize.width.toInt())
        val clampedBottom = bottom.coerceAtMost(canvasSize.height.toInt())
        return IntRect(clampedLeft, clampedTop, clampedRight, clampedBottom)
    }

    private fun ImageBitmap.cropToRect(rect: IntRect): ImageBitmap {
        return if (rect.width > 0 && rect.height > 0) crop(rect) else this
    }

    private fun ImageBitmap.scaleToFinalSize(): ImageBitmap {
        return if (!aspectRatio.isVariable) {
            scale(maxSquareLimit.toInt(), maxSquareLimit.toInt())
        } else {
            this
        }
    }

    private companion object {
        const val PADDING_FOR_TOUCH_RECT = 70f
        const val MIN_LIMIT_MULTIPLIER = 3f
        const val SIZE_REDUCTION = 100f
        const val SQUARE_POSITION_CALCULATION_FACTOR = 2f
    }
}