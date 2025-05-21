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

package tech.ryadom.origami.style

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.util.fastForEach

/**
 * Origami edges
 */
fun interface OrigamiEdges {

    /**
     * Callback for drawing
     * @param scope current [DrawScope]
     * @param rect current [Rect] of [OrigamiCropArea]
     * @param colors current [OrigamiColors]
     */
    fun onDraw(scope: DrawScope, rect: Rect, colors: OrigamiColors)

    /**
     * Circle edges shape
     *
     * @param radius radius of the circle
     */
    class Circle(
        private val radius: Dp
    ) : OrigamiEdges {
        override fun onDraw(scope: DrawScope, rect: Rect, colors: OrigamiColors) {
            with(scope) {
                val edges = listOf(
                    rect.topLeft,
                    Offset(
                        x = rect.topLeft.x + rect.size.width,
                        y = rect.topLeft.y
                    ),
                    Offset(
                        x = rect.topLeft.x,
                        y = rect.topLeft.y + rect.size.height
                    ),
                    Offset(
                        x = rect.topLeft.x + rect.size.width,
                        y = rect.topLeft.y + rect.size.height
                    )
                )

                edges.fastForEach { center ->
                    drawCircle(
                        color = colors.edgesColor,
                        center = center,
                        radius = radius.toPx()
                    )
                }
            }
        }
    }

    /**
     * Rectangle edges shape
     * @param size width and height of rectangle
     * @param cornerRadius rectangle corner's radius
     */
    class Rectangle(
        private val size: DpSize,
        private val cornerRadius: Dp
    ) : OrigamiEdges {
        override fun onDraw(scope: DrawScope, rect: Rect, colors: OrigamiColors) {
            with(scope) {
                val height = this@Rectangle.size.height.toPx()
                val width = this@Rectangle.size.width.toPx()

                val halfOfWidth = width / 2
                val edges = listOf(
                    Offset(
                        x = rect.topLeft.x - halfOfWidth,
                        y = rect.topLeft.y - halfOfWidth
                    ),
                    Offset(
                        x = rect.topLeft.x + rect.size.width - halfOfWidth,
                        y = rect.topLeft.y - halfOfWidth
                    ),
                    Offset(
                        x = rect.topLeft.x - halfOfWidth,
                        y = rect.topLeft.y + rect.size.height - halfOfWidth
                    ),
                    Offset(
                        x = rect.topLeft.x + rect.size.width - halfOfWidth,
                        y = rect.topLeft.y + rect.size.height - halfOfWidth
                    )
                )

                edges.fastForEach { center ->
                    drawRoundRect(
                        color = colors.edgesColor,
                        topLeft = center,
                        size = Size(width, height),
                        cornerRadius = CornerRadius(
                            x = cornerRadius.toPx(),
                            y = cornerRadius.toPx()
                        )
                    )
                }
            }
        }
    }
}