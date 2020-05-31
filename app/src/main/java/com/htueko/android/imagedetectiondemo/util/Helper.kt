package com.htueko.android.imagedetectiondemo.util

import android.content.Context
import android.os.Build
import android.os.Environment
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

// to get the local
fun getCurrentLocale(context: Context): Locale? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        context.resources.configuration.locales[0]
    } else {
        context.resources.configuration.locale
    }
}

// to create image file
fun createImageFile(context: Context): File? {
    var file: File?
    file = try {
        // Create an image file name
        val local = getCurrentLocale(context)
        val timeStamp: String = SimpleDateFormat("yyyy.MM.dd-hh:mm:ss aaa", local!!).format(Date())
        val mFileName = "JPEG_${timeStamp}_"
        val storageDirectory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFile = File.createTempFile(mFileName, ".jpg", storageDirectory)
        imageFile
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
    return file
}