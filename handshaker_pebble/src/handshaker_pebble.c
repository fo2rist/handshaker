#include <pebble.h>
#include <time.h>
#include "../constants.h"

static Window *window;
static TextLayer *text_layer;
static GBitmap *s_bitmap;
static BitmapLayer *s_bitmap_layer;

static int last_start_time;
static int last_stop_time;

static const int16_t KEY_TYPE = 0;
static const int16_t KEY_ACCEL_X = 1;
static const int16_t KEY_ACCEL_Y = 2;
static const int16_t KEY_ACCEL_Z = 3;
static const int16_t KEY_DURATION = 4;

static const int16_t TYPE_START = 0;
static const int16_t TYPE_END = 1;
static const int16_t TYPE_PROGRESS = 2;



// --------------------- APP COMMUNICATION -----------------------
static void send_accel_data(int16_t x, int16_t y, int16_t z) {
  //Disabled because of performance issues
//  DictionaryIterator *iter;
//  app_message_outbox_begin(&iter);
//  dict_write_int(iter, KEY_TYPE, &TYPE_PROGRESS, sizeof(uint16_t), true /*signed*/);
//  dict_write_int(iter, KEY_ACCEL_X, &x, sizeof(int16_t), true /*signed*/);
//  dict_write_int(iter, KEY_ACCEL_Y, &y, sizeof(int16_t), true /*signed*/);
//  dict_write_int(iter, KEY_ACCEL_Z, &z, sizeof(int16_t), true /*signed*/);
//  app_message_outbox_send();
}

static void send_start_notification() {
  DictionaryIterator *iter;
  app_message_outbox_begin(&iter);
  dict_write_int(iter, KEY_TYPE, &TYPE_START, sizeof(uint16_t), true /*signed*/);
  app_message_outbox_send();
}

static void send_stop_notification(int duration) {
  DictionaryIterator *iter;
  app_message_outbox_begin(&iter);
  dict_write_int(iter, KEY_TYPE, &TYPE_END, sizeof(uint16_t), true /*signed*/);
  dict_write_int(iter, KEY_DURATION, &duration, sizeof(int), true /*signed*/);
  app_message_outbox_send();
}

// --------------------- BACKGROUND WORKER COMMUNICATION ---------
static void worker_message_handler(uint16_t type, AppWorkerMessage *data) {
  // Long lived buffer
  static char s_buffer[128];

  switch ((BackgroundMessageType)type) {

    case BackgroundMessageJerkProgress: {
      // Compose string of all data
      AccelData accel_data = {
        .x = data->data0,
        .y = data->data1,
        .z = data->data2,
        .did_vibrate = false
      };
      snprintf(s_buffer, sizeof(s_buffer),
               "Go GO GOOOO!"
               );
      layer_set_hidden(bitmap_layer_get_layer(s_bitmap_layer), !layer_get_hidden(bitmap_layer_get_layer(s_bitmap_layer))); //Add some blinking
    
      // Notify phone
      send_accel_data(accel_data.x, accel_data.y, accel_data.z);

      APP_LOG(APP_LOG_LEVEL_DEBUG, "%d,%d,%d", accel_data.x, accel_data.y, accel_data.z);
      break;
    }
    case BackgroundMessageJerkStarted: {
      // Compose string of all data
      last_start_time = data->data0;
      snprintf(s_buffer, sizeof(s_buffer),
              "Accel Exceeded"
              );
      
      // Notify phone
      send_start_notification();

      APP_LOG(APP_LOG_LEVEL_DEBUG, "Start");
      break;
    }
    case BackgroundMessageJerkStopped: {
      // Compose string of all data
      last_stop_time = data->data0;
      snprintf(s_buffer, sizeof(s_buffer),
               "Did it in %d sec",
               last_stop_time - last_start_time
               );
      layer_set_hidden(bitmap_layer_get_layer(s_bitmap_layer), false); // Stop blinking
      
      // Notify phone
      send_stop_notification(last_stop_time - last_start_time);

      APP_LOG(APP_LOG_LEVEL_DEBUG, "Stop. Duration %d", last_stop_time - last_start_time);
      break;
    }
  };
  
  
  //Show the data
  text_layer_set_text(text_layer, s_buffer);
}

// --------------------- CLICK LISTENERS CONFIG ------------------

static void select_click_handler(ClickRecognizerRef recognizer, void *context) {
  AppWorkerResult result = app_worker_launch();
  switch (result) {
    case APP_WORKER_RESULT_SUCCESS:
      text_layer_set_text(text_layer, "Running");
      break;

    case APP_WORKER_RESULT_ALREADY_RUNNING:
      text_layer_set_text(text_layer, "Stopped");
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

}

static void down_click_handler(ClickRecognizerRef recognizer, void *context) {

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

  // Init layers
  s_bitmap = gbitmap_create_with_resource(RESOURCE_ID_LOGO);

  s_bitmap_layer = bitmap_layer_create(bounds);
  bitmap_layer_set_bitmap(s_bitmap_layer, s_bitmap);
#ifdef PBL_PLATFORM_APLITE
  bitmap_layer_set_compositing_mode(s_bitmap_layer, GCompOpAssign);
#elif PBL_PLATFORM_BASALT
  bitmap_layer_set_compositing_mode(s_bitmap_layer, GCompOpSet);
#endif
  layer_add_child(window_layer, bitmap_layer_get_layer(s_bitmap_layer));

  text_layer = text_layer_create((GRect) { .origin = { 0, 128 }, .size = { bounds.size.w, 20 } });
  text_layer_set_text_alignment(text_layer, GTextAlignmentCenter);
  layer_add_child(window_layer, text_layer_get_layer(text_layer));


  // Populate UI
  // Check to see if the worker is currently active
  bool running = app_worker_is_running();
  if (running) {
    text_layer_set_text(text_layer, "App is running");
  } else {
    text_layer_set_text(text_layer, "Press to launch");
  }
}

static void window_unload(Window *window) {
  text_layer_destroy(text_layer);
  gbitmap_destroy(s_bitmap);
}

static void init(void) {
  // Show the window
  window = window_create();
  window_set_click_config_provider(window, click_config_provider);
  window_set_window_handlers(window, (WindowHandlers) {
    .load = window_load,
    .unload = window_unload,
  });
  const bool animated = true;
  window_stack_push(window, animated);
  
  // Subscribe to worker's messages
  app_worker_message_subscribe(worker_message_handler);
  
  // Prepare to messaging communication
  // Open AppMessage
  app_message_open(app_message_inbox_size_maximum(), app_message_outbox_size_maximum());
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
