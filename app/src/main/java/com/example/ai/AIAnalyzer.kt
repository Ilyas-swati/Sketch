package com.example.ai

import android.graphics.Bitmap
import android.util.Base64
import com.example.data.TutorialProvider
import com.example.model.DetailLevel
import com.example.model.DrawingStep
import com.example.model.NormalizedPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit
import kotlin.math.cos
import kotlin.math.sin

object AIAnalyzer {

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    private fun Bitmap.toBase64Jpg(maxDimension: Int = 1024): String {
        val scale = if (width > maxDimension || height > maxDimension) {
            maxDimension.toFloat() / kotlin.math.max(width, height)
        } else {
            1.0f
        }
        val targetWidth = (width * scale).toInt().coerceAtLeast(1)
        val targetHeight = (height * scale).toInt().coerceAtLeast(1)
        val scaled = Bitmap.createScaledBitmap(this, targetWidth, targetHeight, true)

        val outputStream = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    /**
     * Sends the image to Gemini vision model to generate a progressive vector sketch tutorial.
     */
    suspend fun analyzeImage(
        bitmap: Bitmap,
        detailLevel: DetailLevel,
        apiKey: String
    ): Result<AnalysisResult> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                IllegalArgumentException("No Gemini API key configured. Please enter your API key in Settings or try our built-in offline tutorials!")
            )
        }

        try {
            val base64Data = bitmap.toBase64Jpg()
            val promptText = buildInstructionPrompt(detailLevel)

            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray()
                val contentObj = JSONObject().apply {
                    val partsArray = JSONArray()

                    // Text prompt part
                    partsArray.put(JSONObject().apply {
                        put("text", promptText)
                    })

                    // Image inline data part
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

            val response = httpClient.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorMsg = try {
                    val errJson = JSONObject(responseString)
                    errJson.optJSONObject("error")?.optString("message") ?: "HTTP error: ${response.code}"
                } catch (e: Exception) {
                    "HTTP ${response.code}: $responseString"
                }
                return@withContext Result.failure(Exception("Gemini API Error: $errorMsg"))
            }

            val rootJson = JSONObject(responseString)
            val candidates = rootJson.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext Result.failure(Exception("No tutorial generated from model."))
            }

            val content = candidates.getJSONObject(0).optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val rawOutput = parts?.getJSONObject(0)?.optString("text") ?: ""

            // Strip code fences if present
            val cleaned = cleanJsonString(rawOutput)
            val parsedResult = parseTutorialJson(cleaned)

            if (parsedResult.steps.isEmpty()) {
                return@withContext Result.failure(Exception("Could not extract drawing steps from AI response."))
            }

            Result.success(parsedResult)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    data class AnalysisResult(
        val title: String,
        val detectedSubject: String,
        val steps: List<DrawingStep>
    )

    private fun cleanJsonString(raw: String): String {
        var str = raw.trim()
        if (str.startsWith("```json")) {
            str = str.removePrefix("```json")
        } else if (str.startsWith("```")) {
            str = str.removePrefix("```")
        }
        if (str.endsWith("```")) {
            str = str.removeSuffix("```")
        }
        return str.trim()
    }

    private fun parseTutorialJson(jsonString: String): AnalysisResult {
        val root = JSONObject(jsonString)
        val title = root.optString("title", "AI Progressive Sketch")
        val detectedSubject = root.optString("detectedSubject", "Subject")
        val stepsJson = root.optJSONArray("steps") ?: JSONArray()

        val steps = mutableListOf<DrawingStep>()
        for (i in 0 until stepsJson.length()) {
            val sObj = stepsJson.getJSONObject(i)
            val stepNumber = sObj.optInt("step", i + 1)
            val stepTitle = sObj.optString("title", "Step $stepNumber")
            val instruction = sObj.optString("instruction", "Draw the indicated line.")
            val type = sObj.optString("type", "curve")
            val difficulty = sObj.optString("difficulty", "easy")
            val estimatedTime = sObj.optString("estimatedTime", "30s")
            val hint = sObj.optString("hint", "Follow the glowing line closely")

            val pointsList = mutableListOf<NormalizedPoint>()
            val pointsJson = sObj.optJSONArray("points")
            if (pointsJson != null) {
                for (j in 0 until pointsJson.length()) {
                    val pObj = pointsJson.getJSONObject(j)
                    val x = pObj.optDouble("x", 0.5).toFloat().coerceIn(0.0f, 1.0f)
                    val y = pObj.optDouble("y", 0.5).toFloat().coerceIn(0.0f, 1.0f)
                    pointsList.add(NormalizedPoint(x, y))
                }
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
