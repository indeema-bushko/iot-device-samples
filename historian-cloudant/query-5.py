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
    Example of retrieving the device events between 2 milliseconds values using
    the view by-milliseconds and further process the device data to get events details
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
view = "by-milliseconds"

# Frame the URL using above defned variables values
url = 'https://'+host+'/'+db+'/_design/iotp/_view/'+view

# Define args variable to store required parameter values
args = { 'startkey' : '1478249932915' , 'endkey' : '1478257809438'  }

# Invoke HTTP GET request with all required parameters
response = requests.get(url,params=args,auth=(user,passwd))

# Check the response status code, should be 200 to proceed further
if ( response.status_code == 200):
    # Get the response data in JSON format
    jsonData = response.json()
    # Get the device data records which are JSON array of rows with in jsonData
    records = jsonData['rows']
    # Iterate through records and get event details for each record
    for record in records:
        dType = record['value']['deviceType']
        dID = record['value']['deviceId']
        dValue = record['value']['data']['d']['value']
        print "Device Type: %s  Device Id: %s  Value: %s" %(dType,dID,str(dValue))
else:
    print "HTTP GET Failed with Status Code - %s" %(response.status_code)
