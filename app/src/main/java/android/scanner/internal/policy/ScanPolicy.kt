package android.scanner.internal.policy

import android.scanner.api.BarcodeFormat
import android.scanner.api.ScanMode
import android.scanner.api.ScanResult
import kotlin.time.Duration

internal sealed interface PolicyDecision {
    data class Deliver(val pauseAfterDelivery: Boolean) : PolicyDecision
    data object Suppress : PolicyDecision
}

internal class ScanPolicy(
    private val mode: ScanMode,
    duplicateWindow: Duration,
    private val nowMillis: () -> Long = System::currentTimeMillis,
) {
    private val duplicateWindowMillis = duplicateWindow.inWholeMilliseconds
    private val deliveredAt = mutableMapOf<ResultKey, Long>()

    fun evaluate(result: ScanResult): PolicyDecision {
        if (mode == ScanMode.Single) {
            return PolicyDecision.Deliver(pauseAfterDelivery = true)
        }

        val now = nowMillis()
        deliveredAt.entries.removeAll { now - it.value >= duplicateWindowMillis }
        val key = result.key()
        val previousDelivery = deliveredAt[key]
        if (previousDelivery != null && now - previousDelivery < duplicateWindowMillis) {
            return PolicyDecision.Suppress
        }

        deliveredAt[key] = now
        return PolicyDecision.Deliver(pauseAfterDelivery = false)
    }

    fun reset() {
        deliveredAt.clear()
    }

    private fun ScanResult.key(): ResultKey = ResultKey(
        format = format,
        value = rawValue ?: displayValue ?: rawBytes?.contentHashCode()?.toString().orEmpty(),
    )

    private data class ResultKey(
        val format: BarcodeFormat,
        val value: String,
    )
}
