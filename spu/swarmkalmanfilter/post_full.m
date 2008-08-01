%
% output analysis
%
% STATE_vdot         1
% STATE_v            2
% STATE_phidot       3
% STATE_phi          4
% STATE_theta        5
% STATE_psi          6
% STATE_x    		7
% STATE_y    		8
% STATE_xab    		9
% STATE_yab   		10
% STATE_zab    		11
% STATE_xrb   		12
% STATE_zrb   		13
%
% MEAS_xa      	1
% MEAS_ya      	2
% MEAS_za      	3
% MEAS_xr      	4
% MEAS_zr      	5
% MEAS_xg      	6
% MEAS_yg    	7
% MEAS_psig    	8
% MEAS_vg    	9
% MEAS_omega	10
%

load -ascii full_data;
load -ascii full_data_out;
close all;

figure;
plot(full_data(:,6),full_data(:,7),"r");
hold;
plot(full_data_out(:,7),full_data_out(:,8),"b");
title('Overhead - GPS in Red, Kalman in Blue');
grid;

figure;
plot([full_data_out(:,2) .38*full_data(:,10) full_data(:,9)]);
title('Velocity (m/s)');
grid;

figure;
plot([-.125*(full_data(:,2)-full_data_out(:,10)) -(full_data(:,5)-full_data_out(:,13)) full_data_out(:,4)]);
title('Y acc (blue), Z rate (green), Phi (red)');

figure;
plot([full_data(:,8) + round((full_data_out(:,6)-full_data(:,8))/(2*pi))*2*pi full_data_out(:,6)]);
title('Heading (rad ccw vs East)');
grid;

figure;
gps_error = sqrt((full_data_out(:,7).-full_data(:,6)).^2 + (full_data_out(:,8).-full_data(:,7)).^2);
plot(gps_error);
grid;
title('GPS Error');

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
%plot([full_data(:,6) ya_est])
%title('Y acceleration residual (m/s)');

%  est_measurement[ MEAS_zr ] = - newState[ STATE_v ] * newState[ STATE_phi ] / RADIUS
%	+ newState[ STATE_zrb ];

%figure;
%zr_est = -full_data_out(:,2) .* full_data_out(:,4) ./ 0.38 .+ full_data_out(:,13);
%plot([full_data(:,4) zr_est]);
%title('Yaw Rate Residual (rad/s)');


