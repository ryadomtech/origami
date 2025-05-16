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

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

/**
 * Class representing current crop area rectangle
 *
 * @param topLeft top left edge's offset
 * @param size crop area rectangle size
 */
data class OrigamiCropRect(
    val topLeft: Offset = Offset(0.0f, 0.0f),
    val size: Size = Size(0.0f, 0.0f)
) {

    fun getEdgeContaining(point: Offset, tolerance: Float) = when {
        isPointInRegion(point, topLeft, tolerance) -> Edges.TopLeft
        isPointInRegion(point, topRight, tolerance) -> Edges.TopRight
        isPointInRegion(point, bottomLeft, tolerance) -> Edges.BottomLeft
        isPointInRegion(point, bottomRight, tolerance) -> Edges.BottomRight
        else -> null
    }

    fun contains(point: Offset): Boolean {
        return point.x in topLeft.x..(topLeft.x + size.width) &&
                point.y in topLeft.y..(topLeft.y + size.height)
    }

    private val topRight: Offset = topLeft.copy(x = topLeft.x + size.width)

    private val bottomRight: Offset = Offset(
        x = topLeft.x + size.width,
        y = topLeft.y + size.height
    )

    private val bottomLeft: Offset = topLeft.copy(y = topLeft.y + size.height)

    private fun isPointInRegion(point: Offset, corner: Offset, tolerance: Float): Boolean {
        return point.x in (corner.x - tolerance)..(corner.x + tolerance) &&
                point.y in (corner.y - tolerance)..(corner.y + tolerance)
    }
}