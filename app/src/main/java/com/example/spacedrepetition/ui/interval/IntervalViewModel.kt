package com.example.spacedrepetition.ui.interval

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.spacedrepetition.data.AppDatabase
import com.example.spacedrepetition.data.Interval
import com.example.spacedrepetition.data.IntervalListLiveData
import kotlinx.coroutines.launch

class IntervalViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).intervalDao

    val intervals: IntervalListLiveData = dao.getAllIntervals()

    fun isNameUnique(name: String): Boolean {
        return (intervals.value ?: emptyList()).none { it.name.equals(name, ignoreCase = true) }
    }

    fun addInterval(name: String, notificationTimes: List<Long>) {
        viewModelScope.launch {
            val interval = Interval(name = name, notificationTimes = notificationTimes)
            dao.insert(interval)
            intervals.refresh()
        }
    }

    fun deleteInterval(interval: Interval) {
        viewModelScope.launch {
            dao.deleteById(interval.id)
            intervals.refresh()
        }
    }
}
