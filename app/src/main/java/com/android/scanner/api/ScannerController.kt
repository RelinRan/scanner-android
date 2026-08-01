package com.android.scanner.api

import kotlinx.coroutines.flow.StateFlow

public interface ScannerController {
    public val state: StateFlow<ScannerState>
    public val torchEnabled: StateFlow<Boolean>
    public val zoomRatio: StateFlow<Float>

    public fun start()
    public fun pause()
    public fun resume()
    public fun toggleTorch()
    public fun setTorch(enabled: Boolean)
    public fun setZoom(ratio: Float)
}
