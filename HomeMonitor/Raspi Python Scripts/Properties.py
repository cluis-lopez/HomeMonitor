#!/usr/bin/python

# Sample properties file. Edit and modify with your own values

# URL is the Google App Engine adress where your app is hosted
URL=xxxx.appspot.com

# The number of env. samples taken and stored in the Raspi before sending to Google
NUMSAMPLES=10

# Delay in seconds between samples
DELAY= 300 # 5 minutes

# Number of pictures stored both locally (in the Raspi) and in Google Storage
NUMPICS=15

# The minimum light in the room needed to get a clear picture (0-1023)
THRESHOLD_LIGHT=150

# The maximum stored moving detection pictures (both local and remote)
MAX_ALERT_PICTS=25