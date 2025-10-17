package com.siamatic.tms.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.siamatic.tms.database.dao.TempDao
import com.siamatic.tms.database.dao.OfflineTempDao

// Setting room data table and dao
@Database(entities = [Temp::class, OfflineTemp::class], version = 2, exportSchema = false)
abstract class AppRoomDB : RoomDatabase() {
  abstract fun tempDao(): TempDao
  abstract fun offlineTempDao(): OfflineTempDao
}