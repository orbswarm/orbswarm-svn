#ifndef ADCONVERTER_H

#define ADCONVERTER_H

#define DATA_PAGE 0x80840000
#define CALIB_LOC    2027          //location of calibration values
/* Prototypes */

#define SONAR_CHANNEL 2
#define SONAR_MAX_VOLTAGE 5.0
#define SONAR_SAMPLE_PRECISION 2


static void read_7xxx_adc(int *adc_result, int channel, int numOfSamples);
void read_7xxx_adc(int *adc_result, int channel, int numOfSamples);
double get_ADC_channel(int channel, double maxVoltage, int numOfSamples);
void calibrate_ADC(int **stored_cal);
void startupADC();
void shutdownADC(); 
double _get_sonar(int channel, double maxVoltage, int numOfSamples);
double get_sonar();

#endif
