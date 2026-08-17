package com.aiham.dailycompanion

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.max
import kotlin.math.roundToInt

sealed interface PersonSource {
    val cacheKey: String

    data class Resource(@DrawableRes val resId: Int) : PersonSource {
        override val cacheKey: String = "res:$resId"
    }

    data class ContentUri(val uri: String) : PersonSource {
        override val cacheKey: String = "uri:$uri"
    }
}

private object PersonBitmapCache {
    val cache = ConcurrentHashMap<String, Bitmap>()
}

@Composable
fun PersonCutout(
    source: PersonSource,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    val context = LocalContext.current
    var result by remember(source.cacheKey) { mutableStateOf(PersonBitmapCache.cache[source.cacheKey]) }
    var processing by remember(source.cacheKey) { mutableStateOf(result == null) }

    val segmenter = remember(source.cacheKey) {
        val options = SelfieSegmenterOptions.Builder()
            .setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE)
            .build()
        Segmentation.getClient(options)
    }

    DisposableEffect(segmenter) {
        onDispose { segmenter.close() }
    }

    LaunchedEffect(source.cacheKey) {
        if (result != null) {
            processing = false
            return@LaunchedEffect
        }

        val inputBitmap = loadBitmap(context, source)
        if (inputBitmap == null) {
            processing = false
            return@LaunchedEffect
        }

        val prepared = scaleBitmap(inputBitmap, 640)
        val image = InputImage.fromBitmap(prepared, 0)
        segmenter.process(image)
            .addOnSuccessListener { mask ->
                runCatching {
                    val cutout = applyMask(prepared, mask.buffer, mask.width, mask.height)
                    PersonBitmapCache.cache[source.cacheKey] = cutout
                    result = cutout
                }
                processing = false
            }
            .addOnFailureListener {
                processing = false
            }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        val bitmap = result
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "صورة شخصية بدون خلفية",
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale
            )
        } else if (processing) {
            CircularProgressIndicator(strokeWidth = 2.dp)
        }
    }
}

private fun loadBitmap(context: Context, source: PersonSource): Bitmap? = runCatching {
    when (source) {
        is PersonSource.Resource -> BitmapFactory.decodeResource(context.resources, source.resId)
        is PersonSource.ContentUri -> {
            val uri = Uri.parse(source.uri)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val imageSource = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(imageSource) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = false
                }
            } else {
                context.contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream)
            }
        }
    }
}.getOrNull()

private fun scaleBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
    val largest = max(bitmap.width, bitmap.height)
    if (largest <= maxDimension) return bitmap.copy(Bitmap.Config.ARGB_8888, false)

    val ratio = maxDimension.toFloat() / largest.toFloat()
    val width = (bitmap.width * ratio).roundToInt().coerceAtLeast(1)
    val height = (bitmap.height * ratio).roundToInt().coerceAtLeast(1)
    return Bitmap.createScaledBitmap(bitmap, width, height, true).copy(Bitmap.Config.ARGB_8888, false)
}

private fun applyMask(
    source: Bitmap,
    byteBuffer: java.nio.ByteBuffer,
    maskWidth: Int,
    maskHeight: Int
): Bitmap {
    val maskValues = FloatArray(maskWidth * maskHeight)
    byteBuffer.rewind()
    var m = 0
    while (m < maskValues.size && byteBuffer.remaining() >= 4) {
        maskValues[m++] = byteBuffer.float
    }

    val width = source.width
    val height = source.height
    val pixels = IntArray(width * height)
    source.getPixels(pixels, 0, width, 0, 0, width, height)

    for (y in 0 until height) {
        val my = ((y.toFloat() / height) * maskHeight).toInt().coerceIn(0, maskHeight - 1)
        for (x in 0 until width) {
            val mx = ((x.toFloat() / width) * maskWidth).toInt().coerceIn(0, maskWidth - 1)
            val confidence = maskValues[my * maskWidth + mx]
            val softAlpha = ((confidence - 0.10f) / 0.72f).coerceIn(0f, 1f)
            val index = y * width + x
            val color = pixels[index]
            val originalAlpha = (color ushr 24) and 0xFF
            val alpha = (originalAlpha * softAlpha).roundToInt().coerceIn(0, 255)
            pixels[index] = (alpha shl 24) or (color and 0x00FFFFFF)
        }
    }

    return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
}
