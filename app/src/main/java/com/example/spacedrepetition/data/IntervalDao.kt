package com.example.spacedrepetition.data

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class IntervalDao(private val db: SQLiteDatabase) {

    fun getAllIntervals(): LiveData<List<Interval>> {
        return IntervalListLiveData(db)
    }

    suspend fun insert(interval: Interval): Long = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put(COL_NAME, interval.name)
            put(COL_DURATION, interval.durationMillis)
            put(COL_CREATED_AT, interval.createdAt)
        }
        db.insert(TABLE_INTERVALS, null, values)
    }

    suspend fun deleteById(id: Long) = withContext(Dispatchers.IO) {
        db.delete(TABLE_INTERVALS, "$COL_ID = ?", arrayOf(id.toString()))
    }

    companion object {
        const val TABLE_INTERVALS = "intervals"
        const val COL_ID = "id"
        const val COL_NAME = "name"
        const val COL_DURATION = "duration_millis"
        const val COL_CREATED_AT = "created_at"

        private const val CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS $TABLE_INTERVALS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_NAME TEXT NOT NULL,
                $COL_DURATION INTEGER NOT NULL,
                $COL_CREATED_AT INTEGER NOT NULL
            )
        """

        fun createTable(db: SQLiteDatabase) {
            db.execSQL(CREATE_TABLE)
        }

        fun fromCursor(cursor: Cursor): Interval {
            return Interval(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
                durationMillis = cursor.getLong(cursor.getColumnIndexOrThrow(COL_DURATION)),
                createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COL_CREATED_AT))
            )
        }
    }
}

private class IntervalListLiveData(private val db: SQLiteDatabase) : LiveData<List<Interval>>() {

    override fun onActive() {
        super.onActive()
        load()
    }

    private fun load() {
        val cursor = db.query(
            IntervalDao.TABLE_INTERVALS, null, null, null, null, null,
            "${IntervalDao.COL_CREATED_AT} DESC"
        )
        val list = mutableListOf<Interval>()
        while (cursor.moveToNext()) {
            list.add(IntervalDao.fromCursor(cursor))
        }
        cursor.close()
        postValue(list)
    }
}
