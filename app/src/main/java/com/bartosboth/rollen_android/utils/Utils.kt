package com.bartosboth.rollen_android.utils

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.math.roundToInt

@OptIn(ExperimentalEncodingApi::class)
fun convertBase64ToByteArr(base64: String): ByteArray {
    return Base64.decode(base64)
}

fun timeStampToDuration(position: Double): String {
    if (position < 0) return "--:--"
    val totalSeconds = position.roundToInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}