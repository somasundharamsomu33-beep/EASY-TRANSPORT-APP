package com.example.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
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
import com.example.data.model.DriverEntity
import com.example.data.model.FareConfigEntity
import com.example.data.model.KycStatus
import com.example.data.model.RideEntity
import com.example.data.model.UserEntity
import com.example.data.model.VehicleCategory
import com.example.data.model.formatCurrency
import com.example.ui.components.RideStatusBadge
import com.example.ui.components.getVehicleIcon
import com.example.ui.theme.RideXAmber
import com.example.ui.theme.RideXCyan
import com.example.ui.theme.RideXEmerald
import com.example.ui.theme.RideXRose

@Composable
fun AdminDashboardScreen(
    users: List<UserEntity>,
    drivers: List<DriverEntity>,
    allRides: List<RideEntity>,
    fareConfigs: List<FareConfigEntity>,
    onUpdateDriverKyc: (String, KycStatus) -> Unit,
    onUpdateFareConfig: (FareConfigEntity) -> Unit,
    onToggleUserBlocked: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var adminTab by remember { mutableStateOf("OVERVIEW") } // OVERVIEW, LIVE_RIDES, DRIVERS, FARES, USERS

    val onlineDriversCount = drivers.count { it.isOnline }
    val totalVolume = allRides.filter { it.status.name == "TRIP_COMPLETED" }.sumOf { it.totalFare }
    val platformRevenue = totalVolume * 0.15

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("admin_dashboard_screen")
    ) {
        // Admin Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "RideX Command Center",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
                )
                Text(
                    text = "Platform Analytics & Operational Dispatch",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = RideXEmerald.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "SYSTEM ACTIVE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = RideXEmerald,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Navigation Tabs for Admin (Overview, Live Rides, KYC Drivers, Fare Engine, Users)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val tabs = listOf(
                "OVERVIEW" to "Overview",
                "LIVE_RIDES" to "Live (${allRides.count { it.status.name !in listOf("TRIP_COMPLETED", "CANCELLED") }})",
                "DRIVERS" to "Drivers (${drivers.size})",
                "FARES" to "Fare Engine",
                "USERS" to "Users"
            )
            tabs.forEach { (key, title) ->
                val isSelected = adminTab == key
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { adminTab = key }
                        .testTag("admin_tab_${key.lowercase()}")
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            ),
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Content
        when (adminTab) {
            "OVERVIEW" -> {
                AdminOverviewSection(
                    totalUsers = users.size,
                    totalDrivers = drivers.size,
                    onlineDrivers = onlineDriversCount,
                    totalRides = allRides.size,
                    totalVolume = totalVolume,
                    platformRevenue = platformRevenue
                )
            }
            "LIVE_RIDES" -> {
                AdminLiveRidesSection(allRides = allRides)
            }
            "DRIVERS" -> {
                AdminDriversKycSection(
                    drivers = drivers,
                    onUpdateKyc = onUpdateDriverKyc
                )
            }
            "FARES" -> {
                AdminFareEngineSection(
                    fareConfigs = fareConfigs,
                    onUpdateConfig = onUpdateFareConfig
                )
            }
            "USERS" -> {
                AdminUsersSection(
                    users = users,
                    onToggleBlocked = onToggleUserBlocked
                )
            }
        }
    }
}

@Composable
private fun AdminOverviewSection(
    totalUsers: Int,
    totalDrivers: Int,
    onlineDrivers: Int,
    totalRides: Int,
    totalVolume: Double,
    platformRevenue: Double
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            // Financial KPIs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("15% Platform Net Rev", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onPrimaryContainer))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = formatCurrency(platformRevenue),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Gross GMV Volume", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = formatCurrency(totalVolume),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = RideXEmerald
                            )
                        )
                    }
                }
            }
        }

        item {
            // Fleet & User Stats Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Online Drivers", style = MaterialTheme.typography.labelSmall)
                            Icon(Icons.Default.Speed, contentDescription = "Online", tint = RideXEmerald, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$onlineDrivers / $totalDrivers", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    }
                }

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Total Passengers", style = MaterialTheme.typography.labelSmall)
                            Icon(Icons.Default.People, contentDescription = "Users", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$totalUsers Users", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Platform Dispatch Health", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Server-side Ride Matching: Real-Time Algorithm Active", style = MaterialTheme.typography.bodySmall)
                    Text("• Realtime Telemetry: Vector Maps WebSocket Online", style = MaterialTheme.typography.bodySmall)
                    Text("• Payment Gateway: UPI / Razorpay / Wallet Ready", style = MaterialTheme.typography.bodySmall)
                    Text("• Safety Engine: 4-digit PIN + SOS Center Armed", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun AdminLiveRidesSection(allRides: List<RideEntity>) {
    val activeRides = allRides.filter { it.status.name !in listOf("TRIP_COMPLETED", "CANCELLED") }

    if (activeRides.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No active rides at this moment", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(activeRides) { ride ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RideStatusBadge(status = ride.status)
                            Text(
                                text = formatCurrency(ride.totalFare),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Passenger: ${ride.passengerName} • Driver: ${ride.driverName.ifBlank { "Assigning..." }}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("From: ${ride.pickupAddress}", style = MaterialTheme.typography.bodySmall, maxLines = 1)
                        Text("To: ${ride.dropAddress}", style = MaterialTheme.typography.bodySmall, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminDriversKycSection(
    drivers: List<DriverEntity>,
    onUpdateKyc: (String, KycStatus) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(drivers) { driver ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(driver.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text("${driver.vehicleType.title} • ${driver.vehicleNumber}", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when (driver.kycStatus) {
                                KycStatus.VERIFIED -> RideXEmerald.copy(alpha = 0.15f)
                                KycStatus.PENDING -> RideXAmber.copy(alpha = 0.15f)
                                else -> RideXRose.copy(alpha = 0.15f)
                            }
                        ) {
                            Text(
                                text = driver.kycStatus.name,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = when (driver.kycStatus) {
                                        KycStatus.VERIFIED -> RideXEmerald
                                        KycStatus.PENDING -> RideXAmber
                                        else -> RideXRose
                                    }
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("License: ${driver.licenseNumber} • Bank: ${driver.bankAccount}", style = MaterialTheme.typography.labelSmall)

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (driver.kycStatus != KycStatus.VERIFIED) {
                            Button(
                                onClick = { onUpdateKyc(driver.id, KycStatus.VERIFIED) },
                                colors = ButtonDefaults.buttonColors(containerColor = RideXEmerald),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Approve KYC", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        if (driver.kycStatus != KycStatus.SUSPENDED) {
                            OutlinedButton(
                                onClick = { onUpdateKyc(driver.id, KycStatus.SUSPENDED) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Suspend", fontSize = 11.sp, color = RideXRose)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminFareEngineSection(
    fareConfigs: List<FareConfigEntity>,
    onUpdateConfig: (FareConfigEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(fareConfigs) { config ->
            var surge by remember(config.surgeMultiplier) { mutableStateOf(config.surgeMultiplier) }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = getVehicleIcon(config.vehicleType), contentDescription = config.vehicleType.title, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(config.vehicleType.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Text("Base: ₹${config.baseFare.toInt()} | ₹${config.perKmRate}/km", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Dynamic Surge Multiplier: ${String.format("%.1fx", surge)}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                        if (surge > 1.0) {
                            Surface(shape = RoundedCornerShape(6.dp), color = RideXAmber.copy(alpha = 0.2f)) {
                                Text("SURGE ACTIVE", style = MaterialTheme.typography.labelSmall.copy(color = RideXAmber, fontWeight = FontWeight.Bold), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }

                    Slider(
                        value = surge.toFloat(),
                        onValueChange = {
                            surge = (it * 10).toInt() / 10.0
                            onUpdateConfig(config.copy(surgeMultiplier = surge))
                        },
                        valueRange = 1.0f..3.0f,
                        steps = 19
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminUsersSection(
    users: List<UserEntity>,
    onToggleBlocked: (String, Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(users) { user ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(user.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            if (user.isBlocked) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(shape = RoundedCornerShape(6.dp), color = RideXRose.copy(alpha = 0.2f)) {
                                    Text("BLOCKED", style = MaterialTheme.typography.labelSmall.copy(color = RideXRose, fontWeight = FontWeight.Bold), modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                            }
                        }
                        Text(user.phone, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                        Text("Wallet: ${formatCurrency(user.walletBalance)} • Rating: ${user.rating} ★", style = MaterialTheme.typography.labelSmall)
                    }

                    OutlinedButton(
                        onClick = { onToggleBlocked(user.id, !user.isBlocked) },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (user.isBlocked) "Unblock" else "Block", fontSize = 11.sp, color = if (user.isBlocked) RideXEmerald else RideXRose)
                    }
                }
            }
        }
    }
}
