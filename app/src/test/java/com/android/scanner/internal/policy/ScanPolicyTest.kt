package com.android.scanner.internal.policy

import com.android.scanner.api.BarcodeFormat
import com.android.scanner.api.ScanMode
import com.android.scanner.api.ScanResult
import kotlin.time.Duration.Companion.milliseconds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScanPolicyTest {
    private var now = 1_000L

    @Test
    fun `single mode delivers first result and requests pause`() {
        val policy = ScanPolicy(ScanMode.Single, 1_500.milliseconds) { now }

        val decision = policy.evaluate(result("value"))

        assertTrue(decision is PolicyDecision.Deliver)
        assertTrue((decision as PolicyDecision.Deliver).pauseAfterDelivery)
    }

    @Test
    fun `continuous mode delivers different values immediately`() {
        val policy = ScanPolicy(ScanMode.Continuous, 1_500.milliseconds) { now }

        assertTrue(policy.evaluate(result("one")) is PolicyDecision.Deliver)
        assertTrue(policy.evaluate(result("two")) is PolicyDecision.Deliver)
    }

    @Test
    fun `continuous mode suppresses duplicate value and format within window`() {
        val policy = ScanPolicy(ScanMode.Continuous, 1_500.milliseconds) { now }

        policy.evaluate(result("same"))
        now += 1_499

        assertEquals(PolicyDecision.Suppress, policy.evaluate(result("same")))
    }

    @Test
    fun `continuous mode treats format as part of duplicate key`() {
        val policy = ScanPolicy(ScanMode.Continuous, 1_500.milliseconds) { now }

        policy.evaluate(result("123", BarcodeFormat.Code128))

        assertTrue(policy.evaluate(result("123", BarcodeFormat.QrCode)) is PolicyDecision.Deliver)
    }

    @Test
    fun `continuous mode delivers duplicate after window expires`() {
        val policy = ScanPolicy(ScanMode.Continuous, 1_500.milliseconds) { now }

        policy.evaluate(result("same"))
        now += 1_500

        assertTrue(policy.evaluate(result("same")) is PolicyDecision.Deliver)
    }

    @Test
    fun `reset clears duplicate history`() {
        val policy = ScanPolicy(ScanMode.Continuous, 1_500.milliseconds) { now }

        policy.evaluate(result("same"))
        policy.reset()

        assertFalse(policy.evaluate(result("same")) is PolicyDecision.Suppress)
    }

    private fun result(
        value: String,
        format: BarcodeFormat = BarcodeFormat.QrCode,
    ): ScanResult = ScanResult(
        rawValue = value,
        displayValue = value,
        rawBytes = null,
        format = format,
    )
}
