# Scanner Compose 扫码库

`android.scanner.api` 是一个 Kotlin Android Library，提供 Camera2 实时 Bitmap、Compose 扫描区域预览，以及二维码/条形码 Bitmap 编解码能力。

## 环境要求

- Android API 26 及以上
- Kotlin 与 Jetpack Compose
- Gradle 8.11.1
- 相机权限由宿主应用申请

## 构建

```bash
./gradlew :app:assembleRelease
```

ZXing 使用项目内的本地依赖：`app/libs/core-3.5.3.jar`。

## 相机权限

宿主应用需要声明并动态申请权限：

```xml
<uses-permission android:name="android.permission.CAMERA" />
```

## 相机 Bitmap

`ScannerCamera` 使用 Camera2 打开摄像头，通过 `StateFlow<Bitmap?>` 发布最新的 JPEG 帧。如果请求分辨率不受设备支持，会自动选择宽高比例和尺寸最接近的分辨率，实际分辨率可通过 `actualResolution` 获取。

```kotlin
val camera = remember {
    ScannerCamera(
        context,
        ScannerCameraConfig(
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
```

## Compose 预览

`ScannerPreview` 显示 Bitmap，并绘制可配置的扫描区域。区域坐标使用 `0f..1f` 的归一化值。

```kotlin
ScannerPreview(
    bitmap = bitmap,
    config = ScannerPreviewConfig(
        region = ScanRegion(0.1f, 0.25f, 0.9f, 0.75f),
        outsideColor = Color.Black.copy(alpha = 0.55f),
        insideColor = Color.Transparent,
        borderColor = Color.White,
        borderWidth = 2.dp,
    ),
)
```

## 二维码 Bitmap 编解码

`BarcodeCodec` 支持二维码内容与 Bitmap 互转，并支持配置输出尺寸。

```kotlin
val encoded = BarcodeCodec.encodeQr("scanner-value", BitmapSize(512, 512))
if (encoded is ScanOutcome.Success) {
    val decoded = BarcodeCodec.decode(encoded.value)
}
```

失败通过 `ScanOutcome.Failure` 和 `ScannerError` 返回。空内容和非法尺寸会在分配 Bitmap 前被拒绝。

## Debug 日志

默认关闭调试日志，开发时可开启：

```kotlin
ScannerDebug.enabled = true
```

日志包含 Camera2 打开/关闭、分辨率选择、会话配置、二维码编解码参数、结果和异常。可以通过 `ScannerDebug.logger` 接入宿主日志系统。

## 公共 API

所有公共 API 都位于 `android.scanner.api`：

- `ScannerCamera`、`ScannerCameraConfig`
- `ScannerPreview`、`ScannerPreviewConfig`、`ScanRegion`
- `BarcodeCodec`、`BitmapSize`
- `ScanResult`、`ScanOutcome`、`ScannerError`
- `BarcodeFormat`、`ScannerConfig`、`ScanMode`、`ScannerState`、`ScannerController`
- `ScannerDebug`

## 生命周期

`ScannerCamera` 管理 Camera2 设备、采集会话、ImageReader 和后台线程。离开 Compose 或销毁页面时必须调用 `close()`。获得运行时相机权限前不要调用相机方法。

## 开源协议

MIT，详见 [LICENSE](./LICENSE) 和 [docs/NOTICE.zh-CN.md](./docs/NOTICE.zh-CN.md)。
