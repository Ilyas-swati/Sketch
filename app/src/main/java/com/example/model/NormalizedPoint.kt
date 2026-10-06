package com.example.model

/**
 * Normalized 2D coordinate where x and y are scaled between 0.0f and 1.0f.
 * This ensures vector sketch paths scale crisply to any canvas resolution or screen size.
 */
data class NormalizedPoint(
    val x: Float,
    val y: Float
)
