package com.windnsoft.tbus.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.windnsoft.tbus.designsystem.theme.TBusTheme

@Composable
fun TBusPillButton(
    label: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    leadingIcon: (@Composable (contentColor: Color) -> Unit)? = null,
    onClick: () -> Unit,
) {
    val contentColor = when {
        !enabled -> TBusTheme.colors.contentSubtle.copy(alpha = 0.42f)
        selected -> TBusTheme.colors.contentOnMedia
        else -> TBusTheme.colors.content
    }
    val backgroundColor = when {
        !enabled -> TBusTheme.colors.glassLight.copy(alpha = 0.36f)
        selected -> TBusTheme.colors.surfaceInverse
        else -> TBusTheme.colors.glassLight
    }

    TBusGlassSurface(
        modifier = modifier.then(
            if (enabled) Modifier.clickable(onClick = onClick) else Modifier,
        ),
        shape = TBusTheme.shapes.pill,
        backgroundColor = backgroundColor,
        borderColor = TBusTheme.colors.glassStroke,
        contentPadding = PaddingValues(
            horizontal = TBusTheme.spacing.md,
            vertical = TBusTheme.spacing.sm,
        ),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(TBusTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingIcon?.invoke(contentColor)
            TBusText(
                text = label,
                color = contentColor,
                variant = TBusTextVariant.Label,
                maxLines = 1,
            )
        }
    }
}
