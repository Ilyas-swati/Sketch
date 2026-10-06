package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NormalizedPoint
import com.example.model.UserStroke
import com.example.ui.theme.SketchDarkBackground
import com.example.ui.theme.SketchDarkSurface
import com.example.ui.theme.SketchOrange
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CompareScreen(
    referenceBitmap: Bitmap?,
    userStrokes: List<UserStroke>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSplitSliderMode by remember { mutableStateOf(true) }
    var splitFraction by remember { mutableFloatStateOf(0.50f) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SketchDarkBackground)
    ) {
        // Header
        Surface(
            color = SketchDarkSurface,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("compare_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                    Text(
                        text = "Compare with Original",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = isSplitSliderMode,
                        onClick = { isSplitSliderMode = true },
                        label = { Text("Split Slider", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SketchOrange,
                            selectedLabelColor = Color.Black
                        )
                    )
                    FilterChip(
                        selected = !isSplitSliderMode,
                        onClick = { isSplitSliderMode = false },
                        label = { Text("Side by Side", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SketchOrange,
                            selectedLabelColor = Color.Black
                        )
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            if (isSplitSliderMode) {
                // Interactive Interactive Split Reveal
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val canvasW = maxWidth
                    val canvasH = maxHeight

                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF10111A))
                            .pointerInput(Unit) {
                                detectDragGestures { change, _ ->
                                    change.consume()
                                    if (size.width > 0) {
                                        splitFraction = (change.position.x / size.width).coerceIn(0.05f, 0.95f)
                                    }
                                }
                            }
                    ) {
                        val splitX = size.width * splitFraction

                        // Draw Reference Image on the Right side
                        clipRect(left = splitX, top = 0f, right = size.width, bottom = size.height) {
                            if (referenceBitmap != null && !referenceBitmap.isRecycled) {
                                drawFullReference(referenceBitmap, size)
                            }
                        }

                        // Draw User Drawing on the Left side
                        clipRect(left = 0f, top = 0f, right = splitX, bottom = size.height) {
                            for (stroke in userStrokes) {
                                renderSingleStroke(stroke, size)
                            }
                        }

                        // Draw Divider Line
                        drawLine(
                            color = SketchOrange,
                            start = Offset(splitX, 0f),
                            end = Offset(splitX, size.height),
                            strokeWidth = 3f
                        )

                        // Draw Center Knob
                        drawCircle(
                            color = SketchOrange,
                            radius = 16f,
                            center = Offset(splitX, size.height / 2f)
                        )
                        drawCircle(
                            color = Color.Black,
                            radius = 8f,
                            center = Offset(splitX, size.height / 2f)
                        )
                    }

                    // Floating labels
                    Surface(
                        color = Color.Black.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                    ) {
                        Text("Your Sketch", color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(6.dp))
                    }

                    Surface(
                        color = Color.Black.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                    ) {
                        Text("Original", color = SketchOrange, fontSize = 12.sp, modifier = Modifier.padding(6.dp))
                    }
                }
            } else {
                // Side by Side Mode
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Left: User Drawing
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF10111A))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            for (stroke in userStrokes) {
                                renderSingleStroke(stroke, size)
                            }
                        }
                        Surface(
                            color = Color.Black.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(8.dp)
                        ) {
                            Text("Your Sketch", color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    // Right: Reference Image
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF10111A))
                    ) {
                        if (referenceBitmap != null && !referenceBitmap.isRecycled) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawFullReference(referenceBitmap, size)
                            }
                        }
                        Surface(
                            color = Color.Black.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(8.dp)
                        ) {
                            Text("Original Reference", color = SketchOrange, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawFullReference(bitmap: Bitmap, canvasSize: Size) {
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
        dstSize = IntSize(destW, destH)
    )
}

private fun DrawScope.renderSingleStroke(stroke: UserStroke, canvasSize: Size) {
    val pts = stroke.points
    if (pts.isEmpty() || stroke.isEraser) return

    val strokeColor = Color(stroke.colorArgb.toULong())

    if (pts.size == 1) {
        val center = Offset(pts.first().x * canvasSize.width, pts.first().y * canvasSize.height)
        drawCircle(color = strokeColor, radius = stroke.strokeWidth / 2f, center = center)
        return
    }

    val path = Path()
    path.moveTo(pts.first().x * canvasSize.width, pts.first().y * canvasSize.height)
    for (i in 1 until pts.size) {
        val curr = pts[i]
        val prev = pts[i - 1]
        val midX = (prev.x + curr.x) / 2f * canvasSize.width
        val midY = (prev.y + curr.y) / 2f * canvasSize.height
        path.quadraticTo(prev.x * canvasSize.width, prev.y * canvasSize.height, midX, midY)
    }
    path.lineTo(pts.last().x * canvasSize.width, pts.last().y * canvasSize.height)

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
