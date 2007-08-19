#!/usr/bin/env python
import signal
import sys
import threading
import signal
#from os import *

try:
	import pygame
except:
	print "Sorry: need 'Pygame' to talk to joysticks"
	raise

# globals go here
joy = []
dataFiles = []
dataFileNameBase = "/tmp/joydata%d.txt"

def handleJoyEvent(e):
	if e.type == pygame.JOYAXISMOTION:
                if (e.dict['axis'] == 0):
                        axis = "x1"
                
                if (e.dict['axis'] == 1):
                        axis = "y1"
                        
                if (e.dict['axis'] == 2):
                        axis = "x2"

                if (e.dict['axis'] == 3):
                        axis = "y2"

                str = "axis: %s value: %f" % (axis, e.dict['value'])
                output(str, e.dict['joy'])

	elif e.type == pygame.JOYBUTTONDOWN:
                str = "button: %d" % (e.dict['button'])
                output(str, e.dict['joy'])
	else:
		pass

def output(line, stick):
        print "stick: %d %s" % (stick, line)
        dataFiles[stick].write(line)
        dataFiles[stick].write("\n")
        dataFiles[stick].flush()

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
                dataFiles.append(open(dataFileNameBase % (i), "w"))
		print "Got stick %d axes: %d name: " % (i, joy[i].get_numaxes()) + joy[i].get_name()

		
	# all initialized; wait for and respond to events	
	joystickControl()

if __name__ == "__main__":
    signal.signal(15, onStop)
    main()
