package com.example.ui.passenger

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LocationPoint
import com.example.data.model.PaymentMethod
import com.example.data.model.PromoCodeEntity
import com.example.data.model.VehicleCategory
import com.example.data.model.formatCurrency
import com.example.engine.FareBreakdown
import com.example.ui.components.VehicleCategoryCard
import com.example.ui.components.getPaymentIcon
import com.example.ui.theme.RideXAmber
import com.example.ui.theme.RideXCyan
import com.example.ui.theme.RideXEmerald
import com.example.ui.theme.RideXRose

@Composable
fun VehicleSelectorSheet(
    pickup: LocationPoint,
    drop: LocationPoint,
    distanceKm: Double,
    durationMinutes: Int,
    fareBreakdowns: List<FareBreakdown>,
    selectedCategory: VehicleCategory,
    onSelectCategory: (VehicleCategory) -> Unit,
    selectedPaymentMethod: PaymentMethod,
    onSelectPaymentMethod: (PaymentMethod) -> Unit,
    appliedPromo: PromoCodeEntity?,
    onApplyPromoCode: (String) -> Unit,
    onClearPromo: () -> Unit,
    onConfirmBooking: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var promoInput by remember { mutableStateOf("") }
    var showPromoInput by remember { mutableStateOf(false) }

    val currentSelectedBreakdown = fareBreakdowns.firstOrNull { it.category == selectedCategory }
        ?: fareBreakdowns.firstOrNull()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("vehicle_selector_sheet"),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Sheet Header with Route Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Route,
                            contentDescription = "Route",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$distanceKm km • ~$durationMinutes mins",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                    Text(
                        text = "${pickup.name.take(18)} → ${drop.name.take(18)}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                IconButton(onClick = onCancel) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Vehicle Category List (6 options)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(fareBreakdowns) { breakdown ->
                    VehicleCategoryCard(
                        category = breakdown.category,
                        fare = breakdown.finalFare,
                        etaMinutes = when (breakdown.category) {
                            VehicleCategory.BIKE -> 2
                            VehicleCategory.AUTO -> 3
                            VehicleCategory.MINI -> 4
                            VehicleCategory.SEDAN -> 4
                            VehicleCategory.SUV -> 6
                            VehicleCategory.PREMIUM -> 7
                        },
                        isSelected = breakdown.category == selectedCategory,
                        onSelect = { onSelectCategory(breakdown.category) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Promo Code Section
            if (appliedPromo != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = RideXEmerald.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, RideXEmerald.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalOffer, contentDescription = "Promo", tint = RideXEmerald, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${appliedPromo.code} applied (Saved ${formatCurrency(currentSelectedBreakdown?.discount ?: 0.0)})",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = RideXEmerald
                                )
                            )
                        }
                        IconButton(onClick = onClearPromo, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Remove Promo", tint = RideXEmerald, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            } else {
                if (showPromoInput) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = promoInput,
                            onValueChange = { promoInput = it.uppercase() },
                            placeholder = { Text("Enter Promo (e.g. RIDEX50)", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("promo_input_field"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (promoInput.isNotBlank()) {
                                    onApplyPromoCode(promoInput)
                                    showPromoInput = false
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(50.dp)
                        ) {
                            Text("Apply", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPromoInput = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ConfirmationNumber, contentDescription = "Promo", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Apply Promo Code / Coupon (Try RIDEX50)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Payment Methods Selector Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Payment:",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PaymentMethod.values().forEach { method ->
                        val isSelected = method == selectedPaymentMethod
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier
                                .clickable { onSelectPaymentMethod(method) }
                                .testTag("payment_method_${method.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = getPaymentIcon(method),
                                    contentDescription = method.name,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = method.name,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 10.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CTA Request Ride Button
            Button(
                onClick = onConfirmBooking,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("confirm_book_ride_btn"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Book ${selectedCategory.title}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black
                        )
                    )
                    Text(
                        text = formatCurrency(currentSelectedBreakdown?.finalFare ?: 0.0),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                    )
                }
            }
        }
    }
}
