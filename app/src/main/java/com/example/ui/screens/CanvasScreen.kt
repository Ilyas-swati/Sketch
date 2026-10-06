package com.example.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.model.DrawingMode
import com.example.model.DrawingStep
import com.example.model.EvaluationResult
import com.example.model.UserStroke
import com.example.ui.canvas.CanvasState
import com.example.ui.canvas.CanvasToolsStrip
import com.example.ui.canvas.CanvasTopBar
import com.example.ui.canvas.EvaluationResultDialog
import com.example.ui.canvas.LayersControlDialog
import com.example.ui.canvas.SketchCanvasView
import com.example.ui.canvas.StepProgressionDock
import com.example.ui.theme.SketchDarkBackground

@Composable
fun CanvasScreen(
    title: String,
    steps: List<DrawingStep>,
    currentStepIndex: Int,
    referenceBitmap: Bitmap?,
    canvasState: CanvasState,
    currentMode: DrawingMode,
    evaluationResult: EvaluationResult?,
    showLayersDialog: Boolean,
    onStrokeAdded: (UserStroke) -> Unit,
    onTransformChanged: (Float, Offset) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onClear: () -> Unit,
    onToggleEraser: (Boolean) -> Unit,
    onBrushSizeChange: (Float) -> Unit,
    onBrushColorChange: (Color) -> Unit,
    onTogglePanMode: (Boolean) -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onResetZoom: () -> Unit,
    onModeChange: (DrawingMode) -> Unit,
    onOpenLayers: () -> Unit,
    onCloseLayers: () -> Unit,
    onGuideOpacityChange: (Float) -> Unit,
    onToggleGuide: (Boolean) -> Unit,
    onGuideColorChange: (Color) -> Unit,
    onRefOpacityChange: (Float) -> Unit,
    onToggleRef: (Boolean) -> Unit,
    onPreviousStep: () -> Unit,
    onNextStep: () -> Unit,
    onCheckDrawing: () -> Unit,
    onTryAgainEvaluation: () -> Unit,
    onContinueAnywayEvaluation: () -> Unit,
    onDismissEvaluation: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onBack()
    }

    val currentStep = steps.getOrNull(currentStepIndex)
    val hasStrokesForCurrentStep = canvasState.strokes.any { it.stepIndex == currentStepIndex }

    Scaffold(
        topBar = {
            CanvasTopBar(
                title = title,
                currentMode = currentMode,
                onModeChange = onModeChange,
                canUndo = canvasState.undoStack.isNotEmpty(),
                canRedo = canvasState.redoStack.isNotEmpty(),
                onUndo = onUndo,
                onRedo = onRedo,
                onClear = onClear,
                onOpenLayers = onOpenLayers,
                onBack = onBack
            )
        },
        bottomBar = {
            if (currentMode != DrawingMode.FREE_DRAW) {
                StepProgressionDock(
                    currentStep = currentStep,
                    stepIndex = currentStepIndex,
                    totalSteps = steps.size,
                    onPreviousStep = onPreviousStep,
                    onNextStep = onNextStep,
                    onCheckDrawing = onCheckDrawing,
                    canGoNext = hasStrokesForCurrentStep || currentStepIndex == steps.size - 1
                )
            }
        },
        containerColor = SketchDarkBackground,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Canvas Layer
            SketchCanvasView(
                canvasState = canvasState,
                steps = steps,
                currentStepIndex = currentStepIndex,
                referenceBitmap = referenceBitmap,
                onStrokeAdded = onStrokeAdded,
                onTransformChanged = onTransformChanged,
                modifier = Modifier.fillMaxSize()
            )

            // Canvas Floating Tool Controls Strip (Pencil, Eraser, Size, Color, Zoom, Pan)
            CanvasToolsStrip(
                isEraser = canvasState.isEraser,
                onToggleEraser = onToggleEraser,
                brushSize = canvasState.brushSize,
                onBrushSizeChange = onBrushSizeChange,
                brushColor = canvasState.brushColor,
                onBrushColorChange = onBrushColorChange,
                isPanMode = canvasState.isPanMode,
                onTogglePanMode = onTogglePanMode,
                onZoomIn = onZoomIn,
                onZoomOut = onZoomOut,
                onResetZoom = onResetZoom,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 4.dp, top = 4.dp)
            )

            // Layers & Opacity Dialog Modal
            if (showLayersDialog) {
                LayersControlDialog(
                    canvasState = canvasState,
                    onGuideOpacityChange = onGuideOpacityChange,
                    onToggleGuide = onToggleGuide,
                    onGuideColorChange = onGuideColorChange,
                    onRefOpacityChange = onRefOpacityChange,
                    onToggleRef = onToggleRef,
                    onDismiss = onCloseLayers
                )
            }

            // AI Drawing Evaluation Modal
            if (evaluationResult != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.65f)),
                    contentAlignment = Alignment.Center
                ) {
                    EvaluationResultDialog(
                        result = evaluationResult,
                        onTryAgain = onTryAgainEvaluation,
                        onContinueAnyway = onContinueAnywayEvaluation,
                        onDismiss = onDismissEvaluation
                    )
                }
            }
        }
    }
}
