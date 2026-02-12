package com.bashmaqawa.presentation.screens.transaction.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bashmaqawa.presentation.theme.AppColors
import java.text.NumberFormat
import java.util.Locale

/**
 * Premium Amount Input Field
 * حقل إدخال المبلغ الاحترافي
 * 
 * Features:
 * - Large, prominent display
 * - Currency suffix (ج.م)
 * - Error state with animation
 * - Focus scale animation
 * - Live formatting
 */
@Composable
fun AmountInputField(
    value: String,
    onValueChange: (String) -> Unit,
    currency: String = "ج.م",
    hasError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    onDone: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Focus scale animation
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.02f else 1f,
        animationSpec = tween(200),
        label = "focusScale"
    )
    
    // Border color animation
    val borderColor by animateColorAsState(
        targetValue = when {
            hasError -> AppColors.Error
            isFocused -> AppColors.Primary
            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        },
        animationSpec = tween(200),
        label = "borderColor"
    )
    
    // Background color animation
    val backgroundColor by animateColorAsState(
        targetValue = when {
            hasError -> AppColors.Error.copy(alpha = 0.05f)
            isFocused -> AppColors.Primary.copy(alpha = 0.05f)
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        },
        animationSpec = tween(200),
        label = "bgColor"
    )
    
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "المبلغ",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale)
                .clip(RoundedCornerShape(20.dp))
                .background(backgroundColor)
                .border(
                    width = if (isFocused || hasError) 2.dp else 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(vertical = 24.dp, horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = { newValue ->
                        // Filter to allow only numbers and one decimal point
                        val filtered = newValue.filter { it.isDigit() || it == '.' }
                        val parts = filtered.split(".")
                        val result = when {
                            parts.size <= 1 -> filtered
                            else -> "${parts[0]}.${parts[1].take(2)}"
                        }
                        onValueChange(result)
                    },
                    textStyle = TextStyle(
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (hasError) AppColors.Error else MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            onDone?.invoke()
                        }
                    ),
                    singleLine = true,
                    enabled = enabled,
                    cursorBrush = SolidColor(AppColors.Primary),
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .onFocusChanged { isFocused = it.isFocused },
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.Center) {
                            if (value.isEmpty()) {
                                Text(
                                    text = "0.00",
                                    style = TextStyle(
                                        fontSize = 48.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                        textAlign = TextAlign.Center
                                    )
                                )
                            }
                            innerTextField()
                        }
                    }
                )
                
                Text(
                    text = currency,
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
        
        // Error message
        if (hasError && !errorMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.Error,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        }
    }
}

/**
 * Format number for display (with thousand separators)
 */
fun formatAmountForDisplay(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale("ar", "EG"))
    formatter.minimumFractionDigits = 2
    formatter.maximumFractionDigits = 2
    return formatter.format(amount)
}
