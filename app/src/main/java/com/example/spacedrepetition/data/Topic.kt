package com.example.spacedrepetition.data

data class Topic(
    val id: Long = 0,
    val title: String,
    val intervalId: Long,
    val isPaused: Boolean = false,
    val arrivedNotificationCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)