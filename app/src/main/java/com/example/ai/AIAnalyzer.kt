package com.example.ai

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.data.TutorialProvider
import com.example.model.DetailLevel
import com.example.model.DrawingStep
import com.example.model.NormalizedPoint
import com.example.util.ImageLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

object AIAnalyzer {

    private const val TAG = "LineSketchAI"

    // OkHttpClient with 30s connect and 75s read/write timeouts
    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(75, TimeUnit.SECONDS)
            .writeTimeout(75, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    data class AnalysisResult(
        val title: String,
        val detectedSubject: String,
        val steps: List<DrawingStep>
    )

    /**
     * Executes safe, memory-conscious progressive sketch analysis with multi-stage progress reporting.
     */
    suspend fun analyzeImage(
        bitmap: Bitmap,
        detailLevel: DetailLevel,
        apiKey: String,
        onProgress: (String) -> Unit = {}
    ): Result<AnalysisResult> = withContext(Dispatchers.IO) {
        Log.d(TAG, "ANALYSIS_START")

        if (apiKey.isBlank()) {
            val err = "Please add your AI API key in Settings first."
            Log.e(TAG, "ANALYSIS_ERROR: $err")
            return@withContext Result.failure(IllegalArgumentException(err))
        }

        try {
            // Stage 1: Preparing image
            onProgress("Preparing image...")
            Log.d(TAG, "BITMAP_DECODE_START: Scaling bitmap to safe dimension")
            val safeBitmap = ImageLoader.scaleBitmapWithinBounds(bitmap, 1024)
            Log.d(TAG, "BITMAP_DECODE_SUCCESS: Dimensions = ${safeBitmap.width}x${safeBitmap.height}")

            val jpegBytes = ImageLoader.compressToJpeg(safeBitmap, 85)
            val base64Data = Base64.encodeToString(jpegBytes, Base64.NO_WRAP)
            Log.d(TAG, "IMAGE_COMPRESSED: Base64 string length = ${base64Data.length}")

            // Stage 2: Sending image to AI
            onProgress("Sending image to AI...")
            val promptText = buildInstructionPrompt(detailLevel)

            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray()
                val contentObj = JSONObject().apply {
                    val partsArray = JSONArray()

                    partsArray.put(JSONObject().apply {
                        put("text", promptText)
                    })

                    partsArray.put(JSONObject().apply {
                        put("inlineData", JSONObject().apply {
                            put("mimeType", "image/jpeg")
                            put("data", base64Data)
                        })
                    })

                    put("parts", partsArray)
                }
                contentsArray.put(contentObj)
                put("contents", contentsArray)

                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.2)
                })
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            // Stage 3: AI analyzing drawing
            onProgress("AI is analyzing the drawing...")
            Log.d(TAG, "API_REQUEST_START: Calling Gemini Vision endpoint (gemini-3.5-flash)")

            val response = try {
                httpClient.newCall(request).execute()
            } catch (e: SocketTimeoutException) {
                Log.e(TAG, "ANALYSIS_ERROR: SocketTimeoutException", e)
                return@withContext Result.failure(Exception("Analysis timed out. Please try again."))
            } catch (e: UnknownHostException) {
                Log.e(TAG, "ANALYSIS_ERROR: UnknownHostException", e)
                return@withContext Result.failure(Exception("Network connection failed. Check your internet connection."))
            } catch (e: IOException) {
                Log.e(TAG, "ANALYSIS_ERROR: IOException calling API", e)
                return@withContext Result.failure(Exception("Network error: ${e.localizedMessage ?: "Connection failure"}"))
            }

            val responseCode = response.code
            val responseString = response.body?.string() ?: ""
            Log.d(TAG, "API_RESPONSE_RECEIVED: HTTP $responseCode, length = ${responseString.length}")

            if (!response.isSuccessful) {
                val errorMsg = extractErrorMessage(responseCode, responseString)
                Log.e(TAG, "ANALYSIS_ERROR: HTTP $responseCode - $errorMsg")
                return@withContext Result.failure(Exception(errorMsg))
            }

            if (responseString.isBlank()) {
                Log.e(TAG, "ANALYSIS_ERROR: Empty response body")
                return@withContext Result.failure(Exception("AI service returned an empty response. Please try again."))
            }

            // Stage 4: Creating drawing steps
            onProgress("Creating drawing steps...")
            Log.d(TAG, "JSON_PARSE_START: Extracting candidates and parsing payload")

            val rootJson = try {
                JSONObject(responseString)
            } catch (e: JSONException) {
                Log.e(TAG, "ANALYSIS_ERROR: Malformed root JSON", e)
                return@withContext Result.failure(Exception("AI service returned an invalid response structure."))
            }

            val candidates = rootJson.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                val blockReason = rootJson.optJSONObject("promptFeedback")?.optString("blockReason")
                val msg = if (!blockReason.isNullOrBlank()) "Content blocked: $blockReason" else "No tutorial generated by AI model."
                Log.e(TAG, "ANALYSIS_ERROR: $msg")
                return@withContext Result.failure(Exception(msg))
            }

            val candidateObj = candidates.getJSONObject(0)
            val content = candidateObj.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val rawOutput = parts?.optJSONObject(0)?.optString("text") ?: ""

            if (rawOutput.isBlank()) {
                Log.e(TAG, "ANALYSIS_ERROR: Empty content text in candidate parts")
                return@withContext Result.failure(Exception("The AI returned an empty drawing plan. Please try again."))
            }

            // Stage 5: Almost done
            onProgress("Almost done...")
            val cleanedJson = extractJsonSubstring(rawOutput)
            val parsedResult = parseAndValidateTutorialJson(cleanedJson)

            if (parsedResult.steps.isEmpty()) {
                Log.e(TAG, "ANALYSIS_ERROR: Zero valid drawing steps parsed")
                return@withContext Result.failure(Exception("The AI returned an invalid drawing plan. Please try again."))
            }

            Log.d(TAG, "JSON_PARSE_SUCCESS: Successfully parsed ${parsedResult.steps.size} steps")
            Log.d(TAG, "DRAWING_STEPS_CREATED: Title = \"${parsedResult.title}\"")
            Log.d(TAG, "ANALYSIS_COMPLETE")

            Result.success(parsedResult)
        } catch (oom: OutOfMemoryError) {
            Log.e(TAG, "ANALYSIS_ERROR: OutOfMemoryError during image analysis", oom)
            System.gc()
            Result.failure(Exception("Image is too large. Please choose another image."))
        } catch (e: Exception) {
            Log.e(TAG, "ANALYSIS_ERROR: Unexpected error ${e.javaClass.simpleName}: ${e.message}", e)
            Result.failure(Exception("Analysis failed: ${e.message ?: "Unknown error"}. Please try again."))
        }
    }

    private fun extractErrorMessage(code: Int, responseString: String): String {
        return try {
            val errJson = JSONObject(responseString)
            val errorObj = errJson.optJSONObject("error")
            val message = errorObj?.optString("message") ?: ""
            val status = errorObj?.optString("status") ?: ""

            when {
                code == 400 && (message.contains("API key", ignoreCase = true) || status == "INVALID_ARGUMENT") ->
                    "API key is invalid. Please check your API key in Settings."
                code == 403 || message.contains("API_KEY_INVALID", ignoreCase = true) ->
                    "Invalid API key. Please check your API key in Settings."
                code == 429 || status == "RESOURCE_EXHAUSTED" ->
                    "AI quota limit reached. Please wait a moment and try again."
                message.isNotBlank() ->
                    "AI Error: $message"
                else ->
                    "HTTP Error $code: Please check connection and try again."
            }
        } catch (e: Exception) {
            if (code == 403) "Invalid API key. Please check your API key in Settings."
            else "Server communication failed (HTTP $code)."
        }
    }

    private fun extractJsonSubstring(raw: String): String {
        val trimmed = raw.trim()
        val firstBrace = trimmed.indexOf('{')
        val lastBrace = trimmed.lastIndexOf('}')
        return if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            trimmed.substring(firstBrace, lastBrace + 1)
        } else {
            trimmed
        }
    }

    private fun parseAndValidateTutorialJson(jsonString: String): AnalysisResult {
        val root = JSONObject(jsonString)
        val title = root.optString("title", "Portrait Sketch").ifBlank { "LineSketch Tutorial" }
        val detectedSubject = root.optString("detectedSubject", "Subject")
        val stepsJson = root.optJSONArray("steps") ?: JSONArray()

        val steps = mutableListOf<DrawingStep>()
        for (i in 0 until stepsJson.length()) {
            val sObj = stepsJson.optJSONObject(i) ?: continue
            val stepNumber = sObj.optInt("step", i + 1)
            val stepTitle = sObj.optString("title", "Step $stepNumber").ifBlank { "Step $stepNumber" }
            val instruction = sObj.optString("instruction", "Follow the construction line carefully.")
            val type = sObj.optString("type", "curve")
            val difficulty = sObj.optString("difficulty", "easy")
            val estimatedTime = sObj.optString("estimatedTime", "30s")
            val hint = sObj.optString("hint", "Follow the glowing line closely")

            val pointsList = mutableListOf<NormalizedPoint>()
            val pointsJson = sObj.optJSONArray("points")
            if (pointsJson != null) {
                for (j in 0 until pointsJson.length()) {
                    val pObj = pointsJson.optJSONObject(j) ?: continue
                    val rawX = pObj.optDouble("x", Double.NaN)
                    val rawY = pObj.optDouble("y", Double.NaN)

                    // Safely validate and clamp coordinates
                    if (!rawX.isNaN() && !rawX.isInfinite() && !rawY.isNaN() && !rawY.isInfinite()) {
                        val clampedX = rawX.toFloat().coerceIn(0.0f, 1.0f)
                        val clampedY = rawY.toFloat().coerceIn(0.0f, 1.0f)
                        pointsList.add(NormalizedPoint(clampedX, clampedY))
                    }
                }
            }

            // Ensure step has at least 2 points to render a meaningful line
            if (pointsList.isEmpty()) {
                pointsList.add(NormalizedPoint(0.40f, 0.40f))
                pointsList.add(NormalizedPoint(0.60f, 0.60f))
            } else if (pointsList.size == 1) {
                val p = pointsList.first()
                pointsList.add(NormalizedPoint((p.x + 0.05f).coerceIn(0.0f, 1.0f), (p.y + 0.05f).coerceIn(0.0f, 1.0f)))
            }

            steps.add(
                DrawingStep(
                    step = stepNumber,
                    title = stepTitle,
                    instruction = instruction,
                    type = type,
                    difficulty = difficulty,
                    estimatedTime = estimatedTime,
                    points = pointsList,
                    hint = hint
                )
            )
        }

        return AnalysisResult(title = title, detectedSubject = detectedSubject, steps = steps)
    }

    private fun buildInstructionPrompt(detailLevel: DetailLevel): String {
        val stepGuidance = when (detailLevel) {
            DetailLevel.BEGINNER -> "Divide into approximately 12 to 18 clear construction steps focusing on large basic shapes."
            DetailLevel.NORMAL -> "Divide into approximately 20 to 30 steps with balanced anatomy, guidelines, and main features."
            DetailLevel.DETAILED -> "Divide into approximately 35 to 50 progressive steps including facial expressions, hair locks, and folds."
            DetailLevel.EXTREMELY_DETAILED -> "Divide into 50+ very detailed construction lines and hatching steps."
        }

        return """
You are an expert drawing instructor and vector sketch analyzer.
Analyze the uploaded reference image and convert it into a progressive drawing tutorial.
Break the drawing into the smallest meaningful construction steps.
Start from large simple construction shapes and gradually move toward details.
Never reveal future drawing details before their step.

Special Portrait Guidance:
If the image contains a human face or portrait, strictly follow classical drawing pedagogy (e.g., Loomis Method):
1. Head shape (sphere/oval)
2. Face center line
3. Eye line
4. Jaw contour
5. Ear placement
6. Left eye construction
7. Right eye construction
8. Eyebrows
9. Nose bridge
10. Nose tip and wings
11. Nose shadow
12. Upper lip
13. Lower lip
14. Chin definition
15. Hair outline volume
16. Hair locks & strands
17. Neck columns
18. Clothing & collar
19. Major drop shadows
20. Fine details and highlights.

$stepGuidance

The coordinate system MUST be normalized from 0.0 to 1.0 (where x=0.0 is left, x=1.0 is right, y=0.0 is top, y=1.0 is bottom) so the drawing scales to any screen size.
For every step, provide 6 to 25 continuous points along the line or curve so it can be rendered smoothly.

Return strictly valid JSON in this exact structure:
{
  "title": "Title of Sketch",
  "detectedSubject": "Portrait / Character / Animal / Object",
  "steps": [
    {
      "step": 1,
      "title": "Basic Head Circle",
      "instruction": "Draw a light circular guideline for the cranial mass.",
      "type": "circle",
      "difficulty": "easy",
      "estimatedTime": "30s",
      "hint": "Start from top center and curve around",
      "points": [
        {"x": 0.50, "y": 0.20},
        {"x": 0.65, "y": 0.35},
        {"x": 0.50, "y": 0.50},
        {"x": 0.35, "y": 0.35},
        {"x": 0.50, "y": 0.20}
      ]
    }
  ]
}
""".trimIndent()
    }

    /**
     * Generates an instant offline adaptive sketch plan for any image if the user does not have an API key or is offline.
     */
    fun generateOfflineAdaptivePlan(title: String, detailLevel: DetailLevel): AnalysisResult {
        val baseSteps = TutorialProvider.getBuiltinTutorials().first().steps
        val stepLimit = when (detailLevel) {
            DetailLevel.BEGINNER -> 12
            DetailLevel.NORMAL -> 20
            DetailLevel.DETAILED -> 20
            DetailLevel.EXTREMELY_DETAILED -> 20
        }
        val trimmed = baseSteps.take(stepLimit)
        return AnalysisResult(
            title = title,
            detectedSubject = "Portrait Construction (Adaptive)",
            steps = trimmed
        )
    }
}
