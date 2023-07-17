package com.sscomposeshowcaseview

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask
import kotlin.concurrent.schedule
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * @param targets the target on which showcase will be showed
 * @param onShowCaseCompleted do the needful on completing showcase view
 */
@Composable
fun ShowCaseTarget(
    targets: SnapshotStateMap<String, ShowcaseProperty>,
    isEnableAutoShowCase: Boolean = false,
    key: String,
    onShowCaseCompleted: () -> Unit
) {
    val preferences = Preferences(context = LocalContext.current)
    val uniqueTargets = targets.values.sortedBy { it.index }
    var currentTargetIndex by remember { mutableStateOf(0) }
    val currentTarget = if (uniqueTargets.isNotEmpty() && currentTargetIndex < uniqueTargets.size)
        uniqueTargets[currentTargetIndex]
    else
        null

    if (preferences.getShowing(key)) {
        return
    }

    currentTarget?.let {
        IntroShowCase2(
            targets = it, isAutomaticShowcase = isEnableAutoShowCase,
            content = it.content,
            onShowCaseCompleted = {
                if (++currentTargetIndex >= uniqueTargets.size) {
                    onShowCaseCompleted()
                    preferences.show(key)
                }
            },
            onSkipAll = {
                currentTargetIndex = uniqueTargets.size
                onShowCaseCompleted()
                preferences.show(key)
            }
        )
    }
}

@Composable
private fun IntroShowCase2(
    targets: ShowcaseProperty,
    isAutomaticShowcase: Boolean = false,
    content: @Composable ShowcaseScope.() -> Unit,
    onShowCaseCompleted: () -> Unit,
    onSkipAll: () -> Unit
) {
    val targetRect = targets.coordinates.boundsInRoot()
    val targetRadius = targetRect.maxDimension / 2f + 20

    var textCoordinate: LayoutCoordinates? by remember { mutableStateOf(null) }
    var outerRadius by remember { mutableStateOf(0f) }
    val topArea = 10.dp
    val screenHeight = LocalConfiguration.current.screenHeightDp
    val textYOffset = with(LocalDensity.current) {
        targets.coordinates.positionInRoot().y.toDp()
    }
    var outerOffset by remember { mutableStateOf(Offset(0f, 0f)) }

    textCoordinate?.let {
        val textRect = it.boundsInRoot()
        val textHeight = it.size.height
        val isInGutter = topArea > textYOffset || textYOffset > screenHeight.dp.minus(topArea)
        outerOffset =
            getOuterCircleCenter(targetRect, textRect, targetRadius, textHeight, isInGutter)
        outerRadius = getOuterRadius(textRect, targetRect) + targetRadius
    }

    val showcaseDelayScope = rememberCoroutineScope()
    val pointerInput : suspend PointerInputScope.() -> Unit = remember(isAutomaticShowcase, targets) {
        {
            if (isAutomaticShowcase) {
                showcaseDelayScope.launch {
                    delay(targets.showcaseDelay)
                    onShowCaseCompleted()
                }
            } else {
                detectTapGestures { tapOffset ->
                    if (targetRect.contains(tapOffset)) {
                        onShowCaseCompleted()
                    }
                }
            }
        }
    }

    when (targets.showCaseType) {
        ShowcaseType.SIMPLE_ROUNDED -> {
            DrawOnCanvas(
                targets = targets,
                pointerInput = pointerInput
            ) {
                drawCircle(
                    color = Color.Black.copy(alpha = targets.blurOpacity),
                    radius = size.maxDimension,
                    alpha = 0.9f
                )
                drawCircle(
                    color = Color.White,
                    radius = targetRadius - 10f,
                    center = targetRect.center,
                    blendMode = BlendMode.Clear
                )
            }
        }
        ShowcaseType.SIMPLE_RECTANGLE -> {
            val xOffset = targetRect.topLeft.x
            val yOffset = targetRect.topLeft.y
            val rectSize = targets.coordinates.boundsInParent().size
            DrawOnCanvas(
                targets = targets,
                pointerInput = pointerInput
            ) {
                drawRect(
                    Color.Black.copy(alpha = targets.blurOpacity),
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
        ShowcaseType.ANIMATED_ROUNDED -> {
            // Animation setup for rounded animation
            val animationSpec = infiniteRepeatable<Float>(
                animation = tween(2000, easing = FastOutLinearInEasing),
                repeatMode = RepeatMode.Restart
            )
            val animaTables = listOf(
                remember { Animatable(0f) },
                remember { Animatable(0f) }
            )
            val outerAnimaTable = remember { Animatable(0.6f) }
            animaTables.forEachIndexed { index, animaTable ->
                LaunchedEffect(animaTable) {
                    delay(index * 1000L)
                    animaTable.animateTo(1f, animationSpec = animationSpec)
                }
            }
            LaunchedEffect(targets) {
                outerAnimaTable.snapTo(0.6f)
                outerAnimaTable.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(500, easing = FastOutSlowInEasing)
                )
            }
            // Map animation to y position of the components
            val dys = animaTables.map { it.value }
            DrawOnCanvas(
                targets = targets,
                pointerInput = pointerInput
            ) {
                drawCircle(
                    color = Color.Black,
                    center = outerOffset,
                    radius = outerRadius * outerAnimaTable.value,
                    alpha = targets.blurOpacity
                )
                // draw circle with animation
                dys.forEach { dy ->
                    drawCircle(
                        color = Color.White,
                        radius = targetRect.maxDimension * dy * 2f,
                        center = targetRect.center,
                        alpha = 1 - dy
                    )
                }
                drawCircle(
                    color = Color.White,
                    radius = targetRadius,
                    center = targetRect.center,
                    blendMode = BlendMode.Clear
                )
            }
        }
        ShowcaseType.ANIMATED_RECTANGLE -> {
            val xOffset = targetRect.topLeft.x
            val yOffset = targetRect.topLeft.y
            val rectSize = targets.coordinates.boundsInParent().size
            val animationSpec = infiniteRepeatable<Float>(
                animation = tween(2000, easing = FastOutLinearInEasing),
                repeatMode = RepeatMode.Reverse
            )
            val animaTables = listOf(
                remember { Animatable(0f) },
                remember { Animatable(0f) }
            )
            animaTables.forEachIndexed { index, animaTable ->
                LaunchedEffect(animaTable) {
                    delay(index * 1000L)
                    animaTable.animateTo(1f, animationSpec = animationSpec)
                }
            }
            // Map animation to y position of the components
            val dys = animaTables.map { it.value }
            DrawOnCanvas(
                targets = targets,
                pointerInput = pointerInput
            ) {
                drawRect(
                    Color.Black.copy(alpha = targets.blurOpacity),
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
                dys.forEach { dy ->
                    drawRect(
                        color = Color.White.copy(alpha = targets.blurOpacity),
                        size = Size(rectSize.width * dy * 2, rectSize.height * dy * 2),
                        topLeft = Offset(xOffset - 12, yOffset - 12),
                        alpha = 1 - dy
                    )
                }
            }
        }
        ShowcaseType.POINTER -> {
            textCoordinate?.boundsInParent()?.let { pointerRect ->
                val pathData = remember(pointerRect, targetRect) {
                    getPathData(fromRect = pointerRect, toRect = targetRect)
                }
                val path = remember(pathData) {
                    pathify(pathData)
                }
                val circleWidth = remember {
                    Animatable(0f)
                }
                val drawPathAnimation = remember {
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
                val animatedPath = remember(path) {
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
                }

                if (drawPathAnimation.value >= 1f) {
                    LaunchedEffect(key1 = Unit, block = {
                        circleWidth.animateTo(
                            targetValue = 6f,
                            animationSpec = tween(2000, easing = FastOutSlowInEasing)
                        )
                    })
                    Log.d("TAG", "TestPointer2: finished")
                }
                DrawOnCanvas(
                    targets = targets,
                    pointerInput = pointerInput
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
                    drawPath(
                        path = animatedPath.value,
                        color = Color.White,
                        style = Stroke(width = 3.dp.value)
                    )
                    pathData.lastOrNull()?.let {
                        drawCircle(Color.White, radius = circleWidth.value.dp.toPx(), center = it)
                        drawCircle(Color.Gray, radius = circleWidth.value.dp.toPx() - density, center = it)
                    }
                }
            }
        }
    }

    // Draw content
    ShowText(
        currentTarget = targets,
        targetRect = targetRect,
        targetRadius = targetRadius,
        updateCoordinates = {
            textCoordinate = it
        },
        content = content,
        onSkip = {
            showcaseDelayScope.cancel()
            onShowCaseCompleted()
        },
        onSkipAll = {
            showcaseDelayScope.cancel()
            onSkipAll()
        }
    )
}

/**
 * get the radius, x and y offsets and coordinates to calculate radius and size for the rectangle and circle
 * @param targets the target on which showcase will be showed.
 * @param onShowCaseCompleted do the needful on completing showcase view.
 */
@Composable
private fun IntroShowCase(
    targets: ShowcaseProperty,
    isAutomaticShowcase: Boolean = false,
    content: @Composable ShowcaseScope.() -> Unit,
    onShowCaseCompleted: () -> Unit,
    onSkipAll: () -> Unit
) {
    val targetRect = targets.coordinates.boundsInRoot()
    val targetRadius = targetRect.maxDimension / 2f + 20
    val xOffset = targetRect.topLeft.x
    val yOffset = targetRect.topLeft.y
    val rectSize = targets.coordinates.boundsInParent().size

    // Animation setup for rounded animation
    val animationSpec = infiniteRepeatable<Float>(
        animation = tween(2000, easing = FastOutLinearInEasing),
        repeatMode = if (targets.showCaseType == ShowcaseType.ANIMATED_RECTANGLE)
            RepeatMode.Reverse
        else
            RepeatMode.Restart
    )
    val animaTables = listOf(
        remember { Animatable(0f) },
        remember { Animatable(0f) }
    )
    animaTables.forEachIndexed { index, animaTable ->
        LaunchedEffect(animaTable) {
            delay(index * 1000L)
            animaTable.animateTo(1f, animationSpec = animationSpec)
        }
    }

    val outerAnimaTable = remember { Animatable(0.6f) }

    LaunchedEffect(targets) {
        outerAnimaTable.snapTo(0.6f)
        outerAnimaTable.animateTo(
            targetValue = 1f,
            animationSpec = tween(500, easing = FastOutSlowInEasing)
        )
    }

    // Map animation to y position of the components
    val dys = animaTables.map { it.value }

    //Text coordinates and outer radius
    var textCoordinate: LayoutCoordinates? by remember { mutableStateOf(null) }
    var outerRadius by remember { mutableStateOf(0f) }
    val topArea = 10.dp
    val screenHeight = LocalConfiguration.current.screenHeightDp
    val textYOffset = with(LocalDensity.current) {
        targets.coordinates.positionInRoot().y.toDp()
    }
    var outerOffset by remember { mutableStateOf(Offset(0f, 0f)) }


    textCoordinate?.let {
        val textRect = it.boundsInRoot()
        val textHeight = it.size.height
        val isInGutter = topArea > textYOffset || textYOffset > screenHeight.dp.minus(topArea)
        outerOffset =
            getOuterCircleCenter(targetRect, textRect, targetRadius, textHeight, isInGutter)
        outerRadius = getOuterRadius(textRect, targetRect) + targetRadius
    }

    var timerTask: TimerTask? = null
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(targets) {
                if (isAutomaticShowcase) {
                    timerTask = Timer(true).schedule(targets.showcaseDelay) {
                        onShowCaseCompleted()
                    }
                } else {
                    detectTapGestures { tapOffset ->
                        if (targetRect.contains(tapOffset)) {
                            onShowCaseCompleted()
                        }
                    }
                }
            }
            .graphicsLayer(alpha = 0.99f)
    ) {
        when (targets.showCaseType) {
            /**
             * Rounded ShowCaseView
             */
            ShowcaseType.SIMPLE_ROUNDED -> {
                drawCircle(
                    color = Color.Black.copy(alpha = targets.blurOpacity),
                    radius = size.maxDimension,
                    alpha = 0.9f
                )
                drawCircle(
                    color = Color.White,
                    radius = targetRadius - 10f,
                    center = targetRect.center,
                    blendMode = BlendMode.Clear
                )
            }
            /**
             * Rectangle ShowCaseView
             */
            ShowcaseType.SIMPLE_RECTANGLE -> {
                drawRect(
                    Color.Black.copy(alpha = targets.blurOpacity),
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
            /**
             * Rounded ShowCaseView with animation
             */
            ShowcaseType.ANIMATED_ROUNDED -> {
                drawCircle(
                    color = Color.Black,
                    center = outerOffset,
                    radius = outerRadius * outerAnimaTable.value,
                    alpha = targets.blurOpacity
                )
                // draw circle with animation
                dys.forEach { dy ->
                    drawCircle(
                        color = Color.White,
                        radius = targetRect.maxDimension * dy * 2f,
                        center = targetRect.center,
                        alpha = 1 - dy
                    )
                }
                drawCircle(
                    color = Color.White,
                    radius = targetRadius,
                    center = targetRect.center,
                    blendMode = BlendMode.Clear
                )
            }
            /**
             * Rectangle ShowCaseView with animation
             */
            ShowcaseType.ANIMATED_RECTANGLE -> {
                drawRect(
                    Color.Black.copy(alpha = targets.blurOpacity),
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
                dys.forEach { dy ->
                    drawRect(
                        color = Color.White.copy(alpha = targets.blurOpacity),
                        size = Size(rectSize.width * dy * 2, rectSize.height * dy * 2),
                        topLeft = Offset(xOffset - 12, yOffset - 12),
                        alpha = 1 - dy
                    )
                }
            }
            ShowcaseType.POINTER -> {
                /*drawRect(
                    Color.Black.copy(alpha = targets.blurOpacity),
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

                val contentRect = textCoordinate?.boundsInRoot() ?: Rect.Zero
                if ((rectSize.width > size.width) || (rectSize.height > size.height)) {
                    Log.d("TAG", "Distance from center is : width || height")
                } else {
                    if (contentRect.overlaps(targetRect)) {
                        // find distance from center to top and bottom direction
                        val distanceFromCenter = center.y - targetRect.center.y
                        if (distanceFromCenter > 0) {
                            // top
                            Log.d("TAG", "Distance from center is : Top")
                        } else {
                            // bottom
                            Log.d("TAG", "Distance from center is : Bottom")
                        }
                    }
                }*/

                /*drawRect(
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
                textCoordinate?.boundsInParent()?.let { pointerRect ->
                    drawRect(
                        Color.White,
                        size = pointerRect.size,
                        style = Fill,
                        topLeft = pointerRect.topLeft,
                        blendMode = BlendMode.Clear
                    )
                }
                drawPath(
                    path = animatedPath.value,
                    color = Color.White,
                    style = Stroke(width = 3.dp.value)
                )
                drawCircle(Color.White, radius = circleWidth.value.dp.toPx(), center = pathData.last())
                drawCircle(Color.Gray, radius = circleWidth.value.dp.toPx() - density, center = pathData.last())*/
            }
        }
    }

    //val path = Path()
    //path.moveTo()

    ShowText(
        currentTarget = targets,
        targetRect = targetRect,
        targetRadius = targetRadius,
        updateCoordinates = {
            textCoordinate = it
        },
        content = content,
        onSkip = {
            timerTask?.cancel()
            onShowCaseCompleted()
        },
        onSkipAll = {
            timerTask?.cancel()
            onSkipAll()
        }
    )
}

@Composable
private fun DrawOnCanvas(
    targets: ShowcaseProperty,
    pointerInput: suspend PointerInputScope.() -> Unit,
    content: DrawScope.() -> Unit,
) {
    //val targetRect = targets.coordinates.boundsInRoot()
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(targets, pointerInput)
            .graphicsLayer(alpha = 0.99f),
        onDraw = content
    )
}

/**
 * ShowCase Text based on current target and its coordinates.
 * @param currentTarget current target to showCase
 * @param targetRect current target rect based on its coordinates
 * @param targetRadius radius of current target to draw circles
 * @param updateCoordinates return when coordinates get updated
 */
@Composable
private fun ShowText(
    currentTarget: ShowcaseProperty,
    targetRect: Rect,
    targetRadius: Float,
    updateCoordinates: (LayoutCoordinates) -> Unit,
    content: @Composable ShowcaseScope.() -> Unit,
    onSkip: () -> Unit,
    onSkipAll: () -> Unit
) {
    var txtOffsetY by remember { mutableStateOf(0f) }
    var txtOffsetX by remember { mutableStateOf(0f) }
    var txtRightOffSet by remember { mutableStateOf(0f) }
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.toFloat()

    Column(modifier = Modifier
        .offset(
            x = with(LocalDensity.current) { txtOffsetX.toDp() },
            y = with(LocalDensity.current) { txtOffsetY.toDp() }
        )
        .onGloballyPositioned {
            updateCoordinates(it)
            val textHeight = it.size.height
            val possibleTop =
                if (currentTarget.showCaseType == ShowcaseType.ANIMATED_ROUNDED) {
                    targetRect.center.y - targetRadius - textHeight
                } else {
                    targetRect.center.y - textHeight + 200
                }
            val possibleLeft = targetRect.topLeft.x
            txtOffsetY = if (possibleTop > 0) {
                possibleTop
            } else {
                targetRect.center.y + targetRadius - 140
            }
            txtRightOffSet = it.boundsInRoot().topRight.x
            txtOffsetX = it.boundsInRoot().topRight.x - it.size.width
            txtOffsetX = if (possibleLeft >= screenWidth / 2) {
                screenWidth / 2 + targetRadius
            } else {
                possibleLeft
            }
            txtRightOffSet += targetRadius
        }
        .padding(2.dp)
    ) {

        BoxWithConstraints(
            modifier = Modifier
                .width(
                    if (txtRightOffSet > screenWidth) {
                        configuration.screenWidthDp.dp / 2 + 40.dp
                    } else {
                        configuration.screenWidthDp.dp
                    }
                )
                .background(Color.White, RoundedCornerShape(5))
        ) {
            ShowcaseScopeImpl(this, onSkip, onSkipAll).content()
        }
    }
}

private fun getOuterRadius(textRect: Rect, targetRect: Rect): Float {
    // Get outer rect
    val topLeftX = min(textRect.topLeft.x, targetRect.topLeft.x)
    val topLeftY = min(textRect.topLeft.y, targetRect.topLeft.y)
    val bottomRightX = max(textRect.bottomRight.x, targetRect.bottomRight.x)
    val bottomRightY = max(textRect.bottomRight.y, targetRect.bottomRight.y)
    val newBounds = Rect(topLeftX, topLeftY, bottomRightX, bottomRightY)
    // Distance of outer rect
    val distance =
        sqrt(newBounds.height.toDouble().pow(2.0) + newBounds.width.toDouble().pow(2.0)).toFloat()
    // Return radius
    return (distance / 2f)
}

private fun getOuterCircleCenter(
    targetRect: Rect,
    textRect: Rect,
    targetRadius: Float,
    textHeight: Int,
    isInGutter: Boolean
): Offset {
    val outerCenterX: Float
    var outerCenterY: Float

    val onTop = targetRect.center.y - targetRadius - textHeight > 0
    val left = min(textRect.left, targetRect.left - targetRadius)
    val right = max(textRect.right, targetRect.right + targetRadius)

    val centerY = if (onTop)
        targetRect.center.y - targetRadius - textHeight
    else
        targetRect.center.y + targetRadius + textHeight

    outerCenterY = centerY
    outerCenterX = (left + right) / 2

    if (isInGutter) {
        outerCenterY = targetRect.center.y
    }

    return Offset(outerCenterX, outerCenterY)
}
