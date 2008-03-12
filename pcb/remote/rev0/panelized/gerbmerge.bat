copy ..\remote.sts remote.sts 
copy ..\remote.plc remote.plc
copy ..\remote.stc remote.stc
copy ..\remote.sol remote.sol
copy ..\remote.cmp remote.cmp
copy ..\remote.drd remote.drd
copy ..\remote.bor remote.bor 
c:\python25\python c:\python25\lib\site-packages\gerbmerge\gerbmerge.py GMremote.cfg GMremote.def
del remote.*
