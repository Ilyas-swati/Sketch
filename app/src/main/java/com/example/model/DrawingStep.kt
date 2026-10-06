package com.example.model

/**
 * Represents a single drawing instruction and vector guide in the progressive tutorial.
 */
data class DrawingStep(
    val step: Int,
    val title: String,
    val instruction: String,
    val type: String = "line", // "circle", "line", "curve", "ellipse", "polygon", "shading"
    val difficulty: String = "easy",
    val estimatedTime: String = "30s",
    val points: List<NormalizedPoint> = emptyList(),
    val hint: String = "Follow the highlighted path closely"
)
