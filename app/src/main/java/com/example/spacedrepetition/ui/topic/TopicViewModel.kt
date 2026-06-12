package com.example.spacedrepetition.ui.topic

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.spacedrepetition.data.AppDatabase
import com.example.spacedrepetition.data.Topic
import com.example.spacedrepetition.data.TopicListLiveData
import kotlinx.coroutines.launch

class TopicViewModel(application: Application) : AndroidViewModel(application) {

    private val topicDao = AppDatabase.getInstance(application).topicDao
    private val intervalDao = AppDatabase.getInstance(application).intervalDao

    val topics: TopicListLiveData = topicDao.getAllTopics()
    val intervals = intervalDao.getAllIntervals()

    fun addTopic(title: String, intervalId: Long) {
        viewModelScope.launch {
            val topic = Topic(title = title, intervalId = intervalId)
            topicDao.insert(topic)
            topics.refresh()
        }
    }

    fun deleteTopic(topic: Topic) {
        viewModelScope.launch {
            topicDao.deleteById(topic.id)
            topics.refresh()
        }
    }

    fun recordNotification(topic: Topic) {
        viewModelScope.launch {
            topicDao.updateLastNotifiedAt(topic.id, System.currentTimeMillis())
            topics.refresh()
        }
    }

    fun incrementArrivedCount(topic: Topic) {
        viewModelScope.launch {
            topicDao.incrementArrivedCount(topic.id)
            topics.refresh()
        }
    }
}