#!/usr/bin/python

import numpy as np
import cv2
import requests
import logging
import time

logging.basicConfig(filename='HomeMonitor.log',level=logging.DEBUG)

def getPict(seq, URL):
    cap = cv2.VideoCapture(0)
    # Increase cam definition
    ret = cap.set (3, 1280)
    ret = cap.set (4, 960)
    
    # Get a Picture
    ret, frame = cap.read()
    
    prefix = '{:02d}'.format(seq)
    
    cv2.imwrite('images/'+prefix+'image.jpg', frame)
    cap.release()
    
    logging.info(time.strftime("%D %H:%M:%S")+"\t Saving " + prefix + "image.jpg in the Raspberry Pi"  )
    
    # After saving a local copy we send the picture to Google Storage
    
    filename = prefix+'image.jpg'

    data = {URL : (filename , open('images/' + filename, 'r'), 'image/jpeg', {'Expires': '0'})}  
    r = requests.post('http://' + URL + '/UploadPict', files = data)
    if r.status_code != 201:
        return "Error uploading picture"
    
    return 'storage.googleapis.com/' + r.content

if __name__ == '__main__':
    k = getPict(0, 'homemonitor-156618.appspot.com')
    print "Picture taken ... check under images in file 00image.jpg"
    print "Uploaded to GAE with key: "+k
    
    