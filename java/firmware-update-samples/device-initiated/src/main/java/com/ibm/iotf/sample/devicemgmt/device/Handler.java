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

import com.ibm.iotf.devicemgmt.DeviceFirmwareHandler;
import com.ibm.iotf.devicemgmt.device.ManagedDevice;

public abstract class Handler extends DeviceFirmwareHandler {
	
	protected static String FACTORY_FIRMWARE_NAME = "iot_1.0-1_armhf.deb";
	protected static String FACTORY_FIRMWARE_VERSION = "1.0.1";
	protected ManagedDevice dmClient = null;
	
	public Handler(ManagedDevice dmClient) {
		this.dmClient = dmClient;
	}
	
	protected abstract void prepare(String propertiesFile);
	
	protected String currentFirmware = "iot_1.0-2_armhf.deb";
	protected String latestFirmware;
	protected HTTPFirmwareDownload downloadTask;
	protected DebianFirmwareUpdate updateTask;
	
	protected String getCurrentFirmware() {
		return currentFirmware;
	}
	
	protected void setCurrentFirmware(String currentFirmware) {
		this.currentFirmware = currentFirmware;
	}
	
	protected String getLatestFirmware() {
		return latestFirmware;
	}
	
	protected void setLatestFirmware(String latestFirmware) {
		this.latestFirmware = latestFirmware;
	}
	
	public static Handler createHandler(String option, ManagedDevice dmClient) {
		switch(option) {
			case "Device": return new DeviceInitiatedHandlerSample(dmClient);
			
			case "Platform": return new PlatformInitiatedHandlerSample(dmClient);
			
			case "PlatformBackground": return new PlatformInitiatedWithBkgrndDwnldHandlerSample(dmClient);
		}
		
		return null;
	}
}
