# *****************************************************************************
# Copyright (c) 2016 IBM Corporation and other Contributors.
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Lokesh Haralakatta
# *****************************************************************************

'''
   Example of retrieving device events for 2 devices - piCam-2 and piCam-4.
   Counting device events for each of the device.
   Print details abut the device events count.
'''
import requests

# Define cloudant username
user = "<cloudant-user>"

# Define cloudant password
passwd = "<cloudant-password>"

# Define cloudant host
host = user+".cloudant.com"

# Define cloudant db name
db = "<cloudant-dbname>"

# Define view variable to contain the Map View Name
view = "by-deviceId"

# Frame the URL using above defned variables values
url = 'https://'+host+'/'+db+'/_design/iotp/_view/'+view

# Define args variable to store required parameter values
args={'keys' : ' [ "piCam-2" , "piCam-4" ] ' }

# Invoke HTTP GET request with all required parameters
response = requests.get(url,params=args,auth=(user,passwd))

# Check the response status code, should be 200 to proceed further
if ( response.status_code == 200):
    # Get the response data in JSON format
    jsonData = response.json()
    # Get the device data records which are JSON array of rows with in jsonData
    records = jsonData['rows']
    # Define variables to store different count values
    piCam2Count = 0
    piCam4Count = 0
    otherCount = 0
    totalCount = 0
    # For each record, get deviceType, deviceID and devicedata from the records
    for record in records:
        device = record['value']['deviceId']
        if (device == 'piCam-2'):
            piCam2Count += 1
        elif (device == 'piCam-4'):
            piCam4Count += 1

        else:
            otherCount +=1
        totalCount +=1
    # Print records count for device piCam-2
    print "Device Events for piCam-2: %s" %str(piCam2Count)
    # Print records count for device piCam-4
    print "Device Events for piCam-4: %s" %str(piCam4Count)
    # Print otherCount, should be ZERO
    print "Device Events for other devices: %s" %str(otherCount)
    # Print totalCount, should be sum of piCam2Count and piCam4Count
    print "Total Device Events: %s" %str(totalCount)
else:
    print "HTTP GET Failed with Status Code - %s" %(response.status_code)
