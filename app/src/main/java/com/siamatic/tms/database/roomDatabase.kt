package com.siamatic.tms.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.siamatic.tms.database.dao.TempDao

// Setting room data table and dao
@Database(entities = [Temp::class], version = 1, exportSchema = false)
abstract class AppRoomDB : RoomDatabase() {
  abstract fun tempDao(): TempDao
}