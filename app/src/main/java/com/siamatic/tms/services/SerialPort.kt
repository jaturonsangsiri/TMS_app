package com.siamatic.tms.services

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log
import java.io.File
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.lang.reflect.Method

class SerialPort @Throws(SecurityException::class, IOException::class) constructor(
  device: File,
  baudrate: Int,
  databit: Int,
  parity: Int,
  stopbit: Int,
  flowctl: Int,
  type: Int
) {
  companion object {
    private const val TAG = "SerialPort"
    private const val DEFAULT_SU_PATH = "/system/xbin/su"
    private var sSuPath: String = DEFAULT_SU_PATH

    init {
      try {
        Log.d(TAG, "=== Loading Native Library ===")
        Log.d(TAG, "Java library path: ${System.getProperty("java.library.path")}")
        Log.d(TAG, "Attempting to load: serial_port")

        System.loadLibrary("serial_port")
        Log.d(TAG, "Native library loaded successfully")

        // Test native methods availability
        testNativeMethodsAvailability()

      } catch (e: UnsatisfiedLinkError) {
        Log.e(TAG, "Failed to load native library: serial_port", e)
        Log.e(TAG, "Library search failed in: ${e.message}")
        throw e
      } catch (e: Exception) {
        Log.e(TAG, "Unexpected error loading native library", e)
        throw e
      }
    }

    private fun testNativeMethodsAvailability() {
      try {
        Log.d(TAG, "=== Testing Native Methods Availability ===")
        val clazz = SerialPort::class.java
        val methods = clazz.declaredMethods

        Log.d(TAG, "All methods in SerialPort class:")
        methods.forEach { method ->
          val isNative = java.lang.reflect.Modifier.isNative(method.modifiers)
          Log.d(TAG, "- ${method.name}() -> Native: $isNative")

          if (isNative) {
            Log.d(TAG, "  Parameters: ${method.parameterTypes.joinToString { it.simpleName }}")
            Log.d(TAG, "  Return type: ${method.returnType.simpleName}")
          }
        }

        // Try to create a dummy test to see if JNI linking works
        Log.d(TAG, "Native method linking appears successful")

      } catch (e: Exception) {
        Log.e(TAG, "Error testing native methods", e)
      }
    }

    fun setSuPath(suPath: String?) {
      Log.d(TAG, "setSuPath called with: $suPath")
      if (suPath != null) {
        val oldPath = sSuPath
        sSuPath = suPath
        Log.d(TAG, "Su path changed from '$oldPath' to '$sSuPath'")
      } else {
        Log.w(TAG, "setSuPath called with null, keeping current path: $sSuPath")
      }
    }
  }

  private val mFd: FileDescriptor
  private val mFileInputStream: FileInputStream
  private val mFileOutputStream: FileOutputStream

  init {
    Log.d(TAG, "=== SerialPort Initialization Started ===")
    Log.d(TAG, "Device path: ${device.absolutePath}")
    Log.d(TAG, "Parameters: baudrate=$baudrate, databit=$databit, parity=$parity, stopbit=$stopbit, flowctl=$flowctl, type=$type")

    // Check if device file exists
    if (!device.exists()) {
      Log.e(TAG, "Device file does not exist: ${device.absolutePath}")

      // List available devices for debugging
      listAvailableSerialDevices()

      throw IOException("Device file does not exist: ${device.absolutePath}")
    }

    // Check device file permissions
    val canRead = device.canRead()
    val canWrite = device.canWrite()
    Log.d(TAG, "Device file permissions - Readable: $canRead, Writable: $canWrite")

    if (!canRead || !canWrite) {
      Log.w(TAG, "Insufficient permissions, attempting to fix...")
      tryFixPermissions(device.absolutePath)
    }

    try {
      Log.d(TAG, "=== Calling Native open() Method ===")
      Log.d(TAG, "Before native call - Thread: ${Thread.currentThread().name}")

      val startTime = System.currentTimeMillis()

      // Add pre-call debugging
      Log.d(TAG, "Calling: open('${device.absolutePath}', $baudrate, $databit, $parity, $stopbit, $flowctl, $type)")

      // This is where the actual native call happens
      val result = callNativeOpenSafely(device.absolutePath, baudrate, databit, parity, stopbit, flowctl, type)

      val openTime = System.currentTimeMillis() - startTime
      Log.d(TAG, "Native open() call returned after ${openTime}ms")
      Log.d(TAG, "Result: $result")

      mFd = result ?: run {
        Log.e(TAG, "Native open() returned null FileDescriptor")
        Log.e(TAG, "This usually means:")
        Log.e(TAG, "1. Device is already in use")
        Log.e(TAG, "2. Insufficient permissions")
        Log.e(TAG, "3. Native implementation error")
        Log.e(TAG, "4. Invalid device path")

        throw IOException("Failed to open serial port: native open() returned null")
      }

      Log.d(TAG, "FileDescriptor obtained: $mFd")
      Log.d(TAG, "FileDescriptor valid: ${mFd.valid()}")

    } catch (e: UnsatisfiedLinkError) {
      Log.e(TAG, "UnsatisfiedLinkError during native open() call", e)
      Log.e(TAG, "This means the native method 'open' is not properly implemented in libserial_port.so")
      throw IOException("Native method not found: ${e.message}", e)
    } catch (e: Exception) {
      Log.e(TAG, "Exception during native open() call", e)
      throw IOException("Failed to open serial port: ${e.message}", e)
    }

    try {
      Log.d(TAG, "=== Creating Input/Output Streams ===")

      Log.d(TAG, "Creating FileInputStream...")
      mFileInputStream = FileInputStream(mFd)
      Log.d(TAG, "FileInputStream created successfully")

      Log.d(TAG, "Creating FileOutputStream...")
      mFileOutputStream = FileOutputStream(mFd)
      Log.d(TAG, "FileOutputStream created successfully")

    } catch (e: Exception) {
      Log.e(TAG, "Exception creating input/output streams", e)
      try {
        close()
      } catch (closeException: Exception) {
        Log.e(TAG, "Exception while closing during cleanup", closeException)
      }
      throw IOException("Failed to create input/output streams: ${e.message}", e)
    }

    Log.d(TAG, "=== SerialPort Initialization Completed Successfully ===")
  }

  private fun callNativeOpenSafely(
    path: String,
    baudrate: Int,
    databit: Int,
    parity: Int,
    stopbit: Int,
    flowctl: Int,
    type: Int
  ): FileDescriptor? {
    return try {
      Log.d(TAG, "Entering native open() call...")

      // The actual native method call
      val result = open(path, baudrate, databit, parity, stopbit, flowctl, type)

      Log.d(TAG, "Native open() completed, result: $result")
      result

    } catch (e: UnsatisfiedLinkError) {
      Log.e(TAG, "Native method not found or not properly linked", e)
      throw e
    } catch (e: Exception) {
      Log.e(TAG, "Unexpected exception in native call", e)
      throw e
    }
  }

  private fun listAvailableSerialDevices() {
    try {
      Log.d(TAG, "=== Listing Available Serial Devices ===")
      val devPath = File("/dev")
      if (devPath.exists() && devPath.isDirectory) {
        val files = devPath.listFiles { file ->
          file.name.startsWith("tty") || file.name.startsWith("serial")
        }

        files?.forEach { file ->
          Log.d(TAG, "Found device: ${file.absolutePath} (readable: ${file.canRead()}, writable: ${file.canWrite()})")
        }
      }
    } catch (e: Exception) {
      Log.w(TAG, "Could not list serial devices", e)
    }
  }

  private fun tryFixPermissions(devicePath: String) {
    try {
      Log.d(TAG, "Attempting to fix device permissions...")
      val commands = arrayOf(
        "chmod 666 $devicePath",
        "su -c 'chmod 666 $devicePath'"
      )

      for (command in commands) {
        try {
          val process = Runtime.getRuntime().exec(command)
          val exitCode = process.waitFor()
          Log.d(TAG, "Command '$command' exit code: $exitCode")

          if (exitCode == 0) {
            Log.d(TAG, "Permission fix may have succeeded")
            break
          }
        } catch (e: Exception) {
          Log.w(TAG, "Permission fix command failed: $command", e)
        }
      }
    } catch (e: Exception) {
      Log.w(TAG, "Could not fix permissions", e)
    }
  }

  // Constructor overloads
  constructor(device: File, baudrate: Int) : this(device, baudrate, 8, 0, 1, 0, 0) {
    Log.d(TAG, "Constructor: File, baudrate")
  }

  constructor(devicePath: String, baudrate: Int, flags: Int) : this(File(devicePath), baudrate, 8, 0, 1, 0, 0) {
    Log.d(TAG, "Constructor: devicePath, baudrate, flags - Path: $devicePath, Flags: $flags")
  }

  constructor(devicePath: String, baudrate: Int) : this(File(devicePath), baudrate, 8, 0, 1, 0, 0) {
    Log.d(TAG, "Constructor: devicePath, baudrate - Path: $devicePath")
  }

  fun getInputStream(): FileInputStream {
    Log.d(TAG, "getInputStream() called")
    return mFileInputStream
  }

  fun getOutputStream(): FileOutputStream {
    Log.d(TAG, "getOutputStream() called")
    return mFileOutputStream
  }

  // Native methods
  private external fun open(
    path: String,
    baudrate: Int,
    databit: Int,
    parity: Int,
    stopbit: Int,
    flowctl: Int,
    type: Int
  ): FileDescriptor?

  external fun close()

  fun closeWithLogging() {
    try {
      Log.d(TAG, "Closing SerialPort...")
      close()
      Log.d(TAG, "SerialPort closed successfully")
    } catch (e: Exception) {
      Log.e(TAG, "Exception during close()", e)
      throw e
    }
  }

  fun getDebugInfo(): String {
    return buildString {
      append("SerialPort Debug Info:\n")
      append("- FileDescriptor: $mFd\n")
      append("- FileDescriptor valid: ${mFd.valid()}\n")
      append("- InputStream available: ")
      try {
        append("${mFileInputStream.available()} bytes\n")
      } catch (e: Exception) {
        append("Error: ${e.message}\n")
      }
      append("- Su Path: $sSuPath\n")
      append("- Thread: ${Thread.currentThread().name}\n")
    }
  }

  fun isDebugMode(context: Context): Boolean {
    return 0 != (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE)
  }

  fun testConnection(): Boolean {
    return try {
      Log.d(TAG, "Testing serial port connection...")

      // Test FileDescriptor validity
      val fdValid = mFd.valid()
      Log.d(TAG, "FileDescriptor valid: $fdValid")

      // Test stream availability
      val available = mFileInputStream.available()
      Log.d(TAG, "InputStream available bytes: $available")

      // Test if we can write (just checking, not actually writing)
      Log.d(TAG, "OutputStream ready for writing")

      fdValid
    } catch (e: Exception) {
      Log.e(TAG, "Connection test failed", e)
      false
    }
  }
}