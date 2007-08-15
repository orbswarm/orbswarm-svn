#!/usr/bin/env python
import signal
import sys
import threading
import signal
from os import *

try:
	import pygame
except:
	print "Sorry: need 'Pygame' to talk to joysticks"
	raise

# globals go here
joy = []



def handleJoyEvent(e):
	if e.type == pygame.JOYAXISMOTION:
		if (e.dict['axis'] == 0):
			print "Stick %d Axis 0 %f" % (e.dict['joy'], e.dict['value'])
		elif (e.dict['axis'] == 1):
			print "Stick %d Axis 1 %f" % (e.dict['joy'], e.dict['value'])
	elif e.type == pygame.JOYBUTTONDOWN:
		print "Stick %d Button %d" % (e.dict['joy'], e.dict['button'])
	else:
		pass

def joystickControl():
	while True:
		e = pygame.event.wait()
		if (e.type == pygame.JOYAXISMOTION or e.type == pygame.JOYBUTTONDOWN):
			handleJoyEvent(e)

def onStop(signum, stackframe):
	# or else script doesn't die
	pygame.display.quit()
	signal.signal(15, signal.SIG_DFL)
	kill(getpid(), 15)

def main():
	# init pygame
	pygame.joystick.init()
	pygame.display.init()
	if not pygame.joystick.get_count():
		popen("oops - can't find joysticks")
		raise ValueError
	print "%d joysticks detected." % pygame.joystick.get_count()
	for i in range(pygame.joystick.get_count()):
		myjoy = pygame.joystick.Joystick(i)
		myjoy.init()
		joy.append(myjoy)
		print "Got stick %d axes: %d name: " % (i, joy[i].get_numaxes()) + joy[i].get_name()

		
	# all initialized; wait for and respond to events	
	joystickControl()

if __name__ == "__main__":
    signal.signal(15, onStop)
    main()
