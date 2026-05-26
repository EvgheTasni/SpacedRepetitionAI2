package com.example.spacedrepetition.data

data class Interval(
    val id: Long = 0,
    val name: String,
    val durationMillis: Long,
    val createdAt: Long = System.currentTimeMillis()
)
