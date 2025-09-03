package com.siamatic.tms.services

class StringService {
  // เติมตัวอักษรด้านซ้าย เช่น จาก "8" -> "08"
  fun padLeft(text: String, lenth: Int, padChar: Char): String {
    return text.padStart(lenth, padChar)
  }

  // เติมตัวอักษรด้านซ้าย เช่น จาก "8" -> "800"
  fun padRight(text: String, lenth: Int, padChar: Char): String {
    return text.padEnd(lenth, padChar)
  }

  fun validEmail(email: String): Boolean {
    val emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    return email.matches(emailPattern.toRegex())
  }
}