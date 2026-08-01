package com.android.scanner.api

public sealed interface ScannerState {
    public data object Idle : ScannerState
    public data object Starting : ScannerState
    public data object Scanning : ScannerState
    public data object Paused : ScannerState
    public data object PermissionRequired : ScannerState
    public data class Error(val error: ScannerError) : ScannerState
}
