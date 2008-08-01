%
% Generate Test Data
% 7/29/08
%
% sensordata
% 1.time, 2.ratex, 3.ratez, 4.accx, 5.accy, 6.accz, 7.easting, 8.northing, 9.gps heading , 10.gps vel, 11.omega, 12.utmzone
%

load -ascii sensordata;

my_data = [sensordata(:,4) sensordata(:,5) sensordata(:,6) sensordata(:,2) sensordata(:,3) sensordata(:,7) sensordata(:,8) sensordata(:,9) sensordata(:,10) sensordata(:,11)];

save -text full_data my_data
