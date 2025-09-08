package com.siamatic.tms.constants

val minuteOptions = listOf("5 minute","15 minute","30 minute","1 hour","2 hour","4 hour")
val tabsName = listOf("MAIN", "GRAPH", "TABLE", "SETUP", "MESSAGE", "MENAGE SIM", "ADJUST", "EXIT")

const val debugTag = "Debug"
const val timeFormat = "HH:mm"
const val dateFormat = "yyyy-MM-dd"

// Special character check should not have in file name
val specialCharStrings: List<String> = listOf("/", "\\", ":", "*", "?", "\"", "<", ">")