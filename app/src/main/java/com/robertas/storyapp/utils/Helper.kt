package com.robertas.storyapp.utils

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Environment
import com.robertas.storyapp.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

private const val FILENAME_FORMAT = "dd-MMM-yyyy"

private const val DATETIME_FORMAT = "yyyy-MM-dd'T'hh:mm:ss.SSS"

const val DATETIME_UI_FORMAT = "dd-MMM-yyyy hh:mm:ss"

val timeStamp: String = formatTime(System.currentTimeMillis(), FILENAME_FORMAT)

fun parseTime(unformattedTime: String): Date?{

    val formatter = SimpleDateFormat(
        DATETIME_FORMAT,
        Locale.US)

    return formatter.parse(unformattedTime)
}


fun formatTime(unformattedTime: Long, format: String): String {
    return SimpleDateFormat(
        format,
        Locale.US
    ).format(unformattedTime)
}

fun createTempFile(context: Context): File {
    val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(timeStamp, ".jpg", storageDir)
}

fun createFile(application: Application): File {
    val mediaDir = application.externalMediaDirs.firstOrNull()?.let {
        File(it, application.resources.getString(R.string.app_name)).apply { mkdirs() }
    }

    val outputDirectory = if (
        mediaDir != null && mediaDir.exists()
    ) mediaDir else application.filesDir

    return File(outputDirectory, "$timeStamp.jpg")
}

fun uriToFile(selectedImg: Uri, context: Context): File {
    val contentResolver: ContentResolver = context.contentResolver
    val myFile = createTempFile(context)

    val inputStream = contentResolver.openInputStream(selectedImg) as InputStream
    val outputStream: OutputStream = FileOutputStream(myFile)
    val buf = ByteArray(1024)
    var len: Int
    while (inputStream.read(buf).also { len = it } > 0) outputStream.write(buf, 0, len)
    outputStream.close()
    inputStream.close()

    return myFile
}

fun reduceThenRotateFileImage(file: File, degree: Float): File {

    val matrix = Matrix()

    val bitmap = BitmapFactory.decodeFile(file.path)

    var compressQuality = 100

    var streamLength: Int

    bitmap?.let {
        do {
            val bmpStream = ByteArrayOutputStream()

            bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)

            val bmpPicByteArray = bmpStream.toByteArray()

            streamLength = bmpPicByteArray.size

            compressQuality -= 5

        } while (streamLength > 1000000)

        matrix.postRotate(degree % 360)

        val newBitmap = Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
        newBitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, FileOutputStream(file))
    }

    return file
}

suspend fun rotateBitmap(file: File, rotation: Float): Bitmap{

    val result : Bitmap

    withContext(Dispatchers.IO){
        val matrix = Matrix()

        val bitmap = BitmapFactory.decodeFile(file.path)

        matrix.postRotate(rotation % 360)

        result = Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    }

    return result
}