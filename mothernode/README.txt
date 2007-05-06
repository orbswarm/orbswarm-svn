

This directory contains code for the mothernode, the supervisory computer that controls the orbs

Right now (5/06/07) the only code available is the dashboard, which connects through a serial port to Pete's Orb_ESC motor controller (typically through a Zigbee wireless network, but it can connect directly as well). 

dashboard.py is written in python, using the wxpython GUI widget set. 

There are two ways to run it:

1. Install python on your machine. It should work on any OS. 
   You will need the following extensions:

   python 2.5  https://sourceforge.net/project/showfiles.php?group_id=78018
   wxPython for py version 2.5 (http://www.wxpython.org/download.php)
   pySerial (http://sourceforge.net/project/showfiles.php?group_id=46487)

   (If you are using Windows, you also need this)
   pyWin (http://sourceforge.net/project/showfiles.php?group_id=78018)

   Once these are installed,
   You should be able to click on dashboard.py and it will run. 
   You can edit dashboard.py to change serial port and baud rate.
   (dashboard.py defaults to COM4, if it can't open it it will offer a 
   dialog box letting you choose another. If you are on linux you will have 
   to edit dashboard.py and set "comport = /dev/ttya0" or whatever you need.
   
   If dashboard.py  doesn't run, open a command window, cd to the containing 
   directory, and execute 
   "C:\python25\python.exe dashboard.py" 
   to see any error messages.

   No need to run py2exesetup.py -- that's only for py2exe.


2. I've created a dashboard.exe that should run on windows machines without installing python. It's in the directory trunk/mothernode/dist/dashboard.exe. It needs the other files in that directory so you have to copy the whole directory. 
   
   I haven't tried this on other machines: it may need DLLs. Let me know if this works for you or not. 

   This was created by py2exe. 

