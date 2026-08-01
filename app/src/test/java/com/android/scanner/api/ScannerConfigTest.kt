package android.scanner.api

import kotlin.time.Duration.Companion.milliseconds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScannerConfigTest {

    @Test
    fun `defaults use single scanning and all formats`() {
        val config = ScannerConfig()

        assertEquals(ScanMode.Single, config.scanMode)
        assertEquals(1_500.milliseconds, config.duplicateWindow)
        assertEquals(BarcodeFormat.entries.toSet(), config.formats)
        assertEquals(CameraLens.Back, config.cameraLens)
        assertTrue(config.hapticFeedbackEnabled)
    }

    @Test
    fun `scan region accepts normalized bounds`() {
        val region = ScanRegion(left = 0.1f, top = 0.2f, right = 0.9f, bottom = 0.8f)

        assertEquals(0.8f, region.width, 0.0001f)
        assertEquals(0.6f, region.height, 0.0001f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `scan region rejects coordinates outside normalized range`() {
        ScanRegion(left = -0.1f, top = 0.2f, right = 0.9f, bottom = 0.8f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `scan region rejects inverted bounds`() {
        ScanRegion(left = 0.8f, top = 0.2f, right = 0.2f, bottom = 0.8f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `config rejects an empty format set`() {
        ScannerConfig(formats = emptySet())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `config rejects a negative duplicate window`() {
        ScannerConfig(duplicateWindow = (-1).milliseconds)
    }
}
