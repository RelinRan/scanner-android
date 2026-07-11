# Scanner

**English** | [简体中文](./README.zh-CN.md)

An Android barcode / QR code scanning and generation library built on top of [ZXing](https://github.com/zxing/zxing) `core-3.4.1`. It provides a ready-to-use camera `View`, a decoding pipeline with improved recognition rate, and helpers for creating barcodes, QR codes and decoding from images.

## Features

- Camera-based real-time scanning of barcodes and QR codes
- Decode barcodes / QR codes from image files or `Bitmap`s
- Create QR codes and barcodes (CODE_128, etc.)
- Improved recognition-rate algorithm and graphics-distortion handling
- Customizable scan view: background color, corner style, scan line, animation duration
- Built-in torch toggle, pause / resume decoding, focus sensor, and scan vibration

## Preview

![Preview](./ic_preview.png)

## Integration

### Option A — AAR from GitHub Releases

Download the latest `scanner-<version>.aar` from the [Releases page](https://github.com/RelinRan/Scanner/releases), drop it into your module's `libs/` folder, then configure `app/build.gradle`:

```groovy
android {
    // ...
    repositories {
        flatDir {
            dirs 'libs'
        }
    }
}

dependencies {
    implementation(name: 'scanner-1.1.0', ext: 'aar')
}
```

### Option B — JitPack

Add the JitPack repository in your root `build.gradle` / `settings.gradle`:

```groovy
allprojects {
    repositories {
        // ...
        maven { url 'https://jitpack.io' }
    }
}
```

Then add the dependency in `app/build.gradle`:

```groovy
dependencies {
    implementation 'com.github.RelinRan:Scanner:1.1.0'
}
```

## Permissions

> **Note:** On Android 6.0 (API 23) and above you must request `CAMERA` at runtime.

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.VIBRATE" />
```

## Usage

### ScanCodeView

```xml
<com.android.zxing.view.ScanCodeView
    android:id="@+id/scan_code"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

### Scan callback

```java
ScanCodeView scanCode = findViewById(R.id.scan_code);
scanCode.setOnScanCodeListener(new OnScanCodeListener() {
    @Override
    public void onScanCodeSucceed(Result result) {
        String code = result.getText();
    }

    @Override
    public void onScanCodeFailed(Exception exception) {
        // Called when no result is decoded for a frame
    }
});
```

### Torch

```java
ScanCodeView scanCode = findViewById(R.id.scan_code);
scanCode.toggleTorch();
```

### Pause / Resume decoding

```java
ScanCodeView scanCode = findViewById(R.id.scan_code);
scanCode.onPause();   // pause decoding
scanCode.onResume();  // resume decoding
```

### Create a QR code

```java
Bitmap qrCode = ZXWriter.createQRCode("content");
```

### Create a barcode

```java
Bitmap barCode = ZXWriter.createCode(BarcodeFormat.CODE_128, "content", 300, 150);
```

### Decode from an image file

```java
File file = new File("/sdcard/Download/0001.png");
ZXReader.fromFile(file, new OnScanCodeListener() {
    @Override
    public void onScanCodeSucceed(Result result) {
        String code = result.getText();
    }

    @Override
    public void onScanCodeFailed(Exception exception) {
        // Decoding failed
    }
});
```

## Attributes

| Attribute | Format | Description |
|---|---|---|
| `areaCenterX` | dimension &#124; reference | Scan center X |
| `areaCenterY` | dimension &#124; reference | Scan center Y |
| `areaWidth` | dimension &#124; reference | Scan area width |
| `areaHeight` | dimension &#124; reference | Scan area height |
| `backgroundColor` | color &#124; reference | Scan view background color |
| `cornerVisible` | boolean &#124; reference | Corner marker visibility |
| `cornerLineColor` | color &#124; reference | Corner marker line color |
| `cornerLineMargin` | dimension &#124; reference | Corner marker line margin |
| `cornerLineLength` | dimension &#124; reference | Corner marker line length |
| `cornerLineWidth` | dimension &#124; reference | Corner marker line width |
| `duration` | integer &#124; reference | Scan line animation duration (ms) |
| `lineDrawable` | integer &#124; reference | Scan line drawable |
| `vibrator` | boolean &#124; reference | Vibrate on successful scan |

## Versioning

This project follows [Semantic Versioning 2.0.0](https://semver.org/). See the [releases page](https://github.com/RelinRan/Scanner/releases) for available versions and the [changelog](./CHANGELOG.md) for release history.

## License

Released under the [MIT License](./LICENSE). This library bundles and depends on third-party software — see the [NOTICE](./docs/NOTICE.md) file for attribution and third-party licenses.
