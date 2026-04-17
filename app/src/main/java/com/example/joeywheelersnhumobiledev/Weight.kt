package com.example.joeywheelersnhumobiledev

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weights")
data class Weight(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val quantity: Double,
    val date: String
)