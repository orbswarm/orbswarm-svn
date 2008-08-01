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

figure;
plot(sensordata(:,7),sensordata(:,8),"r");
hold;
plot(kalmanoutput(:,8),kalmanoutput(:,9),"b");
title('Overhead - GPS in Red, Kalman in Blue');
grid;

figure;
plot(kalmanoutput(:,1)-kalmanoutput(1,1), [kalmanoutput(:,3) .38*sensordata(:,11) sensordata(:,10)]);
title('Velocity (m/s)');
grid;

figure;
plot(kalmanoutput(:,1)-kalmanoutput(1,1), [sensordata(:,9) + round((kalmanoutput(:,7)-sensordata(:,9))/(2*pi))*2*pi kalmanoutput(:,7)]);
title('Heading (rad ccw vs East)');
grid;

