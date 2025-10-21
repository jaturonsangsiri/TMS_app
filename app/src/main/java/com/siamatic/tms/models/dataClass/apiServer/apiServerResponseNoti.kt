package com.siamatic.tms.models.dataClass.apiServer

data class ApiServerResponseNoti(
  val success: Boolean,
  val message: String,
  val data: ApiServerResponseNotiData
)

data class ApiServerResponseNotiData(
  val mcuId: String,
  val internet: Boolean,
  val door: Boolean,
  val plugin: Boolean,
  val tempValue: Float,
  val realValue: Float,
  val date: String,
  val time: String,
  val isAlert: Boolean,
  val message: String,
  val probe: String,
  val createdAt: String,
  val updatedAt: String
)
