package com.hoho.android.usbserial.hardcopy;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.example.heavyautometer.igeoscanpreapp.Preview;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;



public class SerialConnector {
	private static String ACTION_USB_PERMISSION = "android.hardware.usb.action.USB_PERMISSION";
	public static final String tag = "SerialConnector";

	private Context mContext;
	private Preview.SerialListener mListener;
	private Handler mHandler;
	
	private SerialMonitorThread mSerialThread;
	
	private UsbSerialDriver mDriver;
	private UsbSerialPort mPort;
	
	public static final int TARGET_VENDOR_ID = 9025;	// Arduino
	public static final int TARGET_VENDOR_ID2 = 1659;	// PL2303
	public static final int TARGET_VENDOR_ID3 = 1027;	// FT232R
	public static final int TARGET_VENDOR_ID4 = 6790;	// CH340G
	public static final int TARGET_VENDOR_ID5 = 4292;	// CP210x
	public static final int BAUD_RATE = 115200;




	/*****************************************************
	*	Constructor, Initialize
	******************************************************/
	public SerialConnector(Context c, Preview.SerialListener l, Handler h) {
		mContext = c;
		mListener = l;
		mHandler = h;
//		Toast.makeText(c,c + " : "+l+" : "+h,Toast.LENGTH_LONG).show();
	}
	
	
	public void initialize(Context c) {
		UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
		List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
		if (availableDrivers.isEmpty()) {
			mListener.onReceive(Constants.MSG_SERIAL_ERROR, 0, 0, "Error: There is no available device. \n", null);
			return;
		}
		
		mDriver = availableDrivers.get(0);
		if(mDriver == null) {
			mListener.onReceive(Constants.MSG_SERIAL_ERROR, 0, 0, "Error: Driver is Null \n", null);
			return;
		}
		
		// Report to UI
		StringBuilder sb = new StringBuilder();
//		UsbDevice device = mDriver.getDevice();

		Intent intent = new Intent();

		HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
		UsbDevice device2 = deviceList.get("deviceName"); //http://eminentstar.tistory.com/29
		UsbDevice device = mDriver.getDevice();
//		UsbDevice device;

//		Toast.makeText(c, "devices : " + deviceList + " original device : "+ device, Toast.LENGTH_LONG).show();


		Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
		while(deviceIterator.hasNext()){
			UsbDevice select_device = deviceIterator.next();
			//your code
//			Toast.makeText(c, "select_device : " + select_device, Toast.LENGTH_LONG).show();
			if(select_device.getVendorId() == 9025) { //ch340 is 6790, 32u4 is 9025
//				Toast.makeText(c, "select_device : " + select_device, Toast.LENGTH_LONG).show();
				device = select_device;

			}
		}



		sb.append(" DName : ").append(device.getDeviceName()).append("\n")
			.append(" DID : ").append(device.getDeviceId()).append("\n")
			.append(" VID : ").append(device.getVendorId()).append("\n")
			.append(" PID : ").append(device.getProductId()).append("\n")
			.append(" IF Count : ").append(device.getInterfaceCount()).append("\n");
		mListener.onReceive(Constants.MSG_DEVICD_INFO, 0, 0, sb.toString(), null); //장치 가져오기 까지 됨
		Toast.makeText(c,"devices : "+Constants.MSG_DEVICD_INFO, Toast.LENGTH_LONG).show();


//		PendingIntent permissionIntent = PendingIntent.getBroadcast(c, 0, new Intent(ACTION_USB_PERMISSION), 0);

		PendingIntent mPermissionIntent = PendingIntent.getBroadcast(c, 0, new Intent(ACTION_USB_PERMISSION), 0);
//		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
//		registerReceiver(mUsbReceiver, filter);
		manager.requestPermission(device, mPermissionIntent);





		UsbDeviceConnection connection = manager.openDevice(device);
//		manager.requestPermission(device, permissionIntent);
		boolean hasPermision = manager.hasPermission(device);
		Toast.makeText(c,"hasPermission : "+hasPermision, Toast.LENGTH_LONG).show();


		if (connection == null) {
			mListener.onReceive(Constants.MSG_SERIAL_ERROR, 0, 0, "Error: Cannot connect to device. \n", null);
			return;
		}
		
		// Read some data! Most have just one port (port 0).
		mPort = mDriver.getPorts().get(0);
		if(mPort == null) {
			mListener.onReceive(Constants.MSG_SERIAL_ERROR, 0, 0, "Error: Cannot get port. \n", null);
			return;
		}
		
		try {
			mPort.open(connection);
			mPort.setParameters(9600, 8, 1, 0);		// baudrate:9600, dataBits:8, stopBits:1, parity:N
//			byte buffer[] = new byte[16];
//			int numBytesRead = mPort.read(buffer, 1000);
//			Log.d(TAG, "Read " + numBytesRead + " bytes.");
		} catch (IOException e) {
			// Deal with error.
			mListener.onReceive(Constants.MSG_SERIAL_ERROR, 0, 0, "Error: Cannot open port \n" + e.toString() + "\n", null);
		} finally {
		}
		
		// Everything is fine. Start serial monitoring thread.
		startThread();
	}	// End of initialize()
	
	public void finalize() {
		try {
			mDriver = null;
			stopThread();
			
			mPort.close();
			mPort = null;
		} catch(Exception ex) {
			mListener.onReceive(Constants.MSG_SERIAL_ERROR, 0, 0, "Error: Cannot finalize serial connector \n" + ex.toString() + "\n", null);
		}
	}
	
	
	
	/*****************************************************
	*	public methods
	******************************************************/
	// send string to remote
	public void sendCommand(String cmd) {
//		byte[] test_command = {0x00, 0x01};

		if(mPort != null && cmd != null) {
			try {
				mPort.write(cmd.getBytes(), cmd.length());		// Send to remote device
//				mPort.write(test_command, test_command.length);
			}
			catch(IOException e) {
				mListener.onReceive(Constants.MSG_SERIAL_ERROR, 0, 0, "Failed in sending command. : IO Exception \n", null);
			}
		}
	}


	/**
	 *
	 * @param value : String amending_angle_value
	 */
	public void amend_angle_value(String value){

		if(mPort != null && value != null) {
			try {
				String A = "A"; //arduino will understand that amending anlge value
				mPort.write(A.getBytes(), A.length());
				mPort.write(value.getBytes(), value.length());		// Send to remote device
//				mPort.write(test_command, test_command.length);
			}
			catch(IOException e) {
				mListener.onReceive(Constants.MSG_SERIAL_ERROR, 0, 0, "Failed in sending command. : IO Exception \n", null);
			}
		}

	}



	/*********
	 *
	 * @param direction 0~9 direction
	 */
	public void motor_contol_command(int direction){


//  android / arduino
//  0x00  0
//  0x01  1
//  0x02  2
//  0x03  3
//  0x04  4
//  0x05  5
//  0x06  6
//  0x07  7
//  0x08  8
//  0x09  9
//  0x10  16
//  0x11  17
//  0x12  18
//  0x13  19
//  0x14  20
//  0x15  21
//  0x16  22
//  0x17  23
//  0x18  24
//  0x19  25
//  0x79  121

		byte[] command_bytes = new byte[3];

		command_bytes[0] = 0x10; // motor control_command
		switch (direction){
			case 1 :
				command_bytes[1] = 0x01; //101
				break;

			case 2 :
				command_bytes[1] = 0x02; //101
				break;

			case 3 :
				command_bytes[1] = 0x03; //101
				break;

			case 4 :
				command_bytes[1] = 0x04; //101
				break;

			case 5 :
				command_bytes[1] = 0x05; //101
				break;

			case 6 :
				command_bytes[1] = 0x06; //101
				break;

			case 7 :
				command_bytes[1] = 0x07; //101
				break;

			case 8 :
				command_bytes[1] = 0x08; //101
				break;

			case 9 :
				command_bytes[1] = 0x09; //101
				break;

			case 0 :
				command_bytes[1] = 0x00; //stop motor
				break;

		}
		command_bytes[1] = (byte) direction; //101
		command_bytes[2] = 0x79; //end_byte



		if(mPort != null) {
			try {

				mPort.write(command_bytes,3);
//				mPort.write(cmd.getBytes(), cmd.length());		// Send to remote device
//				mPort.write(test_command, test_command.length);
			}
			catch(IOException e) {
				mListener.onReceive(Constants.MSG_SERIAL_ERROR, 0, 0, "Failed in sending command. : IO Exception \n", null);
			}
		}

	}







	/*****************************************************
	*	private methods
	******************************************************/
	// start thread
	private void startThread() {
		Log.d(tag, "Start serial monitoring thread");
		mListener.onReceive(Constants.MSG_SERIAL_ERROR, 0, 0, "Start serial monitoring thread \n", null);
		if(mSerialThread == null) {
			mSerialThread = new SerialMonitorThread();
			mSerialThread.start();
		}	
	}
	// stop thread
	private void stopThread() {
		if(mSerialThread != null && mSerialThread.isAlive())
			mSerialThread.interrupt();
		if(mSerialThread != null) {
			mSerialThread.setKillSign(true);
			mSerialThread = null;
		}
	}
	
	
	
	
	
	/*****************************************************
	*	Sub classes, Handler, Listener
	******************************************************/
	
	public class SerialMonitorThread extends Thread {
		// Thread status
		private boolean mKillSign = false;
		private SerialCommand mCmd = new SerialCommand();
		
		
		private void initializeThread() {
			// This code will be executed only once.
		}
		
		private void finalizeThread() {
		}
		
		// stop this thread
		public void setKillSign(boolean isTrue) {
			mKillSign = isTrue;
		}
		
		/**
		*	Main loop
		**/
		@Override
		public void run() 
		{
			byte buffer[] = new byte[128];
			
			while(!Thread.interrupted())
			{
				if(mPort != null) {
					Arrays.fill(buffer, (byte)0x00);
					
					try {
						// Read received buffer
						int numBytesRead = mPort.read(buffer, 1000);
						if(numBytesRead > 0) {
							Log.d(tag, "run : read bytes = " + numBytesRead);
							
							// Print message length
							Message msg = mHandler.obtainMessage(Constants.MSG_READ_DATA_COUNT, numBytesRead, 0, 
									new String(buffer));
							mHandler.sendMessage(msg);
							
							// Extract data from buffer
							for(int i=0; i<numBytesRead; i++) {
								char c = (char)buffer[i];
								if(c == 'z') {
									// This is end signal. Send collected result to UI
									if(mCmd.mStringBuffer != null && mCmd.mStringBuffer.length() < 20) {
										Message msg1 = mHandler.obtainMessage(Constants.MSG_READ_DATA, 0, 0, mCmd.toString());
										mHandler.sendMessage(msg1);
									}
								} else {
									mCmd.addChar(c);
								}
							}
						} // End of if(numBytesRead > 0)
					} 
					catch (IOException e) {
						Log.d(tag, "IOException - mDriver.read");
						Message msg = mHandler.obtainMessage(Constants.MSG_SERIAL_ERROR, 0, 0, "Error # run: " + e.toString() + "\n");
						mHandler.sendMessage(msg);
						mKillSign = true;
					}
				}

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
					break;
				}
				
				if(mKillSign)
					break;
				
			}	// End of while() loop
			
			// Finalize
			finalizeThread();
			
		}	// End of run()
		
		
	}	// End of SerialMonitorThread



}
