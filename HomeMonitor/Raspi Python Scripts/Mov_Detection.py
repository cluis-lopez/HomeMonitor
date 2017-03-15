#!/usr/bin/python

import cv2
import sys

def compare(foto1, foto2):
	img1 = cv2.imread(foto1)
	foto = cv2.imread(foto2)
	img2 = cv2.copy(foto)

	img1 = cv2.cvtColor(img1, cv2.COLOR_BGR2GRAY)
	img2 = cv2.cvtColor(img2, cv2.COLOR_BGR2GRAY)

	img1 = cv2.GaussianBlur(img1, (21, 21), 0)
	img2 = cv2.GaussianBlur(img2, (21, 21), 0)

	delta = cv2.absdiff(img1, img2)
	thres = cv2.threshold(delta, 25, 255, cv2.THRESH_BINARY)[1]
	thres = cv2.dilate(thres, None, iterations=2)

	(contornos, _) = cv2.findContours(thres.copy(), cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
	
	print "Contornos detectados:" , contornos
	
	for c in contornos:
		if cv2.contourArea(c) < 2000:
			continue
		(x, y, w, h) = cv2.boundingRect(c)
		cv2.rectangle(foto, (x, y), (x+w, y+h), (0, 255, 0), 2)
	

	if contornos == None:
		return False, 0
	else:
		return True, foto



if __name__ == '__main__':
	if len(sys.argv) < 3:
		print "Usage Mov_Detection <1st image file> <2nd image file>"
		break
	f1 = open(sys.argv[1], r);
	f2 = open(sys.argv[2], r);
	result, image = compare (f1, f2)
	
	
		