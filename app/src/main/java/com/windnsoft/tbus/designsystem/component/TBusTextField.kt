package com.windnsoft.tbus.designsystem.component

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import com.windnsoft.tbus.designsystem.theme.TBusTheme

@Composable
fun TBusTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    label: String? = null,
    placeholder: String? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    singleLine: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = TBusTheme.typography.body,
        label = label?.let {
            {
                TBusText(
                    text = it,
                    variant = TBusTextVariant.Label,
                    color = TBusTheme.colors.contentMuted,
                )
            }
        },
        placeholder = placeholder?.let {
            {
                TBusText(
                    text = it,
                    variant = TBusTextVariant.Body,
                    color = TBusTheme.colors.contentSubtle,
                )
            }
        },
        supportingText = supportingText?.let {
            {
                TBusText(
                    text = it,
                    variant = TBusTextVariant.Caption,
                    color = if (isError) TBusTheme.colors.danger else TBusTheme.colors.contentSubtle,
                )
            }
        },
        isError = isError,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        shape = TBusTheme.shapes.card,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TBusTheme.colors.content,
            unfocusedTextColor = TBusTheme.colors.content,
            disabledTextColor = TBusTheme.colors.contentSubtle,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = TBusTheme.colors.surfaceRaised.copy(alpha = 0.64f),
            disabledContainerColor = TBusTheme.colors.surfaceRaised.copy(alpha = 0.36f),
            cursorColor = TBusTheme.colors.route,
            errorCursorColor = TBusTheme.colors.danger,
            focusedBorderColor = TBusTheme.colors.route,
            unfocusedBorderColor = TBusTheme.colors.line,
            disabledBorderColor = TBusTheme.colors.line.copy(alpha = 0.44f),
            errorBorderColor = TBusTheme.colors.danger,
            focusedLabelColor = TBusTheme.colors.route,
            unfocusedLabelColor = TBusTheme.colors.contentMuted,
            errorLabelColor = TBusTheme.colors.danger,
        ),
    )
}
