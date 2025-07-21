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

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.roundToIntRect
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.util.fastCoerceIn
import tech.ryadom.origami.style.OrigamiAspectRatio
import tech.ryadom.origami.style.OrigamiColors
import tech.ryadom.origami.style.OrigamiCropArea
import tech.ryadom.origami.util.BitmapSource
import tech.ryadom.origami.util.Edge
import tech.ryadom.origami.util.OrigamiSource
import tech.ryadom.origami.util.PainterSource
import tech.ryadom.origami.util.extensions.copy
import tech.ryadom.origami.util.extensions.crop
import tech.ryadom.origami.util.extensions.findEdgeContaining
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private const val TouchAreaPadding = 40f
private const val MinTouchAreaPadding = TouchAreaPadding * 3f

/**
 * Origami [OrigamiSource] state holder
 *
 * @param source image [OrigamiSource]
 * @param colors [OrigamiColors]
 * @param cropArea [OrigamiCropArea]
 * @param aspectRatio [OrigamiAspectRatio]
 */
class Origami(
    internal val source: OrigamiSource,
    internal val colors: OrigamiColors,
    internal val cropArea: OrigamiCropArea,
    internal val origamiAspectRatio: OrigamiAspectRatio
) {

    internal val cropRect = mutableStateOf(Rect.Zero)

    private var sourceSize: Size = Size.Zero

    private var isTouchedInsideArea: Boolean = false
    private var areaEdgeTouched: Edge? = null

    private var lastPointTouched: Offset? = null

    private var initialTopLeft = Offset.Zero

    private val aspectRatio = origamiAspectRatio.aspectRatio

    constructor(
        imageBitmap: ImageBitmap,
        colors: OrigamiColors = OrigamiColors.createDefault(),
        cropArea: OrigamiCropArea = OrigamiCropArea(),
        aspectRatio: OrigamiAspectRatio = OrigamiAspectRatio()
    ) : this(BitmapSource(imageBitmap), colors, cropArea, aspectRatio)

    constructor(
        painter: Painter,
        density: Density,
        layoutDirection: LayoutDirection,
        colors: OrigamiColors = OrigamiColors.createDefault(),
        cropArea: OrigamiCropArea = OrigamiCropArea(),
        aspectRatio: OrigamiAspectRatio = OrigamiAspectRatio()
    ) : this(PainterSource(painter, density, layoutDirection), colors, cropArea, aspectRatio)

    /**
     * Cropping [source] to crop area
     * @return cropped [source]
     */
    fun crop(): ImageBitmap {
        val bitmapImage = source.getImageBitmap()
        val bitmapWidth = bitmapImage.width.toFloat()
        val bitmapHeight = bitmapImage.height.toFloat()

        if (bitmapWidth <= 0 || bitmapHeight <= 0) {
            return bitmapImage
        }

        val composeWidth = sourceSize.width
        val composeHeight = sourceSize.height

        val scale = min(composeWidth / bitmapWidth, composeHeight / bitmapHeight)
        val scaledWidth = bitmapWidth * scale
        val scaledHeight = bitmapHeight * scale

        val imageOffsetX = (composeWidth - scaledWidth) / 2
        val imageOffsetY = (composeHeight - scaledHeight) / 2

        val imageInBox = initialTopLeft + Offset(imageOffsetX, imageOffsetY)

        val cropRect = getCropRect()

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

    /**
     * Image globally positioned callback
     * @param size new size
     * @param topLeft top left [Offset]
     */
    internal fun onGloballyPositioned(topLeft: Offset, size: IntSize) {
        initialTopLeft = topLeft
        sourceSize = size.toSize()
        resetCropArea()
    }

    /**
     * Dragging start callback
     * @param touchPoint initial point
     */
    internal fun onDragStart(touchPoint: Offset) {
        isTouchedInsideArea = getTouchArea().contains(touchPoint)

        if (!isTouchedInsideArea) {
            areaEdgeTouched = getCropRect().findEdgeContaining(
                point = touchPoint,
                tolerance = MinTouchAreaPadding
            )
        }

        lastPointTouched = touchPoint
    }

    /**
     * Dragging callback
     * @param dragPoint drag point
     */
    internal fun onDrag(dragPoint: Offset) {
        lastPointTouched?.let { lastPoint ->
            when {
                isTouchedInsideArea -> handleAreaDrag(dragPoint)
                areaEdgeTouched != null -> handleEdgeDrag(dragPoint, lastPoint)
            }

            lastPointTouched = dragPoint
        }
    }

    /**
     * Dragging end callback
     */
    internal fun onDragEnd() {
        isTouchedInsideArea = false
        areaEdgeTouched = null
        lastPointTouched = null
    }

    private fun resetCropArea() {
        val maxWidth = min(sourceSize.width, sourceSize.height * aspectRatio)

        val targetWidth = cropArea.initialPaddings.getFactor(maxWidth)
        val targetHeight = targetWidth / aspectRatio

        val left = initialTopLeft.x + (sourceSize.width - targetWidth) / 2
        val top = initialTopLeft.y + (sourceSize.height - targetHeight) / 2

        updateCropRect(
            Rect(
                offset = Offset(left, top),
                size = Size(targetWidth, targetHeight)
            )
        )
    }

    private fun getTouchArea(): Rect {
        val insidePadding = TouchAreaPadding * 2
        val rect = getCropRect()

        return Rect(
            offset = rect.topLeft + Offset(TouchAreaPadding, TouchAreaPadding),
            size = Size(
                width = rect.size.width - insidePadding,
                height = rect.size.height - insidePadding
            )
        )
    }

    private fun handleAreaDrag(dragPoint: Offset) {
        calculateDragOffset(dragPoint)?.let { offset ->
            val rect = getCropRect()
            val newOffset = adjustTopLeftToSource(rect.topLeft + offset)
            updateCropRect(
                rect.copy(offset = newOffset)
            )
        }
    }

    private fun handleEdgeDrag(dragPoint: Offset, lastPoint: Offset) {
        areaEdgeTouched?.let {
            val diff = dragPoint - lastPoint
            dragEdge(it, diff.x, diff.y)
        }
    }

    private fun dragEdge(edge: Edge, dx: Float, dy: Float) {
        var rect = getCropRect()
        if (origamiAspectRatio.isVariable) {
            rect = when (edge) {
                Edge.TopLeft -> rect.copy(left = rect.left + dx, top = rect.top + dy)
                Edge.TopRight -> rect.copy(right = rect.right + dx, top = rect.top + dy)
                Edge.BottomRight -> rect.copy(right = rect.right + dx, bottom = rect.bottom + dy)
                Edge.BottomLeft -> rect.copy(left = rect.left + dx, bottom = rect.bottom + dy)
            }

            val adjustedOffset = adjustTopLeftToSource(rect.topLeft)
            updateCropRect(
                rect.copy(
                    offset = adjustedOffset,
                    size = rect.size.copy(
                        width = rect.width.fastCoerceIn(MinTouchAreaPadding, sourceSize.width),
                        height = rect.height.fastCoerceIn(MinTouchAreaPadding, sourceSize.height)
                    )
                )
            )

            return
        }

        val (x0, y0) = when (edge) {
            Edge.TopLeft -> (rect.left + dx) to (rect.top + dy)
            Edge.TopRight -> (rect.right + dx) to (rect.top + dy)
            Edge.BottomRight -> (rect.right + dx) to (rect.bottom + dy)
            Edge.BottomLeft -> (rect.left + dx) to (rect.bottom + dy)
        }

        val (minWidth, minHeight) = MinTouchAreaPadding to (MinTouchAreaPadding / aspectRatio)
        val (maxWidth, maxHeight) = sourceSize.width to sourceSize.height
        val (anchor, dir) = when (edge) {
            Edge.TopLeft -> Offset(rect.right, rect.bottom) to Offset(-aspectRatio, -1f)
            Edge.TopRight -> Offset(rect.left, rect.bottom) to Offset(aspectRatio, -1f)
            Edge.BottomRight -> Offset(rect.left, rect.top) to Offset(aspectRatio, 1f)
            Edge.BottomLeft -> Offset(rect.right, rect.top) to Offset(-aspectRatio, 1f)
        }

        val vx = x0 - anchor.x
        val vy = y0 - anchor.y

        val numerator = vx * dir.x + vy * dir.y
        val denominator = dir.x * dir.x + dir.y * dir.y

        val absDirX = abs(dir.x)
        val absDirY = abs(dir.y)

        val tMinWidth = minWidth / absDirX
        val tMinHeight = minHeight / absDirY
        val tMin = max(tMinWidth, tMinHeight)

        val tMaxWidth = maxWidth / absDirX
        val tMaxHeight = maxHeight / absDirY
        val tMax = min(tMaxWidth, tMaxHeight)

        val t = (numerator / denominator).fastCoerceIn(tMin, tMax)

        val newEdgeX = anchor.x + dir.x * t
        val newEdgeY = anchor.y + dir.y * t

        rect = when (edge) {
            Edge.TopLeft -> rect.copy(left = newEdgeX, top = newEdgeY)
            Edge.TopRight -> rect.copy(right = newEdgeX, top = newEdgeY)
            Edge.BottomRight -> rect.copy(right = newEdgeX, bottom = newEdgeY)
            Edge.BottomLeft -> rect.copy(left = newEdgeX, bottom = newEdgeY)
        }

        val maxLeftX = sourceSize.width + initialTopLeft.x - rect.width
        val maxTopY = sourceSize.height + initialTopLeft.y - rect.height

        updateCropRect(
            rect.copy(
                left = rect.left.fastCoerceIn(initialTopLeft.x, maxLeftX),
                top = rect.top.fastCoerceIn(initialTopLeft.y, maxTopY)
            )
        )
    }

    private fun calculateDragOffset(newPoint: Offset): Offset? {
        return lastPointTouched?.let { newPoint - it }
    }

    private fun adjustTopLeftToSource(topLeft: Offset): Offset {
        val currentSize = getCropRect().size
        val maxX = sourceSize.width + initialTopLeft.x - currentSize.width
        val maxY = sourceSize.height + initialTopLeft.y - currentSize.height

        return Offset(
            topLeft.x.fastCoerceIn(initialTopLeft.x, maxX),
            topLeft.y.fastCoerceIn(initialTopLeft.y, maxY)
        )
    }

    private fun getCropRect(): Rect {
        return cropRect.value
    }

    private fun updateCropRect(rect: Rect) {
        cropRect.value = rect
    }
}