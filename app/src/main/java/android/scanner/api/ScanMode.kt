package android.scanner.api

public sealed interface ScanMode {
    public data object Single : ScanMode
    public data object Continuous : ScanMode
}
