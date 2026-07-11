# Scanner

[English](./README.md) | **简体中文**

基于 [ZXing](https://github.com/zxing/zxing) `core-3.4.1` 构建的 Android 条形码 / 二维码扫描与生成库。提供开箱即用的相机扫码 `View`、识别率优化后的解码管线，以及创建条形码、二维码和从图片解码的工具方法。

## 功能

- 相机实时扫描条形码与二维码
- 从图片文件或 `Bitmap` 解码条形码 / 二维码
- 创建二维码与条形码（CODE_128 等）
- 识别率算法优化及图形变形问题处理
- 扫码视图可定制：背景颜色、角标样式、扫描线、动画时长
- 内置手电筒开关、暂停 / 恢复解码、焦点传感器与扫码振动

## 预览

![预览](./ic_preview.png)

## 集成

### 方式一 — 从 GitHub Releases 下载 AAR

在 [Releases 页面](https://github.com/RelinRan/Scanner/releases) 下载最新的 `scanner-<version>.aar`，放入模块的 `libs/` 目录，然后在 `app/build.gradle` 中配置：

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

### 方式二 — JitPack

在根目录 `build.gradle` / `settings.gradle` 中添加 JitPack 仓库：

```groovy
allprojects {
    repositories {
        // ...
        maven { url 'https://jitpack.io' }
    }
}
```

然后在 `app/build.gradle` 中添加依赖：

```groovy
dependencies {
    implementation 'com.github.RelinRan:Scanner:1.1.0'
}
```

## 权限

> **注意：** Android 6.0（API 23）及以上需动态申请 `CAMERA` 权限。

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.VIBRATE" />
```

## 用法

### ScanCodeView

```xml
<com.android.zxing.view.ScanCodeView
    android:id="@+id/scan_code"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

### 扫码监听

```java
ScanCodeView scanCode = findViewById(R.id.scan_code);
scanCode.setOnScanCodeListener(new OnScanCodeListener() {
    @Override
    public void onScanCodeSucceed(Result result) {
        String code = result.getText();
    }

    @Override
    public void onScanCodeFailed(Exception exception) {
        // 当前帧未识别到结果时回调
    }
});
```

### 手电筒

```java
ScanCodeView scanCode = findViewById(R.id.scan_code);
scanCode.toggleTorch();
```

### 暂停 / 恢复解码

```java
ScanCodeView scanCode = findViewById(R.id.scan_code);
scanCode.onPause();   // 暂停解码
scanCode.onResume();  // 恢复解码
```

### 创建二维码

```java
Bitmap qrCode = ZXWriter.createQRCode("content");
```

### 创建条形码

```java
Bitmap barCode = ZXWriter.createCode(BarcodeFormat.CODE_128, "content", 300, 150);
```

### 从图片文件解码

```java
File file = new File("/sdcard/Download/0001.png");
ZXReader.fromFile(file, new OnScanCodeListener() {
    @Override
    public void onScanCodeSucceed(Result result) {
        String code = result.getText();
    }

    @Override
    public void onScanCodeFailed(Exception exception) {
        // 解码失败
    }
});
```

## 属性

| 属性 | 格式 | 说明 |
|---|---|---|
| `areaCenterX` | dimension &#124; reference | 扫描中心 X |
| `areaCenterY` | dimension &#124; reference | 扫描中心 Y |
| `areaWidth` | dimension &#124; reference | 扫描宽度 |
| `areaHeight` | dimension &#124; reference | 扫描高度 |
| `backgroundColor` | color &#124; reference | 扫描背景颜色 |
| `cornerVisible` | boolean &#124; reference | 角标可见性 |
| `cornerLineColor` | color &#124; reference | 角标线条颜色 |
| `cornerLineMargin` | dimension &#124; reference | 角标线条间距 |
| `cornerLineLength` | dimension &#124; reference | 角标线条长度 |
| `cornerLineWidth` | dimension &#124; reference | 角标线条宽度 |
| `duration` | integer &#124; reference | 扫描线动画时长（毫秒） |
| `lineDrawable` | integer &#124; reference | 扫描线资源 |
| `vibrator` | boolean &#124; reference | 扫码成功是否振动 |

## 版本

本项目遵循 [语义化版本 2.0.0](https://semver.org/lang/zh-CN/)。可用版本见 [发布页面](https://github.com/RelinRan/Scanner/releases)，发布历史见 [更新日志](./CHANGELOG.md)。

## 开源协议

基于 [MIT 协议](./LICENSE) 开源。本库打包并依赖第三方软件，致谢与第三方协议详见 [软件声明](./docs/NOTICE.zh-CN.md)。
