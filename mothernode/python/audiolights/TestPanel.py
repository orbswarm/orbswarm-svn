import sys
import wx
import colorsys
from math import cos, sin, radians

#from Main import opj

#----------------------------------------------------------------------

BASE  = 100.0    # sizes used in shapes drawn below
BASE2 = BASE/2
BASE4 = BASE/4

#hah 
class TestPanel(wx.Panel):
    def __init__(self, parent, log):
        self.log = log
        wx.Panel.__init__(self, parent, -1)

        self.Bind(wx.EVT_PAINT, self.OnPaint)


    def itemTransform(self,factor):
        ffactor = factor/200.0;
        #print "itemTransform factor: %f" % ffactor
        self.dc = wx.ClientDC(self)
        self.dc.Clear()
        self.dc.SetBrush(wx.Brush("red"))
        self.dc.DrawRectangle(BASE, BASE, 20, int(80*ffactor)) 
        self.dc.SetPen(wx.Pen(wx.Colour(0, 0, 0, 128)))
        self.dc.DrawSpline([(0,0), (50,50), (80,int(80*ffactor))] )
        self.dc.DrawLines([(0,0), (50,50), (80,int(80*ffactor))] )
        sys.stdout.flush()
        return

    def drawBargraph(self,bands, height, width, pad):
        #print "itemTransform factor: %f" % ffactor
        #print(str(bands))
        self.dc = wx.ClientDC(self)
        self.dc.Clear()
        self.dc.SetBrush(wx.Brush("green"))
        self.dc.SetPen(wx.Pen("black", 1))
        startx = pad/2
        for b in bands:
            y = int(height * b)
            self.dc.DrawRectangle(startx, 2*BASE - y, width, y)
            startx += width + pad
                                  
        #self.dc.SetPen(wx.Pen(wx.Colour(0, 0, 0, 128)))
        #self.dc.DrawSpline([(0,0), (50,50), (80,int(80*ffactor))] )
        #self.dc.DrawLines([(0,0), (50,50), (80,int(80*ffactor))] )
        
        return
   


    def OnPaint(self, evt):
        dc = wx.PaintDC(self)
        return
        self.dc = dc
        try:
            gc = wx.GraphicsContext.Create(dc)
        except NotImplementedError:
            dc.DrawText("This build of wxPython does not support the wx.GraphicsContext "
                        "family of classes.",
                        25, 25)
            return
        return

        gc.Translate(60, 75)       # reposition the context origin
        self.gc = gc
        self.tm = gc.CreateMatrix() 

        font = wx.SystemSettings.GetFont(wx.SYS_DEFAULT_GUI_FONT)
        font.SetWeight(wx.BOLD)
        gc.SetFont(font)

        apath = gc.CreatePath()
        #apath.AddCircle(0, 0, BASE2)
        #apath.MoveToPoint(0, -BASE2)
        #apath.AddLineToPoint(0, BASE2)
        #apath.MoveToPoint(-BASE2, 0)
        #apath.AddLineToPoint(BASE2, 0)
        #apath.CloseSubpath()
        apath.MoveToPoint(BASE, BASE)
        apath.CloseSubpath()
        apath.AddRectangle(-BASE4, -BASE4/2, BASE2, BASE4)
        self.apath = apath


        # make a path that contains a circle and some lines, centered at 0,0
        path = gc.CreatePath()
        path.AddCircle(0, 0, BASE2)
        path.MoveToPoint(0, -BASE2)
        path.AddLineToPoint(0, BASE2)
        path.MoveToPoint(-BASE2, 0)
        path.AddLineToPoint(BASE2, 0)
        path.CloseSubpath()
        path.AddRectangle(-BASE4, -BASE4/2, BASE2, BASE4)


        # Now use that path to demonstrate various capbilites of the grpahics context
        gc.PushState()             # save current translation/scale/other state 
        gc.Translate(60, 75)       # reposition the context origin

        gc.SetPen(wx.Pen("navy", 1))
        gc.SetBrush(wx.Brush("pink"))

        # show the difference between stroking, filling and drawing
        for label, PathFunc in [("StrokePath", gc.StrokePath),
                                ("FillPath",   gc.FillPath),
                                ("DrawPath",   gc.DrawPath)]:
            w, h = gc.GetTextExtent(label)
            
            gc.DrawText(label, -w/2, -BASE2-h-4)
            PathFunc(path)
            gc.Translate(2*BASE, 0)
            
        return
            
        gc.PopState()              # restore saved state
        gc.PushState()             # save it again
        gc.Translate(60, 200)      # offset to the lower part of the window
        
        gc.DrawText("Scale", 0, -BASE2)
        gc.Translate(0, 20)

        # for testing clipping
        #gc.Clip(0, 0, 100, 100)
        #rgn = wx.RegionFromPoints([ (0,0), (75,0), (75,25,), (100, 25),
        #                            (100,100), (0,100), (0,0)  ])
        #gc.ClipRegion(rgn)
        #gc.ResetClip()
        
        gc.SetBrush(wx.Brush(wx.Colour(178,  34,  34, 128)))   # 128 == half transparent
        for cnt in range(8):
            gc.Scale(1.08, 1.08)    # increase scale by 8%
            gc.Translate(5,5)     
            gc.DrawPath(path)


        gc.PopState()              # restore saved state
        gc.PushState()             # save it again
        gc.Translate(400, 200)
        gc.DrawText("Rotate", 0, -BASE2)

        # Move the origin over to the next location
        gc.Translate(0, 75)

        # draw our path again, rotating it about the central point,
        # and changing colors as we go
        for angle in range(0, 360, 30):
            gc.PushState()         # save this new current state so we can 
                                   # pop back to it at the end of the loop
            r, g, b = [int(c * 255) for c in colorsys.hsv_to_rgb(float(angle)/360, 1, 1)]
            gc.SetBrush(wx.Brush(wx.Colour(r, g, b, 64)))
            gc.SetPen(wx.Pen(wx.Colour(r, g, b, 128)))
            
            # use translate to artfully reposition each drawn path
            gc.Translate(1.5 * BASE2 * cos(radians(angle)),
                         1.5 * BASE2 * sin(radians(angle)))

            # use Rotate to rotate the path
            gc.Rotate(radians(angle))

            # now draw it
            gc.DrawPath(path)
            gc.PopState()

        # Draw a bitmap with an alpha channel on top of the last group
        bmp = wx.Bitmap('toucan.png')
        bsz = bmp.GetSize()
        gc.DrawBitmap(bmp,
                      #-bsz.width, 
                      #-bsz.height/2,

                      -bsz.width/2.5, 
                      -bsz.height/2.5,
                      
                      bsz.width, bsz.height)


        gc.PopState()
        

#----------------------------------------------------------------------

def runTest(frame, nb, log):
    win = TestPanel(nb, log)
    return win

#----------------------------------------------------------------------



overview = """<html><body>
<h2><center>wx.GraphicsContext and wx.GraphicsPath</center></h2>

The new advanced 2D drawing API is implemented via the
wx.GraphicsContext and wx.GraphicsPath classes.  They wrap GDI+ on
Windows, Cairo on wxGTK and CoreGraphics on OS X.  They allow
path-based drawing with alpha-blending and anti-aliasing, and use a
floating point cooridnate system.

<p>When viewing this sample pay attention to how the rounded edges are
smoothed with anti-aliased drawing, and how the shapes on the lower
half of the window are partially transparent, allowing you to see what
was drawn before.

</body></html>
"""



if __name__ == '__main__':
    import sys,os
    #import run
    #run.main(['', os.path.basename(sys.argv[0])] + sys.argv[1:])
