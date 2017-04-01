#!/usr/bin/python

import json
import time
import urllib
import httplib
import logging
import Temp
import Light
import Image
import Moves
import Properties


# delay is the sample period. 5 minutes is 300 seconds
DELAY = Properties.DELAY # Each minute one temp + hum + ligh sample
# numsamples is the number of samples we pack before send to the servlet
# Ex. if delay = 300 (5 mins) and numsamples = 3, we're sending data each 15 minutes
NUMSAMPLES = Properties.NUMSAMPLES # Each 15 samples (15 minutes) we send data to GAE plus one picture is light is enough

# The number of pictures we store locally before overwriting them
NUMPICS = Properties.NUMPICS

# The first picture start with the prefix "00"
seq = 0

# URL is the address of the servlet at GAE to process and store the JSON

URL = Properties.URL

logging.basicConfig(filename='HomeMonitor.log',level=logging.DEBUG)

def Sample():
    """ No args. Get a sample of Temp+Humidity & Light. Discard it. Wait half a second
    """
    Temp.Temp()
    Light.Light()
    time.sleep(0.5)
    
    # Get 5 samples delayed 0,1 sec each to get the average
    t, h, l  = 0, 0, 0
    for i in range(5):
        x, y = Temp.Temp()
        z = Light.Light()
        t = t + x
        h = h + y
        l = l + z
        time.sleep(0.1)
    
    t = t / 5
    h = h / 5
    l = l / 5; l = abs(l-1023)

    # print "Temperature: ", t, "   Humidity:", h,"%", "Light:", l
    return [t,h,l]

# Send Alerts
def SendAlert (lev, typ, message, data):
    params = urllib.urlencode(json.dumps({"Level":lev, "Type":typ, "Message": message, "Data":data}))
    headers = {"Content-type": "application/x-www-form-urlencoded", "Accept": "text/plain"}
    conn = httplib.HTTPConnection(URL)
    conn.request("POST", "/Alerts", params, headers)
    response = conn.getresponse()
    
# Run forever. Takes a sample of temp, humidity and light each "delay" mins, then packs each sample in an array and 
# serializes into json. Send json to the servlet.

while True:
    n = NUMSAMPLES
    pack = {}
    
    for i in range(n):
        data = Sample()
        print time.strftime("%H %M %S"), " Sample: ", i, "Datos:", data
        pack[str(i)]= {"Temp":data[0], "Hum":data[1], "Light":data[2], "Timestamp":time.time()}
        logging.info(time.strftime("%D %H:%M:%S")+"\t"+str(pack[str(i)]))
        time.sleep(DELAY)
        if data[0] > 32.0:
            SendAlert(1, 0, "Temperature too high", data[0])
            logging.info(time.strftime("%D %H:%M:%S")+"\t Alerta Temperatura"+data[0])
        if data[1]< 10 | data[1]>80:
            SendAlert(1, 1, "Humidity too low", data[0])
            logging.info(time.strftime("%D %H:%M:%S")+"\t Alerta Humedad"+data[1])
        

        
    # If light is enough, let's take a picture
    if data[2]>150:
        # Pics are stored as "XXimage.jpg" where XX is in the range 00 up to NUMPICS
        if seq >= NUMPICS:
            seq = 0
            
        blobid = Image.getPict(seq, 'http://'+URL)
        pack[str(i)]["Pict"] = blobid
        seq = seq + 1
        logging.info(time.strftime("%D %H:%M:%S")+" Adding picture with id:"+blobid)
        
        # Now check for differences in the current picture from the previous one
        prefix = '{:02d}'.format(seq)
        file1 = 'images/' + prefix + 'image.jpg'
        
        if seq == 0:
            prefix = '{:02d}'.format(NUMPICS)
        else:
            prefix = '{:02d}'.format(seq-1)
            
        file2 = 'images/' + prefix + 'image.jpg'
        
        ret , pict = Moves.compare(file1, file2)
        
        if ret != 0:
            SendAlert(2, 2, "Movement Detected", ret)
  
    
    # Make the HTTP POST to send the json 
    params = urllib.urlencode({'NumSamples': str(NUMSAMPLES), 'JSON':json.dumps(pack)})
    headers = {"Content-type": "application/x-www-form-urlencoded", "Accept": "text/plain"}
    conn = httplib.HTTPConnection(URL)
    conn.request("POST", "/HomeMonitorServlet", params, headers)
    response = conn.getresponse()
    # print response.status, response.reason
    # print response.read()
    logging.info(time.strftime("%D %H:%M:%S")+" Sending data to main servlet")
    logging.info(time.strftime("%D %H:%M:%S")+"\t"+str(response.status)+"\t"+str(response.reason))
    logging.info(time.strftime("%D %H:%M:%S")+"\t"+str(response.read))
