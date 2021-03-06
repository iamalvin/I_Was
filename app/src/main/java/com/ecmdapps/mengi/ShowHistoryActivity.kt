package com.ecmdapps.mengi

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.format.DateUtils
import android.view.View
import android.view.ViewGroup
import android.widget.*

class ShowHistoryActivity: AppCompatActivity() {
    private var historyList = ArrayList<Source>()
    companion object {
        var sourceId: Long? = null
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_showhistory)
        try {
            sourceId = intent.getLongExtra(SourceDbManager.colId, 0L)
            if ( sourceId == 0L ) {
                loadQueryAll()
            } else {
                val sid:Long = sourceId ?: 0L
                loadQueryPerId(sid)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        val slv = findViewById<ListView>(R.id.sourceList)
        slv.adapter = SourceListAdapter(this, historyList)
        slv.onItemClickListener = AdapterView.OnItemClickListener{ _, _, position, _ ->
            loadInWebView(historyList[position])
        }
    }

    inner class SourceListAdapter(context: Context, private var historyList: ArrayList<Source>) : BaseAdapter() {
        private var context: Context? = context

        override fun getCount(): Int {
            return historyList.size        }

        override fun getItemId(position: Int): Long {
            return position.toLong()        }

        override fun getItem(position: Int): Any {
            return historyList[position]        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
            val view: View?
            val vh: ViewHolder
            val activity = context as Activity

            if (convertView == null) {
                view = activity.layoutInflater.inflate(R.layout.source, parent, false)
                vh = ViewHolder(view)
                view.tag = vh
            } else {
                view = convertView
                vh = view.tag as ViewHolder
            }

            val mSource = historyList[position]

            vh.lastViewTitle.text = mSource.lastViewTitle
            vh.sourceName.text = mSource.sourceName
            vh.lastViewTime.text = if (mSource.lastViewTime != 0L) DateUtils.getRelativeDateTimeString(context, mSource.lastViewTime!!, DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL) else "never viewed"
            val img : Bitmap = BitmapFactory.decodeByteArray(mSource.sourceImage!!, 0, mSource.sourceImage?.size!!)
            vh.ivFavicon.setImageBitmap(Bitmap.createScaledBitmap(img, img.width, img.height, false))

            vh.lastViewTitle.setOnClickListener {
                loadInWebView(mSource)
            }

            vh.ivFavicon.setOnClickListener {
                loadSourceInWebView(mSource)
            }

            vh.ivDelete.setOnClickListener {
                val dbManager = HistoryDbManager(this.context!!)
                val selectionArgs = arrayOf(mSource.id.toString())
                dbManager.delete("Id=?", selectionArgs)
                if ( sourceId == 0L ) {
                    loadQueryAll()
                } else {
                    val sid:Long = sourceId ?: 0L
                    loadQueryPerId(sid)
                }
            }

            vh.ivEdit.visibility = View.GONE

            return view
        }
    }

    private fun loadInWebView(source: Source) {
        val intent = Intent(this, WebviewActivity::class.java)
        val sid:Long = sourceId ?: 0L
        intent.putExtra(SourceDbManager.colId, sid)
        intent.putExtra(SourceDbManager.colLastViewTitle, source.lastViewTitle)
        intent.putExtra(SourceDbManager.colLastViewLink, source.lastViewLink)
        startActivity(intent)
    }

    private fun loadSourceInWebView(source: Source) {
        val intent = Intent(this, WebviewActivity::class.java)
        val sid:Long = sourceId ?: 0L
        intent.putExtra(SourceDbManager.colId, sid)
        intent.putExtra(SourceDbManager.colLastViewTitle, source.lastViewTitle)
        intent.putExtra(SourceDbManager.colLastViewLink, source.sourceLink)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        if ( sourceId == 0L ) {
            loadQueryAll()
        } else {
            val sid:Long = sourceId ?: 0L
            loadQueryPerId(sid)
        }
    }

    private fun loadQueryPerId(sourceId: Long) {
        val dbManager = HistoryDbManager(this)
        val cursor = dbManager.queryPerSourceId(sourceId)

        historyList.clear()

        if (cursor.count > 0) {
            cursor.moveToFirst()

            do {
                val id = cursor.getLong(cursor.getColumnIndex(SourceDbManager.colId))
                val sourceImage = cursor.getBlob(cursor.getColumnIndex(SourceDbManager.colSourceImage))
                val lastViewTitle = cursor.getString(cursor.getColumnIndex(SourceDbManager.colLastViewTitle))
                val lastViewLink = cursor.getString(cursor.getColumnIndex(SourceDbManager.colLastViewLink))
                val lastViewTime = cursor.getLong(cursor.getColumnIndex(SourceDbManager.colLastViewTime))

                historyList.add(Source(id, "", "", sourceImage, lastViewTitle, lastViewLink, lastViewTime))

            } while (cursor.moveToNext())
            cursor.close()
            dbManager.close()
        }
        val slv = findViewById<ListView>(R.id.sourceList)
        slv.adapter = SourceListAdapter(this, historyList)
    }

    fun loadQueryAll() {
        val dbManager = HistoryDbManager(this)
        val cursor = dbManager.queryAll()

        historyList.clear()

        if (cursor.count > 0) {
            cursor.moveToFirst()

            do {
                val id = cursor.getLong(cursor.getColumnIndex(SourceDbManager.colId))
                val sourceImage = cursor.getBlob(cursor.getColumnIndex(SourceDbManager.colSourceImage))
                val lastViewTitle = cursor.getString(cursor.getColumnIndex(SourceDbManager.colLastViewTitle))
                val lastViewLink = cursor.getString(cursor.getColumnIndex(SourceDbManager.colLastViewLink))
                val lastViewTime = cursor.getLong(cursor.getColumnIndex(SourceDbManager.colLastViewTime))

                historyList.add(Source(id, "", "", sourceImage, lastViewTitle, lastViewLink, lastViewTime))

            } while (cursor.moveToNext())
            cursor.close()
            dbManager.close()
        }
        val slv = findViewById<ListView>(R.id.sourceList)
        slv.adapter = SourceListAdapter(this, historyList)
    }

    private class ViewHolder(view: View?) {
        val ivEdit: ImageView = view?.findViewById(R.id.ivEdit) as ImageView
        val ivDelete: ImageView = view?.findViewById(R.id.ivDelete) as ImageView
        val ivFavicon: ImageView = view?.findViewById(R.id.ivFavicon) as ImageView
        val sourceName: TextView = view?.findViewById(R.id.sourceName) as TextView
        val lastViewTime: TextView = view?.findViewById(R.id.lastViewTime) as TextView
        val lastViewTitle: TextView = view?.findViewById(R.id.lastViewTitle) as TextView
    }
}