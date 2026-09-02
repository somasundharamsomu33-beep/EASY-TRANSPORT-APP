package com.example.ui

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.LocationPoint
import com.example.data.model.RideStatus
import com.example.data.model.UserRole
import com.example.ui.admin.AdminDashboardScreen
import com.example.ui.components.RideXAppBar
import com.example.ui.components.RideXVectorMap
import com.example.ui.driver.DriverActiveTripScreen
import com.example.ui.driver.DriverDashboardScreen
import com.example.ui.driver.DriverEarningsScreen
import com.example.ui.driver.DriverKycScreen
import com.example.ui.passenger.ActiveRideTrackingSheet
import com.example.ui.passenger.AiAssistantDialog
import com.example.ui.passenger.DestinationSearchSheet
import com.example.ui.passenger.PassengerHistoryScreen
import com.example.ui.passenger.PassengerWalletScreen
import com.example.ui.passenger.RideCompletionSheet
import com.example.ui.passenger.SafetyCenterDialog
import com.example.ui.passenger.VehicleSelectorSheet
import com.example.ui.theme.RideXAmber
import com.example.ui.theme.RideXCyan
import com.example.ui.theme.RideXEmerald
import com.example.ui.theme.RideXRose
import com.example.ui.viewmodel.RideXViewModel

@Composable
fun RideXApp(
    viewModel: RideXViewModel = viewModel()
) {
    val context = LocalContext.current

    val currentRole by viewModel.currentRole.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val currentDriver by viewModel.currentDriver.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val allDrivers by viewModel.allDrivers.collectAsState()
    val allRides by viewModel.allRides.collectAsState()
    val passengerHistory by viewModel.passengerHistory.collectAsState()
    val savedPlaces by viewModel.savedPlaces.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val fareConfigs by viewModel.fareConfigs.collectAsState()
    val activePassengerRide by viewModel.activePassengerRide.collectAsState()
    val activeDriverRide by viewModel.activeDriverRide.collectAsState()
    val pendingDriverRequest by viewModel.pendingDriverRequest.collectAsState()

    val pickupLocation by viewModel.pickupLocation.collectAsState()
    val dropLocation by viewModel.dropLocation.collectAsState()
    val routePoints by viewModel.routePoints.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedPaymentMethod by viewModel.selectedPaymentMethod.collectAsState()
    val appliedPromo by viewModel.appliedPromo.collectAsState()
    val fareBreakdowns by viewModel.fareBreakdowns.collectAsState()

    val aiMessages by viewModel.aiMessages.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()

    // Navigation & Overlay Modal States
    var showSearchSheet by remember { mutableStateOf(false) }
    var showSafetyDialog by remember { mutableStateOf(false) }
    var showAiDialog by remember { mutableStateOf(false) }
    var showHistoryScreen by remember { mutableStateOf(false) }
    var showWalletScreen by remember { mutableStateOf(false) }
    var showDriverKycScreen by remember { mutableStateOf(false) }
    var showDriverEarningsScreen by remember { mutableStateOf(false) }
    var activeCompletedRideForReview by remember { mutableStateOf<com.example.data.model.RideEntity?>(null) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("ridex_app_scaffold"),
        topBar = {
            RideXAppBar(
                selectedRole = currentRole,
                onRoleSelected = { role ->
                    viewModel.setRole(role)
                    showHistoryScreen = false
                    showWalletScreen = false
                    showDriverKycScreen = false
                    showDriverEarningsScreen = false
                },
                currentLocationName = pickupLocation.name.ifBlank { "Chennai Central" },
                walletBalance = currentUser?.walletBalance ?: 750.0,
                onWalletClick = { showWalletScreen = true },
                onSafetyClick = { showSafetyDialog = true },
                onAiAssistantClick = { showAiDialog = true }
            )
        },
        bottomBar = {
            if (activePassengerRide == null && activeDriverRide == null && !showHistoryScreen && !showWalletScreen && !showDriverKycScreen && !showDriverEarningsScreen) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = currentRole == UserRole.PASSENGER,
                        onClick = { viewModel.setRole(UserRole.PASSENGER) },
                        icon = { Icon(Icons.Default.DirectionsCar, contentDescription = "Ride") },
                        label = { Text("Passenger", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary)
                    )
                    NavigationBarItem(
                        selected = currentRole == UserRole.DRIVER,
                        onClick = { viewModel.setRole(UserRole.DRIVER) },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Drive") },
                        label = { Text("Driver", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary)
                    )
                    NavigationBarItem(
                        selected = currentRole == UserRole.ADMIN,
                        onClick = { viewModel.setRole(UserRole.ADMIN) },
                        icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin") },
                        label = { Text("Admin", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary)
                    )
                    NavigationBarItem(
                        selected = showHistoryScreen,
                        onClick = { showHistoryScreen = true },
                        icon = { Icon(Icons.Default.History, contentDescription = "Trips") },
                        label = { Text("My Trips", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    NavigationBarItem(
                        selected = showWalletScreen,
                        onClick = { showWalletScreen = true },
                        icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Wallet") },
                        label = { Text("Wallet", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Fullscreen Screens if opened
            when {
                showHistoryScreen -> {
                    PassengerHistoryScreen(
                        rides = passengerHistory,
                        onBack = { showHistoryScreen = false }
                    )
                    return@Box
                }
                showWalletScreen -> {
                    PassengerWalletScreen(
                        walletBalance = currentUser?.walletBalance ?: 750.0,
                        transactions = transactions,
                        onAddFunds = { amount ->
                            viewModel.addWalletFunds(amount)
                            Toast.makeText(context, "Added ₹${amount.toInt()} to RideX Wallet!", Toast.LENGTH_SHORT).show()
                        },
                        onBack = { showWalletScreen = false }
                    )
                    return@Box
                }
                showDriverKycScreen -> {
                    currentDriver?.let { driver ->
                        DriverKycScreen(
                            driver = driver,
                            onBack = { showDriverKycScreen = false }
                        )
                    }
                    return@Box
                }
                showDriverEarningsScreen -> {
                    currentDriver?.let { driver ->
                        DriverEarningsScreen(
                            driver = driver,
                            onWithdrawEarnings = {
                                viewModel.driverWithdrawEarnings()
                                Toast.makeText(context, "Payout dispatched to bank account!", Toast.LENGTH_SHORT).show()
                            },
                            onBack = { showDriverEarningsScreen = false }
                        )
                    }
                    return@Box
                }
            }

            // Role 1: PASSENGER VIEW
            if (currentRole == UserRole.PASSENGER) {
                // Interactive Vector Map Canvas
                RideXVectorMap(
                    pickupLocation = pickupLocation,
                    dropLocation = dropLocation,
                    routePoints = routePoints,
                    nearbyDrivers = allDrivers.filter { it.isOnline },
                    activeDriverPoint = activePassengerRide?.let {
                        LocationPoint(it.driverCurrentLat, it.driverCurrentLng)
                    },
                    activeDriverCategory = activePassengerRide?.vehicleType,
                    rideStatus = activePassengerRide?.status,
                    routeProgressPercent = activePassengerRide?.routeProgressPercent ?: 0f
                )

                // Passenger Bottom Interface (Search Trigger OR Vehicle Selector OR Active Ride Tracking)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                ) {
                    when {
                        activePassengerRide != null -> {
                            val activeRide = activePassengerRide!!
                            ActiveRideTrackingSheet(
                                ride = activeRide,
                                onCancelRide = {
                                    viewModel.cancelActiveRide(activeRide.id)
                                    Toast.makeText(context, "Ride cancelled", Toast.LENGTH_SHORT).show()
                                },
                                onOpenSafety = { showSafetyDialog = true },
                                onOpenChat = {
                                    Toast.makeText(context, "Chat with Captain opened: 'I am waiting near entrance'", Toast.LENGTH_SHORT).show()
                                },
                                onShareTrip = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "I'm riding with RideX! Track my live trip to ${activeRide.dropAddress}: https://ridex.io/track/${activeRide.id}"
                                        )
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share RideX Live Trip"))
                                }
                            )
                        }
                        dropLocation != null && fareBreakdowns.isNotEmpty() -> {
                            val selectedBreakdown = fareBreakdowns.firstOrNull { it.category == selectedCategory }
                                ?: fareBreakdowns.first()

                            VehicleSelectorSheet(
                                pickup = pickupLocation,
                                drop = dropLocation!!,
                                distanceKm = selectedBreakdown.distanceKm,
                                durationMinutes = selectedBreakdown.estimatedMinutes,
                                fareBreakdowns = fareBreakdowns,
                                selectedCategory = selectedCategory,
                                onSelectCategory = { viewModel.selectCategory(it) },
                                selectedPaymentMethod = selectedPaymentMethod,
                                onSelectPaymentMethod = { viewModel.selectPaymentMethod(it) },
                                appliedPromo = appliedPromo,
                                onApplyPromoCode = { code ->
                                    viewModel.applyPromoCode(code)
                                    Toast.makeText(context, "Promo code applied!", Toast.LENGTH_SHORT).show()
                                },
                                onClearPromo = { viewModel.clearPromo() },
                                onConfirmBooking = {
                                    viewModel.confirmBookRide()
                                    Toast.makeText(context, "Requesting ${selectedCategory.title}...", Toast.LENGTH_SHORT).show()
                                },
                                onCancel = { viewModel.clearDestination() }
                            )
                        }
                        else -> {
                            // Where to? Search Launch Card
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .clickable { showSearchSheet = true }
                                    .testTag("passenger_search_launch_card"),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    Icons.Default.Search,
                                                    contentDescription = "Search",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Text(
                                                text = "Where to?",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                            Text(
                                                text = "Tap to search destination or pick saved place",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSize = 11.sp
                                                )
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = "GO",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Black,
                                                color = MaterialTheme.colorScheme.primary
                                            ),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Destination Search Sheet Overlay
                if (showSearchSheet) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f))
                            .clickable { showSearchSheet = false },
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        DestinationSearchSheet(
                            pickupLocation = pickupLocation,
                            savedPlaces = savedPlaces,
                            onSelectDestination = { target ->
                                viewModel.setDropLocation(target)
                                showSearchSheet = false
                            },
                            onSelectPickup = { target ->
                                viewModel.setPickupLocation(target)
                                showSearchSheet = false
                            },
                            onClose = { showSearchSheet = false }
                        )
                    }
                }
            }

            // Role 2: DRIVER VIEW
            if (currentRole == UserRole.DRIVER) {
                currentDriver?.let { driver ->
                    if (activeDriverRide != null) {
                        val ride = activeDriverRide!!
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Map for Driver Navigation
                            Box(modifier = Modifier.weight(1f)) {
                                RideXVectorMap(
                                    pickupLocation = LocationPoint(ride.pickupLat, ride.pickupLng),
                                    dropLocation = LocationPoint(ride.dropLat, ride.dropLng),
                                    routePoints = routePoints,
                                    activeDriverPoint = LocationPoint(ride.driverCurrentLat, ride.driverCurrentLng),
                                    activeDriverCategory = driver.vehicleType,
                                    rideStatus = ride.status,
                                    routeProgressPercent = ride.routeProgressPercent
                                )
                            }
                            DriverActiveTripScreen(
                                ride = ride,
                                onDriverArrived = { viewModel.driverArrivedAtPickup(ride.id) },
                                onStartTrip = { pin -> viewModel.driverVerifyPinAndStartTrip(ride, pin) },
                                onCompleteTrip = {
                                    viewModel.completeTrip(ride)
                                    Toast.makeText(context, "Trip completed! ₹${(ride.totalFare * 0.85).toInt()} added to your earnings.", Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    } else {
                        DriverDashboardScreen(
                            driver = driver,
                            pendingRideRequest = pendingDriverRequest,
                            activeRide = activeDriverRide,
                            onToggleOnline = { isOnline -> viewModel.toggleDriverOnline(isOnline) },
                            onAcceptRide = { ride ->
                                viewModel.driverAcceptRide(ride)
                                Toast.makeText(context, "Ride Accepted! Navigating to pickup...", Toast.LENGTH_SHORT).show()
                            },
                            onDeclineRide = { ride -> viewModel.driverDeclineRide(ride) },
                            onOpenKyc = { showDriverKycScreen = true },
                            onOpenEarnings = { showDriverEarningsScreen = true }
                        )
                    }
                }
            }

            // Role 3: ADMIN VIEW
            if (currentRole == UserRole.ADMIN) {
                AdminDashboardScreen(
                    users = allUsers,
                    drivers = allDrivers,
                    allRides = allRides,
                    fareConfigs = fareConfigs,
                    onUpdateDriverKyc = { driverId, status ->
                        viewModel.updateDriverKyc(driverId, status)
                        Toast.makeText(context, "Driver KYC updated to $status", Toast.LENGTH_SHORT).show()
                    },
                    onUpdateFareConfig = { config ->
                        viewModel.updateFareConfig(config)
                        Toast.makeText(context, "Fare config for ${config.vehicleType.title} updated", Toast.LENGTH_SHORT).show()
                    },
                    onToggleUserBlocked = { userId, blocked ->
                        viewModel.toggleUserBlocked(userId, blocked)
                        Toast.makeText(context, "User status updated", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // AI Assistant Dialog
            if (showAiDialog) {
                AiAssistantDialog(
                    messages = aiMessages,
                    isLoading = isAiLoading,
                    onSendMessage = { prompt -> viewModel.sendAiMessage(prompt) },
                    onConfirmBookRide = { dest, lat, lng, cat ->
                        viewModel.bookFromAi(dest, lat, lng, cat)
                        Toast.makeText(context, "Booking ride to $dest...", Toast.LENGTH_SHORT).show()
                    },
                    onConfirmCancelRide = {
                        activePassengerRide?.let { viewModel.cancelActiveRide(it.id) }
                        Toast.makeText(context, "Ride cancelled via AI Assistant", Toast.LENGTH_SHORT).show()
                    },
                    onClose = { showAiDialog = false }
                )
            }

            // Safety Center Dialog
            if (showSafetyDialog) {
                SafetyCenterDialog(
                    emergencyContactName = currentUser?.emergencyContactName ?: "David Rivera",
                    emergencyContactPhone = currentUser?.emergencyContactPhone ?: "+91 98840 99887",
                    onClose = { showSafetyDialog = false },
                    onShareLiveTrip = {
                        val text = if (activePassengerRide != null) {
                            "I am on a RideX trip to ${activePassengerRide!!.dropAddress}. Track live: https://ridex.io/track/${activePassengerRide!!.id}"
                        } else {
                            "I'm using RideX Safety Center. My emergency alert is active."
                        }
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, text)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share RideX Emergency Safety Link"))
                    }
                )
            }
        }
    }
}
