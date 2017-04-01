#!/usr/bin/python

import time
import logging
import requests

URL='homemonitor-156618.appspot.com/Alerts'

def alert_t (t):
    data = {'Level':1, 'Message':'Temperature above limits', 'Temp': t}
    res = requests.post (URL, files=data)
    
def alert_h (h):
    data = {'Level':1, 'Message':'Humidity too high or low', 'Hum': h}
    res = requests.post (URL, files=data)
        
def alert_mov(num, pict):
    data = {'Level':2, 'Message':'Movement detected in the room', 'Num': num}
    res = requests.post (URL, files=data)   
