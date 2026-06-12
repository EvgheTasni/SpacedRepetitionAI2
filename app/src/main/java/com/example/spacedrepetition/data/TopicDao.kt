package com.example.spacedrepetition.data

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TopicDao(private val db: SQLiteDatabase) {

    @Volatile
    private var _allTopics: TopicListLiveData? = null

    fun getAllTopics(): TopicListLiveData {
        return _allTopics ?: synchronized(this) {
            _allTopics ?: TopicListLiveData(db).also { _allTopics = it }
        }
    }

    fun getAllTopicsSync(): List<Topic> {
        val cursor = db.query(
            TABLE_TOPICS, null, null, null, null, null,
            "$COL_CREATED_AT DESC"
        )
        val list = mutableListOf<Topic>()
        while (cursor.moveToNext()) {
            list.add(fromCursor(cursor))
        }
        cursor.close()
        return list
    }

    @Volatile
    private var _activeTopics: ActiveTopicListLiveData? = null

    fun getActiveTopics(): ActiveTopicListLiveData {
        return _activeTopics ?: synchronized(this) {
            _activeTopics ?: ActiveTopicListLiveData(db).also { _activeTopics = it }
        }
    }

    fun getTopicById(id: Long): TopicByIdLiveData {
        return TopicByIdLiveData(db, id)
    }

    suspend fun insert(topic: Topic): Long = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put(COL_TITLE, topic.title)
            put(COL_INTERVAL_ID, topic.intervalId)
            put(COL_IS_PAUSED, if (topic.isPaused) 1 else 0)
            put(COL_ARRIVED_NOTIFICATION_COUNT, topic.arrivedNotificationCount)
            put(COL_CREATED_AT, topic.createdAt)
        }
        db.insert(TABLE_TOPICS, null, values)
    }

    suspend fun update(topic: Topic) = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put(COL_TITLE, topic.title)
            put(COL_INTERVAL_ID, topic.intervalId)
            put(COL_IS_PAUSED, if (topic.isPaused) 1 else 0)
            put(COL_ARRIVED_NOTIFICATION_COUNT, topic.arrivedNotificationCount)
        }
        db.update(TABLE_TOPICS, values, "$COL_ID = ?", arrayOf(topic.id.toString()))
    }

    suspend fun incrementArrivedCount(id: Long) = withContext(Dispatchers.IO) {
        db.execSQL(
            "UPDATE $TABLE_TOPICS SET $COL_ARRIVED_NOTIFICATION_COUNT = $COL_ARRIVED_NOTIFICATION_COUNT + 1 WHERE $COL_ID = ?",
            arrayOf(id.toString())
        )
    }

    suspend fun setPaused(id: Long, paused: Boolean) = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put(COL_IS_PAUSED, if (paused) 1 else 0)
        }
        db.update(TABLE_TOPICS, values, "$COL_ID = ?", arrayOf(id.toString()))
    }

    suspend fun deleteById(id: Long) = withContext(Dispatchers.IO) {
        db.delete(TABLE_TOPICS, "$COL_ID = ?", arrayOf(id.toString()))
    }

    companion object {
        const val TABLE_TOPICS = "topics"
        const val COL_ID = "id"
        const val COL_TITLE = "title"
        const val COL_INTERVAL_ID = "interval_id"
        const val COL_IS_PAUSED = "is_paused"
        const val COL_ARRIVED_NOTIFICATION_COUNT = "arrived_notification_count"
        const val COL_CREATED_AT = "created_at"

        private const val CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS $TABLE_TOPICS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TITLE TEXT NOT NULL,
                $COL_INTERVAL_ID INTEGER NOT NULL,
                $COL_IS_PAUSED INTEGER NOT NULL DEFAULT 0,
                $COL_ARRIVED_NOTIFICATION_COUNT INTEGER NOT NULL DEFAULT 0,
                $COL_CREATED_AT INTEGER NOT NULL
            )
        """

        fun createTable(db: SQLiteDatabase) {
            db.execSQL(CREATE_TABLE)
        }

        fun fromCursor(cursor: Cursor): Topic {
            return Topic(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
                title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE)),
                intervalId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_INTERVAL_ID)),
                isPaused = cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_PAUSED)) == 1,
                arrivedNotificationCount = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ARRIVED_NOTIFICATION_COUNT)),
                createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COL_CREATED_AT))
            )
        }
    }
}

class TopicListLiveData(
    private val db: SQLiteDatabase
) : LiveData<List<Topic>>() {

    override fun onActive() {
        super.onActive()
        load()
    }

    fun refresh() {
        load()
    }

    private fun load() {
        val cursor = db.query(
            TopicDao.TABLE_TOPICS, null, null, null, null, null,
            "${TopicDao.COL_CREATED_AT} DESC"
        )
        val list = mutableListOf<Topic>()
        while (cursor.moveToNext()) {
            list.add(TopicDao.fromCursor(cursor))
        }
        cursor.close()
        postValue(list)
    }
}

class ActiveTopicListLiveData(
    private val db: SQLiteDatabase
) : LiveData<List<Topic>>() {

    override fun onActive() {
        super.onActive()
        load()
    }

    fun refresh() {
        load()
    }

    private fun load() {
        val cursor = db.query(
            TopicDao.TABLE_TOPICS,
            null,
            "${TopicDao.COL_IS_PAUSED} = 0",
            null, null, null,
            "${TopicDao.COL_CREATED_AT} DESC"
        )
        val list = mutableListOf<Topic>()
        while (cursor.moveToNext()) {
            list.add(TopicDao.fromCursor(cursor))
        }
        cursor.close()
        postValue(list)
    }
}

class TopicByIdLiveData(
    private val db: SQLiteDatabase,
    private val topicId: Long
) : LiveData<Topic?>() {

    override fun onActive() {
        super.onActive()
        load()
    }

    fun refresh() {
        load()
    }

    private fun load() {
        val cursor = db.query(
            TopicDao.TABLE_TOPICS,
            null,
            "${TopicDao.COL_ID} = ?",
            arrayOf(topicId.toString()),
            null, null, null,
            "1"
        )
        val topic = if (cursor.moveToNext()) {
            TopicDao.fromCursor(cursor)
        } else {
            null
        }
        cursor.close()
        postValue(topic)
    }
}