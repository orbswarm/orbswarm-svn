%
% Data Analysis for rolling test
% 7/28/08 MAP
%
% sensordata file:
% 1.time, 2.ratex, 3.ratez, 4.accx, 5.accy, 6.accz, 7.easting, 8.northing, 9.gps heading , 10.gps vel, 11.omega, 12.utmzone
%
% kalmanoutput
% 1.time, 2.vdot, 3. v, 4.phidot, 5.phi, 6.theta, 7.psi, 8.x, 9.y, 10.xab, 11.yab, 12.zab, 13.xrb, 14.zrb
%

close all;

load -ascii sensordata;
load -ascii kalmanoutput;

initialbias = kalmanoutput(1,:);

x(1) = sensordata(1,7);
y(1) = sensordata(1,8);
for idx=2:(max(size(sensordata)))
	x(idx) = sensordata(idx,7)*0.01 + x(idx-1)*.99;
	y(idx) = sensordata(idx,8)*0.01 + y(idx-1)*.99;
endfor;

for idx=(max(size(sensordata))-1):1
	x(idx) = sensordata(idx,7)*0.01 + x(idx+1)*.99;
	y(idx) = sensordata(idx,8)*0.01 + y(idx+1)*.99;
endfor;

figure;
plot(x,y);
title('smooth gps');

gps_speed = sqrt(diff(x').^2+diff(y').^2)*10;
plot([gps_speed(1:1700) .38*sensordata(1:1700,11)]);
title('GPS Speed vs Encoder Speed (m/s)');

center_x = 9 * cos(0.3*pi);
center_y = 9 * sin(0.3*pi);
theta = linspace(0,2*pi);
circle_x = 9*cos(theta) + center_x;
circle_y = 9*sin(theta) + center_y;

figure;
plot(sensordata(:,7),sensordata(:,8),"r");
hold on;
%plot(kalmanoutput(:,8),kalmanoutput(:,9),"b");
plot(circle_x,circle_y,"g");
title('Overhead - GPS in Red, Kalman in Blue, Circle in Green');
grid on;
hold off;

figure;
plot(kalmanoutput(1:1700,1)-kalmanoutput(1,1), [kalmanoutput(1:1700,3) .38*sensordata(1:1700,11) sensordata(1:1700,10)]);
title('Velocity (m/s)');

figure;
plot( [sensordata(1:1700,9) + round((kalmanoutput(1:1700,7)-sensordata(1:1700,9))/(2*pi))*2*pi kalmanoutput(1:1700,7)]);
title('Heading (rad ccw vs East)');


