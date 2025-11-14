package com.siamatic.tms.util

import android.net.Uri

class SimManage {
  fun getTelDetail(network: String): String {
    return when (network) {
      "AIS" -> "*121#"
      "DTAC" -> "*101#"
      else -> "*123#"
    }
  }

  fun refillTel(network: String, refillCode: String): String {
    val encodeHash = Uri.encode("#")
    return when (network) {
      "AIS" -> "*120*$refillCode$encodeHash"
      "DTAC" -> "*100*$refillCode$encodeHash"
      else -> "*123*$refillCode$encodeHash"
    }
  }
}