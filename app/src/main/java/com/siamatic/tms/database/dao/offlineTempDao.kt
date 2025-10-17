package com.siamatic.tms.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.siamatic.tms.database.OfflineTemp
import kotlinx.coroutines.flow.Flow

// Endpoints function of query
@Dao
interface OfflineTempDao {
    // Get all offline temp
    @Query("SELECT * FROM `offline_temp`")
    fun getAll(): List<OfflineTemp>?

    // Add offline temp
    @Insert
    suspend fun insertAll(temps: OfflineTemp): Long

    // Reset all records for Debugs.
    @Query("DELETE FROM `offline_temp`")
    suspend fun resetData()

    // Delete offline temp by id
    @Query("DELETE FROM `offline_temp` WHERE id = :id")
    suspend fun deleteById(id: Int)
}