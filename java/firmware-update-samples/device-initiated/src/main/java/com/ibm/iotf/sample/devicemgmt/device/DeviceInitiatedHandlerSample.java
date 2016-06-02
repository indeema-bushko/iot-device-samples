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

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Properties;

import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.cloudant.client.api.model.*;
import com.cloudant.client.api.model.ChangesResult.Row;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.ibm.iotf.devicemgmt.DeviceFirmware;
import com.ibm.iotf.devicemgmt.DeviceInfo;
import com.ibm.iotf.devicemgmt.device.ManagedDevice;

import org.apache.commons.net.util.Base64;
import org.eclipse.paho.client.mqttv3.MqttException;

/**
 * This Device Initiated Firmware handler demonstrates how the device snoops for the 
 * availability of the newer instance of firmware in the repository. Once a newer release
 * is available, then Firmware Upgrade is performed after downloading the package. 
 * On failure, the sample performs the factory reset.
 */

/**
 * Device Initiated Factory Reset
 * --------------------------------
 * Based on the Properties file parameter, as set by the user preference, the DeviceInitiated action shall be executed
 * based on the flow defined in this section. The Device shall snoop the Cloudant NoSQL DB ( Firmware Repository)
 * and checks for the availability of a newer version of firmware. If a newer firmware version is available, then it 
 * shall initiate download and subsequently trigger firmware update process. If upgrade fails, then, the action is to 
 * fall back on the Active firmware. If successful, then fine, else next action is to fall back on to the Base firmware
 * version 
 * 
 * Here, the flow is as follows:
 * 1. Snoop the Cloudant NoSQL DB to see if there's a newer version of firmware available for download
 * 2. If No, then, do nothing
 * 3. If Yes, then, cross verify, if the latest version is different from the Active Firmware available on the Device
 * 4. On confirmation, download the latest firmware and pass on the file information to the 'applyFirmware() method
 * 
 */

public class DeviceInitiatedHandlerSample extends Handler implements Runnable {
	
	public DeviceInitiatedHandlerSample(ManagedDevice dmClient) {
		super(dmClient);
	}

	private Database firmwareDB;
	private String firmwareDBSequence;
	private String docId;
	private String currentFirmwareVersion = "1.0.2";
	private String latestFirmwareVersion;
	
	/**
	 * Connect to Cloundant NoSQL DB based on the user inputs and 
	 * create the config DB if its not created already
	 * 
	 **/
	@Override
	public void prepare(String propertiesFile) {
	
		/**
		 * Load device properties
		 */
		Properties props = new Properties();
		try {
			props.load(DeviceInitiatedHandlerSample.class.getResourceAsStream(propertiesFile));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}
			
		String username = trimedValue(props.getProperty("User-Name"));
		String password = trimedValue(props.getProperty("Password"));
		
		currentFirmwareVersion = (props.getProperty("DeviceInfo.fwVersion"));

		StringBuilder sb = new StringBuilder();
		sb.append("https://")
		.append(username)
		.append(":")
		.append(password)
		.append("@")
		.append(username)
		.append(".cloudant.com");
			
		System.out.println(sb);
		
		CloudantClient client = new CloudantClient(sb.toString(), username, password);
			
		System.out.println("Connected to Cloudant");
		System.out.println("Server Version: " + client.serverVersion());

		// firmwareDB = client.database("firmwareDB", true);
		// firmwareDB = client.database("bar_packages", false);
		firmwareDB = client.database("firmware_repository", false);
		
		// Create update task
		updateTask = new DebianFirmwareUpdate(false);
		
		Thread t = new Thread(this);
		t.start();
	}
	
	@Override
	public void run() {
		while(true) {
			try{
				if (checkAndSetFirmware()){
					downloadFromCloudant();
					// ToDo: update the firmware
					updateFirmware(dmClient.getDeviceData().getDeviceFirmware()); // ToDo: we need to maintain and keep updating the DeviceFirmware object
					updateWatsonIoT();
				} 
			    Thread.sleep(1000 * 60); // ToDo: configure 
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * The following code snippet snoops the Cloudant NoSQL DB for any changes / updates
	 * If the changes / updates are detected, then, it returns the name of the package, 
	 * that has changed / updated since the last known state / snooped time.
	 **/
	
	private boolean checkAndSetFirmware() {
			
		ChangesResult changes = null;
		if(this.firmwareDBSequence == null) {
			changes = firmwareDB.changes()
					.includeDocs(true)
					.getChanges();
		} else {
			changes = firmwareDB.changes()
					.includeDocs(true)
					.since(firmwareDBSequence)
					.getChanges();
		}
		firmwareDBSequence = firmwareDB.info().getUpdateSeq();
		String version = this.currentFirmwareVersion;
		boolean updatedNeeded = false;
		if(changes.getResults().size() != 0) {
			List<ChangesResult.Row> rows = changes.getResults();
			List<JsonObject> jsonList = new ArrayList<JsonObject>(rows.size());
			
			for (Row row : rows) {
				jsonList.add(row.getDoc());
				JsonObject attachment = row.getDoc();
				if(attachment.get("_deleted") != null && attachment.get("_deleted").getAsBoolean() == true) {
					continue;
				}
				
				System.out.println(attachment);
				String retrievedVersion = attachment.get("version").getAsString();
				System.out.println(retrievedVersion);
                if(isVersionGreater(version, retrievedVersion)) {
                	updatedNeeded = true;
                	docId = row.getId();
                	latestFirmwareVersion = retrievedVersion;
                	version = retrievedVersion;
                    
	                JsonObject obj = attachment.get("_attachments").getAsJsonObject();
					Set<Entry<String, JsonElement>> entrySet = obj.entrySet();
									
					Iterator<Entry<String, JsonElement>> itr = entrySet.iterator();
					if(itr.hasNext()) {
						Entry<String, JsonElement> entry = itr.next();
						setLatestFirmware(entry.getKey());
						System.out.println("Setting latest firmware to "+entry.getKey());
					}
				}
			}
		} 
		return updatedNeeded;
	}
	
	/**
	 * This method checks whether the retrieved version is higher than
	 * the deviceVersion.
	 */
	private boolean isVersionGreater(String deviceVersion, String retrievedVersion) {
		String[] retrieved = retrievedVersion.split("\\.");
		String[] device = deviceVersion.split("\\.");
		try {
			for(int i = 0; i < device.length; i++) {
				int retInt = Integer.parseInt(retrieved[i]);
				int deviceInt = Integer.parseInt(device[i]);
				if(retInt > deviceInt) {
					return true;
        		}
        	}
        } catch(Exception e) {}
		return false;
	}
	
	/** 
	* Placeholder for an Empty Foo class
	*/
	
	private static class Foo extends Document {
			
	}
	

	/**
	* Process the Restore Operation
	*/
	
	public void downloadFromCloudant() {
		
		// The following call, picks the Cloudant Document ID from a file on file system 
		// restoreOptions();
		
		// The following call, picks the Cloudant Document from the Cloudant NoSQL DB
		// readCollections();
		
		// Assigning the output of jsonList to fileContent variable
			
		try{
			Foo foo = firmwareDB.find(Foo.class, docId, new Params().attachments());
			String attachmentData = foo.getAttachments().get(getLatestFirmware()).getData();
			String bytes = attachmentData;
			byte[] buffer = Base64.decodeBase64(bytes);
			FileOutputStream outputStream = new FileOutputStream(getLatestFirmware());
			outputStream.write(buffer);
			outputStream.close();       
			System.out.println("Completed Restoration of Cloudant Document ID " +docId + " into the Debian package " +getLatestFirmware());
										
		} catch(FileNotFoundException ex) {
			ex.printStackTrace();
		} catch(IOException ex) {
			System.out.println("Error writing to the Debian package" +getLatestFirmware());
		}
	}

	
	private static String trimedValue(String value) {
		if(value == null || value == "") {
			return "";
		} else {
			return value.trim();
		}
	}

	@Override
	public void downloadFirmware(DeviceFirmware deviceFirmware) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateFirmware(DeviceFirmware deviceFirmware) {
		boolean status = updateTask.updateFirmware(getLatestFirmware());
		
		System.out.println("value of status is " +status);
		
		/**
		 * Trigger the upgrade to the latest firmware
		 */
		if ( status == true ){
			System.out.println("Successfully Upgraded Latest Firmware");
			setCurrentFirmware(getLatestFirmware());
			this.currentFirmwareVersion = this.latestFirmwareVersion;
		} else {
			System.out.println("Upgrade failed. Reverting back to the current version");
			status = updateTask.updateFirmware(getCurrentFirmware());
			if (status == true) {
				System.out.println("Retained Current Firmware as is ");
			} else {
				updateTask.updateFirmware(Handler.FACTORY_FIRMWARE_NAME);
				this.currentFirmwareVersion = Handler.FACTORY_FIRMWARE_VERSION;
				System.out.println("Restored Factory Firmware version after failing to revert back to Current version");
				setCurrentFirmware(Handler.FACTORY_FIRMWARE_NAME);
			} 
		}
	}

	private void updateWatsonIoT() {
		DeviceInfo deviceInfo = dmClient.getDeviceData().getDeviceInfo();
		System.out.println("Updating the Firmware Version to the current version "+currentFirmwareVersion);
		deviceInfo.setFwVersion(currentFirmwareVersion);
		try {
			dmClient.sendManageRequest(0, true, true);
		} catch (MqttException e) {
			System.err.println("Failed to update the new Firmware version to the Watson IoT Platform");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

}	