package com.struperto.androidappdays.data.repository

import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import java.time.Instant

data class MediaItem(
    val uri: String,
    val displayName: String,
    val dateTaken: Instant?,
    val width: Int,
    val height: Int,
)

interface ScreenshotRepository {
    suspend fun loadRecentScreenshots(limit: Int = 20): List<MediaItem>
}

class DeviceScreenshotRepository(
    private val context: Context,
) : ScreenshotRepository {
    override suspend fun loadRecentScreenshots(limit: Int): List<MediaItem> {
        val items = mutableListOf<MediaItem>()
        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
        )
        val selection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?"
        } else {
            "${MediaStore.Images.Media.DATA} LIKE ?"
        }
        val selectionArgs = arrayOf("%Screenshot%")
        val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"
        context.contentResolver.query(
            collection, projection, selection, selectionArgs, sortOrder,
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
            val widthCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
            val heightCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
            var count = 0
            while (cursor.moveToNext() && count < limit) {
                val id = cursor.getLong(idCol)
                items += MediaItem(
                    uri = ContentUris.withAppendedId(collection, id).toString(),
                    displayName = cursor.getString(nameCol) ?: "",
                    dateTaken = cursor.getLong(dateCol).takeIf { it > 0 }?.let { Instant.ofEpochMilli(it) },
                    width = cursor.getInt(widthCol),
                    height = cursor.getInt(heightCol),
                )
                count++
            }
        }
        return items
    }
}
