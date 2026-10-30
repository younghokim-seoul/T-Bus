package com.windnsoft.tbus.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.windnsoft.tbus.designsystem.theme.TBusTheme

@Composable
fun TBusCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Checkbox(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        colors = CheckboxDefaults.colors(
            checkedColor = TBusTheme.colors.surfaceInverse,
            uncheckedColor = TBusTheme.colors.contentMuted,
            checkmarkColor = TBusTheme.colors.contentOnMedia,
            disabledCheckedColor = TBusTheme.colors.contentSubtle.copy(alpha = 0.32f),
            disabledUncheckedColor = TBusTheme.colors.contentSubtle.copy(alpha = 0.24f),
        ),
    )
}

@Composable
fun TBusSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        colors = SwitchDefaults.colors(
            checkedThumbColor = TBusTheme.colors.contentOnMedia,
            checkedTrackColor = TBusTheme.colors.surfaceInverse,
            uncheckedThumbColor = TBusTheme.colors.surfaceRaised,
            uncheckedTrackColor = TBusTheme.colors.contentSubtle.copy(alpha = 0.34f),
            uncheckedBorderColor = TBusTheme.colors.line,
        ),
    )
}

@Composable
fun TBusSelectionRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: @Composable () -> Unit,
) {
    Row(
        modifier = modifier.padding(vertical = TBusTheme.spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(TBusTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(TBusTheme.spacing.xxs),
        ) {
            TBusText(
                text = title,
                variant = TBusTextVariant.Label,
            )
            if (subtitle != null) {
                TBusText(
                    text = subtitle,
                    variant = TBusTextVariant.Caption,
                    color = TBusTheme.colors.contentMuted,
                )
            }
        }
        trailing()
    }
}
