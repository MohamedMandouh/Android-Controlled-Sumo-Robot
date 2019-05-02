#include <SoftwareSerial.h>
 
SoftwareSerial sumo(0, 1); // TX, RX
#define MAX_BUFFER 150
char data;
char* buffer;
int pos;
int speed;
double th;
double cur_th;
long lastTimeStamp;
int clockWise;
const int IN1 = 3;
const int IN2 = 6;
const int IN3 = 11;
const int IN4 = 5;
void setup() {
  sumo.begin(9600);
  buffer = new char[MAX_BUFFER];
  pinMode(IN1, OUTPUT);
  pinMode(IN2, OUTPUT);
  pinMode(IN3, OUTPUT);
  pinMode(IN4, OUTPUT);
}
 
void modifyData(int nspeed, double nth) {
  if (nspeed == -1)
    speed = th = cur_th = 0;
  else {
    speed = nspeed;
    th = nth;
  }
}
void readData() {
  if (sumo.available()) {
    data = sumo.read();
    if (data == 3) {
      int s;
      double t;
      int x;
      if (x = buffer2int(buffer, &s, &t)) {
        modifyData(s, t);
      }
      resetBuffer();
 
    } else {
      buffer[pos++] = data;
    }
  }
}
void moveLeftWheel(double speed, bool forward){
  if(forward){
     analogWrite(IN1, speed);
     analogWrite(IN2, 0);  
  }else{
     analogWrite(IN1, 0);
     analogWrite(IN2, speed);
  }
}
void moveRightWheel(double speed, bool forward){
  if(forward){
     analogWrite(IN3, speed);
     analogWrite(IN4, 0);  
  }else{
     analogWrite(IN3, 0);
     analogWrite(IN4, speed);
  }
}
void setVelocities(double leftVel, double rightVel){
  bool leftDirection = leftVel > 0; // 1 forward, 0 backward;
  bool rightDirection = rightVel > 0; // 1 forward, 0 backward;
  moveLeftWheel(abs(leftVel), leftDirection);
  moveRightWheel(abs(rightVel), rightDirection);
 
}
void moveRight(){
  double leftWheelSpeed, rightWheelSpeed;
  double ratio = 2 * (1 - th); // 0 move forward, 1, rotate in position
  double factor = speed;
  leftWheelSpeed = factor * 1;
  rightWheelSpeed = factor * -(ratio * 2 - 1);
  setVelocities(leftWheelSpeed, rightWheelSpeed);
}
void moveLeft(){
  double leftWheelSpeed, rightWheelSpeed;
  double ratio = 2 * th; // 0 move forward, 1, rotate in position
  double factor = speed;
  leftWheelSpeed = factor * -(ratio * 2 - 1);
  rightWheelSpeed = factor * 1;
  setVelocities(leftWheelSpeed, rightWheelSpeed);
}
void adjustPosition() {
  if(th < .5)
    moveLeft();
  else
    moveRight();
}
void loop() {
  readData();
  adjustPosition();
}
 
void resetBuffer() {
  for (int i = 0; i <= pos; i++) buffer[i] = 0;
  pos = 0;
}
 
bool all_good(char* buffer){
  while(*buffer){
    char c = *buffer;
    if(c != '-' && !(c >= '0' && c <= '9') && c != ' ')
      return false;
    buffer++;
  }
  return true;
}
bool buffer2int(char* buffer, int* s, double * t) {
  int a, b;
  int num = sscanf(buffer, "%d %d", &a, &b);
  *s = a;
  *t = b / 100.0;
  return num == 2 && (*s >= 0 && *s <= 255 && *t >= 0 && *t <= 1.0 || *s == -1 && *t == -1.0)  && all_good(buffer);
}
