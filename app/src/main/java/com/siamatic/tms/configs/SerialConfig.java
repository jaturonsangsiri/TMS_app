package com.siamatic.tms.configs;

// การตั้งค่า RS232 ซึ่งเป็นแบบ 8N1 หรือ 8 data bits, N none parity bit, 1 stop bit
// ข้อมูลที่ได้จาก RS232(บิตแบบไฟฟ้าอนุกรม) คือ Start(0) BIT | 1 0 0 0 0 0 1 0 | Stop(1) BIT
// ข้อมูลที่ได้จาก Micro-USB(เลขฐาน 16 Heximal) คือ 41
public class SerialConfig  {
  public static final int BAUD_RATE = 9600; // Bit rate per second Ex. 9600, 19200, 115200 bps
  public static final byte DATA_BIT = 8; // data send per time: 7, 8 bit EX. 01000001 = 8Bit
  public static final byte PARITY = 0; // การตรวจสอบความถูกต้องของข้อมูล (error checking):      0 = none, 1 = odd, 2 = even, 3 = mark, 4 = space
  public static final byte STOP_BIT = 1; // จำนวน stop bit: 1 หรือ 2
  public static final byte FLOW_CONTROL = 0; // การควบคุมการไหลของข้อมูล:     0 = none, 1 = ใช้ CTS/RTS (hardware flow control)
}