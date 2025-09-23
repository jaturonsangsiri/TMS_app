package com.siamatic.tms.pages

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siamatic.tms.R
import com.siamatic.tms.composables.MainTables
import com.siamatic.tms.constants.debugTag
import com.siamatic.tms.constants.specialCharStrings
import com.siamatic.tms.defaultCustomComposable
import com.siamatic.tms.services.AlternativeDatePickerModal
import com.siamatic.tms.services.checkDateIsBefore
import com.siamatic.tms.services.excel.CreateExcel
import com.siamatic.tms.services.formatDate
import com.siamatic.tms.ui.theme.BabyBlue

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun DataTable(paddingValues: PaddingValues) {
  var email by remember { mutableStateOf("ldv.rde@gmail.com") }
  var fileName by remember { mutableStateOf("") }
  var showModal by remember { mutableStateOf(false) }
  var showModal2 by remember { mutableStateOf(false) }
  var selectedStartDate by remember { mutableStateOf<Long?>(System.currentTimeMillis()) }
  var selectedEndDate by remember { mutableStateOf<Long?>(System.currentTimeMillis()) }
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  // ******  For responsive ui *******
  val isTab3 = defaultCustomComposable.getDeviceHeightPixels(context)

  Column(modifier = Modifier.padding(paddingValues), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
    Row {
      Card(modifier = Modifier.weight(0.6f).height(if (isTab3) 430.dp else 390.dp).padding(20.dp), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        MainTables()
      }

      if (showModal) {
        AlternativeDatePickerModal(selectedStartDate ?: 0L, onDateSelected = {
          if (!checkDateIsBefore(selectedStartDate, selectedEndDate)) {
            selectedStartDate = it
          } }, onDismiss = { showModal = false })
      }

      if (showModal2) {
        AlternativeDatePickerModal(selectedStartDate ?: 0L, onDateSelected = { if (!checkDateIsBefore(selectedStartDate, selectedEndDate)) { selectedEndDate = it } }, onDismiss = { showModal2 = false })
      }

      Card(modifier = Modifier.weight(0.4f).height(if (isTab3) 430.dp else 390.dp).padding(20.dp), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Column(modifier = Modifier.fillMaxSize().padding(10.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
          // Start Date Row
          Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Start Date:", fontSize = 16.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Row(modifier = Modifier.clickable { showModal = true }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color.Gray.copy(alpha = 0.2f)).padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text(text = formatDate(selectedStartDate), fontSize = 14.sp, color = Color.Black.copy(alpha = 0.5f))
              }
              Image(painter = painterResource(id = R.drawable.calendar), contentDescription = "Select Start Date", modifier = Modifier.size(30.dp))
            }
          }

          // End Date Row
          Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "End Date:", fontSize = 16.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Row(modifier = Modifier.clickable { showModal2 = true }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color.Gray.copy(alpha = 0.2f)).padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text(text = formatDate(selectedEndDate), fontSize = 14.sp, color = Color.Black.copy(alpha = 0.5f))
              }
              Image(painter = painterResource(id = R.drawable.calendar), contentDescription = "Select End Date", modifier = Modifier.size(30.dp))
            }
          }

          // Email textField
          OutlinedTextField(
            value = fileName,
            onValueChange = { fileName = it.replace(' ', '_') },
            label = { Text("File name") },
            placeholder = { Text("Enter your file") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(6.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BabyBlue, focusedLabelColor = BabyBlue, cursorColor = BabyBlue),
            keyboardOptions = KeyboardOptions( keyboardType = KeyboardType.Email ),
            singleLine = true
          )

          OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            placeholder = { Text("Enter your Email") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(6.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BabyBlue, focusedLabelColor = BabyBlue, cursorColor = BabyBlue),
            keyboardOptions = KeyboardOptions( keyboardType = KeyboardType.Email ),
            singleLine = true
          )
          Spacer(modifier = Modifier.weight(1f))

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            defaultCustomComposable.BuildTextIconButton(text = "E-mail", bgColor = BabyBlue,
              onClick = {
                // Run in background not in main thread
                coroutineScope.launch(Dispatchers.IO) {
                  try {
                    var fileNotAllow = false
                    // Check for Special charecter in file name
                    for (char in specialCharStrings) {
                      if (fileName.contains(char)) {
                        fileNotAllow = true
                      }
                    }

                    if (!fileNotAllow) {
                      val excel = CreateExcel()
                      excel.addCell(0, 0, "Date")
                      excel.addCell(0, 1, "Temperature")
                      excel.addCell(1, 0, "2025-08-29")
                      excel.addCell(1, 1, 36.5)

                      val outFile = File(context.getExternalFilesDir(null), "$fileName.xlsx")
                      val fos = FileOutputStream(outFile)
                      excel.exportToFile(fos)
                      fos.close()

                      withContext(Dispatchers.Main) {
                        Toast.makeText(
                          context,
                          "Exported to: ${outFile.absolutePath}",
                          Toast.LENGTH_SHORT
                        ).show()
                        Log.i(debugTag, "Saved to ${outFile.absolutePath}")
                        // ส่งอีเมลทันที
                        //sendEmailWithAttachment(context, file)

                        fileName = ""
                        email = "ldv.rde@gmail.com"
                      }
                    } else {
                      withContext(Dispatchers.Main) {
                        defaultCustomComposable.showToast(context, "File name not allow")
                      }
                    }
                  } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                      Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                  }
                }
              }, painter = null, imageVector = Icons.Default.Email, 20.dp)
          }
        }
      }
    }
  }
}