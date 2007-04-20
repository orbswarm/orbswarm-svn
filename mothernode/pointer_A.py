from wxPython.wx import *
import wx
import serial
from Servo import Servo

ID_ABOUT = 101
ID_EXIT  = 102


class MyFrame(wxFrame):
    def __init__(self, parent, ID, title):
        wxFrame.__init__(self, parent, ID, title,
                         wxDefaultPosition, wxSize(500, 600))
        self.CreateStatusBar()
        self.SetStatusText("This is the statusbar")
    
#        self.SetBackgroundColour(wxColour(0,128,0))
        wxInitAllImageHandlers()



        self.ser = serial.Serial("COM1", 115200, timeout=0)  #open first serial port
        print self.ser.portstr       #check which port was realy used

            # define servo addresses
        self.S = ( Servo(0,self.ser,'Servo 1: Vertical'),
            Servo(1,self.ser,'Servo 2: Horizontal'),
            Servo(2,self.ser,'Controller 3: Laser On/Off'))

        self.S[1].minlimit=60
        self.S[1].maxlimit=200
        self.S[0].minlimit=100
        self.S[0].maxlimit=210
        self.S[2].minlimit=self.S[2].DEFMIN
        self.S[2].maxlimit=self.S[2].DEFMAX

        #ser.close()             #close port

        wxStaticText(self, -1, "Horizontal servo position", wxPoint(45, 15))

        self.slider = wxSlider(self, 100, 128, 0, 255, wxPoint(30, 60),
                          wxSize(250, -1),
                          wxSL_HORIZONTAL | wxSL_AUTOTICKS | wxSL_LABELS )
        self.slider.SetTickFreq(8, 1)

        wxStaticText(self, -1, "Vertical servo position", wxPoint(45, 75))
        self.slider2 = wxSlider(self, 101, 128, 0, 255, wxPoint(30, 90),
                          wxSize(250, -1),
                          wxSL_HORIZONTAL | wxSL_AUTOTICKS | wxSL_LABELS )
        self.slider2.SetTickFreq(8, 1)


        menu = wxMenu()
        menu.Append(ID_ABOUT, "&About",
                    "More information about this program")
        menu.AppendSeparator()
        menu.Append(ID_EXIT, "E&xit", "Terminate the program")

        menuBar = wxMenuBar()
        menuBar.Append(menu, "&File");

        self.SetMenuBar(menuBar)

        EVT_SCROLL(self, self.EvtSlide)

        # turn off laser
        self.S[2].Goto(self.S[2].DEFMAX)


    #       wxStaticText(self, -1, "This example uses the wxCheckBox control.",

        cID = 390
        self.cb1 = wxCheckBox(self, cID,   "Laser", wxPoint(65, 140), wxSize(150, 20), wxNO_BORDER)
        EVT_CHECKBOX(self, cID,   self.EvtCheckBox)

        try:
            self.stick = wxJoystick()
            self.stick.SetCapture(self)
            # Get the joystick position as a float
            joyx =  float(self.stick.GetPosition().x)
            joyy =  float(self.stick.GetPosition().y)

            # Get the joystick range of motion
            xmin = self.stick.GetXMin()
            xmax = self.stick.GetXMax()
            if xmin < 0:
                xmax += abs(xmin)
                joyx += abs(xmin)
                xmin = 0
            self.xrange = float(max(xmax - xmin, 1))

            ymin = self.stick.GetYMin()
            ymax = self.stick.GetYMax()
            if ymin < 0:
                ymax += abs(ymin)
                joyy += abs(ymin)
                ymin = 0
            self.yrange = float(max(ymax - ymin, 1))


            EVT_JOYSTICK_EVENTS(self, self.OnJoystick)
            self.UpdateFields()
        except NotImplementedError, v:
            wxMessageBox(str(v), "Error: Joystick not implemented.")


    def UpdateFields(self):
        joyx =  float(self.stick.GetPosition().x)
        joyy =  float(self.stick.GetPosition().y)
        joyb =  self.stick.GetButtonState()
        #print 'X: %6f Y: %6f'%(joyx/self.xrange,joyy/self.yrange)

        normX = 1-(joyx/self.xrange)
        normY = 1-(joyy/self.yrange)

        self.S[0].GotoNormPos(normX)
        i = self.slider.SetValue(int(normX*self.slider.GetMax()))

        self.S[1].GotoNormPos(normY)
        i = self.slider2.SetValue(int(normY*self.slider2.GetMax()))
        
        if joyb == 1:
            self.S[2].Goto(self.S[2].DEFMIN)
            self.cb1.SetValue(True)
        else:
            self.S[2].Goto(self.S[2].DEFMAX)
            self.cb1.SetValue(False)


    def OnJoystick(self, evt):
            self.UpdateFields()


    def EvtCheckBox(self, event):
        if self.cb1.GetValue():
            self.S[2].Goto(self.S[2].DEFMIN)
        else:
            self.S[2].Goto(self.S[2].DEFMAX)

    def EvtSlide(self,event):
        normX = 1 - (float(self.slider.GetValue())/self.slider.GetMax())
        self.S[0].GotoNormPos(normX)
        normY = 1 - (float(self.slider2.GetValue())/self.slider2.GetMax())
        self.S[1].GotoNormPos(normY)
        print normX
        print normY


 
class MyApp(wxApp):
    def OnInit(self):
        frame = MyFrame(NULL, -1, "Hello from wxPython")
        frame.Show(true)
        self.SetTopWindow(frame)
        return true

app = MyApp(0)
app.MainLoop()


















