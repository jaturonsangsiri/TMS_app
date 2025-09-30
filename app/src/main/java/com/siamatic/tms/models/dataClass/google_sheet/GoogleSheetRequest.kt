package com.siamatic.tms.models.dataClass.google_sheet

data class GoogleSheetRequest(
  val sheetId: String,
  val serialNumber: String,
  val probe: String,
  val temp: Float,
  val acStatus: String,
  val machineIP: String,
  val minTemp: Float,
  val maxTemp: Float,
  val adjTemp: Float
)
