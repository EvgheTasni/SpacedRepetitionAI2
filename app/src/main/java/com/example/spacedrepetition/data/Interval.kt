package com.example.spacedrepetition.data

data class Interval(
    val id: Long = 0,
    val name: String,
    val notificationTimes: List<Long> = emptyList(), // Duration in milliseconds for each notification
    val createdAt: Long = System.currentTimeMillis()
)
