package com.example.spacedrepetition.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AppDatabase private constructor(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    private var _dao: IntervalDao? = null

    val intervalDao: IntervalDao
        get() {
            if (_dao == null) {
                _dao = IntervalDao(writableDatabase)
            }
            return _dao!!
        }

    override fun onCreate(db: SQLiteDatabase) {
        IntervalDao.createTable(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS intervals")
        onCreate(db)
    }

    companion object {
        private const val DB_NAME = "spaced_repetition.db"
        private const val DB_VERSION = 1

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                AppDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
