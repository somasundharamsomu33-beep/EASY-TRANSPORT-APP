package com.example.data.service

import com.example.BuildConfig
import com.example.data.model.VehicleCategory
import com.example.engine.LocationEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class AiAssistantAction {
    data class BookRideProposed(
        val destination: String,
        val destinationLat: Double,
        val destinationLng: Double,
        val category: VehicleCategory,
        val estimatedFare: String,
        val summary: String
    ) : AiAssistantAction()

    data class CancelRideProposed(
        val rideId: String,
        val summary: String
    ) : AiAssistantAction()

    data class ShowFareEstimate(
        val destination: String,
        val categoryFares: List<Pair<VehicleCategory, Double>>,
        val summary: String
    ) : AiAssistantAction()

    data class DriverStatusInfo(
        val driverName: String,
        val status: String,
        val etaMinutes: Int,
        val summary: String
    ) : AiAssistantAction()

    data class GeneralResponse(
        val text: String
    ) : AiAssistantAction()
}

data class AiAssistantMessage(
    val id: String,
    val sender: String, // "USER" or "AI"
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val proposedAction: AiAssistantAction? = null,
    val isPendingConfirmation: Boolean = false
)

object GeminiAssistantService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun queryAssistant(
        prompt: String,
        currentLocationName: String,
        activeRideStatus: String?
    ): AiAssistantMessage = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        // Check for local smart rule matches or call Gemini REST
        val localAction = parseLocalIntent(prompt, currentLocationName, activeRideStatus)

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY" && !apiKey.startsWith("YOUR_")) {
            try {
                val systemPrompt = """
                    You are RideX AI Ride Assistant for the RideX ride-hailing platform.
                    Your goal is to assist passengers with booking, checking fare estimates, finding closest rides, canceling rides safely, and safety guides.
                    For booking or canceling, always be clear and specify exact details so the user can confirm.
                    Current user pickup: $currentLocationName
                    Active ride state: ${activeRideStatus ?: "None"}
                """.trimIndent()

                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
                val jsonBody = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("parts", JSONArray().apply {
                                put(JSONObject().put("text", "$systemPrompt\n\nUser request: $prompt"))
                            })
                        })
                    })
                }

                val request = Request.Builder()
                    .url(url)
                    .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseStr = response.body?.string() ?: ""
                    val rootJson = JSONObject(responseStr)
                    val candidates = rootJson.optJSONArray("candidates")
                    val firstCandidate = candidates?.optJSONObject(0)
                    val content = firstCandidate?.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    val replyText = parts?.optJSONObject(0)?.optString("text")

                    if (!replyText.isNullOrBlank()) {
                        return@withContext AiAssistantMessage(
                            id = "ai_" + System.currentTimeMillis(),
                            sender = "AI",
                            message = replyText,
                            proposedAction = localAction,
                            isPendingConfirmation = localAction is AiAssistantAction.BookRideProposed || localAction is AiAssistantAction.CancelRideProposed
                        )
                    }
                }
            } catch (e: Exception) {
                // Fallback to local rule engine seamlessly
            }
        }

        // Local intelligence fallback
        val defaultReply = when (localAction) {
            is AiAssistantAction.BookRideProposed ->
                "I've prepared a ride to ${localAction.destination} in ${localAction.category.title}. Please review the summary below and confirm to book."
            is AiAssistantAction.CancelRideProposed ->
                "Are you sure you want to cancel your ongoing ride? Please confirm below."
            is AiAssistantAction.ShowFareEstimate ->
                "Here are the estimated fares from your current pickup to ${localAction.destination}:\n• Bike: ₹65\n• Auto: ₹95\n• Mini: ₹145\n• Sedan: ₹190\n• SUV: ₹260"
            is AiAssistantAction.DriverStatusInfo ->
                "Your driver ${localAction.driverName} is currently ${localAction.status}. Estimated arrival: ~${localAction.etaMinutes} mins."
            is AiAssistantAction.GeneralResponse ->
                localAction.text
        }

        AiAssistantMessage(
            id = "ai_" + System.currentTimeMillis(),
            sender = "AI",
            message = defaultReply,
            proposedAction = localAction,
            isPendingConfirmation = localAction is AiAssistantAction.BookRideProposed || localAction is AiAssistantAction.CancelRideProposed
        )
    }

    private fun parseLocalIntent(
        prompt: String,
        currentLocationName: String,
        activeRideStatus: String?
    ): AiAssistantAction {
        val lower = prompt.lowercase()

        // Match destination from presets
        val matchedPlace = LocationEngine.PRESET_PLACES.firstOrNull { place ->
            lower.contains(place.name.lowercase().take(6)) ||
                    (place.name.contains("Airport", ignoreCase = true) && lower.contains("airport")) ||
                    (place.name.contains("Central", ignoreCase = true) && (lower.contains("central") || lower.contains("station"))) ||
                    (place.name.contains("Phoenix", ignoreCase = true) && lower.contains("phoenix")) ||
                    (place.name.contains("DLF", ignoreCase = true) && lower.contains("dlf")) ||
                    (place.name.contains("Marina", ignoreCase = true) && lower.contains("marina")) ||
                    (place.name.contains("T. Nagar", ignoreCase = true) && lower.contains("nagar"))
        } ?: LocationEngine.PRESET_PLACES[1] // Default Airport

        // Match category
        val category = when {
            lower.contains("bike") || lower.contains("moto") -> VehicleCategory.BIKE
            lower.contains("auto") -> VehicleCategory.AUTO
            lower.contains("mini") || lower.contains("hatchback") -> VehicleCategory.MINI
            lower.contains("suv") || lower.contains("innova") -> VehicleCategory.SUV
            lower.contains("lux") || lower.contains("premium") -> VehicleCategory.PREMIUM
            else -> VehicleCategory.SEDAN
        }

        return when {
            lower.contains("cancel") && (activeRideStatus != null && activeRideStatus != "None") -> {
                AiAssistantAction.CancelRideProposed(
                    rideId = "active_ride",
                    summary = "Cancel active ride request immediately."
                )
            }
            lower.contains("book") || lower.contains("take me to") || lower.contains("ride to") -> {
                AiAssistantAction.BookRideProposed(
                    destination = matchedPlace.name,
                    destinationLat = matchedPlace.latitude,
                    destinationLng = matchedPlace.longitude,
                    category = category,
                    estimatedFare = "₹185 - ₹240",
                    summary = "Trip to ${matchedPlace.name} with ${category.title}"
                )
            }
            lower.contains("cheapest") || lower.contains("lowest fare") -> {
                AiAssistantAction.GeneralResponse(
                    "The most affordable ride option right now is RideX Moto (Bike) starting at ₹25 base fare + ₹8/km, followed by RideX Auto at ₹35 base fare!"
                )
            }
            lower.contains("how far") || lower.contains("where is my driver") || lower.contains("driver status") -> {
                AiAssistantAction.DriverStatusInfo(
                    driverName = "Rajesh Kumar",
                    status = "on the way to your pickup location",
                    etaMinutes = 3,
                    summary = "Driver is 1.2 km away."
                )
            }
            lower.contains("estimate") || lower.contains("fare") || lower.contains("how much") -> {
                AiAssistantAction.ShowFareEstimate(
                    destination = matchedPlace.name,
                    categoryFares = listOf(
                        VehicleCategory.BIKE to 65.0,
                        VehicleCategory.AUTO to 95.0,
                        VehicleCategory.MINI to 140.0,
                        VehicleCategory.SEDAN to 185.0,
                        VehicleCategory.SUV to 260.0,
                        VehicleCategory.PREMIUM to 380.0
                    ),
                    summary = "Fare estimates to ${matchedPlace.name}"
                )
            }
            lower.contains("safety") || lower.contains("emergency") || lower.contains("sos") -> {
                AiAssistantAction.GeneralResponse(
                    "RideX Safety includes 4-digit Ride PIN verification, live GPS sharing with your emergency contacts, 24/7 dedicated helpline (+91 800-RIDE-SOS), and in-app 112 emergency trigger."
                )
            }
            else -> {
                AiAssistantAction.GeneralResponse(
                    "I can help you book a ride (e.g. 'Book a ride to Airport in Sedan'), check fare estimates, track your driver, or explain RideX safety features. What would you like to do?"
                )
            }
        }
    }
}
