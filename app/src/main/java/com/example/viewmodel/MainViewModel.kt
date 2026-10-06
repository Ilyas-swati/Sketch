package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.ai.AIAnalyzer
import com.example.ai.DrawingEvaluator
import com.example.data.PreferencesManager
import com.example.data.SketchDatabase
import com.example.data.SketchProjectEntity
import com.example.data.TutorialProvider
import com.example.model.DetailLevel
import com.example.model.DrawingMode
import com.example.model.DrawingStep
import com.example.model.EvaluationResult
import com.example.model.NormalizedPoint
import com.example.model.UserStroke
import com.example.ui.canvas.CanvasState
import com.example.ui.theme.GuideOrange
import com.example.ui.theme.PencilGraphite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.util.UUID

enum class AppScreen {
    HOME,
    CANVAS,
    COMPARE,
    COMPLETION
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SketchDatabase.getInstance(application)
    private val dao = db.sketchDao()
    private val prefs = PreferencesManager(application)

    // Navigation & UI Screen
    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Recent Projects from Room
    private val _recentProjects = MutableStateFlow<List<SketchProjectEntity>>(emptyList())
    val recentProjects: StateFlow<List<SketchProjectEntity>> = _recentProjects.asStateFlow()

    // Current Project & Canvas Session
    private val _currentProjectId = MutableStateFlow(UUID.randomUUID().toString())
    val currentProjectId: StateFlow<String> = _currentProjectId.asStateFlow()

    private val _projectTitle = MutableStateFlow("Untitled Sketch")
    val projectTitle: StateFlow<String> = _projectTitle.asStateFlow()

    private val _referenceBitmap = MutableStateFlow<Bitmap?>(null)
    val referenceBitmap: StateFlow<Bitmap?> = _referenceBitmap.asStateFlow()

    private val _steps = MutableStateFlow<List<DrawingStep>>(emptyList())
    val steps: StateFlow<List<DrawingStep>> = _steps.asStateFlow()

    private val _currentStepIndex = MutableStateFlow(0)
    val currentStepIndex: StateFlow<Int> = _currentStepIndex.asStateFlow()

    private val _canvasState = MutableStateFlow(
        CanvasState(
            guideColor = androidx.compose.ui.graphics.Color(prefs.guideColorArgb.toULong()),
            guideOpacity = prefs.guideOpacity,
            referenceOpacity = prefs.referenceOpacity,
            brushSize = prefs.brushSize
        )
    )
    val canvasState: StateFlow<CanvasState> = _canvasState.asStateFlow()

    private val _currentMode = MutableStateFlow(DrawingMode.LEARN)
    val currentMode: StateFlow<DrawingMode> = _currentMode.asStateFlow()

    // AI Evaluation
    private val _evaluationResult = MutableStateFlow<EvaluationResult?>(null)
    val evaluationResult: StateFlow<EvaluationResult?> = _evaluationResult.asStateFlow()

    // AI Analysis State
    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _analysisMessage = MutableStateFlow("")
    val analysisMessage: StateFlow<String> = _analysisMessage.asStateFlow()

    // Dialog Toggles
    private val _showSettingsDialog = MutableStateFlow(false)
    val showSettingsDialog: StateFlow<Boolean> = _showSettingsDialog.asStateFlow()

    private val _showLayersDialog = MutableStateFlow(false)
    val showLayersDialog: StateFlow<Boolean> = _showLayersDialog.asStateFlow()

    private val _showApiKeyPrompt = MutableStateFlow(false)
    val showApiKeyPrompt: StateFlow<Boolean> = _showApiKeyPrompt.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Settings
    val apiKey: String
        get() {
            val userKey = prefs.apiKey
            if (userKey.isNotBlank()) return userKey
            val buildKey = BuildConfig.GEMINI_API_KEY
            if (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY") return buildKey
            return ""
        }

    val detailLevel: DetailLevel
        get() = prefs.detailLevel

    init {
        // Observe Room projects
        viewModelScope.launch {
            dao.getAllProjects().collectLatest { list ->
                _recentProjects.value = list
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun setMode(mode: DrawingMode) {
        _currentMode.value = mode
        when (mode) {
            DrawingMode.CHALLENGE -> {
                // In challenge mode, guide is hidden until user asks or finishes
                _canvasState.value = _canvasState.value.copy(showGuide = false, showReference = false)
            }
            DrawingMode.TRACE -> {
                _canvasState.value = _canvasState.value.copy(showGuide = true, showReference = true)
            }
            DrawingMode.COMPARE -> {
                _currentScreen.value = AppScreen.COMPARE
            }
            else -> {
                _canvasState.value = _canvasState.value.copy(showGuide = true)
            }
        }
    }

    // --- Media Input & AI Analysis ---

    fun onImageSelectedFromGallery(uri: Uri) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (bitmap != null) {
                    processUploadedImage(bitmap, "Sketch from Gallery", uri.toString())
                } else {
                    _errorMessage.value = "Unable to load the selected image."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error reading image: ${e.message}"
            }
        }
    }

    fun onImageCapturedFromCamera(bitmap: Bitmap) {
        processUploadedImage(bitmap, "Camera Capture", null)
    }

    private fun processUploadedImage(bitmap: Bitmap, defaultTitle: String, imageUri: String?) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            _analysisMessage.value = "AI is inspecting subject, anatomy, guidelines & shapes..."

            val activeApiKey = apiKey
            val currentLevel = prefs.detailLevel

            if (activeApiKey.isBlank()) {
                // Prompt user for key or use intelligent offline plan
                _analysisMessage.value = "No API key found. Generating progressive offline construction plan..."
                val offlineResult = AIAnalyzer.generateOfflineAdaptivePlan(defaultTitle, currentLevel)
                setupNewProject(
                    title = offlineResult.title,
                    steps = offlineResult.steps,
                    bitmap = bitmap,
                    imageUri = imageUri
                )
                _isAnalyzing.value = false
                _showApiKeyPrompt.value = true
                return@launch
            }

            val analysisResult = AIAnalyzer.analyzeImage(bitmap, currentLevel, activeApiKey)
            analysisResult.onSuccess { result ->
                setupNewProject(
                    title = result.title,
                    steps = result.steps,
                    bitmap = bitmap,
                    imageUri = imageUri
                )
            }.onFailure { err ->
                // Fallback gracefully so user can always draw!
                _errorMessage.value = "AI analysis issue: ${err.message}. Loaded offline tutorial structure."
                val fallback = AIAnalyzer.generateOfflineAdaptivePlan(defaultTitle, currentLevel)
                setupNewProject(
                    title = fallback.title,
                    steps = fallback.steps,
                    bitmap = bitmap,
                    imageUri = imageUri
                )
            }

            _isAnalyzing.value = false
        }
    }

    fun loadBuiltinTutorial(tutorial: TutorialProvider.TutorialTemplate) {
        setupNewProject(
            title = tutorial.title,
            steps = tutorial.steps,
            bitmap = null,
            imageUri = null
        )
    }

    private fun setupNewProject(
        title: String,
        steps: List<DrawingStep>,
        bitmap: Bitmap?,
        imageUri: String?
    ) {
        val newId = UUID.randomUUID().toString()
        _currentProjectId.value = newId
        _projectTitle.value = title
        _steps.value = steps
        _currentStepIndex.value = 0
        _referenceBitmap.value = bitmap
        _evaluationResult.value = null

        _canvasState.value = CanvasState(
            strokes = emptyList(),
            guideColor = androidx.compose.ui.graphics.Color(prefs.guideColorArgb.toULong()),
            guideOpacity = prefs.guideOpacity,
            referenceOpacity = prefs.referenceOpacity,
            brushSize = prefs.brushSize,
            showReference = (bitmap != null)
        )

        _currentScreen.value = AppScreen.CANVAS
        autoSaveProject()
    }

    // --- Canvas Drawing Actions ---

    fun addStroke(stroke: UserStroke) {
        val currentStrokes = _canvasState.value.strokes
        val newUndo = _canvasState.value.undoStack + listOf(currentStrokes)
        val newStrokes = currentStrokes + stroke
        _canvasState.value = _canvasState.value.copy(
            strokes = newStrokes,
            undoStack = newUndo,
            redoStack = emptyList()
        )
    }

    fun undo() {
        val undoStack = _canvasState.value.undoStack
        if (undoStack.isNotEmpty()) {
            val prevStrokes = undoStack.last()
            val newUndo = undoStack.dropLast(1)
            val newRedo = _canvasState.value.redoStack + listOf(_canvasState.value.strokes)
            _canvasState.value = _canvasState.value.copy(
                strokes = prevStrokes,
                undoStack = newUndo,
                redoStack = newRedo
            )
        }
    }

    fun redo() {
        val redoStack = _canvasState.value.redoStack
        if (redoStack.isNotEmpty()) {
            val nextStrokes = redoStack.last()
            val newRedo = redoStack.dropLast(1)
            val newUndo = _canvasState.value.undoStack + listOf(_canvasState.value.strokes)
            _canvasState.value = _canvasState.value.copy(
                strokes = nextStrokes,
                undoStack = newUndo,
                redoStack = newRedo
            )
        }
    }

    fun clearCanvas() {
        val currentStrokes = _canvasState.value.strokes
        if (currentStrokes.isNotEmpty()) {
            val newUndo = _canvasState.value.undoStack + listOf(currentStrokes)
            _canvasState.value = _canvasState.value.copy(
                strokes = emptyList(),
                undoStack = newUndo,
                redoStack = emptyList()
            )
        }
    }

    fun updateTransform(zoom: Float, pan: androidx.compose.ui.geometry.Offset) {
        _canvasState.value = _canvasState.value.copy(zoom = zoom, panOffset = pan)
    }

    fun zoomIn() {
        val newZoom = (_canvasState.value.zoom * 1.25f).coerceAtMost(5.0f)
        _canvasState.value = _canvasState.value.copy(zoom = newZoom)
    }

    fun zoomOut() {
        val newZoom = (_canvasState.value.zoom / 1.25f).coerceAtLeast(0.5f)
        _canvasState.value = _canvasState.value.copy(zoom = newZoom)
    }

    fun resetZoom() {
        _canvasState.value = _canvasState.value.copy(
            zoom = 1.0f,
            panOffset = androidx.compose.ui.geometry.Offset.Zero
        )
    }

    fun setPanMode(enabled: Boolean) {
        _canvasState.value = _canvasState.value.copy(isPanMode = enabled)
    }

    fun setEraserMode(isEraser: Boolean) {
        _canvasState.value = _canvasState.value.copy(isEraser = isEraser)
    }

    fun setBrushSize(size: Float) {
        prefs.brushSize = size
        _canvasState.value = _canvasState.value.copy(brushSize = size)
    }

    fun setBrushColor(color: androidx.compose.ui.graphics.Color) {
        _canvasState.value = _canvasState.value.copy(brushColor = color, isEraser = false)
    }

    fun setGuideOpacity(opacity: Float) {
        prefs.guideOpacity = opacity
        _canvasState.value = _canvasState.value.copy(guideOpacity = opacity)
    }

    fun toggleGuide(enabled: Boolean) {
        _canvasState.value = _canvasState.value.copy(showGuide = enabled)
    }

    fun setGuideColor(color: androidx.compose.ui.graphics.Color) {
        prefs.guideColorArgb = color.value.toLong()
        _canvasState.value = _canvasState.value.copy(guideColor = color)
    }

    fun setReferenceOpacity(opacity: Float) {
        prefs.referenceOpacity = opacity
        _canvasState.value = _canvasState.value.copy(referenceOpacity = opacity)
    }

    fun toggleReference(enabled: Boolean) {
        _canvasState.value = _canvasState.value.copy(showReference = enabled)
    }

    // --- Step Progression & AI Evaluation ---

    fun checkDrawing() {
        val allSteps = _steps.value
        val currentIndex = _currentStepIndex.value
        if (currentIndex !in allSteps.indices) return

        val step = allSteps[currentIndex]
        val strokesForCurrentStep = _canvasState.value.strokes.filter { it.stepIndex == currentIndex }

        val eval = DrawingEvaluator.evaluateStep(step, strokesForCurrentStep)
        _evaluationResult.value = eval
    }

    fun dismissEvaluation() {
        _evaluationResult.value = null
    }

    fun tryAgainCurrentStep() {
        // Erase strokes drawn during the current step so user can re-trace
        val currentIndex = _currentStepIndex.value
        val filtered = _canvasState.value.strokes.filter { it.stepIndex != currentIndex }
        _canvasState.value = _canvasState.value.copy(strokes = filtered)
        _evaluationResult.value = null
    }

    fun nextStep() {
        _evaluationResult.value = null
        val total = _steps.value.size
        val nextIdx = _currentStepIndex.value + 1
        if (nextIdx < total) {
            _currentStepIndex.value = nextIdx
            autoSaveProject()
        } else {
            // Completed all steps!
            autoSaveProject()
            _currentScreen.value = AppScreen.COMPLETION
        }
    }

    fun previousStep() {
        _evaluationResult.value = null
        if (_currentStepIndex.value > 0) {
            _currentStepIndex.value = _currentStepIndex.value - 1
            autoSaveProject()
        }
    }

    // --- Room Persistence ---

    fun autoSaveProject() {
        viewModelScope.launch(Dispatchers.IO) {
            val total = _steps.value.size
            val currentIdx = _currentStepIndex.value
            val percent = if (total > 0) ((currentIdx.toFloat() / total) * 100).toInt() else 0

            val stepsJson = serializeSteps(_steps.value)
            val strokesJson = serializeStrokes(_canvasState.value.strokes)

            val entity = SketchProjectEntity(
                id = _currentProjectId.value,
                title = _projectTitle.value,
                imageUri = null,
                stepsJson = stepsJson,
                strokesJson = strokesJson,
                currentStepIndex = currentIdx,
                totalSteps = total,
                completedPercent = percent,
                detailLevel = prefs.detailLevel.name,
                updatedAt = System.currentTimeMillis()
            )
            dao.insertOrUpdate(entity)
        }
    }

    fun resumeProject(project: SketchProjectEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val parsedSteps = deserializeSteps(project.stepsJson)
            val parsedStrokes = deserializeStrokes(project.strokesJson)

            withContext(Dispatchers.Main) {
                _currentProjectId.value = project.id
                _projectTitle.value = project.title
                _steps.value = parsedSteps
                _currentStepIndex.value = project.currentStepIndex.coerceIn(0, (parsedSteps.size - 1).coerceAtLeast(0))
                _referenceBitmap.value = null
                _evaluationResult.value = null

                _canvasState.value = CanvasState(
                    strokes = parsedStrokes,
                    guideColor = androidx.compose.ui.graphics.Color(prefs.guideColorArgb.toULong()),
                    guideOpacity = prefs.guideOpacity,
                    referenceOpacity = prefs.referenceOpacity,
                    brushSize = prefs.brushSize,
                    showReference = false
                )

                _currentScreen.value = AppScreen.CANVAS
            }
        }
    }

    fun deleteProject(project: SketchProjectEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteProject(project)
        }
    }

    // --- Settings & Dialogs ---

    fun openSettings() {
        _showSettingsDialog.value = true
    }

    fun closeSettings() {
        _showSettingsDialog.value = false
    }

    fun openLayers() {
        _showLayersDialog.value = true
    }

    fun closeLayers() {
        _showLayersDialog.value = false
    }

    fun dismissApiKeyPrompt() {
        _showApiKeyPrompt.value = false
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun saveApiKey(newKey: String) {
        prefs.apiKey = newKey
    }

    fun saveDetailLevel(level: DetailLevel) {
        prefs.detailLevel = level
    }

    fun saveGuideColor(color: androidx.compose.ui.graphics.Color) {
        prefs.guideColorArgb = color.value.toLong()
        _canvasState.value = _canvasState.value.copy(guideColor = color)
    }

    // --- JSON Serialization Helpers ---

    private fun serializeSteps(steps: List<DrawingStep>): String {
        val array = JSONArray()
        for (s in steps) {
            val obj = JSONObject().apply {
                put("step", s.step)
                put("title", s.title)
                put("instruction", s.instruction)
                put("type", s.type)
                put("difficulty", s.difficulty)
                put("estimatedTime", s.estimatedTime)
                put("hint", s.hint)
                val pts = JSONArray()
                for (p in s.points) {
                    pts.put(JSONObject().apply {
                        put("x", p.x.toDouble())
                        put("y", p.y.toDouble())
                    })
                }
                put("points", pts)
            }
            array.put(obj)
        }
        return array.toString()
    }

    private fun deserializeSteps(json: String): List<DrawingStep> {
        val list = mutableListOf<DrawingStep>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val pts = mutableListOf<NormalizedPoint>()
                val ptsArr = obj.optJSONArray("points")
                if (ptsArr != null) {
                    for (j in 0 until ptsArr.length()) {
                        val p = ptsArr.getJSONObject(j)
                        pts.add(NormalizedPoint(p.optDouble("x", 0.0).toFloat(), p.optDouble("y", 0.0).toFloat()))
                    }
                }
                list.add(
                    DrawingStep(
                        step = obj.optInt("step", i + 1),
                        title = obj.optString("title", "Step ${i + 1}"),
                        instruction = obj.optString("instruction", ""),
                        type = obj.optString("type", "curve"),
                        difficulty = obj.optString("difficulty", "easy"),
                        estimatedTime = obj.optString("estimatedTime", "30s"),
                        hint = obj.optString("hint", ""),
                        points = pts
                    )
                )
            }
        } catch (e: Exception) {
            // fallback
        }
        return list
    }

    private fun serializeStrokes(strokes: List<UserStroke>): String {
        val array = JSONArray()
        for (s in strokes) {
            val obj = JSONObject().apply {
                put("id", s.id)
                put("colorArgb", s.colorArgb)
                put("strokeWidth", s.strokeWidth.toDouble())
                put("isEraser", s.isEraser)
                put("stepIndex", s.stepIndex)
                val pts = JSONArray()
                for (p in s.points) {
                    pts.put(JSONObject().apply {
                        put("x", p.x.toDouble())
                        put("y", p.y.toDouble())
                    })
                }
                put("points", pts)
            }
            array.put(obj)
        }
        return array.toString()
    }

    private fun deserializeStrokes(json: String): List<UserStroke> {
        val list = mutableListOf<UserStroke>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val pts = mutableListOf<NormalizedPoint>()
                val ptsArr = obj.optJSONArray("points")
                if (ptsArr != null) {
                    for (j in 0 until ptsArr.length()) {
                        val p = ptsArr.getJSONObject(j)
                        pts.add(NormalizedPoint(p.optDouble("x", 0.0).toFloat(), p.optDouble("y", 0.0).toFloat()))
                    }
                }
                list.add(
                    UserStroke(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        points = pts,
                        colorArgb = obj.optLong("colorArgb", 0xFFFFFFFF),
                        strokeWidth = obj.optDouble("strokeWidth", 6.0).toFloat(),
                        isEraser = obj.optBoolean("isEraser", false),
                        stepIndex = obj.optInt("stepIndex", 0)
                    )
                )
            }
        } catch (e: Exception) {
            // fallback
        }
        return list
    }
}
