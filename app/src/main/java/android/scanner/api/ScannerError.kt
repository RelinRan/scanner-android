package android.scanner.api

public sealed interface ScannerError {
    public val cause: Throwable?

    public data object PermissionMissing : ScannerError {
        override val cause: Throwable? = null
    }

    public data object CameraUnavailable : ScannerError {
        override val cause: Throwable? = null
    }

    public data object TorchUnavailable : ScannerError {
        override val cause: Throwable? = null
    }

    public data class CameraBindingFailed(override val cause: Throwable) : ScannerError
    public data class RecognitionFailed(override val cause: Throwable) : ScannerError
    public data class ImageLoadingFailed(override val cause: Throwable) : ScannerError
    public data class EncodingFailed(override val cause: Throwable) : ScannerError
    public data class InvalidInput(val message: String) : ScannerError {
        override val cause: Throwable? = null
    }
}
