package com.example.ai

import com.example.BuildConfig
import com.example.data.EventEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

data class ParsedNlEvent(
    val title: String = "New Event",
    val description: String = "",
    val date: String = "", // yyyy-MM-dd
    val startTime: String = "12:00", // HH:mm
    val endTime: String = "13:00", // HH:mm
    val durationMinutes: Int = 60,
    val priority: String = "MEDIUM",
    val category: String = "Personal",
    val colorHex: String = "#718096",
    val iconName: String = "📌"
)

object GeminiSchedulerService {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Parses natural language input like "Tomorrow 3 PM Gym for 1 hour" into structured event details.
     */
    suspend fun parseNaturalLanguageEvent(input: String): ParsedNlEvent = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext fallbackParse(input, todayStr)
        }

        val prompt = """
            You are FocusFlow's AI Scheduling Assistant. Today's date is $todayStr.
            Parse the following user text into an event schedule JSON object.
            Input: "$input"

            Respond ONLY with a valid raw JSON object matching this schema without markdown:
            {
              "title": "Short event title",
              "description": "Brief description",
              "date": "yyyy-MM-dd",
              "startTime": "HH:mm",
              "endTime": "HH:mm",
              "durationMinutes": 60,
              "priority": "HIGH or MEDIUM or LOW",
              "category": "Study or College or Work or Exercise or Meeting or Shopping or Health or Finance or Travel or Personal",
              "colorHex": "#3182CE or #38A169 or #805AD5 or #DD6B20 or #00B5D8 or #D69E2E or #E53E3E or #319795 or #D53F8C or #718096",
              "iconName": "📚 or 🏫 or 💻 or 🏋 or 📞 or 🛒 or ❤️ or 💰 or ✈ or 📝"
            }
        """.trimIndent()

        try {
            val partObj = JSONObject().put("text", prompt)
            val partsArr = JSONArray().put(partObj)
            val contentObj = JSONObject().put("parts", partsArr)
            val contentsArr = JSONArray().put(contentObj)
            val reqJson = JSONObject().put("contents", contentsArr)

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(reqJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val respBody = response.body?.string() ?: ""

            val jsonElement = JSONObject(respBody)
            val candidates = jsonElement.optJSONArray("candidates")
            val candidate0 = candidates?.optJSONObject(0)
            val content = candidate0?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text") ?: ""

            val cleanJson = text.replace("```json", "").replace("```", "").trim()
            if (cleanJson.startsWith("{")) {
                val parsedObj = JSONObject(cleanJson)
                return@withContext ParsedNlEvent(
                    title = parsedObj.optString("title", "New Event"),
                    description = parsedObj.optString("description", ""),
                    date = parsedObj.optString("date", todayStr).ifBlank { todayStr },
                    startTime = parsedObj.optString("startTime", "12:00"),
                    endTime = parsedObj.optString("endTime", "13:00"),
                    durationMinutes = parsedObj.optInt("durationMinutes", 60),
                    priority = parsedObj.optString("priority", "MEDIUM"),
                    category = parsedObj.optString("category", "Personal"),
                    colorHex = parsedObj.optString("colorHex", "#718096"),
                    iconName = parsedObj.optString("iconName", "📌")
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext fallbackParse(input, todayStr)
    }

    /**
     * Generates personalized smart AI advice and schedule suggestions.
     */
    suspend fun getSmartScheduleAdvice(events: List<EventEntity>): String = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "💡 **FocusFlow AI Insight:**\n\n• Peak Focus Window: 08:30 - 11:30 AM is ideal for high-priority Study & Work.\n• Break Recommendation: Take a 15-minute breather after your 90-minute Algorithm Lab.\n• Workload Balance: You have 3 high-priority tasks today. Stay hydrated!"
        }

        val eventListSummary = events.joinToString("; ") { "${it.title} (${it.startTime}-${it.endTime}, ${it.priority} Priority, ${it.category})" }
        val prompt = """
            You are FocusFlow's AI Productivity Coach.
            Analyze this user's daily schedule: [$eventListSummary].
            Provide a concise, encouraging 3-point advice breakdown covering:
            1. Optimal Focus Window
            2. Recommended Break/Rest interval
            3. Workload Balance & Procrastination Prevention tip.
            Use markdown formatting with bullet points.
        """.trimIndent()

        try {
            val partObj = JSONObject().put("text", prompt)
            val partsArr = JSONArray().put(partObj)
            val contentObj = JSONObject().put("parts", partsArr)
            val contentsArr = JSONArray().put(contentObj)
            val reqJson = JSONObject().put("contents", contentsArr)

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(reqJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val respBody = response.body?.string() ?: ""

            val jsonElement = JSONObject(respBody)
            val candidates = jsonElement.optJSONArray("candidates")
            val candidate0 = candidates?.optJSONObject(0)
            val content = candidate0?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")

            if (!text.isNullOrBlank()) {
                return@withContext text.trim()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext "💡 **FocusFlow AI Insight:**\n\n• Peak Focus Window: 08:30 - 11:30 AM is ideal for high-priority Study & Work.\n• Break Recommendation: Take a 15-minute breather after your 90-minute Algorithm Lab.\n• Workload Balance: You have 3 high-priority tasks today. Stay hydrated!"
    }

    private fun fallbackParse(input: String, todayStr: String): ParsedNlEvent {
        val lower = input.lowercase()
        val title = input.replace(Regex("(?i)(tomorrow|today|at|pm|am|for|hours?|mins?)"), "").trim().ifBlank { input }

        val category = when {
            lower.contains("study") || lower.contains("read") || lower.contains("exam") -> "Study"
            lower.contains("college") || lower.contains("lab") || lower.contains("lecture") -> "College"
            lower.contains("work") || lower.contains("code") || lower.contains("sprint") -> "Work"
            lower.contains("gym") || lower.contains("run") || lower.contains("workout") -> "Exercise"
            lower.contains("meet") || lower.contains("call") || lower.contains("sync") -> "Meeting"
            lower.contains("buy") || lower.contains("shop") || lower.contains("store") -> "Shopping"
            else -> "Personal"
        }

        val color = when (category) {
            "Study" -> "#3182CE"
            "College" -> "#38A169"
            "Work" -> "#805AD5"
            "Exercise" -> "#DD6B20"
            "Meeting" -> "#00B5D8"
            "Shopping" -> "#D69E2E"
            else -> "#718096"
        }

        val icon = when (category) {
            "Study" -> "📚"
            "College" -> "🏫"
            "Work" -> "💻"
            "Exercise" -> "🏋"
            "Meeting" -> "📞"
            "Shopping" -> "🛒"
            else -> "📌"
        }

        return ParsedNlEvent(
            title = title,
            description = "Quick event created via Natural Language",
            date = todayStr,
            startTime = "15:00",
            endTime = "16:00",
            durationMinutes = 60,
            priority = "MEDIUM",
            category = category,
            colorHex = color,
            iconName = icon
        )
    }
}
