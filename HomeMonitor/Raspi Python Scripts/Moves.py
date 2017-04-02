#!/usr/bin/python

import cv2
import sys
import os

def compare(foto1, foto2):
	img1 = cv2.imread(foto1, cv2.IMREAD_COLOR)
	foto = cv2.imread(foto2, cv2.IMREAD_COLOR)
	img2 = foto

	img1 = cv2.cvtColor(img1, cv2.COLOR_BGR2GRAY)
	img2 = cv2.cvtColor(img2, cv2.COLOR_BGR2GRAY)

	img1 = cv2.GaussianBlur(img1, (21, 21), 0)
	img2 = cv2.GaussianBlur(img2, (21, 21), 0)

	delta = cv2.absdiff(img1, img2)
	thres = cv2.threshold(delta, 100, 255, cv2.THRESH_BINARY)[1]
	thres = cv2.dilate(thres, None, iterations=2)

	(contornos, _) = cv2.findContours(thres.copy(), cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
	
	numdiff = 0
	
	print "Numero de contornos: ", len(contornos)
	
	if len(contornos) == 0:
		return 0, 0
	else:
		for c in contornos:
			print "Contorno:  area:", cv2.contourArea(c)
			if cv2.contourArea(c) < 2000:
				continue
			(x, y, w, h) = cv2.boundingRect(c)
			cv2.rectangle(foto, (x, y), (x+w, y+h), (0, 255, 0), 2)
			numdiff = numdiff + 1
		return numdiff, foto




if __name__ == '__main__':
	if len(sys.argv) < 3:
		print "Usage: Mov_Detection <1st image file> <2nd image file>"
		exit()

	result, image = compare (sys.argv[1], sys.argv[2])
	print "Numero de diferencias: ", result
	
	if result == 0:
		exit()

	try:
		if os.environ["DISPLAY"] != '':
			cv2.imshow('Diferencias',image)
			key = cv2.waitKey(0)
			cv2.destroyAllWindows()
	except:
		print "No hay DISPLAY para mostrar la imagen"
		
		
		