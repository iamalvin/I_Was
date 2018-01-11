package com.ecmdapps.mengi

import android.content.Context
import android.content.Intent

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
}