package com.sscomposeshowcaseview

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PointerTest(targets: SnapshotStateMap<String, ShowcaseProperty>) {
    val uniqueTargets = targets.values.sortedBy { it.index }
    uniqueTargets.getOrNull(0)?.let { target ->
        val targetRect = target.coordinates.boundsInRoot()
        val xOffset = targetRect.topLeft.x
        val yOffset = targetRect.topLeft.y
        val rectSize = target.coordinates.boundsInParent().size
        Canvas(
            modifier = Modifier
                .fillMaxSize()
        ) {
            drawRect(
                Color.Black.copy(alpha = 0.8f),
                size = Size(size.width + 40f, size.height + 40f),
                style = Fill,
            )
            drawRect(
                Color.White,
                size = Size(rectSize.width + 15f, rectSize.height + 15f),
                style = Fill,
                topLeft = Offset(xOffset - 8, yOffset - 8),
                blendMode = BlendMode.Clear
            )
        }
    }
}

@Composable
fun PointerTestPreview() {
    val targets = remember { mutableStateMapOf<String, ShowcaseProperty>() }
    Surface {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More icon",
                modifier = Modifier
                    .padding(10.dp)
                    .onGloballyPositioned {
                        targets["more"] = ShowcaseProperty(
                            index = 1,
                            coordinates = it,
                            showCaseType = ShowcaseType.POINTER,
                            showcaseDelay = 5000
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "More options",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Click here to see options",
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                Button(onClick = this@ShowcaseProperty.onSkipSequence) {
                                    Text(text = "Skip All")
                                }
                            }
                        }
                    }
            )
        }
    }

    PointerTest(targets)
}

/**
 *
 * Find the middle of each line of the rectangles (should be easy, just avarage x1+x2 and
y1+y2)
• Determine the edges that are closest to each other using Pythagoras formula on the points
you got in the previous step.
• Start drawing a line starting at a,ya (first point you got in the step above), and draw it in the
direction away from the rectangle. You should know this direction, because you can know
the line segment this point is on.
• Do the same for xbyb (point on the second rectangle). If the lines are in opposite
directions, you should draw them to halfway xa-xb or ya-yb (depending on if you're drawing
horizontally or vertically). If they are perpendicular (is that the right word?) you should draw
them to the point where they cross, so you draw the line from xa,ya to xa,yb or a,ya to xb,
ya, depending if you draw the horizontal or vertical line.
• There should be some additional check to see if the rectangles overlap. You should not
draw lines in the same direction for example. Maybe it would suffice for you to draw just a
diagonal line between the two points in those cases where you cannot determine how to
draw these straight lines.
 */

@Composable
fun TestPointer2() {
    val targetRect = Rect(
        offset = Offset(700f, 100f),
        size = Size(300f, 100f)
    )
    val pointerRect = Rect(
        offset = Offset(500f, 700f),
        size = Size(550f, 400f)
    )

    /**
     * Find the middle of each line of the rectangles
     * (should be easy, just avarage x1+x2 and y1+y2)
     */

    val centerLeftT = targetRect.centerLeft
    val centerRightT = targetRect.centerRight
    val centerTopT = targetRect.topCenter
    val centerBottomT = targetRect.bottomCenter
    val centerLeftP = pointerRect.centerLeft
    val centerRightP = pointerRect.centerRight
    val centerTopP = pointerRect.topCenter
    val centerBottomP = pointerRect.bottomCenter
    val listT = listOf(
        centerLeftT, centerRightT, centerTopT, centerBottomT,
    )
    val listP = listOf(
        centerLeftP, centerRightP, centerTopP, centerBottomP,
    )

    /**
     * Determine the edges that are closest to each other using Pythagoras formula on the points
     * you got in the previous step.
     */

    var closeOffset = Pair(Offset.Zero, Offset.Zero)
    var closeDistance = Int.MAX_VALUE

    listT.forEach { t ->
        listP.forEach { p ->
            val d = (t - p).getDistanceSquared().toInt()
            if (d < closeDistance) {
                closeDistance = d
                closeOffset = Pair(t, p)
            }
        }
    }

    Log.d("TAG", "TestPointer2: $closeOffset")

    /**
     * Start drawing a line starting at a,ya (first point you got in the step above),
     * and draw it in the direction away from the rectangle.
     * You should know this direction, because you can know the line segment this point is on.
     */
    val path = Path()
    path.moveTo(closeOffset.second.x, closeOffset.second.y)
    path.lineTo(closeOffset.second.x, closeOffset.first.y)
    path.lineTo(closeOffset.first.x, closeOffset.first.y)

    /*val drawPathAnimation = remember {
        Animatable(0f)
    }
    val pathMeasure = remember {
        PathMeasure()
    }
    LaunchedEffect(key1 = Unit, block = {
        drawPathAnimation.animateTo(
            1f,
            animationSpec = tween(2000, easing = FastOutSlowInEasing)
        )
    })
    val animatedPath = remember {
        derivedStateOf {
            val newPath = Path()
            pathMeasure.setPath(path, false)
            // get a segment of the path at the percentage of the animation, to show a drawing on
            // screen animation
            pathMeasure.getSegment(
                0f,
                drawPathAnimation.value * pathMeasure.length, newPath
            )
            newPath
        }
    }*/

    Canvas(
        modifier = Modifier
            .fillMaxSize()
    ) {
        drawRect(
            Color.Black.copy(alpha = 0.4f),
            size = Size(size.width + 40f, size.height + 40f),
            style = Fill,
        )
        drawRect(
            Color.White,
            size = targetRect.size,
            style = Fill,
            topLeft = targetRect.topLeft,
            blendMode = BlendMode.Clear
        )
        drawRect(
            Color.White,
            size = pointerRect.size,
            style = Fill,
            topLeft = pointerRect.topLeft,
            blendMode = BlendMode.Clear
        )
        drawPath(
            path = path,
            color = Color.White,
            style = Stroke(width = 3f)
        )
        /*drawCircle(Color.Red, radius = 5f, center = closeOffset.first)
        drawCircle(Color.Red, radius = 5f, center = closeOffset.second)
        drawLine(Color.Red, closeOffset.first, Offset(closeOffset.second.x, closeOffset.first.y))
        drawLine(Color.Red, Offset(closeOffset.second.x, closeOffset.first.y), closeOffset.second)*/
    }
}

@Preview
@Composable
fun PointerTest2Preview() {
    Surface {
        Box(modifier = Modifier.fillMaxSize()) {
            TestPointer2()
        }
    }
}
