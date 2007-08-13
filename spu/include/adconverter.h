#ifndef ADCONVERTER_H

#define ADCONVERTER_H

#include "../include/swarmdefines.h"

#define DATA_PAGE 0x80840000
#define CALIB_LOC    2027          //location of calibration values
/* Prototypes */


void read_7xxx_adc(int *adc_result, int channel, int numOfSamples);
double get_ADC_channel(int channel, double maxVoltage, int numOfSamples);
void calibrate_ADC(int **stored_cal);
void startupADC();
void shutdownADC(); 
double _get_sonar(int channel, double maxVoltage, int numOfSamples);
double get_sonar();
void getAdConverterStatus(spuADConverterStatus *adConverterStatus, double maxVoltage, int precision);
#endif
