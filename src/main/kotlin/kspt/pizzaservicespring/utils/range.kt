package kspt.pizzaservicespring.utils

import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import kotlin.math.max
import kotlin.math.min

fun IntRange.intersect(other: IntRange): IntRange {
    val from = listOf(max(first, other.first), endInclusive, other.endInclusive).min()!!
    val to = listOf(min(endInclusive, other.endInclusive), first, other.first).max()!!
    return IntRange(from, to)
}

fun IntRange.toLong() = LongRange(first.toLong(), endInclusive.toLong())

fun ResponseEntity.BodyBuilder.contentRange(
        range: LongRange?,
        fullLength: Long? = null,
        unit: String = RangeUnits.Bytes.unitToken
) {
    header(HttpHeaders.CONTENT_RANGE, contentRangeHeaderValue(range, fullLength, unit))
    header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_RANGE)
}


/**
 * Possible content range units: bytes and none
 */
enum class RangeUnits {
    /**
     * Range unit `bytes`
     */
    Bytes,

    /**
     * Range unit `none`
     */
    None;

    /**
     * Lower-case unit name
     */
    val unitToken: String = name.toLowerCase()
}

/**
 * Format `Content-Range` header value
 */
fun contentRangeHeaderValue(
        range: LongRange?,
        fullLength: Long? = null,
        unit: RangeUnits = RangeUnits.Bytes
): String =
        contentRangeHeaderValue(range, fullLength, unit.unitToken)

/**
 * Format `Content-Range` header value
 */
fun contentRangeHeaderValue(
        range: LongRange?,
        fullLength: Long? = null,
        unit: String = RangeUnits.Bytes.unitToken
): String = buildString {
    append(unit)
    append(" ")
    if (range != null) {
        append(range.start)
        append('-')
        append(range.endInclusive)
    } else {
        append('*')
    }
    append('/')
    append(fullLength ?: "*")
}

