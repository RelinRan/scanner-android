# Scanner Compose Library

[简体中文](./README.zh-CN.md)

Modern Kotlin Android library under `android.scanner.api`.

## Features

- Camera2 Bitmap stream with automatic nearest supported resolution selection
- Jetpack Compose overlay with configurable scan region and colors
- QR code Bitmap encode/decode with configurable output size
- Optional debug logging for camera and codec flows
- Android API 26+

## Permission

The host app must declare and request camera permission:

```xml
<uses-permission android:name="android.permission.CAMERA" />
```

## Camera Bitmap

```kotlin
val camera = remember {
    ScannerPreview(
        context = context,
        config = ScannerPreviewConfig(
            cameraId = "0",
            width = 1280,
            height = 720,
            torchEnabled = false,
            autoFocus = true,
        ),
    )
}
val bitmap by camera.bitmap.collectAsState()

DisposableEffect(camera) {
    camera.start()
    onDispose { camera.close() }
}

ScannerOverlay(bitmap = bitmap)
```

`actualResolution` exposes the size selected from the camera's supported outputs.

## Compose Overlay

```kotlin
ScannerOverlay(
    bitmap = bitmap,
    config = ScannerOverlayConfig(
        region = ScanRegion(0.1f, 0.25f, 0.9f, 0.75f),
        outsideColor = Color.Black.copy(alpha = 0.55f),
        insideColor = Color.Transparent,
        borderColor = Color.White,
        borderWidth = 2.dp,
    ),
)
```

## QR Bitmap Codec

```kotlin
val encoded = BarcodeCodec.encodeQr("scanner-value", BitmapSize(512, 512))
if (encoded is ScanOutcome.Success) {
    val decoded = BarcodeCodec.decode(encoded.value)
}
```

Results use `ScanOutcome` and `ScannerError`; normal failures do not throw.

## Debug Logging

```kotlin
ScannerDebug.enabled = true
```

Logs cover camera opening, resolution selection, capture sessions, QR encoding, decoding, and failures. Replace `ScannerDebug.logger` to integrate with the host logger.

## Local Dependency

ZXing is bundled as `app/libs/core-3.5.3.jar`. Build the release AAR with:

```bash
./gradlew :app:assembleRelease
```

## Lifecycle

Call `ScannerPreview.close()` when the owning screen or composition is destroyed. Do not start it before camera permission is granted.

## License

MIT. See [LICENSE](./LICENSE) and [docs/NOTICE.md](./docs/NOTICE.md).
