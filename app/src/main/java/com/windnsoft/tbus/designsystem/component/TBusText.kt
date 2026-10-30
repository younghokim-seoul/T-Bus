package com.windnsoft.tbus.designsystem.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.windnsoft.tbus.designsystem.theme.TBusTheme

enum class TBusTextVariant {
    Display,
    Title,
    Headline,
    Body,
    BodySmall,
    Label,
    Caption,
}

@Composable
fun TBusText(
    text: String,
    modifier: Modifier = Modifier,
    variant: TBusTextVariant = TBusTextVariant.Body,
    color: Color = TBusTheme.colors.content,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    style: TextStyle = variant.tbusTextStyle(),
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
        style = style,
    )
}

@Composable
fun TBusTextVariant.tbusTextStyle(): TextStyle {
    return when (this) {
        TBusTextVariant.Display -> TBusTheme.typography.display
        TBusTextVariant.Title -> TBusTheme.typography.title
        TBusTextVariant.Headline -> TBusTheme.typography.headline
        TBusTextVariant.Body -> TBusTheme.typography.body
        TBusTextVariant.BodySmall -> TBusTheme.typography.bodySmall
        TBusTextVariant.Label -> TBusTheme.typography.label
        TBusTextVariant.Caption -> TBusTheme.typography.caption
    }
}
