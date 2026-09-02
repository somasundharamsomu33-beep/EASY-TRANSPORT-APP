package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DriverEntity
import com.example.data.model.FareConfigEntity
import com.example.data.model.KycStatus
import com.example.data.model.PromoCodeEntity
import com.example.data.model.RideEntity
import com.example.data.model.RideStatus
import com.example.data.model.SavedPlaceEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.UserEntity
import com.example.data.model.VehicleCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users ORDER BY name ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET walletBalance = walletBalance + :amount WHERE id = :userId")
    suspend fun addWalletBalance(userId: String, amount: Double)

    @Query("UPDATE users SET walletBalance = walletBalance - :amount WHERE id = :userId")
    suspend fun deductWalletBalance(userId: String, amount: Double)

    @Query("UPDATE users SET isBlocked = :isBlocked WHERE id = :userId")
    suspend fun setBlockedStatus(userId: String, isBlocked: Boolean)
}

@Dao
interface DriverDao {
    @Query("SELECT * FROM drivers WHERE id = :driverId")
    fun getDriverById(driverId: String): Flow<DriverEntity?>

    @Query("SELECT * FROM drivers ORDER BY isOnline DESC, rating DESC")
    fun getAllDrivers(): Flow<List<DriverEntity>>

    @Query("SELECT * FROM drivers WHERE isOnline = 1 AND kycStatus = 'VERIFIED'")
    fun getOnlineVerifiedDrivers(): Flow<List<DriverEntity>>

    @Query("SELECT * FROM drivers WHERE isOnline = 1 AND vehicleType = :category AND kycStatus = 'VERIFIED'")
    suspend fun getAvailableDriversForCategory(category: VehicleCategory): List<DriverEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDriver(driver: DriverEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrivers(drivers: List<DriverEntity>)

    @Update
    suspend fun updateDriver(driver: DriverEntity)

    @Query("UPDATE drivers SET isOnline = :isOnline WHERE id = :driverId")
    suspend fun setOnlineStatus(driverId: String, isOnline: Boolean)

    @Query("UPDATE drivers SET kycStatus = :status WHERE id = :driverId")
    suspend fun updateKycStatus(driverId: String, status: KycStatus)

    @Query("UPDATE drivers SET currentLat = :lat, currentLng = :lng WHERE id = :driverId")
    suspend fun updateDriverLocation(driverId: String, lat: Double, lng: Double)

    @Query("UPDATE drivers SET todayEarnings = todayEarnings + :amount, totalRides = totalRides + 1 WHERE id = :driverId")
    suspend fun addTripEarnings(driverId: String, amount: Double)
}

@Dao
interface RideDao {
    @Query("SELECT * FROM rides WHERE id = :rideId")
    fun getRideById(rideId: String): Flow<RideEntity?>

    @Query("SELECT * FROM rides WHERE id = :rideId")
    suspend fun getRideByIdOnce(rideId: String): RideEntity?

    @Query("SELECT * FROM rides WHERE passengerId = :passengerId ORDER BY createdAt DESC")
    fun getPassengerRides(passengerId: String): Flow<List<RideEntity>>

    @Query("SELECT * FROM rides WHERE driverId = :driverId ORDER BY createdAt DESC")
    fun getDriverRides(driverId: String): Flow<List<RideEntity>>

    @Query("SELECT * FROM rides ORDER BY createdAt DESC")
    fun getAllRides(): Flow<List<RideEntity>>

    @Query("SELECT * FROM rides WHERE status IN ('REQUESTED', 'SEARCHING_DRIVER', 'DRIVER_ASSIGNED', 'DRIVER_ARRIVING', 'DRIVER_REACHED', 'TRIP_STARTED') ORDER BY createdAt DESC")
    fun getActiveRides(): Flow<List<RideEntity>>

    @Query("SELECT * FROM rides WHERE passengerId = :passengerId AND status NOT IN ('TRIP_COMPLETED', 'CANCELLED') LIMIT 1")
    fun getActivePassengerRide(passengerId: String): Flow<RideEntity?>

    @Query("SELECT * FROM rides WHERE driverId = :driverId AND status NOT IN ('TRIP_COMPLETED', 'CANCELLED') LIMIT 1")
    fun getActiveDriverRide(driverId: String): Flow<RideEntity?>

    @Query("SELECT * FROM rides WHERE status = 'REQUESTED' AND vehicleType = :category LIMIT 1")
    fun getPendingRideForDriver(category: VehicleCategory): Flow<RideEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRide(ride: RideEntity)

    @Update
    suspend fun updateRide(ride: RideEntity)

    @Query("UPDATE rides SET status = :status WHERE id = :rideId")
    suspend fun updateRideStatus(rideId: String, status: RideStatus)

    @Query("UPDATE rides SET driverId = :driverId, driverName = :driverName, driverPhone = :driverPhone, driverRating = :rating, vehicleModel = :model, vehicleNumber = :number, vehicleColor = :color, status = 'DRIVER_ASSIGNED' WHERE id = :rideId")
    suspend fun assignDriverToRide(
        rideId: String,
        driverId: String,
        driverName: String,
        driverPhone: String,
        rating: Double,
        model: String,
        number: String,
        color: String
    )

    @Query("UPDATE rides SET driverCurrentLat = :lat, driverCurrentLng = :lng, routeProgressPercent = :progress WHERE id = :rideId")
    suspend fun updateRideDriverTelemetry(rideId: String, lat: Double, lng: Double, progress: Float)
}

@Dao
interface FareConfigDao {
    @Query("SELECT * FROM fare_configs")
    fun getAllFareConfigs(): Flow<List<FareConfigEntity>>

    @Query("SELECT * FROM fare_configs WHERE vehicleType = :type")
    suspend fun getFareConfig(type: VehicleCategory): FareConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFareConfigs(configs: List<FareConfigEntity>)

    @Update
    suspend fun updateFareConfig(config: FareConfigEntity)
}

@Dao
interface SavedPlaceDao {
    @Query("SELECT * FROM saved_places WHERE userId = :userId")
    fun getSavedPlaces(userId: String): Flow<List<SavedPlaceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedPlace(place: SavedPlaceEntity)

    @Query("DELETE FROM saved_places WHERE id = :id")
    suspend fun deleteSavedPlace(id: Long)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY timestamp DESC")
    fun getTransactions(userId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)
}

@Dao
interface PromoCodeDao {
    @Query("SELECT * FROM promo_codes WHERE isActive = 1")
    fun getActivePromoCodes(): Flow<List<PromoCodeEntity>>

    @Query("SELECT * FROM promo_codes WHERE code = :code AND isActive = 1")
    suspend fun getPromoCode(code: String): PromoCodeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPromoCodes(codes: List<PromoCodeEntity>)
}
