/**
 *****************************************************************************
 Copyright (c) 2015-16 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Mike Tran - Initial Contribution
 Sathiskumar Palaniappan - Initial Contribution
 *****************************************************************************
 *
 */
package com.ibm.iotf.sample.devicemgmt.device;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ibm.iotf.devicemgmt.DeviceFirmware;
import com.ibm.iotf.devicemgmt.device.ManagedDevice;

/**
 * This Platform Initiated Firmware handler demonstrates how the Firmware Upgrade 
 * is performed, with the package being downloaded by providing the URL over the 
 * Platform and have it upgraded in simple steps. On failure, the sample performs 
 * the factory reset.
 */

public class PlatformInitiatedHandlerSample extends Handler {
	
	private static ExecutorService executor = Executors.newSingleThreadExecutor();

	public PlatformInitiatedHandlerSample(ManagedDevice dmClient) {
		super(dmClient);
	}
	
	private ManagedDevice managedDevice;
	
	@Override
	public void prepare(String propertiesFile) {
		
		// Create download task
		downloadTask = new HTTPFirmwareDownload(true, managedDevice);
		
		// Create update task
		updateTask = new DebianFirmwareUpdate(false);
		
		try {
			managedDevice.addFirmwareHandler(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Create Handler
	 * Add it to Library
	 * Make the platform to call back the method / function
	 * https://github.com/ibm-watson-iot/iot-java/blob/master/docs/java_cli_for_manageddevice.rst
	 */
	
	@Override
	public void downloadFirmware(final DeviceFirmware deviceFirmware) {
		
		Runnable r = new Runnable() {
			public void run() {
				downloadTask.setDeviceFirmware(deviceFirmware);
				setLatestFirmware(downloadTask.downloadFirmware());
				System.out.println("Latest Firmware is " +latestFirmware);
			}
		};
		
		executor.execute(r);
	}
	
	@Override
	public void updateFirmware(final DeviceFirmware deviceFirmware) {
		Runnable r = new Runnable(){
			public void run(){
				updateTask.setDeviceFirmware(deviceFirmware);
				boolean status = updateTask.updateFirmware(getLatestFirmware());
				
				System.out.println("value of status is " +status);
				
				/**
				 * Trigger the upgrade to the latest firmware
				 */
				if ( status == true ){
					System.out.println("Successfully Upgraded Latest Firmware");
					setCurrentFirmware(getLatestFirmware());
				} else {
					System.out.println("Upgrade failed. Reverting back to the current version");
					status = updateTask.updateFirmware(getLatestFirmware());
					if (status == true) {
						System.out.println("Retained Current Firmware as is ");
					} else {
						updateTask.updateFirmware(Handler.FACTORY_FIRMWARE);
						System.out.println("Restored Factory Firmware version after failing to revert back to Current version");
						setCurrentFirmware(Handler.FACTORY_FIRMWARE);
					} 
				}
			}
		};
		
		executor.execute(r);
	}
	
	private static String trimedValue(String value) {
		if(value == null || value == "") {
			return "";
		} else {
			return value.trim();
		}
	}

}	