package com.example.janbarktask.utills

import android.content.Context
import android.os.Environment
import android.text.TextUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object MyFileUtils {

    fun getFolderName(name: String): String {
        val mediaStorageDir = File(Environment.getExternalStorageDirectory().absolutePath, name)
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return ""
            }
        }
        return mediaStorageDir.absolutePath
    }

    private fun isSDAvailable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    fun getNewFile(context: Context, folderName: String): File? {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd-hh-mm-ss-a", Locale.US)
        val timeStamp = simpleDateFormat.format(Date())

        val path = if (isSDAvailable()) {
            getFolderName(folderName) + File.separator + timeStamp + ".jpg"
        } else {
            context.filesDir.path + File.separator + folderName + File.separator + timeStamp + ".jpg"
        }

        if (TextUtils.isEmpty(path)) {
            return null
        }

        return File(path)
    }
}
