package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricRickshaw
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PaymentMethod
import com.example.data.model.RideStatus
import com.example.data.model.VehicleCategory
import com.example.data.model.formatCurrency
import com.example.ui.theme.RideXAmber
import com.example.ui.theme.RideXCyan
import com.example.ui.theme.RideXEmerald
import com.example.ui.theme.RideXRose

fun getVehicleIcon(category: VehicleCategory): ImageVector {
    return when (category) {
        VehicleCategory.BIKE -> Icons.Default.TwoWheeler
        VehicleCategory.AUTO -> Icons.Default.ElectricRickshaw
        VehicleCategory.MINI -> Icons.Default.DirectionsCar
        VehicleCategory.SEDAN -> Icons.Default.DirectionsCar
        VehicleCategory.SUV -> Icons.Default.LocalTaxi
        VehicleCategory.PREMIUM -> Icons.Default.LocalTaxi
    }
}

fun getPaymentIcon(method: PaymentMethod): ImageVector {
    return when (method) {
        PaymentMethod.CASH -> Icons.Default.Money
        PaymentMethod.UPI -> Icons.Default.QrCode
        PaymentMethod.CARD -> Icons.Default.CreditCard
        PaymentMethod.WALLET -> Icons.Default.AccountBalanceWallet
    }
}

@Composable
fun VehicleCategoryCard(
    category: VehicleCategory,
    fare: Double,
    etaMinutes: Int,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("vehicle_card_${category.name.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = getVehicleIcon(category),
                            contentDescription = category.title,
                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = category.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Capacity",
                                modifier = Modifier.size(13.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${category.capacity}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$etaMinutes mins away • ${category.description}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        ),
                        maxLines = 1
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatCurrency(fare),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp
                    )
                )
                if (isSelected) {
                    Text(
                        text = "Selected",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun RidePinDisplay(
    pin: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "START OTP PIN",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                pin.forEach { digit ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(width = 22.dp, height = 26.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = digit.toString(),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RatingStarsBar(
    rating: Float,
    onRatingChanged: ((Float) -> Unit)? = null,
    modifier: Modifier = Modifier,
    maxStars: Int = 5,
    starSize: Int = 28
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..maxStars) {
            val isFilled = i <= rating
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Star $i",
                tint = if (isFilled) RideXAmber else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                modifier = Modifier
                    .size(starSize.dp)
                    .clickable(enabled = onRatingChanged != null) {
                        onRatingChanged?.invoke(i.toFloat())
                    }
            )
        }
    }
}

@Composable
fun RideStatusBadge(
    status: RideStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (status) {
        RideStatus.REQUESTED -> Triple(Color(0xFF3B82F6).copy(alpha = 0.15f), Color(0xFF3B82F6), "Requested")
        RideStatus.SEARCHING_DRIVER -> Triple(RideXAmber.copy(alpha = 0.15f), RideXAmber, "Finding Driver...")
        RideStatus.DRIVER_ASSIGNED -> Triple(RideXCyan.copy(alpha = 0.15f), RideXCyan, "Driver Assigned")
        RideStatus.DRIVER_ARRIVING -> Triple(RideXCyan.copy(alpha = 0.15f), RideXCyan, "Driver Arriving")
        RideStatus.DRIVER_REACHED -> Triple(RideXEmerald.copy(alpha = 0.15f), RideXEmerald, "Driver Reached")
        RideStatus.TRIP_STARTED -> Triple(RideXEmerald.copy(alpha = 0.15f), RideXEmerald, "On Trip")
        RideStatus.TRIP_COMPLETED -> Triple(RideXEmerald.copy(alpha = 0.15f), RideXEmerald, "Completed")
        RideStatus.CANCELLED -> Triple(RideXRose.copy(alpha = 0.15f), RideXRose, "Cancelled")
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            ),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun EmergencySosButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = RideXRose),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.testTag("emergency_sos_btn")
    ) {
        Icon(Icons.Default.Shield, contentDescription = "SOS", tint = Color.White, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "SOS Safety",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
    }
}
