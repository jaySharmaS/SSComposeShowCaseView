package com.sscomposeshowcaseview

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.Stable

@Stable
interface ShowcaseScope : BoxWithConstraintsScope {
    val onSkipShowcase: () -> Unit
    val onSkipSequence: () -> Unit
}

internal data class ShowcaseScopeImpl(
    val constraintsScope: BoxWithConstraintsScope,
    override val onSkipShowcase: () -> Unit,
    override val onSkipSequence: () -> Unit,
) : ShowcaseScope, BoxWithConstraintsScope by constraintsScope
