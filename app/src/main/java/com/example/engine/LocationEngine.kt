package com.example.engine

import com.example.data.model.LocationPoint
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object LocationEngine {

    val DEFAULT_PICKUP = LocationPoint(
        latitude = 13.0827,
        longitude = 80.2707,
        name = "Current Location",
        address = "Chennai Central Station, Park Town"
    )

    val PRESET_PLACES = listOf(
        LocationPoint(
            latitude = 13.0827,
            longitude = 80.2707,
            name = "Chennai Central Railway Station",
            address = "Park Town, Grand Southern Trunk Road"
        ),
        LocationPoint(
            latitude = 12.9941,
            longitude = 80.1709,
            name = "Chennai International Airport (MAA)",
            address = "GST Road, Meenambakkam, Terminal 2"
        ),
        LocationPoint(
            latitude = 12.9925,
            longitude = 80.2173,
            name = "Phoenix Marketcity",
            address = "Velachery Main Road, Indira Gandhi Nagar"
        ),
        LocationPoint(
            latitude = 13.0033,
            longitude = 80.1742,
            name = "DLF CyberCity IT Tech Park",
            address = "Mount Poonamallee High Road, Manapakkam"
        ),
        LocationPoint(
            latitude = 13.0588,
            longitude = 80.2642,
            name = "Express Avenue Mall",
            address = "Club House Road, Mount Road, Royapettah"
        ),
        LocationPoint(
            latitude = 13.0499,
            longitude = 80.2824,
            name = "Marina Beach Promenade",
            address = "Kamarajar Salai, Triplicane"
        ),
        LocationPoint(
            latitude = 12.9863,
            longitude = 80.2432,
            name = "TIDEL Park & Ascendas IT Expressway",
            address = "Rajiv Gandhi Salai, Taramani"
        ),
        LocationPoint(
            latitude = 13.0336,
            longitude = 80.2337,
            name = "T. Nagar Shopping District",
            address = "Pondy Bazaar & Ranganathan Street"
        ),
        LocationPoint(
            latitude = 13.0405,
            longitude = 80.2505,
            name = "Apollo Speciality Hospitals",
            address = "Greams Road, Thousand Lights"
        ),
        LocationPoint(
            latitude = 12.9716,
            longitude = 80.2184,
            name = "Velachery MRTS Railway Station",
            address = "Velachery Main Road"
        )
    )

    fun calculateDistanceKm(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val dist = r * c
        // Urban road route factor (approx 1.25x direct Euclidean)
        return (dist * 1.25 * 10.0).toInt() / 10.0
    }

    fun estimateDurationMinutes(distanceKm: Double): Int {
        // Average urban speed: ~22 km/h + 3 min traffic base buffer
        val hours = distanceKm / 22.0
        val mins = (hours * 60).toInt() + 3
        return mins.coerceAtLeast(4)
    }

    fun generateRouteWaypoints(
        start: LocationPoint,
        end: LocationPoint,
        numPoints: Int = 12
    ): List<LocationPoint> {
        val list = mutableListOf<LocationPoint>()
        for (i in 0..numPoints) {
            val fraction = i.toDouble() / numPoints.toDouble()
            // Add subtle natural street bends to polyline
            val bendFactor = sin(fraction * Math.PI) * 0.003
            val lat = start.latitude + (end.latitude - start.latitude) * fraction + bendFactor
            val lng = start.longitude + (end.longitude - start.longitude) * fraction - (bendFactor * 0.5)
            list.add(
                LocationPoint(
                    latitude = lat,
                    longitude = lng,
                    name = "Waypoint $i"
                )
            )
        }
        return list
    }

    fun interpolatePoint(
        start: LocationPoint,
        end: LocationPoint,
        progress: Float
    ): LocationPoint {
        val lat = start.latitude + (end.latitude - start.latitude) * progress
        val lng = start.longitude + (end.longitude - start.longitude) * progress
        return LocationPoint(latitude = lat, longitude = lng)
    }
}
