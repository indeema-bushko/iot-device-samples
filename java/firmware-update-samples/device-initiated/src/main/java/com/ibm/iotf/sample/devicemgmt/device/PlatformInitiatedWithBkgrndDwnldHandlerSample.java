/**
 *****************************************************************************
 Copyright (c) 2016 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Sathiskumar Palaniappan - Initial Contribution
 Prasanna Alur Mathada - Initial Contribution
 *****************************************************************************
 *
 */

package com.ibm.iotf.sample.devicemgmt.device;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import com.ibm.iotf.devicemgmt.DeviceFirmware;

import com.ibm.iotf.devicemgmt.device.ManagedDevice;


/**
 * This Platform Initiated Firmware handler demonstrates how the Firmware Upgrade 
 * is performed, with the package being downloaded by providing the URL over the 
 * Platform ( with the download action being performed in the background, without
 * affecting the foreground applications) and have it upgraded in simple steps. 
 * On failure, the sample performs the factory reset.
 */

public class PlatformInitiatedWithBkgrndDwnldHandlerSample extends PlatformInitiatedHandlerSample{
	
//	private static ExecutorService executor = Executors.newSingleThreadExecutor();

	public PlatformInitiatedWithBkgrndDwnldHandlerSample(ManagedDevice dmClient) {
		super(dmClient);
		
		// TODO Auto-generated constructor stub
	}

	
//	private Database firmwareDB;
//	private String firmwareDBSequence;
//	private String docId;
//	private static String currentFirmware = "iot_1.0-2_armhf.deb";
//	private String currentFirmwareVersion;
//	private String latestFirmwareVersion;
	private static String latestFirmware;
	
//	private URL firmwareURL = null;
	
	// private static ExecutorService executor = Executors.newSingleThreadExecutor();
//	private static final String CLASS_NAME = PlatformInitiatedWithBkgrndDwnldHandlerSample.class.getName();
	private ManagedDevice managedDevice;
//	private ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);

	
	private ExecutorService exec = Executors.newFixedThreadPool(1,
	        new ThreadFactory() {
	            public Thread newThread(Runnable r) {
	                Thread t = Executors.defaultThreadFactory().newThread(r);
	                t.setDaemon(true);
	                return t;
	            }
	        });
	
	
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
		
		exec.execute(r);
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
		
		exec.execute(r);
	}
	
	private static String trimedValue(String value) {
		if(value == null || value == "") {
			return "";
		} else {
			return value.trim();
		}
	}
	
}	
	