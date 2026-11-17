package com.arrazyfathan.lerun.feature.settings.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.arrazyfathan.lerun.ui.designsystem.Inter
import com.arrazyfathan.lerun.ui.designsystem.LerunGreen
import com.arrazyfathan.lerun.ui.designsystem.LerunMainDark
import com.arrazyfathan.lerun.utils.BitmapConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private const val MAX_DECODE_DIMENSION = 1600
private const val OUTPUT_AVATAR_SIZE = 512
private const val MIN_ZOOM = 1f
private const val MAX_ZOOM = 5f

@Composable
fun ImageCropperDialog(
    imageUri: Uri,
    onCropSuccess: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var sourceBitmap by remember(imageUri) { mutableStateOf<Bitmap?>(null) }
    var isDecoding by remember(imageUri) { mutableStateOf(true) }
    var isCropping by remember { mutableStateOf(false) }

    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var scale by remember(imageUri) { mutableFloatStateOf(1f) }
    var offset by remember(imageUri) { mutableStateOf(Offset.Zero) }

    LaunchedEffect(imageUri) {
        isDecoding = true
        val decoded =
            withContext(Dispatchers.IO) {
                decodeCorrectedBitmap(context, imageUri)
            }
        sourceBitmap = decoded
        isDecoding = false
        if (decoded == null) {
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = { if (!isCropping) onDismiss() },
        properties =
            DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(LerunMainDark)
                    .statusBarsPadding()
                    .navigationBarsPadding(),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Crop Profile Image",
                    fontFamily = Inter,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = Color.White,
                )

                TextButton(
                    onClick = {
                        scale = 1f
                        offset = Offset.Zero
                    },
                    enabled = !isDecoding && !isCropping,
                ) {
                    Text(
                        text = "Reset",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = LerunGreen,
                    )
                }
            }

            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .onSizeChanged { canvasSize = it },
                contentAlignment = Alignment.Center,
            ) {
                val bitmap = sourceBitmap
                if (isDecoding || bitmap == null) {
                    CircularProgressIndicator(color = LerunGreen)
                } else {
                    val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }
                    val cropBoxSide =
                        remember(canvasSize) {
                            min(canvasSize.width, canvasSize.height) * 0.82f
                        }
                    val baseScale =
                        remember(bitmap, cropBoxSide) {
                            if (bitmap.width > 0 && bitmap.height > 0 && cropBoxSide > 0f) {
                                max(
                                    cropBoxSide / bitmap.width.toFloat(),
                                    cropBoxSide / bitmap.height.toFloat(),
                                )
                            } else {
                                1f
                            }
                        }

                    fun clampOffset(
                        candidateOffset: Offset,
                        candidateScale: Float,
                    ): Offset {
                        val effectiveScale = baseScale * candidateScale
                        val scaledWidth = bitmap.width * effectiveScale
                        val scaledHeight = bitmap.height * effectiveScale
                        val maxOffsetX = max(0f, (scaledWidth - cropBoxSide) / 2f)
                        val maxOffsetY = max(0f, (scaledHeight - cropBoxSide) / 2f)
                        return Offset(
                            x = candidateOffset.x.coerceIn(-maxOffsetX, maxOffsetX),
                            y = candidateOffset.y.coerceIn(-maxOffsetY, maxOffsetY),
                        )
                    }

                    Canvas(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .pointerInput(bitmap, cropBoxSide) {
                                    detectTransformGestures { _, pan, zoom, _ ->
                                        val newScale = (scale * zoom).coerceIn(MIN_ZOOM, MAX_ZOOM)
                                        val newOffset = clampOffset(offset + pan, newScale)
                                        scale = newScale
                                        offset = newOffset
                                    }
                                },
                    ) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val effectiveScale = baseScale * scale
                        val drawWidth = bitmap.width * effectiveScale
                        val drawHeight = bitmap.height * effectiveScale
                        val topLeft =
                            Offset(
                                x = center.x - drawWidth / 2f + offset.x,
                                y = center.y - drawHeight / 2f + offset.y,
                            )

                        withTransform({
                            translate(left = topLeft.x, top = topLeft.y)
                            scale(
                                scaleX = effectiveScale,
                                scaleY = effectiveScale,
                                pivot = Offset.Zero,
                            )
                        }) {
                            drawImage(image = imageBitmap)
                        }

                        val cropRect =
                            Rect(
                                left = center.x - cropBoxSide / 2f,
                                top = center.y - cropBoxSide / 2f,
                                right = center.x + cropBoxSide / 2f,
                                bottom = center.y + cropBoxSide / 2f,
                            )

                        val cropPath =
                            Path().apply {
                                addRect(cropRect)
                            }

                        // Dimmed overlay outside the crop square
                        clipPath(path = cropPath, clipOp = ClipOp.Difference) {
                            drawRect(color = Color.Black.copy(alpha = 0.68f))
                        }

                        // Rule-of-thirds guidelines inside crop square
                        val third = cropBoxSide / 3f
                        val gridColor = Color.White.copy(alpha = 0.45f)
                        for (i in 1..2) {
                            val x = cropRect.left + third * i
                            drawLine(
                                color = gridColor,
                                start = Offset(x, cropRect.top),
                                end = Offset(x, cropRect.bottom),
                                strokeWidth = 1.dp.toPx(),
                            )
                            val y = cropRect.top + third * i
                            drawLine(
                                color = gridColor,
                                start = Offset(cropRect.left, y),
                                end = Offset(cropRect.right, y),
                                strokeWidth = 1.dp.toPx(),
                            )
                        }

                        // Outer crop frame border
                        drawRect(
                            color = LerunGreen,
                            topLeft = cropRect.topLeft,
                            size = Size(cropBoxSide, cropBoxSide),
                            style = Stroke(width = 2.dp.toPx()),
                        )
                    }
                }
            }

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    enabled = !isCropping,
                    shape = RoundedCornerShape(10.dp),
                    modifier =
                        Modifier
                            .weight(1f)
                            .height(50.dp),
                ) {
                    Text(
                        text = "Cancel",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        color = Color.White,
                    )
                }

                Button(
                    onClick = {
                        val bitmap = sourceBitmap ?: return@Button
                        val cropBoxSide = min(canvasSize.width, canvasSize.height) * 0.82f
                        if (cropBoxSide <= 0f) return@Button
                        isCropping = true
                        coroutineScope.launch {
                            val base64 =
                                withContext(Dispatchers.Default) {
                                    val cropped =
                                        cropBitmap(
                                            bitmap = bitmap,
                                            cropBoxSide = cropBoxSide,
                                            userScale = scale,
                                            userOffset = offset,
                                        )
                                    BitmapConverter.converterBitmapToString(cropped)
                                }
                            isCropping = false
                            onCropSuccess(base64)
                        }
                    },
                    enabled = !isDecoding && !isCropping && sourceBitmap != null,
                    shape = RoundedCornerShape(10.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = LerunGreen,
                            contentColor = LerunMainDark,
                        ),
                    modifier =
                        Modifier
                            .weight(1f)
                            .height(50.dp),
                ) {
                    Text(
                        text = if (isCropping) "Saving..." else "Crop & Save",
                        fontFamily = Inter,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = LerunMainDark,
                    )
                }
            }
        }
    }
}

private fun cropBitmap(
    bitmap: Bitmap,
    cropBoxSide: Float,
    userScale: Float,
    userOffset: Offset,
): Bitmap {
    val baseScale =
        max(
            cropBoxSide / bitmap.width.toFloat(),
            cropBoxSide / bitmap.height.toFloat(),
        )
    val effectiveScale = baseScale * userScale
    val drawWidth = bitmap.width * effectiveScale
    val drawHeight = bitmap.height * effectiveScale

    // Crop box top-left relative to the scaled image top-left
    val relativeLeft = (drawWidth - cropBoxSide) / 2f - userOffset.x
    val relativeTop = (drawHeight - cropBoxSide) / 2f - userOffset.y

    val srcX =
        (relativeLeft / effectiveScale)
            .roundToInt()
            .coerceIn(0, (bitmap.width - 1).coerceAtLeast(0))
    val srcY =
        (relativeTop / effectiveScale)
            .roundToInt()
            .coerceIn(0, (bitmap.height - 1).coerceAtLeast(0))
    val maxSide = min(bitmap.width - srcX, bitmap.height - srcY).coerceAtLeast(1)
    val srcSide =
        (cropBoxSide / effectiveScale)
            .roundToInt()
            .coerceIn(1, maxSide)

    val cropped = Bitmap.createBitmap(bitmap, srcX, srcY, srcSide, srcSide)
    return if (cropped.width > OUTPUT_AVATAR_SIZE) {
        Bitmap.createScaledBitmap(cropped, OUTPUT_AVATAR_SIZE, OUTPUT_AVATAR_SIZE, true)
    } else {
        cropped
    }
}

private fun decodeCorrectedBitmap(
    context: Context,
    uri: Uri,
): Bitmap? =
    runCatching {
        val boundsOptions =
            BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, boundsOptions)
        }

        var sampleSize = 1
        while (
            boundsOptions.outWidth / sampleSize > MAX_DECODE_DIMENSION ||
            boundsOptions.outHeight / sampleSize > MAX_DECODE_DIMENSION
        ) {
            sampleSize *= 2
        }

        val decodeOptions =
            BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }
        val rawBitmap =
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: return null

        val rotationDegrees =
            context.contentResolver.openInputStream(uri)?.use { stream ->
                when (
                    ExifInterface(stream).getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL,
                    )
                ) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            } ?: 0f

        if (rotationDegrees != 0f) {
            val matrix = Matrix().apply { postRotate(rotationDegrees) }
            Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true)
        } else {
            rawBitmap
        }
    }.getOrNull()
