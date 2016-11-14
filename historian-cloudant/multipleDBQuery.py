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
    Retrieve device events from 2 databases using the view by-deviceId and further
    process the device records to find out the count of events from each device.
'''
import requests

# Define cloudant username
user = "<cloudant-user>"

# Define cloudant password
passwd = "<cloudant-password"

# Define cloudant host
host = user+".cloudant.com"

# Define cloudant dbs name
db1 = "<cloudant-dbname1>"
db2 = "<cloudant-dbname2"

# Define view variable to contain the Map View Name
view = "by-deviceId"

# Frame the URLs using above defned variables values
url1 = 'https://'+host+'/'+db1+'/_design/iotp/_view/'+view
url2 = 'https://'+host+'/'+db2+'/_design/iotp/_view/'+view

# Define args variable to store required parameter values
args = { 'keys' : ' [ "piCam-1", "piCam-2", "piCam-3", "piCam-4" ] ' }

# Invoke HTTP GET for URL1
response1 = requests.get(url1,params=args,auth=(user,passwd))

# Invoke HTTP GET for URL2
response2 = requests.get(url2,params=args,auth=(user,passwd))

# Check the response status code for both GET, should be 200 to proceed further
if ( response1.status_code == 200 and response2.status_code == 200):
    # Define Python Dictionary to keep track of events count for devices
    eventsCount = { "piCam-1" : 0 , "piCam-2" : 0 , "piCam-3" : 0 , "piCam-4" : 0 }
    # Iterate through records from Database-1 and get device events count
    jsonData = response1.json()
    records = jsonData['rows']
    for record in records:
        device = record['value']['deviceId']
        eventsCount[device] += 1
    # Print device events count after processing records from Database-1
    print eventsCount
    # Iterate through records from Database-2 and get device events count
    jsonData = response2.json()
    records = jsonData['rows']
    for record in records:
        device = record['value']['deviceId']
        eventsCount[device] += 1
    # Print device events count after processing records from Database-2
    print eventsCount
else:
    print "HTTP GET1 Status Code - %s" %(response1.status_code)
    print "HTTP GET2 Status Code - %s" %(response2.status_code)
