package com.ecmdapps.mengi

import android.content.ContentValues
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.Toast


class SourceActivity : AppCompatActivity(){
    var id:Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_source)

        val sourceNameET = findViewById<EditText>(R.id.sourceName)
        val sourceLinkET = findViewById<EditText>(R.id.sourceLink)
        val btAdd = findViewById<Button>(R.id.btAdd)

        try {
            id = intent.getLongExtra("MainActId", 0L)
            if (id != 0L) {
                sourceNameET.setText(intent.getStringExtra("MainActSourceName"))
                sourceLinkET.setText(intent.getStringExtra("MainActSourceLink"))
            }
        } catch (ex: Exception) { }

        btAdd.setOnClickListener{
            val dbManager = SourceDbManager(this)

            val values = ContentValues()
            values.put(SourceDbManager.colSourceName, sourceNameET.text.toString())
            values.put(SourceDbManager.colSourceLink, sourceLinkET.text.toString())
            values.put(SourceDbManager.colLastViewLink, sourceLinkET.text.toString())

            if (id == 0L ){
                val mID = dbManager.insert(values)

                if (mID > 0) {
                    Toast.makeText(this, "Added source successfully!", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this, "Failed to add source!", Toast.LENGTH_LONG).show()
                }
            } else {
                val selectionArs = arrayOf(id.toString())
                val mID = dbManager.update(values, "Id=?", selectionArs)

                if (mID > 0) {
                    Toast.makeText(this, "updated source successfully!", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this, "Failed to update source!", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}