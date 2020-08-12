#include <ESP8266WiFi.h>
#include <WiFiClient.h>
#include <ESP8266WebServer.h>
#include <EEPROM.h>
#include <ArduinoJson.h>
#include <NTPClient.h>
#include <WiFiUdp.h>
#include <SPI.h>
#include <Wire.h>


#include "tetris.h"
#include "secrets.h"

#define SCREEN_WIDTH 128 
#define SCREEN_HEIGHT 64
#define OLED_RESET     -1

#define ALARM_PIN 12

ESP8266WebServer server(80); //Server on port 80
WiFiUDP ntpUDP;
NTPClient timeClient(ntpUDP, "asia.pool.ntp.org", 3600 * 3, 60000);

char daysOfTheWeek[7][12] = { "Sunday","Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
int days[7];
int alarm;
int counter = 0;
bool reading_light = false;
int RGB[4] = {0,0,0,0};
unsigned long previousMillis = 0;
int day_prev = -1;
bool alarm_ran = false;

// ESP8266 Network Variables
IPAddress staticIP(192, 168, 2, 7); 
IPAddress gateway(192, 168, 2, 1);   
IPAddress subnet(255, 255, 255, 0);
IPAddress dns(192, 168, 2, 3);
const char* deviceName = "yeetclock.xyz";


void setup() {

    // Set Pins
    pinMode(14,OUTPUT);
    pinMode(13,OUTPUT);
    digitalWrite(13,LOW);
    pinMode(15, OUTPUT);
   
    pinMode(0,OUTPUT);
    pinMode(2,OUTPUT);
    pinMode(ALARM_PIN, OUTPUT);
    
    digitalWrite(ALARM_PIN,LOW);
    // Start WiFi
    WiFi.hostname(deviceName);
    WiFi.config(staticIP, subnet, gateway, dns);
    WiFi.begin(ssid, password);

    Serial.begin(9600); // Start serial connection (Baudrate 9600)

    while (WiFi.status() != WL_CONNECTED) { // Halt the program until WiFi connects
      delay(500);
      Serial.println("Waiting to connect…");
    }

    Serial.print("IP address: "); // Print IP
    Serial.println(WiFi.localIP());

    // Server settings
    server.on("/setcolor", setColor);
    server.on("/getcolor", getColor);
    server.on("/setalarm", setAlarm);
    server.on("/getalarm", getAlarm);
    server.on("/getreading", getReadingLight);
    server.on("/togglereading", toggleReadingLight);
    server.on("/geteeprom", geteeprom);
    server.on("/tone", playTone);
    server.begin(); // Server begin

    timeClient.begin(); // Start UDP time client

    EEPROM.begin(16); // Start EEPROM with 16 bytes of space

    alarm = read_alarm_time(); // Save alarm days & time to memory from EEPROM
    read_alarm_date(days); 

    Serial.println("test"); // ESP8266 gives errors without this line idk why
    while(!timeClient.update()){
      Serial.println("Receiving NTP time for the first time");
    }
    Serial.println(timeClient.getFormattedTime());
    Serial.println("test2"); // Again, gives errors idk why

}

int bits_to_int(int bits[64]){ // Converts a binary integer to decimal int (Used to store the days that alarm is enabled)
  int num;
  for(int i=0;i<64;i++){
    num += pow(2,i)*bits[i];
  }
  return num;
}

int read_alarm_time(){ // Reads alarm time from memory (ESP8266 gives errors without the debug lines)
  int h = EEPROM.read(0); // Get h,m,s from eeprom
  int m = EEPROM.read(5);
  int s = EEPROM.read(7);
  int alarm = 0;
  Serial.println("\naaa");
  Serial.println(h);
  Serial.println("bbb");
  Serial.println(m);
  Serial.println("ccc");
  Serial.println(s);
  alarm += h*3600;
  alarm += m*60;
  alarm += s;
  return alarm;
}

void read_alarm_date(int days[]){ 
  int day_int;
  day_int = EEPROM.read(10);
  decimal_to_binary(day_int,days);
}

void decimal_to_binary(int decimal, int binary[]){
  int result = decimal;
  Serial.print("Date decimal: ");
  Serial.println(decimal);
  int binary_length = 0;
  for(int i=0; result>0; i++)    {   
    binary_length += 1; 
    binary[i]=result%2;    
    result= result/2;  
  }   
  while(binary_length<7){
    binary[binary_length] = 0;
    binary_length += 1;
  }
}

void playTone(){
  digitalWrite(ALARM_PIN, HIGH);
  double duration;
  double freq;
  for (int i = 0; i < server.args(); i++) {
    if(server.argName(i) == "duration"){
      duration=server.arg(i).toDouble();
    }
    if(server.argName(i) == "freq"){
      freq=server.arg(i).toDouble();
    }
  }
  tone(15,freq,duration*1000);
  server.send(200, "text/plain", "OK");
  digitalWrite(ALARM_PIN, LOW);
}


void setColor(){ 
  bool ON;
  for (int i = 0; i < server.args(); i++) {
    switch(server.argName(i).charAt(0)){
      case 'R':
        analogWrite(0,server.arg(i).toInt());
        RGB[0] = server.arg(i).toInt();
      case 'G':
        analogWrite(2,server.arg(i).toInt());
        RGB[1] = server.arg(i).toInt();
      case 'B':
        analogWrite(14,server.arg(i).toInt());
        RGB[2] = server.arg(i).toInt();
      case 'O':
        ON = server.arg(i).toInt();
        RGB[3] = server.arg(i).toInt();
    }
  } 
  if(!ON){
    Serial.println("OFF");
   analogWrite(14,0);
   analogWrite(0,0);
   analogWrite(2,0);
  } 
  server.send(200, "text/plain", "OK");       //Response to the HTTP request
}

void print_arr(int arr[],int size, String name){
  Serial.println("SHOWING ARRAY " + name);
  for(int i=0;i<size;i++){
    Serial.println(arr[i]);
  }
  Serial.println("DONE");
}

void setAlarm(){
  for (int i = 0; i < server.args(); i++) {
      if(server.argName(i)=="time"){
        alarm = server.arg(i).toInt();
        int h = floor(alarm/3600);
        int m = floor((alarm%3600)/60);
        int s = alarm%60;
        EEPROM.put(0,h);
        EEPROM.commit();
        EEPROM.put(5,m);
        EEPROM.commit();
        EEPROM.put(7,s);
        EEPROM.commit();
      }
      if(server.argName(i)=="days"){
        EEPROM.put(10,server.arg(i).toInt());
      }
  } 
  EEPROM.commit();
  server.send(200,"text/plain","OK");
}

void toggleReadingLight(){
  digitalWrite(13,!reading_light);
  reading_light = !reading_light;
  server.send(200,"text/plain","OK");
}

void getReadingLight(){
  if(reading_light){
    server.send(200,"text/plain","ON");
  } else{
    server.send(200,"text/plain","OFF");
  }
}

void getAlarm(){
  int days[7];
  read_alarm_date(days);
  int time = read_alarm_time();
  String response = "{\"days\":[";
  for(int i=0;i<7;i++){
    response += String(days[i]);
    if(i != 6){
      response += ",";
    }
  }
  response += "],\"time\":\"";
  response += String(time) + "\"}";
  server.send(200,"application/json",response);
}

void getColor(){
  server.send(200, "application/json","{\"RGB\":[" + String(RGB[0]) + "," + String(RGB[1]) + "," + String(RGB[2]) + "], \"ON\":" + String(RGB[3]) + "}");
}

void geteeprom(){
  int data;
  data = EEPROM.read(server.arg(0).toInt());
  server.send(200, "application/text",String(data));

}
void loop() {
  unsigned long currentMillis = millis();
  if (currentMillis - previousMillis >= 1000) {
    int day = timeClient.getDay();
    if(day!=day_prev){
      day_prev = day;
      alarm_ran=false;
    }
    previousMillis = currentMillis;
    int time = timeClient.getHours()*3600 + timeClient.getMinutes()*60 + timeClient.getSeconds();
    Serial.println("TIME");
    Serial.println(time);
    Serial.println(alarm);
    Serial.println(timeClient.getDay());
    if (time > alarm && days[timeClient.getDay()] && !alarm_ran){
      alarm_ran = true;
      analogWrite(0,255);
      analogWrite(2,255);
      analogWrite(14,255);
      Serial.println("ALARM");
      digitalWrite(ALARM_PIN, HIGH);
      playTetris();
      digitalWrite(ALARM_PIN, LOW);
      analogWrite(0,0);
      analogWrite(2,0);
      analogWrite(14,0);
    }
    counter += 1;
  }
  server.handleClient();
}
