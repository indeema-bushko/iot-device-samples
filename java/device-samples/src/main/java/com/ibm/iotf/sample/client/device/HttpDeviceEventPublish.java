/**
 *****************************************************************************
 * Copyright (c) 2016 IBM Corporation and other Contributors.

 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Amit M Mangalvedkar - Initial Contribution
 * Sathiskumar Palaniappan - Initial Contribution 
 *****************************************************************************
 */

package com.ibm.iotf.sample.client.device;

import java.io.IOException;
import java.util.Properties;
import com.google.gson.JsonObject;
import com.ibm.iotf.client.device.DeviceClient;
import com.ibm.iotf.sample.client.SystemObject;

/**
 * 
 * This sample shows how a device can publish events 
 * using HTTP(s) to IBM Watson IoT Platform
 *
 */
public class HttpDeviceEventPublish {
	
	private final static String PROPERTIES_FILE_NAME = "/device.properties";

	public static void main(String[] args) throws Exception {
		
		/**
		  * Load device properties
		  */
		Properties props = new Properties();
		try {
			props.load(HttpDeviceEventPublish.class.getResourceAsStream(PROPERTIES_FILE_NAME));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}		
		
		DeviceClient myClient = null;
		try {
			//Instantiate the class by passing the properties file
			myClient = new DeviceClient(props);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		SystemObject obj = new SystemObject();
		
		/**
		 * Publishes the process load event for every 1 second
		 */
		while(true) {
			int code = 0;
			try {
				//Generate a JSON object of the event to be published
				JsonObject event = new JsonObject();
				event.addProperty("name", SystemObject.getName());
				event.addProperty("cpu",  obj.getProcessCpuLoad());
				event.addProperty("mem",  obj.getMemoryUsed());
				
				code = myClient.publishEventOverHTTP("blink", event);
				Thread.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if(code == 200) {
				System.out.println("Published the event successfully !");
			} else {
				System.out.println("Failed to publish the event......");
				System.exit(-1);
			}
		}
	}
	
}
