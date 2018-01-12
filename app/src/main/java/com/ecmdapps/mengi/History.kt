package com.ecmdapps.mengi

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

class History(val context: Context) {
    fun show():Boolean{
        val intent = Intent(context, ShowHistoryActivity::class.java)
        context.startActivity(intent)
        return true
    }

    fun showWithId(Id:Long):Boolean {
        val intent = Intent(context, ShowHistoryActivity::class.java)
        intent.putExtra(SourceDbManager.colId, Id)
        context.startActivity(intent)
        return true
    }

    fun store(url: String?, sourceId: Long, pageID: String, favicon: Bitmap?, currentTitle: String?) : Long {
        val title = currentTitle ?: url
        val fav = favicon ?: BitmapFactory.decodeResource(context.resources, R.drawable.default_favicon)

        val stream = ByteArrayOutputStream()
        fav.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val fByteArray = stream.toByteArray()

        val values = ContentValues()

        values.put(SourceDbManager.colId, sourceId)
        values.put(SourceDbManager.colLastViewLink, url!!)
        values.put(SourceDbManager.colLastViewId, pageID)
        values.put(SourceDbManager.colSourceImage, fByteArray)
        values.put(SourceDbManager.colLastViewTitle, title)
        values.put(SourceDbManager.colLastViewTime, System.currentTimeMillis())

        val dbManager = SourceDbManager(context)
        val newId = dbManager.add(values)
        dbManager.close()

        val webHistory = HistoryDbManager(context)
        webHistory.add(values)
        webHistory.close()

        return newId
    }
}