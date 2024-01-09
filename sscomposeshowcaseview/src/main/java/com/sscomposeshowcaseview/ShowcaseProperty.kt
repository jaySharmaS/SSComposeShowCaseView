package com.sscomposeshowcaseview

import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class ShowcaseProperty(
    val index: Int,
    val coordinates: LayoutCoordinates,
    val showCaseType: ShowcaseType = ShowcaseType.SIMPLE_ROUNDED,
    val blurOpacity: Float = 0.8f,
    val showcaseDelay: Long = 5000,
    val padding: Dp = 10.dp,
    val content: @Composable ShowcaseScope.() -> Unit
)
