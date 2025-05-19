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

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tech.ryadom.origami.style.OrigamiColors
import tech.ryadom.origami.style.OrigamiCropArea
import tech.ryadom.origami.util.OrigamiCropRect

/**
 * Composable for origami cropping component
 * @param origami [Origami] instance to hold and manage component state
 * @param modifier [Modifier]
 * @param colors [OrigamiColors]
 * @param cropArea [OrigamiCropArea]
 */
@Composable
fun OrigamiImage(
    origami: Origami,
    modifier: Modifier = Modifier,
    colors: OrigamiColors = OrigamiColors.defaults(),
    cropArea: OrigamiCropArea = OrigamiCropArea()
) {
    val origamiCropRect by remember { origami.prepare(cropArea) }

    Box(
        modifier = modifier.fillMaxSize()
            .onSizeChanged { intSize ->
                origami.onCanvasSizeChanged(intSize = intSize)
            }
    ) {
        origami.source.Content(
            modifier = Modifier.fillMaxSize()
                .align(Alignment.Center)
        )

        Canvas(
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
                },
            onDraw = {
                clipPath(
                    path = cropArea.highlightedShape.getPath(origamiCropRect),
                    clipOp = ClipOp.Difference
                ) {
                    drawRect(
                        SolidColor(colors.backgroundColor)
                    )
                }

                drawCropArea(
                    guidelinesColor = colors.guidelinesColor,
                    guidelinesWidth = cropArea.guidelinesWidth,
                    origamiCropRect = origamiCropRect,
                    guidelinesCount = cropArea.guidelinesCount
                )

                if (cropArea.edges != null) {
                    cropArea.edges.onDraw(this, origamiCropRect, colors)
                }
            }
        )
    }
}

private fun DrawScope.drawCropArea(
    guidelinesColor: Color,
    guidelinesWidth: Dp,
    guidelinesCount: Int,
    origamiCropRect: OrigamiCropRect
) = with(origamiCropRect) {
    drawRect(
        color = guidelinesColor,
        topLeft = topLeft,
        size = size,
        style = Stroke(guidelinesWidth.toPx())
    )

    if (guidelinesWidth > 0.dp && guidelinesCount > 0) {
        drawGuidelines(
            guidelinesCount = guidelinesCount,
            guidelinesColor = guidelinesColor,
            guidelinesWidth = guidelinesWidth,
            origamiCropRect = origamiCropRect
        )
    }
}

private fun DrawScope.drawGuidelines(
    guidelinesCount: Int,
    guidelinesColor: Color,
    guidelinesWidth: Dp,
    origamiCropRect: OrigamiCropRect
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