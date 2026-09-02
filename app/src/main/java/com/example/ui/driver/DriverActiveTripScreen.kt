package com.example.ui.driver

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RideEntity
import com.example.data.model.RideStatus
import com.example.data.model.formatCurrency
import com.example.ui.components.RideStatusBadge
import com.example.ui.theme.RideXEmerald
import com.example.ui.theme.RideXRose

@Composable
fun DriverActiveTripScreen(
    ride: RideEntity,
    onDriverArrived: () -> Unit,
    onStartTrip: (pin: String) -> Boolean,
    onCompleteTrip: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("driver_active_trip_screen")
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header: Status & Passenger info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RideStatusBadge(status = ride.status)
                Text(
                    text = "Fare: " + formatCurrency(ride.totalFare * 0.85),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = RideXEmerald
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Passenger Info Row
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, contentDescription = "Passenger", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = ride.passengerName,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = ride.passengerPhone,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Text(
                        text = "★ 4.9",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Route addresses
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "• Pickup: ${ride.pickupAddress}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "• Drop: ${ride.dropAddress}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Flow based on ride state
            when (ride.status) {
                RideStatus.DRIVER_ASSIGNED, RideStatus.DRIVER_ARRIVING -> {
                    Button(
                        onClick = onDriverArrived,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("driver_arrived_btn"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Navigation, contentDescription = "Arrived", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("I Have Arrived at Pickup", fontWeight = FontWeight.Bold)
                    }
                }
                RideStatus.DRIVER_REACHED -> {
                    // Driver PIN Input Verification Required from Passenger
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = "PIN", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Enter Passenger 4-Digit PIN to Start Trip",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = inputPin,
                                onValueChange = {
                                    if (it.length <= 4) {
                                        inputPin = it
                                        pinError = false
                                    }
                                },
                                placeholder = { Text("Enter 4-digit PIN (e.g. ${ride.ridePin})") },
                                singleLine = true,
                                isError = pinError,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("driver_pin_input_field"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            if (pinError) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Incorrect PIN. Ask the passenger for the 4-digit OTP shown on their screen.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = RideXRose, fontSize = 11.sp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    val success = onStartTrip(inputPin)
                                    if (!success) {
                                        pinError = true
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("driver_start_trip_btn"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "Start", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Verify & Start Trip", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                RideStatus.TRIP_STARTED -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "En Route to Destination",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = RideXEmerald
                                )
                            )
                            Text(
                                text = "${(ride.routeProgressPercent * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { ride.routeProgressPercent.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = RideXEmerald
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onCompleteTrip,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("driver_complete_trip_btn"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RideXEmerald)
                        ) {
                            Text("Complete Trip & Collect Fare", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
                else -> {}
            }
        }
    }
}
