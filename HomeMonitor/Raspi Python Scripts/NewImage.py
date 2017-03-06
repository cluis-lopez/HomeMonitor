#!/usr/bin/python


import requests

r = requests.post('http://1-dot-homemonitor-156618.appspot.com/NewUpload',
                  files={'images/00image.jpg': open('images/00image.jpg', 'rb')});
print r