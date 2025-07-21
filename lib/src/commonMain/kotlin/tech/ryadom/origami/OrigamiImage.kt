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

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.unit.Dp
import tech.ryadom.origami.style.OrigamiCropArea

/**
 * Composable for origami cropping component
 * @param modifier [Modifier]
 * @param origami [Origami] instance to hold and manage component state
 */
@Composable
fun OrigamiImage(
    modifier: Modifier = Modifier,
    origami: Origami
) {
    // State of crop rect.
    val origamiCropRect by origami.cropRect

    Box(modifier = modifier) {
        // Draw image source
        origami.source.Content(
            modifier = Modifier
                .align(Alignment.Center)
                .onGloballyPositioned {
                    origami.onGloballyPositioned(
                        size = it.size,
                        topLeft = it.positionInParent()
                    )
                }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { touchPoint ->
                            origami.onDragStart(touchPoint)
                        },
                        onDrag = { pointerInputChange, _ ->
                            pointerInputChange.consume()

                            val dragPoint = pointerInputChange.position
                            origami.onDrag(dragPoint)
                        },
                        onDragEnd = {
                            origami.onDragEnd()
                        }
                    )
                }
                .drawWithCache {
                    onDrawBehind {
                        clipPath(
                            path = origami.cropArea.highlightedShape.getPath(origamiCropRect),
                            clipOp = ClipOp.Difference
                        ) {
                            drawRect(
                                brush = SolidColor(origami.colors.backgroundColor)
                            )
                        }

                        // Draw crop area
                        drawCropArea(
                            guidelinesColor = origami.colors.guidelinesColor,
                            guidelinesWidth = origami.cropArea.guidelinesWidth,
                            guidelinesCount = origami.cropArea.guidelinesCount,
                            origamiCropRect = origamiCropRect
                        )

                        // Draw edges if necessary
                        origami.cropArea.edges?.onDraw(
                            scope = this,
                            rect = origamiCropRect,
                            colors = origami.colors
                        )
                    }
                }
        )
    }
}

/**
 * Drawing crop area
 * @param guidelinesColor guidelines color
 * @param guidelinesWidth guidelines width
 * @param guidelinesCount guidelines count
 * @param origamiCropRect [Rect]
 *
 * @see [OrigamiCropArea]
 */
private fun DrawScope.drawCropArea(
    guidelinesColor: Color,
    guidelinesWidth: Dp,
    guidelinesCount: Int,
    origamiCropRect: Rect
) = with(origamiCropRect) {
    val guidelinesWidthPx = guidelinesWidth.toPx()
    drawRect(
        color = guidelinesColor,
        topLeft = topLeft,
        size = size,
        style = Stroke(width = guidelinesWidthPx)
    )

    // Not drawing invisible guidelines
    if (guidelinesWidthPx <= 0 || guidelinesCount <= 0 || guidelinesColor == Color.Transparent) {
        return@with
    }

    drawGuidelines(
        guidelinesCount = guidelinesCount,
        guidelinesColor = guidelinesColor,
        guidelinesWidth = guidelinesWidth,
        origamiCropRect = origamiCropRect
    )
}

/**
 * Drawing guidelines inside crop area
 * @param guidelinesColor guidelines color
 * @param guidelinesWidth guidelines width
 * @param guidelinesCount guidelines count
 * @param origamiCropRect [Rect]
 *
 * @see drawCropArea
 * @see [OrigamiCropArea]
 */
private fun DrawScope.drawGuidelines(
    guidelinesCount: Int,
    guidelinesColor: Color,
    guidelinesWidth: Dp,
    origamiCropRect: Rect
) = with(origamiCropRect) {
    val strokeWidth = guidelinesWidth.toPx()

    val verticalStep = size.height / (guidelinesCount + 1)
    val horizontalStep = size.width / (guidelinesCount + 1)

    repeat(guidelinesCount) { index ->
        val lineIndex = index + 1

        drawLine(
            color = guidelinesColor,
            start = Offset(topLeft.x + horizontalStep * lineIndex, topLeft.y),
            end = Offset(topLeft.x + horizontalStep * lineIndex, topLeft.y + size.height),
            strokeWidth = strokeWidth
        )

        drawLine(
            color = guidelinesColor,
            start = Offset(topLeft.x, topLeft.y + verticalStep * lineIndex),
            end = Offset(topLeft.x + size.width, topLeft.y + verticalStep * lineIndex),
            strokeWidth = strokeWidth
        )
    }
}