package com.example.engine

import com.example.data.model.FareConfigEntity
import com.example.data.model.VehicleCategory
import kotlin.math.max
import kotlin.math.roundToInt

data class FareBreakdown(
    val category: VehicleCategory,
    val baseFare: Double,
    val distanceFare: Double,
    val timeFare: Double,
    val bookingFee: Double,
    val surgeMultiplier: Double,
    val subtotal: Double,
    val discount: Double,
    val finalFare: Double,
    val estimatedMinutes: Int,
    val distanceKm: Double,
    val capacity: Int
)

object FareEngine {

    fun calculateFare(
        config: FareConfigEntity,
        distanceKm: Double,
        durationMinutes: Int,
        discountPercent: Double = 0.0,
        maxDiscount: Double = 0.0
    ): FareBreakdown {
        val base = config.baseFare
        val distFare = distanceKm * config.perKmRate
        val timeFare = durationMinutes * config.perMinuteRate
        val booking = config.bookingFee
        val surge = config.surgeMultiplier

        val rawFare = (base + distFare + timeFare + booking) * surge
        val subtotal = max(config.minFare, rawFare)

        var discountAmount = 0.0
        if (discountPercent > 0.0) {
            val calcDiscount = subtotal * discountPercent
            discountAmount = if (maxDiscount > 0.0) calcDiscount.coerceAtMost(maxDiscount) else calcDiscount
        }

        val finalFare = max(config.minFare, subtotal - discountAmount)

        return FareBreakdown(
            category = config.vehicleType,
            baseFare = round2(base),
            distanceFare = round2(distFare),
            timeFare = round2(timeFare),
            bookingFee = round2(booking),
            surgeMultiplier = surge,
            subtotal = round2(subtotal),
            discount = round2(discountAmount),
            finalFare = round2(finalFare),
            estimatedMinutes = durationMinutes,
            distanceKm = round2(distanceKm),
            capacity = config.capacity
        )
    }

    private fun round2(value: Double): Double {
        return (value * 100.0).roundToInt() / 100.0
    }
}
