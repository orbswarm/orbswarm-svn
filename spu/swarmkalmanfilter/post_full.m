%
% output analysis
%

load rect_data_out;
close all;

plot(rect_data_out(:,7),rect_data_out(:,8));
hold;

plot((cleandata(:,9)-cleandata(1,9)),(cleandata(:,8)-cleandata(1,8)),"r");



gps_error = sqrt((rect_data_out(:,7).-(cleandata(:,9)-cleandata(1,9))).^2 + (rect_data_out(:,8).-(cleandata(:,8)-cleandata(1,8))).^2);

%figure;
%plot(t,gps_error);

figure;
plot([rect_data_out(:,2) .38*cleandata(:,13) cleandata(:,12)]);
title('Velocity (m/s)');

% in post_full
%  est_measurement[ MEAS_ya ] = newState[ STATE_v ] * newState[ STATE_v ] * newState[ STATE_phi ] / RADIUS
%	- newState[ STATE_phi ] * GRAVITY + newState[ STATE_yab ];

% current kalmanswarm
%  est_measurement[ MEAS_ya ] = -newState[ STATE_v ] * newState[ STATE_v ] * newState[ STATE_phi ] / RADIUS
%	- newState[ STATE_phi ] * GRAVITY + newState[ STATE_yab ];

% reduced kalmanswarm 
%  est_measurement[ MEAS_ya ] = newState[ STATE_v ] * newState[ STATE_v ] * newState[ STATE_phi ] / RADIUS
%	- newState[ STATE_phi ] * GRAVITY + newState[ STATE_yab ];

ya_est = rect_data_out(:,2).^2 .* rect_data_out(:,4) / .38 .- rect_data_out(:,4)*9.81 + rect_data_out(:,10);
figure;
%subplot(2,1,1);
plot([cleandata(:,6) ya_est])
%subplot(2,1,2);
%plot(rect_data_out(:,10));
title('Y acceleration residual (m/s)');

%  est_measurement[ MEAS_zr ] = - newState[ STATE_v ] * newState[ STATE_phi ] / RADIUS
%	+ newState[ STATE_zrb ];

figure;
zr_est = -rect_data_out(:,2) .* rect_data_out(:,4) ./ 0.38 .+ rect_data_out(:,13);
plot([cleandata(:,4) zr_est]);
title('Yaw Rate Residual (rad/s)');

figure;
plot( [cleandata(:,11) + round((rect_data_out(:,6)-cleandata(:,11))/(2*pi))*2*pi rect_data_out(:,6)]);
title('Heading');
