package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Converters {
    @TypeConverter
    fun fromUserRole(value: UserRole): String = value.name

    @TypeConverter
    fun toUserRole(value: String): UserRole = enumValueOf(value)

    @TypeConverter
    fun fromVehicleCategory(value: VehicleCategory): String = value.name

    @TypeConverter
    fun toVehicleCategory(value: String): VehicleCategory = enumValueOf(value)

    @TypeConverter
    fun fromRideStatus(value: RideStatus): String = value.name

    @TypeConverter
    fun toRideStatus(value: String): RideStatus = enumValueOf(value)

    @TypeConverter
    fun fromKycStatus(value: KycStatus): String = value.name

    @TypeConverter
    fun toKycStatus(value: String): KycStatus = enumValueOf(value)

    @TypeConverter
    fun fromPaymentMethod(value: PaymentMethod): String = value.name

    @TypeConverter
    fun toPaymentMethod(value: String): PaymentMethod = enumValueOf(value)

    @TypeConverter
    fun fromPaymentStatus(value: PaymentStatus): String = value.name

    @TypeConverter
    fun toPaymentStatus(value: String): PaymentStatus = enumValueOf(value)
}

@Database(
    entities = [
        UserEntity::class,
        DriverEntity::class,
        RideEntity::class,
        FareConfigEntity::class,
        SavedPlaceEntity::class,
        TransactionEntity::class,
        PromoCodeEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun driverDao(): DriverDao
    abstract fun rideDao(): RideDao
    abstract fun fareConfigDao(): FareConfigDao
    abstract fun savedPlaceDao(): SavedPlaceDao
    abstract fun transactionDao(): TransactionDao
    abstract fun promoCodeDao(): PromoCodeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ridex_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            // Seed Default Users (Passenger, Driver, Admin)
            val passenger = UserEntity(
                id = "usr_pass_1",
                name = "Alex Rivera",
                email = "alex.rivera@example.com",
                phone = "+91 98401 23456",
                role = UserRole.PASSENGER,
                profilePhotoUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
                rating = 4.92,
                walletBalance = 750.0,
                emergencyContactName = "David Rivera (Brother)",
                emergencyContactPhone = "+91 98840 99887"
            )

            val adminUser = UserEntity(
                id = "usr_admin_1",
                name = "Platform Supervisor",
                email = "admin@ridex.io",
                phone = "+91 99000 11223",
                role = UserRole.ADMIN,
                rating = 5.0,
                walletBalance = 25000.0
            )

            database.userDao().insertUser(passenger)
            database.userDao().insertUser(adminUser)

            // Seed Drivers across categories
            val drivers = listOf(
                DriverEntity(
                    id = "drv_1",
                    name = "Rajesh Kumar",
                    phone = "+91 98765 43210",
                    email = "rajesh.k@ridex.com",
                    vehicleType = VehicleCategory.SEDAN,
                    vehicleModel = "Honda City VX",
                    vehicleNumber = "TN 09 AB 4589",
                    vehicleColor = "Silver Frost",
                    kycStatus = KycStatus.VERIFIED,
                    isOnline = true,
                    currentLat = 13.0827,
                    currentLng = 80.2707,
                    rating = 4.94,
                    totalRides = 580,
                    todayEarnings = 2350.0,
                    weeklyEarnings = 14800.0,
                    monthlyEarnings = 54200.0
                ),
                DriverEntity(
                    id = "drv_2",
                    name = "Manoj Sharma",
                    phone = "+91 98765 11223",
                    email = "manoj.auto@ridex.com",
                    vehicleType = VehicleCategory.AUTO,
                    vehicleModel = "Bajaj Compact RE",
                    vehicleNumber = "TN 01 CZ 2201",
                    vehicleColor = "Yellow & Green",
                    kycStatus = KycStatus.VERIFIED,
                    isOnline = true,
                    currentLat = 13.0850,
                    currentLng = 80.2750,
                    rating = 4.88,
                    totalRides = 890,
                    todayEarnings = 1420.0
                ),
                DriverEntity(
                    id = "drv_3",
                    name = "Karthik Raja",
                    phone = "+91 98402 77889",
                    email = "karthik.moto@ridex.com",
                    vehicleType = VehicleCategory.BIKE,
                    vehicleModel = "Yamaha FZ-S",
                    vehicleNumber = "TN 02 DX 7711",
                    vehicleColor = "Midnight Matte Blue",
                    kycStatus = KycStatus.VERIFIED,
                    isOnline = true,
                    currentLat = 13.0790,
                    currentLng = 80.2680,
                    rating = 4.96,
                    totalRides = 1240,
                    todayEarnings = 1150.0
                ),
                DriverEntity(
                    id = "drv_4",
                    name = "Suresh Menon",
                    phone = "+91 97908 44332",
                    email = "suresh.suv@ridex.com",
                    vehicleType = VehicleCategory.SUV,
                    vehicleModel = "Toyota Innova Crysta",
                    vehicleNumber = "TN 07 EX 8008",
                    vehicleColor = "Garnet Red",
                    kycStatus = KycStatus.VERIFIED,
                    isOnline = true,
                    currentLat = 13.0880,
                    currentLng = 80.2620,
                    rating = 4.97,
                    totalRides = 430,
                    todayEarnings = 3100.0
                ),
                DriverEntity(
                    id = "drv_5",
                    name = "Vikram Aditya",
                    phone = "+91 96001 98765",
                    email = "vikram.mini@ridex.com",
                    vehicleType = VehicleCategory.MINI,
                    vehicleModel = "Maruti Swift VXi",
                    vehicleNumber = "TN 05 MN 3399",
                    vehicleColor = "Arctic White",
                    kycStatus = KycStatus.PENDING,
                    isOnline = false,
                    currentLat = 13.0750,
                    currentLng = 80.2800,
                    rating = 4.75,
                    totalRides = 120,
                    todayEarnings = 0.0
                ),
                DriverEntity(
                    id = "drv_6",
                    name = "Arjun Kapoor",
                    phone = "+91 95512 34567",
                    email = "arjun.lux@ridex.com",
                    vehicleType = VehicleCategory.PREMIUM,
                    vehicleModel = "Audi A6 Matrix",
                    vehicleNumber = "TN 04 LX 0007",
                    vehicleColor = "Mythos Black Metallic",
                    kycStatus = KycStatus.VERIFIED,
                    isOnline = true,
                    currentLat = 13.0810,
                    currentLng = 80.2740,
                    rating = 4.99,
                    totalRides = 210,
                    todayEarnings = 4200.0
                )
            )
            database.driverDao().insertDrivers(drivers)

            // Seed Configurable Fare Settings (Per Vehicle Category)
            val fareConfigs = listOf(
                FareConfigEntity(
                    vehicleType = VehicleCategory.BIKE,
                    baseFare = 25.0,
                    perKmRate = 8.0,
                    perMinuteRate = 1.0,
                    bookingFee = 5.0,
                    surgeMultiplier = 1.0,
                    minFare = 30.0,
                    capacity = 1,
                    description = "Fast, affordable, zip past city jams"
                ),
                FareConfigEntity(
                    vehicleType = VehicleCategory.AUTO,
                    baseFare = 35.0,
                    perKmRate = 12.0,
                    perMinuteRate = 1.5,
                    bookingFee = 8.0,
                    surgeMultiplier = 1.0,
                    minFare = 45.0,
                    capacity = 3,
                    description = "Popular 3-wheel doorstep ride"
                ),
                FareConfigEntity(
                    vehicleType = VehicleCategory.MINI,
                    baseFare = 50.0,
                    perKmRate = 14.0,
                    perMinuteRate = 2.0,
                    bookingFee = 12.0,
                    surgeMultiplier = 1.0,
                    minFare = 70.0,
                    capacity = 4,
                    description = "Comfy everyday air-conditioned hatchbacks"
                ),
                FareConfigEntity(
                    vehicleType = VehicleCategory.SEDAN,
                    baseFare = 70.0,
                    perKmRate = 17.0,
                    perMinuteRate = 2.5,
                    bookingFee = 15.0,
                    surgeMultiplier = 1.0,
                    minFare = 100.0,
                    capacity = 4,
                    description = "Prime sedans with high-rated captains"
                ),
                FareConfigEntity(
                    vehicleType = VehicleCategory.SUV,
                    baseFare = 110.0,
                    perKmRate = 22.0,
                    perMinuteRate = 3.0,
                    bookingFee = 20.0,
                    surgeMultiplier = 1.0,
                    minFare = 150.0,
                    capacity = 6,
                    description = "Spacious 6-seaters for family & extra luggage"
                ),
                FareConfigEntity(
                    vehicleType = VehicleCategory.PREMIUM,
                    baseFare = 180.0,
                    perKmRate = 32.0,
                    perMinuteRate = 4.5,
                    bookingFee = 30.0,
                    surgeMultiplier = 1.0,
                    minFare = 250.0,
                    capacity = 4,
                    description = "Executive luxury cars with top-tier service"
                )
            )
            database.fareConfigDao().insertFareConfigs(fareConfigs)

            // Seed Saved Places
            val savedPlaces = listOf(
                SavedPlaceEntity(
                    userId = "usr_pass_1",
                    title = "Home",
                    address = "14, Greenways Road, RA Puram, Chennai",
                    lat = 13.0232,
                    lng = 80.2541,
                    iconType = "home"
                ),
                SavedPlaceEntity(
                    userId = "usr_pass_1",
                    title = "Work",
                    address = "DLF CyberCity, IT Expressway, Chennai",
                    lat = 13.0033,
                    lng = 80.1742,
                    iconType = "work"
                ),
                SavedPlaceEntity(
                    userId = "usr_pass_1",
                    title = "Chennai Airport T2",
                    address = "Meenambakkam International Terminal, Chennai",
                    lat = 12.9941,
                    lng = 80.1709,
                    iconType = "airport"
                )
            )
            savedPlaces.forEach { database.savedPlaceDao().insertSavedPlace(it) }

            // Seed Promo Codes
            val promoCodes = listOf(
                PromoCodeEntity(
                    code = "RIDEX50",
                    title = "50% Off First Ride",
                    description = "Get 50% discount up to ₹100 on any ride",
                    discountPercent = 0.50,
                    maxDiscount = 100.0,
                    minRideAmount = 80.0
                ),
                PromoCodeEntity(
                    code = "SAVER20",
                    title = "Flat 20% Off",
                    description = "Save 20% up to ₹50 on Sedan & SUV rides",
                    discountPercent = 0.20,
                    maxDiscount = 50.0,
                    minRideAmount = 150.0
                ),
                PromoCodeEntity(
                    code = "WEEKENDVIP",
                    title = "Weekend Lux",
                    description = "Flat ₹150 off on Premium & SUV bookings",
                    discountPercent = 0.30,
                    maxDiscount = 150.0,
                    minRideAmount = 300.0
                )
            )
            database.promoCodeDao().insertPromoCodes(promoCodes)

            // Seed Completed History
            val samplePastRide = RideEntity(
                id = "rx_ride_9918",
                passengerId = "usr_pass_1",
                passengerName = "Alex Rivera",
                passengerPhone = "+91 98401 23456",
                driverId = "drv_1",
                driverName = "Rajesh Kumar",
                driverPhone = "+91 98765 43210",
                driverRating = 4.94,
                vehicleType = VehicleCategory.SEDAN,
                vehicleModel = "Honda City VX",
                vehicleNumber = "TN 09 AB 4589",
                vehicleColor = "Silver Frost",
                pickupAddress = "Phoenix Marketcity Mall, Velachery",
                pickupLat = 12.9925,
                pickupLng = 80.2173,
                dropAddress = "Express Avenue Mall, Royapettah",
                dropLat = 13.0588,
                dropLng = 80.2642,
                distanceKm = 11.4,
                durationMin = 26,
                baseFare = 70.0,
                distanceFare = 193.8,
                timeFare = 65.0,
                bookingFee = 15.0,
                surgeMultiplier = 1.0,
                discount = 50.0,
                totalFare = 293.8,
                status = RideStatus.TRIP_COMPLETED,
                paymentMethod = PaymentMethod.UPI,
                paymentStatus = PaymentStatus.SUCCESS,
                passengerRatingForDriver = 5.0f,
                passengerReview = "Punctual driver, clean AC car and very safe driving!",
                createdAt = System.currentTimeMillis() - 86400000L,
                startedAt = System.currentTimeMillis() - 86400000L + 600000L,
                completedAt = System.currentTimeMillis() - 86400000L + 2160000L
            )
            database.rideDao().insertRide(samplePastRide)

            // Seed Transaction
            database.transactionDao().insertTransaction(
                TransactionEntity(
                    id = "tx_88192",
                    userId = "usr_pass_1",
                    rideId = "rx_ride_9918",
                    amount = 293.8,
                    type = "DEBIT",
                    method = PaymentMethod.UPI,
                    status = PaymentStatus.SUCCESS,
                    timestamp = System.currentTimeMillis() - 86400000L + 2160000L,
                    title = "Ride Payment: Phoenix Mall to Express Ave",
                    description = "Paid via Google Pay UPI"
                )
            )
        }
    }
}
