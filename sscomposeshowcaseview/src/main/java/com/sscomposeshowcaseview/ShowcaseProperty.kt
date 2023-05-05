package com.sscomposeshowcaseview

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.LayoutCoordinates

data class ShowcaseProperty(
    val index: Int,
    val coordinates: LayoutCoordinates,
    val showCaseType: ShowcaseType = ShowcaseType.SIMPLE_ROUNDED,
    val blurOpacity: Float = 0.8f,
    val showcaseDelay: Long = 2000,
    val content: @Composable BoxScope.() -> Unit
)
