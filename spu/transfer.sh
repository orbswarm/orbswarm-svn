#!/bin/sh

# install.sh: FTP the argument file(s) to a list of orbs
# written by JTF
# SORRY, THIS ONLY SEEM TO WORK IN CYGWIN
# OTHER DISTROS HAVE BRAINDEAD FTP W/O -s option

# this are the last IP numbers of all orbs
orbList="60 61 62 63 64 65 66"
orbList="61"
# conncatenate the above with this prefix to get full IP addr
iprefix="192.168.1."


fname=${1}
for orb in ${orbList}
  do
  echo "root++binary" | tr "+" "\n" > ftpcommands

  for arg in ${@}
    do
    echo "put ${arg}" >> ftpcommands
  done
  echo "bye+" | tr "+" "\n" >> ftpcommands
#  echo "root++binary+put $fname+bye+" | tr "+" "\n" > ftpcommands
# ping dest to see if connected, 1 ping, 1000 ms timeout
# ping on different machines have different syntaxes: check this is OK for yers

  ping   -n 1  -w 500 ${iprefix}${orb}
# if ping was OK
  if [ $?  == 0 ]
      then
# if reachable, transfer file
 #     echo ftp -s:ftpcommands ${iprefix}${orb}
      ftp -s:ftpcommands ${iprefix}${orb}
  else
      echo "oops - "${orb}" not reachable"
  fi
done
