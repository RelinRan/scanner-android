# Compose Scanner Rewrite Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the legacy Java/View camera scanner with a publishable Kotlin Compose library using CameraX, ML Kit, immutable state, live and still-image scanning, and barcode generation.

**Architecture:** Keep one Android library module and separate its public API from internal CameraX and ML Kit adapters. Drive live scanning through a controller-backed state machine and a small scan policy, with the composable owning lifecycle-bound camera resources.

**Tech Stack:** Kotlin, Jetpack Compose, CameraX, ML Kit Barcode Scanning, coroutines/StateFlow, ZXing Core for encoding, JUnit, kotlinx-coroutines-test, Robolectric, AndroidX Compose test.

---

## File Map

- `build.gradle`, `settings.gradle`, `gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.properties`: modern plugin and dependency baseline.
- `app/build.gradle`, `app/src/main/AndroidManifest.xml`: library configuration, Compose, dependencies, and optional camera declaration.
- `app/src/main/java/com/android/scanner/api/*`: public immutable config, state, result, error, outcome, and controller contracts.
- `app/src/main/java/com/android/scanner/internal/policy/ScanPolicy.kt`: single/continuous result filtering.
- `app/src/main/java/com/android/scanner/encoding/*`: public encoder and options.
- `app/src/main/java/com/android/scanner/image/*`: still-image scanner facade and internal ML Kit bridge.
- `app/src/main/java/com/android/scanner/internal/camera/*`: CameraX binding, analysis, torch, zoom, and recognition mapping.
- `app/src/main/java/com/android/scanner/compose/*`: controller implementation, scanner composable, preview, overlay scope, and default overlay.
- `app/src/test/java/com/android/scanner/*`: JVM tests for contracts, policy, controller, mapping, image facade, and encoder.
- `app/src/androidTest/java/com/android/scanner/*`: Compose and lifecycle integration tests.
- `README.md`, `README.zh-CN.md`, `CHANGELOG.md`: installation, complete usage, migration, and release notes.

### Task 1: Modern Kotlin Library Baseline

**Files:**
- Create: `gradle/libs.versions.toml`
- Modify: `build.gradle`
- Modify: `settings.gradle`
- Modify: `gradle/wrapper/gradle-wrapper.properties`
- Modify: `app/build.gradle`
- Modify: `app/src/main/AndroidManifest.xml`

- [ ] **Step 1: Replace the build baseline**

Use version-catalog aliases for stable Kotlin, Android Gradle Plugin, Compose BOM, CameraX, ML Kit, coroutines, ZXing, JUnit, Robolectric, and AndroidX test dependencies. Keep the temporary legacy namespace `com.android.zxing` so existing sources compile during incremental migration; configure `compileSdk = 36`, `minSdk = 26`, `targetSdk = 36`, Java/Kotlin 17, Compose, consumer ProGuard rules, sources JAR, and release AAR publishing metadata. Remove AppCompat, Material Views, ConstraintLayout, and replace the local `core-3.4.1.jar` dependency with the catalog-managed ZXing dependency.

- [ ] **Step 2: Declare optional camera hardware**

Set the manifest to:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-feature android:name="android.hardware.camera.any" android:required="false" />
</manifest>
```

- [ ] **Step 3: Verify the empty modern baseline**

Run: `./gradlew clean :app:assembleDebug`

Expected: `BUILD SUCCESSFUL` with Kotlin and Compose enabled.

- [ ] **Step 4: Commit**

```bash
git add build.gradle settings.gradle gradle app/build.gradle app/src/main/AndroidManifest.xml
git commit -m "build: migrate scanner library to Kotlin and Compose"
```

### Task 2: Public Models And Validation

**Files:**
- Create: `app/src/main/java/com/android/scanner/api/BarcodeFormat.kt`
- Create: `app/src/main/java/com/android/scanner/api/ScanMode.kt`
- Create: `app/src/main/java/com/android/scanner/api/ScannerConfig.kt`
- Create: `app/src/main/java/com/android/scanner/api/ScanResult.kt`
- Create: `app/src/main/java/com/android/scanner/api/ScannerError.kt`
- Create: `app/src/main/java/com/android/scanner/api/ScanOutcome.kt`
- Create: `app/src/main/java/com/android/scanner/api/ScannerState.kt`
- Create: `app/src/main/java/com/android/scanner/api/ScannerController.kt`
- Create: `app/src/test/java/com/android/scanner/api/ScannerConfigTest.kt`

- [ ] **Step 1: Write configuration tests**

Cover defaults (`Single`, back lens, all formats, 1,500 ms deduplication, haptics enabled), valid centered scan-region fractions, and rejection of fractions outside `0f..1f` or empty accepted-format sets.

```kotlin
@Test fun `defaults use single scanning and all formats`() {
    val config = ScannerConfig()
    assertEquals(ScanMode.Single, config.scanMode)
    assertEquals(1_500.milliseconds, config.duplicateWindow)
    assertEquals(BarcodeFormat.entries.toSet(), config.formats)
}
```

- [ ] **Step 2: Run the test and confirm failure**

Run: `./gradlew :app:testDebugUnitTest --tests '*.ScannerConfigTest'`

Expected: compilation fails because the API models do not exist.

- [ ] **Step 3: Implement public contracts**

Define library-owned barcode formats, immutable config, lens preference, normalized `ScanRegion`, raw/display values and corner points in `ScanResult`, sealed `ScannerError`, `ScanOutcome.Success/Failure`, the approved scanner states, and a `ScannerController` exposing read-only `StateFlow` plus `start`, `pause`, `resume`, `setTorch`, `toggleTorch`, and `setZoom`.

- [ ] **Step 4: Run API tests**

Run: `./gradlew :app:testDebugUnitTest --tests '*.ScannerConfigTest'`

Expected: all `ScannerConfigTest` tests pass.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/android/scanner/api app/src/test/java/com/android/scanner/api
git commit -m "feat: define scanner public API"
```

### Task 3: Scan Policy State Machine

**Files:**
- Create: `app/src/main/java/com/android/scanner/internal/policy/ScanPolicy.kt`
- Create: `app/src/test/java/com/android/scanner/internal/policy/ScanPolicyTest.kt`

- [ ] **Step 1: Write policy tests**

Test that single mode accepts the first result and requests pause, continuous mode accepts different values immediately, identical value/format pairs are suppressed for 1,500 ms, the same value in a different format is accepted, and expired entries are accepted again. Inject `nowMillis: () -> Long` for deterministic time.

- [ ] **Step 2: Verify tests fail**

Run: `./gradlew :app:testDebugUnitTest --tests '*.ScanPolicyTest'`

Expected: compilation fails because `ScanPolicy` does not exist.

- [ ] **Step 3: Implement the minimal policy**

Return `PolicyDecision.Deliver(pauseAfterDelivery: Boolean)` or `PolicyDecision.Suppress`; key deduplication by `format` plus `rawValue`, prune expired entries on each result, and expose `reset()` for a new session.

- [ ] **Step 4: Verify tests pass**

Run: `./gradlew :app:testDebugUnitTest --tests '*.ScanPolicyTest'`

Expected: all policy tests pass.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/android/scanner/internal/policy app/src/test/java/com/android/scanner/internal/policy
git commit -m "feat: add single and continuous scan policy"
```

### Task 4: Barcode Encoder

**Files:**
- Create: `app/src/main/java/com/android/scanner/encoding/BarcodeEncoder.kt`
- Create: `app/src/main/java/com/android/scanner/encoding/BarcodeEncoderOptions.kt`
- Create: `app/src/test/java/com/android/scanner/encoding/BarcodeEncoderTest.kt`

- [ ] **Step 1: Write encoder tests**

Verify blank content, non-positive dimensions, and unsupported ML Kit-only formats return `ScanOutcome.Failure(ScannerError.InvalidInput)`; verify QR and CODE_128 output dimensions and configured foreground/background pixels.

- [ ] **Step 2: Verify tests fail**

Run: `./gradlew :app:testDebugUnitTest --tests '*.BarcodeEncoderTest'`

Expected: compilation fails because encoder types do not exist.

- [ ] **Step 3: Implement encoding**

Map supported public formats to ZXing, use `MultiFormatWriter`, and expose:

```kotlin
object BarcodeEncoder {
    fun encode(content: String, options: BarcodeEncoderOptions): ScanOutcome<Bitmap>
    fun qrCode(content: String, size: Int = 512): ScanOutcome<Bitmap>
}
```

Catch ZXing writer failures and map them to `ScannerError.EncodingFailed`; do not catch fatal errors.

- [ ] **Step 4: Verify encoder tests pass**

Run: `./gradlew :app:testDebugUnitTest --tests '*.BarcodeEncoderTest'`

Expected: all encoder tests pass.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/android/scanner/encoding app/src/test/java/com/android/scanner/encoding
git commit -m "feat: add barcode encoding API"
```

### Task 5: ML Kit Mapping And Still-Image Scanner

**Files:**
- Create: `app/src/main/java/com/android/scanner/internal/recognition/BarcodeRecognizer.kt`
- Create: `app/src/main/java/com/android/scanner/internal/recognition/MlKitBarcodeRecognizer.kt`
- Create: `app/src/main/java/com/android/scanner/internal/recognition/MlKitResultMapper.kt`
- Create: `app/src/main/java/com/android/scanner/image/BitmapScanner.kt`
- Create: `app/src/test/java/com/android/scanner/internal/recognition/MlKitResultMapperTest.kt`
- Create: `app/src/test/java/com/android/scanner/image/BitmapScannerTest.kt`

- [ ] **Step 1: Write mapping and facade tests**

Use fake recognizer inputs to verify supported formats, null raw/display values, bounding boxes, corner points, empty recognition, recognizer failure mapping, URI load failure mapping, and propagation of `CancellationException`.

- [ ] **Step 2: Verify tests fail**

Run: `./gradlew :app:testDebugUnitTest --tests '*.MlKitResultMapperTest' --tests '*.BitmapScannerTest'`

Expected: compilation fails because recognition and image APIs do not exist.

- [ ] **Step 3: Implement the recognizer boundary and facade**

Wrap ML Kit Tasks with `kotlinx-coroutines-play-services.await()`. Configure the recognizer from public format sets, map every result to library-owned models, and expose `suspend scan(Bitmap)` and `suspend scan(Context, Uri)` returning `ScanOutcome<List<ScanResult>>`. Always rethrow coroutine cancellation and close owned recognizers.

- [ ] **Step 4: Verify tests pass**

Run: `./gradlew :app:testDebugUnitTest --tests '*.MlKitResultMapperTest' --tests '*.BitmapScannerTest'`

Expected: all mapping and still-image tests pass.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/android/scanner/internal/recognition app/src/main/java/com/android/scanner/image app/src/test/java/com/android/scanner/internal/recognition app/src/test/java/com/android/scanner/image
git commit -m "feat: add ML Kit image scanning"
```

### Task 6: Controller Implementation

**Files:**
- Create: `app/src/main/java/com/android/scanner/compose/ScannerControllerImpl.kt`
- Create: `app/src/main/java/com/android/scanner/compose/RememberScannerController.kt`
- Create: `app/src/main/java/com/android/scanner/internal/camera/CameraCommands.kt`
- Create: `app/src/test/java/com/android/scanner/compose/ScannerControllerImplTest.kt`

- [ ] **Step 1: Write controller tests**

Test initial idle state, idempotent start/pause/resume, intended-running state across lifecycle stop/start, torch unsupported errors, torch state reflection, zoom clamping to injected limits, and command behavior before camera attachment.

- [ ] **Step 2: Verify tests fail**

Run: `./gradlew :app:testDebugUnitTest --tests '*.ScannerControllerImplTest'`

Expected: compilation fails because controller implementation does not exist.

- [ ] **Step 3: Implement controller and camera command boundary**

Use private `MutableStateFlow` values, expose `asStateFlow()`, serialize state transitions on the main dispatcher, and attach/detach an internal `CameraCommands` implementation without exposing CameraX. `rememberScannerController()` returns the stable public interface.

- [ ] **Step 4: Verify controller tests pass**

Run: `./gradlew :app:testDebugUnitTest --tests '*.ScannerControllerImplTest'`

Expected: all controller tests pass.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/android/scanner/compose app/src/main/java/com/android/scanner/internal/camera/CameraCommands.kt app/src/test/java/com/android/scanner/compose
git commit -m "feat: implement scanner controller state"
```

### Task 7: CameraX Runtime And Frame Analysis

**Files:**
- Create: `app/src/main/java/com/android/scanner/internal/camera/CameraCoordinator.kt`
- Create: `app/src/main/java/com/android/scanner/internal/camera/FrameAnalyzer.kt`
- Create: `app/src/main/java/com/android/scanner/internal/camera/CameraXCommands.kt`
- Create: `app/src/test/java/com/android/scanner/internal/camera/FrameAnalyzerTest.kt`

- [ ] **Step 1: Write analyzer tests**

With fake image frames and recognizer, verify only one request is active, every frame closes exactly once on success/failure/cancellation, format filtering occurs before delivery, single mode pauses before callback, and continuous mode uses `ScanPolicy`.

- [ ] **Step 2: Verify tests fail**

Run: `./gradlew :app:testDebugUnitTest --tests '*.FrameAnalyzerTest'`

Expected: compilation fails because camera runtime classes do not exist.

- [ ] **Step 3: Implement runtime**

Bind Preview and ImageAnalysis with `STRATEGY_KEEP_ONLY_LATEST`, use a single-thread executor, map rotation into ML Kit input, guard recognition with an atomic in-flight flag, and close image proxies in one completion path. Implement back/front lens selection, tap focus, torch capability, zoom limits, lifecycle bind/unbind, and typed failure mapping.

- [ ] **Step 4: Verify analyzer tests and assemble**

Run: `./gradlew :app:testDebugUnitTest --tests '*.FrameAnalyzerTest' :app:assembleDebug`

Expected: analyzer tests pass and debug AAR assembles.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/android/scanner/internal/camera app/src/test/java/com/android/scanner/internal/camera
git commit -m "feat: add lifecycle-aware CameraX runtime"
```

### Task 8: Compose Scanner And Overlay

**Files:**
- Create: `app/src/main/java/com/android/scanner/compose/BarcodeScanner.kt`
- Create: `app/src/main/java/com/android/scanner/compose/CameraPreview.kt`
- Create: `app/src/main/java/com/android/scanner/compose/ScannerOverlayScope.kt`
- Create: `app/src/main/java/com/android/scanner/compose/DefaultScannerOverlay.kt`
- Create: `app/src/androidTest/java/com/android/scanner/compose/BarcodeScannerTest.kt`

- [ ] **Step 1: Write Compose tests**

Verify permission-required semantics, default loading/error/paused overlays, custom overlay state access, stable controller across recomposition, and disposal invoking runtime close exactly once. Inject an internal runtime factory via a composition local restricted to tests.

- [ ] **Step 2: Verify tests fail**

Run: `./gradlew :app:connectedDebugAndroidTest`

Expected: test compilation fails because composable scanner types do not exist.

- [ ] **Step 3: Implement the Compose API**

Render CameraX `PreviewView` through `AndroidView`, observe permission with `ContextCompat.checkSelfPermission`, bind using `LocalLifecycleOwner`, update callbacks through `rememberUpdatedState`, and close runtime/executor/recognizer in `DisposableEffect`. Draw the default overlay with Canvas and stable dimensions; expose state and scan-region geometry through `ScannerOverlayScope`.

- [ ] **Step 4: Verify Compose tests**

Run: `./gradlew :app:connectedDebugAndroidTest`

Expected: all Compose integration tests pass on an API 26+ emulator. If no emulator is available, record that limitation and run `./gradlew :app:assembleAndroidTest` successfully.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/android/scanner/compose app/src/androidTest/java/com/android/scanner/compose
git commit -m "feat: add Compose barcode scanner"
```

### Task 9: Remove Legacy API And Add Consumer Coverage

**Files:**
- Modify: `app/build.gradle`
- Delete: `app/src/main/java/com/android/zxing/**`
- Delete: `app/src/main/res/values/attrs.xml`
- Delete: `app/src/main/res/mipmap-xxhdpi/ic_scan_code_line.png`
- Delete: `app/src/main/res/mipmap-xxhdpi/ic_scan_code_back.png`
- Delete: `app/libs/core-3.4.1.jar`
- Delete: template tests under `app/src/test/java/com/android/zxing` and `app/src/androidTest/java/com/android/zxing`
- Create: `app/src/test/java/com/android/scanner/api/PublicApiSmokeTest.kt`

- [ ] **Step 1: Add a public consumer smoke test**

Compile a minimal usage of `ScannerConfig`, `ScanMode.Continuous`, `ScannerController`, `BitmapScanner`, and `BarcodeEncoder` using only public packages. Assert no public declared method parameter or return type begins with `androidx.camera` or `com.google.mlkit`.

- [ ] **Step 2: Remove legacy sources and resources**

Delete the Java/View/Camera implementation, XML attributes, old image resources, bundled ZXing JAR, and template tests listed above. Change the library namespace from `com.android.zxing` to `com.android.scanner`. Do not retain compatibility wrappers.

- [ ] **Step 3: Verify removal and API boundary**

Run: `rg "android\.hardware\.Camera|com\.android\.zxing|ScanCodeView|ScanAreaView" app/src app/build.gradle`

Expected: no matches.

Run: `./gradlew :app:testDebugUnitTest :app:assembleRelease`

Expected: tests pass and release AAR builds.

- [ ] **Step 4: Commit**

```bash
git add -A app
git commit -m "refactor: remove legacy View scanner API"
```

### Task 10: Documentation, Lint, And Release Verification

**Files:**
- Modify: `README.md`
- Modify: `README.zh-CN.md`
- Modify: `CHANGELOG.md`
- Modify: `.github/workflows/*`
- Create: `app/consumer-rules.pro`

- [ ] **Step 1: Rewrite usage documentation**

Document Gradle installation, camera permission request owned by the host, the minimal `BarcodeScanner` call, single mode resume, continuous deduplication, custom overlay, controller torch/zoom, still-image scanning, encoding, typed errors, lifecycle ownership, API 26 minimum, and breaking migration from the removed Java/View API. Keep English and Chinese examples structurally equivalent.

- [ ] **Step 2: Update CI and release notes**

Run CI on JDK 17 and include `testDebugUnitTest`, `lintRelease`, `assembleRelease`, and Android-test APK assembly. Mark the rewrite as the next major release in the changelog without inventing a release date or tag.

- [ ] **Step 3: Run the full verification suite**

Run:

```bash
./gradlew clean :app:testDebugUnitTest :app:lintRelease :app:assembleRelease :app:assembleAndroidTest
```

Expected: `BUILD SUCCESSFUL` and an AAR under `app/build/outputs/aar/`.

- [ ] **Step 4: Inspect the published surface**

Unzip the release AAR and verify it contains the Compose scanner, API, image, and encoding packages; verify it contains no legacy `com/android/zxing` classes and no bundled duplicate ZXing JAR.

- [ ] **Step 5: Commit**

```bash
git add README.md README.zh-CN.md CHANGELOG.md .github app/consumer-rules.pro
git commit -m "docs: publish Compose scanner usage"
```

- [ ] **Step 6: Final repository check**

Run: `git status --short`

Expected: no output.
