#include "DHT.h"
#include <esp_now.h>
#include <WiFi.h>
#include <Wire.h>
#include <Ticker.h>
#include <esp_wifi.h>

#define DHT_PIN 17
#define DHT_TYPE DHT11
#define LIGHT_SENSOR_PIN 36
#define LED_PIN 22

int count;
Ticker timer;
DHT dht(DHT_PIN, DHT_TYPE);

typedef struct struct_data {
  float humidity;
  float temperature;
  int lightLevel;
};

typedef struct struct_state {
  char type = '#';
  uint8_t ledResponse;
};

uint8_t gatewayAddress[] = {0x30, 0xAE, 0xA4, 0x98, 0x01, 0xF4};

// Insert your SSID
constexpr char WIFI_SSID[] = "GK";

struct_data myData;
String ledRequest;
struct_state myState;

void onDataSent(const uint8_t *mac_addr, esp_now_send_status_t status) {
  Serial.print("\r\nLast Packet Send Status:\t");
  Serial.println(status == ESP_NOW_SEND_SUCCESS ? "Delivery Success" : "Delivery Fail");
}

void onDataRecv(const uint8_t * mac, const uint8_t *incomingData, int len) {
  memcpy(&ledRequest, incomingData, len);
  if (ledRequest == "1") {
    digitalWrite(LED_PIN, HIGH);
    myState.ledResponse = 3;
  }
  else if (ledRequest == "0") {
    digitalWrite(LED_PIN, LOW);
    myState.ledResponse = 2;
  }
  // Send message via ESP-NOW
  esp_err_t result = esp_now_send(gatewayAddress, (uint8_t *) &myState, sizeof(myState));
  if (result == ESP_OK) {
    Serial.println("Sent led state with success");
  }
  else {
    Serial.println("Error sending the led state");
  }
}

int32_t getWiFiChannel(const char *ssid) {
  if (int32_t n = WiFi.scanNetworks()) {
      for (uint8_t i=0; i<n; i++) {
          if (!strcmp(ssid, WiFi.SSID(i).c_str())) {
              return WiFi.channel(i);
          }
      }
  }
  return 0;
}

void ISR() {
  sendMyData();
}

void sendMyData() {
  count++;
  if (count == 200) {
    count = 0;
    myData.humidity = dht.readHumidity();
    myData.temperature = dht.readTemperature();
    myData.lightLevel = analogRead(LIGHT_SENSOR_PIN);

    // Check if any reads failed and exit early (to try again).
    if (isnan(myData.humidity) || isnan(myData.temperature)) {
      Serial.println(F("Failed to read from DHT sensor!"));
      return;
    }
    
    Serial.print("Humidity: ");
    Serial.print(myData.humidity);
    Serial.print(" Temperature: ");
    Serial.print(myData.temperature);
    Serial.print(" Light: ");
    Serial.println(myData.lightLevel);
    
    // Send message via ESP-NOW
    esp_err_t result = esp_now_send(gatewayAddress, (uint8_t *) &myData, sizeof(myData));
    if (result == ESP_OK) {
      Serial.println("Sent with success");
    }
    else {
      Serial.println("Error sending the data");
    }
  }
}

// main -----------------------------------------------------------------------//
void setup() {
  Serial.begin(115200);
  WiFi.mode(WIFI_AP_STA);

  int32_t channel = getWiFiChannel(WIFI_SSID);
  esp_wifi_set_channel(channel, WIFI_SECOND_CHAN_NONE);
  
  if (esp_now_init() != ESP_OK) {
    Serial.println("Error initializing ESP-NOW");
    return;
  }
  else {
    Serial.println("init ESP-NOW successfully");
  }
  
  esp_now_register_send_cb(onDataSent);

  esp_now_peer_info_t gatewayInfo;
  memcpy(gatewayInfo.peer_addr, gatewayAddress, 6);
  gatewayInfo.channel = 0;
  gatewayInfo.encrypt = false;

  if(esp_now_add_peer(&gatewayInfo) != ESP_OK) {
    Serial.println("There was an error registering the gateway");
    return;
  }
  else {
    Serial.println("registering gateway successfully");
  }
  
  esp_now_register_recv_cb(onDataRecv);

  timer.attach_ms(10, ISR);
  dht.begin();
  pinMode(LED_PIN, OUTPUT);
}

void loop() {

}
// end main -------------------------------------------------------------------//
