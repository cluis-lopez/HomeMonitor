This is a small app to monitor your home environment assuming you have a Raspberry Pi and some extra cheap hardware:

HW needed:

	- Raspberry Pi: model 3 is perfect but any older/simpler model with Internet connection is fine (€35)
	- DHT22 temp + humidity sensor with an SPI interface. Cheaper models (like DHT10) are also an option (€10)
	- MCP3008: A/D converter and some LDR to measure your room's light level (€4)
	- A protoboard to connect all the above (€5)
	- USB webcam (€15/20)
	
	
The application:

	- Raspi Python scripts: download under any directory in your RaspPi. You'll need in addition the libraries to drive thee DHT22 from Adafruit.
	
		- Temp.py: to read temp & humidity from the DTH22 sensor
		- Light.py: to read light level in your room using a simple photoresistor and one cheap A/D chip like the MCP3008
		- Image.py: to take pictures from the webcam, analyze movement and (to be done) identify faces and send video
		- daemon.py: the controller script that calls the above, formats data and sends it to the cloud
		
		
	- The web application: to deploy in your favorite cloud provider. Google App Engine is my cloud of choice here:
	
		- HomeMonitorServlet.java: reads the JSON sent by the Python daemon in the Rasp and stores it in Google Datastore. Makes some cleaning of the datastore if needed