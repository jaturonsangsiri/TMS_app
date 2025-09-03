package com.siamatic.tms.services

class BufferRead {
  companion object {
    // Convert heximal to ASCii code charecters
    fun hexToAscii(s: String): String {
      val n = s.length
      val sb = java.lang.StringBuilder(n / 2)
      var i = 0
      while (i < n) {
        val a = s[i]
        val b = s[i + 1]
        sb.append(((hexToInt(a) shl 4) or hexToInt(b)).toChar())
        i += 2
      }
      return sb.toString()
    }

    // Convert heximal to Int code
    private fun hexToInt(ch: Char): Int {
      if ('a' <= ch && ch <= 'f') {
        return ch.code - 'a'.code + 10
      }
      if ('A' <= ch && ch <= 'F') {
        return ch.code - 'A'.code + 10
      }
      if ('0' <= ch && ch <= '9') {
        return ch.code - '0'.code
      }
      throw java.lang.IllegalArgumentException(ch.toString())
    }
  }

  // Convert heximal to decimal
  fun hextToDeximal(hex: Byte): Int = hex.toInt()
}