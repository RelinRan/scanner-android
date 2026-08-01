package com.android.scanner.api

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

public enum class CameraLens {
    Back,
    Front,
}

public data class ScanRegion(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    init {
        require(left in 0f..1f && top in 0f..1f && right in 0f..1f && bottom in 0f..1f) {
            "Scan region coordinates must be normalized to 0..1"
        }
        require(left < right && top < bottom) {
            "Scan region bounds must define a positive area"
        }
    }

    public val width: Float get() = right - left
    public val height: Float get() = bottom - top

    public companion object {
        public val Centered: ScanRegion = ScanRegion(
            left = 0.1f,
            top = 0.25f,
            right = 0.9f,
            bottom = 0.75f,
        )
    }
}

public data class ScannerConfig(
    val scanMode: ScanMode = ScanMode.Single,
    val formats: Set<BarcodeFormat> = BarcodeFormat.entries.toSet(),
    val cameraLens: CameraLens = CameraLens.Back,
    val scanRegion: ScanRegion = ScanRegion.Centered,
    val duplicateWindow: Duration = 1_500.milliseconds,
    val analysisEnabled: Boolean = true,
    val initialTorchEnabled: Boolean = false,
    val hapticFeedbackEnabled: Boolean = true,
) {
    init {
        require(formats.isNotEmpty()) { "At least one barcode format is required" }
        require(!duplicateWindow.isNegative()) { "Duplicate window cannot be negative" }
    }
}
