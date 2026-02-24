package com.wajebaat.tracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "entries")
data class Entry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val amount: Double,
    val wajebaat: Double,
    val moneyLeft: Double,
    val date: String,
    val timestamp: Long
)
