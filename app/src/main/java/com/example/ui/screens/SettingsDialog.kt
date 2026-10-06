package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.window.Dialog
import com.example.model.DetailLevel
import com.example.ui.theme.GuideCyan
import com.example.ui.theme.GuideGreen
import com.example.ui.theme.GuideMagenta
import com.example.ui.theme.GuideOrange
import com.example.ui.theme.SketchDarkSurface
import com.example.ui.theme.SketchDarkSurfaceElevated
import com.example.ui.theme.SketchOrange
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsDialog(
    currentApiKey: String,
    currentDetailLevel: DetailLevel,
    currentGuideColor: Color,
    onSaveApiKey: (String) -> Unit,
    onSaveDetailLevel: (DetailLevel) -> Unit,
    onSaveGuideColor: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    var apiKeyInput by remember { mutableStateOf(currentApiKey) }
    var selectedLevel by remember { mutableStateOf(currentDetailLevel) }
    var selectedGuideColor by remember { mutableStateOf(currentGuideColor) }

    val guideColors = listOf(GuideOrange, GuideCyan, GuideGreen, GuideMagenta)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SketchDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF32344A)),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Title & Close
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = SketchOrange)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("App Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("settings_close_button")) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Gemini AI API Key section
                Text(
                    text = "Gemini AI API Key",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Required for analyzing new uploaded photos. Offline tutorials always work without a key.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    placeholder = { Text("AIzaSy...", color = TextSecondary.copy(alpha = 0.5f)) },
                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = SketchOrange) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SketchOrange,
                        unfocusedBorderColor = Color(0xFF36384C),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = SketchDarkSurfaceElevated,
                        unfocusedContainerColor = SketchDarkSurfaceElevated
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("api_key_text_field")
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Drawing Detail Level
                Text(
                    text = "Drawing Detail Level",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Controls step granularity when breaking down uploaded images.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                DetailLevel.entries.forEach { level ->
                    val isSelected = selectedLevel == level
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) SketchOrange.copy(alpha = 0.15f) else SketchDarkSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) SketchOrange else Color(0xFF2C2D40)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { selectedLevel = level }
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    level.displayName,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) SketchOrange else TextPrimary
                                )
                                Text(
                                    level.targetSteps,
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                            Text(
                                level.description,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Guide Line Color
                Text(
                    text = "Default Guide Line Color",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    guideColors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (selectedGuideColor == color) 3.dp else 1.dp,
                                    color = Color.White,
                                    shape = CircleShape
                                )
                                .clickable { selectedGuideColor = color }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Save button
                Button(
                    onClick = {
                        onSaveApiKey(apiKeyInput.trim())
                        onSaveDetailLevel(selectedLevel)
                        onSaveGuideColor(selectedGuideColor)
                        onDismiss()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SketchOrange, contentColor = Color.Black),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_settings_button")
                ) {
                    Text("Save Settings", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
