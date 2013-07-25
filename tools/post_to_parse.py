#!/usr/bin/python

import csv
from datetime import datetime, timedelta
import json
import re
import requests
import sys

def create_INT(data):
    return int(data)

def create_FLOAT(data):
    return float(data)

def _create_DATEOBJ(data, tmz_delta, format):
    date = datetime.strptime(data, format)
    if tmz_delta:
        date += tmz_delta
    return date

def create_DATE(data, tmz_delta):
    date = _create_DATEOBJ(data, tmz_delta, "%m/%d/%y")
    return {"__type": "Date", "iso": date.isoformat()}

def create_TIME(data, tmz_delta):
    date = _create_DATEOBJ(data, None, "%H:%M:%S")
    time_struct = date.timetuple()
    hour = time_struct[3] * 60 * 60
    min =  time_struct[4] * 60
    sec =  time_struct[5]
    return hour + min + sec

def create_TIMESTAMP(data, tmz_delta):
    date = _create_DATEOBJ(data, tmz_delta, "%m/%d/%y %H:%M:%S")
    return {"__type": "Date", "iso": date.isoformat()}

def create_STRING(data):
    return str(data)

not_allowed_re = re.compile("[^a-zA-Z0-9]")
def massage_column_header(header):
    return re.sub(not_allowed_re, "_", header)

def hilite(string, status, bold):
    attr = []
    if status >= 200 and status < 400:
        # green
        attr.append('32')
    elif status >= 400 and status < 500:
        # red
        attr.append('31')
    if bold:
        attr.append('1')
    return '\x1b[%sm%s\x1b[0m' % (';'.join(attr), string)

API_KEY_HEADER = "X-Parse-REST-API-Key"
APP_ID_HEADER = "X-Parse-Application-Id"

API_KEY = ""
APP_ID = ""

URL = "https://api.parse.com/1/classes/"
CLASS_NAME = "MedtronicMinimedParadigmRevel755PumpData"

HEADERS = {API_KEY_HEADER: API_KEY, APP_ID_HEADER: APP_ID, "Content-Type": "application/json"}

# Data types
INT = 0
FLOAT = 1
DATE = 2
TIME = 3
TIMESTAMP = 4
STRING = 5

# Export format v1.0.1
column_types = [INT, DATE, TIME, TIMESTAMP, TIMESTAMP, INT, STRING, FLOAT, STRING, TIME, STRING, FLOAT, FLOAT, TIME, STRING, FLOAT, STRING, STRING, FLOAT, INT, INT, FLOAT, INT, FLOAT, INT, FLOAT, FLOAT, FLOAT, STRING, INT, INT, FLOAT, FLOAT, STRING, STRING, FLOAT, FLOAT, FLOAT, STRING]

skip_info_rows_count = 11

# Don't have any way to get the GMT offset of the device
# Need to reverse time zone because Parse expects GMT
pst_delta = timedelta(hours=7)

csv_file = sys.argv[1]

if not csv_file:
    print "Takes a path to the csv to upload", "No data uploaded"
    exit(-1)

isTTY = sys.stdout.isatty()

csv_reader = csv.reader(open(csv_file, 'rb'))

while skip_info_rows_count:
    csv_reader.next()
    skip_info_rows_count -= 1

column_headers = csv_reader.next()
if len(column_headers) != len(column_types):
    print "Columnt data types length does not match headers length"
    print "Found %d columns, expected %d" % (len(column_headers), len(column_types))
    print "No data uploaded"
    exit(-1)

for row in csv_reader:
    payload = {}

    for type, data, column_header in zip(column_types, row, column_headers):
        if data == "":
            continue
        value = {}
        if type == INT:
            value = create_INT(data)
        elif type == FLOAT:
            value = create_FLOAT(data)
        elif type == DATE:
            value = create_DATE(data, pst_delta)
        elif type == TIME:
            value = create_TIME(data, pst_delta)
        elif type == TIMESTAMP:
            value = create_TIMESTAMP(data, pst_delta)
        elif type == STRING:
            value = create_STRING(data)
        else:
            print "Unknown column type '", type, "'"
            exit(-1)

        payload[massage_column_header(column_header)] = value
    
    if not payload:
        print "Empty payload, skipping"
        continue

    print "Uploading ", payload

    response = requests.post(URL + CLASS_NAME, data=json.dumps(payload), headers=HEADERS)
    
    if isTTY and response.status_code >= 400 and response.status_code < 500:
        print hilite("Response code " + str(response.status_code) + " => " + response.text, response.status_code, True)
    else:
        print hilite("Response code " + str(response.status_code) + " => " + response.text, response.status_code, False)

