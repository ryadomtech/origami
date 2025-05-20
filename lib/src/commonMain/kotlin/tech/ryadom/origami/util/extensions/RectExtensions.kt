package tech.ryadom.origami.util.extensions

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import tech.ryadom.origami.util.Edge

internal val Rect.offset get() = topLeft

internal fun Rect.findEdgeContaining(point: Offset, tolerance: Float): Edge? {
    return when {
        isPointInRegion(point, topLeft, tolerance) -> Edge.TopLeft
        isPointInRegion(point, topRight, tolerance) -> Edge.TopRight
        isPointInRegion(point, bottomLeft, tolerance) -> Edge.BottomLeft
        isPointInRegion(point, bottomRight, tolerance) -> Edge.BottomRight
        else -> null
    }
}

// Checking point in given region
private fun isPointInRegion(
    point: Offset,
    region: Offset,
    tolerance: Float = 0f
): Boolean {
    return point.x in (region.x - tolerance)..(region.x + tolerance) &&
            point.y in (region.y - tolerance)..(region.y + tolerance)
}