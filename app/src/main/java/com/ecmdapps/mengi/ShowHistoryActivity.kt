package com.ecmdapps.mengi

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.format.DateUtils
import android.view.View
import android.view.ViewGroup
import android.widget.*


class ShowHistoryActivity: AppCompatActivity() {
    private var sourcesList = ArrayList<Source>()
    companion object {
        var sourceId: Long? = null
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_showhistory)
        try {
            sourceId = intent.getLongExtra(SourceDbManager.colId, 0)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        if ( sourceId == null ) {
            loadQueryAll()
        } else {
            loadQueryPerId(sourceId!!)
        }
        val slv = findViewById<ListView>(R.id.sourceList)
        slv.adapter = SourceListAdapter(this, sourcesList)
        slv.onItemClickListener = AdapterView.OnItemClickListener{ _, _, position, _ ->
            loadInWebView(sourcesList[position])
        }
    }

    inner class SourceListAdapter(context: Context, private var sourcesList: ArrayList<Source>) : BaseAdapter() {
        private var context: Context? = context

        override fun getCount(): Int {
            return sourcesList.size        }

        override fun getItemId(position: Int): Long {
            return position.toLong()        }

        override fun getItem(position: Int): Any {
            return sourcesList[position]        }

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

            val mSource = sourcesList[position]

            vh.lastViewTitle.text = mSource.lastViewTitle
            vh.sourceName.text = mSource.sourceName
            vh.lastViewTime.text = if (mSource.lastViewTime != 0L) DateUtils.getRelativeDateTimeString(context, mSource.lastViewTime!!, DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL) else "never viewed"

            vh.lastViewTitle.setOnClickListener {
                loadInWebView(mSource)
            }

            vh.ivDelete.setOnClickListener {
                val dbManager = HistoryDbManager(this.context!!)
                val selectionArgs = arrayOf(mSource.id.toString())
                dbManager.delete("Id=?", selectionArgs)
                loadQueryAll()
            }

            vh.ivEdit.visibility = View.GONE

            return view
        }
    }

    private fun loadInWebView(source: Source) {
        val intent = Intent(this, WebviewActivity::class.java)
        intent.putExtra(SourceDbManager.colId, source.id)
        intent.putExtra(SourceDbManager.colLastViewTitle, source.lastViewTitle)
        intent.putExtra(SourceDbManager.colLastViewLink, source.lastViewLink)

        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        loadQueryAll()
    }

    private fun loadQueryPerId(sourceId: Long) {
        val dbManager = HistoryDbManager(this)
        val cursor = dbManager.queryPerSourceId(sourceId)

        sourcesList.clear()

        if (cursor.count > 0) {
            cursor.moveToFirst()

            do {
                val id = cursor.getLong(cursor.getColumnIndex(SourceDbManager.colId))
                val sourceImage = cursor.getBlob(cursor.getColumnIndex(SourceDbManager.colSourceImage))
                val lastViewTitle = cursor.getString(cursor.getColumnIndex(SourceDbManager.colLastViewTitle))
                val lastViewLink = cursor.getString(cursor.getColumnIndex(SourceDbManager.colLastViewLink))
                val lastViewTime = cursor.getLong(cursor.getColumnIndex(SourceDbManager.colLastViewTime))

                sourcesList.add(Source(id, "", "", sourceImage, lastViewTitle, lastViewLink, lastViewTime))

            } while (cursor.moveToNext())
            cursor.close()
        }
        val slv = findViewById<ListView>(R.id.sourceList)
        slv.adapter = SourceListAdapter(this, sourcesList)
    }

    fun loadQueryAll() {
        val dbManager = HistoryDbManager(this)
        val cursor = dbManager.queryAll()

        sourcesList.clear()

        if (cursor.count > 0) {
            cursor.moveToFirst()

            do {
                val id = cursor.getLong(cursor.getColumnIndex(SourceDbManager.colId))
                val sourceImage = cursor.getBlob(cursor.getColumnIndex(SourceDbManager.colSourceImage))
                val lastViewTitle = cursor.getString(cursor.getColumnIndex(SourceDbManager.colLastViewTitle))
                val lastViewLink = cursor.getString(cursor.getColumnIndex(SourceDbManager.colLastViewLink))
                val lastViewTime = cursor.getLong(cursor.getColumnIndex(SourceDbManager.colLastViewTime))

                sourcesList.add(Source(id, "", "", sourceImage, lastViewTitle, lastViewLink, lastViewTime))

            } while (cursor.moveToNext())
            cursor.close()
        }
        val slv = findViewById<ListView>(R.id.sourceList)
        slv.adapter = SourceListAdapter(this, sourcesList)
    }

    private class ViewHolder(view: View?) {
        val sourceName: TextView
        val lastViewTitle: TextView
        val lastViewTime: TextView
        val ivEdit: ImageView
        val ivDelete: ImageView

        init {
            this.sourceName = view?.findViewById<TextView>(R.id.sourceName) as TextView
            this.lastViewTitle = view.findViewById<TextView>(R.id.lastViewTitle) as TextView
            this.lastViewTime = view.findViewById<TextView>(R.id.lastViewTime) as TextView
            this.ivEdit = view.findViewById<ImageView>(R.id.ivEdit) as ImageView
            this.ivDelete = view.findViewById<ImageView>(R.id.ivDelete) as ImageView
        }
    }
}