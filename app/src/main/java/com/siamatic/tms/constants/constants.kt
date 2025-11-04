package com.siamatic.tms.constants

import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.stringPreferencesKey

val minuteOptions = listOf("5 minute", "15 minute", "30 minute", "1 hour", "2 hour", "4 hour")
val minOptionsLng = minuteOptions.associateWith {
  val (num, unit) = it.split(" ")
  num.toLong() * 60 * 1000L * if (unit == "hour") 60 else 1
}
val tabsName = listOf("MAIN", "GRAPH", "TABLE", "SETUP", "MESSAGE", "SIM", "ADJUST", "REPORT", "EXIT")

const val debugTag = "Debug"
const val timeFormat = "HH:mm"
const val dateFormat = "yyyy-MM-dd"

// Special character check should not have in file name
val specialCharStrings: List<String> = listOf("/", "\\", ":", "*", "?", "\"", "<", ">")

// All keys in preference
const val WIFI_NAME = "WIFI_NAME"
const val WIFI_PASSWORD = "WIFI_PASSWORD"
const val DEVICE_ID = "SN_DEVICE_KEY"
const val DEVICE_NAME1 = "DEVICE_NAME1"
const val DEVICE_NAME2 = "DEVICE_NAME2"
const val TEMP_MAX_P1 = "TEMP_MAX_P1"
const val TEMP_MIN_P1 = "TEMP_MIN_P1"
const val TEMP_MAX_P2 = "TEMP_MAX_P2"
const val TEMP_MIN_P2 = "TEMP_MIN_P2"

const val RECORD_INTERVAL = "RECORD_INTERVAL"
const val IS_MUTE = "IS_MUTE"
const val IS_SEND_MESSAGE = "IS_SEND_MESSAGE"
const val SEND_MESSAGE = "SEND_MESSAGE"          // -1  = immediately
const val IS_MESSAGE_REPEAT = "IS_MESSAGE_REPEAT"
const val MESSAGE_REPEAT = "MESSAGE_REPEAT"      // -1  = oneTime
const val RETURN_TO_NORMAL = "RETURN_TO_NORMAL"  // boolean (true=send, false=doNotSend)
const val NETWORK = "NETWORK"                    // 'AIS' / 'DTAC' / 'TRUE'
const val SERIAL_NO = "SERIAL_NO"
const val P1_ADJUST_TEMP = "P1_ADJUST_TEMP"
const val P2_ADJUST_TEMP = "P2_ADJUST_TEMP"
const val EMAIL_PASSWORD = "EMAIL_PASSWORD"

// Google script ID
const val GOOGLE_SCRIPT_ID = "GOOGLE_SCRIPT_ID"
// DEVICE_API_TOKEN for API TOKEN KEY
const val DEVICE_API_TOKEN = "DEVICE_API_TOKEN"
// Google Sheet ID
const val SHEET_ID = "SHEET_ID"


/*** For Styling ***/
val outerBoxPadding = 15.dp
val innerBoxPadding = 10.dp