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
    Example code of deriving Cloudant NoSQL Database names for Watson IoT Platform
    Historian Data Storage Service. We assume that, we have configured the cloudant
    NoSQL DB as Historian Data Storage with bucket size - monthly starting from
    2016-01 and we need to dynamically generate all DB Names till 2016-12
'''

# Define OrgId to store Watson IoT Instance Organization ID
orgId = "abcde"

# Define dbChoice to store user provided db name
dbChoice = "default"

# Define bktPattern to store the selected bucket size pattern
bktPattern = "2016-"

# Define dbStart to store the starting db name
dbStart = 01

# Define dbEnd tp store the ending db name range
dbEnd = 12

# Define a list to store all generated DB names
dbNames = []

while (dbStart < dbEnd):
    if (dbStart < 10):
        dbName = 'iotp_' + orgId + '_' + dbChoice + '_' + bktPattern + '0' + str(dbStart)
    else:
        dbName = 'iotp_' + orgId + '_' + dbChoice + '_' + bktPattern + str(dbStart)
    dbNames.append(dbName)
    dbStart = dbStart + 1

# Print Derived DB names
print dbNames
