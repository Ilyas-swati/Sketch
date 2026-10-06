package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.model.NormalizedPoint
import com.example.model.UserStroke
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object ExportHelper {

    /**
     * Renders the user's vector strokes onto a high-resolution Bitmap.
     */
    fun renderStrokesToBitmap(
        strokes: List<UserStroke>,
        width: Int = 1440,
        height: Int = 1440,
        backgroundColor: Int = 0xFF10111A.toInt()
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(backgroundColor)

        val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        for (stroke in strokes) {
            val pts = stroke.points
            if (pts.isEmpty()) continue

            if (stroke.isEraser) {
                paint.color = backgroundColor
                paint.strokeWidth = stroke.strokeWidth * 3.5f
            } else {
                paint.color = stroke.colorArgb.toInt()
                paint.strokeWidth = stroke.strokeWidth * 2.2f
            }

            if (pts.size == 1) {
                canvas.drawCircle(
                    pts.first().x * width,
                    pts.first().y * height,
                    paint.strokeWidth / 2f,
                    paint
                )
                continue
            }

            val path = Path()
            path.moveTo(pts.first().x * width, pts.first().y * height)
            for (i in 1 until pts.size) {
                val curr = pts[i]
                val prev = pts[i - 1]
                val midX = (prev.x + curr.x) / 2f * width
                val midY = (prev.y + curr.y) / 2f * height
                path.quadTo(prev.x * width, prev.y * height, midX, midY)
            }
            path.lineTo(pts.last().x * width, pts.last().y * height)

            canvas.drawPath(path, paint)
        }

        return bitmap
    }

    /**
     * Saves the rendered sketch directly into the device's Pictures directory or App storage.
     */
    fun saveBitmapToGallery(context: Context, bitmap: Bitmap, title: String): Uri? {
        val filename = "LineSketch_${System.currentTimeMillis()}.png"
        var uri: Uri? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/LineSketch")
                }
                uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    val stream: OutputStream? = context.contentResolver.openOutputStream(uri)
                    stream?.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                }
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val appDir = File(imagesDir, "LineSketch").apply { mkdirs() }
                val imageFile = File(appDir, filename)
                FileOutputStream(imageFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                uri = Uri.fromFile(imageFile)
            }
            Toast.makeText(context, "Sketch saved to Pictures/LineSketch!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to save: ${e.message}", Toast.LENGTH_SHORT).show()
        }

        return uri
    }

    /**
     * Shares the sketch via standard Android Share Sheet Intent.
     */
    fun shareSketch(context: Context, bitmap: Bitmap) {
        try {
            val cachePath = File(context.cacheDir, "images").apply { mkdirs() }
            val file = File(cachePath, "share_sketch_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_TEXT, "Look at my sketch made with LineSketch AI!")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Share your LineSketch"))
        } catch (e: Exception) {
            // Fallback text share if file provider fails
            val textIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "I created a sketch with LineSketch AI!")
            }
            context.startActivity(Intent.createChooser(textIntent, "Share LineSketch"))
        }
    }
}
