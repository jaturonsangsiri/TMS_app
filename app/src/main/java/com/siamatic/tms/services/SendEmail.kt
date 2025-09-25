package com.siamatic.tms.services

import android.content.Context
import android.util.Log
import com.siamatic.tms.constants.debugTag
import com.siamatic.tms.services.excel.CreateExcel
import java.io.File
import java.io.FileOutputStream
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
  private val password = "bbuvdgxybmrgqzcc"
  private val mailHost = "smtp.gmail.com"

  fun sendEmail(context: Context, fileName: String, receiverEmail: String, startDate: String, endDate: String): Boolean {
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

      // ไฟล์ Exel
      val excel = CreateExcel()

      // Excel headers
      excel.addCell(0, 0, "Date")
      excel.addCell(0, 1, "Temperature")
      // Excel Data
      excel.addCell(1, 0, "tableData[0].temp1")
      excel.addCell(1, 1, "tableData[0].temp2")

      // เซฟไฟล์ Excel ลงเครื่อง
      val outFile = File(context.getExternalFilesDir(null), "$fileName.xlsx")

      val fos = FileOutputStream(outFile)
      excel.exportToFile(fos)
      fos.close()

      // สำหรับเพิ่มไฟล์ลง Email
      val attachment = MimeBodyPart()
      val fileSource = FileDataSource(outFile)

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
      attachment.fileName = fileSource.name
      multipart.addBodyPart(attachment)

      // เพิ่มไฟล์ลงใน Email
      mimeMessage.setContent(multipart)
      // ส่งเมล
      Transport.send(mimeMessage)
      Log.i(debugTag, "Email sent successfully to $receiverEmail with attachment: ${outFile.absolutePath}")
      return true
    } catch (error: Exception) {
      Log.e(debugTag, "Error in send email: $error")
      return false
    }
  }
}