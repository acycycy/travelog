package com.acycycy.travelog

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class TravelDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "travelog.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_NAME = "travel"
        private const val COLUMN_ID = "id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_MEMO = "memo"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT,
                $COLUMN_DATE TEXT,
                $COLUMN_MEMO TEXT
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

    fun insertTravel(title: String, date: String, memo: String): Long {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_TITLE, title)
            put(COLUMN_DATE, date)
            put(COLUMN_MEMO, memo)
        }

        return db.insert(TABLE_NAME, null, values)
    }

    fun getAllTravels(): List<Travel> {
        val travelList = mutableListOf<Travel>()
        val db = readableDatabase

        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME ORDER BY $COLUMN_ID DESC", null)

        if (cursor.moveToFirst()) {
            do {
                val title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE))
                val date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))
                val memo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEMO))

                travelList.add(Travel(title, date, memo))
            } while (cursor.moveToNext())
        }

        cursor.close()
        return travelList
    }
}