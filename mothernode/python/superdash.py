#----------------------------------------------------------------------
# superdashboard.py:
#               SWARM Python dashboard to connect with Orb SPU
#               Send commands and receive/display telemetry and status info
#               super version supports multiple joysticks using pyGame
#----------------------------------------------------------------------

try:
	import pygame
except:
	print "Sorry: need 'Pygame' to talk to joysticks"
	raise

import wx
import serial
import sys

import csv

# User settings. OK to edit these if you know what you are doing 
comport = "COM4"
baudrate = 38400
logFileName = 'dash-log.txt'

# User settings: max/min steering and drive values. OK to edit!
driveMax = 30
driveMin = -30

#use 40 for PID speed control 
#driveMax = 40
#driveMin = -40

steerMax = 100
steerMin = -100



# don't edit these
driveRange = driveMax - driveMin
steerRange = steerMax - steerMin


# struct to hang events from 
class eventStruct:
    pass

class Dashboard(wx.Frame):
    
    def __init__(self, parent, title):
        wx.Frame.__init__(self, parent, -1, title,
                          pos=(150, 150), size=(500, 600))

        # Create the menubar
        menuBar = wx.MenuBar()

        # and a menu 
        menu = wx.Menu()

        # array of events ordered by time tick
        self.eventList = []

        # add an item to the menu, using \tKeyName automatically
        # creates an accelerator, the third param is some help text
        # that will show up in the statusbar
        menu.Append(wx.ID_EXIT, "E&xit\tAlt-X", "Exit dashboard")

        # bind the menu event to an event handler
        self.Bind(wx.EVT_MENU, self.OnTimeToClose, id=wx.ID_EXIT)

        # and put the menu on the menubar
        menuBar.Append(menu, "&File")
        self.SetMenuBar(menuBar)
        
        self.CreateStatusBar()
        self.InitSerial(comport,baudrate)
        #self.ser.write('$v16*')  # Kp -- PID prop. gain
        #self.ser.write('$f20*')  # Ki -- PID integral
        #self.ser.write('$e10*')  # Kd -- PID derivative
        #self.ser.write('$b60*')  # Min drive
        #self.ser.write('$c200*') # Max drive
        #self.ser.write('$a255*') # Max accel
        #self.ser.write('$d10*')  # dead band


        #self.ser.write('$v5*')  # Kp -- PID prop. gain
        #self.ser.write('$f0*')  # Ki -- PID integral
        #self.ser.write('$e00*')  # Kd -- PID derivative
        #self.ser.write('$b60*')  # Min drive
        #self.ser.write('$c200*') # Max drive
        #self.ser.write('$a255*') # Max accel
        #self.ser.write('$d10*')  # dead band

        # NEW PID CONSTANTS
#        self.ser.write('$v15*')  # Kp -- PID prop. gain
#        self.ser.write('$f1*')  # Ki -- PID integral
#        self.ser.write('$e0*')  # Kd -- PID derivative
#        self.ser.write('$b30*')  # Min drive
#        self.ser.write('$c200*') # Max drive
#        self.ser.write('$a255*') # Max accel
#        self.ser.write('$d10*')  # dead band

        self.InitJoystick()
        self.InitTimer(100)
        self.Bind(wx.EVT_TIMER, self.OnTimerEvt)
        self.tick=0
        self.outQ = []  #output queue for serial commands
        
        # create the Panel to put the controls on.
        panel = wx.Panel(self)

        # add drive control
        drivelabel = wx.StaticText(panel, -1, "Drive")
        drivelabel.SetFont(wx.Font(12, wx.SWISS, wx.NORMAL, wx.BOLD))
        drivelabel.SetSize(drivelabel.GetBestSize())
        # slider args: parent, ID, value, min, max, pos, size, flags
        self.drive = wx.Slider(panel, -1, 0, driveMin, driveMax,
                               wx.DefaultPosition,(-1,250), 
                               wx.SL_VERTICAL |
                               wx.SL_AUTOTICKS |
                               wx.SL_LABELS |
                               wx.SL_LEFT  |
                               wx.SL_INVERSE)
        self.drive.SetTickFreq(8, 1)

        #steering control
        steerlabel = wx.StaticText(panel, -1, "Steering")
        steerlabel.SetFont(wx.Font(12, wx.SWISS, wx.NORMAL, wx.BOLD))
        steerlabel.SetSize(steerlabel.GetBestSize())
        # slider args: parent, ID, value, min, max, pos, size, flags
        self.steer = wx.Slider(panel, -1, 0, steerMin, steerMax,
                               wx.DefaultPosition, (250,-1), 
                               wx.SL_HORIZONTAL |
                               wx.SL_AUTOTICKS |
                               wx.SL_LABELS)
        self.steer.SetTickFreq(8, 1)


        # status box for returned serial data
        self.statusbox = wx.TextCtrl(panel, -1,"serial output in this box",
                                     size=(300, 100),
                                     style=wx.TE_MULTILINE|wx.TE_PROCESS_ENTER)

        # text box to enter custom commands 
        self.commandbox = wx.TextCtrl(panel, -1,"send this",
                                      size=(200, 20),
                                      style=wx.TE_PROCESS_ENTER)
        self.resultbox = wx.TextCtrl(panel, -1,"",
                                      size=(200, 20),
                                      style=wx.TE_PROCESS_ENTER)
        
        # send emergency stop command
        stopBtn = wx.Button(panel, -1, "EMERGENCY STOP")
        stopBtn.SetForegroundColour((255,0,0))

        # send any command typed into commandbox
        sendBtn = wx.Button(panel, -1, "Send custom command")

        # start/stop logging command
        self.logBtn = wx.Button(panel, -1, "Start record")
        self.logBtn.SetBackgroundColour((0,255,0))
        self.currentlyLogging = 0 # set if we are writing to the logfile
        self.ticks = 0
        self.fileLen = 0 #  number of data points written to logfile

        # start/stop logging command
        self.playBtn = wx.Button(panel, -1, "Start playback")
        self.playBtn.SetBackgroundColour((0,255,0))
        self.currentlyPlaying = 0 # set if we are writing to the logfile
        self.pbtick = 0
        self.maxplayticks = 0;


        # bind the control events to handlers
        self.Bind(wx.EVT_SCROLL, self.OnSlideEvt,self.drive)
        self.Bind(wx.EVT_SCROLL, self.OnSlideEvt,self.steer)
        self.Bind(wx.EVT_BUTTON, self.OnStopBtn, stopBtn)
        self.Bind(wx.EVT_BUTTON, self.OnSendBtn, sendBtn)
        self.Bind(wx.EVT_BUTTON, self.OnLogBtn, self.logBtn)
        self.Bind(wx.EVT_BUTTON, self.OnPlayBtn, self.playBtn)

        # Bind commandbox "enter" key to execute command
        self.Bind(wx.EVT_TEXT_ENTER , self.OnSendBtn, self.commandbox)

        # Use a sizer to lay out the controls, in a grid
        bigGrid = wx.FlexGridSizer(5, 2, 10, 10)  # rows, cols, hgap, vgap
        bigGrid.AddMany([
            # top row
            (drivelabel,     0,  wx.ALIGN_CENTER),
            (steerlabel,     0,  0),
            # second row
            (self.drive,     0, wx.ALIGN_RIGHT),
            (self.steer,     0, wx.ALIGN_LEFT ),
            # third row
            (stopBtn,        0, wx.ALIGN_CENTER),
            (self.statusbox, 0, wx.ALIGN_CENTER),
            #fourth row
            (sendBtn,        0, wx.ALIGN_CENTER),
            (self.commandbox,0, wx.ALIGN_LEFT),
            #fifth row
            (self.logBtn,        0, wx.ALIGN_CENTER),
            (self.playBtn,       0, wx.ALIGN_CENTER),
            ])
        panel.SetSizer(bigGrid)
        panel.Layout()


######################################### event handlers
    def OnTimeToClose(self, evt):
        """Event handler for any close events."""
        self.ser.Close()
        self.Close()

    def OnStopBtn(self, evt):
        """Event handler for the Stop button."""
        self.ser.write("$!*")
        self.drive.SetValue(0)

    def OnLogBtn(self, evt):
        """Event handler for the Log button."""
        if self.currentlyPlaying:
            return
        if  self.currentlyLogging == 0:
            # start logging
            self.startRecord()
        else:
            self.stopRecord()

    def OnPlayBtn(self, evt):
        """toggle stop/start playback."""
        if self.currentlyLogging:
            return
        if  self.currentlyPlaying == 0:
            self.startPlayback()
        else:
            self.stopPlayback()


    def OnSendBtn(self, evt):
        """Event handler for the Send button.
            Queue the send text for serial output"""
        cmdStr = self.commandbox.GetValue()
        # automatically format if necessary
        if cmdStr[0] != '$':
            cmdStr = '$' + cmdStr + '*'
        self.outQ.append(cmdStr)
        # echo command so no retyping
        self.commandbox.SetValue(cmdStr)

    def OnSlideEvt(self,evt):
        foo=1
       # self.SendSerial()

    # this is called regularly by the timer.    
    def OnTimerEvt(self,evt):


        if self.currentlyPlaying:
            if (self.pbtick < self.maxplayticks):
                self.playbackTick(self.pbtick)
                self.pbtick = self.pbtick + 1
            else:
                self.stopPlayback()
            
        else:
            # Poll pygame for joystick events
            e = pygame.event.poll()
            while (e.type != pygame.NOEVENT):
                self.OnJoystick(e)
                e = pygame.event.poll()
            if self.currentlyLogging:
                self.ticks = self.ticks + 1
                self.maxplayticks = self.ticks
                self.recordState(self.ticks)
                self.printState(self.ticks)
        #self.SendSerial()


    ########################################################
    # recording/playback handlers

    # store events in event structure at time "tick" 
    def recordState(self,tick):
        print "recording state %d" % tick
        thisEv = eventStruct()
        thisEv.tick = tick
        thisEv.steer = self.steer.GetValue()
        thisEv.drive = self.drive.GetValue()
        self.eventList.append(thisEv)

    def printState(self,tick):
        print "event at %d" % tick
        thisEv =  self.eventList[tick-1]

    def playbackTick(self,tick):
        thisEv = self.eventList[tick]
        print "playback percent  %2.0f%%" % (100*float(tick)/float(self.maxplayticks))
        self.steer.SetValue(thisEv.steer) 
        self.drive.SetValue(thisEv.drive)


    def startRecord(self):
        self.currentlyLogging = 1
        self.logBtn.SetBackgroundColour((255,0,0))
        self.logBtn.SetLabel('STOP')

    def stopRecord(self):
        self.logBtn.SetBackgroundColour((0,255,0))
        self.logBtn.SetLabel('RECORD')
        self.currentlyLogging = 0
        self.ticks=0
    
    def startPlayback(self):
        # start playback
        self.currentlyPlaying = 1
        self.playBtn.SetBackgroundColour((255,0,255))
        self.playBtn.SetLabel('STOP')
        self.pbtick=0
            
    def stopPlayback(self):
        self.playBtn.SetBackgroundColour((0,255,0))
        self.playBtn.SetLabel('PLAY')
        self.currentlyPlaying = 0


    #####################################  serial handlers

    # all serial commands except stop are filtererd through this
    # process which runs at a rate set by InitTimer()
    # (This is so commands are not sent too fast as requested by Pete.)
    # (olddrive and oldsteer mimic static vars with immutable Python lists)

    def SendSerial(self,olddrive=[0],oldsteer=[0]): 
        #self.outputStatus(self.ReadSerial())
        drive = self.drive.GetValue()
        if drive != olddrive[0]:
            print "Drive: %d" % drive
            #self.ser.write('$t%d*' % drive)
            self.ser.write('$p%d*' % drive)
            olddrive[0] = drive
            #self.outputStatus(self.ReadSerial())
        steer = self.steer.GetValue()
        if steer != oldsteer[0]:
            print "Steer: %d" % steer
            self.ser.write('$s%d*' % steer)
            #self.outputStatus(self.ReadSerial())
            self.ser.write('$?*')
            statline=self.ReadSerial()
            print statline
            oldsteer[0] = steer
        # send any queued serial commands
        while (len(self.outQ) > 0) :
            self.ser.write(self.outQ.pop(0))
            self.resultbox.SetValue('')  # clear result box
            #self.resultbox.AppendText(self.ReadSerial())
            print self.ReadSerial()
        #send status query command to flush queue:
        #send debug off query command to flush queue:
        self.ser.write('$L0*')
        #statline=self.ReadSerial()
        #print statline
        #self.outputStatus(self.ReadSerial())
        #self.ser.write('$QI*')
        #self.parseStatus(self.ReadSerial())
            
    def ReadSerial(self):
        incount = self.ser.inWaiting()
        readstring = ""
        while (incount > 0):
            readstring += self.ser.read(incount)
            incount = self.ser.inWaiting()
        #print "got %s" % readstring
        return readstring

    def outputStatus(self,statusText):
        self.statusbox.Clear()
        self.statusbox.AppendText(statusText)
        if (self.currentlyLogging):
            self.logfile.write(statusText)       

    # parse stuff out of the output given key value pairs
    def parseStatus(self,statusText):
        
        #print "status: %s\n" % statusText
        statusList = statusText.split()
        #print "status list length %d\n" % len(statusList)
        if (self.currentlyLogging):
            if self.fileLen == 0:
                dataHeader = statusList[0:len(statusList):2]
                #print dataHeader
                strList= [str(value) for value in dataHeader]
                self.logger.writerow(strList)
            
        dataLine =  statusList[1:len(statusList):2]
        #print dataLine
        if (self.currentlyLogging):
            intList= [int(value) for value in dataLine]
            self.logger.writerow(intList)
            self.fileLen += 1




########################################  joystick handling 

    def OnJoystick(self,e):
        """ Called on any joystick event: change sliders """

        if e.type == pygame.JOYAXISMOTION:
            if (e.dict['axis'] == 0):
                print "Stick %d Axis 0 %f" % (e.dict['joy'], e.dict['value'])
                self.steer.SetValue(self.JoyXtoSteer(e.dict['value']))
            elif (e.dict['axis'] == 1):
                print "Stick %d Axis 1 %f" % (e.dict['joy'], e.dict['value'])
                self.drive.SetValue(self.JoyYtoDrive(e.dict['value']))
        elif e.type == pygame.JOYBUTTONDOWN:
            print "Stick %d Button %d" % (e.dict['joy'], e.dict['button'])

    def JoyYtoDrive(self,y):
        """ return joystick value normalized to drive max/min"""
        normy = float((y - self.jymin)/self.jyrange)
        return int( (1-normy)*driveRange + driveMin)

    def JoyXtoSteer(self,x):
        """ return joystick value normalized to steer max/min"""
        normx = float((x - self.jxmin)/self.jxrange)
        return int( normx*steerRange + steerMin)

######################################## init functions

    def InitTimer(self,milliseconds):
        self.t1 = wx.Timer(self)
        self.t1.Start(milliseconds)
    
    # get joystick and init
    def InitJoystick(self):

        self.myJoy = multiJoy()
        if (self.myJoy.numjoy <=0):
            wx.MessageBox("Sorry, no joystick found?")

        # Get the joystick range of motion (can't read them so assume)
        self.jxmin = -1
        self.jxmax =  1
        self.jxrange = max(self.jxmax - self.jxmin, 1)
        
        self.jymin = -1
        self.jymax = 1
        self.jyrange = max(self.jymax - self.jymin, 1)


    def InitSerial(self,comport,baudrate):
        self.SetStatusText("opening serial port: "+ comport) 
        try:
            self.ser = serial.Serial(comport, baudrate, timeout=0)  
        except serial.SerialException, v:
            dlg = wx.SingleChoiceDialog(
                self, "Can't open port "+comport+', Please select another.\n (Look in the Device Manager for available ports.\nIf no ports available, select "stdout" to test',
                "Select a port",
                ['COM2',
                 'COM3',
                 'COM3',
                 'COM4',
                 'COM5',
                 'COM7',
                 'COM6',
                 'COM7',
                 'COM8',
                 'stdout'],
                wx.CHOICEDLG_STYLE
                )
            
            if dlg.ShowModal() == wx.ID_OK:
                comport = dlg.GetStringSelection()
            dlg.Destroy()

            if comport == "stdout":
                self.ser = sys.stdout
                self.SetStatusText("Using stdout for serial out") 
                return
            try:
                self.ser = serial.Serial(comport, baudrate, timeout=1)  
            except serial.SerialException, v:
                wx.MessageBox("Serial error: could not open port %s" % comport)
                self.SetStatusText("No serial out") 
                return
        
        self.SetStatusText("Using serial port: "+ self.ser.portstr) 



class DashboardApp(wx.App):
    def OnInit(self):
        frame = Dashboard(None, "SWARM Dashboard v1.0")
        self.SetTopWindow(frame)
        frame.Show(True)
        return True


class joyStruct:
    pass

class multiJoy:
    joy = []
    numjoy = 0;

    def __init__(self):

	# init pygame
	pygame.joystick.init()
	pygame.display.init()
	if not pygame.joystick.get_count():
            print "oops - can't find joysticks"
            self.numjoy=-1
            return 
	print "%d joysticks detected." % pygame.joystick.get_count()
	for i in range(pygame.joystick.get_count()):
            myjoy = joyStruct()
            myjoy.n = i
            myjoy.j = pygame.joystick.Joystick(i)
            myjoy.j.init()
            self.joy.append(myjoy)
            print "Got stick %d axes: %d name: " % (i, self.joy[i].j.get_numaxes()) + self.joy[i].j.get_name()
        self.numjoy=pygame.joystick.get_count()

    def handleJoyEvent(self,e):
        if e.type == pygame.JOYAXISMOTION:
            if (e.dict['axis'] == 0):
                print "Stick %d Axis 0 %f" % (e.dict['joy'], e.dict['value'])
            elif (e.dict['axis'] == 1):
                print "Stick %d Axis 1 %f" % (e.dict['joy'], e.dict['value'])
        elif e.type == pygame.JOYBUTTONDOWN:
            print "Stick %d Button %d" % (e.dict['joy'], e.dict['button'])
	else:
            pass

    def getJoy(self):
        e = pygame.event.poll()
        while (e.type == pygame.JOYAXISMOTION or e.type == pygame.JOYBUTTONDOWN):
            print "got event!"
            self.handleJoyEvent(e)
            e = pygame.event.poll()
        
app = DashboardApp(redirect=False)
app.MainLoop()

