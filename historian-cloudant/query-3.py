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
    Example of retrieving most recent device event using view by-date and
    further process to get the details for device type, event type and device
    for the most recent device event.
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
view = "by-date"

# Frame the URL using above defned variables values
url = 'https://'+host+'/'+db+'/_design/iotp/_view/'+view

# Define args variable to store required parameter values
args = { 'descending' : 'true' , 'limit' : '1'  }

# Invoke HTTP GET request with all required parameters
response = requests.get(url,params=args,auth=(user,passwd))

# Check the response status code, should be 200 to proceed further
if ( response.status_code == 200):
    # Get the response data in JSON format
    jsonData = response.json()
    # Records must have just 1 record at index 0
    # Get the device data records which are JSON array of rows with in jsonData
    records = jsonData['rows']
    # Retrieve and print device type
    print "Device Type: %s" %(records[0]['value']['deviceType'])
    # Retrieve and print device Id
    print "Device Id: %s" %(records[0]['value']['deviceId'])
    # Retrieve and print event type
    print "Event Type: %s" %(records[0]['value']['eventType'])
    # Retrieve and print timestamp
    print "Timestamp: %s" %(records[0]['value']['timestamp'])
else:
    print "HTTP GET Failed with Status Code - %s" %(response.status_code)
