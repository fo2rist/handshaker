#include <pebble_worker.h>

static DataLoggingSessionRef logging_session;

static void data_handler(AccelData *data, uint32_t num_samples) {
  // Construct a data packet
  AppWorkerMessage msg_data = {
    .data0 = data->x,
    .data1 = data->y,
    .data2 = data->z
  };
  
  // Send the data to the foreground app
  app_worker_send_message(0, &msg_data);
  //Send data to phone
  // Fake creating some data and logging it to the session.
  uint16_t log_data[] = { data->x,
    data->y,
    data->z};
  data_logging_log(logging_session, &log_data, 3);
}

static void worker_init() {
  //Enable Pebble-Phone logging
  logging_session = data_logging_create(0x1234, DATA_LOGGING_UINT, 2, true);
  
  // Subscribe to the accelerometer data service
  int num_samples = 1;
  accel_data_service_subscribe(num_samples, data_handler);
  
  // Choose update rate
  accel_service_set_sampling_rate(ACCEL_SAMPLING_10HZ);
}

static void worker_deinit() {
  // When we don't need to log anything else, we can close off the session.
  data_logging_finish(logging_session);
  // unsubscribe from accel events
  accel_data_service_unsubscribe();
}

int main(void) {
  worker_init();
  worker_event_loop();
  worker_deinit();
}
