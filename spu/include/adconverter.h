#ifndef ADCONVERTER_H

#define ADCONVERTER_H

#define DATA_PAGE 0x80840000
#define CALIB_LOC    2027          //location of calibration values
/* Prototypes */

static void read_7xxx_adc(int *adc_result, int channel, int numOfSamples);
double get_ADC_channel(int channel, double maxVoltage, int numOfSamples);
void calibrate_ADC(int **stored_cal);
int startupADC();

#endif
