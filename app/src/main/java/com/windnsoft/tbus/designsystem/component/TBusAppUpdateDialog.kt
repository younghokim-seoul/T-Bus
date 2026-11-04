package com.windnsoft.tbus.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.windnsoft.tbus.R
import com.windnsoft.tbus.designsystem.theme.TBusTheme

private val TBusUpdateAccentYellow = Color(0xFFFFF100)

@Composable
fun TBusAppUpdateDialogContent(
    onConfirmClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "업데이트 안내",
    message: String = "앱이 업데이트 되었습니다.\n원활한 서비스 이용을 위해\n앱을 업데이트 해주시기 바랍니다.",
    confirmText: String = "업데이트 하기",
    cancelText: String = "취소",
    logoPainter: Painter = painterResource(id = R.drawable.img_app_update_logo),
) {
    TBusSurface(
        modifier = modifier.fillMaxWidth(),
        shape = TBusTheme.shapes.sheet,
        color = TBusTheme.colors.surfaceRaised,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = TBusTheme.spacing.xxl,
                    top = TBusTheme.spacing.xxl,
                    end = TBusTheme.spacing.xxl,
                    bottom = TBusTheme.spacing.xxl,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TBusText(
                text = title,
                variant = TBusTextVariant.Title,
                color = TBusTheme.colors.content,
                textAlign = TextAlign.Center,
                style = TBusTheme.typography.title.copy(
                    fontSize = 20.sp,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                ),
            )

            Spacer(modifier = Modifier.height(TBusTheme.spacing.lg))

            TBusHorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = TBusTheme.colors.line.copy(alpha = 0.08f),
            )

            Spacer(modifier = Modifier.height(TBusTheme.spacing.xxl))

            Image(
                painter = logoPainter,
                contentDescription = title,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(210.dp)
                    .clip(RoundedCornerShape(36.dp)),
            )

            Spacer(modifier = Modifier.height(TBusTheme.spacing.xxl))

            TBusText(
                text = message,
                variant = TBusTextVariant.Body,
                color = TBusTheme.colors.content,
                textAlign = TextAlign.Center,
                style = TBusTheme.typography.body.copy(
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
            )

            Spacer(modifier = Modifier.height(TBusTheme.spacing.xxl))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TBusTheme.spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = onCancelClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TBusTheme.colors.surface,
                        contentColor = TBusTheme.colors.content,
                    ),
                    border = BorderStroke(1.dp, TBusTheme.colors.line),
                    contentPadding = PaddingValues(
                        horizontal = TBusTheme.spacing.md,
                        vertical = TBusTheme.spacing.sm,
                    ),
                ) {
                    TBusText(
                        text = cancelText,
                        variant = TBusTextVariant.Headline,
                        color = TBusTheme.colors.content,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        style = TBusTheme.typography.headline.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                }

                Button(
                    onClick = onConfirmClick,
                    modifier = Modifier
                        .weight(1.4f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TBusTheme.colors.surfaceInverse,
                        contentColor = TBusUpdateAccentYellow,
                    ),
                    contentPadding = PaddingValues(
                        horizontal = TBusTheme.spacing.md,
                        vertical = TBusTheme.spacing.sm,
                    ),
                ) {
                    TBusText(
                        text = confirmText,
                        variant = TBusTextVariant.Headline,
                        color = TBusUpdateAccentYellow,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        style = TBusTheme.typography.headline.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                        ),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF333333)
@Composable
private fun TBusAppUpdateDialogContentPreview() {
    TBusTheme(darkTheme = false) {
        TBusAppUpdateDialogContent(
            onConfirmClick = {},
            onCancelClick = {},
            modifier = Modifier.padding(24.dp),
        )
    }
}
