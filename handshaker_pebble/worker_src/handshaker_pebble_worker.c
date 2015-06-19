#include <pebble_worker.h>


static void data_handler(AccelData *data, uint32_t num_samples) {
  // Construct a data packet
  AppWorkerMessage msg_data = {
    .data0 = data->x,
    .data1 = data->y,
    .data2 = data->z
  };
  
  // Send the data to the foreground app
  app_worker_send_message(0, &msg_data);
}

static void worker_init() {
  // Subscribe to the accelerometer data service
  int num_samples = 1;
  accel_data_service_subscribe(num_samples, data_handler);
  
  // Choose update rate
  accel_service_set_sampling_rate(ACCEL_SAMPLING_10HZ);
}

static void worker_deinit() {
  accel_data_service_unsubscribe();
}

int main(void) {
  worker_init();
  worker_event_loop();
  worker_deinit();
}
