package com.example.data

import com.example.model.DrawingStep
import com.example.model.NormalizedPoint
import kotlin.math.cos
import kotlin.math.sin

object TutorialProvider {

    data class TutorialTemplate(
        val id: String,
        val title: String,
        val subtitle: String,
        val category: String,
        val difficulty: String,
        val stepCount: Int,
        val iconRes: String,
        val steps: List<DrawingStep>
    )

    fun getBuiltinTutorials(): List<TutorialTemplate> {
        return listOf(
            createPortraitTutorial(),
            createAnimeEyeTutorial(),
            createCatSketchTutorial(),
            createStillLifeTutorial()
        )
    }

    private fun createCirclePoints(cx: Float, cy: Float, radius: Float, count: Int = 36): List<NormalizedPoint> {
        val points = mutableListOf<NormalizedPoint>()
        for (i in 0..count) {
            val angle = (i.toFloat() / count) * 2f * Math.PI.toFloat()
            points.add(NormalizedPoint(cx + radius * cos(angle), cy + radius * sin(angle)))
        }
        return points
    }

    private fun createArcPoints(
        cx: Float,
        cy: Float,
        rx: Float,
        ry: Float,
        startDeg: Float,
        endDeg: Float,
        count: Int = 20
    ): List<NormalizedPoint> {
        val points = mutableListOf<NormalizedPoint>()
        val startRad = Math.toRadians(startDeg.toDouble()).toFloat()
        val endRad = Math.toRadians(endDeg.toDouble()).toFloat()
        for (i in 0..count) {
            val t = i.toFloat() / count
            val angle = startRad + t * (endRad - startRad)
            points.add(NormalizedPoint(cx + rx * cos(angle), cy + ry * sin(angle)))
        }
        return points
    }

    private fun createLinePoints(x1: Float, y1: Float, x2: Float, y2: Float, count: Int = 12): List<NormalizedPoint> {
        val points = mutableListOf<NormalizedPoint>()
        for (i in 0..count) {
            val t = i.toFloat() / count
            points.add(NormalizedPoint(x1 + t * (x2 - x1), y1 + t * (y2 - y1)))
        }
        return points
    }

    // 1. Comprehensive 20-Step Portrait Construction Tutorial
    private fun createPortraitTutorial(): TutorialTemplate {
        val steps = listOf(
            DrawingStep(
                step = 1,
                title = "1. Head Shape (Cranial Sphere)",
                instruction = "Draw a smooth circle for the cranial mass of the skull. Keep your wrist loose and fluid.",
                type = "circle",
                difficulty = "easy",
                estimatedTime = "30s",
                points = createCirclePoints(0.50f, 0.36f, 0.20f),
                hint = "Trace smoothly around the center circular guide"
            ),
            DrawingStep(
                step = 2,
                title = "2. Face Center Line",
                instruction = "Draw a vertical plumb line through the exact center of the head to establish symmetrical balance.",
                type = "line",
                difficulty = "easy",
                estimatedTime = "15s",
                points = createLinePoints(0.50f, 0.16f, 0.50f, 0.70f),
                hint = "Draw straight down from crown to chin"
            ),
            DrawingStep(
                step = 3,
                title = "3. Eye Horizon Line",
                instruction = "Add a horizontal guideline midway down the head circle to position the eyes and brow ridge.",
                type = "line",
                difficulty = "easy",
                estimatedTime = "15s",
                points = createLinePoints(0.28f, 0.38f, 0.72f, 0.38f),
                hint = "Draw a horizontal perpendicular line across"
            ),
            DrawingStep(
                step = 4,
                title = "4. Jaw & Chin Contour",
                instruction = "Construct the jaw planes descending from the temples, curving inward to meet at the chin.",
                type = "curve",
                difficulty = "medium",
                estimatedTime = "40s",
                points = listOf(
                    NormalizedPoint(0.32f, 0.38f),
                    NormalizedPoint(0.35f, 0.48f),
                    NormalizedPoint(0.40f, 0.58f),
                    NormalizedPoint(0.46f, 0.65f),
                    NormalizedPoint(0.50f, 0.66f),
                    NormalizedPoint(0.54f, 0.65f),
                    NormalizedPoint(0.60f, 0.58f),
                    NormalizedPoint(0.65f, 0.48f),
                    NormalizedPoint(0.68f, 0.38f)
                ),
                hint = "Follow the angular jaw taper towards the chin"
            ),
            DrawingStep(
                step = 5,
                title = "5. Ear Positions",
                instruction = "Sketch the outer ear curves aligned between the brow level and the nose base.",
                type = "curve",
                difficulty = "easy",
                estimatedTime = "25s",
                points = listOf(
                    NormalizedPoint(0.30f, 0.36f),
                    NormalizedPoint(0.27f, 0.42f),
                    NormalizedPoint(0.29f, 0.48f),
                    NormalizedPoint(0.32f, 0.47f),
                    NormalizedPoint(0.68f, 0.47f),
                    NormalizedPoint(0.71f, 0.48f),
                    NormalizedPoint(0.73f, 0.42f),
                    NormalizedPoint(0.70f, 0.36f)
                ),
                hint = "Place subtle C-curves on both sides"
            ),
            DrawingStep(
                step = 6,
                title = "6. Left Eye Construction",
                instruction = "Draw the upper and lower almond contours of the left eye sitting on the eye horizon line.",
                type = "curve",
                difficulty = "medium",
                estimatedTime = "30s",
                points = listOf(
                    NormalizedPoint(0.36f, 0.38f),
                    NormalizedPoint(0.39f, 0.36f),
                    NormalizedPoint(0.43f, 0.37f),
                    NormalizedPoint(0.44f, 0.38f),
                    NormalizedPoint(0.41f, 0.40f),
                    NormalizedPoint(0.36f, 0.38f)
                ),
                hint = "Curve upward from inner tear duct to outer corner"
            ),
            DrawingStep(
                step = 7,
                title = "7. Right Eye Construction",
                instruction = "Match the right eye symmetrical spacing (one eye-width apart from the left eye).",
                type = "curve",
                difficulty = "medium",
                estimatedTime = "30s",
                points = listOf(
                    NormalizedPoint(0.56f, 0.38f),
                    NormalizedPoint(0.57f, 0.37f),
                    NormalizedPoint(0.61f, 0.36f),
                    NormalizedPoint(0.64f, 0.38f),
                    NormalizedPoint(0.60f, 0.40f),
                    NormalizedPoint(0.56f, 0.38f)
                ),
                hint = "Mirror the left eye curve smoothly"
            ),
            DrawingStep(
                step = 8,
                title = "8. Eyebrows",
                instruction = "Form the expressive arches of both eyebrows tapering outward toward the temples.",
                type = "curve",
                difficulty = "easy",
                estimatedTime = "20s",
                points = listOf(
                    NormalizedPoint(0.35f, 0.34f),
                    NormalizedPoint(0.40f, 0.32f),
                    NormalizedPoint(0.45f, 0.33f),
                    NormalizedPoint(0.55f, 0.33f),
                    NormalizedPoint(0.60f, 0.32f),
                    NormalizedPoint(0.65f, 0.34f)
                ),
                hint = "Arch slightly above each eye socket"
            ),
            DrawingStep(
                step = 9,
                title = "9. Nose Bridge",
                instruction = "Draw the slender bridge of the nose tracing down the facial midline.",
                type = "line",
                difficulty = "easy",
                estimatedTime = "15s",
                points = createLinePoints(0.49f, 0.37f, 0.49f, 0.49f),
                hint = "A gentle vertical accent along the midline"
            ),
            DrawingStep(
                step = 10,
                title = "10. Nose Tip & Nostril Wings",
                instruction = "Sketch the rounded ball of the nose and the subtle flares of the nostrils.",
                type = "curve",
                difficulty = "medium",
                estimatedTime = "30s",
                points = listOf(
                    NormalizedPoint(0.45f, 0.51f),
                    NormalizedPoint(0.47f, 0.52f),
                    NormalizedPoint(0.50f, 0.51f),
                    NormalizedPoint(0.53f, 0.52f),
                    NormalizedPoint(0.55f, 0.51f)
                ),
                hint = "Shape the small curved nostril base"
            ),
            DrawingStep(
                step = 11,
                title = "11. Nose Shadow Plane",
                instruction = "Define the small cast shadow shelf underneath the nose tip.",
                type = "shading",
                difficulty = "easy",
                estimatedTime = "20s",
                points = listOf(
                    NormalizedPoint(0.48f, 0.52f),
                    NormalizedPoint(0.50f, 0.53f),
                    NormalizedPoint(0.52f, 0.52f)
                ),
                hint = "A soft downward shadow notch"
            ),
            DrawingStep(
                step = 12,
                title = "12. Upper Lip & Cupid's Bow",
                instruction = "Draw the elegant double-curve of the Cupid's bow and the parting line of the mouth.",
                type = "curve",
                difficulty = "medium",
                estimatedTime = "30s",
                points = listOf(
                    NormalizedPoint(0.42f, 0.57f),
                    NormalizedPoint(0.47f, 0.56f),
                    NormalizedPoint(0.50f, 0.57f),
                    NormalizedPoint(0.53f, 0.56f),
                    NormalizedPoint(0.58f, 0.57f)
                ),
                hint = "Follow the soft M-curve of the upper lip"
            ),
            DrawingStep(
                step = 13,
                title = "13. Lower Lip Arc",
                instruction = "Add the fullness of the lower lip with a gentle upward cradle curve.",
                type = "curve",
                difficulty = "easy",
                estimatedTime = "20s",
                points = listOf(
                    NormalizedPoint(0.44f, 0.58f),
                    NormalizedPoint(0.50f, 0.60f),
                    NormalizedPoint(0.56f, 0.58f)
                ),
                hint = "Support the smile with a center curve"
            ),
            DrawingStep(
                step = 14,
                title = "14. Chin Shadow & Form",
                instruction = "Emphasize the chin ball by adding the labiomental crease line beneath the lower lip.",
                type = "curve",
                difficulty = "easy",
                estimatedTime = "15s",
                points = listOf(
                    NormalizedPoint(0.47f, 0.62f),
                    NormalizedPoint(0.50f, 0.63f),
                    NormalizedPoint(0.53f, 0.62f)
                ),
                hint = "Define the chin prominence"
            ),
            DrawingStep(
                step = 15,
                title = "15. Hair Outline (Volume)",
                instruction = "Draft the overarching crown of hair sitting above the skull sphere to give realistic volume.",
                type = "curve",
                difficulty = "medium",
                estimatedTime = "45s",
                points = listOf(
                    NormalizedPoint(0.26f, 0.40f),
                    NormalizedPoint(0.24f, 0.28f),
                    NormalizedPoint(0.32f, 0.16f),
                    NormalizedPoint(0.50f, 0.12f),
                    NormalizedPoint(0.68f, 0.16f),
                    NormalizedPoint(0.76f, 0.28f),
                    NormalizedPoint(0.74f, 0.40f)
                ),
                hint = "Sweep smoothly over the top of the skull"
            ),
            DrawingStep(
                step = 16,
                title = "16. Hair Bangs & Flowing Locks",
                instruction = "Add the sweeping parting line and bangs framing the forehead.",
                type = "curve",
                difficulty = "medium",
                estimatedTime = "35s",
                points = listOf(
                    NormalizedPoint(0.48f, 0.16f),
                    NormalizedPoint(0.42f, 0.25f),
                    NormalizedPoint(0.36f, 0.30f),
                    NormalizedPoint(0.52f, 0.17f),
                    NormalizedPoint(0.58f, 0.26f),
                    NormalizedPoint(0.64f, 0.31f)
                ),
                hint = "Break hair into clean overlapping groups"
            ),
            DrawingStep(
                step = 17,
                title = "17. Neck & Cylinder Foundation",
                instruction = "Draw the strong neck columns extending downward from behind the jaw angle.",
                type = "line",
                difficulty = "easy",
                estimatedTime = "20s",
                points = listOf(
                    NormalizedPoint(0.40f, 0.64f),
                    NormalizedPoint(0.38f, 0.78f),
                    NormalizedPoint(0.60f, 0.64f),
                    NormalizedPoint(0.62f, 0.78f)
                ),
                hint = "Two straight lines anchoring the head"
            ),
            DrawingStep(
                step = 18,
                title = "18. Shoulders & Collar",
                instruction = "Sketch the sloping shoulder line and collarbone garment boundary.",
                type = "curve",
                difficulty = "easy",
                estimatedTime = "25s",
                points = listOf(
                    NormalizedPoint(0.20f, 0.88f),
                    NormalizedPoint(0.35f, 0.82f),
                    NormalizedPoint(0.50f, 0.84f),
                    NormalizedPoint(0.65f, 0.82f),
                    NormalizedPoint(0.80f, 0.88f)
                ),
                hint = "Sweep down smoothly along shoulders"
            ),
            DrawingStep(
                step = 19,
                title = "19. Jawline Drop Shadow",
                instruction = "Add the cast shadow hatching beneath the jaw to detach head from neck.",
                type = "shading",
                difficulty = "medium",
                estimatedTime = "30s",
                points = listOf(
                    NormalizedPoint(0.42f, 0.67f),
                    NormalizedPoint(0.50f, 0.70f),
                    NormalizedPoint(0.58f, 0.67f)
                ),
                hint = "Crosshatch lightly under the chin"
            ),
            DrawingStep(
                step = 20,
                title = "20. Final Iris Pupils & Highlights",
                instruction = "Place the circular irises, dark pupils, and sparkling catchlight reflections to bring the portrait to life!",
                type = "circle",
                difficulty = "easy",
                estimatedTime = "30s",
                points = listOf(
                    NormalizedPoint(0.40f, 0.38f),
                    NormalizedPoint(0.40f, 0.37f),
                    NormalizedPoint(0.60f, 0.38f),
                    NormalizedPoint(0.60f, 0.37f)
                ),
                hint = "Add center eye pupils and sparkle"
            )
        )

        return TutorialTemplate(
            id = "tutorial_portrait_20",
            title = "Master Portrait Construction",
            subtitle = "Loomis Method: Complete 20-step facial anatomy",
            category = "Portraits",
            difficulty = "Normal",
            stepCount = steps.size,
            iconRes = "portrait",
            steps = steps
        )
    }

    // 2. Anime Eye Masterclass (12 steps)
    private fun createAnimeEyeTutorial(): TutorialTemplate {
        val steps = listOf(
            DrawingStep(
                step = 1,
                title = "1. Upper Eyelid Arch",
                instruction = "Draw a bold, confident arc for the thick upper lash line.",
                type = "curve",
                difficulty = "easy",
                estimatedTime = "20s",
                points = listOf(
                    NormalizedPoint(0.25f, 0.45f),
                    NormalizedPoint(0.40f, 0.32f),
                    NormalizedPoint(0.65f, 0.30f),
                    NormalizedPoint(0.78f, 0.38f)
                ),
                hint = "Arch high and thicken at the outer corner"
            ),
            DrawingStep(
                step = 2,
                title = "2. Outer Lash Wing",
                instruction = "Flick the outer corner upward into a stylish anime wing.",
                type = "curve",
                difficulty = "easy",
                estimatedTime = "15s",
                points = listOf(
                    NormalizedPoint(0.78f, 0.38f),
                    NormalizedPoint(0.85f, 0.33f)
                ),
                hint = "Sharp taper flick"
            ),
            DrawingStep(
                step = 3,
                title = "3. Lower Lash Accent",
                instruction = "Sketch a delicate lower curve, leaving a gap for modern anime styling.",
                type = "curve",
                difficulty = "easy",
                estimatedTime = "15s",
                points = listOf(
                    NormalizedPoint(0.42f, 0.65f),
                    NormalizedPoint(0.60f, 0.66f),
                    NormalizedPoint(0.70f, 0.62f)
                ),
                hint = "Soft subtle curve below"
            ),
            DrawingStep(
                step = 4,
                title = "4. Iris Outer Oval",
                instruction = "Draw a large vertical ellipse tucked under the top eyelid.",
                type = "ellipse",
                difficulty = "medium",
                estimatedTime = "30s",
                points = createArcPoints(0.52f, 0.49f, 0.16f, 0.18f, 0f, 360f, 30),
                hint = "Draw an elongated oval"
            ),
            DrawingStep(
                step = 5,
                title = "5. Primary Catchlight Bubble",
                instruction = "Circle a bright highlight at the top-left of the iris.",
                type = "circle",
                difficulty = "easy",
                estimatedTime = "15s",
                points = createCirclePoints(0.46f, 0.40f, 0.04f),
                hint = "Keep this area pure white"
            ),
            DrawingStep(
                step = 6,
                title = "6. Secondary Highlight",
                instruction = "Add a smaller reflection bubble near the bottom edge.",
                type = "circle",
                difficulty = "easy",
                estimatedTime = "15s",
                points = createCirclePoints(0.58f, 0.58f, 0.025f),
                hint = "Small sparkling accent"
            ),
            DrawingStep(
                step = 7,
                title = "7. Center Pupil Oval",
                instruction = "Darken the center vertical pupil oval.",
                type = "ellipse",
                difficulty = "medium",
                estimatedTime = "25s",
                points = createArcPoints(0.52f, 0.49f, 0.06f, 0.09f, 0f, 360f, 20),
                hint = "Centered inside the iris"
            ),
            DrawingStep(
                step = 8,
                title = "8. Iris Upper Shadow",
                instruction = "Shade the upper half of the iris cast by the heavy upper eyelid.",
                type = "shading",
                difficulty = "easy",
                estimatedTime = "25s",
                points = listOf(
                    NormalizedPoint(0.38f, 0.45f),
                    NormalizedPoint(0.52f, 0.48f),
                    NormalizedPoint(0.66f, 0.45f)
                ),
                hint = "Horizontal gradation shelf"
            ),
            DrawingStep(
                step = 9,
                title = "9. Double Eyelid Crease",
                instruction = "Draw a fine parallel fold line hovering above the top lid.",
                type = "curve",
                difficulty = "easy",
                estimatedTime = "15s",
                points = listOf(
                    NormalizedPoint(0.32f, 0.28f),
                    NormalizedPoint(0.52f, 0.24f),
                    NormalizedPoint(0.70f, 0.27f)
                ),
                hint = "Fine curved crease"
            ),
            DrawingStep(
                step = 10,
                title = "10. Eyebrow Curve",
                instruction = "Draw an arched anime eyebrow expressing personality.",
                type = "curve",
                difficulty = "easy",
                estimatedTime = "20s",
                points = listOf(
                    NormalizedPoint(0.26f, 0.18f),
                    NormalizedPoint(0.48f, 0.14f),
                    NormalizedPoint(0.76f, 0.19f)
                ),
                hint = "Graceful tapering arch"
            ),
            DrawingStep(
                step = 11,
                title = "11. Individual Eyelash Spikes",
                instruction = "Add 2-3 expressive eyelash spikes from the upper lid contour.",
                type = "line",
                difficulty = "easy",
                estimatedTime = "20s",
                points = listOf(
                    NormalizedPoint(0.45f, 0.31f),
                    NormalizedPoint(0.43f, 0.26f),
                    NormalizedPoint(0.62f, 0.31f),
                    NormalizedPoint(0.63f, 0.26f)
                ),
                hint = "Sharp decorative lash tips"
            ),
            DrawingStep(
                step = 12,
                title = "12. Final Iris Glow & Reflected Light",
                instruction = "Add soft crescent radiant lines at the bottom of the iris for dazzling sparkle!",
                type = "curve",
                difficulty = "easy",
                estimatedTime = "25s",
                points = listOf(
                    NormalizedPoint(0.44f, 0.58f),
                    NormalizedPoint(0.52f, 0.62f),
                    NormalizedPoint(0.60f, 0.58f)
                ),
                hint = "Lower iris reflection glow"
            )
        )

        return TutorialTemplate(
            id = "tutorial_anime_eye_12",
            title = "Anime Eye Masterclass",
            subtitle = "Shonen & Shojo style expressive eye with glossy highlights",
            category = "Anime",
            difficulty = "Beginner",
            stepCount = steps.size,
            iconRes = "eye",
            steps = steps
        )
    }

    // 3. Cute Cat Sketch (15 steps)
    private fun createCatSketchTutorial(): TutorialTemplate {
        val steps = listOf(
            DrawingStep(
                step = 1,
                title = "1. Head Sphere",
                instruction = "Draw a slightly flattened circle for the cat's cranial structure.",
                type = "circle",
                difficulty = "easy",
                estimatedTime = "20s",
                points = createCirclePoints(0.50f, 0.32f, 0.18f),
                hint = "Round head guide"
            ),
            DrawingStep(
                step = 2,
                title = "2. Body Oval",
                instruction = "Draw a leaning oval below the head for the seated torso.",
                type = "ellipse",
                difficulty = "easy",
                estimatedTime = "25s",
                points = createArcPoints(0.50f, 0.62f, 0.20f, 0.22f, 0f, 360f, 25),
                hint = "Seated cat body foundation"
            ),
            DrawingStep(
                step = 3,
                title = "3. Left Ear Triangle",
                instruction = "Form the pointy left ear with slightly curved outward sides.",
                type = "polygon",
                difficulty = "easy",
                estimatedTime = "20s",
                points = listOf(
                    NormalizedPoint(0.36f, 0.22f),
                    NormalizedPoint(0.32f, 0.10f),
                    NormalizedPoint(0.44f, 0.16f)
                ),
                hint = "Perky upright triangle"
            ),
            DrawingStep(
                step = 4,
                title = "4. Right Ear Triangle",
                instruction = "Mirror the right ear triangle with matching angle.",
                type = "polygon",
                difficulty = "easy",
                estimatedTime = "20s",
                points = listOf(
                    NormalizedPoint(0.56f, 0.16f),
                    NormalizedPoint(0.68f, 0.10f),
                    NormalizedPoint(0.64f, 0.22f)
                ),
                hint = "Symmetric perky ear"
            ),
            DrawingStep(
                step = 5,
                title = "5. Facial Midline & Eye Line",
                instruction = "Cross guidelines through the cat's face.",
                type = "line",
                difficulty = "easy",
                estimatedTime = "15s",
                points = listOf(
                    NormalizedPoint(0.50f, 0.16f),
                    NormalizedPoint(0.50f, 0.48f),
                    NormalizedPoint(0.34f, 0.33f),
                    NormalizedPoint(0.66f, 0.33f)
                ),
                hint = "Center facial cross"
            ),
            DrawingStep(
                step = 6,
                title = "6. Left Almond Eye",
                instruction = "Draw the tilted almond eye shape.",
                type = "curve",
                difficulty = "medium",
                estimatedTime = "20s",
                points = listOf(
                    NormalizedPoint(0.38f, 0.33f),
                    NormalizedPoint(0.42f, 0.30f),
                    NormalizedPoint(0.46f, 0.33f),
                    NormalizedPoint(0.42f, 0.35f),
                    NormalizedPoint(0.38f, 0.33f)
                ),
                hint = "Slanted feline eye"
            ),
            DrawingStep(
                step = 7,
                title = "7. Right Almond Eye",
                instruction = "Draw the matching right eye.",
                type = "curve",
                difficulty = "medium",
                estimatedTime = "20s",
                points = listOf(
                    NormalizedPoint(0.54f, 0.33f),
                    NormalizedPoint(0.58f, 0.30f),
                    NormalizedPoint(0.62f, 0.33f),
                    NormalizedPoint(0.58f, 0.35f),
                    NormalizedPoint(0.54f, 0.33f)
                ),
                hint = "Matching right feline eye"
            ),
            DrawingStep(
                step = 8,
                title = "8. Nose Triangle",
                instruction = "Place a small inverted triangle for the nose tip.",
                type = "polygon",
                difficulty = "easy",
                estimatedTime = "15s",
                points = listOf(
                    NormalizedPoint(0.48f, 0.38f),
                    NormalizedPoint(0.52f, 0.38f),
                    NormalizedPoint(0.50f, 0.40f),
                    NormalizedPoint(0.48f, 0.38f)
                ),
                hint = "Inverted nose triangle"
            ),
            DrawingStep(
                step = 9,
                title = "9. Muzzle & Whisker Pads",
                instruction = "Draw the double W-curve muzzle mouth line.",
                type = "curve",
                difficulty = "easy",
                estimatedTime = "20s",
                points = listOf(
                    NormalizedPoint(0.45f, 0.43f),
                    NormalizedPoint(0.50f, 0.41f),
                    NormalizedPoint(0.55f, 0.43f)
                ),
                hint = "Cute cat smile curve"
            ),
            DrawingStep(
                step = 10,
                title = "10. Fluffy Cheeks",
                instruction = "Add tufts of fur flaring outward from both cheeks.",
                type = "curve",
                difficulty = "easy",
                estimatedTime = "25s",
                points = listOf(
                    NormalizedPoint(0.32f, 0.35f),
                    NormalizedPoint(0.28f, 0.38f),
                    NormalizedPoint(0.33f, 0.41f),
                    NormalizedPoint(0.68f, 0.35f),
                    NormalizedPoint(0.72f, 0.38f),
                    NormalizedPoint(0.67f, 0.41f)
                ),
                hint = "Fluffy cheek fur tufts"
            ),
            DrawingStep(
                step = 11,
                title = "11. Front Paws",
                instruction = "Draw the two vertical front legs and rounded paw pads.",
                type = "line",
                difficulty = "medium",
                estimatedTime = "30s",
                points = listOf(
                    NormalizedPoint(0.44f, 0.55f),
                    NormalizedPoint(0.44f, 0.82f),
                    NormalizedPoint(0.47f, 0.84f),
                    NormalizedPoint(0.50f, 0.82f),
                    NormalizedPoint(0.53f, 0.84f),
                    NormalizedPoint(0.56f, 0.82f),
                    NormalizedPoint(0.56f, 0.55f)
                ),
                hint = "Neat parallel paws standing"
            ),
            DrawingStep(
                step = 12,
                title = "12. Hind Legs & Flanks",
                instruction = "Curve the curved outer haunches where the cat sits.",
                type = "curve",
                difficulty = "easy",
                estimatedTime = "25s",
                points = listOf(
                    NormalizedPoint(0.32f, 0.65f),
                    NormalizedPoint(0.28f, 0.78f),
                    NormalizedPoint(0.36f, 0.84f),
                    NormalizedPoint(0.68f, 0.65f),
                    NormalizedPoint(0.72f, 0.78f),
                    NormalizedPoint(0.64f, 0.84f)
                ),
                hint = "Rounded seated thighs"
            ),
            DrawingStep(
                step = 13,
                title = "13. Curled Tail",
                instruction = "Sweep an elegant S-curve tail wrapping comfortably around the paws.",
                type = "curve",
                difficulty = "medium",
                estimatedTime = "25s",
                points = listOf(
                    NormalizedPoint(0.66f, 0.80f),
                    NormalizedPoint(0.78f, 0.82f),
                    NormalizedPoint(0.80f, 0.74f),
                    NormalizedPoint(0.75f, 0.68f)
                ),
                hint = "Graceful curled tail"
            ),
            DrawingStep(
                step = 14,
                title = "14. Long Whisker Strands",
                instruction = "Flick three dynamic whiskers extending out from each cheek pad.",
                type = "line",
                difficulty = "easy",
                estimatedTime = "20s",
                points = listOf(
                    NormalizedPoint(0.42f, 0.40f),
                    NormalizedPoint(0.20f, 0.38f),
                    NormalizedPoint(0.42f, 0.42f),
                    NormalizedPoint(0.18f, 0.43f),
                    NormalizedPoint(0.58f, 0.40f),
                    NormalizedPoint(0.80f, 0.38f),
                    NormalizedPoint(0.58f, 0.42f),
                    NormalizedPoint(0.82f, 0.43f)
                ),
                hint = "Quick straight flick lines"
            ),
            DrawingStep(
                step = 15,
                title = "15. Slit Pupils & Ear Depth",
                instruction = "Add inner ear shadow folds and vertical slit pupils to finish your adorable cat!",
                type = "shading",
                difficulty = "easy",
                estimatedTime = "20s",
                points = listOf(
                    NormalizedPoint(0.42f, 0.32f),
                    NormalizedPoint(0.42f, 0.34f),
                    NormalizedPoint(0.58f, 0.32f),
                    NormalizedPoint(0.58f, 0.34f)
                ),
                hint = "Dark pupil accents"
            )
        )

        return TutorialTemplate(
            id = "tutorial_cat_15",
            title = "Cute Cat Silhouette",
            subtitle = "Step-by-step feline posture, perky ears, and whiskers",
            category = "Animals",
            difficulty = "Beginner",
            stepCount = steps.size,
            iconRes = "pets",
            steps = steps
        )
    }

    // 4. Still Life Apple & Shading Study (10 steps)
    private fun createStillLifeTutorial(): TutorialTemplate {
        val steps = listOf(
            DrawingStep(
                step = 1,
                title = "1. Basic Sphere",
                instruction = "Draw a loose circle for the overall form of the apple.",
                type = "circle",
                difficulty = "easy",
                estimatedTime = "20s",
                points = createCirclePoints(0.50f, 0.50f, 0.28f),
                hint = "Solid circular boundary"
            ),
            DrawingStep(
                step = 2,
                title = "2. Top & Bottom Dimples",
                instruction = "Indraw the gentle dips at the top stem basin and base calyx.",
                type = "curve",
                difficulty = "easy",
                estimatedTime = "20s",
                points = listOf(
                    NormalizedPoint(0.40f, 0.24f),
                    NormalizedPoint(0.50f, 0.27f),
                    NormalizedPoint(0.60f, 0.24f),
                    NormalizedPoint(0.44f, 0.77f),
                    NormalizedPoint(0.50f, 0.75f),
                    NormalizedPoint(0.56f, 0.77f)
                ),
                hint = "Organic indented dimples"
            ),
            DrawingStep(
                step = 3,
                title = "3. Curved Stem",
                instruction = "Draw the woody curved stem emerging from the upper dimple.",
                type = "curve",
                difficulty = "easy",
                estimatedTime = "20s",
                points = listOf(
                    NormalizedPoint(0.50f, 0.26f),
                    NormalizedPoint(0.52f, 0.18f),
                    NormalizedPoint(0.58f, 0.12f)
                ),
                hint = "Graceful curved branch"
            ),
            DrawingStep(
                step = 4,
                title = "4. Leaf Contour",
                instruction = "Attach an arched leaf pointing outward from the stem node.",
                type = "curve",
                difficulty = "medium",
                estimatedTime = "25s",
                points = listOf(
                    NormalizedPoint(0.53f, 0.18f),
                    NormalizedPoint(0.65f, 0.14f),
                    NormalizedPoint(0.74f, 0.19f),
                    NormalizedPoint(0.62f, 0.22f),
                    NormalizedPoint(0.53f, 0.18f)
                ),
                hint = "Pointed leaf shape"
            ),
            DrawingStep(
                step = 5,
                title = "5. Leaf Center Vein",
                instruction = "Draw the midrib vein running down the length of the leaf.",
                type = "line",
                difficulty = "easy",
                estimatedTime = "15s",
                points = createLinePoints(0.53f, 0.18f, 0.74f, 0.19f),
                hint = "Center vein line"
            ),
            DrawingStep(
                step = 6,
                title = "6. Specular Highlight Boundary",
                instruction = "Sketch the boundary where light hits brightest on the upper-left crest.",
                type = "ellipse",
                difficulty = "easy",
                estimatedTime = "15s",
                points = createCirclePoints(0.38f, 0.38f, 0.06f),
                hint = "Crescent highlight zone"
            ),
            DrawingStep(
                step = 7,
                title = "7. Terminator (Core Shadow Line)",
                instruction = "Map the crescent line separating illuminated side from shaded side.",
                type = "curve",
                difficulty = "medium",
                estimatedTime = "30s",
                points = listOf(
                    NormalizedPoint(0.48f, 0.28f),
                    NormalizedPoint(0.58f, 0.44f),
                    NormalizedPoint(0.64f, 0.60f),
                    NormalizedPoint(0.54f, 0.76f)
                ),
                hint = "Curved shadow divide"
            ),
            DrawingStep(
                step = 8,
                title = "8. Core Shadow Hatching",
                instruction = "Hatch diagonal lines following the spherical curvature into the shaded half.",
                type = "shading",
                difficulty = "medium",
                estimatedTime = "35s",
                points = listOf(
                    NormalizedPoint(0.58f, 0.40f),
                    NormalizedPoint(0.70f, 0.50f),
                    NormalizedPoint(0.60f, 0.55f),
                    NormalizedPoint(0.72f, 0.65f)
                ),
                hint = "Form-following contour hatching"
            ),
            DrawingStep(
                step = 9,
                title = "9. Table Surface & Cast Shadow",
                instruction = "Draw the ground table horizon and the squashed oval cast shadow below.",
                type = "ellipse",
                difficulty = "easy",
                estimatedTime = "25s",
                points = listOf(
                    NormalizedPoint(0.15f, 0.68f),
                    NormalizedPoint(0.85f, 0.68f),
                    NormalizedPoint(0.40f, 0.76f),
                    NormalizedPoint(0.55f, 0.82f),
                    NormalizedPoint(0.78f, 0.79f),
                    NormalizedPoint(0.65f, 0.75f)
                ),
                hint = "Table line and shadow ellipse"
            ),
            DrawingStep(
                step = 10,
                title = "10. Occlusion Contact Shadow",
                instruction = "Darken the intense contact shadow directly where the fruit touches the surface.",
                type = "shading",
                difficulty = "easy",
                estimatedTime = "20s",
                points = listOf(
                    NormalizedPoint(0.44f, 0.77f),
                    NormalizedPoint(0.52f, 0.77f),
                    NormalizedPoint(0.58f, 0.77f)
                ),
                hint = "Deep contact dark accent"
            )
        )

        return TutorialTemplate(
            id = "tutorial_still_life_10",
            title = "Still Life Form & Shading",
            subtitle = "Master 3D volume, spherical shading, and cast shadow",
            category = "Fundamentals",
            difficulty = "Beginner",
            stepCount = steps.size,
            iconRes = "palette",
            steps = steps
        )
    }
}
