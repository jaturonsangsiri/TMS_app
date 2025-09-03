package com.siamatic.tms.database

import android.content.Context
import androidx.room.Room

// Create database object and prevents duplicate DB creation
object DatabaseProvider {
  @Volatile
  private var INSTANCE: AppRoomDB? = null

  fun getDatabase(context: Context): AppRoomDB {
    return INSTANCE ?: synchronized(this) {
      INSTANCE ?: Room.databaseBuilder(
        context.applicationContext,
        AppRoomDB::class.java,
        "app_roomdb.db"
      ).build().also { INSTANCE = it }
    }
  }
}