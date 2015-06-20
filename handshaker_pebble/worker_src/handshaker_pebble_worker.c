#include <pebble_worker.h>


static DataLoggingSessionRef logging_session;

static const int32_t G = 1000;
static const int32_t THRESHOLD = 150000;
static const int16_t SAMPLES_BUFFER_SIZE = 30;
static int last_samples_ampl_buffer[30] = {};
static int16_t last_sample_position = 0;
static bool has_enough_data = false;
static bool is_threshold_exceded = false;


static bool accumulate_data(AccelData *accel_data) {
  int acceleration_sq = abs((int)accel_data->x * accel_data->x + (int)accel_data->y * accel_data->y + (int)accel_data->z * accel_data->z - 1000000);
  
  last_samples_ampl_buffer[last_sample_position] = acceleration_sq;
  if (last_sample_position == SAMPLES_BUFFER_SIZE-1) {
    has_enough_data = true;
  }
  if (has_enough_data) {
    int total = 0;
    for (int i=0; i<SAMPLES_BUFFER_SIZE; i++) {
      total += last_samples_ampl_buffer[i];
    }
    total = total /SAMPLES_BUFFER_SIZE;
    
    if (total > THRESHOLD && !is_threshold_exceded) {
      is_threshold_exceded = true;
      
      // Wake the app
      worker_launch_app();
      
      // Send the data to the foreground app
      AppWorkerMessage msg_data;
      app_worker_send_message(1, &msg_data);
      
//      //Enable Pebble-Phone logging
//      logging_session = data_logging_create(1234, DATA_LOGGING_BYTE_ARRAY, 6, false);
    }
    if (total <= THRESHOLD && is_threshold_exceded) {
      is_threshold_exceded = false;
      
      // Send the data to the foreground app
      AppWorkerMessage msg_data;
      app_worker_send_message(2, &msg_data);
      
//      // When we don't need to log anything else, we can close off the session.
//      data_logging_finish(logging_session);
    }
  }
  
  last_sample_position = (last_sample_position + 1) % SAMPLES_BUFFER_SIZE;
  
  return is_threshold_exceded;
}

static void data_handler(AccelData *data, uint32_t num_samples) {
  //Accumulate statistics
  accumulate_data(data);
  
  if (is_threshold_exceded) {
    // Construct a data packet
    AppWorkerMessage msg_data = {
      .data0 = data->x,
      .data1 = data->y,
      .data2 = data->z
    };
    
    // Send the data to the foreground app
    app_worker_send_message(0, &msg_data);
  
//    //Send data to phone
//    uint16_t log_data[] = { data->x,
//      data->y,
//      data->z};
//  
//    DataLoggingResult result = data_logging_log(logging_session, &log_data, 1);
//    switch (result) {
//      case 0: //DATA_LOG_SUCCESS
//        ;
//        break;
//    
//      default:
//      //  DATA_LOG_BUSY, //! Someone else is writing to this log
//      //  DATA_LOG_FULL, //! No more space to save data
//      //  DATA_LOG_NOT_FOUND, //! The log does not exist
//      //  DATA_LOG_CLOSED //! The log was made inactive
//      //  DATA_LOG_INVALID_PARAMS
//        break;
//    }
  }
}

static void worker_init() {
  // Subscribe to the accelerometer data service
  int num_samples = 1;
  accel_data_service_subscribe(num_samples, data_handler);
  
  // Choose update rate
  accel_service_set_sampling_rate(ACCEL_SAMPLING_10HZ);
}

static void worker_deinit() {
  // unsubscribe from accel events
  accel_data_service_unsubscribe();
}

int main(void) {
  worker_init();
  worker_event_loop();
  worker_deinit();
}
