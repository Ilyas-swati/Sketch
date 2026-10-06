package com.example.ui.canvas

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.model.UserStroke
import com.example.ui.theme.GuideOrange
import com.example.ui.theme.PencilGraphite

data class CanvasState(
    val zoom: Float = 1.0f,
    val panOffset: Offset = Offset.Zero,
    val isPanMode: Boolean = false,
    val brushColor: Color = PencilGraphite,
    val brushSize: Float = 6f,
    val brushOpacity: Float = 1.0f,
    val isEraser: Boolean = false,
    val guideColor: Color = GuideOrange,
    val guideOpacity: Float = 0.95f,
    val showGuide: Boolean = true,
    val referenceOpacity: Float = 0.40f,
    val showReference: Boolean = true,
    val strokes: List<UserStroke> = emptyList(),
    val undoStack: List<List<UserStroke>> = emptyList(),
    val redoStack: List<List<UserStroke>> = emptyList()
)
