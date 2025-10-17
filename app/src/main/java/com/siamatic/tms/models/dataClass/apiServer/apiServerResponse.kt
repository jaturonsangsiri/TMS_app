package com.siamatic.tms.models.dataClass.apiServer

data class ApiServerResponse(
    val success: Boolean,
    val message: String,
    val data: Data
)

data class Data(
    val id: Int,
    val mcuId: String,
    val status: String,
    val tempValue: Float,
    val realValue: Float,
    val date: String,
    val time: String,
    val createdAt: String,
    val updatedAt: String
)