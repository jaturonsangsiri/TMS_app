package com.siamatic.tms.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.siamatic.tms.database.Temp

// Endpoints function of query
@Dao
interface TempDao {
  // Get all temp
  @Query("SELECT * FROM `temp`")
  fun getAll(): List<Temp>

  // Add temp
  @Insert
  fun insertAll(temps: Temp): Long
}