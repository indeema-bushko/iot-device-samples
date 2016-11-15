
#Sample cURL Statements to query device data from Cloudant NoSQL database:

###Query-1: Example of retrieving a list of the first five documents from a database, applying the view by-deviceId:

curl 'https://**cloudant-username:cloudant-password**@cloudant-host/cloudant-database/_design/iotp/_view/by-deviceId?limit=5'

###Query-2: Example of retrieving third to seventh record for the device "piCam-4":

curl 'https://**cloudant-username:cloudant-password**@cloudant-host/cloudant-database/_design/iotp/_view/by-deviceId?key="piCam-4"&skip=2&limit=5'

###Query-3: Example of retrieving most recent device event using view by-date:

curl 'https://**cloudant-username:cloudant-password**@cloudant-host/cloudant-database/_design/iotp/_view/by-date?descending=true&limit=1'

###Query-4: Example of retrieving the device events for 2 device types – elevator and egType using view by-deviceType:

curl 'https://**cloudant-username:cloudant-password**@cloudant-host/cloudant-database/_design/iotp/_view/by-deviceType?keys=["elevator","egType"]' --globoff

###Query-5: Example of retrieving the device events between 2 milliseconds values using view by-milliseconds:

curl 'https://**cloudant-username:cloudant-password**@cloudant-host/cloudant-database/_design/iotp/_view/by-milliseconds?startkey=1478249932915&endkey=1478257809438'
