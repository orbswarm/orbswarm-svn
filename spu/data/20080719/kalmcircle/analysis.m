%
% Data Analysis for rolling test
% 6/02/08
%
% 1.seconds, 2.milliseconds, 3.ratex, 4.ratey, 5.accx, 6.accy, 7.accz, 8.northing, 9.easting, 10.utmzone
%

load -ascii cleandata;

t = cleandata(:,1)+cleandata(:,2)/1000-cleandata(1,1);

for idx = 2:max(size(cleandata))
	if (abs(cleandata(idx,8)-cleandata(1,8)) > 1000)
		cleandata(idx,8) = cleandata((idx-1),8);
	endif
	if (abs(cleandata(idx,9)-cleandata(1,9)) > 1000)
		cleandata(idx,9) = cleandata((idx-1),9);
	endif
endfor

for idx = 1:max(size(cleandata))
	if (cleandata(idx,11) < -pi)
		cleandata(idx,11) += 2*pi; 
	endif
endfor


rolling = cleandata(300:1200,:);
still = cleandata(1500:2000,:);

bias = mean(still)

still(:,1) = still(:,1)+still(:,2)/1000 - still(1,1);

rolling(:,1) = rolling(:,1)+rolling(:,2)/1000 - rolling(1,1);

rolling(:,3:10) = rolling(:,3:10) - ones(max(size(rolling)),1)*bias(1,3:10);

still(:,3:9) = still(:,3:9) - ones(max(size(still)),1)*bias(1,3:9);

plot(rolling(:,1), [rolling(:,8) rolling(:,9)]);

sqrt(mean(still(:,3:9).^2))

my_data = [-rolling(:,6) rolling(:,7) -9.81-rolling(:,5) -rolling(:,4) -rolling(:,3) rolling(:,9) rolling(:,8) rolling(:,11) rolling(:,12) rolling(:,13)];

save -text test_data my_data

my_data = [-cleandata(:,6) cleandata(:,7) cleandata(:,5) -cleandata(:,4) -cleandata(:,3) cleandata(:,9)-cleandata(1,9) cleandata(:,8)-cleandata(1,8) cleandata(:,11) cleandata(:,12) cleandata(:,13)];

save -text full_data my_data
