package com.siamatic.tms.services.api_Service.api_server

class MessagePattern {
  fun messagePatternToServer(type: String, probeName: String, min: Float, max: Float, roundedTemp: Float, date: String, time: String): String {
    var message: String = when (type) {
      "warning" -> "!!Warning!! Refrig.: $probeName, Temp. = $roundedTemp °C (MAX:$max MIN:$min) on $date $time"
      "report" -> "**Report** $probeName Temp = $roundedTemp °C at $date $time"
      else -> "Refrig.: $probeName, Temperature returned to normal. Temp.= $roundedTemp °C on $date $time"
    }
    return message
  }
}