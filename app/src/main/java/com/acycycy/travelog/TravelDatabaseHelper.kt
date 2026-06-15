package com.acycycy.travelog

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.Calendar

class TravelDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "travelog.db"
        private const val DATABASE_VERSION = 7

        private const val TABLE_TRAVEL = "travel"
        private const val COLUMN_ID = "id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_MEMO = "memo"
        private const val COLUMN_IMAGE_URI = "image_uri"
        private const val COLUMN_RATING = "rating"
        private const val COLUMN_LOCATION = "location"

        private const val TABLE_PLAN = "travel_plan"
        private const val COLUMN_PLAN_ID = "id"
        private const val COLUMN_DESTINATION = "destination"
        private const val COLUMN_PLANNED_DATE = "planned_date"
        private const val COLUMN_PLAN_MEMO = "memo"
        private const val COLUMN_PLAN_LOCATION = "location"

        private const val TABLE_PHOTOS = "travel_photos"
        private const val COLUMN_PHOTO_ID = "id"
        private const val COLUMN_PHOTO_TRAVEL_ID = "travel_id"
        private const val COLUMN_PHOTO_URI = "image_uri"
        private const val COLUMN_PHOTO_COMMENT = "comment"
    }

    private val createTravelTable = """
        CREATE TABLE $TABLE_TRAVEL (
            $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_TITLE TEXT,
            $COLUMN_DATE TEXT,
            $COLUMN_MEMO TEXT,
            $COLUMN_IMAGE_URI TEXT,
            $COLUMN_RATING INTEGER DEFAULT 0,
            $COLUMN_LOCATION TEXT
        )
    """.trimIndent()

    private val createPlanTable = """
        CREATE TABLE $TABLE_PLAN (
            $COLUMN_PLAN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_DESTINATION TEXT,
            $COLUMN_PLANNED_DATE TEXT,
            $COLUMN_PLAN_MEMO TEXT,
            $COLUMN_PLAN_LOCATION TEXT
        )
    """.trimIndent()

    private val createPhotosTable = """
        CREATE TABLE $TABLE_PHOTOS (
            $COLUMN_PHOTO_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_PHOTO_TRAVEL_ID INTEGER,
            $COLUMN_PHOTO_URI TEXT,
            $COLUMN_PHOTO_COMMENT TEXT
        )
    """.trimIndent()

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(createTravelTable)
        db.execSQL(createPlanTable)
        db.execSQL(createPhotosTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 3) db.execSQL(createPlanTable)
        if (oldVersion < 4) db.execSQL(createPhotosTable)
        if (oldVersion < 5) db.execSQL("ALTER TABLE $TABLE_TRAVEL ADD COLUMN $COLUMN_RATING INTEGER DEFAULT 0")
        if (oldVersion < 6) db.execSQL("ALTER TABLE $TABLE_TRAVEL ADD COLUMN $COLUMN_LOCATION TEXT")
        if (oldVersion < 7) db.execSQL("ALTER TABLE $TABLE_PLAN ADD COLUMN $COLUMN_PLAN_LOCATION TEXT")
    }

    // ─── Travel CRUD ───────────────────────────────────────────────────────────

    fun insertTravel(title: String, date: String, memo: String, imageUri: String?, rating: Int = 0, location: String? = null): Long {
        val values = ContentValues().apply {
            put(COLUMN_TITLE, title)
            put(COLUMN_DATE, date)
            put(COLUMN_MEMO, memo)
            put(COLUMN_IMAGE_URI, imageUri)
            put(COLUMN_RATING, rating)
            put(COLUMN_LOCATION, location)
        }
        return writableDatabase.insert(TABLE_TRAVEL, null, values)
    }

    fun getAllTravels(): List<Travel> {
        val list = mutableListOf<Travel>()
        val cursor = readableDatabase.rawQuery("SELECT * FROM $TABLE_TRAVEL ORDER BY $COLUMN_ID DESC", null)
        if (cursor.moveToFirst()) {
            do {
                val ratingIdx = cursor.getColumnIndex(COLUMN_RATING)
                val rating = if (ratingIdx >= 0) cursor.getInt(ratingIdx) else 0
                val locationIdx = cursor.getColumnIndex(COLUMN_LOCATION)
                val location = if (locationIdx >= 0) cursor.getString(locationIdx) else null
                list.add(
                    Travel(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEMO)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URI)),
                        rating,
                        location
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun deleteTravel(id: Int): Int {
        deletePhotosForTravel(id)
        return writableDatabase.delete(TABLE_TRAVEL, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    fun updateTravel(id: Int, title: String, date: String, memo: String, imageUri: String?, rating: Int = 0, location: String? = null): Int {
        val values = ContentValues().apply {
            put(COLUMN_TITLE, title)
            put(COLUMN_DATE, date)
            put(COLUMN_MEMO, memo)
            put(COLUMN_IMAGE_URI, imageUri)
            put(COLUMN_RATING, rating)
            put(COLUMN_LOCATION, location)
        }
        return writableDatabase.update(TABLE_TRAVEL, values, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    fun getTravelCount(): Int {
        val cursor = readableDatabase.rawQuery("SELECT COUNT(*) FROM $TABLE_TRAVEL", null)
        val count = if (cursor.moveToFirst()) cursor.getInt(0) else 0
        cursor.close()
        return count
    }

    fun getThisMonthCount(): Int {
        val cal = Calendar.getInstance()
        val prefix = String.format("%04d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
        val cursor = readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_TRAVEL WHERE $COLUMN_DATE LIKE ?",
            arrayOf("$prefix%")
        )
        val count = if (cursor.moveToFirst()) cursor.getInt(0) else 0
        cursor.close()
        return count
    }

    // ─── Photo CRUD ────────────────────────────────────────────────────────────

    fun insertPhoto(travelId: Int, imageUri: String, comment: String): Long {
        val values = ContentValues().apply {
            put(COLUMN_PHOTO_TRAVEL_ID, travelId)
            put(COLUMN_PHOTO_URI, imageUri)
            put(COLUMN_PHOTO_COMMENT, comment)
        }
        return writableDatabase.insert(TABLE_PHOTOS, null, values)
    }

    fun getPhotosForTravel(travelId: Int): List<TravelPhoto> {
        val list = mutableListOf<TravelPhoto>()
        val cursor = readableDatabase.rawQuery(
            "SELECT * FROM $TABLE_PHOTOS WHERE $COLUMN_PHOTO_TRAVEL_ID = ? ORDER BY $COLUMN_PHOTO_ID ASC",
            arrayOf(travelId.toString())
        )
        if (cursor.moveToFirst()) {
            do {
                list.add(
                    TravelPhoto(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PHOTO_ID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PHOTO_TRAVEL_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHOTO_URI)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHOTO_COMMENT)) ?: ""
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun deletePhotosForTravel(travelId: Int) {
        writableDatabase.delete(TABLE_PHOTOS, "$COLUMN_PHOTO_TRAVEL_ID = ?", arrayOf(travelId.toString()))
    }

    // ─── Plan CRUD ─────────────────────────────────────────────────────────────

    fun insertPlan(destination: String, plannedDate: String, memo: String, location: String? = null): Long {
        val values = ContentValues().apply {
            put(COLUMN_DESTINATION, destination)
            put(COLUMN_PLANNED_DATE, plannedDate)
            put(COLUMN_PLAN_MEMO, memo)
            put(COLUMN_PLAN_LOCATION, location)
        }
        return writableDatabase.insert(TABLE_PLAN, null, values)
    }

    fun getAllPlans(orderByIdDesc: Boolean = false): List<Plan> {
        val list = mutableListOf<Plan>()
        val order = if (orderByIdDesc) "$COLUMN_PLAN_ID DESC" else "$COLUMN_PLANNED_DATE ASC"
        val cursor = readableDatabase.rawQuery("SELECT * FROM $TABLE_PLAN ORDER BY $order", null)
        if (cursor.moveToFirst()) {
            do {
                val locationIdx = cursor.getColumnIndex(COLUMN_PLAN_LOCATION)
                val location = if (locationIdx >= 0) cursor.getString(locationIdx) else null
                list.add(
                    Plan(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PLAN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESTINATION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PLANNED_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PLAN_MEMO)),
                        location
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun deletePlan(id: Int): Int =
        writableDatabase.delete(TABLE_PLAN, "$COLUMN_PLAN_ID = ?", arrayOf(id.toString()))

    fun updatePlan(id: Int, destination: String, plannedDate: String, memo: String, location: String? = null): Int {
        val values = ContentValues().apply {
            put(COLUMN_DESTINATION, destination)
            put(COLUMN_PLANNED_DATE, plannedDate)
            put(COLUMN_PLAN_MEMO, memo)
            put(COLUMN_PLAN_LOCATION, location)
        }
        return writableDatabase.update(TABLE_PLAN, values, "$COLUMN_PLAN_ID = ?", arrayOf(id.toString()))
    }
}
