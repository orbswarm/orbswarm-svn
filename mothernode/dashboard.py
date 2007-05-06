#----------------------------------------------------------------------
# dashboard.py: SWARM Python dashboard to connect with Orb SPU
#               Send commands and receive/display telemetry and status info
#               v1.0  J. Foote 5/07
#----------------------------------------------------------------------

import wx
import serial
import sys


# User settings. OK to edit these if you know what you are doing 
comport = "COM4"
baudrate = 38400

# User settings: max/min steering and power values. OK to edit!
powerMax = 25
powerMin = -25
steerMax = 100
steerMin = -100


# don't edit these
powerRange = powerMax - powerMin
steerRange = steerMax - steerMin

class Dashboard(wx.Frame):
    
    def __init__(self, parent, title):
        wx.Frame.__init__(self, parent, -1, title,
                          pos=(150, 150), size=(500, 600))

        # Create the menubar
        menuBar = wx.MenuBar()

        # and a menu 
        menu = wx.Menu()

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
       
        self.outQ = []  #output queue for serial commands
        
        # create the Panel to put the controls on.
        panel = wx.Panel(self)

        # add power control
        powerlabel = wx.StaticText(panel, -1, "Drive")
        powerlabel.SetFont(wx.Font(12, wx.SWISS, wx.NORMAL, wx.BOLD))
        powerlabel.SetSize(powerlabel.GetBestSize())
        # slider args: parent, ID, value, min, max, pos, size, flags
        self.power = wx.Slider(panel, -1, 0, powerMin, powerMax,
                               wx.DefaultPosition,(-1,250), 
                               wx.SL_VERTICAL |
                               wx.SL_AUTOTICKS |
                               wx.SL_LABELS |
                               wx.SL_LEFT  |
                               wx.SL_INVERSE)
        self.power.SetTickFreq(8, 1)

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

        # bind the control events to handlers
        self.Bind(wx.EVT_SCROLL, self.OnSlideEvt,self.power)
        self.Bind(wx.EVT_SCROLL, self.OnSlideEvt,self.steer)
        self.Bind(wx.EVT_BUTTON, self.OnStopBtn, stopBtn)
        self.Bind(wx.EVT_BUTTON, self.OnSendBtn, sendBtn)

        # Bind commandbox "enter" key to execute command
        self.Bind(wx.EVT_TEXT_ENTER , self.OnSendBtn, self.commandbox)

        # Use a sizer to lay out the controls, in a grid
        bigGrid = wx.FlexGridSizer(5, 2, 10, 10)  # rows, cols, hgap, vgap
        bigGrid.AddMany([
            # top row
            (powerlabel,     0,  wx.ALIGN_CENTER),
            (steerlabel,     0,  0),
            # second row
            (self.power,     0, wx.ALIGN_RIGHT),
            (self.steer,     0, wx.ALIGN_LEFT ),
            # third row
            (stopBtn,        0, wx.ALIGN_CENTER),
            (self.statusbox, 0, wx.ALIGN_CENTER),
            #fourth row
            (sendBtn,        0, wx.ALIGN_CENTER),
            (self.commandbox,0, wx.ALIGN_LEFT),
            #fifth row
            ((20,20),        0, wx.ALIGN_CENTER),
            (self.resultbox,0, wx.ALIGN_LEFT),
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
        self.ser.write("$STOP*")
        self.power.SetValue(0)

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

    def OnJoystick(self,evt):
        """ Handle any joystick events """
        self.SendJoyCommand()

    def OnSlideEvt(self,evt):
        foo=1
       # self.SendSerial()

    # this is called regularly by the timer.    
    def OnTimerEvt(self,evt):
        self.SendSerial()


    #####################################  serial handlers

    # all serial commands except stop are filtererd through this
    # process which runs at a rate set by InitTimer()
    # (This is so commands are not sent too fast as requested by Pete.)
    # (oldpower and oldsteer mimic static vars with immutable Python lists)

    def SendSerial(self,oldpower=[0],oldsteer=[0]):
        self.statusbox.AppendText(self.ReadSerial())
        power = self.power.GetValue()
        if power != oldpower[0]:
            print "Power: %d" % power
            self.ser.write('$p%d*' % power)
            oldpower[0] = power
            self.statusbox.AppendText(self.ReadSerial())
        steer = self.steer.GetValue()
        if steer != oldsteer[0]:
            print "Steer: %d" % steer
            self.ser.write('$s%d*' % steer)
            self.statusbox.AppendText(self.ReadSerial())
            oldsteer[0] = steer
        # send any queued serial commands
        while (len(self.outQ) > 0) :
            self.ser.write(self.outQ.pop(0))
            self.resultbox.SetValue('')  # clear result box
            self.resultbox.AppendText(self.ReadSerial())
        # send status query command:
        self.ser.write('$?*')
        self.statusbox.AppendText(self.ReadSerial())
            
    def ReadSerial(self):
        incount = self.ser.inWaiting()
        readstring = ""
        while (incount > 0):
            readstring += self.ser.read(incount)
            incount = self.ser.inWaiting()
        #print "got %s" % outstr
        return readstring


########################################  joystick handling 

    def SendJoyCommand(self):
        """ Called on any joystick event: change sliders """
        joyx =  float(self.stick.GetPosition().x)
        joyy =  float(self.stick.GetPosition().y)
        joyb =  self.stick.GetButtonState()

        self.steer.SetValue(self.JoyXtoSteer(joyx))
        self.power.SetValue(self.JoyYtoPower(joyy))
        
        if joyb == 1:
            self.statusbox.AppendText("button on")
#        else:
#            self.statusbox.AppendText("button off")

    def JoyYtoPower(self,y):
        """ return joystick value normalized to power max/min"""
        normy = float((y - self.jymin)/self.jyrange)
        return int( (1-normy)*powerRange + powerMin)

    def JoyXtoSteer(self,x):
        """ return joystick value normalized to power max/min"""
        normx = float((x - self.jxmin)/self.jxrange)
        return int( normx*steerRange + steerMin)

######################################## init functions

    def InitTimer(self,milliseconds):
        self.t1 = wx.Timer(self)
        self.t1.Start(milliseconds)
    
    # get joystick and init
    def InitJoystick(self):
        try:
            self.stick = wx.Joystick()
            if not self.stick:
                wx.MessageBox("Joystick not found.")
                return

            self.stick.SetCapture(self)
            self.stick.SetMovementThreshold(10)
    	    print "got joystick"
            # Get the joystick range of motion
            self.jxmin = self.stick.GetXMin()
            self.jxmax = self.stick.GetXMax()
            self.jxrange = max(self.jxmax - self.jxmin, 1)

            self.jymin = self.stick.GetYMin()
            self.jymax = self.stick.GetYMax()
            self.jyrange = max(self.jymax - self.jymin, 1)

                    
            self.Bind(wx.EVT_JOYSTICK_EVENTS, self.OnJoystick)
            #self.OnJoystick()
        except NotImplementedError, v:
            wx.MessageBox(str(v), "Error: Joystick not implemented.")



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
        
app = DashboardApp(redirect=False)
app.MainLoop()

