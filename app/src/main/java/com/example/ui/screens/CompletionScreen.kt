package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserStroke
import com.example.ui.theme.SketchDarkBackground
import com.example.ui.theme.SketchDarkSurface
import com.example.ui.theme.SketchDarkSurfaceElevated
import com.example.ui.theme.SketchOrange
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CompletionScreen(
    title: String,
    totalSteps: Int,
    averageAccuracy: Int,
    userStrokes: List<UserStroke>,
    onViewDrawing: () -> Unit,
    onCompareWithOriginal: () -> Unit,
    onSaveProject: () -> Unit,
    onExportPng: () -> Unit,
    onShare: () -> Unit,
    onStartAgain: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SketchDarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Success Icon
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(SketchOrange.copy(alpha = 0.15f))
                .border(2.dp, SketchOrange, CircleShape)
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Success",
                tint = SketchOrange,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Your Sketch is Complete!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Bravo! You reconstructed \"$title\" line by line.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Sketch Preview Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SketchDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2B2C3F)),
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .shadow(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF10111A))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    for (stroke in userStrokes) {
                        renderSingleStroke(stroke, size)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Stats Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SketchDarkSurfaceElevated, RoundedCornerShape(14.dp))
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            StatItem(label = "Steps Finished", value = "$totalSteps / $totalSteps")
            StatItem(label = "Accuracy", value = "$averageAccuracy%")
            StatItem(label = "Mastery Level", value = if (averageAccuracy >= 80) "Expert" else "Proficient")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons Group
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Compare with Original
            Button(
                onClick = onCompareWithOriginal,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SketchOrange, contentColor = Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("completion_compare_button")
            ) {
                Icon(Icons.Default.Compare, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Compare with Original", fontWeight = FontWeight.Bold)
            }

            // Export PNG
            OutlinedButton(
                onClick = onExportPng,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("completion_export_button")
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export PNG to Gallery")
            }

            // Share & Save Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onShare,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("completion_share_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share")
                }

                OutlinedButton(
                    onClick = onSaveProject,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("completion_save_button")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Project")
                }
            }

            // Return / Start Again Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onViewDrawing,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("completion_view_drawing_button")
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View Drawing", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onStartAgain,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("completion_start_again_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Start Again", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SketchOrange)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
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
