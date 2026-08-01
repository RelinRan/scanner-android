# Scanner Compose 鎵爜搴?
[English](./README.md)

`android.scanner.api` 鏄竴涓?Kotlin Android Library锛屾彁渚?Camera2 瀹炴椂 Bitmap銆丆ompose 鎵弿鍖哄煙棰勮锛屼互鍙婁簩缁寸爜/鏉″舰鐮?Bitmap 缂栬В鐮佽兘鍔涖€?
## 鐜瑕佹眰

- Android API 26 鍙婁互涓?- Kotlin 涓?Jetpack Compose
- Gradle 8.11.1
- 鐩告満鏉冮檺鐢卞涓诲簲鐢ㄧ敵璇?
## 鏋勫缓

```bash
./gradlew :app:assembleRelease
```

ZXing 浣跨敤椤圭洰鍐呯殑鏈湴渚濊禆锛歚app/libs/core-3.5.3.jar`銆?
## 鐩告満鏉冮檺

瀹夸富搴旂敤闇€瑕佸０鏄庡苟鍔ㄦ€佺敵璇锋潈闄愶細

```xml
<uses-permission android:name="android.permission.CAMERA" />
```

## 鐩告満 Bitmap

`ScannerPreview` 浣跨敤 Camera2 鎵撳紑鎽勫儚澶达紝閫氳繃 `StateFlow<Bitmap?>` 鍙戝竷鏈€鏂扮殑 JPEG 甯с€傚鏋滆姹傚垎杈ㄧ巼涓嶅彈璁惧鏀寔锛屼細鑷姩閫夋嫨瀹介珮姣斾緥鍜屽昂瀵告渶鎺ヨ繎鐨勫垎杈ㄧ巼锛屽疄闄呭垎杈ㄧ巼鍙€氳繃 `actualResolution` 鑾峰彇銆?
```kotlin
val camera = remember {
    ScannerPreview(
        context,
        ScannerOverlayConfig(
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

## Compose 棰勮

`ScannerPreview` 鏄剧ず Bitmap锛屽苟缁樺埗鍙厤缃殑鎵弿鍖哄煙銆傚尯鍩熷潗鏍囦娇鐢?`0f..1f` 鐨勫綊涓€鍖栧€笺€?
```kotlin
ScannerPreview(
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

## 浜岀淮鐮?Bitmap 缂栬В鐮?
`BarcodeCodec` 鏀寔浜岀淮鐮佸唴瀹逛笌 Bitmap 浜掕浆锛屽苟鏀寔閰嶇疆杈撳嚭灏哄銆?
```kotlin
val encoded = BarcodeCodec.encodeQr("scanner-value", BitmapSize(512, 512))
if (encoded is ScanOutcome.Success) {
    val decoded = BarcodeCodec.decode(encoded.value)
}
```

澶辫触閫氳繃 `ScanOutcome.Failure` 鍜?`ScannerError` 杩斿洖銆傜┖鍐呭鍜岄潪娉曞昂瀵镐細鍦ㄥ垎閰?Bitmap 鍓嶈鎷掔粷銆?
## Debug 鏃ュ織

榛樿鍏抽棴璋冭瘯鏃ュ織锛屽紑鍙戞椂鍙紑鍚細

```kotlin
ScannerDebug.enabled = true
```

鏃ュ織鍖呭惈 Camera2 鎵撳紑/鍏抽棴銆佸垎杈ㄧ巼閫夋嫨銆佷細璇濋厤缃€佷簩缁寸爜缂栬В鐮佸弬鏁般€佺粨鏋滃拰寮傚父銆傚彲浠ラ€氳繃 `ScannerDebug.logger` 鎺ュ叆瀹夸富鏃ュ織绯荤粺銆?
## 鍏叡 API

鎵€鏈夊叕鍏?API 閮戒綅浜?`android.scanner.api`锛?
- `ScannerPreview`銆乣ScannerOverlayConfig`
- `ScannerPreview`銆乣ScannerOverlayConfig`銆乣ScanRegion`
- `BarcodeCodec`銆乣BitmapSize`
- `ScanResult`銆乣ScanOutcome`銆乣ScannerError`
- `BarcodeFormat`銆乣ScannerConfig`銆乣ScanMode`銆乣ScannerState`銆乣ScannerController`
- `ScannerDebug`

## 鐢熷懡鍛ㄦ湡

`ScannerPreview` 绠＄悊 Camera2 璁惧銆侀噰闆嗕細璇濄€両mageReader 鍜屽悗鍙扮嚎绋嬨€傜寮€ Compose 鎴栭攢姣侀〉闈㈡椂蹇呴』璋冪敤 `close()`銆傝幏寰楄繍琛屾椂鐩告満鏉冮檺鍓嶄笉瑕佽皟鐢ㄧ浉鏈烘柟娉曘€?
## 寮€婧愬崗璁?
MIT锛岃瑙?[LICENSE](./LICENSE) 鍜?[docs/NOTICE.zh-CN.md](./docs/NOTICE.zh-CN.md)銆?
