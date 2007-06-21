#ifndef GLOBAL_H
#define GLOBAL_H

// CPU clock speed
//#define F_CPU        20000000               		// 20MHz processor
//#define F_CPU        16000000               		// 16MHz processor
//#define F_CPU        14745000               		// 14.745MHz processor
//#define F_CPU        8000000               		// 8MHz processor
#define F_CPU        7372800               		// 7.37MHz processor
//#define F_CPU        4000000               		// 4MHz processor
//#define F_CPU        3686400               		// 3.69MHz processor


// CYCLES_PER_US is used by some short delay loops
#define CYCLES_PER_US ((F_CPU+500000)/1000000) 	// cpu cycles per microsecond
#endif
