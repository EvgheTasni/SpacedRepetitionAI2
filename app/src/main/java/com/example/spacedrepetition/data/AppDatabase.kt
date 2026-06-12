package com.example.spacedrepetition.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AppDatabase private constructor(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    private var _intervalDao: IntervalDao? = null
    private var _topicDao: TopicDao? = null

    val intervalDao: IntervalDao
        get() {
            if (_intervalDao == null) {
                _intervalDao = IntervalDao(writableDatabase)
            }
            return _intervalDao!!
        }

    val topicDao: TopicDao
        get() {
            if (_topicDao == null) {
                _topicDao = TopicDao(writableDatabase)
            }
            return _topicDao!!
        }

    override fun onCreate(db: SQLiteDatabase) {
        IntervalDao.createTable(db)
        TopicDao.createTable(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS intervals")
        db.execSQL("DROP TABLE IF EXISTS topics")
        onCreate(db)
    }

    companion object {
        private const val DB_NAME = "spaced_repetition.db"
        private const val DB_VERSION = 4

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                AppDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
