%
% output analysis
%

load full_data_out;
close all;

plot(full_data_out(:,7),full_data_out(:,8));
hold;

plot((cleandata(:,9)-cleandata(1,9)),(cleandata(:,8)-cleandata(1,8)),"r");

figure;

gps_error = sqrt((full_data_out(:,9).-(cleandata(:,7)-cleandata(1,7))).^2 + (full_data_out(:,8).-(cleandata(:,8)-cleandata(1,8))).^2);

plot(t,gps_error);

figure;
plot([full_data_out(:,2) .38*cleandata(:,13).*cos(full_data_out(:,4)) cleandata(:,12)]);

%  est_measurement[ MEAS_ya ] = newState[ STATE_v ] * newState[ STATE_v ] * newState[ STATE_phi ] / RADIUS
%	- newState[ STATE_phi ] * GRAVITY + newState[ STATE_yab ];

ya_est = full_data_out(:,2).^2 .* full_data_out(:,4).^2 / .38 .- full_data_out(:,4)*9.81 + full_data_out(:,10);

figure;
plot([cleandata(:,7) ya_est])
