%
% smoothing function
%

function y = smoother(x, smoothness)

y(1)=x(1);

for idx = 2:max(size(x))
	y(idx) = (1-1/smoothness) * y(idx-1) + 1/smoothness * x(idx);
endfor

endfunction
