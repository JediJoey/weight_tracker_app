package com.example.joeywheelersnhumobiledev

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface WeightDao {
    @Query("SELECT * FROM weights WHERE username = :username")
    suspend fun getWeightsForUser(username: String): List<Weight>

    @Insert
    suspend fun insert(weight: Weight)

    @Update
    suspend fun update(weight: Weight)

    @Query("DELETE FROM weights WHERE id = :weightId")
    suspend fun deleteById(weightId: Int)
}