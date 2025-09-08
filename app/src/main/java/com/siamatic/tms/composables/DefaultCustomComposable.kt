package com.siamatic.tms.composables

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.siamatic.tms.R
import com.siamatic.tms.constants.dateFormat
import com.siamatic.tms.constants.timeFormat
import com.siamatic.tms.ui.theme.BabyBlue
import java.text.SimpleDateFormat
import java.util.Date

class DefaultCustomComposable {
  @Composable
  fun BuildButton(text: String, bgColor: Color, onClick: () -> Unit) {
    Button(onClick = onClick, shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = bgColor)) {
      Text(text)
    }
  }

  @Composable
  fun BuildIconButton(onClick: () -> Unit, painter: Painter?, imageVector: ImageVector?, iconSize: Dp) {
    IconButton(onClick = onClick) {
      Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        if (imageVector != null) {
          Icon(modifier = Modifier.size(20.dp), tint = Color.White, imageVector = Icons.Default.Email, contentDescription = "")
        }
        if (painter != null) {
          Icon(modifier = Modifier.size(iconSize), tint = Color.White, painter = painter, contentDescription = "")
        }
      }
    }
  }

  @Composable
  fun BuildTextIconButton(text: String, bgColor: Color, onClick: () -> Unit, painter: Painter?, imageVector: ImageVector?, iconSize: Dp) {
    Button( onClick = onClick, shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = bgColor)) {
      Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        if (imageVector != null) {
          Icon(modifier = Modifier.size(20.dp), tint = Color.White, imageVector = Icons.Default.Email, contentDescription = "")
        }
        if (painter != null) {
          Icon(modifier = Modifier.size(iconSize), tint = Color.White, painter = painter, contentDescription = "")
        }
        Spacer(Modifier.width(8.dp))
        Text(text)
      }
    }
  }

  @Composable
  fun BuildCheckBox(text: String, checker: Boolean, onCheckedChange: ((Boolean) -> Unit)) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 10.dp)) {
      Checkbox(modifier = Modifier.size(24.dp).padding(0.dp), checked = checker, onCheckedChange = onCheckedChange, interactionSource = remember { MutableInteractionSource() })
      Text(text = text, modifier = Modifier.padding(start = 4.dp))
    }
  }

  @Composable
  fun RadioButtonCustom(text: String, selected: Boolean, onClick: (() -> Unit)) {
    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
      RadioButton(modifier = Modifier.size(24.dp).padding(0.dp), selected = !selected, onClick = onClick)
      Text(text)
    }
  }

  @Composable
  fun BuildAddMinusControl(plusEnable: Boolean, plusClick: () -> Unit, minusEnable: Boolean, minusClick: () -> Unit, value: String) {
    Card(onClick = plusClick, modifier = Modifier.size(36.dp), enabled = plusEnable, colors = CardDefaults.cardColors(containerColor = BabyBlue, disabledContainerColor = Color.Gray)) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Icon(painter = painterResource(id = R.drawable.minus), contentDescription = "Decrease", modifier = Modifier.size(16.dp), tint = Color.White)
      }
    }
    Spacer(modifier = Modifier.width(10.dp))
    OutlinedCard(modifier = Modifier.widthIn(min = 48.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
      Text(text = value, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
    }
    Spacer(modifier = Modifier.width(10.dp))
    Card(onClick = minusClick, modifier = Modifier.size(36.dp), enabled = minusEnable, colors = CardDefaults.cardColors(containerColor = BabyBlue, disabledContainerColor = Color.Gray)) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp), tint = Color.White)
      }
    }
  }

  fun showAlertDialog(context: Context, message: String) {
    val alertDialog = AlertDialog.Builder(context)
    alertDialog.setTitle("Error")
    alertDialog.setMessage(message)
    alertDialog.setPositiveButton("OK") { dialog: DialogInterface, _ ->
      dialog.dismiss()
    }
    alertDialog.show()
  }

  // To covert Long to "2025-08-28"
  fun convertLongToDateOnly(time: Long): String {
    val date = Date(time)
    val format = SimpleDateFormat(dateFormat)
    return format.format(date)
  }

  // To covert Long to "14:35"
  fun convertLongToTime(time: Long): String {
    val date = Date(time)
    val format = SimpleDateFormat(timeFormat)
    return format.format(date)
  }

  // To covert "2025-08-28 14:35" to Long
  fun convertDateToLong(date: String): Long {
    val df = SimpleDateFormat(timeFormat)
    return df.parse(date).time
  }

  // Show Toast message
  fun showToast(context: Context, text: String) {
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
  }
}