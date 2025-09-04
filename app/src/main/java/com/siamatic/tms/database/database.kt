package com.siamatic.tms.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// Temp database schema
@Entity(tableName = "temp")
data class Temp(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // ID of temperature autogenerate
    @ColumnInfo(defaultValue = "0.0") val temp1: Float?, // Probe 1 temperature
    @ColumnInfo(defaultValue = "0.0") val temp2: Float?, // Probe 2 temperature
    val createdAt: Long // timestamp of record added!
)