package com.example.spacedrepetition.ui.interval

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.spacedrepetition.data.AppDatabase
import com.example.spacedrepetition.data.DefaultIntervalLiveData
import com.example.spacedrepetition.data.Interval
import com.example.spacedrepetition.data.IntervalListLiveData
import com.example.spacedrepetition.data.Topic
import kotlinx.coroutines.launch

class IntervalViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).intervalDao
    private val topicDao = AppDatabase.getInstance(application).topicDao

    val intervals: IntervalListLiveData = dao.getAllIntervals()

    val defaultInterval: DefaultIntervalLiveData = dao.getDefaultInterval()

    fun getTopicsForInterval(intervalId: Long): List<Topic> {
        return topicDao.getAllTopicsSync().filter { it.intervalId == intervalId }
    }

    fun isNameUnique(name: String): Boolean {
        return (intervals.value ?: emptyList()).none { it.name.equals(name, ignoreCase = true) }
    }

    fun addInterval(name: String, notificationTimes: List<Long>) {
        viewModelScope.launch {
            val interval = Interval(name = name, notificationTimes = notificationTimes)
            dao.insert(interval)
            intervals.refresh()
            defaultInterval.refresh()
        }
    }

    fun deleteInterval(interval: Interval) {
        viewModelScope.launch {
            // DAO handles topic cascade-delete inside a DB transaction
            dao.deleteById(interval.id)
            intervals.refresh()
            defaultInterval.refresh()
            // Also refresh topic lists since topics were cascade-deleted
            topicDao.getAllTopics().refresh()
        }
    }

    fun setAsDefault(interval: Interval) {
        viewModelScope.launch {
            dao.setAsDefault(interval.id)
            intervals.refresh()
            defaultInterval.refresh()
        }
    }
}