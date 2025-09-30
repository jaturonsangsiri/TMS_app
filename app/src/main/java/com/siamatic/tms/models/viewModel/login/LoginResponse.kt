package com.siamatic.tms.models.viewModel.login

data class LoginResponse(
  val success: Boolean,
  val message: String,
  val data: Data
)

data class Data(
  val token: String,
  val refreshToken: String,
  val id: String,
  val name: String,
  val hosId: String,
  val wardId: String,
  val role: String,
  val pci: String
)