#!/bin/sh

exe=joypatcher
opts=
while [ 1 ]
do
  pidof $exe > /dev/null
  if [ $? -eq 0 ]
  then
    echo "running" > /dev/null
  else
    echo "not" > /dev/null
    /root/$exe > /dev/null
  fi
  sleep 1
  done
exit 0
