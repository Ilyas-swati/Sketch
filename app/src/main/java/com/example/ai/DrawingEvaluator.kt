package com.example.ai

import com.example.model.DrawingStep
import com.example.model.EvaluationRating
import com.example.model.EvaluationResult
import com.example.model.NormalizedPoint
import com.example.model.UserStroke
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

object DrawingEvaluator {

    /**
     * Evaluates user drawn strokes for the current step against the target guide points.
     * Computes position proximity, shape similarity, and angle alignment.
     */
    fun evaluateStep(step: DrawingStep, strokesForStep: List<UserStroke>): EvaluationResult {
        val guidePoints = step.points
        if (guidePoints.isEmpty()) {
            return EvaluationResult(
                score = 85,
                rating = EvaluationRating.EXCELLENT,
                title = "Excellent",
                message = "Great! Your line is very close.",
                positionScore = 85,
                shapeScore = 85,
                angleScore = 85
            )
        }

        // Collect all non-eraser user points
        val userPoints = strokesForStep
            .filter { !it.isEraser }
            .flatMap { it.points }

        if (userPoints.size < 2) {
            return EvaluationResult(
                score = 30,
                rating = EvaluationRating.TRY_AGAIN,
                title = "Try Again",
                message = "The line is too short or missing. Please trace along the highlighted guide line.",
                positionScore = 30,
                shapeScore = 30,
                angleScore = 30
            )
        }

        // 1. Position Metric: Mean distance between user points and closest guide points
        var totalDistUserToGuide = 0.0
        for (up in userPoints) {
            var minDist = Double.MAX_VALUE
            for (gp in guidePoints) {
                val d = distance(up, gp)
                if (d < minDist) minDist = d
            }
            totalDistUserToGuide += minDist
        }
        val avgDistToGuide = totalDistUserToGuide / userPoints.size

        // In normalized coordinates (0.0 to 1.0), 0.04 (4% of canvas) is near-perfect
        val maxAllowableDist = 0.22
        val positionRaw = (1.0 - (avgDistToGuide / maxAllowableDist)).coerceIn(0.0, 1.0)
        val positionScore = (positionRaw * 100).toInt()

        // 2. Shape / Coverage Metric: Guide points covered by user drawing
        var coveredGuidePoints = 0
        for (gp in guidePoints) {
            var minDist = Double.MAX_VALUE
            for (up in userPoints) {
                val d = distance(up, gp)
                if (d < minDist) minDist = d
            }
            if (minDist <= 0.08) {
                coveredGuidePoints++
            }
        }
        val coverageRatio = (coveredGuidePoints.toDouble() / guidePoints.size).coerceIn(0.0, 1.0)
        val shapeScore = ((coverageRatio * 0.7 + positionRaw * 0.3) * 100).toInt()

        // 3. Angle / Direction Alignment
        val guideAngle = calculatePrincipalAngle(guidePoints)
        val userAngle = calculatePrincipalAngle(userPoints)
        val angleDiff = min(abs(guideAngle - userAngle), abs(abs(guideAngle - userAngle) - 180.0))
        val angleAlignment = (1.0 - (angleDiff / 90.0)).coerceIn(0.0, 1.0)
        val angleScore = (angleAlignment * 100).toInt()

        // Weighted Overall Score
        val compositeScore = (positionScore * 0.45 + shapeScore * 0.35 + angleScore * 0.20).toInt().coerceIn(15, 98)

        val rating: EvaluationRating
        val title: String
        val message: String

        when {
            compositeScore >= 75 -> {
                rating = EvaluationRating.EXCELLENT
                title = "Excellent!"
                message = "Great! Your line is very close. Precision and confidence are looking great."
            }
            compositeScore >= 52 -> {
                rating = EvaluationRating.GOOD
                title = "Good!"
                message = "Good attempt! Try adjusting the curve slightly or follow the guide closer."
            }
            else -> {
                rating = EvaluationRating.TRY_AGAIN
                title = "Try Again"
                message = "The line is too far from the guide. Don't worry, relax your hand and trace along the highlight."
            }
        }

        return EvaluationResult(
            score = compositeScore,
            rating = rating,
            title = title,
            message = message,
            positionScore = positionScore,
            shapeScore = shapeScore,
            angleScore = angleScore
        )
    }

    private fun distance(p1: NormalizedPoint, p2: NormalizedPoint): Double {
        val dx = (p1.x - p2.x).toDouble()
        val dy = (p1.y - p2.y).toDouble()
        return sqrt(dx * dx + dy * dy)
    }

    private fun calculatePrincipalAngle(points: List<NormalizedPoint>): Double {
        if (points.size < 2) return 0.0
        val pFirst = points.first()
        val pLast = points.last()
        val rad = atan2((pLast.y - pFirst.y).toDouble(), (pLast.x - pFirst.x).toDouble())
        val deg = Math.toDegrees(rad)
        return if (deg < 0) deg + 360.0 else deg
    }
}
