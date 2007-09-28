#!/usr/bin/env python
import signal
import sys
import threading
import signal
import time
#from os import *

try:
	import pygame
except:
	print "Sorry: need 'Pygame' to talk to joysticks"
	raise

# handle events from pygame

def handleEvent(e):

        # handle joystick motion 
        
        if (e.type == pygame.JOYAXISMOTION):
                str = "axis: %d value: %f" % (
                        e.dict['axis'], 
                        e.dict['value'])
                output(str, e)
                
        # handle hat motion 
                
        elif (e.type == pygame.JOYHATMOTION):
                value = e.dict['value']
                str = "hat: %d x: %s y: %s" % (
                        e.dict['hat'],  
                        value[0], 
                        value[1])
                output(str, e)
                
        # handle button down
                
        elif (e.type == pygame.JOYBUTTONDOWN):
                str = "button: %d value: 1" % e.dict['button']
                output(str, e)
                
        # handle button up
                
        elif (e.type == pygame.JOYBUTTONUP):
                str = "button: %d value: 0" % e.dict['button']
                output(str, e)

        # unknow event type
        
        else:
                str = "event: %s" % pygame.event.event_name(e.type)
                output(str, e)

# format line for output                

def output(line, e):
        
        if e.dict.has_key('joy'):
                print "stick: %d %s" % (e.dict['joy'], line)
        else:
                print "%s" % line
        sys.stdout.flush()

# handle program stop

def onStop(signum, stackframe):
	# or else script doesn't die
	pygame.display.quit()
	signal.signal(15, signal.SIG_DFL)
	kill(getpid(), 15)

# where the action starts

def main():

	# init pygame

	pygame.display.init()
	pygame.joystick.init()

        # report joystick facts

	print "stickcount: %d" % pygame.joystick.get_count()
	for i in range(pygame.joystick.get_count()):
		myjoy = pygame.joystick.Joystick(i)
		myjoy.init()
		print ("stickinfo: %d buttons: %d axes: %d hats: %d name: %s" %
                       (i, 
                        myjoy.get_numbuttons(), 
                        myjoy.get_numaxes(), 
                        myjoy.get_numhats(), 
                        myjoy.get_name()))
        sys.stdout.flush();

        # if we have joysticks, loop forever, handling events as they come

	if pygame.joystick.get_count() > 0:
                while True:
                        handleEvent(pygame.event.wait())

# not sure what mojo is happening here

if __name__ == "__main__":
    signal.signal(15, onStop)
    main()
