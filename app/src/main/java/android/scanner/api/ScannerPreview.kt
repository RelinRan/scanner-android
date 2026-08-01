package android.scanner.api

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

public data class ScannerPreviewConfig(
    val region: ScanRegion = ScanRegion.Centered,
    val outsideColor: Color = Color.Black.copy(alpha = 0.55f),
    val insideColor: Color = Color.Transparent,
    val borderColor: Color = Color.White,
    val borderWidth: Dp = 2.dp,
)

@Composable
public fun ScannerPreview(
    bitmap: Bitmap?,
    modifier: Modifier = Modifier,
    config: ScannerPreviewConfig = ScannerPreviewConfig(),
) {
    Box(modifier = modifier) {
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Canvas(Modifier.fillMaxSize()) {
            val left = size.width * config.region.left
            val top = size.height * config.region.top
            val right = size.width * config.region.right
            val bottom = size.height * config.region.bottom
            drawRect(config.outsideColor)
            drawRect(config.insideColor, topLeft = androidx.compose.ui.geometry.Offset(left, top), size = androidx.compose.ui.geometry.Size(right - left, bottom - top))
            drawRect(config.borderColor, topLeft = androidx.compose.ui.geometry.Offset(left, top), size = androidx.compose.ui.geometry.Size(right - left, bottom - top), style = Stroke(config.borderWidth.toPx()))
        }
    }
}
