package com.siamatic.tms.util

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

// Use SharedPreferences to store device settings data
class sharedPreferencesClass(context: Context) {
  private val sharedPreferences: SharedPreferences = context.getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
  private val editor = sharedPreferences.edit()

  // save SharedPreference value
  fun savePreference(key: String, value: Any?): Boolean {
    if (value == null) return false

    when (value) {
      is String -> editor.putString(key, value)
      is Long -> editor.putLong(key, value)
      is Int -> editor.putInt(key, value)
      is Boolean -> editor.putBoolean(key, value)
      is Float -> editor.putFloat(key, value)
      else -> return false
    }
    editor.apply()
    return true
  }

  // Get SharedPreference value
  fun getPreference(key: String, type: String, default: Any): Any? {
    val result = when (type) {
      "String" -> sharedPreferences.getString(key, default.toString())
      "Long" -> sharedPreferences.getLong(key, (default.toString()).toLong())
      "Int" -> sharedPreferences.getInt(key, (default.toString()).toInt())
      "Boolean" -> sharedPreferences.getBoolean(key, (default.toString()) == "true")
      "Float" -> sharedPreferences.getFloat(key, (default.toString()).toFloat())
      else -> return null
    }
    return result
  }
}