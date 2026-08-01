package android.scanner.api

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat as ZxingFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.MultiFormatWriter
import com.google.zxing.ReaderException
import com.google.zxing.common.BitMatrix
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

public data class BitmapSize(val width: Int, val height: Int) {
    init {
        require(width > 0 && height > 0) { "Bitmap dimensions must be positive" }
    }
}

public object BarcodeCodec {
    private const val TAG = "BarcodeCodec"
    public fun encodeQr(
        content: String,
        size: BitmapSize = BitmapSize(512, 512),
    ): ScanOutcome<Bitmap> {
        ScannerDebug.log(TAG, "encodeQr start contentLength=${content.length}, size=${size.width}x${size.height}")
        if (content.isBlank()) {
            ScannerDebug.log(TAG, "encodeQr rejected blank content")
            return ScanOutcome.Failure(ScannerError.InvalidInput("Content cannot be blank"))
        }
        return try {
            val matrix = MultiFormatWriter().encode(
                content,
                ZxingFormat.QR_CODE,
                size.width,
                size.height,
                mapOf(
                    EncodeHintType.CHARACTER_SET to "UTF-8",
                    EncodeHintType.MARGIN to 1,
                    EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
                ),
            )
            val bitmap = matrix.toBitmap()
            ScannerDebug.log(TAG, "encodeQr success bitmap=${bitmap.width}x${bitmap.height}")
            ScanOutcome.Success(bitmap)
        } catch (error: Exception) {
            ScannerDebug.error(TAG, "encodeQr failed", error)
            ScanOutcome.Failure(ScannerError.EncodingFailed(error))
        }
    }

    public fun decode(bitmap: Bitmap): ScanOutcome<List<ScanResult>> {
        ScannerDebug.log(TAG, "decode start bitmap=${bitmap.width}x${bitmap.height}")
        if (bitmap.width <= 0 || bitmap.height <= 0) {
            return ScanOutcome.Failure(ScannerError.InvalidInput("Bitmap dimensions must be positive"))
        }
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        val source = RGBLuminanceSource(bitmap.width, bitmap.height, pixels)
        return try {
            val result = MultiFormatReader().decode(BinaryBitmap(HybridBinarizer(source)))
            ScannerDebug.log(TAG, "decode success format=${result.barcodeFormat}, textLength=${result.text?.length ?: 0}")
            ScanOutcome.Success(
                listOf(
                    ScanResult(
                        rawValue = result.text,
                        displayValue = result.text,
                        rawBytes = result.rawBytes,
                        format = result.barcodeFormat.toPublicFormat(),
                    ),
                ),
            )
        } catch (error: ReaderException) {
            ScannerDebug.error(TAG, "decode failed: no barcode found", error)
            ScanOutcome.Failure(ScannerError.RecognitionFailed(error))
        }
    }

    private fun BitMatrix.toBitmap(): Bitmap {
        val pixels = IntArray(width * height) { index ->
            if (get(index % width, index / width)) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    }

    private fun ZxingFormat.toPublicFormat(): BarcodeFormat = when (this) {
        ZxingFormat.AZTEC -> BarcodeFormat.Aztec
        ZxingFormat.CODABAR -> BarcodeFormat.Codabar
        ZxingFormat.CODE_39 -> BarcodeFormat.Code39
        ZxingFormat.CODE_93 -> BarcodeFormat.Code93
        ZxingFormat.CODE_128 -> BarcodeFormat.Code128
        ZxingFormat.DATA_MATRIX -> BarcodeFormat.DataMatrix
        ZxingFormat.EAN_8 -> BarcodeFormat.Ean8
        ZxingFormat.EAN_13 -> BarcodeFormat.Ean13
        ZxingFormat.ITF -> BarcodeFormat.Itf
        ZxingFormat.PDF_417 -> BarcodeFormat.Pdf417
        ZxingFormat.QR_CODE -> BarcodeFormat.QrCode
        ZxingFormat.UPC_A -> BarcodeFormat.UpcA
        ZxingFormat.UPC_E -> BarcodeFormat.UpcE
        else -> BarcodeFormat.QrCode
    }
}
