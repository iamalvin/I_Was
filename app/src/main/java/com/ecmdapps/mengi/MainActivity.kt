package com.ecmdapps.mengi

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.format.DateUtils.*
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import java.util.*

class MainActivity : AppCompatActivity() {
    private var sourcesList = ArrayList<Source>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        loadQueryAll()
        val slv = findViewById<ListView>(R.id.sourceList)
        slv.adapter = SourceListAdapter(this, sourcesList)
        slv.onItemClickListener = AdapterView.OnItemClickListener{ _, _, position, _ ->
            loadInWebView(sourcesList[position])
        }
        fab.setOnClickListener { _ ->
            val intent = Intent(this, SourceActivity::class.java)
            startActivity(intent)
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
            val mainActivity = context as MainActivity

            if (convertView == null) {
                view = mainActivity.layoutInflater.inflate(R.layout.source, parent, false)
                vh = ViewHolder(view)
                view.tag = vh
            } else {
                view = convertView
                vh = view.tag as ViewHolder
            }

            val mSource = sourcesList[position]

            vh.lastViewTitle.text = if (mSource.lastViewTitle != null) mSource.lastViewTitle else mSource.sourceName
            vh.sourceName.text = mSource.sourceName
            mSource.sourceImage?.size
            vh.lastViewTime.text = if (mSource.lastViewTime != 0L) getRelativeDateTimeString(context, mSource.lastViewTime!!, SECOND_IN_MILLIS, WEEK_IN_MILLIS, FORMAT_ABBREV_ALL) else "never viewed"
            val img : Bitmap = BitmapFactory.decodeByteArray(mSource.sourceImage!!, 0, mSource.sourceImage?.size!!)
            vh.ivFavicon.setImageBitmap(Bitmap.createScaledBitmap(img, img.width, img.height, false))

            vh.lastViewTitle.setOnClickListener {
                loadInWebView(mSource)
            }
            vh.ivEdit.setOnClickListener {
                updateSource(mSource)
            }

            vh.ivDelete.setOnClickListener {
                val dbManager = SourceDbManager(this.context!!)
                val selectionArgs = arrayOf(mSource.id.toString())
                dbManager.delete("Id=?", selectionArgs)
                loadQueryAll()
            }
            return view
        }
    }

    private fun loadInWebView(source: Source) {
        val intent = Intent(this, WebviewActivity::class.java)
        intent.putExtra(SourceDbManager.colId, source.id)
        intent.putExtra(SourceDbManager.colSourceName, source.sourceName)
        intent.putExtra(SourceDbManager.colSourceLink, source.sourceLink)
        intent.putExtra(SourceDbManager.colLastViewTitle, source.lastViewTitle)
        intent.putExtra(SourceDbManager.colLastViewLink, source.lastViewLink)

        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        loadQueryAll()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val history = History(this)
        return when (item.itemId) {
            R.id.action_history -> history.show()
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun loadQueryAll() {
        val dbManager = SourceDbManager(this)
        val cursor = dbManager.queryAll()

        sourcesList.clear()

        if (cursor.count > 0) {
            cursor.moveToFirst()

            do {
                val id = cursor.getLong(cursor.getColumnIndex(SourceDbManager.colId))
                val sourceName = cursor.getString(cursor.getColumnIndex(SourceDbManager.colSourceName))
                val sourceLink = cursor.getString(cursor.getColumnIndex(SourceDbManager.colSourceLink))
                val sourceImage = cursor.getBlob(cursor.getColumnIndex(SourceDbManager.colSourceImage))
                val lastViewTitle = cursor.getString(cursor.getColumnIndex(SourceDbManager.colLastViewTitle))
                val lastViewLink = cursor.getString(cursor.getColumnIndex(SourceDbManager.colLastViewLink))
                val lastViewTime = cursor.getLong(cursor.getColumnIndex(SourceDbManager.colLastViewTime))

                sourcesList.add(Source(id, sourceName, sourceLink, sourceImage, lastViewTitle, lastViewLink, lastViewTime))

            } while (cursor.moveToNext())
            cursor.close()
            dbManager.close()
        } else {
            val name = "Google Search"
            val url = "http://google.com"
            val fav = BitmapFactory.decodeResource(this.resources, R.drawable.default_favicon)
            val stream = ByteArrayOutputStream()
            fav.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val fByteArray = stream.toByteArray()

            val values = ContentValues()
            values.put(SourceDbManager.colSourceName, name)
            values.put(SourceDbManager.colSourceLink, url)
            values.put(SourceDbManager.colLastViewLink, url)
            values.put(SourceDbManager.colLastViewTime, System.currentTimeMillis())
            values.put(SourceDbManager.colSourceImage, fByteArray)
            dbManager.insert(values)
            dbManager.close()
            loadQueryAll()
        }

        val slv = findViewById<ListView>(R.id.sourceList)
        slv.adapter = SourceListAdapter(this, sourcesList)
    }

    private fun updateSource(source: Source) {
        val intent = Intent(this, SourceActivity::class.java)
        intent.putExtra("MainActId", source.id)
        intent.putExtra("MainActSourceName", source.sourceName)
        intent.putExtra("MainActSourceLink", source.sourceLink)
        startActivity(intent)
    }

    private class ViewHolder(view: View?) {
        val sourceName: TextView
        val lastViewTitle: TextView
        val lastViewTime: TextView
        val ivEdit: ImageView
        val ivDelete: ImageView
        val ivFavicon: ImageView

        init {
            this.sourceName = view?.findViewById(R.id.sourceName) as TextView
            this.lastViewTitle = view.findViewById<TextView>(R.id.lastViewTitle)
            this.lastViewTime = view.findViewById<TextView>(R.id.lastViewTime)
            this.ivEdit = view.findViewById<ImageView>(R.id.ivEdit)
            this.ivDelete = view.findViewById<ImageView>(R.id.ivDelete)
            this.ivFavicon = view.findViewById(R.id.ivFavicon) as ImageView
        }
    }
}
