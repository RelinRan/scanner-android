package android.scanner.api

import android.graphics.Bitmap
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BarcodeBitmapCodecTest {

    @Test
    fun `qr content converts to bitmap and back`() {
        val encoded = BarcodeBitmapCodec.encodeQr("scanner-value", BitmapSize(256, 256))
        assertTrue(encoded is ScanOutcome.Success)

        val bitmap = (encoded as ScanOutcome.Success<Bitmap>).value
        val decoded = BarcodeBitmapCodec.decode(bitmap)

        assertEquals("scanner-value", (decoded as ScanOutcome.Success<List<ScanResult>>).value.single().rawValue)
    }

    @Test
    fun `bitmap size is configurable`() {
        val encoded = BarcodeBitmapCodec.encodeQr("value", BitmapSize(320, 180))

        val bitmap = (encoded as ScanOutcome.Success<Bitmap>).value
        assertEquals(320, bitmap.width)
        assertEquals(180, bitmap.height)
    }

    @Test
    fun `blank content returns invalid input`() {
        val result = BarcodeBitmapCodec.encodeQr(" ", BitmapSize(200, 200))

        assertTrue(result is ScanOutcome.Failure)
        assertTrue((result as ScanOutcome.Failure).error is ScannerError.InvalidInput)
    }
}
