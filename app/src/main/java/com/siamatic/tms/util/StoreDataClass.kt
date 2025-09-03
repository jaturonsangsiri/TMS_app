package com.siamatic.tms.util
//
//import android.util.Log
//import com.siamatic.tms.constants.debugTag
//
///*
// * Created by YinG on 16-Mar-17.
// * Updated to kotlin by Jaturon on 02-Sep-25.
// */
//class StoreDataClass {
//  private var mcuId: Char? = null          // Broad microcontroller ID
//  private var ch: CharArray? = null
//  private var biStatus: String? = null
//  private var plugStatus: Int = 0          // Cable Plug in status
//
//  fun storeParameters(readData: StringBuffer) {
//    ch = readData.toString().toCharArray()
//    var SB: StringBuilder
//
//    // Get MCU ID in Char Array
//    mcuId = ch!![1];
//    plugStatus = ch!![3].code
//    // Covert byte to binary 8 bit ex. from 5 -> '00000101'
//    biStatus = String.format("%8s", Integer.toBinaryString(ch!![3].code and 0xFF)).replace(' ', '0')
//
//    // Settings plug in status in main Page
//    Log.d(debugTag, "Plug in status: $plugStatus");
//    //MainFragment.plugInStatus(plug_status);
//    //MainTabActivity.getACstate(plug_status);
//
//    // Converts temperature at position 5+6
//    //MainFragment.plugInStatus(plug_status);
//    //MainTabActivity.getACstate(plug_status);
//
//    //Converts temperature at position 5+6
//    for (i in 5 until ch!!.size - 2) {
//      val temp = String.format("%02x", ch!![i].code)
//      Log.d(debugTag, "temp: $temp")
//      if (temp.length == 4) {
//        SB.append(temp.substring(2, 4))
//      } else {
//        SB.append(temp)
//      }
//    }
//  }
//}