This is a small app to monitor your home environment assuming you have a Raspberry Pi and some extra cheap hardware:

HW needed:

	- Raspberry Pi: model 3 is perfect but any older/simpler model with Internet connection is fine (€35)
	- DHT22 temp + humidity sensor with an SPI interface. Cheaper models (like DHT10) are also an option (€10)
	- MCP3008: A/D converter and some LDR to measure your room's light level (€4)
	- A protoboard to connect all the above (€5)
	- USB webcam (€15/20)
	
	
The application:

	- Raspi Python scripts: download under any directory in your RaspPi.
	You'll need in addition the libraries to drive the DHT22 from [Adafruit](https://github.com/adafruit/Adafruit_Python_DHT/)
	More details in the README file under the scripts directory
		
		
	- The web application: the java classes to deploy in Google App. Engine.
	The app used extensively the Google Datastore and Cloud Storage so if you plan to use
	a different cloud provider APIs will change for sure. More details about Java servlets
	under the webapp directory
