package android.scanner.api

public sealed interface ScanOutcome<out T> {
    public data class Success<T>(val value: T) : ScanOutcome<T>
    public data class Failure(val error: ScannerError) : ScanOutcome<Nothing>
}
