//#include "EspMQTTClient.h"
#include <esp_now.h>
#include <WiFi.h>
#include <Wire.h>
#include "WiFiClientSecure.h"
#include "Adafruit_MQTT.h"
#include "Adafruit_MQTT_Client.h"
#include <Ticker.h>

#define WLAN_SSID         "GK"
#define WLAN_PASS         "244466666"
#define AIO_SERVER        "io.adafruit.com"
#define AIO_SERVERPORT    1883
#define AIO_USERNAME      "ThanhVinhCE"
#define AIO_KEY           "aio_WMEp70yicqaMwxPnTySIYbnjcgEA"

// WiFiFlientSecure for SSL/TLS support
WiFiClient client;
// Setup the MQTT client class by passing in the WiFi client and MQTT server and login details.
Adafruit_MQTT_Client mqtt(&client, AIO_SERVER, AIO_SERVERPORT, AIO_USERNAME, AIO_KEY);

// Notice MQTT paths for AIO follow the form: <username>/feeds/<feedname>
Adafruit_MQTT_Publish humidityPub = Adafruit_MQTT_Publish(&mqtt, AIO_USERNAME "/feeds/humidity");
Adafruit_MQTT_Publish temperaturePub = Adafruit_MQTT_Publish(&mqtt, AIO_USERNAME "/feeds/temperature");
Adafruit_MQTT_Publish lightPub = Adafruit_MQTT_Publish(&mqtt, AIO_USERNAME "/feeds/light");
Adafruit_MQTT_Publish ledPub = Adafruit_MQTT_Publish(&mqtt, AIO_USERNAME "/feeds/led");

Adafruit_MQTT_Subscribe ledSub = Adafruit_MQTT_Subscribe(&mqtt, AIO_USERNAME "/feeds/led");
Adafruit_MQTT_Subscribe *subscription;

typedef struct struct_data {
  float humidity;
  float temperature;
  int lightLevel;
};

typedef struct struct_state {
  char type;
  uint8_t ledResponse;
};

Ticker timer;

uint8_t nodeAddress[] = {0xE8, 0xDB, 0x84, 0x11, 0xF5, 0xE4};

struct_data nodeData;
struct_state nodeState;
String ledRequest;
int count;

void onDataSent(const uint8_t *mac_addr, esp_now_send_status_t status) {
  Serial.print("\r\nLast Packet Send Status:\t");
  Serial.println(status == ESP_NOW_SEND_SUCCESS ? "Delivery Success" : "Delivery Fail");
}

void onDataRecv(const uint8_t * mac, const uint8_t *incomingData, int len) {
  if ((char)incomingData[0] == '#') {
    memcpy(&nodeState, incomingData, len);
    if (!ledPub.publish(nodeState.ledResponse)) {
      Serial.println("failed to publish led state to feed");
    }
    else {
      Serial.println("published led response successfully");
    }
  }
  else {
    memcpy(&nodeData, incomingData, len);
    Serial.print("Received from node: ");
    Serial.print("Humidity: ");
    Serial.print(nodeData.humidity);
    Serial.print(" Temperature: ");
    Serial.print(nodeData.temperature);
    Serial.print(" Light: ");
    Serial.println(nodeData.lightLevel);
  }
}

void ISR() {
  sendNodeDataMQTT();
}

void sendNodeDataMQTT() {
  count++;
  if (count == 1000) {
    count = 0;
    if (!humidityPub.publish(nodeData.humidity)) {
      Serial.println("failed to publish humidity to feed");
    }
    else {
      Serial.println("published humidity");
    }

    if (!temperaturePub.publish(nodeData.temperature)) {
      Serial.println("failed to publish temperature to feed");
    }
    else {
      Serial.println("published temperature");
    }

    if (!lightPub.publish(nodeData.lightLevel)) {
      Serial.println("failed to publish light level to feed");
    }
    else {
      Serial.println("published light level");
    }
  }
}

void MQTT_connect() {
  int8_t ret;
  // Stop if already connected.
  if (mqtt.connected()) {
    return;
  }
  Serial.println("Connecting to MQTT... ");
  while ((ret = mqtt.connect()) != 0) { // connect will return 0 for connected
    Serial.println(mqtt.connectErrorString(ret));
    Serial.println("Retrying MQTT connection in 5 seconds...");
    mqtt.disconnect();
    delay(5000);
  }
  Serial.println("MQTT Connected!");
}

void checkSubscription() {
  while (subscription = mqtt.readSubscription(2000)) {
    if (subscription == &ledSub) {
      ledRequest = (char *)ledSub.lastread;
      if(ledRequest == "1" || ledRequest == "0"){
        // Send message via ESP-NOW
        esp_err_t result = esp_now_send(nodeAddress, (uint8_t *)&ledRequest, sizeof(ledRequest));
        if (result == ESP_OK) {
          Serial.println("Sent with led request success");
        }
        else {
          Serial.println("Error sending the led request");
        }
      }
    }
  }
}

// main -----------------------------------------------------------------------//
void setup() {
  Serial.begin(115200);

  timer.attach_ms(10, ISR);

  WiFi.mode(WIFI_AP_STA);
  Serial.print("Connecting to ");
  Serial.print(WLAN_SSID);
  WiFi.begin(WLAN_SSID, WLAN_PASS);
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(500);
  }
  Serial.println();
  Serial.println("WiFi connected");

  if (esp_now_init() != ESP_OK) {
    Serial.println("Error initializing ESP-NOW");
    return;
  }
  else {
    Serial.println("init ESP-NOW successfully");
  }

  esp_now_register_send_cb(onDataSent);

  esp_now_peer_info_t nodeInfo;
  memcpy(nodeInfo.peer_addr, nodeAddress, 6);
  nodeInfo.channel = 0;
  nodeInfo.encrypt = false;

  if (esp_now_add_peer(&nodeInfo) != ESP_OK) {
    Serial.println("There was an error registering the gateway");
    return;
  }
  else {
    Serial.println("registering gateway successfully");
  }

  esp_now_register_recv_cb(onDataRecv);

  mqtt.subscribe(&ledSub);
}

void loop() {
  MQTT_connect();
  checkSubscription();
}
// end main -------------------------------------------------------------------//
