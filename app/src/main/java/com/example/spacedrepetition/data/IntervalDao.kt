package com.example.spacedrepetition.data

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.lifecycle.LiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class IntervalDao(private val db: SQLiteDatabase) {

    private val gson = Gson()

    @Volatile
    private var _allIntervals: IntervalListLiveData? = null

    fun getAllIntervals(): IntervalListLiveData {
        return _allIntervals ?: synchronized(this) {
            _allIntervals ?: IntervalListLiveData(db, gson).also { _allIntervals = it }
        }
    }

    @Volatile
    private var _defaultInterval: DefaultIntervalLiveData? = null

    fun getDefaultInterval(): DefaultIntervalLiveData {
        return _defaultInterval ?: synchronized(this) {
            _defaultInterval ?: DefaultIntervalLiveData(db, gson).also { _defaultInterval = it }
        }
    }

    suspend fun insert(interval: Interval): Long = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put(COL_NAME, interval.name)
            put(COL_NOTIFICATION_TIMES, gson.toJson(interval.notificationTimes))
            put(COL_CREATED_AT, interval.createdAt)
            put(COL_IS_DEFAULT, if (interval.isDefault) 1 else 0)
        }
        db.insert(TABLE_INTERVALS, null, values)
    }

    suspend fun deleteById(id: Long) = withContext(Dispatchers.IO) {
        db.run {
            try {
               beginTransactionNonExclusive()

                // If the deleted interval was the default, clear the default flag
                val cursor = query(
                    TABLE_INTERVALS,
                    arrayOf(COL_IS_DEFAULT),
                    "$COL_ID = ?",
                    arrayOf(id.toString()),
                    null, null, null,
                    "1"
                )
                val wasDefault = if (cursor.moveToNext()) {
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_DEFAULT)) == 1
                } else {
                    false
                }
                cursor.close()

                // Delete interval
                delete(TABLE_INTERVALS, "$COL_ID = ?", arrayOf(id.toString()))

                // If it was the default, clear all defaults
                if (wasDefault) {
                    execSQL("UPDATE $TABLE_INTERVALS SET $COL_IS_DEFAULT = 0")
                }

                // Also delete all topics linked to this interval
                db.delete(TopicDao.TABLE_TOPICS, "${TopicDao.COL_INTERVAL_ID} = ?", arrayOf(id.toString()))

                setTransactionSuccessful()
            } finally {
                endTransaction()
            }
        }
    }

    suspend fun setAsDefault(id: Long) = withContext(Dispatchers.IO) {
        // First, unset all defaults
        db.execSQL("UPDATE $TABLE_INTERVALS SET $COL_IS_DEFAULT = 0")
        // Then set the specified interval as default
        db.execSQL("UPDATE $TABLE_INTERVALS SET $COL_IS_DEFAULT = 1 WHERE $COL_ID = ?", arrayOf(id.toString()))
    }

    companion object {
        const val TABLE_INTERVALS = "intervals"
        const val COL_ID = "id"
        const val COL_NAME = "name"
        const val COL_NOTIFICATION_TIMES = "notification_times"
        const val COL_CREATED_AT = "created_at"
        const val COL_IS_DEFAULT = "is_default"

        private const val CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS $TABLE_INTERVALS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_NAME TEXT NOT NULL,
                $COL_NOTIFICATION_TIMES TEXT NOT NULL,
                $COL_CREATED_AT INTEGER NOT NULL,
                $COL_IS_DEFAULT INTEGER NOT NULL DEFAULT 0
            )
        """

        fun createTable(db: SQLiteDatabase) {
            db.execSQL(CREATE_TABLE)
        }

        fun fromCursor(cursor: Cursor, gson: Gson): Interval {
            val json = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTIFICATION_TIMES))
            val listType = object : TypeToken<List<Long>>() {}.type
            val notificationTimes = gson.fromJson(json, listType) ?: emptyList<Long>()

            return Interval(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
                notificationTimes = notificationTimes,
                createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COL_CREATED_AT)),
                isDefault = cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_DEFAULT)) == 1
            )
        }
    }
}

class IntervalListLiveData(
    private val db: SQLiteDatabase,
    private val gson: Gson
) : LiveData<List<Interval>>() {

    override fun onActive() {
        super.onActive()
        load()
    }

    fun refresh() {
        load()
    }

    private fun load() {
        val cursor = db.query(
            IntervalDao.TABLE_INTERVALS, null, null, null, null, null,
            "${IntervalDao.COL_CREATED_AT} DESC"
        )
        val list = mutableListOf<Interval>()
        while (cursor.moveToNext()) {
            list.add(IntervalDao.fromCursor(cursor, gson))
        }
        cursor.close()
        postValue(list)
    }
}

class DefaultIntervalLiveData(
    private val db: SQLiteDatabase,
    private val gson: Gson
) : LiveData<Interval?>() {

    override fun onActive() {
        super.onActive()
        load()
    }

    fun refresh() {
        load()
    }

    private fun load() {
        val cursor = db.query(
            IntervalDao.TABLE_INTERVALS,
            null,
            "${IntervalDao.COL_IS_DEFAULT} = 1",
            null, null, null, null,
            "1"
        )
        val interval = if (cursor.moveToNext()) {
            IntervalDao.fromCursor(cursor, gson)
        } else {
            null
        }
        cursor.close()
        postValue(interval)
    }
}