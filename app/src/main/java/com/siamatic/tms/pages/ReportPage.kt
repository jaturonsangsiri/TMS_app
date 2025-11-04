package com.siamatic.tms.pages

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
import com.siamatic.tms.util.sharedPreferencesClass

@Composable
fun ReportPage(paddingValues: PaddingValues) {
  val appContext = LocalContext.current.applicationContext
  val sharePref = sharedPreferencesClass(appContext)
  val reportSettings = remember {
    mutableStateListOf(
      ReportSetting("Send report 1", mutableStateOf(sharePref.getPreference("IS_REPORT1", "Boolean", true) == true), mutableIntStateOf(sharePref.getPreference("REPORT1_TIME", "Int", 0).toString().toInt())),
      ReportSetting("Send report 2", mutableStateOf(sharePref.getPreference("IS_REPORT2", "Boolean", false) == true), mutableIntStateOf(sharePref.getPreference("REPORT2_TIME", "Int", 4).toString().toInt())),
      ReportSetting("Send report 3", mutableStateOf(sharePref.getPreference("IS_REPORT3", "Boolean", false) == true), mutableIntStateOf(sharePref.getPreference("REPORT3_TIME", "Int", 8).toString().toInt())),
      ReportSetting("Send report 4", mutableStateOf(sharePref.getPreference("IS_REPORT4", "Boolean", false) == true), mutableIntStateOf(sharePref.getPreference("REPORT4_TIME", "Int", 12).toString().toInt())),
      ReportSetting("Send report 5", mutableStateOf(sharePref.getPreference("IS_REPORT5", "Boolean", false) == true), mutableIntStateOf(sharePref.getPreference("REPORT5_TIME", "Int", 16).toString().toInt())),
      ReportSetting("Send report 6", mutableStateOf(sharePref.getPreference("IS_REPORT6", "Boolean", false) == true), mutableIntStateOf(sharePref.getPreference("REPORT6_TIME", "Int", 20).toString().toInt()))
    )
  }

  val isTab3 = defaultCustomComposable.getDeviceHeightPixels(appContext)

  // 👇 ย้ายออกมานอก forEach
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
        .fillMaxHeight(if (isTab3) 0.9f else 0.65f)
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
        reportSettings.forEach { report ->
          Row(modifier = Modifier.fillMaxWidth().padding(0.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            switchReport(report)
            Text(report.title, fontSize = if (isTab3) 15.sp else 17.sp, modifier = Modifier.weight(1f))

            Button(onClick = { selectedReport = report }, enabled = report.isSelect.value, colors = ButtonDefaults.buttonColors(containerColor = if (report.isSelect.value) BabyBlue else Color.LightGray)) {
              Text("${report.time.value.toString().padStart(2, '0')}:00")
            }
          }
        }

        Spacer(modifier = Modifier.weight(1f))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
          defaultCustomComposable.BuildButton("Save", isTab3, Color(0xFF3CB371)) {
            // ดึงเฉพาะรายการที่เปิดอยู่
            val enabledReports = reportSettings.filter { it.isSelect.value }
            // ดึงเฉพาะเวลาที่เปิดอยู่
            val times = enabledReports.map { it.time.value }
            // ตรวจสอบเวลาซ้ำ
            val hasDuplicate = times.size != times.toSet().size
            // ตรวจสอบเรียงลำดับ (เวลาเรียงจากน้อยไปมาก)
            val isSorted = times.zipWithNext().all { (a, b) -> a < b }

            if (hasDuplicate || !isSorted) {
              defaultCustomComposable.showToast(appContext, "กรุณาแก้ไขข้อมูลให้ถูกต้อง")
              return@BuildButton
            }

            // ✅ ผ่านการตรวจสอบแล้ว ค่อยบันทึก
            reportSettings.forEachIndexed { index, report ->
              sharePref.savePreference("IS_REPORT${index + 1}", report.isSelect.value)
              if (report.isSelect.value) {
                sharePref.savePreference("REPORT${index + 1}_TIME", report.time.value)
              }

              Log.i(debugTag, "${report.title} -> ${report.time.value}:00 ${report.isSelect.value}")
            }

            defaultCustomComposable.showToast(appContext, "บันทึกสำเร็จ!")
          }
        }
      }
    }
  }

  // ✅ แสดง dialog ตรงนี้แทน
  selectedReport?.let { report ->
    HourSelectDialog(
      initialHour = report.time.value,
      onConfirm = { selectedHour ->
        report.time.value = selectedHour
        selectedReport = null
      },
      onDismiss = { selectedReport = null }
    )
  }
}

@Composable
fun switchReport(report: ReportSetting) {
  Switch(
    colors = SwitchDefaults.colors(
      checkedThumbColor = Color.White,
      checkedTrackColor = BabyBlue,
      uncheckedThumbColor = Color.Gray,
      uncheckedTrackColor = Color.LightGray,
    ),
    checked = report.isSelect.value,
    onCheckedChange = { report.isSelect.value = it }
  )
}

@Composable
fun HourSelectDialog(initialHour: Int, onConfirm: (Int) -> Unit, onDismiss: () -> Unit) {
  var hour by remember { mutableIntStateOf(initialHour) }

  Dialog(onDismissRequest = { onDismiss() }) {
    Surface(shape = RoundedCornerShape(16.dp), tonalElevation = 6.dp, color = Color.White) {
      Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Select hour", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          defaultCustomComposable.BuildAddMinusControl(
            true,
            { hour = if (hour > 0) hour - 1 else 23 },
            true,
            { hour = if (hour < 23) hour + 1 else 0 },
            hour.toString()
          )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
          Button(onClick = { onConfirm(hour) }, colors = ButtonDefaults.buttonColors(containerColor = Color.Blue.copy(alpha = 0.7f))) {
            Text("Confirm")
          }
          Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.colorRed))) {
            Text("Cancel")
          }
        }
      }
    }
  }
}

data class ReportSetting(val title: String, var isSelect: MutableState<Boolean> = mutableStateOf(false), var time: MutableState<Int>)