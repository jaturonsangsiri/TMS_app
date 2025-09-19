//package com.siamatic.tms
//
//import android.util.Log
//import android.widget.Toast
//import kotlin.math.abs
//
//class TestClass {
//  var diffTemp: Float = 0f // ค่าลบระหว่างอุณหภูมิที่ปรับแล้ว กับ อุณหภูมิปัจจุบัน
//
//  private fun checkRangeTemp() {
//    diff_temp = abs((fTempAdjusted - fTemp_old).toDouble())
//    diff_temp_count = diff_temp_count + 1
//
//    if (diff_temp >= 5 && count_abnormal_serge <= 6) {
//      if (diff_temp_count == 1) {
//        count_abnormal_serge = 0
//      } else {
//        count_abnormal_serge += 1
//        fTempAdjusted = fTemp_old
//        Toast.makeText(this@MainTabActivity, "System found abnormal temperature signal.", Toast.LENGTH_LONG).show()
//      }
//    } else if (diff_temp >= 5 && count_abnormal_serge >= 6) {
//      count_abnormal_serge = 0
//      Toast.makeText(this@MainTabActivity, "Reset probe due to system found abnormal temperature signal.", Toast.LENGTH_LONG).show()
//      // Don't change fTempAdjust to fTemp_old
//    }
//    if (diff_temp_count == 50) {
//      diff_temp_count = 2
//    }
//
//    if (fTempAdjusted > mMax || fTempAdjusted < mMin) {
//      textColor = 1 //red color
//      if (muteState.toInt() == 0) {
//        checkAlarm("A")   // A = Alarm
//        statusNormal = 1 // Temp over range
//        countAbnormal++ // count for send Message
//
//        countSendMessage(countAbnormal)
//      } else {
//        Log.i("AndroidTMS", "Silent")
//      }
//    } else if (fTempAdjusted <= mMax && fTempAdjusted >= mMin) {
//      textColor = 0 //white color
//      checkAlarm("S")
//      muteState = 0
//      countAbnormal = 0
//      statusNormal = 0
//
//
//      handlerAppendData.removeCallbacks(runnable) //temp return to normal cancel the delay message
//
//      // send sms when temp. returned to normal
//      if (firstMessage.toInt() == 1 && statusNormal.toInt() == 0) {
//        if (saveIndexNormalMessage.toInt() == 0) {
//          sendAlertToServer("Refrig")
//          if (timerSendRepeat != null) {
//            timerSendRepeat.cancel()
//          }
//        } else if (timerSendRepeat != null) {
//          timerSendRepeat.cancel()
//        }
//      }
//      firstMessage = 0
//    }
//    MainFragment.textTempColor(textColor)
//    fTemp_old = fTempAdjusted
//  }
//}