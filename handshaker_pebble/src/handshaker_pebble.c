#include <pebble.h>

static Window *window;
static TextLayer *text_layer;

// --------------------- BACKGROUND WORKER COMMUNICATION ---------
static void worker_message_handler(uint16_t type, AppWorkerMessage *data) {
  if (type == 0) {
    // Read ticks from worker's packet
    AccelData accel_data = {
      .x = data->data0,
      .y = data->data1,
      .z = data->data2,
      .did_vibrate = false
    };
    
    // Long lived buffer
    static char s_buffer[128];
    
    // Compose string of all data
    snprintf(s_buffer, sizeof(s_buffer),
             "Accel X,Y,Z\n %d,%d,%d\n",
             accel_data.x, accel_data.y, accel_data.z
             );
    
    //Show the data
    text_layer_set_text(text_layer, s_buffer);
    APP_LOG(APP_LOG_LEVEL_DEBUG, "%d,%d,%d", accel_data.x, accel_data.y, accel_data.z);
  }
}

// --------------------- CLICK LISTENERS CONFIG ------------------

static void select_click_handler(ClickRecognizerRef recognizer, void *context) {
  AppWorkerResult result = app_worker_launch();
  switch (result) {
    case APP_WORKER_RESULT_SUCCESS:
      text_layer_set_text(text_layer, "Runned");
      break;

    case APP_WORKER_RESULT_ALREADY_RUNNING:
      text_layer_set_text(text_layer, "Already running.\nLet's stop.");
      app_worker_kill();
      break;

    case APP_WORKER_RESULT_ASKING_CONFIRMATION:
      //no special handling now
    default:
      text_layer_set_text(text_layer, "Failed");
      break;
  }
}

static void up_click_handler(ClickRecognizerRef recognizer, void *context) {
  text_layer_set_text(text_layer, "Up");
}

static void down_click_handler(ClickRecognizerRef recognizer, void *context) {
  text_layer_set_text(text_layer, "Down");
}

static void click_config_provider(void *context) {
  window_single_click_subscribe(BUTTON_ID_SELECT, select_click_handler);
  window_single_click_subscribe(BUTTON_ID_UP, up_click_handler);
  window_single_click_subscribe(BUTTON_ID_DOWN, down_click_handler);
}

// ----------------------- INIT PART ------------------------------

static void window_load(Window *window) {
  Layer *window_layer = window_get_root_layer(window);
  GRect bounds = layer_get_bounds(window_layer);

  text_layer = text_layer_create((GRect) { .origin = { 0, 72 }, .size = { bounds.size.w, 40 } });
  text_layer_set_text(text_layer, "Press a button");
  text_layer_set_text_alignment(text_layer, GTextAlignmentCenter);
  layer_add_child(window_layer, text_layer_get_layer(text_layer));
}

static void window_unload(Window *window) {
  text_layer_destroy(text_layer);
}

static void init(void) {
  //Show the window
  window = window_create();
  window_set_click_config_provider(window, click_config_provider);
  window_set_window_handlers(window, (WindowHandlers) {
    .load = window_load,
    .unload = window_unload,
  });
  const bool animated = true;
  window_stack_push(window, animated);
  
  //Subscribe to worker's messages
  app_worker_message_subscribe(worker_message_handler);
}

static void deinit(void) {
  window_destroy(window);
  // No more worker updates
  app_worker_message_unsubscribe();
}

int main(void) {
  init();

  APP_LOG(APP_LOG_LEVEL_DEBUG, "Done initializing, pushed window: %p", window);

  app_event_loop();
  deinit();
}
