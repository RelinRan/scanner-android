# Scanner Compose Library

`android.scanner.api` is a Kotlin Android library for Camera2 bitmap previews, Compose scan overlays, and QR/barcode bitmap conversion.

## Requirements

- Android API 26+
- Kotlin and Jetpack Compose
- Gradle 8.11.1
- Camera permission requested by the host application

## Installation

Build the AAR with:

```bash
./gradlew :app:assembleRelease
```

The library uses the local ZXing dependency at `app/libs/core-3.5.3.jar`.

## Permission

Add the permission to the host application's manifest and request it at runtime:

```xml
<uses-permission android:name="android.permission.CAMERA" />
```

## Camera Preview

`ScannerCamera` opens a Camera2 device and publishes the newest JPEG frame as `StateFlow<Bitmap?>`. Unsupported requested resolutions are replaced with the closest supported camera size. The selected size is available as `actualResolution`.

```kotlin
@Composable
fun CameraScreen(context: Context) {
    val camera = remember {
        ScannerCamera(
            context = context,
            config = ScannerCameraConfig(
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

    ScannerPreview(bitmap = bitmap)
}
```

## Compose Overlay

`ScannerPreview` draws a Bitmap and a configurable scan region. Coordinates are normalized from `0f` to `1f`.

```kotlin
ScannerPreview(
    bitmap = bitmap,
    config = ScannerPreviewConfig(
        region = ScanRegion(left = 0.1f, top = 0.25f, right = 0.9f, bottom = 0.75f),
        outsideColor = Color.Black.copy(alpha = 0.55f),
        insideColor = Color.Transparent,
        borderColor = Color.White,
        borderWidth = 2.dp,
    ),
)
```

## QR Bitmap Conversion

`BarcodeCodec` converts QR content to a configurable-size Bitmap and decodes a Bitmap back to typed scan results.

```kotlin
val encoded = BarcodeCodec.encodeQr("scanner-value", BitmapSize(512, 512))
if (encoded is ScanOutcome.Success) {
    val decoded = BarcodeCodec.decode(encoded.value)
}
```

Failures are returned as `ScanOutcome.Failure` with a `ScannerError` value. Blank content and invalid dimensions are rejected before allocation.

## Debug Logging

Debug logging is disabled by default. Enable it during development:

```kotlin
ScannerDebug.enabled = true
```

Logs include Camera2 open/close events, resolution selection, capture-session setup, QR encode/decode parameters, results, and failures. A custom logger can be supplied through `ScannerDebug.logger`.

## Public API

All public classes are in `android.scanner.api`:

- `ScannerCamera`, `ScannerCameraConfig`
- `ScannerPreview`, `ScannerPreviewConfig`, `ScanRegion`
- `BarcodeCodec`, `BitmapSize`
- `ScanResult`, `ScanOutcome`, `ScannerError`
- `BarcodeFormat`, `ScannerConfig`, `ScanMode`, `ScannerState`, `ScannerController`
- `ScannerDebug`

## Lifecycle

`ScannerCamera` owns a Camera2 device, capture session, image reader, and background thread. Call `close()` when the owner leaves composition or the screen is destroyed. Do not call camera methods before runtime permission is granted.

## License

MIT. See [LICENSE](./LICENSE) and [docs/NOTICE.md](./docs/NOTICE.md).
