package com.siamatic.tms.models.dataClass.apiServer

data class ApiServerRequestNoti(
  val mcuId: String,
  val status: String,
  val tempValue: Float,
  val realValue: Float,
  val message: String,
  val date: String,
  val time: String
)
