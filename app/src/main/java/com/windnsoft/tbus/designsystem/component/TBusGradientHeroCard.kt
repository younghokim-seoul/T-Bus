package com.windnsoft.tbus.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.windnsoft.tbus.designsystem.theme.TBusTheme

@Composable
fun TBusGradientHeroCard(
    title: String,
    description: String,
    metric: String,
    palette: List<Color>,
    height: Dp,
    modifier: Modifier = Modifier,
    contentColor: Color = TBusTheme.colors.contentOnMedia,
    emphasizedTitle: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val shape = TBusTheme.shapes.sheet
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .background(Brush.linearGradient(palette.ifEmpty { DefaultHeroPalette }))
            .padding(TBusTheme.spacing.xxl),
    ) {
        TBusText(
            text = metric,
            modifier = Modifier.align(Alignment.TopEnd),
            color = contentColor.copy(alpha = 0.72f),
            variant = TBusTextVariant.Label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Column(
            modifier = Modifier.align(Alignment.BottomStart),
            verticalArrangement = Arrangement.spacedBy(TBusTheme.spacing.xs),
        ) {
            TBusText(
                text = title,
                color = contentColor,
                style = if (emphasizedTitle) {
                    TBusTheme.typography.title.copy(fontSize = 25.sp)
                } else {
                    TBusTheme.typography.title
                },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            TBusText(
                text = description,
                color = contentColor.copy(alpha = 0.76f),
                variant = TBusTextVariant.BodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private val DefaultHeroPalette = listOf(
    Color(0xFF101114),
    Color(0xFF2D333B),
    Color(0xFF6B7178),
)
