package android.scanner.api

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.view.Surface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

public data class ScannerPreviewConfig(
    val cameraId: String = "0",
    val width: Int = 1280,
    val height: Int = 720,
    val torchEnabled: Boolean = false,
    val autoFocus: Boolean = true,
) {
    init {
        require(width > 0 && height > 0)
    }
}

public class ScannerPreview(
    context: Context,
    private val config: ScannerPreviewConfig = ScannerPreviewConfig(),
) : AutoCloseable {
    private companion object { const val TAG = "ScannerPreview" }
    private val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val _bitmap = MutableStateFlow<Bitmap?>(null)
    public val bitmap: StateFlow<Bitmap?> = _bitmap.asStateFlow()
    private val thread = HandlerThread("camera-bitmap").apply { start() }
    private val handler = Handler(thread.looper)
    private var device: CameraDevice? = null
    private var session: CameraCaptureSession? = null
    private var reader: ImageReader? = null
    public var actualResolution: Size? = null
        private set

    @SuppressLint("MissingPermission")
    public fun start(surface: Surface? = null) {
        ScannerDebug.log(TAG, "start cameraId=${config.cameraId}, requested=${config.width}x${config.height}, torch=${config.torchEnabled}, autoFocus=${config.autoFocus}")
        if (device != null) { ScannerDebug.log(TAG, "start ignored: camera already open"); return }
        manager.openCamera(config.cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                ScannerDebug.log(TAG, "camera opened id=${camera.id}")
                device = camera
                createSession(camera, surface)
            }
            override fun onDisconnected(camera: CameraDevice) { ScannerDebug.log(TAG, "camera disconnected id=${camera.id}"); close() }
            override fun onError(camera: CameraDevice, error: Int) { ScannerDebug.error(TAG, "camera error=$error id=${camera.id}"); close() }
        }, handler)
    }

    public fun stop() {
        ScannerDebug.log(TAG, "stop actualResolution=$actualResolution")
        session?.close(); session = null
        device?.close(); device = null
        reader?.close(); reader = null
        _bitmap.value = null
        actualResolution = null
    }

    override fun close() {
        stop()
        thread.quitSafely()
    }

    private fun createSession(camera: CameraDevice, surface: Surface?) {
        val characteristics = manager.getCameraCharacteristics(config.cameraId)
        val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val supported = map?.getOutputSizes(ImageFormat.JPEG).orEmpty().toList()
        val selected = ScannerCameraResolutionSelector.select(supported, Size(config.width, config.height))
            ?: Size(config.width, config.height)
        actualResolution = selected
        ScannerDebug.log(TAG, "selected resolution requested=${config.width}x${config.height}, actual=${selected.width}x${selected.height}, supportedCount=${supported.size}")
        val imageReader = ImageReader.newInstance(selected.width, selected.height, ImageFormat.JPEG, 2)
        reader = imageReader
        imageReader.setOnImageAvailableListener({ source ->
            source.acquireLatestImage()?.use { image ->
                val buffer = image.planes[0].buffer
                val bytes = ByteArray(buffer.remaining()).also(buffer::get)
                _bitmap.value = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }
        }, handler)
        val outputs = listOf(imageReader.surface) + listOfNotNull(surface)
        camera.createCaptureSession(outputs, object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(cameraSession: CameraCaptureSession) {
                session = cameraSession
                ScannerDebug.log(TAG, "capture session configured targets=${outputs.size}")
                val request = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                    addTarget(imageReader.surface)
                    surface?.let(::addTarget)
                    set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
                    if (config.autoFocus) set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                    if (config.torchEnabled) set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH)
                }.build()
                cameraSession.setRepeatingRequest(request, null, handler)
                ScannerDebug.log(TAG, "repeating preview request started")
            }
            override fun onConfigureFailed(session: CameraCaptureSession) { ScannerDebug.error(TAG, "capture session configuration failed"); close() }
        }, handler)
    }

}

internal object ScannerCameraResolutionSelector {
    fun select(supported: List<Size>, requested: Size): Size? {
        if (supported.isEmpty()) return null
        return supported.minWithOrNull(
            compareBy<Size> {
                val requestedRatio = requested.width.toDouble() / requested.height
                val ratio = it.width.toDouble() / it.height
                kotlin.math.abs(ratio - requestedRatio)
            }.thenBy {
                kotlin.math.abs(it.width - requested.width) + kotlin.math.abs(it.height - requested.height)
            },
        )
    }
}
