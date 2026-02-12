package com.bashmaqawa.presentation.screens.transaction.components

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.bashmaqawa.data.database.entities.Project
import com.bashmaqawa.data.database.entities.Worker
import com.bashmaqawa.presentation.screens.transaction.PaymentMethod
import com.bashmaqawa.presentation.theme.AppColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Project Selector Card
 * بطاقة اختيار المشروع
 */
@Composable
fun ProjectSelectorCard(
    selectedProject: Project?,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    OptionalFieldCard(
        icon = Icons.Filled.BusinessCenter,
        label = "المشروع",
        value = selectedProject?.name,
        placeholder = "اختر المشروع (اختياري)",
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
    )
}

/**
 * Worker Selector Card
 * بطاقة اختيار العامل
 */
@Composable
fun WorkerSelectorCard(
    selectedWorker: Worker?,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    OptionalFieldCard(
        icon = Icons.Filled.Person,
        label = "العامل",
        value = selectedWorker?.name,
        placeholder = "اختر العامل (اختياري)",
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
    )
}

/**
 * Date Selector Card
 * بطاقة اختيار التاريخ
 */
@Composable
fun DateSelectorCard(
    selectedDate: LocalDate,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val arabicFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ar"))
    val formattedDate = selectedDate.format(arabicFormatter)
    
    val isToday = selectedDate == LocalDate.now()
    val displayText = if (isToday) "اليوم - $formattedDate" else formattedDate
    
    OptionalFieldCard(
        icon = Icons.Filled.CalendarToday,
        label = "التاريخ",
        value = displayText,
        placeholder = "",
        onClick = onClick,
        enabled = enabled,
        showChevron = true,
        modifier = modifier
    )
}

/**
 * Payment Method Selector Card
 * بطاقة اختيار طريقة الدفع
 */
@Composable
fun PaymentMethodCard(
    selectedMethod: PaymentMethod?,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    OptionalFieldCard(
        icon = Icons.Filled.Payment,
        label = "طريقة الدفع",
        value = selectedMethod?.arabicName,
        placeholder = "اختر طريقة الدفع (اختياري)",
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
    )
}

/**
 * Reference Number Card
 * بطاقة رقم المرجع
 */
@Composable
fun ReferenceNumberCard(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Tag,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "رقم المرجع (اختياري)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        androidx.compose.material3.OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("مثال: INV-001")
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

/**
 * Receipt Attachment Card
 * بطاقة إرفاق الإيصال
 */
@Composable
fun ReceiptAttachmentCard(
    receiptUri: String?,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Receipt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "إيصال / فاتورة (اختياري)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        if (receiptUri != null) {
            // Show attached image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                AsyncImage(
                    model = receiptUri,
                    contentDescription = "Receipt",
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Remove button
                IconButton(
                    onClick = onRemoveClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(32.dp)
                        .background(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "إزالة",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        } else {
            // Show add options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Camera button
                AttachmentButton(
                    icon = Icons.Filled.CameraAlt,
                    label = "الكاميرا",
                    onClick = onCameraClick,
                    modifier = Modifier.weight(1f)
                )
                
                // Gallery button
                AttachmentButton(
                    icon = Icons.Filled.Image,
                    label = "المعرض",
                    onClick = onGalleryClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun AttachmentButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = AppColors.Primary.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = AppColors.Primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Generic Optional Field Card
 * بطاقة حقل اختياري عامة
 */
@Composable
private fun OptionalFieldCard(
    icon: ImageVector,
    label: String,
    value: String?,
    placeholder: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    showChevron: Boolean = true,
    modifier: Modifier = Modifier
) {
    val hasValue = !value.isNullOrBlank()
    
    val borderColor by animateColorAsState(
        targetValue = if (hasValue) 
            AppColors.Primary.copy(alpha = 0.3f) 
        else 
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
        animationSpec = tween(200),
        label = "borderColor"
    )
    
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, borderColor),
        enabled = enabled
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = if (hasValue) AppColors.Primary.copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (hasValue) AppColors.Primary 
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value ?: placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (hasValue) MaterialTheme.colorScheme.onSurface 
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Chevron
            if (showChevron) {
                Icon(
                    imageVector = Icons.Filled.ChevronLeft,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
