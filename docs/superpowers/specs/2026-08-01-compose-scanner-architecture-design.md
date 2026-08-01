# Compose Scanner Library Architecture

## Goal

Replace the legacy Java/View/Camera implementation with a publishable Kotlin and Jetpack Compose barcode scanning library. The new major version does not preserve source or binary compatibility with the legacy API.

The library must provide a convenient complete API for live camera scanning, still-image scanning, and barcode generation while keeping CameraX and ML Kit implementation details internal.

## Platform Baseline

- Minimum SDK: 26
- Compile SDK: 36
- Target SDK: 36
- Language: Kotlin
- UI: Jetpack Compose
- Camera: CameraX Preview and ImageAnalysis
- Recognition: Google ML Kit Barcode Scanning
- Concurrency: Kotlin coroutines and Flow
- Distribution: one Android Library AAR

Dependency versions must use stable releases and a Compose BOM. The build must expose only dependencies that are part of the public API.

## Package Boundaries

The single Gradle module is internally separated into focused packages:

- `api`: public models, configuration, controller, errors, and result types
- `compose`: public scanner composable, overlay scope, and default overlay
- `camera`: internal CameraX binding, preview, frame analysis, torch, and zoom handling
- `recognition`: internal ML Kit adapter and result mapping
- `image`: public still-image scanning facade
- `encoding`: public barcode and QR code generation facade
- `internal`: lifecycle and coroutine utilities that are not public API

CameraX and ML Kit types must not appear in public method signatures. Public API compatibility is therefore independent of either implementation.

## Public Compose API

The primary entry point is a composable scanner:

```kotlin
@Composable
fun BarcodeScanner(
    modifier: Modifier = Modifier,
    controller: ScannerController = rememberScannerController(),
    config: ScannerConfig = ScannerConfig(),
    overlay: @Composable ScannerOverlayScope.() -> Unit = {
        DefaultScannerOverlay()
    },
    onResult: (ScanResult) -> Unit,
)
```

The component owns preview rendering and analysis but does not own navigation or application permission policy. It binds to the current lifecycle automatically and releases CameraX and ML Kit resources when it leaves composition.

The overlay API is slot-based. The default overlay provides a scan region, animated scan line, loading state, permission-required state, and error state. Consumers may replace it without replacing camera behavior.

## Controller And State

`ScannerController` is remembered with `rememberScannerController()` and exposes read-only state plus commands:

```kotlin
interface ScannerController {
    val state: StateFlow<ScannerState>
    val torchEnabled: StateFlow<Boolean>
    val zoomRatio: StateFlow<Float>

    fun start()
    fun pause()
    fun resume()
    fun toggleTorch()
    fun setTorch(enabled: Boolean)
    fun setZoom(ratio: Float)
}
```

The stable state model is:

```kotlin
sealed interface ScannerState {
    data object Idle : ScannerState
    data object Starting : ScannerState
    data object Scanning : ScannerState
    data object Paused : ScannerState
    data object PermissionRequired : ScannerState
    data class Error(val error: ScannerError) : ScannerState
}
```

Commands are idempotent. Invalid zoom values are clamped to the active camera range. Torch requests on unsupported hardware update state with a typed error instead of throwing.

## Configuration

`ScannerConfig` is immutable and includes:

- scan mode: `Single` or `Continuous`
- accepted barcode formats, with all supported formats as the default
- camera lens preference, defaulting to the back camera
- scan-region fraction, defaulting to a centered region
- continuous-mode duplicate suppression duration
- analysis enablement and initial torch preference
- haptic feedback enablement

Single mode is the default. A successful result atomically pauses analysis before invoking `onResult`. The caller resumes with `controller.resume()`.

Continuous mode keeps analysis active. Identical barcode values and formats are suppressed during the configured deduplication window. Different values are delivered immediately. The default window is 1,500 milliseconds.

## Data Flow

1. The composable observes permission and lifecycle state.
2. The internal camera coordinator binds Preview and ImageAnalysis to the lifecycle owner.
3. ImageAnalysis uses `STRATEGY_KEEP_ONLY_LATEST` and a dedicated executor.
4. The analyzer converts the current frame to an ML Kit input image without copying pixel buffers when supported.
5. The recognizer maps ML Kit barcodes to library-owned `ScanResult` values.
6. The scan policy applies format filtering, single-mode pausing, and continuous-mode deduplication.
7. Results and state changes are delivered on the main dispatcher.
8. Each image proxy is closed exactly once, including cancellation and failure paths.

Only one recognition request may be active. Frames arriving while recognition is active are discarded through CameraX backpressure rather than queued.

## Permission Contract

The host application owns runtime permission requests. This avoids forcing activity-result behavior, copy, or navigation policy into a reusable library.

When camera permission is absent, the controller reports `PermissionRequired` and the default overlay shows a neutral permission-required surface. `ScannerOverlayScope` exposes the state so consumers can provide their own action UI. Once permission is granted, composition automatically retries camera binding.

The manifest declares the camera permission and marks camera hardware as optional so applications that only use image scanning or encoding remain installable on devices without a camera.

## Still-Image Scanning

`BitmapScanner` is a suspend-based facade that accepts Android `Bitmap` and a content `Uri` through an explicit `Context` parameter. It returns a typed outcome rather than invoking callbacks:

```kotlin
suspend fun BitmapScanner.scan(bitmap: Bitmap): ScanOutcome<List<ScanResult>>
suspend fun BitmapScanner.scan(context: Context, uri: Uri): ScanOutcome<List<ScanResult>>
```

`ScanOutcome` is a library-owned sealed type with `Success` and `Failure(ScannerError)` variants. Empty recognition returns `Success(emptyList())`. Invalid input, image loading failures, and recognizer failures return typed failures consistently across both overloads. Coroutine cancellation is rethrown and is never converted to a scanner error.

## Barcode Generation

`BarcodeEncoder` generates QR codes and common one-dimensional formats as `Bitmap` values. Encoding is independent of CameraX and ML Kit recognition. ZXing core may be retained as an internal implementation dependency solely for encoding.

The public encoder accepts immutable options for dimensions, margin, foreground/background colors, character set, and QR error correction. It validates blank data, invalid dimensions, unsupported formats, and insufficient dimensions before allocation.

## Errors

`ScannerError` is a library-owned sealed hierarchy covering:

- permission missing
- no matching camera
- camera binding failure
- torch unsupported or camera unavailable
- recognizer initialization or processing failure
- invalid configuration
- image loading failure
- encoder input failure

Expected runtime conditions are represented in state. Programming errors in configuration are rejected early with clear messages. Internal CameraX and ML Kit exceptions may be retained as causes but are never required for consumer branching.

## Lifecycle And Resource Ownership

The composable owns live-camera resources. It closes the recognizer, unbinds camera use cases, cancels analysis work, and shuts down its executor on disposal. Lifecycle stop pauses active analysis and lifecycle start restores the prior intended running state.

`BitmapScanner` and `BarcodeEncoder` are independent of composition. A closable image scanner may be supplied for repeated batch operations; convenience functions create and close a short-lived scanner safely.

Haptic feedback uses Compose/platform haptic APIs and follows the configuration. No accelerometer listener is registered; CameraX focus behavior and user tap-to-focus replace the legacy sensor-driven autofocus.

## Testing

JVM unit tests cover:

- scanner state transitions and command idempotency
- single-result automatic pause
- continuous-result deduplication and expiry
- format filtering
- configuration validation
- ML Kit-to-public-model mapping
- error mapping
- encoder validation and generated matrix dimensions

Android instrumented and Compose tests cover:

- lifecycle binding and disposal
- permission-required rendering and recovery after permission grant
- default overlay states and custom overlay slots
- controller state collection across recomposition
- image scanning with known barcode fixtures

Camera and recognizer boundaries use internal interfaces so state and policy tests do not require real hardware. CI builds the release AAR, runs lint and JVM tests, and runs instrumented tests where an emulator is available.

## Migration And Release

This is a clean major-version rewrite. Legacy Java classes, XML attributes, `android.hardware.Camera`, and View-based APIs are removed. Documentation must include:

- Gradle installation
- required permission declaration and request example
- minimal scanner composable example
- single and continuous mode examples
- custom overlay example
- torch and zoom controls
- still-image scanning
- QR and barcode generation
- lifecycle and error-handling expectations

The repository remains a single publishable library rather than adding a sample application. README snippets and tests act as the executable usage reference.

## Acceptance Criteria

- A consumer can add the AAR/dependency and render a working scanner with one composable and one result callback.
- Camera permission absence is safe and observable, with no crash or implicit permission request.
- Single and continuous modes behave as documented.
- Camera and recognizer resources do not survive composable disposal.
- Public APIs contain no CameraX or ML Kit types.
- Still-image recognition and barcode generation work without starting a camera.
- Release AAR, lint, unit tests, and available instrumented tests pass.
- No legacy View or Camera API remains in the published source set.
