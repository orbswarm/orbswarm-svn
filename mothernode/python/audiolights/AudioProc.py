""" AudioProc: Class to import and analyze audio. Henerates WX events"""


#import psyco 
#psyco.full()

# dependencies: need numPy, pyaudio, tkInter, 
import thread
import time

import  wx
import  wx.lib.newevent
import  thread

import pyaudio
import sys
from numpy import *
from numpy.fft import *
from struct import *


chunk = 1024
FORMAT = pyaudio.paInt16
CHANNELS = 1
RATE = 22500
RECORD_SECONDS = 5

#----------------------------------------------------------------------

# This creates a new Event class and a EVT binder function


class AudioProc():
    def __init__(self, win, nbands=8):
        self.win = win

        p = pyaudio.PyAudio()
        self.p=p
        self.stream = p.open(format = FORMAT,
                             channels = CHANNELS, 
                             rate = RATE, 
                             input = True,
                             output = True)
        
#        print "AudioProc init: recording"
        (self.UpdateAudioEvent, self.EVT_AUDIO) = wx.lib.newevent.NewEvent()
        csum = []
        spectlen = RATE / chunk * RECORD_SECONDS
        self.makebands(chunk/2)
        self.bmax=0
        self.gain = 1.0
        self.skip =8            # skip this many windows

    def setgain(self,intgain):
        self.gain = 2*intgain/100.0
#        print "AudioProc gain is " + str(self.gain)
#        sys.stdout.flush()

    def makebands(self,max):
        "make a set of power-of-two bands. Max must be power of 2"
        self.bands = []
        self.scale = []
        while max > 2:
#            print "start : %4d stop: %4d" % (max/2, max)
            self.bands.append(range(max/2, max))
            self.scale.append(max)
            max = max/2
        self.bands[-1] = range(0,4)
        # reverse the bands from low to high
        self.bands.reverse()
        self.scale.reverse()

    def Start(self):
        self.keepGoing = self.running = True
        thread.start_new_thread(self.Run, ())

    def Stop(self):
#        print "got stop command"
        sys.stdout.flush()
        self.keepGoing = False

    def Restart(self):
#        print "got restart command"
        sys.stdout.flush()
        self.keepGoing = True

    def Run(self):
        i = 0
        while self.keepGoing:
            i += 1
            data =self.stream.read(chunk)
            if (i>2):
#            if (i>self.skip):
                i = 0
            #print unpack('B',data[0])
                buff = array(unpack_from('1024h',data))
                #e = buff.std()
                #print "Energy: %f" % e
                #sys.stdout.flush()
             ## use stdev to calculate energy in this buffer (root sum of squared mag)      
            #csum.append(buff.std())
                fourier = fft(buff)
            ## calculate log mag of fft
                logmag = hypot(fourier.real[0:chunk/2],fourier.imag[0:chunk/2])
                if False:
                    self.lmfile=open("logmag.txt","w")
                    logmag.tofile(self.lmfile, sep=" ", format="%s")
                    self.lmfile.close()
                bdata = []
                i = 0
                for b in self.bands:
                    # sum energy in this band
                    bdata.append(logmag[b].mean()*self.scale[i])
                    i += 1

                    # normalize so max energy is 1.0
                localmax = []
                localmax.append(max(bdata))
                localmax.append(self.bmax)
                self.bmax = max(localmax)
                for i in range(len(bdata)):
                    bdata[i] = (bdata[i])*self.gain/self.bmax

                evt = self.UpdateAudioEvent(bands=bdata, value = int(localmax[0]*self.gain/50000))
                #print localmax[0]
                if True:
                    time.sleep(0.01)
                wx.PostEvent(self.win, evt)
                if True:
                    time.sleep(0.01)
                #self.bwfile=open("bands.txt","w")
                #self.bwfile.write(str(bdata))
                #self.bwfile.close()


                #bands = logmag[0:chunk/4]
                #nbands = 16
                #bwidth = chunk/(4*nbands)
                #bsum = mean(bands.reshape(nbands,bwidth),axis=1)
            #line.set_ydata(logmag)
                
            ## convert to 2d array and append to running show. 
            #logmag2 = array(logmag.tolist(),ndmin=2)
        self.running = False

    def close(self):
        print "closing audio device"
        sys.stdout.flush()
        self.Stop()
        self.stream.stop_stream()
        self.stream.close()
        self.p.terminate()

