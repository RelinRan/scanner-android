package android.scanner.api

import android.graphics.Color
import android.graphics.Bitmap
import android.scanner.api.BarcodeFormat
import android.scanner.api.ScanOutcome
import android.scanner.api.ScannerError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BarcodeEncoderTest {

    @Test
    fun `blank content returns invalid input`() {
        val outcome = BarcodeEncoder.qrCode("   ")

        assertTrue(outcome is ScanOutcome.Failure)
        assertTrue((outcome as ScanOutcome.Failure).error is ScannerError.InvalidInput)
    }

    @Test
    fun `non-positive dimensions return invalid input`() {
        val outcome = BarcodeEncoder.encode(
            content = "value",
            options = BarcodeEncoderOptions(format = BarcodeFormat.QrCode, width = 0, height = 100),
        )

        assertTrue(outcome is ScanOutcome.Failure)
        assertTrue((outcome as ScanOutcome.Failure).error is ScannerError.InvalidInput)
    }

    @Test
    fun `unsupported format returns invalid input`() {
        val outcome = BarcodeEncoder.encode(
            content = "value",
            options = BarcodeEncoderOptions(format = BarcodeFormat.UpcE, width = 200, height = 100),
        )

        assertTrue(outcome is ScanOutcome.Failure)
    }

    @Test
    fun `qr code uses requested dimensions and colors`() {
        val outcome = BarcodeEncoder.encode(
            content = "https://example.com",
            options = BarcodeEncoderOptions(
                format = BarcodeFormat.QrCode,
                width = 240,
                height = 240,
                foregroundColor = Color.RED,
                backgroundColor = Color.YELLOW,
            ),
        ) as ScanOutcome.Success<Bitmap>

        assertEquals(240, outcome.value.width)
        assertEquals(240, outcome.value.height)
        val colors = IntArray(240 * 240)
        outcome.value.getPixels(colors, 0, 240, 0, 0, 240, 240)
        assertTrue(colors.contains(Color.RED))
        assertTrue(colors.contains(Color.YELLOW))
    }

    @Test
    fun `code 128 uses requested dimensions`() {
        val outcome = BarcodeEncoder.encode(
            content = "1234567890",
            options = BarcodeEncoderOptions(
                format = BarcodeFormat.Code128,
                width = 320,
                height = 120,
            ),
        ) as ScanOutcome.Success<Bitmap>

        assertEquals(320, outcome.value.width)
        assertEquals(120, outcome.value.height)
    }
}
