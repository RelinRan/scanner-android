package android.scanner.api

import android.util.Log

public object ScannerDebug {
    @Volatile
    public var enabled: Boolean = false

    public var logger: (tag: String, message: String, throwable: Throwable?) -> Unit = { tag, message, throwable ->
        if (throwable == null) Log.d(tag, message) else Log.e(tag, message, throwable)
    }

    internal fun log(tag: String, message: String) {
        if (enabled) logger(tag, message, null)
    }

    internal fun error(tag: String, message: String, throwable: Throwable? = null) {
        if (enabled) logger(tag, message, throwable)
    }
}
