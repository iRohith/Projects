package com.rohith.vsa_kotlin.util

import java.util.*

val suffixes: TreeMap<Long, String> = TreeMap(mapOf(
    1_000L to "k",
    1_000_000L to "M",
    1_000_000_000L to "B",
    1_000_000_000_000L to "T"
))

fun parseInt(value: Long) : String {
    if (value == Long.MIN_VALUE) return parseInt(Long.MIN_VALUE + 1)
    if (value < 0) return "-" + parseInt(-value)
    if (value < 1000) return value.toString() //deal with easy case

    val e = suffixes.floorEntry(value)!!
    val divideBy = e.key
    val suffix = e.value

    val truncated = value / (divideBy / 10) //the number part of the output times 10

    val hasDecimal =
        truncated < 100 && truncated / 10.0 != (truncated / 10).toDouble()
    return if (hasDecimal) (truncated / 10.0).toString() + suffix else (truncated / 10).toString() + suffix
}

class DataParcel (
    var data: Any? = null
)
/*
inline fun View.afterMeasured(crossinline block: () -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if (measuredWidth > 0 && measuredHeight > 0) {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                block()
            }
        }
    })
}*/