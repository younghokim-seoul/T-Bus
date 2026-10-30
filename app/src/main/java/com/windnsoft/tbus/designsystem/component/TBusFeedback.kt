package com.windnsoft.tbus.designsystem.component

import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.windnsoft.tbus.designsystem.theme.TBusTheme

@Composable
fun TBusSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
    ) { snackbarData ->
        Snackbar(
            snackbarData = snackbarData,
            shape = TBusTheme.shapes.card,
            containerColor = TBusTheme.colors.surfaceInverse,
            contentColor = TBusTheme.colors.contentOnMedia,
            actionColor = TBusTheme.colors.signal,
            dismissActionContentColor = TBusTheme.colors.contentOnMedia.copy(alpha = 0.78f),
        )
    }
}

@Composable
fun TBusFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = TBusTheme.colors.surfaceInverse,
    contentColor: Color = TBusTheme.colors.contentOnMedia,
    content: @Composable () -> Unit,
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        shape = TBusTheme.shapes.pill,
        containerColor = containerColor,
        contentColor = contentColor,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = TBusTheme.elevation.floating,
            pressedElevation = TBusTheme.elevation.sheet,
            focusedElevation = TBusTheme.elevation.floating,
            hoveredElevation = TBusTheme.elevation.floating,
        ),
        content = content,
    )
}
