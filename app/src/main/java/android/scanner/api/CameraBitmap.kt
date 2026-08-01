package android.scanner.api

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Rect
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

public data class CameraBitmapConfig(
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

public class CameraBitmap(
    context: Context,
    private val config: CameraBitmapConfig = CameraBitmapConfig(),
) : AutoCloseable {
    private val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val _bitmap = MutableStateFlow<Bitmap?>(null)
    public val bitmap: StateFlow<Bitmap?> = _bitmap.asStateFlow()
    private val thread = HandlerThread("camera-bitmap").apply { start() }
    private val handler = Handler(thread.looper)
    private var device: CameraDevice? = null
    private var session: CameraCaptureSession? = null
    private var reader: ImageReader? = null

    @SuppressLint("MissingPermission")
    public fun start(surface: Surface? = null) {
        if (device != null) return
        manager.openCamera(config.cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                device = camera
                createSession(camera, surface)
            }
            override fun onDisconnected(camera: CameraDevice) = close()
            override fun onError(camera: CameraDevice, error: Int) = close()
        }, handler)
    }

    public fun stop() {
        session?.close(); session = null
        device?.close(); device = null
        reader?.close(); reader = null
        _bitmap.value = null
    }

    override fun close() {
        stop()
        thread.quitSafely()
    }

    private fun createSession(camera: CameraDevice, surface: Surface?) {
        val imageReader = ImageReader.newInstance(config.width, config.height, ImageFormat.JPEG, 2)
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
                val request = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                    addTarget(imageReader.surface)
                    surface?.let(::addTarget)
                    set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
                    if (config.autoFocus) set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                    if (config.torchEnabled) set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH)
                }.build()
                cameraSession.setRepeatingRequest(request, null, handler)
            }
            override fun onConfigureFailed(session: CameraCaptureSession) = close()
        }, handler)
    }
}
