package com.example.janbarktask.viewModels

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.media.ImageReader
import android.media.MediaScannerConnection
import android.media.projection.MediaProjection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.core.database.getLongOrNull
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ads.myExtensios.showToast
import com.example.janbarktask.R
import com.example.janbarktask.utills.MyFileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GalleryViewModel(private val appContext: Context) : ViewModel() {

    fun takeScreenshot(mediaProjection: MediaProjection?) {
        mediaProjection?.let {
            val metrics = appContext.resources.displayMetrics
            val width = metrics.widthPixels
            val height = metrics.heightPixels
            val density = metrics.densityDpi

            val imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 1)
            val handlerThread = HandlerThread("ImageReaderThread").apply { start() }
            val handler = Handler(handlerThread.looper)

            val virtualDisplay = mediaProjection.createVirtualDisplay(
                "Screenshot", width, height, density,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.surface, null, handler
            )

            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val bitmap = imageReader.awaitImageAvailable(width, height, handler)
                    saveBitmapWithMediaStore(bitmap)
                } catch (e: Exception) {
                    Log.e("GalleryViewModel", "Error taking screenshot: ${e.message}", e)
                } finally {
                    imageReader.close()
                    virtualDisplay.release()
                    handlerThread.quitSafely()
                }
            }
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private suspend fun ImageReader.awaitImageAvailable(
        screenWidth: Int,
        screenHeight: Int,
        handler: Handler
    ): Bitmap = suspendCancellableCoroutine { cont ->
        var continuationResumed = false

        try {
            setOnImageAvailableListener({ reader ->
                val image = reader.acquireLatestImage()
                if (image == null) {
                    if (!continuationResumed) {
                        continuationResumed = true
                        cont.resumeWith(Result.failure(Throwable("Failed to get screen image")))
                    }
                    return@setOnImageAvailableListener
                }

                val planes = image.planes
                val buffer = planes[0].buffer

                val pixelStride = planes[0].pixelStride
                val rowStride = planes[0].rowStride
                val rowPadding = rowStride - pixelStride * screenWidth

                var bitmap = Bitmap.createBitmap(
                    screenWidth + rowPadding / pixelStride,
                    screenHeight,
                    Bitmap.Config.ARGB_8888
                )
                bitmap.copyPixelsFromBuffer(buffer)
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, screenWidth, screenHeight)
                image.close()

                if (!bitmap.isEmptyBitmap()) {
                    if (!continuationResumed) {
                        continuationResumed = true
                        setOnImageAvailableListener(null, null)
                        cont.resume(bitmap) {}
                    }
                } else {
                    if (!continuationResumed) {
                        continuationResumed = true
                        cont.resumeWith(Result.failure(Throwable("Bitmap is empty")))
                    }
                }
            }, handler)
        } catch (e: Exception) {
            Log.e("TAG000", "awaitImageAvailable: ${e.message}")
        }

//        cont.invokeOnCancellation { setOnImageAvailableListener(null, null) }
    }


    private fun Bitmap.isEmptyBitmap(): Boolean {
        val emptyBitmap = Bitmap.createBitmap(width, height, config)
        return this.sameAs(emptyBitmap)
    }


    private val _savedImageUriLatest = MutableStateFlow<Uri?>(null)
    val savedImageUriLatest: StateFlow<Uri?> get() = _savedImageUriLatest

    private suspend fun saveBitmapWithMediaStore(resource: Bitmap) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(
                        MediaStore.MediaColumns.DISPLAY_NAME,
                        getCurrentTime()
                    )
                    put(
                        MediaStore.MediaColumns.MIME_TYPE,
                        "image/jpeg"
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(
                            MediaStore.MediaColumns.RELATIVE_PATH,
                            Environment.DIRECTORY_PICTURES + File.separator + appContext.getString(R.string.folder_name)
                        )
                    } else {
                        put(
                            MediaStore.MediaColumns.DATA,
                            Environment.DIRECTORY_PICTURES + File.separator + appContext.getString(
                                R.string.folder_name
                            )
                        )
                    }
                }

                with(appContext.contentResolver) {
                    this?.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        values
                    )?.also {
                        Log.e("TAG00000", "saveBitmapWithMediaStore: dfskfdskfd")
                        _savedImageUriLatest.value = it
                        openOutputStream(it)?.use { stream ->
                            if (!resource.compress(
                                    Bitmap.CompressFormat.JPEG,
                                    100,
                                    stream
                                )
                            ) {
                            }
                            it
                        }
                    }
                }
            } else {
                val savePath =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        .toString() + File.separator + appContext.getString(R.string.folder_name)
                val file = File(savePath)
                if (!file.exists()) file.mkdirs()
                val imageFile = File(file.absolutePath, "${getCurrentTime()}.jpg")
                withContext(Dispatchers.IO) {
                    FileOutputStream(imageFile).use {
                        resource.compress(Bitmap.CompressFormat.PNG, 100, it)
                    }
                }
                MediaScannerConnection.scanFile(
                    appContext,
                    arrayOf(imageFile.toString()),
                    null
                ) { _, _ -> }
                _savedImageUriLatest.value = savePath.toUri()
            }
        } catch (ex: IOException) {
            _savedImageUriLatest.value?.let { orphanUri ->
                appContext.contentResolver.delete(
                    orphanUri,
                    null,
                    null
                )
                _savedImageUriLatest.value = null
            }
        } catch (ex: FileNotFoundException) {
            _savedImageUriLatest.value?.let { orphanUri ->
                appContext.contentResolver.delete(
                    orphanUri,
                    null,
                    null
                )
                _savedImageUriLatest.value = null
            }
        } catch (ex: Exception) {
            _savedImageUriLatest.value = null
            Log.d("Mehran", "saveBitmap1122: ${ex.message}")
        }
    }


    private fun scanFile(context: Context, path: String) {
        MediaScannerConnection.scanFile(
            context,
            arrayOf<String>(path), null
        ) { newPath, _ ->
        }
    }


    private val _imageList: MutableLiveData<List<Uri>> = MutableLiveData()
    val imageList: LiveData<List<Uri>> get() = _imageList

    fun getImages() {
        val imageList = ArrayList<Uri>()
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL
            )
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA
        )
        val path = "${Environment.DIRECTORY_PICTURES}/${appContext.getString(R.string.folder_name)}"
        val selection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.ImageColumns.RELATIVE_PATH + " like ? "
        } else {
            MediaStore.Images.Media.DATA + " LIKE ?"
        }
        val selectionArgs = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf("%$path%")
        } else {
            arrayOf(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .toString() + File.separator + appContext.getString(R.string.folder_name) + "%"
            )
        }

        appContext.contentResolver.query(uri, projection, selection, selectionArgs, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val columnIndexId = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                    //val columnIndexData = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    do {
                        val imageId: Long? = cursor.getLongOrNull(columnIndexId)
                        //val imagePath: Long? = cursor.getLongOrNull(columnIndexData)
                        imageId?.let {
                            imageList.add(
                                ContentUris.withAppendedId(
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    it
                                )
                            )
                        }
                    } while (cursor.moveToNext())
                }
            }
        _imageList.postValue(imageList.toList().reversed())
    }

    fun deleteExternalImage(uri: Uri) {
        if (appContext.contentResolver.delete(uri, null, null) > 0) {
            val temp = _imageList.value?.toMutableList()
            temp?.apply {
                remove(uri)
                _imageList.postValue(toList())
            }
            appContext.showToast("Deleted...!")
        } else {
            appContext.showToast("Failed...!")
        }
    }

    private fun getCurrentTime(): String {
        val dateFormat = SimpleDateFormat("HH_mm_ss", Locale.getDefault())
        val currentTime = Date()
        return dateFormat.format(currentTime)
    }

}