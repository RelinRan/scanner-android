package android.scanner.api

import android.graphics.Color
import android.scanner.api.BarcodeFormat

public enum class QrErrorCorrection {
    Low,
    Medium,
    Quartile,
    High,
}

public data class BarcodeEncoderOptions(
    val format: BarcodeFormat,
    val width: Int,
    val height: Int,
    val margin: Int = 1,
    val foregroundColor: Int = Color.BLACK,
    val backgroundColor: Int = Color.WHITE,
    val characterSet: String = "UTF-8",
    val qrErrorCorrection: QrErrorCorrection = QrErrorCorrection.High,
)
