package com.siamatic.tms.database

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Create database object and prevents duplicate DB creation
object DatabaseProvider {
  @Volatile
  private var INSTANCE: AppRoomDB? = null

  // Migration Example if change Database structure
  val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
      // ตัวอย่าง: เพิ่ม column ใหม่
      database.execSQL("ALTER TABLE temp ADD COLUMN dateStr TEXT NOT NULL DEFAULT 'undefined' ")
      database.execSQL("ALTER TABLE temp ADD COLUMN timeStr TEXT NOT NULL DEFAULT 'undefined' ")
    }
  }

  fun getDatabase(context: Context): AppRoomDB {
    return INSTANCE ?: synchronized(this) {
      INSTANCE ?: Room.databaseBuilder(
        context.applicationContext,
        AppRoomDB::class.java,
        "app_roomdb.db"
      ).build().also { INSTANCE = it }
        //.addMigrations(MIGRATION_1_2)
    }
  }
}