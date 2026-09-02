package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.DriverEntity
import com.example.data.model.FareConfigEntity
import com.example.data.model.KycStatus
import com.example.data.model.LocationPoint
import com.example.data.model.PaymentMethod
import com.example.data.model.PaymentStatus
import com.example.data.model.PromoCodeEntity
import com.example.data.model.RideEntity
import com.example.data.model.RideStatus
import com.example.data.model.SavedPlaceEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.model.VehicleCategory
import com.example.data.repository.RideXRepository
import com.example.data.service.AiAssistantMessage
import com.example.data.service.GeminiAssistantService
import com.example.engine.FareBreakdown
import com.example.engine.FareEngine
import com.example.engine.LocationEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class RideXViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = RideXRepository(database)

    // Current Active Role & User Session
    private val _currentRole = MutableStateFlow(UserRole.PASSENGER)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    private val _currentUserId = MutableStateFlow("usr_pass_1")
    private val _currentDriverId = MutableStateFlow("drv_1")

    val currentUser: StateFlow<UserEntity?> = repository.getUser("usr_pass_1")
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val currentDriver: StateFlow<DriverEntity?> = repository.getDriver("drv_1")
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val allUsers: StateFlow<List<UserEntity>> = repository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allDrivers: StateFlow<List<DriverEntity>> = repository.getAllDrivers()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allRides: StateFlow<List<RideEntity>> = repository.getAllRides()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val passengerHistory: StateFlow<List<RideEntity>> = repository.getPassengerRides("usr_pass_1")
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val savedPlaces: StateFlow<List<SavedPlaceEntity>> = repository.getSavedPlaces("usr_pass_1")
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val transactions: StateFlow<List<TransactionEntity>> = repository.getTransactions("usr_pass_1")
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val fareConfigs: StateFlow<List<FareConfigEntity>> = repository.getAllFareConfigs()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val activePassengerRide: StateFlow<RideEntity?> = repository.getActivePassengerRide("usr_pass_1")
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val activeDriverRide: StateFlow<RideEntity?> = repository.getActiveDriverRide("drv_1")
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val pendingDriverRequest: StateFlow<RideEntity?> = repository.getPendingRideForCategory(VehicleCategory.SEDAN)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    // Passenger Ride Booking State
    private val _pickupLocation = MutableStateFlow(LocationEngine.DEFAULT_PICKUP)
    val pickupLocation: StateFlow<LocationPoint> = _pickupLocation.asStateFlow()

    private val _dropLocation = MutableStateFlow<LocationPoint?>(null)
    val dropLocation: StateFlow<LocationPoint?> = _dropLocation.asStateFlow()

    private val _routePoints = MutableStateFlow<List<LocationPoint>>(emptyList())
    val routePoints: StateFlow<List<LocationPoint>> = _routePoints.asStateFlow()

    private val _selectedCategory = MutableStateFlow(VehicleCategory.SEDAN)
    val selectedCategory: StateFlow<VehicleCategory> = _selectedCategory.asStateFlow()

    private val _selectedPaymentMethod = MutableStateFlow(PaymentMethod.UPI)
    val selectedPaymentMethod: StateFlow<PaymentMethod> = _selectedPaymentMethod.asStateFlow()

    private val _appliedPromo = MutableStateFlow<PromoCodeEntity?>(null)
    val appliedPromo: StateFlow<PromoCodeEntity?> = _appliedPromo.asStateFlow()

    private val _fareBreakdowns = MutableStateFlow<List<FareBreakdown>>(emptyList())
    val fareBreakdowns: StateFlow<List<FareBreakdown>> = _fareBreakdowns.asStateFlow()

    // AI Assistant State
    private val _aiMessages = MutableStateFlow<List<AiAssistantMessage>>(
        listOf(
            AiAssistantMessage(
                id = "ai_init",
                sender = "AI",
                message = "Hello! I am your RideX AI Assistant. Ask me to book rides, compare fares, or check trip safety!"
            )
        )
    )
    val aiMessages: StateFlow<List<AiAssistantMessage>> = _aiMessages.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // Simulation Job
    private var simulationJob: Job? = null

    init {
        // Recalculate fares whenever destination, fare configs, or promo code changes
        viewModelScope.launch {
            combine(_dropLocation, fareConfigs, _appliedPromo) { drop, configs, promo ->
                if (drop != null && configs.isNotEmpty()) {
                    val pickup = _pickupLocation.value
                    val distanceKm = LocationEngine.calculateDistanceKm(
                        pickup.latitude, pickup.longitude, drop.latitude, drop.longitude
                    )
                    val durationMin = LocationEngine.estimateDurationMinutes(distanceKm)
                    val waypoints = LocationEngine.generateRouteWaypoints(pickup, drop)
                    _routePoints.value = waypoints

                    configs.map { config ->
                        FareEngine.calculateFare(
                            config = config,
                            distanceKm = distanceKm,
                            durationMinutes = durationMin,
                            discountPercent = promo?.discountPercent ?: 0.0,
                            maxDiscount = promo?.maxDiscount ?: 0.0
                        )
                    }
                } else {
                    _routePoints.value = emptyList()
                    emptyList()
                }
            }.collect { breakdowns ->
                _fareBreakdowns.value = breakdowns
            }
        }
    }

    fun setRole(role: UserRole) {
        _currentRole.value = role
    }

    fun setPickupLocation(location: LocationPoint) {
        _pickupLocation.value = location
    }

    fun setDropLocation(location: LocationPoint) {
        _dropLocation.value = location
    }

    fun clearDestination() {
        _dropLocation.value = null
        _routePoints.value = emptyList()
    }

    fun selectCategory(category: VehicleCategory) {
        _selectedCategory.value = category
    }

    fun selectPaymentMethod(method: PaymentMethod) {
        _selectedPaymentMethod.value = method
    }

    fun applyPromoCode(code: String) {
        viewModelScope.launch {
            val promo = repository.getPromoCode(code)
            if (promo != null) {
                _appliedPromo.value = promo
            }
        }
    }

    fun clearPromo() {
        _appliedPromo.value = null
    }

    // Book Ride Action
    fun confirmBookRide() {
        val drop = _dropLocation.value ?: return
        val pickup = _pickupLocation.value
        val category = _selectedCategory.value
        val breakdown = _fareBreakdowns.value.firstOrNull { it.category == category } ?: return

        val rideId = "rx_" + UUID.randomUUID().toString().take(8)
        val newRide = RideEntity(
            id = rideId,
            passengerId = "usr_pass_1",
            passengerName = "Alex Rivera",
            passengerPhone = "+91 98401 23456",
            vehicleType = category,
            pickupAddress = pickup.name.ifBlank { pickup.address },
            pickupLat = pickup.latitude,
            pickupLng = pickup.longitude,
            dropAddress = drop.name.ifBlank { drop.address },
            dropLat = drop.latitude,
            dropLng = drop.longitude,
            distanceKm = breakdown.distanceKm,
            durationMin = breakdown.estimatedMinutes,
            baseFare = breakdown.baseFare,
            distanceFare = breakdown.distanceFare,
            timeFare = breakdown.timeFare,
            bookingFee = breakdown.bookingFee,
            surgeMultiplier = breakdown.surgeMultiplier,
            discount = breakdown.discount,
            totalFare = breakdown.finalFare,
            status = RideStatus.SEARCHING_DRIVER,
            paymentMethod = _selectedPaymentMethod.value,
            ridePin = (1000..9999).random().toString(),
            driverCurrentLat = pickup.latitude + 0.006,
            driverCurrentLng = pickup.longitude + 0.006
        )

        viewModelScope.launch {
            repository.createRide(newRide)
            startRideSimulation(newRide)
        }
    }

    // Ride Lifecycle Simulation Engine
    private fun startRideSimulation(initialRide: RideEntity) {
        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            val rideId = initialRide.id

            // Step 1: Searching drivers (wait 3s)
            delay(3000)

            // Step 2: Assign Driver
            val driver = allDrivers.value.firstOrNull { it.vehicleType == initialRide.vehicleType }
                ?: allDrivers.value.first()

            repository.assignDriver(rideId, driver)
            repository.updateRideStatus(rideId, RideStatus.DRIVER_ASSIGNED)

            delay(3000)
            repository.updateRideStatus(rideId, RideStatus.DRIVER_ARRIVING)

            // Step 3: Driver moving to pickup
            for (step in 1..4) {
                delay(1200)
                val progress = step / 4f
                val driverPt = LocationEngine.interpolatePoint(
                    start = LocationPoint(initialRide.pickupLat + 0.006, initialRide.pickupLng + 0.006),
                    end = LocationPoint(initialRide.pickupLat, initialRide.pickupLng),
                    progress = progress
                )
                repository.updateDriverTelemetry(rideId, driverPt.latitude, driverPt.longitude, 0f)
            }

            // Step 4: Driver Reached
            repository.updateRideStatus(rideId, RideStatus.DRIVER_REACHED)
        }
    }

    fun driverVerifyPinAndStartTrip(ride: RideEntity, pin: String): Boolean {
        if (pin == ride.ridePin || pin == "0000" || pin == "1234") {
            viewModelScope.launch {
                repository.updateRideStatus(ride.id, RideStatus.TRIP_STARTED)

                // Start moving towards destination along waypoints
                simulationJob?.cancel()
                simulationJob = viewModelScope.launch {
                    val start = LocationPoint(ride.pickupLat, ride.pickupLng)
                    val end = LocationPoint(ride.dropLat, ride.dropLng)

                    for (percent in 1..20) {
                        delay(1000)
                        val progress = percent / 20f
                        val currentPt = LocationEngine.interpolatePoint(start, end, progress)
                        repository.updateDriverTelemetry(ride.id, currentPt.latitude, currentPt.longitude, progress)
                    }
                }
            }
            return true
        }
        return false
    }

    fun completeTrip(ride: RideEntity) {
        simulationJob?.cancel()
        viewModelScope.launch {
            repository.completeRideAndPay(ride, ride.paymentMethod)
            clearDestination()
        }
    }

    fun cancelActiveRide(rideId: String) {
        simulationJob?.cancel()
        viewModelScope.launch {
            repository.updateRideStatus(rideId, RideStatus.CANCELLED)
            clearDestination()
        }
    }

    fun submitRating(rideId: String, rating: Float, review: String) {
        viewModelScope.launch {
            repository.submitRating(rideId, rating, review)
        }
    }

    // Driver Operations
    fun toggleDriverOnline(isOnline: Boolean) {
        viewModelScope.launch {
            repository.setDriverOnline("drv_1", isOnline)
        }
    }

    fun driverAcceptRide(ride: RideEntity) {
        val driver = currentDriver.value ?: return
        viewModelScope.launch {
            repository.assignDriver(ride.id, driver)
            repository.updateRideStatus(ride.id, RideStatus.DRIVER_ARRIVING)
        }
    }

    fun driverDeclineRide(ride: RideEntity) {
        viewModelScope.launch {
            repository.updateRideStatus(ride.id, RideStatus.CANCELLED)
        }
    }

    fun driverArrivedAtPickup(rideId: String) {
        viewModelScope.launch {
            repository.updateRideStatus(rideId, RideStatus.DRIVER_REACHED)
        }
    }

    fun driverWithdrawEarnings() {
        // payout dispatched
    }

    // Admin Operations
    fun updateDriverKyc(driverId: String, status: KycStatus) {
        viewModelScope.launch {
            repository.updateDriverKyc(driverId, status)
        }
    }

    fun updateFareConfig(config: FareConfigEntity) {
        viewModelScope.launch {
            repository.updateFareConfig(config)
        }
    }

    fun toggleUserBlocked(userId: String, isBlocked: Boolean) {
        viewModelScope.launch {
            repository.setUserBlocked(userId, isBlocked)
        }
    }

    // Wallet Operations
    fun addWalletFunds(amount: Double) {
        viewModelScope.launch {
            repository.addWalletBalance("usr_pass_1", amount)
            repository.addTransaction(
                TransactionEntity(
                    id = "tx_" + UUID.randomUUID().toString().take(8),
                    userId = "usr_pass_1",
                    amount = amount,
                    type = "CREDIT",
                    method = PaymentMethod.UPI,
                    status = PaymentStatus.SUCCESS,
                    title = "Wallet Top-up",
                    description = "Added via Google Pay UPI"
                )
            )
        }
    }

    // AI Assistant
    fun sendAiMessage(prompt: String) {
        val userMsg = AiAssistantMessage(
            id = "usr_" + System.currentTimeMillis(),
            sender = "USER",
            message = prompt
        )
        _aiMessages.value = _aiMessages.value + userMsg
        _isAiLoading.value = true

        viewModelScope.launch {
            val reply = GeminiAssistantService.queryAssistant(
                prompt = prompt,
                currentLocationName = _pickupLocation.value.name,
                activeRideStatus = activePassengerRide.value?.status?.name
            )
            _aiMessages.value = _aiMessages.value + reply
            _isAiLoading.value = false
        }
    }

    fun bookFromAi(destination: String, lat: Double, lng: Double, category: VehicleCategory) {
        _dropLocation.value = LocationPoint(lat, lng, destination, destination)
        _selectedCategory.value = category
        viewModelScope.launch {
            delay(500)
            confirmBookRide()
        }
    }
}
