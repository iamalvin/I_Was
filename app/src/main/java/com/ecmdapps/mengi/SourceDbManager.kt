package com.ecmdapps.mengi

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.google.common.net.InternetDomainName
import java.net.URL

class SourceDbManager(context: Context) {
    companion object {
        private val dbName = "MengiSources"
        private val dbTable = "Sources"
        val colId = "Id"
        val colSourceLink = "SourceLink"
        val colSourceName = "SourceName"
        val colSourceImage = "LastViewFavicon"
        val colLastViewTitle = "LastViewTitle"
        val colLastViewId = "LastViewId"
        val colLastViewLink = "LastViewLink"
        val colLastViewTime = "LastViewTime"
        private val dbVersion = 5
        private val CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS " + dbTable + " (" + colId + " INTEGER PRIMARY KEY," + colSourceName + " TEXT, " + colSourceLink + " TEXT, " + colLastViewId + " TEXT, " + colLastViewLink + " TEXT, " + colLastViewTitle + " TEXT, " + colSourceImage + " BLOB, " + colLastViewTime + " INTEGER);"

    }

    private var db: SQLiteDatabase? = null

    fun insert(values: ContentValues): Long {
        val ID = db!!.insert(dbTable, "", values)
        return ID
    }

    fun queryAll(): Cursor {
        return db!!.rawQuery("select * from " + dbTable + " ORDER BY "+ colLastViewTime + " DESC", null)
    }

    fun delete(selection: String, selectionArgs: Array<String>): Int {
        return db!!.delete(dbTable, selection, selectionArgs)
    }

    fun find(Id: Long) : Cursor {
        return db!!.rawQuery("select * from " + dbTable + " where " + colId + " = ? "  + " ORDER BY "+ colLastViewTime + " DESC", arrayOf(Id.toString()))
    }

    fun add(values: ContentValues) : Long {
        val id = values.get(colId) as Long
        val cursor = find(id)
        if (cursor.count > 0) {
            cursor.moveToFirst()
            val sourceHost = URL(cursor.getString(cursor.getColumnIndex(colSourceLink))).host
            val lastViewHost= URL(values.get(colLastViewLink).toString()).host
            val sourceTPD = InternetDomainName.from(sourceHost).topPrivateDomain().toString()
            val lastViewTPD = InternetDomainName.from(lastViewHost).topPrivateDomain().toString()
            cursor.close()

            if (sourceTPD.equals(lastViewTPD)) {
                update(values, colId + " = ? ", arrayOf(id.toString()))
                return id.toString().toLong()
            } else {
                values.put(colSourceName, lastViewTPD)
                values.put(colSourceLink, values.get(colLastViewLink).toString())
                values.remove(colId)
                return insert(values)
            }
        } else {
            return 0L
        }
    }

    fun update(values: ContentValues, selection: String, selectionargs: Array<String>): Int {
        val count = db!!.update(dbTable, values, selection, selectionargs)
        return count
    }

    inner class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, dbName, null, dbVersion) {
        var defaultValues = ContentValues()
        fun populateDefaultValues (){
            defaultValues.put(colSourceName, "Google")
            defaultValues.put(colSourceLink, "http://google.com")
            defaultValues.put(colLastViewLink, "http://google.com")
        }
        override fun onCreate(db: SQLiteDatabase?) {
            populateDefaultValues()
            db!!.execSQL(CREATE_TABLE_SQL)
            insert(defaultValues)
        }

        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
            populateDefaultValues()
            db!!.execSQL("Drop table IF EXISTS " + dbTable)
            db.execSQL(CREATE_TABLE_SQL)
            insert(defaultValues)
        }
    }

    init {
        val dbHelper = DatabaseHelper(context)
        db = dbHelper.writableDatabase
    }
}