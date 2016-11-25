# *****************************************************************************
# Copyright (c) 2016 IBM Corporation and other Contributors.
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Maeve O'Reilly
# *****************************************************************************

'''
    Example of retrieving a list of the cloudant databases following the conventions for 
	Watson IoT Platform historical data storage.  If there are other databases in your
	services starting with the string "iotp" they will also be found.
	The Cloudant username and password can be accessed in the Credentials tab of your
	Cloudant service
'''

import requests
import re

# Define cloudant username
user = "somethingVeryLongFromCredentials"

# Define cloudant password
passwd = "somethingVeryLongFromCredentials"

# Define cloudant host
host = user+".cloudant.com"

 # Frame the URL using above defned variables values
url = 'https://'+host+'/_all_dbs'

# Define a list to store all generated DB names
dbNames = []

# Invoke HTTP GET request with all required parameters
response = requests.get(url,auth=(user,passwd))

#List databases with iotp in the name but exclude the configuration one
dbNames = response.json()
if (response.status_code == 200):
 for x in dbNames:
  #print x,'\n'
  if re.match("iotp",x) and re.match("^((?!configuration).)*$",x):
   print "Device Database: ",x
