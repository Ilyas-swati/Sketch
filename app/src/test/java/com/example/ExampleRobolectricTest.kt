package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ai.DrawingEvaluator
import com.example.model.DrawingStep
import com.example.model.EvaluationRating
import com.example.model.NormalizedPoint
import com.example.model.UserStroke
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun readStringFromContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("LineSketch AI", appName)
    }

    @Test
    fun testDrawingEvaluatorAccuracy() {
        val guidePoints = listOf(
            NormalizedPoint(0.1f, 0.1f),
            NormalizedPoint(0.2f, 0.2f),
            NormalizedPoint(0.3f, 0.3f)
        )
        val step = DrawingStep(
            step = 1,
            title = "Test Line",
            instruction = "Draw diagonal line",
            points = guidePoints
        )

        // Closely matching user stroke
        val matchingStroke = UserStroke(
            points = listOf(
                NormalizedPoint(0.11f, 0.10f),
                NormalizedPoint(0.21f, 0.20f),
                NormalizedPoint(0.31f, 0.30f)
            ),
            stepIndex = 0
        )

        val result = DrawingEvaluator.evaluateStep(step, listOf(matchingStroke))
        assertNotNull(result)
        assertTrue(result.score >= 70)
        assertEquals(EvaluationRating.EXCELLENT, result.rating)
    }
}
