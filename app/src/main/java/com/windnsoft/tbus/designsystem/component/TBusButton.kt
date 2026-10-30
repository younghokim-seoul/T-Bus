package com.windnsoft.tbus.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.windnsoft.tbus.designsystem.theme.TBusTheme

enum class TBusButtonVariant {
    Primary,
    Secondary,
    Danger,
}

@Composable
fun TBusButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: TBusButtonVariant = TBusButtonVariant.Primary,
    leadingIcon: (@Composable (contentColor: Color) -> Unit)? = null,
    trailingIcon: (@Composable (contentColor: Color) -> Unit)? = null,
) {
    TBusButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        variant = variant,
    ) {
        TBusButtonContent(
            text = text,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
        )
    }
}

@Composable
fun TBusButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: TBusButtonVariant = TBusButtonVariant.Primary,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = TBusTheme.shapes.pill,
        colors = ButtonDefaults.buttonColors(
            containerColor = variant.containerColor(),
            contentColor = variant.contentColor(),
            disabledContainerColor = TBusTheme.colors.surfaceRaised.copy(alpha = 0.48f),
            disabledContentColor = TBusTheme.colors.contentSubtle.copy(alpha = 0.48f),
        ),
        contentPadding = contentPadding,
        content = content,
    )
}

@Composable
fun TBusOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable (contentColor: Color) -> Unit)? = null,
    trailingIcon: (@Composable (contentColor: Color) -> Unit)? = null,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = TBusTheme.shapes.pill,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = TBusTheme.colors.content,
            disabledContentColor = TBusTheme.colors.contentSubtle.copy(alpha = 0.48f),
        ),
        border = BorderStroke(
            width = TBusOutlinedButtonBorderWidth,
            color = if (enabled) {
                TBusTheme.colors.line
            } else {
                TBusTheme.colors.line.copy(alpha = 0.42f)
            },
        ),
    ) {
        TBusButtonContent(
            text = text,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
        )
    }
}

private val TBusOutlinedButtonBorderWidth = 1.dp

@Composable
fun TBusTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable (contentColor: Color) -> Unit)? = null,
    trailingIcon: (@Composable (contentColor: Color) -> Unit)? = null,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = TBusTheme.shapes.pill,
        colors = ButtonDefaults.textButtonColors(
            contentColor = TBusTheme.colors.content,
            disabledContentColor = TBusTheme.colors.contentSubtle.copy(alpha = 0.48f),
        ),
    ) {
        TBusButtonContent(
            text = text,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
        )
    }
}

@Composable
fun TBusIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false,
    content: @Composable (contentColor: Color) -> Unit,
) {
    val contentColor = if (selected) {
        TBusTheme.colors.contentOnMedia
    } else {
        TBusTheme.colors.content
    }
    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = if (selected) TBusTheme.colors.surfaceInverse else Color.Transparent,
            contentColor = contentColor,
            disabledContentColor = TBusTheme.colors.contentSubtle.copy(alpha = 0.48f),
        ),
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            content(contentColor)
        }
    }
}

@Composable
private fun TBusButtonContent(
    text: String,
    leadingIcon: (@Composable (contentColor: Color) -> Unit)?,
    trailingIcon: (@Composable (contentColor: Color) -> Unit)?,
) {
    val contentColor = LocalContentColor.current
    Row(
        horizontalArrangement = Arrangement.spacedBy(TBusTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leadingIcon?.invoke(contentColor)
        TBusText(
            text = text,
            color = contentColor,
            variant = TBusTextVariant.Label,
            maxLines = 1,
        )
        trailingIcon?.invoke(contentColor)
    }
}

@Composable
private fun TBusButtonVariant.containerColor(): Color {
    return when (this) {
        TBusButtonVariant.Primary -> TBusTheme.colors.surfaceInverse
        TBusButtonVariant.Secondary -> TBusTheme.colors.route
        TBusButtonVariant.Danger -> TBusTheme.colors.danger
    }
}

@Composable
private fun TBusButtonVariant.contentColor(): Color {
    return when (this) {
        TBusButtonVariant.Primary -> TBusTheme.colors.contentOnMedia
        TBusButtonVariant.Secondary -> TBusTheme.colors.contentOnMedia
        TBusButtonVariant.Danger -> TBusTheme.colors.contentOnMedia
    }
}
