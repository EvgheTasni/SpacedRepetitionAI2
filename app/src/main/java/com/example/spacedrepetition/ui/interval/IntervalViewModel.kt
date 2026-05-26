package com.example.spacedrepetition.ui.interval

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.spacedrepetition.data.AppDatabase
import com.example.spacedrepetition.data.Interval

class IntervalViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).intervalDao

    val intervals: LiveData<List<Interval>> = dao.getAllIntervals()
}
