package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.DriverEntity
import com.example.data.model.FareConfigEntity
import com.example.data.model.KycStatus
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
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class RideXRepository(private val database: AppDatabase) {

    // User Operations
    fun getUser(userId: String): Flow<UserEntity?> = database.userDao().getUserById(userId)
    fun getAllUsers(): Flow<List<UserEntity>> = database.userDao().getAllUsers()
    suspend fun updateUser(user: UserEntity) = database.userDao().updateUser(user)
    suspend fun addWalletBalance(userId: String, amount: Double) = database.userDao().addWalletBalance(userId, amount)
    suspend fun deductWalletBalance(userId: String, amount: Double) = database.userDao().deductWalletBalance(userId, amount)
    suspend fun setUserBlocked(userId: String, isBlocked: Boolean) = database.userDao().setBlockedStatus(userId, isBlocked)

    // Driver Operations
    fun getDriver(driverId: String): Flow<DriverEntity?> = database.driverDao().getDriverById(driverId)
    fun getAllDrivers(): Flow<List<DriverEntity>> = database.driverDao().getAllDrivers()
    fun getOnlineVerifiedDrivers(): Flow<List<DriverEntity>> = database.driverDao().getOnlineVerifiedDrivers()
    suspend fun setDriverOnline(driverId: String, isOnline: Boolean) = database.driverDao().setOnlineStatus(driverId, isOnline)
    suspend fun updateDriverKyc(driverId: String, status: KycStatus) = database.driverDao().updateKycStatus(driverId, status)
    suspend fun updateDriver(driver: DriverEntity) = database.driverDao().updateDriver(driver)
    suspend fun updateDriverLocation(driverId: String, lat: Double, lng: Double) = database.driverDao().updateDriverLocation(driverId, lat, lng)

    // Ride Operations
    fun getRide(rideId: String): Flow<RideEntity?> = database.rideDao().getRideById(rideId)
    suspend fun getRideOnce(rideId: String): RideEntity? = database.rideDao().getRideByIdOnce(rideId)
    fun getPassengerRides(passengerId: String): Flow<List<RideEntity>> = database.rideDao().getPassengerRides(passengerId)
    fun getDriverRides(driverId: String): Flow<List<RideEntity>> = database.rideDao().getDriverRides(driverId)
    fun getAllRides(): Flow<List<RideEntity>> = database.rideDao().getAllRides()
    fun getActiveRides(): Flow<List<RideEntity>> = database.rideDao().getActiveRides()
    fun getActivePassengerRide(passengerId: String): Flow<RideEntity?> = database.rideDao().getActivePassengerRide(passengerId)
    fun getActiveDriverRide(driverId: String): Flow<RideEntity?> = database.rideDao().getActiveDriverRide(driverId)
    fun getPendingRideForCategory(category: VehicleCategory): Flow<RideEntity?> = database.rideDao().getPendingRideForDriver(category)

    suspend fun createRide(ride: RideEntity) = database.rideDao().insertRide(ride)
    suspend fun updateRide(ride: RideEntity) = database.rideDao().updateRide(ride)
    suspend fun updateRideStatus(rideId: String, status: RideStatus) = database.rideDao().updateRideStatus(rideId, status)
    suspend fun assignDriver(
        rideId: String,
        driver: DriverEntity
    ) {
        database.rideDao().assignDriverToRide(
            rideId = rideId,
            driverId = driver.id,
            driverName = driver.name,
            driverPhone = driver.phone,
            rating = driver.rating,
            model = driver.vehicleModel,
            number = driver.vehicleNumber,
            color = driver.vehicleColor
        )
    }

    suspend fun updateDriverTelemetry(rideId: String, lat: Double, lng: Double, progress: Float) {
        database.rideDao().updateRideDriverTelemetry(rideId, lat, lng, progress)
    }

    suspend fun completeRideAndPay(
        ride: RideEntity,
        paymentMethod: PaymentMethod
    ) {
        val completedRide = ride.copy(
            status = RideStatus.TRIP_COMPLETED,
            paymentMethod = paymentMethod,
            paymentStatus = PaymentStatus.SUCCESS,
            completedAt = System.currentTimeMillis()
        )
        database.rideDao().updateRide(completedRide)

        // Credit driver earnings (85% net of platform 15% commission)
        val driverEarning = ride.totalFare * 0.85
        if (ride.driverId.isNotEmpty()) {
            database.driverDao().addTripEarnings(ride.driverId, driverEarning)
        }

        // Deduct from wallet if wallet payment
        if (paymentMethod == PaymentMethod.WALLET) {
            database.userDao().deductWalletBalance(ride.passengerId, ride.totalFare)
        }

        // Record Transaction
        database.transactionDao().insertTransaction(
            TransactionEntity(
                id = "tx_" + UUID.randomUUID().toString().take(8),
                userId = ride.passengerId,
                rideId = ride.id,
                amount = ride.totalFare,
                type = "DEBIT",
                method = paymentMethod,
                status = PaymentStatus.SUCCESS,
                title = "Ride Payment: ${ride.pickupAddress.take(20)}... to ${ride.dropAddress.take(20)}...",
                description = "Paid via $paymentMethod"
            )
        )
    }

    suspend fun submitRating(rideId: String, rating: Float, review: String) {
        val ride = database.rideDao().getRideByIdOnce(rideId)
        if (ride != null) {
            val updated = ride.copy(
                passengerRatingForDriver = rating,
                passengerReview = review
            )
            database.rideDao().updateRide(updated)
        }
    }

    // Fare Configs
    fun getAllFareConfigs(): Flow<List<FareConfigEntity>> = database.fareConfigDao().getAllFareConfigs()
    suspend fun getFareConfig(type: VehicleCategory): FareConfigEntity? = database.fareConfigDao().getFareConfig(type)
    suspend fun updateFareConfig(config: FareConfigEntity) = database.fareConfigDao().updateFareConfig(config)

    // Saved Places
    fun getSavedPlaces(userId: String): Flow<List<SavedPlaceEntity>> = database.savedPlaceDao().getSavedPlaces(userId)
    suspend fun insertSavedPlace(place: SavedPlaceEntity) = database.savedPlaceDao().insertSavedPlace(place)
    suspend fun deleteSavedPlace(id: Long) = database.savedPlaceDao().deleteSavedPlace(id)

    // Transactions
    fun getTransactions(userId: String): Flow<List<TransactionEntity>> = database.transactionDao().getTransactions(userId)
    fun getAllTransactions(): Flow<List<TransactionEntity>> = database.transactionDao().getAllTransactions()
    suspend fun addTransaction(transaction: TransactionEntity) = database.transactionDao().insertTransaction(transaction)

    // Promo Codes
    fun getActivePromoCodes(): Flow<List<PromoCodeEntity>> = database.promoCodeDao().getActivePromoCodes()
    suspend fun getPromoCode(code: String): PromoCodeEntity? = database.promoCodeDao().getPromoCode(code)
}
