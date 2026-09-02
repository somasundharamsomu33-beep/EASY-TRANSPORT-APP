package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.NumberFormat
import java.util.Locale

enum class UserRole {
    PASSENGER,
    DRIVER,
    ADMIN
}

enum class VehicleCategory(
    val title: String,
    val capacity: Int,
    val iconName: String,
    val description: String
) {
    BIKE("RideX Moto", 1, "two_wheeler", "Fastest for solo trips & traffic"),
    AUTO("RideX Auto", 3, "electric_rickshaw", "Affordable doorstep 3-wheeler"),
    MINI("RideX Mini", 4, "directions_car", "Comfy everyday hatchbacks"),
    SEDAN("RideX Sedan", 4, "airport_shuttle", "Spacious sedans with top drivers"),
    SUV("RideX SUV", 6, "directions_car_filled", "6-seater spacious family rides"),
    PREMIUM("RideX Lux", 4, "local_taxi", "Luxury luxury sedans & executive service")
}

enum class RideStatus {
    REQUESTED,
    SEARCHING_DRIVER,
    DRIVER_ASSIGNED,
    DRIVER_ARRIVING,
    DRIVER_REACHED,
    TRIP_STARTED,
    TRIP_COMPLETED,
    CANCELLED
}

enum class KycStatus {
    PENDING,
    VERIFIED,
    REJECTED,
    SUSPENDED
}

enum class PaymentMethod {
    CASH,
    UPI,
    CARD,
    WALLET
}

enum class PaymentStatus {
    PENDING,
    PROCESSING,
    SUCCESS,
    FAILED,
    REFUNDED
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: UserRole = UserRole.PASSENGER,
    val profilePhotoUrl: String = "",
    val rating: Double = 4.85,
    val walletBalance: Double = 350.0,
    val isBlocked: Boolean = false,
    val emergencyContactName: String = "Mom",
    val emergencyContactPhone: String = "+91 98765 43210"
)

@Entity(tableName = "drivers")
data class DriverEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val email: String,
    val profilePhotoUrl: String = "",
    val vehicleType: VehicleCategory = VehicleCategory.SEDAN,
    val vehicleModel: String = "Honda City",
    val vehicleNumber: String = "DL 01 AB 8842",
    val vehicleColor: String = "Pearl White",
    val kycStatus: KycStatus = KycStatus.VERIFIED,
    val isOnline: Boolean = true,
    val currentLat: Double = 13.0827,
    val currentLng: Double = 80.2707,
    val rating: Double = 4.92,
    val totalRides: Int = 412,
    val todayEarnings: Double = 1840.0,
    val weeklyEarnings: Double = 12450.0,
    val monthlyEarnings: Double = 48600.0,
    val licenseNumber: String = "DL-1420110012345",
    val rcNumber: String = "RC-9988221",
    val insuranceNumber: String = "INS-7744110",
    val bankAccount: String = "HDFC Bank **** 4892"
)

@Entity(tableName = "rides")
data class RideEntity(
    @PrimaryKey val id: String,
    val passengerId: String,
    val passengerName: String,
    val passengerPhone: String,
    val driverId: String = "",
    val driverName: String = "",
    val driverPhone: String = "",
    val driverPhotoUrl: String = "",
    val driverRating: Double = 4.9,
    val vehicleType: VehicleCategory = VehicleCategory.SEDAN,
    val vehicleModel: String = "",
    val vehicleNumber: String = "",
    val vehicleColor: String = "",
    val pickupAddress: String,
    val pickupLat: Double,
    val pickupLng: Double,
    val dropAddress: String,
    val dropLat: Double,
    val dropLng: Double,
    val distanceKm: Double,
    val durationMin: Int,
    val baseFare: Double,
    val distanceFare: Double,
    val timeFare: Double,
    val bookingFee: Double,
    val surgeMultiplier: Double = 1.0,
    val discount: Double = 0.0,
    val totalFare: Double,
    val status: RideStatus = RideStatus.REQUESTED,
    val paymentMethod: PaymentMethod = PaymentMethod.UPI,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val ridePin: String = "4821",
    val passengerRatingForDriver: Float? = null,
    val passengerReview: String = "",
    val driverRatingForPassenger: Float? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val driverCurrentLat: Double = 13.0827,
    val driverCurrentLng: Double = 80.2707,
    val routeProgressPercent: Float = 0f
)

@Entity(tableName = "fare_configs")
data class FareConfigEntity(
    @PrimaryKey val vehicleType: VehicleCategory,
    val baseFare: Double,
    val perKmRate: Double,
    val perMinuteRate: Double,
    val bookingFee: Double,
    val surgeMultiplier: Double = 1.0,
    val minFare: Double,
    val capacity: Int,
    val description: String
)

@Entity(tableName = "saved_places")
data class SavedPlaceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val title: String,
    val address: String,
    val lat: Double,
    val lng: Double,
    val iconType: String = "home" // home, work, airport, gym, favorite
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val rideId: String? = null,
    val amount: Double,
    val type: String, // CREDIT, DEBIT, REFUND, PAYOUT
    val method: PaymentMethod,
    val status: PaymentStatus,
    val timestamp: Long = System.currentTimeMillis(),
    val title: String,
    val description: String
)

@Entity(tableName = "promo_codes")
data class PromoCodeEntity(
    @PrimaryKey val code: String,
    val title: String,
    val description: String,
    val discountPercent: Double,
    val maxDiscount: Double,
    val minRideAmount: Double = 100.0,
    val isActive: Boolean = true
)

data class LocationPoint(
    val latitude: Double,
    val longitude: Double,
    val name: String = "",
    val address: String = ""
)

fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    return formatter.format(amount).replace("INR", "₹").trim()
}
