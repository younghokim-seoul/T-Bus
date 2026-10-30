package com.windnsoft.tbus.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.windnsoft.tbus.designsystem.theme.TBusTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TBusTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    containerColor: Color = TBusTheme.colors.background,
) {
    TopAppBar(
        title = {
            TBusTopAppBarTitle(
                title = title,
                subtitle = subtitle,
            )
        },
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor,
            scrolledContainerColor = TBusTheme.colors.glassLightStrong,
            titleContentColor = TBusTheme.colors.content,
            navigationIconContentColor = TBusTheme.colors.content,
            actionIconContentColor = TBusTheme.colors.content,
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TBusCenterAlignedTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    containerColor: Color = TBusTheme.colors.background,
) {
    CenterAlignedTopAppBar(
        title = {
            TBusTopAppBarTitle(
                title = title,
                subtitle = subtitle,
            )
        },
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor,
            scrolledContainerColor = TBusTheme.colors.glassLightStrong,
            titleContentColor = TBusTheme.colors.content,
            navigationIconContentColor = TBusTheme.colors.content,
            actionIconContentColor = TBusTheme.colors.content,
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TBusLargeTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    containerColor: Color = TBusTheme.colors.background,
) {
    LargeTopAppBar(
        title = {
            TBusTopAppBarTitle(
                title = title,
                subtitle = subtitle,
            )
        },
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor,
            scrolledContainerColor = TBusTheme.colors.glassLightStrong,
            titleContentColor = TBusTheme.colors.content,
            navigationIconContentColor = TBusTheme.colors.content,
            actionIconContentColor = TBusTheme.colors.content,
        ),
    )
}

@Composable
private fun TBusTopAppBarTitle(
    title: String,
    subtitle: String?,
) {
    if (subtitle == null) {
        TBusText(
            text = title,
            variant = TBusTextVariant.Title,
            maxLines = 1,
        )
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(TBusTheme.spacing.xxs)) {
            TBusText(
                text = title,
                variant = TBusTextVariant.Title,
                maxLines = 1,
            )
            TBusText(
                text = subtitle,
                variant = TBusTextVariant.Caption,
                color = TBusTheme.colors.contentMuted,
                maxLines = 1,
            )
        }
    }
}
