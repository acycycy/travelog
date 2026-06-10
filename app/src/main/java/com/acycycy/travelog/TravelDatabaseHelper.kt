package com.acycycy.travelog

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class TravelDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "travelog.db"
        private const val DATABASE_VERSION = 2

        private const val TABLE_NAME = "travel"
        private const val COLUMN_ID = "id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_MEMO = "memo"
        private const val COLUMN_IMAGE_URI = "image_uri"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT,
                $COLUMN_DATE TEXT,
                $COLUMN_MEMO TEXT,
                $COLUMN_IMAGE_URI TEXT
            )
        """.trimIndent()

        db.execSQL(createTable)
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertTravel(title: String, date: String, memo: String, imageUri: String?): Long {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_TITLE, title)
            put(COLUMN_DATE, date)
            put(COLUMN_MEMO, memo)
            put(COLUMN_IMAGE_URI, imageUri)
        }

        return db.insert(TABLE_NAME, null, values)
    }

    fun getAllTravels(): List<Travel> {
        val travelList = mutableListOf<Travel>()
        val db = readableDatabase

        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME ORDER BY $COLUMN_ID DESC", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE))
                val date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))
                val memo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEMO))
                val imageUri = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URI))

                travelList.add(Travel(id, title, date, memo, imageUri))
            } while (cursor.moveToNext())
        }

        cursor.close()
        return travelList
    }

    fun deleteTravel(id: Int): Int {
        val db = writableDatabase
        return db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    fun updateTravel(id: Int, title: String, date: String, memo: String, imageUri: String?): Int {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_TITLE, title)
            put(COLUMN_DATE, date)
            put(COLUMN_MEMO, memo)
            put(COLUMN_IMAGE_URI, imageUri)
        }

        return db.update(
            TABLE_NAME,
            values,
            "$COLUMN_ID = ?",
            arrayOf(id.toString())
        )
    }

    fun getTravelCount(): Int {
        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_NAME",
            null
        )

        var count = 0

        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }

        cursor.close()
        return count
    }
}