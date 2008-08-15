#!/bin/sh

#cd /root
#while true
#do
#    ps_cnt=0
#    ps -efw | grep DDISPATCHER
#    ps_cnt=`ps -efw | grep DDISPATCHER | wc -l`
#    
#    if [ ${ps_cnt} != 4 ] ; then
#           echo "ps_cnt=${ps_cnt}"
#           pkill dispatcher
#	   echo "killed old dispatcher"
#	   nohup ./dispatcher -DDISPATCHER >stdout.log 2>&1 &
#	   echo "restarted dispatcher"
#    else
#	echo "ps_cnt=${ps_cnt}" 
#	echo "dispatcher is running"
#    fi
#    sleep 5
#done

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
