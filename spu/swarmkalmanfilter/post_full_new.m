%
% output analysis
%

load -ascii full_data;
load -ascii full_data_out;
close all;

plot(full_data_out(:,7),full_data_out(:,8));
hold;
plot(full_data(:,6),full_data(:,7),"r");
title('Overhead');

figure;
plot([-.125*(full_data(1:2000,2)-full_data_out(1:2000,10)) -(full_data(1:2000,5)-full_data_out(1:2000,13)) full_data_out(1:2000,4)]);
title('Y acc, Z rate, Phi');

figure;
plot([full_data_out(:,2) .38*full_data(:,10) full_data(:,9)]);
title('Velocity (m/s)');

figure;
plot( [full_data(:,8) + round((full_data_out(:,6)-full_data(:,8))/(2*pi))*2*pi full_data_out(:,6)]);
title('Heading');

%gps_error = sqrt((full_data_out(:,7).-(full_data(:,9)-full_data(1,9))).^2 + (full_data_out(:,8).-(full_data(:,8)-full_data(1,8))).^2);

%figure;
%plot(t,gps_error);

%figure;
%plot([full_data_out(:,2) .38*full_data(:,13) full_data(:,12)]);
%title('Velocity (m/s)');

% in post_full
%  est_measurement[ MEAS_ya ] = newState[ STATE_v ] * newState[ STATE_v ] * newState[ STATE_phi ] / RADIUS
%	- newState[ STATE_phi ] * GRAVITY + newState[ STATE_yab ];

% current kalmanswarm
%  est_measurement[ MEAS_ya ] = -newState[ STATE_v ] * newState[ STATE_v ] * newState[ STATE_phi ] / RADIUS
%	- newState[ STATE_phi ] * GRAVITY + newState[ STATE_yab ];

% reduced kalmanswarm 
%  est_measurement[ MEAS_ya ] = newState[ STATE_v ] * newState[ STATE_v ] * newState[ STATE_phi ] / RADIUS
%	- newState[ STATE_phi ] * GRAVITY + newState[ STATE_yab ];

%ya_est = full_data_out(:,2).^2 .* full_data_out(:,4) / .38 .- full_data_out(:,4)*9.81 + full_data_out(:,10);
%figure;
%subplot(2,1,1);
%plot([full_data(:,6) ya_est])
%subplot(2,1,2);
%plot(full_data_out(:,10));
%title('Y acceleration residual (m/s)');

%  est_measurement[ MEAS_zr ] = - newState[ STATE_v ] * newState[ STATE_phi ] / RADIUS
%	+ newState[ STATE_zrb ];

%figure;
%zr_est = -full_data_out(:,2) .* full_data_out(:,4) ./ 0.38 .+ full_data_out(:,13);
%plot([full_data(:,4) zr_est]);
%title('Yaw Rate Residual (rad/s)');


