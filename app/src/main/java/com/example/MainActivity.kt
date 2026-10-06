package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.CanvasScreen
import com.example.ui.screens.CompareScreen
import com.example.ui.screens.CompletionScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsDialog
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SketchDarkBackground
import com.example.ui.theme.SketchDarkSurface
import com.example.ui.theme.SketchOrange
import com.example.ui.theme.SketchOrangeLight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.ExportHelper
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                LineSketchApp()
            }
        }
    }
}

@Composable
fun LineSketchApp(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val recentProjects by viewModel.recentProjects.collectAsStateWithLifecycle()
    val projectTitle by viewModel.projectTitle.collectAsStateWithLifecycle()
    val referenceBitmap by viewModel.referenceBitmap.collectAsStateWithLifecycle()
    val steps by viewModel.steps.collectAsStateWithLifecycle()
    val currentStepIndex by viewModel.currentStepIndex.collectAsStateWithLifecycle()
    val canvasState by viewModel.canvasState.collectAsStateWithLifecycle()
    val currentMode by viewModel.currentMode.collectAsStateWithLifecycle()
    val evaluationResult by viewModel.evaluationResult.collectAsStateWithLifecycle()

    val previewBitmap by viewModel.previewBitmap.collectAsStateWithLifecycle()
    val isAnalyzing by viewModel.isAnalyzing.collectAsStateWithLifecycle()
    val analysisStage by viewModel.analysisStage.collectAsStateWithLifecycle()
    val analysisError by viewModel.analysisError.collectAsStateWithLifecycle()

    val showSettingsDialog by viewModel.showSettingsDialog.collectAsStateWithLifecycle()
    val showLayersDialog by viewModel.showLayersDialog.collectAsStateWithLifecycle()
    val showApiKeyPrompt by viewModel.showApiKeyPrompt.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    LaunchedEffect(errorMessage) {
        errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearErrorMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = SketchDarkBackground,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.HOME -> {
                    HomeScreen(
                        recentProjects = recentProjects,
                        detailLevel = viewModel.detailLevel,
                        previewBitmap = previewBitmap,
                        isAnalyzing = isAnalyzing,
                        analysisStage = analysisStage,
                        analysisError = analysisError,
                        onSelectGalleryImage = { uri -> viewModel.onImageSelectedFromGallery(uri) },
                        onCameraCapture = { bmp -> viewModel.onImageCapturedFromCamera(bmp) },
                        onStartAnalysis = { viewModel.startImageAnalysis() },
                        onDismissPreview = { viewModel.dismissPreview() },
                        onUseOfflineTutorial = { viewModel.useOfflineTutorialForCurrentPreview() },
                        onSelectTutorial = { tut -> viewModel.loadBuiltinTutorial(tut) },
                        onResumeProject = { proj -> viewModel.resumeProject(proj) },
                        onDeleteProject = { proj -> viewModel.deleteProject(proj) },
                        onOpenSettings = { viewModel.openSettings() }
                    )
                }

                AppScreen.CANVAS -> {
                    CanvasScreen(
                        title = projectTitle,
                        steps = steps,
                        currentStepIndex = currentStepIndex,
                        referenceBitmap = referenceBitmap,
                        canvasState = canvasState,
                        currentMode = currentMode,
                        evaluationResult = evaluationResult,
                        showLayersDialog = showLayersDialog,
                        onStrokeAdded = { stroke -> viewModel.addStroke(stroke) },
                        onTransformChanged = { zoom, pan -> viewModel.updateTransform(zoom, pan) },
                        onUndo = { viewModel.undo() },
                        onRedo = { viewModel.redo() },
                        onClear = { viewModel.clearCanvas() },
                        onToggleEraser = { isEraser -> viewModel.setEraserMode(isEraser) },
                        onBrushSizeChange = { size -> viewModel.setBrushSize(size) },
                        onBrushColorChange = { col -> viewModel.setBrushColor(col) },
                        onTogglePanMode = { pan -> viewModel.setPanMode(pan) },
                        onZoomIn = { viewModel.zoomIn() },
                        onZoomOut = { viewModel.zoomOut() },
                        onResetZoom = { viewModel.resetZoom() },
                        onModeChange = { mode -> viewModel.setMode(mode) },
                        onOpenLayers = { viewModel.openLayers() },
                        onCloseLayers = { viewModel.closeLayers() },
                        onGuideOpacityChange = { op -> viewModel.setGuideOpacity(op) },
                        onToggleGuide = { g -> viewModel.toggleGuide(g) },
                        onGuideColorChange = { col -> viewModel.setGuideColor(col) },
                        onRefOpacityChange = { op -> viewModel.setReferenceOpacity(op) },
                        onToggleRef = { r -> viewModel.toggleReference(r) },
                        onPreviousStep = { viewModel.previousStep() },
                        onNextStep = { viewModel.nextStep() },
                        onCheckDrawing = { viewModel.checkDrawing() },
                        onTryAgainEvaluation = { viewModel.tryAgainCurrentStep() },
                        onContinueAnywayEvaluation = { viewModel.nextStep() },
                        onDismissEvaluation = { viewModel.dismissEvaluation() },
                        onBack = {
                            viewModel.autoSaveProject()
                            viewModel.navigateTo(AppScreen.HOME)
                        }
                    )
                }

                AppScreen.COMPARE -> {
                    CompareScreen(
                        referenceBitmap = referenceBitmap,
                        userStrokes = canvasState.strokes,
                        onBack = { viewModel.navigateTo(AppScreen.CANVAS) }
                    )
                }

                AppScreen.COMPLETION -> {
                    CompletionScreen(
                        title = projectTitle,
                        totalSteps = steps.size,
                        averageAccuracy = 88,
                        userStrokes = canvasState.strokes,
                        onViewDrawing = { viewModel.navigateTo(AppScreen.CANVAS) },
                        onCompareWithOriginal = { viewModel.navigateTo(AppScreen.COMPARE) },
                        onSaveProject = {
                            viewModel.autoSaveProject()
                            Toast.makeText(context, "Project saved to LineSketch library!", Toast.LENGTH_SHORT).show()
                        },
                        onExportPng = {
                            val rendered = ExportHelper.renderStrokesToBitmap(canvasState.strokes)
                            ExportHelper.saveBitmapToGallery(context, rendered, projectTitle)
                        },
                        onShare = {
                            val rendered = ExportHelper.renderStrokesToBitmap(canvasState.strokes)
                            ExportHelper.shareSketch(context, rendered)
                        },
                        onStartAgain = {
                            viewModel.clearCanvas()
                            viewModel.navigateTo(AppScreen.CANVAS)
                        }
                    )
                }
            }

            // Global Fullscreen Loading Overlay (when analyzing)
            AnimatedVisibility(
                visible = isAnalyzing,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.75f))
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = SketchDarkSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E3048)),
                        modifier = Modifier
                            .padding(32.dp)
                            .shadow(16.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(28.dp)
                        ) {
                            CircularProgressIndicator(
                                color = SketchOrange,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(52.dp)
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Text(
                                text = "Analyzing Image...",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = analysisStage,
                                style = MaterialTheme.typography.bodySmall,
                                color = SketchOrangeLight,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Settings Dialog
            if (showSettingsDialog || showApiKeyPrompt) {
                SettingsDialog(
                    currentApiKey = viewModel.apiKey,
                    currentDetailLevel = viewModel.detailLevel,
                    currentGuideColor = canvasState.guideColor,
                    onSaveApiKey = { key ->
                        viewModel.saveApiKey(key)
                        viewModel.dismissApiKeyPrompt()
                    },
                    onSaveDetailLevel = { level -> viewModel.saveDetailLevel(level) },
                    onSaveGuideColor = { color -> viewModel.saveGuideColor(color) },
                    onDismiss = {
                        viewModel.closeSettings()
                        viewModel.dismissApiKeyPrompt()
                    }
                )
            }
        }
    }
}
