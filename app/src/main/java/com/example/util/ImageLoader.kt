package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.math.max

object ImageLoader {

    private const val TAG = "LineSketchAI"
    private const val MAX_DIMENSION = 1024

    /**
     * Safely decodes a downsampled Bitmap from an Android content URI without loading full resolution into RAM.
     * Prevents OutOfMemoryError on high-resolution camera images.
     */
    suspend fun decodeSampledBitmapFromUri(
        context: Context,
        uri: Uri,
        maxDim: Int = MAX_DIMENSION
    ): Result<Bitmap> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "IMAGE_URI_RECEIVED: $uri")
            Log.d(TAG, "BITMAP_DECODE_START: Reading image bounds for uri")

            // Step 1: Read image dimensions only with inJustDecodeBounds
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

            var inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                return@withContext Result.failure(IllegalArgumentException("Unable to open input stream for image"))
            }

            inputStream.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }

            val origWidth = options.outWidth
            val origHeight = options.outHeight
            Log.d(TAG, "IMAGE_SIZE: Original dimensions = ${origWidth}x${origHeight}")

            if (origWidth <= 0 || origHeight <= 0) {
                return@withContext Result.failure(IllegalArgumentException("Unable to decode image dimensions. File may be corrupted or unsupported."))
            }

            // Step 2: Calculate inSampleSize to downsample during decode
            options.inSampleSize = calculateInSampleSize(options, maxDim, maxDim)
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.ARGB_8888

            // Step 3: Decode the safely downsampled Bitmap
            inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                return@withContext Result.failure(IllegalArgumentException("Unable to open input stream for second pass"))
            }

            val sampledBitmap = inputStream.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            } ?: return@withContext Result.failure(IllegalArgumentException("Failed to decode bitmap from image data"))

            Log.d(TAG, "BITMAP_DECODE_SUCCESS: Sampled dimensions = ${sampledBitmap.width}x${sampledBitmap.height} with inSampleSize=${options.inSampleSize}")

            // Step 4: If still larger than maxDim, scale down to strictly fit within maxDim
            val finalBitmap = if (sampledBitmap.width > maxDim || sampledBitmap.height > maxDim) {
                val scale = maxDim.toFloat() / max(sampledBitmap.width, sampledBitmap.height)
                val targetW = (sampledBitmap.width * scale).toInt().coerceAtLeast(1)
                val targetH = (sampledBitmap.height * scale).toInt().coerceAtLeast(1)
                val scaled = Bitmap.createScaledBitmap(sampledBitmap, targetW, targetH, true)
                if (scaled != sampledBitmap) {
                    sampledBitmap.recycle()
                }
                scaled
            } else {
                sampledBitmap
            }

            Log.d(TAG, "BITMAP_FINAL: Final resolution = ${finalBitmap.width}x${finalBitmap.height}")
            Result.success(finalBitmap)
        } catch (oom: OutOfMemoryError) {
            Log.e(TAG, "ANALYSIS_ERROR: OutOfMemoryError while decoding image", oom)
            System.gc()
            Result.failure(Exception("Image is too large for device memory. Please choose another image or take a standard photo."))
        } catch (e: Exception) {
            Log.e(TAG, "ANALYSIS_ERROR: Exception decoding image: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Safely scales any existing in-memory bitmap (e.g. from camera thumbnail preview) within maxDim.
     */
    fun scaleBitmapWithinBounds(bitmap: Bitmap, maxDim: Int = MAX_DIMENSION): Bitmap {
        if (bitmap.width <= maxDim && bitmap.height <= maxDim) {
            return bitmap
        }
        val scale = maxDim.toFloat() / max(bitmap.width, bitmap.height)
        val targetW = (bitmap.width * scale).toInt().coerceAtLeast(1)
        val targetH = (bitmap.height * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
    }

    /**
     * Compresses bitmap to JPEG byte array.
     */
    fun compressToJpeg(bitmap: Bitmap, quality: Int = 85): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        val bytes = stream.toByteArray()
        Log.d(TAG, "IMAGE_COMPRESSED: Compressed size = ${bytes.size} bytes (${bytes.size / 1024} KB)")
        return bytes
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize.coerceAtLeast(1)
    }
}
