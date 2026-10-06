package com.example.ui.canvas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DrawingMode
import com.example.model.DrawingStep
import com.example.model.EvaluationRating
import com.example.model.EvaluationResult
import com.example.ui.theme.GuideCyan
import com.example.ui.theme.GuideGreen
import com.example.ui.theme.GuideMagenta
import com.example.ui.theme.GuideOrange
import com.example.ui.theme.PencilBlue
import com.example.ui.theme.PencilCharcoal
import com.example.ui.theme.PencilGold
import com.example.ui.theme.PencilGraphite
import com.example.ui.theme.PencilGreen
import com.example.ui.theme.PencilRed
import com.example.ui.theme.SketchDarkSurface
import com.example.ui.theme.SketchDarkSurfaceElevated
import com.example.ui.theme.SketchOrange
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CanvasTopBar(
    title: String,
    currentMode: DrawingMode,
    onModeChange: (DrawingMode) -> Unit,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onClear: () -> Unit,
    onOpenLayers: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = SketchDarkSurface.copy(alpha = 0.94f),
        tonalElevation = 6.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("canvas_back_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Projects",
                            tint = TextPrimary
                        )
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        modifier = Modifier.width(140.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onUndo,
                        enabled = canUndo,
                        modifier = Modifier.testTag("undo_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo",
                            tint = if (canUndo) TextPrimary else TextSecondary.copy(alpha = 0.4f)
                        )
                    }
                    IconButton(
                        onClick = onRedo,
                        enabled = canRedo,
                        modifier = Modifier.testTag("redo_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Redo,
                            contentDescription = "Redo",
                            tint = if (canRedo) TextPrimary else TextSecondary.copy(alpha = 0.4f)
                        )
                    }
                    IconButton(
                        onClick = onClear,
                        modifier = Modifier.testTag("clear_button")
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Clear Canvas",
                            tint = TextSecondary
                        )
                    }
                    IconButton(
                        onClick = onOpenLayers,
                        modifier = Modifier.testTag("layers_button")
                    ) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = "Canvas Settings",
                            tint = SketchOrange
                        )
                    }
                }
            }

            // Mode Selector Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                DrawingMode.entries.forEach { mode ->
                    val selected = currentMode == mode
                    FilterChip(
                        selected = selected,
                        onClick = { onModeChange(mode) },
                        label = {
                            Text(
                                mode.title,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SketchOrange,
                            selectedLabelColor = Color.Black,
                            containerColor = SketchDarkSurfaceElevated,
                            labelColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("mode_${mode.name.lowercase()}")
                    )
                }
            }
        }
    }
}

@Composable
fun CanvasToolsStrip(
    isEraser: Boolean,
    onToggleEraser: (Boolean) -> Unit,
    brushSize: Float,
    onBrushSizeChange: (Float) -> Unit,
    brushColor: Color,
    onBrushColorChange: (Color) -> Unit,
    isPanMode: Boolean,
    onTogglePanMode: (Boolean) -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onResetZoom: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showColorPicker by remember { mutableStateOf(false) }
    var showSizeSlider by remember { mutableStateOf(false) }

    val colors = listOf(
        PencilGraphite,
        PencilCharcoal,
        PencilBlue,
        PencilRed,
        PencilGold,
        PencilGreen
    )

    Column(
        modifier = modifier.padding(8.dp),
        horizontalAlignment = Alignment.End
    ) {
        // Quick tools floating bubble
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SketchDarkSurface.copy(alpha = 0.92f),
            tonalElevation = 6.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C2D40)),
            modifier = Modifier.shadow(8.dp, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier.padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Pencil Tool
                IconButton(
                    onClick = { onToggleEraser(false) },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (!isEraser && !isPanMode) SketchOrange else Color.Transparent
                    ),
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Pencil",
                        tint = if (!isEraser && !isPanMode) Color.Black else TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Eraser Tool
                IconButton(
                    onClick = { onToggleEraser(true) },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (isEraser && !isPanMode) SketchOrange else Color.Transparent
                    ),
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(
                        Icons.Default.Brush,
                        contentDescription = "Eraser",
                        tint = if (isEraser && !isPanMode) Color.Black else TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Color picker toggle
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(brushColor)
                        .border(2.dp, Color.White, CircleShape)
                        .clickable { showColorPicker = !showColorPicker }
                )

                // Brush size toggle
                IconButton(
                    onClick = { showSizeSlider = !showSizeSlider },
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size((brushSize * 1.5f).coerceIn(8f, 24f).dp)
                            .clip(CircleShape)
                            .background(SketchOrange)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Pan Mode toggle
                IconButton(
                    onClick = { onTogglePanMode(!isPanMode) },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (isPanMode) Color(0xFF3897F0) else Color.Transparent
                    ),
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(
                        Icons.Default.PanTool,
                        contentDescription = "Pan Canvas",
                        tint = if (isPanMode) Color.White else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Zoom in
                IconButton(
                    onClick = onZoomIn,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        Icons.Default.ZoomIn,
                        contentDescription = "Zoom In",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Zoom out
                IconButton(
                    onClick = onZoomOut,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        Icons.Default.ZoomOut,
                        contentDescription = "Zoom Out",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Reset zoom
                IconButton(
                    onClick = onResetZoom,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Reset Zoom",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Expanded Color Picker Panel
        AnimatedVisibility(
            visible = showColorPicker,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SketchDarkSurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C2D40)),
                modifier = Modifier
                    .padding(top = 8.dp)
                    .width(180.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Pencil Color", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        colors.forEach { col ->
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(col)
                                    .border(
                                        width = if (brushColor == col) 2.dp else 1.dp,
                                        color = if (brushColor == col) SketchOrange else Color.Gray,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        onBrushColorChange(col)
                                        showColorPicker = false
                                    }
                            )
                        }
                    }
                }
            }
        }

        // Expanded Size Slider Panel
        AnimatedVisibility(
            visible = showSizeSlider,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SketchDarkSurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C2D40)),
                modifier = Modifier
                    .padding(top = 8.dp)
                    .width(190.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Stroke Size", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        Text("${brushSize.toInt()}px", style = MaterialTheme.typography.labelMedium, color = SketchOrange)
                    }
                    Slider(
                        value = brushSize,
                        onValueChange = onBrushSizeChange,
                        valueRange = 2f..30f,
                        colors = SliderDefaults.colors(
                            thumbColor = SketchOrange,
                            activeTrackColor = SketchOrange
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun StepProgressionDock(
    currentStep: DrawingStep?,
    stepIndex: Int,
    totalSteps: Int,
    onPreviousStep: () -> Unit,
    onNextStep: () -> Unit,
    onCheckDrawing: () -> Unit,
    canGoNext: Boolean,
    modifier: Modifier = Modifier
) {
    if (currentStep == null) return

    val progress = if (totalSteps > 0) (stepIndex + 1).toFloat() / totalSteps.toFloat() else 0f

    Surface(
        color = SketchDarkSurface.copy(alpha = 0.96f),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        tonalElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF262738)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Step Number and Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = SketchOrange.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Step ${stepIndex + 1}/$totalSteps",
                            color = SketchOrange,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = currentStep.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1
                    )
                }

                Surface(
                    color = SketchDarkSurfaceElevated,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "${currentStep.difficulty.replaceFirstChar { it.uppercase() }} • ${currentStep.estimatedTime}",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Step Instruction Teacher Tip
            Text(
                text = currentStep.instruction,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Linear Progress Indicator
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = SketchOrange,
                trackColor = Color(0xFF252636),
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Actions Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Previous Step Button
                OutlinedButton(
                    onClick = onPreviousStep,
                    enabled = stepIndex > 0,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    modifier = Modifier.testTag("prev_step_button")
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Step",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Previous", fontSize = 12.sp)
                }

                // Check Drawing AI Button
                Button(
                    onClick = onCheckDrawing,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3897F0),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("check_drawing_button")
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = "Check Drawing",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Check Drawing", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                // Next Step Button
                Button(
                    onClick = onNextStep,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canGoNext) SketchOrange else SketchDarkSurfaceElevated,
                        contentColor = if (canGoNext) Color.Black else TextSecondary
                    ),
                    modifier = Modifier.testTag("next_step_button")
                ) {
                    Text(
                        text = if (stepIndex == totalSteps - 1) "Complete" else "Next Step",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Step",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EvaluationResultDialog(
    result: EvaluationResult,
    onTryAgain: () -> Unit,
    onContinueAnyway: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SketchDarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF32344A)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val badgeColor = when (result.rating) {
                EvaluationRating.EXCELLENT -> Color(0xFF00E676)
                EvaluationRating.GOOD -> SketchOrange
                EvaluationRating.TRY_AGAIN -> Color(0xFFFF5252)
            }

            // Score Circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(badgeColor.copy(alpha = 0.15f))
                    .border(3.dp, badgeColor, CircleShape)
            ) {
                Text(
                    text = "${result.score}%",
                    color = badgeColor,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = result.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = badgeColor
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = result.message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Breakdown Metrics
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SketchDarkSurfaceElevated, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                MetricColumn(title = "Position", score = result.positionScore)
                MetricColumn(title = "Shape", score = result.shapeScore)
                MetricColumn(title = "Angle", score = result.angleScore)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onTryAgain,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("evaluation_try_again_button")
                ) {
                    Text("Try Again", color = TextPrimary)
                }

                Button(
                    onClick = onContinueAnyway,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SketchOrange, contentColor = Color.Black),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("evaluation_continue_button")
                ) {
                    Text("Continue", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun MetricColumn(title: String, score: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, fontSize = 11.sp, color = TextSecondary)
        Text(
            "$score%",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (score >= 70) Color(0xFF00E676) else if (score >= 50) SketchOrange else Color(0xFFFF5252)
        )
    }
}

@Composable
fun LayersControlDialog(
    canvasState: CanvasState,
    onGuideOpacityChange: (Float) -> Unit,
    onToggleGuide: (Boolean) -> Unit,
    onGuideColorChange: (Color) -> Unit,
    onRefOpacityChange: (Float) -> Unit,
    onToggleRef: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val guideColors = listOf(GuideOrange, GuideCyan, GuideGreen, GuideMagenta)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SketchDarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF32344A)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(16.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Layers, contentDescription = null, tint = SketchOrange)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Canvas Layers & Guides", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // AI Guide Layer Section
            Surface(
                color = SketchDarkSurfaceElevated,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Show AI Guide", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Switch(
                            checked = canvasState.showGuide,
                            onCheckedChange = onToggleGuide,
                            colors = SwitchDefaults.colors(checkedThumbColor = SketchOrange, checkedTrackColor = SketchOrange.copy(alpha = 0.4f))
                        )
                    }

                    if (canvasState.showGuide) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Guide Opacity", fontSize = 12.sp, color = TextSecondary)
                            Text("${(canvasState.guideOpacity * 100).toInt()}%", fontSize = 12.sp, color = SketchOrange)
                        }
                        Slider(
                            value = canvasState.guideOpacity,
                            onValueChange = onGuideOpacityChange,
                            valueRange = 0.1f..1.0f,
                            colors = SliderDefaults.colors(thumbColor = SketchOrange, activeTrackColor = SketchOrange)
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Guide Line Color", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            guideColors.forEach { col ->
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(col)
                                        .border(
                                            width = if (canvasState.guideColor == col) 2.dp else 0.dp,
                                            color = Color.White,
                                            shape = CircleShape
                                        )
                                        .clickable { onGuideColorChange(col) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Reference Image Layer Section
            Surface(
                color = SketchDarkSurfaceElevated,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Show Reference Image", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Switch(
                            checked = canvasState.showReference,
                            onCheckedChange = onToggleRef,
                            colors = SwitchDefaults.colors(checkedThumbColor = SketchOrange, checkedTrackColor = SketchOrange.copy(alpha = 0.4f))
                        )
                    }

                    if (canvasState.showReference) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Reference Opacity", fontSize = 12.sp, color = TextSecondary)
                            Text("${(canvasState.referenceOpacity * 100).toInt()}%", fontSize = 12.sp, color = SketchOrange)
                        }
                        Slider(
                            value = canvasState.referenceOpacity,
                            onValueChange = onRefOpacityChange,
                            valueRange = 0.1f..1.0f,
                            colors = SliderDefaults.colors(thumbColor = SketchOrange, activeTrackColor = SketchOrange)
                        )

                        // Quick Opacity Presets: 10%, 25%, 50%, 75%, 100%
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(0.10f, 0.25f, 0.50f, 0.75f, 1.0f).forEach { preset ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (kotlin.math.abs(canvasState.referenceOpacity - preset) < 0.05f) SketchOrange else Color(0xFF2C2D40),
                                    modifier = Modifier.clickable { onRefOpacityChange(preset) }
                                ) {
                                    Text(
                                        "${(preset * 100).toInt()}%",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (kotlin.math.abs(canvasState.referenceOpacity - preset) < 0.05f) Color.Black else TextSecondary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
