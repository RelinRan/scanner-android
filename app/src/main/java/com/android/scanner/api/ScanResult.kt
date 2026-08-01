package com.android.scanner.api

import android.graphics.Point
import android.graphics.Rect

public data class ScanResult(
    val rawValue: String?,
    val displayValue: String?,
    val rawBytes: ByteArray?,
    val format: BarcodeFormat,
    val valueType: BarcodeValueType = BarcodeValueType.Unknown,
    val boundingBox: Rect? = null,
    val cornerPoints: List<Point> = emptyList(),
) {
    override fun equals(other: Any?): Boolean =
        other is ScanResult &&
            rawValue == other.rawValue &&
            displayValue == other.displayValue &&
            rawBytes.contentEquals(other.rawBytes) &&
            format == other.format &&
            valueType == other.valueType &&
            boundingBox == other.boundingBox &&
            cornerPoints == other.cornerPoints

    override fun hashCode(): Int {
        var result = rawValue.hashCode()
        result = 31 * result + displayValue.hashCode()
        result = 31 * result + rawBytes.contentHashCode()
        result = 31 * result + format.hashCode()
        result = 31 * result + valueType.hashCode()
        result = 31 * result + boundingBox.hashCode()
        result = 31 * result + cornerPoints.hashCode()
        return result
    }
}

public enum class BarcodeValueType {
    Unknown,
    ContactInfo,
    Email,
    Isbn,
    Phone,
    Product,
    Sms,
    Text,
    Url,
    Wifi,
    Geo,
    CalendarEvent,
    DriverLicense,
}
