package tech.ryadom.origami.util.extensions

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
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

private fun isPointInRegion(
    point: Offset,
    region: Offset,
    tolerance: Float = 0f
): Boolean {
    return point.x in (region.x - tolerance)..(region.x + tolerance) &&
            point.y in (region.y - tolerance)..(region.y + tolerance)
}

internal fun Rect.copy(offset: Offset = this.offset, size: Size = this.size): Rect {
    return Rect(offset = offset, size = size)
}

internal class RectStateSaver : Saver<State<Rect>, String> {
    override fun restore(value: String): State<Rect> {
        val (left, top, right, bottom) = value.split(";")
        return mutableStateOf(
            Rect(
                left = left.toFloat(),
                top = top.toFloat(),
                right = right.toFloat(),
                bottom = bottom.toFloat()
            )
        )
    }

    override fun SaverScope.save(value: State<Rect>): String {
        val rect = value.value
        return listOf(rect.left, rect.top, rect.right, rect.bottom)
            .joinToString(";")
    }
}