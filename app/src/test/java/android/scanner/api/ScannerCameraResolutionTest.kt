package android.scanner.api

import android.util.Size
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ScannerCameraResolutionTest {
    @Test
    fun `selects closest supported resolution by aspect ratio then distance`() {
        val camera = ScannerCameraResolutionSelector
        val selected = camera.select(
            supported = listOf(Size(640, 480), Size(1280, 720), Size(1920, 1080)),
            requested = Size(1000, 700),
        )

        assertEquals(Size(1280, 720), selected)
    }

    @Test
    fun `returns null when camera exposes no sizes`() {
        assertNull(ScannerCameraResolutionSelector.select(emptyList(), Size(1280, 720)))
    }
}
