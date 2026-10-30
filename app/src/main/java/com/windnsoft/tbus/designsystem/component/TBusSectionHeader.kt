package com.windnsoft.tbus.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.windnsoft.tbus.designsystem.theme.TBusTheme

@Composable
fun TBusSectionHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(TBusTheme.spacing.sm),
    ) {
        if (eyebrow != null) {
            TBusText(
                text = eyebrow,
                color = TBusTheme.colors.contentMuted,
                variant = TBusTextVariant.Label,
            )
        }
        TBusText(
            text = title,
            color = TBusTheme.colors.content,
            variant = TBusTextVariant.Display,
        )
        TBusText(
            text = subtitle,
            color = TBusTheme.colors.contentMuted,
            variant = TBusTextVariant.Body,
        )
    }
}
