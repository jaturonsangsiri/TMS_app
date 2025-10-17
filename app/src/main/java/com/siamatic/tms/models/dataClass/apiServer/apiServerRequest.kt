package com.siamatic.tms.models.dataClass.apiServer

data class ApiServerRequest(
    val mcuId: String,
    val status: String,
    val tempValue: Float,
    val realValue: Float,
    val date: String,
    val time: String
)