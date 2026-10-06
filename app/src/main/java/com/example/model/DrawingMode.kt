package com.example.model

enum class DrawingMode(val title: String, val description: String) {
    LEARN("Learn Mode", "AI guides step-by-step with progressive line highlights"),
    TRACE("Trace Mode", "Semi-transparent reference image & guide beneath canvas"),
    FREE_DRAW("Free Draw", "Unrestricted canvas to practice your sketch freely"),
    CHALLENGE("Challenge", "Draw from visual memory, then reveal the guide"),
    COMPARE("Compare", "Inspect your drawing alongside the original reference")
}

enum class DetailLevel(val displayName: String, val targetSteps: String, val description: String) {
    BEGINNER("Beginner", "10-18 steps", "Broad construction shapes & primary guidelines"),
    NORMAL("Normal", "20-30 steps", "Balanced step progression with major features"),
    DETAILED("Detailed", "35-50 steps", "Fine lines, subtle curves, and anatomy details"),
    EXTREMELY_DETAILED("Extreme", "50+ steps", "Micro strokes, hatching, and precise silhouettes")
}
