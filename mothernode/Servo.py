import serial
class Servo:
    def __init__(self, ID,ser,labelStr):
        self.DEFMIN = 0;
        self.DEFMAX = 255;
        self._ID = ID;
        self.ser = ser; # serial controller for I/O
        self.label = labelStr
        self.maxlimit = self.DEFMAX  # max should > min so no divide-by-zero
        self.minlimit = self.DEFMIN
        self.currentpos = 0
        self.homepos = 0
	self.sense = 0 # Toggle this bit to reverse limit switch sense
        self.homepos = (self.minlimit+self.maxlimit)/2
        self.verbose = 1  #output debugging info to console

##    # convert zero-one normalized position to absolute position
##   # def Norm2Abs(self,norm):
##   #     if (norm < 0):
##    #        norm = 0
##     #   elif (norm > 1):
##      #      norm = 1
##       # range = self.maxlimit - self.minlimit
##        #print "Norm2Abs: %f -> %d"%(norm,self.minlimit + int(norm*range))
##        #return((self.minlimit + int(norm*range)))
##       # return((int((norm*100)-50)))
##
##    def Abs2Norm(self,abs):
##        if (abs < self.minlimit):
##            abs = self.minlimit
##        elif (abs > self.maxlimit):
##            abs = self.maxlimit
##        range = self.maxlimit - self.minlimit
##        #print "Abs2Norm: %d -> %f"%(abs, float(abs - self.minlimit)/range)
##        return(int(float (abs - self.minlimit)/range))

    # move this servo to the specified normalized position
    # normalized position is a float between 0 and 1
    # send move command only if motion difference is more than thresh away
    # from currentpos

    def GotoNormPos(self,normpos):
        self.GotoAbsPosition(normpos)
        
    def Goto(self,position,thresh=1):
        if position < self.minlimit:
            position = self.minlimit
            
        if position > self.maxlimit:
                position = self.maxlimit;
        if self.verbose:
            print 'pos, cpos: %d %d'%(position, self.currentpos)
            print 'min, max: %d %d'%(self.minlimit, self.maxlimit)
        if (abs(self.currentpos - position) > thresh):
            self.GotoAbsPosition(position)
            self.currentpos = position
            #time.sleep(0.01)

    # move this servo to the specified absolute position
    def GotoAbsPosition(self,position):
        if self.verbose:
            print 'Channel: %d; position %d'%(self._ID,position)
	#self.ser.write("\xff")      #write a string
	#out = '%c' % self._ID
        out = ''
        if (self._ID == 0):
            out = '$s%d*' % (int(position*1024)-512)
        if (self._ID == 1):
            out = '$p%d*' % (int(position*200)-100)
            
	#self.ser.write(out)      #write a string
        #out = '%c' % position
	self.ser.write(out)      #write a string        
        print out
        
    def HomeS(self):
        print 'at HomeM %d'%self._ID
        self.homepos = self.minposition + abs((self.maxposition-self.minposition)/2)
        self.GotoAbsPosition(self.homepos)
        
    def PrintStatus(self):
        print 'Servo ID      \"%s\"'%self._ID

    def ReadPosition(self):
        print self.currentpos
