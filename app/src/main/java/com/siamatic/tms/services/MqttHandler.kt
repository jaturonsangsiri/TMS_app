package com.siamatic.tms.services

import android.util.Log
import com.siamatic.tms.constants.debugTag
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

class MqttHandler(private val broker: String, clientId: String, private val username: String? = null, private val passwordMQTT: String? = null) {
  private val client = MqttClient(broker, clientId, MemoryPersistence())

  private fun isNotConnect(): Boolean {
    return if (!client.isConnected) {
      Log.e(debugTag, "MQTT is not connect!!")
      false
    } else {
      true
    }
  }

  fun connect(): Boolean {
    return try {
      val options = MqttConnectOptions().apply {
        isCleanSession = true
        connectionTimeout = 5
        keepAliveInterval = 30
        username?.let { userName = it}
        passwordMQTT?.let { this.password = it.toCharArray() }
      }

      client.setCallback(object : MqttCallback {
        override fun connectionLost(cause: Throwable?) {
          Log.i(debugTag, "Connection of MQTT has been lost: ${cause?.message}")
        }

        override fun messageArrived(topic: String?, message: MqttMessage?) {
          Log.d(debugTag, "Message of MQTT has received on topic: $topic: ${message.toString()}")
        }

        override fun deliveryComplete(token: IMqttDeliveryToken?) {
          Log.i(debugTag, "MQTT delivery complete")
        }
      })

      client.connect(options)
      Log.i(debugTag, "Connected to broker: $broker")
      true
    } catch (e: MqttException) {
      Log.e(debugTag, "Error in MQTT while connect to server: ${e.reasonCode} ${e.message}", e)
      false
    }
  }

  fun subscribe(topic: String, qos: Int = 1): Boolean {
    return try {
      if (isNotConnect()) {
        client.subscribe(topic, qos)
        Log.d(debugTag, "MQTT subscribe to $topic")
        true
      } else {
        false
      }
    } catch (e: MqttException) {
      Log.e(debugTag, "There are error while subscribe $topic: ${e.message}")
      false
    }
  }

  fun publish(topic: String, message: String, qos: Int = 1): Boolean {
    return try {
      if (isNotConnect()) {
        val mqttMessage = MqttMessage(message.toByteArray()).apply { this.qos = qos }
        client.publish(topic, mqttMessage)
        println("MQTT Message published to $topic: $message")
        true
      } else {
        false
      }
    } catch (e: MqttException) {
      Log.e(debugTag, "There are error while publish $topic: ${e.message}")
      false
    }
  }

  // หยุดการเชื่อมต่อ MQTT
  fun disconnect(): Boolean {
    return try {
      if (client.isConnected) {
        client.disconnect()
        Log.i(debugTag, "MQTT is disconnected from $broker")
      }
      true
    } catch (e: MqttException) {
      Log.e(debugTag, "Failed to disconnect: ${e.message}")
      false
    }
  }
}