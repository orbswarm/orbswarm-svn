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
comport = "COM5"
baudrate = 38400
logFileName = 'dash-log.txt'

# User settings: max/min steering and drive values. OK to edit!
driveMax = 40
driveMin = -40

#use 40 for PID speed control 
#driveMax = 40
#driveMin = -40

steerMax = 100
steerMin = -100

STEERID = 100
DRIVEID = 101
AUXID = 102

# don't edit these
driveRange = driveMax - driveMin
steerRange = steerMax - steerMin

# struct for orb controller
class orbStruct:
	id = 0
	pass

# struct to hang events from 
class eventStruct:
	pass

class Dashboard(wx.Frame):
    
    def __init__(self, parent, title):
        wx.Frame.__init__(self, parent, -1, title,
                          pos=(150, 20), size=(550, 700))

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

        self.InitJoystick()
        self.InitTimer(100)
        self.Bind(wx.EVT_TIMER, self.OnTimerEvt)
        self.tick=0
        self.outQ = []  #output queue for serial commands

	# make list of orbstructs
	self.orb = []
	for n in range(6):
            self.orb.append(orbStruct())

	count = 0
	for orb in self.orb:
		orb.enabled = 1
		orb.orbID = count + 60
		count = count + 1
		
        # create the Panel to put the controls on.
        panel = wx.Panel(self)

        # add drive control
        drivelabel = wx.StaticText(panel, DRIVEID, "Drive")
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
	self.drive.sID = DRIVEID

        #steering control
        steerlabel = wx.StaticText(panel,STEERID, "Steering")
        steerlabel.SetFont(wx.Font(12, wx.SWISS, wx.NORMAL, wx.BOLD))
        steerlabel.SetSize(steerlabel.GetBestSize())
        # slider args: parent, ID, value, min, max, pos, size, flags
        self.steer = wx.Slider(panel, -1, 0, steerMin, steerMax,
                               wx.DefaultPosition, (250,-1), 
                               wx.SL_HORIZONTAL |
                               wx.SL_AUTOTICKS |
                               wx.SL_LABELS)
        self.steer.SetTickFreq(8, 1)
	self.steer.sID = STEERID


	        #steering control
        auxlabel = wx.StaticText(panel, AUXID, "AUX")
        auxlabel.SetFont(wx.Font(12, wx.SWISS, wx.NORMAL, wx.BOLD))
        auxlabel.SetSize(steerlabel.GetBestSize())
        # slider args: parent, ID, value, min, max, pos, size, flags
        self.aux = wx.Slider(panel, -1, 0, -100, 100,
                               wx.DefaultPosition, (-1,250), 
                               wx.SL_VERTICAL |
                               wx.SL_AUTOTICKS |
                               wx.SL_LABELS |
                               wx.SL_LEFT  |
                               wx.SL_INVERSE)
        self.aux.SetTickFreq(8, 1)
	self.aux.sID = AUXID


	


        # status box for returned serial data
        self.statusbox = wx.TextCtrl(panel, -1,"serial output in this box",
                                     size=(300, 100),
                                     style=wx.TE_MULTILINE|wx.TE_PROCESS_ENTER)

        # text box to enter custom commands 
        self.commandbox = wx.TextCtrl(panel, -1,"send this",
                                      size=(200, 20),
                                      style=wx.TE_PROCESS_ENTER)
#        self.resultbox = wx.TextCtrl(panel, -1,"",
#                                      size=(200, 20),
#                                      style=wx.TE_PROCESS_ENTER)

        self.cbox = []
	self.cbox.append(wx.CheckBox(panel, -1, "Orb60"))
	self.cbox.append(wx.CheckBox(panel, -1, "Orb61"))
	self.cbox.append(wx.CheckBox(panel, -1, "Orb62"))
	self.cbox.append(wx.CheckBox(panel, -1, "Orb63"))
	self.cbox.append(wx.CheckBox(panel, -1, "Orb64"))
	self.cbox.append(wx.CheckBox(panel, -1, "Orb65"))


	count = 0;
	for cbox in self.cbox:
		cbox.n = count
		cbox.SetValue(self.orb[count].enabled)
		count = count + 1
# Should disable if no joystick
#		if (count > self.myJoy.numjoy): 
#			cbox.Enable(0)

        # send emergency stop command
        stopBtn = wx.Button(panel, -1, "ALL STOP")
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
        self.Bind(wx.EVT_SCROLL, self.OnSlideEvt,self.aux)
        self.Bind(wx.EVT_BUTTON, self.OnStopBtn, stopBtn)
        self.Bind(wx.EVT_BUTTON, self.OnSendBtn, sendBtn)
        self.Bind(wx.EVT_BUTTON, self.OnLogBtn, self.logBtn)
        self.Bind(wx.EVT_BUTTON, self.OnPlayBtn, self.playBtn)
	for cbox in self.cbox:
		self.Bind(wx.EVT_CHECKBOX, self.OnCheckBox, cbox)


        # Bind commandbox "enter" key to execute command
        self.Bind(wx.EVT_TEXT_ENTER , self.OnSendBtn, self.commandbox)

 
        # Use a sizer to lay out the controls, in a grid
        bigGrid = wx.FlexGridSizer(11, 3, 10, 10)  # rows, cols, hgap, vgap
        bigGrid.AddMany([
            # top row
            (drivelabel,     0,  wx.ALIGN_CENTER),
            (steerlabel,     0,  0),
            (auxlabel,     0,  0),
            # second row
            (self.drive,     0, wx.ALIGN_RIGHT),
            (self.steer,     0, wx.ALIGN_LEFT ),
            (self.aux,     0, wx.ALIGN_LEFT ),
            # third row
            (stopBtn,        0, wx.ALIGN_CENTER),
            (self.statusbox, 0, wx.ALIGN_CENTER),
            ((10,10),     0,  wx.ALIGN_CENTER),	    
            #fourth row
            (sendBtn,        0, wx.ALIGN_CENTER),
            (self.commandbox,0, wx.ALIGN_LEFT),
            ((10,10),     0,  wx.ALIGN_CENTER),	    
            #fifth row
            (self.logBtn,        0, wx.ALIGN_CENTER),
            (self.playBtn,       0, wx.ALIGN_CENTER),
            ((10,10),     0,  wx.ALIGN_CENTER),	    
	     #sixth row
            (self.cbox[0],     0,  wx.ALIGN_CENTER),
            ((10,10),     0,  wx.ALIGN_CENTER),
            ((10,10),     0,  wx.ALIGN_CENTER),	    
	     # row 7
            (self.cbox[1],     0,  wx.ALIGN_CENTER),
            ((10,10),     0,  wx.ALIGN_CENTER),	    
            ((10,10),     0,  wx.ALIGN_CENTER),	    
	     # row 8
            (self.cbox[2],     0,  wx.ALIGN_CENTER),
            ((10,10),     0,  wx.ALIGN_CENTER),	    
            ((10,10),     0,  wx.ALIGN_CENTER),	    
	     # row 9
            (self.cbox[3],     0,  wx.ALIGN_CENTER),
            ((10,10),     0,  wx.ALIGN_CENTER),	    
            ((10,10),     0,  wx.ALIGN_CENTER),	    
	     # row 10
            (self.cbox[4],     0,  wx.ALIGN_CENTER),
            ((10,10),     0,  wx.ALIGN_CENTER),	    
            ((10,10),     0,  wx.ALIGN_CENTER),	    
	     # row 11
            (self.cbox[5],     0,  wx.ALIGN_CENTER),
            ((10,10),     0,  wx.ALIGN_CENTER),	    
            ((10,10),     0,  wx.ALIGN_CENTER)	    
            ])
        panel.SetSizer(bigGrid)
        panel.Layout()


######################################### event handlers
    def OnCheckBox(self, evt):
	cbox = evt.GetEventObject()
	print cbox.GetValue()
	self.orb[cbox.n].enabled = cbox.GetValue() 

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
	slider = evt.GetEventObject()
	if slider.sID == STEERID:
	    self.DoDriveEvt()	
	elif slider.sID == DRIVEID:
	    self.DoSteerEvt()
        elif slider.sID == AUXID:
	    self.DoAuxEvt()	


    def DoDriveEvt(self):
	drive = self.drive.GetValue()
	cmd = ""
	for o in self.orb:
	    if(o.enabled):    
		cmd +=  "{%d $p%d*}\n" % (o.orbID,drive)   
	print cmd
	self.ser.write(cmd);

    def DoSteerEvt(self):
	steer = self.steer.GetValue()
	for o in self.orb:
	    if(o.enabled):    
	        cmd =  "{%d $s%d*}" % (o.orbID,steer)
	        print cmd
		self.ser.write(cmd);
		
    def DoAuxEvt(self):
	aux = self.aux.GetValue()
	for o in self.orb:
	    if(o.enabled):    
	        cmd =  "{%d <LB%d>}" % (o.orbID,int(2.5*(aux+100)))
	        cmd +=  "{%d <LF>}" % o.orbID
	        print cmd
		self.ser.write(cmd);

    def DoJoyButton(self, joy, button):
	print "Stick %d Button %d" % (joy,button)
	for o in self.orb:
	    cmd = ""	
	    if(o.enabled):
		if button == 0:    
		    cmd =  "{%d <M1 VPA>}" % o.orbID
		elif  button == 1:       
		    cmd =  "{%d <LR%d>}" %   (o.orbID,0)
		    cmd +=  "{%d <LG%d>}" %   (o.orbID,0)
		    cmd +=  "{%d <LB%d>}" %   (o.orbID,0)
		    cmd +=  "{%d <LF>}\n" % o.orbID
		elif  button == 2:
		    cmd =  "{%d <M1 VPF 49443526.mp3>}\n" % o.orbID
		elif  button == 3:       
		    cmd =  "{%d <M1 VPF 55061608.mp3>}" % o.orbID
		elif  button == 4:
		    cmd =  "{%d <M1 VPF 58720567.mp3>}" % o.orbID
		elif  button == 6:
		    cmd =  "{%d <LR%d>}" %   (o.orbID,255)
		    cmd +=  "{%d <LG%d>}" %   (o.orbID,0)
		    cmd +=  "{%d <LB%d>}" %   (o.orbID,0)
		    cmd +=  "{%d <LF>}\n" % o.orbID
		    cmd +=  "{%d <M1 VPF 58720567.mp3>}" % o.orbID
		print cmd
		self.ser.write(cmd);

    # this is called regularly by the timer.    
    def OnTimerEvt(self,evt):

        if self.currentlyPlaying:
            if (self.pbtick < self.maxplayticks):
                self.playbackTick(self.pbtick)
                self.pbtick = self.pbtick + 1
            else:
                self.stopPlayback()
            
        else:
	    # send all commands all the time 
	    self.DoDriveEvt()
	    self.DoSteerEvt()
	    self.DoAuxEvt()

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
                #print "Stick %d Axis 0 %f" % (e.dict['joy'], e.dict['value'])
                self.steer.SetValue(self.JoyXtoSteer(e.dict['value']))
		self.DoSteerEvt()
            elif (e.dict['axis'] == 1):
                #print "Stick %d Axis 1 %f" % (e.dict['joy'], e.dict['value'])
                self.drive.SetValue(self.JoyYtoDrive(e.dict['value']))
		self.DoDriveEvt()
            elif (e.dict['axis'] == 2):
                #print "Stick %d Axis 2 %f" % (e.dict['joy'], e.dict['value'])
		return  # ignore for now
            elif (e.dict['axis'] == 3):
                print "Stick %d Axis 3 %f" % (e.dict['joy'], e.dict['value'])
                self.aux.SetValue(100*(e.dict['value']))
		self.DoAuxEvt()

        elif e.type == pygame.JOYBUTTONDOWN:
	    self.DoJoyButton(e.dict['joy'], e.dict['button'])

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

