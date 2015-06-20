#ifndef data_processor_c
#define data_processor_c

#include <stdio.h>
#include <math.h>
#include <time.h>
#include "sqrt.h"

// ---------------- PUBLIC PART --------------

typedef struct {
    double x;
    double y;
    double z;
} AccelerometerData;


//init handler with a bunch of options
//if sample rate is 60, then update interval for accelerometer should be 1.0/60
void initialize_accelerometer_data_handler(double _sampleRate, double _cutoffFrequency);

//add another bunch of data from accelerometer
//it will get filtrated in the real time
void add_data(AccelerometerData accelerometerData);

//get last filtered values
AccelerometerData get_data();

//this callback is called when a user started jerking off
void set_jerkingoff_started_callback(void (*callback)(void));


// ------------------- BODY -------------------
//this means that update interval for accelerometer should be 1.0/60
double sampleRate = 60;
double cutoffFrequency = 5.0;
double filterConstant;

AccelerometerData currentData;
AccelerometerData lastFilteredData;
AccelerometerData lastData;


int frameIncriment;
int numberOfPeaks = 0;
double currentDirection = 0;
double previousDirection = 0;

static void handleAccelerationChange();
//a callback for jerking off started case
static void (*jerkingoffCallback)(void);

void setSampleRate(double newSampleRate) {
  sampleRate = newSampleRate;
}

void setCutoffFerquency(double newCutoffFrequency) {
  cutoffFrequency = newCutoffFrequency;
}

void updateFilterConstant() {
  double dt = 1.0 / sampleRate;
  double RC = 1.0 / cutoffFrequency;
  filterConstant = RC / (dt + RC);
}

void initialize_accelerometer_data_handler(double _sampleRate, double _cutoffFrequency) {
  setSampleRate(_sampleRate);
  setCutoffFerquency(_cutoffFrequency);
  updateFilterConstant();
}

void add_data(AccelerometerData accelerometerData) {
  double alpha = filterConstant;
  
  lastFilteredData = currentData;
  
  currentData = (AccelerometerData) {
    .x = alpha * (currentData.x + accelerometerData.x - lastData.x),
    .y = alpha * (currentData.y + accelerometerData.y - lastData.y),
    .z = alpha * (currentData.z + accelerometerData.z - lastData.z)
  };
  
  lastData = accelerometerData;
  
  handleAccelerationChange();
}

AccelerometerData get_data() {
  return currentData;
}

void set_jerkingoff_started_callback(void (*callback)(void)) {
  jerkingoffCallback = callback;
}

void resetCounter() {
  frameIncriment = 0;
}

double NormData(AccelerometerData data) {
  return my_sqrt(data.x * data.x + data.y * data.y + data.z * data.z);
}

//check if we got another peak value and if we did then coun the peak
void handleAccelerationChange() {
  currentDirection = NormData(currentData) - NormData(lastFilteredData);
  
  if (((previousDirection < 0 && currentDirection > 0) || (previousDirection > 0 && currentDirection < 0)) &&
      fabs(currentDirection) > 0.2f) {
    //the direction has changed
    
    resetCounter();
    numberOfPeaks++;
    
    if (numberOfPeaks % 2 == 0 && numberOfPeaks > 2) {
      jerkingoffCallback();
    }
  }
  
  previousDirection = currentDirection;
}


#endif /* data_processor_c */
