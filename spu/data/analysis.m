%
% Data Analysis for rolling test
% 7/28/08 MAP
%
% sensordata file:
% 1.time, 2.ratex, 3.ratez, 4.accx, 5.accy, 6.accz, 7.easting, 8.northing, 9.gps heading , 10.gps vel, 11.omega, 12.utmzone
%
% kalmandata
% 1.time, 2.vdot, 3. v, 4.phidot, 5.phi, 6.theta, 7.psi, 8.x, 9.y, 10.xab, 11.yab, 12.zab, 13.xrb, 14.zrb
%

close all;

load -ascii sensordata;
load -ascii kalmandata;

initialbias = kalmandata(1,:);

figure;
plot(sensordata(:,7),sensordata(:,8),"r");
hold;
plot(kalmandata(:,8),kalmandata(:,9),"b");
title('Overhead - GPS in Red, Kalman in Blue');

figure;
plot([kalmandata(:,3) .38*sensordata(:,11) sensordata(:,10)]);
title('Velocity (m/s)');

figure;
plot( [sensordata(:,9) + round((kalmandata(:,7)-sensordata(:,9))/(2*pi))*2*pi kalmandata(:,7)]);
title('Heading (rad ccw vs East)');

my_data = [sensordata(:,4) sensordata(:,5) sensordata(:,6) sensordata(:,2) sensordata(:,3) sensordata(:,7) sensordata(:,8) sensordata(:,9) sensordata(:,10) sensordata(:,11)];
save -text full_data my_data
