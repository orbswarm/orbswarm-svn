% generate_data.m

my_data = randn(1000,10)/10 .+ [zeros(1000,7) ones(1000,1) zeros(1000,2)] * .1;

save -text test_data my_data
