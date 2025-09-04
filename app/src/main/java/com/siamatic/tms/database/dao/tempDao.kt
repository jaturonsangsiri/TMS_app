package com.siamatic.tms.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.siamatic.tms.database.Temp
import kotlinx.coroutines.flow.Flow

// Endpoints function of query
@Dao
interface TempDao {
  // Get all temp
  @Query("SELECT * FROM `temp` WHERE createdAt ")
  fun getAll(): Flow<List<Temp>>

  // Add temp
  @Insert
  suspend fun insertAll(temps: Temp): Long

  // Reset all records for Debugs.
  @Query("DELETE FROM `temp`")
  suspend fun resetData()
}