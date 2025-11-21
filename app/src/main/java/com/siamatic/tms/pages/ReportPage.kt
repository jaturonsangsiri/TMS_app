package com.siamatic.tms.pages

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.siamatic.tms.R
import com.siamatic.tms.constants.debugTag
import com.siamatic.tms.defaultCustomComposable
import com.siamatic.tms.ui.theme.BabyBlue
import com.siamatic.tms.util.TimeSelectDialog
import com.siamatic.tms.util.sharedPreferencesClass

@Composable
fun ReportPage(paddingValues: PaddingValues) {
  val appContext = LocalContext.current.applicationContext
  val sharePref = sharedPreferencesClass(appContext)
  val reportSettings = remember {
    mutableStateListOf(
      ReportSetting("Send report 1", mutableStateOf(sharePref.getPreference("IS_REPORT1", "Boolean", true) == true), mutableStateOf(sharePref.getPreference("REPORT1_TIME", "String", "00:00").toString())),
      ReportSetting("Send report 2", mutableStateOf(sharePref.getPreference("IS_REPORT2", "Boolean", false) == true), mutableStateOf(sharePref.getPreference("REPORT2_TIME", "String", "04:00").toString())),
      ReportSetting("Send report 3", mutableStateOf(sharePref.getPreference("IS_REPORT3", "Boolean", false) == true), mutableStateOf(sharePref.getPreference("REPORT3_TIME", "String", "08:00").toString())),
      ReportSetting("Send report 4", mutableStateOf(sharePref.getPreference("IS_REPORT4", "Boolean", false) == true), mutableStateOf(sharePref.getPreference("REPORT4_TIME", "String", "12:00").toString())),
      ReportSetting("Send report 5", mutableStateOf(sharePref.getPreference("IS_REPORT5", "Boolean", false) == true), mutableStateOf(sharePref.getPreference("REPORT5_TIME", "String", "16:00").toString())),
      ReportSetting("Send report 6", mutableStateOf(sharePref.getPreference("IS_REPORT6", "Boolean", false) == true), mutableStateOf(sharePref.getPreference("REPORT6_TIME", "String", "20:00").toString()))
    )
  }

  val isTab3 = defaultCustomComposable.getDeviceHeightPixels(appContext)

  var selectedReport by remember { mutableStateOf<ReportSetting?>(null) }

  Column(
    modifier = Modifier
      .padding(paddingValues)
      .fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Card(
      modifier = Modifier
        .fillMaxHeight(if (isTab3) 0.9f else 1f)
        .fillMaxWidth(if (isTab3) 0.6f else 0.6f)
        .padding(20.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(15.dp),
        verticalArrangement = Arrangement.Top
      ) {
        Text(
          text = "Settings report sending",
          fontSize = if (isTab3) 18.sp else 20.sp,
          fontWeight = FontWeight.W600,
          color = Color.Black.copy(alpha = 0.70f),
          modifier = Modifier.padding(bottom = 8.dp)
        )

        // ✅ ใช้ forEachIndexed ปลอดภัยกว่า
        reportSettings.forEachIndexed { inddex, report ->
          Row(modifier = Modifier.fillMaxWidth().padding(0.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            switchReport(report, inddex, reportSettings, appContext)
            Text(report.title, fontSize = if (isTab3) 15.sp else 17.sp, modifier = Modifier.weight(1f))

            Button(onClick = { selectedReport = report }, enabled = report.isSelect.value, colors = ButtonDefaults.buttonColors(containerColor = if (report.isSelect.value) BabyBlue else Color.LightGray)) {
              Text( if (report.isSelect.value) report.time.value.toString() else "__ : __")
            }
          }
        }

        Spacer(modifier = Modifier.weight(1f))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
          defaultCustomComposable.BuildButton("Save", isTab3, Color(0xFF3CB371)) {
            // ดึงเฉพาะรายการที่เปิดอยู่
            val enabledReports = reportSettings.filter { it.isSelect.value }

            // helper: แปลง "HH:mm" -> total minutes หรือ null ถ้าไม่ valid
            fun parseToMinutes(timeStr: String): Int? {
              val parts = timeStr.split(":")
              if (parts.size != 2) return null
              val h = parts[0].toIntOrNull() ?: return null
              val m = parts[1].toIntOrNull() ?: return null
              if (h !in 0..23 || m !in 0..59) return null
              return h * 60 + m
            }

            // ดึง list ของ total minutes (valid)
            val timesInMinutes = enabledReports.mapNotNull { parseToMinutes(it.time.value) }

            // ถ้ามี item ที่ไม่ parse ได้ -> แจ้ง error
            if (timesInMinutes.size != enabledReports.size) {
              defaultCustomComposable.showToast(appContext, "เวลาไม่ถูกต้อง")
              return@BuildButton
            }

            // ตรวจสอบเวลาซ้ำ (total minutes จะเป็ฯ unique key)
            val hasDuplicate = timesInMinutes.size != timesInMinutes.toSet().size

            // ตรวจสอบเรียงลำดับ (น้อย -> มาก)
            val isSorted = timesInMinutes.zipWithNext().all { (a, b) -> a < b }

            if (hasDuplicate || !isSorted) {
              defaultCustomComposable.showToast(appContext, "กรุณาแก้ไขข้อมูลให้ถูกต้อง (ห้ามซ้ำและต้องเรียงลำดับ)")
              return@BuildButton
            }

            // ผ่านการตรวจสอบแล้ว ค่อยบันทึก
            reportSettings.forEachIndexed { index, report ->
              sharePref.savePreference("IS_REPORT${index + 1}", report.isSelect.value)
              if (report.isSelect.value) {
                sharePref.savePreference("REPORT${index + 1}_TIME", report.time.value)
              }
              Log.i(debugTag, "saved report IS_REPORT${index + 1}, isreport: ${report.isSelect.value}, report time: ${report.time.value}")
            }

            defaultCustomComposable.showToast(appContext, "บันทึกสำเร็จ!")
          }
        }
      }
    }
  }

  // ✅ แสดง dialog ตรงนี้แทน
  selectedReport?.let { report ->
    TimeSelectDialog(
      initialHour = report.time.value.split(":").getOrNull(0)?.toIntOrNull() ?: 0,
      initialMinute = report.time.value.split(":").getOrNull(1)?.toIntOrNull() ?: 0,
      onConfirm = { timeString ->
        report.time.value = timeString
        selectedReport = null
      },
      onDismiss = { selectedReport = null }
    )
  }
}

@Composable
fun switchReport(report: ReportSetting, index: Int, reportSettings: List<ReportSetting>, context: Context) {
  Switch(
    colors = SwitchDefaults.colors(
      checkedThumbColor = Color.White,
      checkedTrackColor = BabyBlue,
      uncheckedThumbColor = Color.Gray,
      uncheckedTrackColor = Color.LightGray,
    ),
    checked = report.isSelect.value,
    onCheckedChange = { isChecked ->
      // 🔒 ถ้าจะเปิด ต้องเช็คว่ารายการก่อนหน้าเปิดหมดแล้ว
      if (isChecked) {
        if (index > 0 && !reportSettings[index - 1].isSelect.value) {
          // ป้องกันเปิดข้ามลำดับ
          defaultCustomComposable.showToast(context, "Please open the previous item first")
          return@Switch
        }
        report.isSelect.value = true
      } else {
        // ❌ ถ้าปิด ต้องปิดรายการหลังจากนี้ทั้งหมด
        report.isSelect.value = false
        for (i in index + 1 until reportSettings.size) {
          reportSettings[i].isSelect.value = false
        }
      }
    }
  )
}

data class ReportSetting(val title: String, var isSelect: MutableState<Boolean> = mutableStateOf(false), var time: MutableState<String>)