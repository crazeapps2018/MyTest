package com.it.mytest.util

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import androidx.recyclerview.widget.RecyclerView

object Utility {

    fun getNameFromUri(context: Context,uri: Uri?): String? {
        var fileName = ""
        var cursor: Cursor? = null
        cursor = context.contentResolver.query(
            uri!!, arrayOf(
                MediaStore.Images.ImageColumns.DISPLAY_NAME
            ), null, null, null
        )
        if (cursor != null && cursor.moveToFirst()) {
            fileName =
                cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME))
        }
        cursor?.close()
        return fileName
    }




}