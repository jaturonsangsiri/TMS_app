package com.siamatic.tms.services

import android.content.Context
import android.util.Log
import com.siamatic.tms.constants.DEVICE_ID
import com.siamatic.tms.constants.EMAIL_PASSWORD
import com.siamatic.tms.constants.debugTag
import com.siamatic.tms.database.Temp
import com.siamatic.tms.defaultCustomComposable
import com.siamatic.tms.util.sharedPreferencesClass
import java.io.File
import java.util.Properties
import javax.activation.DataHandler
import javax.activation.FileDataSource
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

class SendEmail {
  private val senderEmail = "rnd.thanes@gmail.com"
  private var password =  ""
  private var deviceId = ""
  private val mailHost = "smtp.gmail.com"

  fun sendEmail(context: Context, data: List<Temp>, fileName: String, receiverEmail: String, startDate: String, endDate: String): Boolean {
    val prefev = sharedPreferencesClass(context)
    password = prefev.getPreference(EMAIL_PASSWORD, "String", "").toString()
    deviceId = prefev.getPreference(DEVICE_ID, "String", "").toString()
    if (password != "" && deviceId != "") {
      try {
        val mailProperties: Properties = System.getProperties()

        mailProperties.setProperty("mail.host", mailHost)
        mailProperties["mail.smtp.host"] = mailHost
        mailProperties["mail.smtp.port"] = "465"
        mailProperties["mail.smtp.socketFactory.fallback"] = "false"
        mailProperties.setProperty("mail.smtp.quitwait", "false")
        mailProperties["mail.smtp.socketFactory.port"] = "465"
        mailProperties["mail.smtp.starttls.enable"] = "true"
        mailProperties["mail.smtp.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"
        mailProperties["mail.smtp.enable"] = "true"
        mailProperties["mail.smtp.auth"] = "true"

        val session: Session = Session.getInstance(mailProperties, object : Authenticator() {
          override fun getPasswordAuthentication(): PasswordAuthentication {
            return PasswordAuthentication(senderEmail, password)
          }
        }).apply {
          debug = true  // จะ log SMTP interaction ลง Logcat
        }

        val mimeMessage = MimeMessage(session)
        // Body Email
        val messageBodyPart = MimeBodyPart()
        val multipart = MimeMultipart()

        // ไฟล์ CSV
        val csvFile = File(context.getExternalFilesDir(null), "$fileName.csv")
        csvFile.bufferedWriter().use { csv ->
          // header row
          csv.write("No.,Serial Number,Time,Temp 1,Temp 2")
          csv.newLine()

          // data rows
          data.forEachIndexed { index, value ->
            val timeString = value.timeStr.split(":")
            val temp1 = value.temp1.toString().split(".")
            val temp2 = value.temp2.toString().split(".")
            val txt = "${index + 1},$deviceId,${defaultCustomComposable.formatTwoIndex(timeString[0], "Start")}:${defaultCustomComposable.formatTwoIndex(timeString[1], "End")},${defaultCustomComposable.formatTwoIndex(temp1[0], "Start")}.${defaultCustomComposable.formatTwoIndex(temp1[1], "End")},${defaultCustomComposable.formatTwoIndex(temp2[0], "Start")}.${defaultCustomComposable.formatTwoIndex(temp2[1], "End")}"
            //Log.i(debugTag, "Line: $txt")
            csv.write(txt)
            csv.newLine()
          }
        }

        // สำหรับเพิ่มไฟล์ลง Email
        val attachment = MimeBodyPart()
        val fileSource = FileDataSource(csvFile)

        // เซ็ตผู้ส่ง
        mimeMessage.setFrom(InternetAddress(senderEmail))
        mimeMessage.addRecipient(Message.RecipientType.TO, InternetAddress(receiverEmail))
        // เซ็ตหัวข้อ Email (Subject)
        mimeMessage.subject = "Temperature Report"

        // เซ็ต Body Email
        messageBodyPart.setText("รายงานการบันทึกผลอุณหภูมิ ณ วันที่ $startDate ถึงวันที่ $endDate\n ")
        multipart.addBodyPart(messageBodyPart)

        // ตั้งค่าไฟล์
        attachment.setDataHandler(DataHandler(fileSource))
        // ตั้งค่าชื่อไฟล์บน Email body
        attachment.fileName = csvFile.name
        multipart.addBodyPart(attachment)

        // เพิ่มไฟล์ลงใน Email
        mimeMessage.setContent(multipart)
        // ส่งเมล
        Transport.send(mimeMessage)
        Log.i(debugTag, "Email sent successfully to $receiverEmail with attachment: ${csvFile.absolutePath}")
        return true
      } catch (error: Exception) {
        Log.e(debugTag, "Error in send email: $error")
        return false
      }
    } else {
      Log.e(debugTag, "Error in getting config")
      return false
    }
  }
}