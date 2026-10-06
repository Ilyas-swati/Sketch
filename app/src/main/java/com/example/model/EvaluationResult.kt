package com.example.model

enum class EvaluationRating {
    EXCELLENT,
    GOOD,
    TRY_AGAIN
}

data class EvaluationResult(
    val score: Int, // 0 - 100
    val rating: EvaluationRating,
    val title: String,
    val message: String,
    val positionScore: Int,
    val shapeScore: Int,
    val angleScore: Int
)
