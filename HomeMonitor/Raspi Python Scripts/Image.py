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
    
    # After saving a local copy we send the picture lo Google Blobstore
    
    # Get the URL to upload the last image file to Google blobstore    
    r = requests.get(URL+'/GetURL')
    if r.status_code != 200:
        return "Error getting the URL"
    bloburl = r.content
    # print bloburl
    
    # Now sends the last image stored also in the Raspi to the Blobstore
    data = {'Foto': ('Foto', open('images/'+prefix+'image.jpg', 'r'), 'image/jpeg', {'Expires': '0'})}
    response = requests.post(bloburl, files=data)
    blobkey = response.content
    # print  blobkey
    
    return blobkey

if __name__ == '__main__':
    k = getPict(0, 'http://homemonitor-156618.appspot.com')
    print "Picture taken ... check under images in file 00image.jpg"
    print "Uploaded to GAE with key: "+k
    
    