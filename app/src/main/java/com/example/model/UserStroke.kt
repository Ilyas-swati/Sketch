package com.example.model

import java.util.UUID

/**
 * Represents a vector stroke drawn on the canvas by the user.
 */
data class UserStroke(
    val id: String = UUID.randomUUID().toString(),
    val points: List<NormalizedPoint>,
    val colorArgb: Long = 0xFFFFFFFF,
    val strokeWidth: Float = 6f,
    val isEraser: Boolean = false,
    val stepIndex: Int = 0
)
