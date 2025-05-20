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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp

/**
 * Shape of the highlighted space inside crop area
 */
fun interface OrigamiHighlightedShape {

    /**
     * Provides path to shape
     * @param rect current [Rect]
     * @return path will be drawn
     */
    fun getPath(rect: Rect): Path

    /**
     * Default rectangle shape match rect edges
     */
    object Default : OrigamiHighlightedShape {
        override fun getPath(rect: Rect): Path {
            return Path().apply {
                addRect(
                    Rect(
                        offset = rect.topLeft,
                        size = Size(
                            width = rect.size.width,
                            height = rect.size.height
                        )
                    )
                )
            }
        }
    }

    /**
     * Circle shape match rect edges
     */
    object Circle : OrigamiHighlightedShape {
        override fun getPath(rect: Rect): Path {
            return Path().apply {
                addOval(
                    Rect(
                        offset = rect.topLeft,
                        size = Size(
                            width = rect.size.width,
                            height = rect.size.height
                        )
                    )
                )
            }
        }
    }

    /**
     * Rounded rectangle shape match rect edges
     * @param cornerRadius corner's radius
     */
    class RoundedRectangle(
        private val cornerRadius: CornerRadius
    ) : OrigamiHighlightedShape {

        constructor(density: Density, cornerRadius: Dp) : this(
            with(density) {
                CornerRadius(
                    x = cornerRadius.toPx(),
                    y = cornerRadius.toPx()
                )
            }
        )

        override fun getPath(rect: Rect): Path {
            return Path().apply {
                addRoundRect(
                    RoundRect(
                        rect = Rect(
                            offset = rect.topLeft,
                            size = Size(
                                width = rect.size.width,
                                height = rect.size.height
                            )
                        ),
                        cornerRadius = cornerRadius
                    )
                )
            }
        }
    }
}