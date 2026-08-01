package android.scanner.api

import android.graphics.Bitmap
import android.scanner.api.BarcodeFormat
import android.scanner.api.ScanOutcome
import android.scanner.api.ScannerError
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

public object BarcodeEncoder {

    public fun qrCode(content: String, size: Int = 512): ScanOutcome<Bitmap> = encode(
        content = content,
        options = BarcodeEncoderOptions(
            format = BarcodeFormat.QrCode,
            width = size,
            height = size,
        ),
    )

    public fun encode(
        content: String,
        options: BarcodeEncoderOptions,
    ): ScanOutcome<Bitmap> {
        validate(content, options)?.let { return ScanOutcome.Failure(it) }
        val zxingFormat = options.format.toZxingFormat()
            ?: return ScanOutcome.Failure(
                ScannerError.InvalidInput("Encoding ${options.format} is not supported"),
            )

        val hints = mapOf<EncodeHintType, Any>(
            EncodeHintType.CHARACTER_SET to options.characterSet,
            EncodeHintType.MARGIN to options.margin,
            EncodeHintType.ERROR_CORRECTION to options.qrErrorCorrection.toZxingLevel(),
        )

        return try {
            val matrix = MultiFormatWriter().encode(
                content,
                zxingFormat,
                options.width,
                options.height,
                hints,
            )
            ScanOutcome.Success(matrix.toBitmap(options.foregroundColor, options.backgroundColor))
        } catch (error: WriterException) {
            ScanOutcome.Failure(ScannerError.EncodingFailed(error))
        } catch (error: IllegalArgumentException) {
            ScanOutcome.Failure(ScannerError.InvalidInput(error.message ?: "Invalid encoding input"))
        }
    }

    private fun validate(content: String, options: BarcodeEncoderOptions): ScannerError.InvalidInput? = when {
        content.isBlank() -> ScannerError.InvalidInput("Content cannot be blank")
        options.width <= 0 || options.height <= 0 ->
            ScannerError.InvalidInput("Width and height must be positive")
        options.margin < 0 -> ScannerError.InvalidInput("Margin cannot be negative")
        options.characterSet.isBlank() -> ScannerError.InvalidInput("Character set cannot be blank")
        else -> null
    }

    private fun BitMatrix.toBitmap(foreground: Int, background: Int): Bitmap {
        val pixels = IntArray(width * height) { index ->
            val x = index % width
            val y = index / width
            if (get(x, y)) foreground else background
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    }

    private fun BarcodeFormat.toZxingFormat(): com.google.zxing.BarcodeFormat? = when (this) {
        BarcodeFormat.Aztec -> com.google.zxing.BarcodeFormat.AZTEC
        BarcodeFormat.Codabar -> com.google.zxing.BarcodeFormat.CODABAR
        BarcodeFormat.Code39 -> com.google.zxing.BarcodeFormat.CODE_39
        BarcodeFormat.Code93 -> com.google.zxing.BarcodeFormat.CODE_93
        BarcodeFormat.Code128 -> com.google.zxing.BarcodeFormat.CODE_128
        BarcodeFormat.DataMatrix -> com.google.zxing.BarcodeFormat.DATA_MATRIX
        BarcodeFormat.Ean8 -> com.google.zxing.BarcodeFormat.EAN_8
        BarcodeFormat.Ean13 -> com.google.zxing.BarcodeFormat.EAN_13
        BarcodeFormat.Itf -> com.google.zxing.BarcodeFormat.ITF
        BarcodeFormat.Pdf417 -> com.google.zxing.BarcodeFormat.PDF_417
        BarcodeFormat.QrCode -> com.google.zxing.BarcodeFormat.QR_CODE
        BarcodeFormat.UpcA -> com.google.zxing.BarcodeFormat.UPC_A
        BarcodeFormat.UpcE -> null
    }

    private fun QrErrorCorrection.toZxingLevel(): ErrorCorrectionLevel = when (this) {
        QrErrorCorrection.Low -> ErrorCorrectionLevel.L
        QrErrorCorrection.Medium -> ErrorCorrectionLevel.M
        QrErrorCorrection.Quartile -> ErrorCorrectionLevel.Q
        QrErrorCorrection.High -> ErrorCorrectionLevel.H
    }
}
