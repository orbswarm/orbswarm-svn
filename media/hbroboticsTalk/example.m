%
% Kalman Filter Example
%
% For presentation to the Homebrew Robotics Club, http://hbrobotics.org
% May 28, 2008
%
% by Michael Prados, mpradosNOSPAM@gmail.com
% The SWARM Project, http://orbswarm.com
%
% Typical Output-
%
%octave.exe:26> example
%A =
%
%   0   0
%   1   0
%
%B =
%
%   1
%   0
%
%C =
%
%   1.00000   0.00000
%   0.00000   9.81000
%
%D =
%
%   0
%   0
%
%Ad =
%
%   1.00000   0.00000
%   0.10000   1.00000
%
%Bd =
%
%   0.1000000
%   0.0050000
%
%A_obs =
%
%   0.0843433  -0.0516992
%   0.0031643   0.6433115
%
%B_obs =
%
%   0.9156567   0.0052701
%   0.0968357   0.0363597
%
%acc_rms_error =  0.096007
%kalman_rms_error =  0.040700
%

close all;

%
% sample rate T
%
T = 0.1; % seconds


%
% Define our inverted "pendulum" system.
% System has inertia, but no gravity acts on the mass.
% Two sensors- rate gyro, and accelerometer at Center of Mass
% Gravity acts on the accelerometer to provide a tilt angle measurement
%
Im = 1; % kg * m^2
g = 9.81; % m/s^2

A = [0 0;1 0]
B = [1/Im;0]
C = [1 0;0 g]
D = [0;0]

continuous_sys = ss(A,B,C,D,0);

%
% Alternative Manual Discretization
% Ad = expm(A*T);
% Bd = (eye(2)*T + 0.5 * A * T^2) * B; % a typical approximation, in this case also precise
% Cd = C;
% Cd = D;
%


%
% Octave derives the discrete system for us, using c2d.
%
discrete_sys = c2d(continuous_sys, T);

Ad = discrete_sys.a
Bd = discrete_sys.b
Cd = discrete_sys.c;


G = eye(2);

%
% Set expected value for system noise
%
% The upper left value is magnitude of u,
% since we are modeling actuator input as random noise.
% The lower right value is essentially zero, but the dkalman
% function requires a positive definite QW.
%
QW = [1 0;0 0.001];

%
% Set the expected value for sensor noise.
% Here, we have 0.1 rad/s of noise on the rate gyro,
% and 100 mG of noise on the accelerometer.
%
RV = [.1 0;0 .1*g];

%
% dkalman finds the optimal observer L for us.
%
L = dkalman (Ad, G, Cd, QW, RV);

%
% Synthesize u 
%
t = 0:.1:20;
square_wave_frequency = .2 * 2 * pi; 
u = sign(sin( (square_wave_frequency * t + .25 * 2 * pi) )); % make square wave

%
% Simulate the "real" system
%
[y,x_sim] = lsim(discrete_sys, u', t, [-0.05;-0.78]);


%
% Simulate the estimated system
%

y = y + [randn(size(t')) * .1 randn(size(t')) * .1 * g]';

A_obs = Ad - L* Cd
B_obs = L
C_obs = eye(2);
D_obs = zeros(2,2);

obs_sys = ss(A_obs,B_obs,C_obs,D_obs,T);

x_est = lsim(obs_sys, y', t, [0;0]);


%
% Compare RMS error for scaled accelerometer vs kalman estimate
% Kalman filter initializes to zero values for both state estimates,
% so we start at row 50 here for a more fair comparison.
%
acc_rms_error = sqrt(mean((y(2,50:201)'/9.81-x_sim(2,50:201)').^2))
kalman_rms_error = sqrt(mean((x_est(2,50:201)'-x_sim(2,50:201)').^2))

plot(t,[y(2,:)'/9.81 x_est(2,:)' x_sim(2,:)']);
grid;
title('Accelerometer Result, Kalman Estimate, and State Value for Theta');

figure;
plot(t,[y(2,:)'/9.81-x_sim(2,:)' x_est(2,:)'-x_sim(2,:)']);
grid;
title('Error in Accelerometer vs. Kalman Estimate for Theta');

%
% Error in small angle approximation
%
sa_theta = linspace(-90,90,100);
sa_sine  = sin( sa_theta * pi / 180 );
figure;
plot(sa_theta, [sa_sine' sa_theta'*pi/180]);
grid;
title('Small Angle Approximation - sin \theta vs \theta');
xlabel('Degrees');

