package com.example.ui.canvas

import android.graphics.Bitmap
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.example.model.DrawingStep
import com.example.model.NormalizedPoint
import com.example.model.UserStroke
import java.util.UUID

@Composable
fun SketchCanvasView(
    canvasState: CanvasState,
    steps: List<DrawingStep>,
    currentStepIndex: Int,
    referenceBitmap: Bitmap?,
    onStrokeAdded: (UserStroke) -> Unit,
    onTransformChanged: (Float, Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentPoints = remember { mutableStateListOf<NormalizedPoint>() }
    var canvasSize by remember { mutableStateOf(Size.Zero) }

    // Pulsing animation for the start marker of the current guide
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF10111A))
    ) {
        val gestureModifier = if (canvasState.isPanMode) {
            Modifier.pointerInput(canvasState.zoom, canvasState.panOffset) {
                detectTransformGestures { _, pan, zoomDelta, _ ->
                    val newZoom = (canvasState.zoom * zoomDelta).coerceIn(0.5f, 5.0f)
                    val newPan = canvasState.panOffset + pan
                    onTransformChanged(newZoom, newPan)
                }
            }
        } else {
            Modifier.pointerInput(canvasState.zoom, canvasState.panOffset, canvasSize, currentStepIndex) {
                detectDragGestures(
                    onDragStart = { offset ->
                        if (canvasSize.width > 0 && canvasSize.height > 0) {
                            val normPoint = screenToNormalized(offset, canvasSize, canvasState.zoom, canvasState.panOffset)
                            currentPoints.clear()
                            currentPoints.add(normPoint)
                        }
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        if (canvasSize.width > 0 && canvasSize.height > 0) {
                            val normPoint = screenToNormalized(change.position, canvasSize, canvasState.zoom, canvasState.panOffset)
                            currentPoints.add(normPoint)
                        }
                    },
                    onDragEnd = {
                        if (currentPoints.isNotEmpty()) {
                            val stroke = UserStroke(
                                id = UUID.randomUUID().toString(),
                                points = currentPoints.toList(),
                                colorArgb = if (canvasState.isEraser) 0x00000000 else canvasState.brushColor.value.toLong(),
                                strokeWidth = canvasState.brushSize,
                                isEraser = canvasState.isEraser,
                                stepIndex = currentStepIndex
                            )
                            onStrokeAdded(stroke)
                            currentPoints.clear()
                        }
                    },
                    onDragCancel = {
                        currentPoints.clear()
                    }
                )
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .then(gestureModifier)
        ) {
            canvasSize = size

            translate(canvasState.panOffset.x, canvasState.panOffset.y) {
                scale(canvasState.zoom, pivot = Offset(size.width / 2f, size.height / 2f)) {
                    // 1. Draw Reference Image (if enabled and present)
                    if (canvasState.showReference && referenceBitmap != null && !referenceBitmap.isRecycled) {
                        drawReferenceImage(referenceBitmap, size, canvasState.referenceOpacity)
                    }

                    // 2. Draw Previous Completed Step Guides (subtle skeleton)
                    if (canvasState.showGuide && currentStepIndex > 0) {
                        for (i in 0 until currentStepIndex) {
                            val prevStep = steps.getOrNull(i) ?: continue
                            drawGuideStep(
                                step = prevStep,
                                canvasSize = size,
                                guideColor = canvasState.guideColor,
                                opacity = (canvasState.guideOpacity * 0.22f).coerceIn(0.05f, 0.40f),
                                isCurrentStep = false,
                                pulseScale = 1.0f
                            )
                        }
                    }

                    // 3. Draw Current Active Step Guide (vibrant glowing line)
                    if (canvasState.showGuide && currentStepIndex in steps.indices) {
                        val currentStep = steps[currentStepIndex]
                        drawGuideStep(
                            step = currentStep,
                            canvasSize = size,
                            guideColor = canvasState.guideColor,
                            opacity = canvasState.guideOpacity,
                            isCurrentStep = true,
                            pulseScale = pulseScale
                        )
                    }

                    // 4. Draw Committed User Strokes
                    for (stroke in canvasState.strokes) {
                        drawUserStroke(stroke, size, canvasState.brushOpacity)
                    }

                    // 5. Draw Active In-Progress Stroke
                    if (currentPoints.isNotEmpty()) {
                        val activeStroke = UserStroke(
                            points = currentPoints.toList(),
                            colorArgb = if (canvasState.isEraser) 0xFFFF5252 else canvasState.brushColor.value.toLong(),
                            strokeWidth = canvasState.brushSize,
                            isEraser = canvasState.isEraser,
                            stepIndex = currentStepIndex
                        )
                        drawUserStroke(activeStroke, size, canvasState.brushOpacity)
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawReferenceImage(bitmap: Bitmap, canvasSize: Size, opacity: Float) {
    val bmpWidth = bitmap.width.toFloat()
    val bmpHeight = bitmap.height.toFloat()
    if (bmpWidth <= 0 || bmpHeight <= 0) return

    val scaleFactor = kotlin.math.min(canvasSize.width / bmpWidth, canvasSize.height / bmpHeight)
    val destW = (bmpWidth * scaleFactor).toInt()
    val destH = (bmpHeight * scaleFactor).toInt()
    val left = ((canvasSize.width - destW) / 2f).toInt()
    val top = ((canvasSize.height - destH) / 2f).toInt()

    drawImage(
        image = bitmap.asImageBitmap(),
        dstOffset = IntOffset(left, top),
        dstSize = IntSize(destW, destH),
        alpha = opacity.coerceIn(0.0f, 1.0f)
    )
}

private fun DrawScope.drawGuideStep(
    step: DrawingStep,
    canvasSize: Size,
    guideColor: Color,
    opacity: Float,
    isCurrentStep: Boolean,
    pulseScale: Float
) {
    val pts = step.points
    if (pts.size < 2) return

    val path = Path()
    val firstScreen = normalizedToScreen(pts.first(), canvasSize)
    path.moveTo(firstScreen.x, firstScreen.y)

    for (i in 1 until pts.size) {
        val currScreen = normalizedToScreen(pts[i], canvasSize)
        val prevScreen = normalizedToScreen(pts[i - 1], canvasSize)
        val midX = (prevScreen.x + currScreen.x) / 2f
        val midY = (prevScreen.y + currScreen.y) / 2f
        path.quadraticTo(prevScreen.x, prevScreen.y, midX, midY)
    }
    val lastScreen = normalizedToScreen(pts.last(), canvasSize)
    path.lineTo(lastScreen.x, lastScreen.y)

    if (isCurrentStep) {
        // Subtle outer neon glow for current active guide
        drawPath(
            path = path,
            color = guideColor.copy(alpha = (opacity * 0.35f).coerceIn(0.0f, 1.0f)),
            style = Stroke(
                width = 12f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
        // Core crisp highlighted line
        drawPath(
            path = path,
            color = guideColor.copy(alpha = opacity),
            style = Stroke(
                width = 4.5f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Pulsing start marker dot showing user where to initiate the stroke
        drawCircle(
            color = guideColor.copy(alpha = 0.35f),
            radius = 12f * pulseScale,
            center = firstScreen
        )
        drawCircle(
            color = Color.White,
            radius = 6f,
            center = firstScreen
        )
    } else {
        // Faint completed reference lines
        drawPath(
            path = path,
            color = Color(0xFF8A90A5).copy(alpha = opacity),
            style = Stroke(
                width = 2.5f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

private fun DrawScope.drawUserStroke(stroke: UserStroke, canvasSize: Size, globalOpacity: Float) {
    val pts = stroke.points
    if (pts.isEmpty()) return

    if (stroke.isEraser) {
        // Eraser stroke drawn in canvas background color
        if (pts.size == 1) {
            val center = normalizedToScreen(pts.first(), canvasSize)
            drawCircle(
                color = Color(0xFF10111A),
                radius = stroke.strokeWidth * 1.5f,
                center = center
            )
            return
        }

        val path = Path()
        val p0 = normalizedToScreen(pts.first(), canvasSize)
        path.moveTo(p0.x, p0.y)
        for (i in 1 until pts.size) {
            val pCurr = normalizedToScreen(pts[i], canvasSize)
            path.lineTo(pCurr.x, pCurr.y)
        }
        drawPath(
            path = path,
            color = Color(0xFF10111A),
            style = Stroke(
                width = stroke.strokeWidth * 2.2f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
        return
    }

    val strokeColor = Color(stroke.colorArgb.toULong()).copy(alpha = globalOpacity)

    if (pts.size == 1) {
        val center = normalizedToScreen(pts.first(), canvasSize)
        drawCircle(
            color = strokeColor,
            radius = stroke.strokeWidth / 2f,
            center = center
        )
        return
    }

    val path = Path()
    val first = normalizedToScreen(pts.first(), canvasSize)
    path.moveTo(first.x, first.y)

    for (i in 1 until pts.size) {
        val curr = normalizedToScreen(pts[i], canvasSize)
        val prev = normalizedToScreen(pts[i - 1], canvasSize)
        val midX = (prev.x + curr.x) / 2f
        val midY = (prev.y + curr.y) / 2f
        path.quadraticTo(prev.x, prev.y, midX, midY)
    }
    val last = normalizedToScreen(pts.last(), canvasSize)
    path.lineTo(last.x, last.y)

    drawPath(
        path = path,
        color = strokeColor,
        style = Stroke(
            width = stroke.strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}

private fun screenToNormalized(screenPos: Offset, canvasSize: Size, zoom: Float, panOffset: Offset): NormalizedPoint {
    val cx = canvasSize.width / 2f
    val cy = canvasSize.height / 2f

    val untranslatedX = screenPos.x - panOffset.x
    val untranslatedY = screenPos.y - panOffset.y

    val unscaledX = (untranslatedX - cx) / zoom + cx
    val unscaledY = (untranslatedY - cy) / zoom + cy

    val normX = (unscaledX / canvasSize.width).coerceIn(0.0f, 1.0f)
    val normY = (unscaledY / canvasSize.height).coerceIn(0.0f, 1.0f)
    return NormalizedPoint(normX, normY)
}

private fun normalizedToScreen(point: NormalizedPoint, canvasSize: Size): Offset {
    return Offset(point.x * canvasSize.width, point.y * canvasSize.height)
}
