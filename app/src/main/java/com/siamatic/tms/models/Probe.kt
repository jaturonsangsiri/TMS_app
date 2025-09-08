package com.siamatic.tms.models

data class Probe(
  var name: String,
  var temperature: Float? = 0.0f,
  var over: Float? = 0.0f,
  var under: Float? = 0.0f,
  var maxTemp: Float? = 0.0f,
  var minTemp: Float? = 0.0f
)
