# Scanner Compose 扫码库

[English](./README.md)

基于 Kotlin、Camera2 和 Jetpack Compose，公共 API 位于 `android.scanner.api`。

## 功能

- Camera2 实时输出 Bitmap
- 自动选择最接近的摄像头支持分辨率
- 可配置扫描区域、遮罩颜色和边框的 Compose 覆盖层
- 二维码内容与 Bitmap 互转
- 可配置二维码输出尺寸
- 相机和编解码调试日志
- 最低支持 Android API 26

## 权限

宿主应用需要声明并动态申请相机权限：

```xml
<uses-permission android:name="android.permission.CAMERA" />
```

## 相机 Bitmap

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

`actualResolution` 可以获取摄像头最终采用的实际分辨率。

## Compose 覆盖层

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

## 二维码 Bitmap 编解码

```kotlin
val encoded = BarcodeCodec.encodeQr("scanner-value", BitmapSize(512, 512))
if (encoded is ScanOutcome.Success) {
    val decoded = BarcodeCodec.decode(encoded.value)
}
```

结果通过 `ScanOutcome` 和 `ScannerError` 返回，普通识别失败不会抛出异常。

## 调试日志

```kotlin
ScannerDebug.enabled = true
```

日志包含相机打开、分辨率选择、采集会话、二维码编码、解码和异常。可以替换 `ScannerDebug.logger` 接入宿主日志系统。

## 本地依赖

ZXing 本地 JAR 位于 `app/libs/core-3.5.3.jar`。构建 AAR：

```bash
./gradlew :app:assembleRelease
```

## 生命周期

页面或 Compose 销毁时调用 `ScannerPreview.close()`。获得运行时相机权限前不要启动相机。

## 开源协议

MIT，详见 [LICENSE](./LICENSE) 和 [docs/NOTICE.zh-CN.md](./docs/NOTICE.zh-CN.md)。
