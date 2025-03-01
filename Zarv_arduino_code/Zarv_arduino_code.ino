#include<SoftwareSerial.h>

// SoftwareSerial bluetooth(9,10);

const int C1 = 2;
const int C2 = 3;
const int vcc_voltage = 8;
const int m1 = 12;
const int m2 = 13;
const int input_voltage = A0;
//const int blue_gnd = 10;
//const int blue_vcc = 9;
const int pot = A5;

// Floats for ADC voltage & Input voltage
float adc_voltage = 0.0;
float in_voltage = 0.0;
 
// Floats for resistor values in divider (in ohms)
float R1 = 30000.0;
float R2 = 7500.0; 

double power = 0;
double dist = 0;
double cc = 0;
long elapsed_time = 0;
 
// Float for Reference Voltage
float ref_voltage = 5.0;
 
// Integer for ADC value
int adc_value = 0;

volatile unsigned int pulseCount = 0;
unsigned long lastTime = 0; 
const unsigned long interval = 1000;
float rpm = 0;      

const int pulsesPerRevolution = 20; 

void countPulses() {
  pulseCount++;
}

void setup() {
  Serial.begin(9600);
  pinMode(C1, INPUT_PULLUP); // Set C1 pin as input with pull-up resistor
  pinMode(C2, INPUT_PULLUP); // Set C2 pin as input with pull-up resistor
  pinMode(vcc_voltage, OUTPUT);
  pinMode(m1, OUTPUT);
  pinMode(m2, OUTPUT);
  pinMode(input_voltage, INPUT);
//  pinMode(blue_vcc, OUTPUT);
//  pinMode(blue_gnd, OUTPUT);
  pinMode(pot, INPUT);

//  digitalWrite(vcc_voltage, HIGH);

  //bluetooth.begin(9600);

  attachInterrupt(digitalPinToInterrupt(C1), countPulses, RISING); // Trigger on rising edge of C1
  attachInterrupt(digitalPinToInterrupt(C2), countPulses, RISING); // Trigger on rising edge of C2
}

void loop() {
  unsigned long currentTime = millis();

  digitalWrite(m1, HIGH);
  digitalWrite(m2, LOW);
  analogWrite(6, map(analogRead(pot), 370, 1023, 0, 255));

  if (currentTime - lastTime >= interval) {
    noInterrupts();
    unsigned int pulses = pulseCount;
    pulseCount = 0;
    interrupts();

    rpm = (pulses * 60.0) / pulsesPerRevolution; // RPM formula

     // Read the Analog Input
    adc_value = analogRead(input_voltage);
//    Serial.println(adc_value);
    
    // Determine voltage at ADC input
    adc_voltage  = (adc_value * ref_voltage) / 1024.0;
    
    // Calculate voltage at divider input
    in_voltage = adc_voltage*(R1+R2)/R2;
    in_voltage = (10.67 / 17.45) * (0.124344 * (rpm / 64));
    
    // Print results to Serial Monitor to 2 decimal places
//    Serial.print("Input Voltage = ");
//    Serial.print(in_voltage, 2);
//    Serial.print("V      ");
    if (in_voltage != 0) {
      dist = dist + ((0.124344 * (rpm / 64)) / 3600);
      power = (power + (in_voltage * in_voltage * 0.03));
      elapsed_time++; 
      cc = cc + (0.001114 * dist);
    } else {
      elapsed_time = 0;
    }
    String s = String(0.124344 * (rpm / 64)) + "," + String(in_voltage) + "," + String(in_voltage * 0.03) + "," + String(power) + "," + String(cc);

      Serial.println(s);

//    Serial.print("Input Current = ");
//    Serial.print(in_voltage * 0.03);
//    Serial.print("A      ");
//
//    Serial.print("Power Consumed = ");
//    Serial.print((in_voltage * in_voltage * 0.03 * 60 * 60) / 1000);
//    Serial.print("kWh      ");
//  
//    Serial.print("RPM: ");
//    Serial.print(rpm / 64);
//    Serial.print("      ");
//
//    Serial.print("Speed: ");
//    Serial.print(0.124344 * (rpm / 64));
//    Serial.println("Kph     ");

    lastTime = currentTime;
  }
}
