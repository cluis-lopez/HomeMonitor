To deploy the scripts into your Raspberry create a directory into your home dir like /home/pi/HomeMonitor

Copy these scripts into your just-created directory, then create empty subdirs /homepi/HomeMonitor/Images and /home/pi/HomeMonitor/Alerts.

- **Properties.py**: initializes global variables. The content is self-explained

- **Temp.py**: reads temperature and humidity from the DTH22 sensor.
Uses the Adafruit library you should download and install first 

- **Light.py**: reads the voltage value on the A/D converter. This value is
dependant of the LDR which in turn depends of the ambiance light in your room

- **Image.py**: opens your USB webcam and takes a picture which uploads
to Google Cloud Storage. Saves a local copy under your Images subdirectory

- **Moves.py**: compares two pictures (the current and the previous one) to check
if thre're relevant differences. If so sends an alert to the cloud and uploads
a picture to Cloud Storage showing the differences. This script and Image.py uses
the OpenCV (v2) library included in the standard Raspbian distribution

- **daemon.py**: the main controller script. Runs forever and samples the environment
according with the rules of the Properties.py file. Logs into <HOME_DIR>/HomeMonitor.log 
and should be launched as:
```
	# nohup python daemon.py 2>&1 &
```
