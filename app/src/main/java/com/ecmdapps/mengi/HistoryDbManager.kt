package com.ecmdapps.mengi

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class HistoryDbManager(context: Context) {
    companion object {
        private val dbName = "MengiHistory"
        private val dbTable = "History"
        val colId = SourceDbManager.colId
        val colSourceId = "SourceId"
        val colSourceImage = SourceDbManager.colSourceImage
        val colLastViewTitle = SourceDbManager.colLastViewTitle
        val colLastViewId = SourceDbManager.colLastViewId
        val colLastViewLink = SourceDbManager.colLastViewLink
        val colLastViewTime = SourceDbManager.colLastViewTime
        private val dbVersion = 2


        private val CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS " + dbTable + " (" + colId + " INTEGER PRIMARY KEY," + colLastViewId + " TEXT UNIQUE, " + colLastViewLink + " TEXT, " + colLastViewTitle + " TEXT, " + colSourceImage + " BLOB, " + colSourceId + " INTEGER, " + colLastViewTime + " INTEGER);"
    }

    private var db: SQLiteDatabase? = null

    fun insert(values: ContentValues): Long {
        return db!!.insert(dbTable, "", values)
    }

    fun queryAll(): Cursor {
        return db!!.rawQuery("select * from " + dbTable  + " ORDER BY "+ colLastViewTime + " DESC", null)
    }

    fun queryPerSourceId(sourceId: Long) : Cursor {
        return db!!.rawQuery("select * from " + dbTable  + " where " + colSourceId + " = ? "  + " ORDER BY "+ colLastViewTime + " DESC", arrayOf(sourceId.toString()))
    }

    fun delete(selection: String, selectionArgs: Array<String>): Int {
        val count = db!!.delete(dbTable, selection, selectionArgs)
        return count
    }

    fun find(LastViewId: String) : Cursor {
        return db!!.rawQuery("select * from " + dbTable + " where " + colLastViewId + " = ? " + " ORDER BY "+ colLastViewTime + " DESC", arrayOf(LastViewId))
    }

    fun add(values: ContentValues) {
        val lid = values.get(colLastViewId)
        values.put(colSourceId, values.getAsLong(colId))
        values.remove(colId)
        values.remove(SourceDbManager.colSourceName)
        values.remove(SourceDbManager.colSourceLink)
        val cursor = find(lid.toString())
        if (cursor.count > 0) {
            cursor.moveToFirst()
            cursor.close()
            update(values, colLastViewId + " = ? ", arrayOf(lid.toString()))
        } else {
            insert(values)
        }
    }

    fun update(values: ContentValues, selection: String, selectionargs: Array<String>): Int {
        return db!!.update(dbTable, values, selection, selectionargs)
    }

    inner class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, dbName, null, dbVersion) {

        override fun onCreate(db: SQLiteDatabase?) {
            db!!.execSQL(CREATE_TABLE_SQL)
        }

        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
            db!!.execSQL("Drop table IF EXISTS " + dbTable)
            db.execSQL(CREATE_TABLE_SQL)
        }
    }

    init {
        val dbHelper = DatabaseHelper(context)
        db = dbHelper.writableDatabase
    }
}