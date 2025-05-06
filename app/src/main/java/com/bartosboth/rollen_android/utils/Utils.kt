package com.bartosboth.rollen_android.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
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

@OptIn(ExperimentalEncodingApi::class)
fun convertBase64ToBitmap(base64String: String): Bitmap {
    val decodedBytes = Base64.decode(base64String)
    return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
}
