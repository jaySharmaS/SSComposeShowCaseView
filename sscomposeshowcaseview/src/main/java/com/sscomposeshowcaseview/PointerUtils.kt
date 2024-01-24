package com.sscomposeshowcaseview

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import com.sscomposeshowcaseview.PointerPosition.BOTTOM_TO_TOP
import com.sscomposeshowcaseview.PointerPosition.LEFT_TO_RIGHT
import com.sscomposeshowcaseview.PointerPosition.RIGHT_TO_LEFT
import com.sscomposeshowcaseview.PointerPosition.TOP_TO_BOTTOM

fun pathify(pathData: Array<Offset>): Path {
    return Path().apply {
        pathData.forEachIndexed { index, offset ->
            if (index == 0) {
                moveTo(offset.x, offset.y)
            } else {
                lineTo(offset.x, offset.y)
            }
        }
    }
}

private fun getPosition(from: Rect, to: Rect): PointerPosition? {
    val allowYConnect =
        from.left - POS_OFFSET < to.right && from.right + to.width > to.right - POS_OFFSET

    val bottomToTop = from.bottom < to.top && allowYConnect
    val topToBottom = from.top > to.bottom && allowYConnect
    val rightToLeft = from.left > to.right
    val leftToRight = from.right < to.left

    if (bottomToTop) return BOTTOM_TO_TOP
    if (topToBottom) return TOP_TO_BOTTOM
    if (rightToLeft) return RIGHT_TO_LEFT
    if (leftToRight) return LEFT_TO_RIGHT

    return null
}

fun getPathData(fromRect: Rect, toRect: Rect): Array<Offset> {
    return when (getPosition(fromRect, toRect)) {
        BOTTOM_TO_TOP -> {
            arrayOf(
                Offset(
                    x = fromRect.left + fromRect.width / 2,
                    y = fromRect.bottom,
                ),
                Offset(
                    x = fromRect.left + fromRect.width / 2,
                    y = fromRect.bottom - (fromRect.bottom - toRect.top) / 2,
                ),
                Offset(
                    x = toRect.left + toRect.width / 2,
                    y = fromRect.bottom - (fromRect.bottom - toRect.top) / 2,
                ),
                Offset(
                    x = toRect.left + toRect.width / 2,
                    y = toRect.top - LINE_OFFSET,
                ),
            )
        }

        TOP_TO_BOTTOM -> {
            arrayOf(
                Offset(
                    x = fromRect.left + fromRect.width / 2,
                    y = fromRect.top,
                ),
                Offset(
                    x = fromRect.left + fromRect.width / 2,
                    y = fromRect.top - (fromRect.top - toRect.bottom) / 2,
                ),
                Offset(
                    x = toRect.left + toRect.width / 2,
                    y = fromRect.top - (fromRect.top - toRect.bottom) / 2,
                ),
                Offset(
                    x = toRect.left + toRect.width / 2,
                    y = toRect.bottom + LINE_OFFSET,
                ),
            )
        }

        RIGHT_TO_LEFT -> {
            arrayOf(
                Offset(
                    x = fromRect.left,
                    y = fromRect.bottom - fromRect.height / 2,
                ),
                Offset(
                    x = (toRect.right + fromRect.left) / 2,
                    y = fromRect.bottom - fromRect.height / 2,
                ),
                Offset(
                    x = (toRect.right + fromRect.left) / 2,
                    y = toRect.top + toRect.height / 2,
                ),
                Offset(
                    x = toRect.right + LINE_OFFSET,
                    y = toRect.top + toRect.height / 2,
                ),
            )
        }

        LEFT_TO_RIGHT -> {
            arrayOf(
                Offset(
                    x = fromRect.right,
                    y = fromRect.bottom - fromRect.height / 2,
                ),
                Offset(
                    x = (toRect.left + fromRect.right) / 2,
                    y = fromRect.bottom - fromRect.height / 2,
                ),
                Offset(
                    x = (toRect.left + fromRect.right) / 2,
                    y = toRect.top + toRect.height / 2,
                ),
                Offset(
                    x = toRect.left - LINE_OFFSET,
                    y = toRect.top + toRect.height / 2,
                ),
            )
        }

        else -> arrayOf()
    }
}

enum class PointerPosition {
    BOTTOM_TO_TOP,
    TOP_TO_BOTTOM,
    RIGHT_TO_LEFT,
    LEFT_TO_RIGHT;
}


private const val LINE_OFFSET = 9
private const val POS_OFFSET = 40
