#!/bin/sh

cd /root
while true
do 
    dispatcher_pids=`pidof dispatcher`
    echo "dispatcher pids=${dispatcher_pids}"
    pid_cnt=0
    for dispatcher_pid in ${dispatcher_pids}
    do
	pid_cnt=$[${pid_cnt} + 1]
    done
    echo "pid_cnt=${pid_cnt}"
    if [ ${pid_cnt} != 3 ]; then
	pkill dispatcher
	echo "killed old dispatcher"
	nohup ./dispatcher >stdout.log 2>&1 &
	echo "restarted dispatcher"
    else
	echo "dispatcher is running"
    fi
    sleep 5 
done
