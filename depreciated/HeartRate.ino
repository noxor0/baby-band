/*
Timer that throws an interrupt every other millisecond
Gives a sample rate of 500Hz and beat to beat timing resolution of 2mS
Disables PWM output on pin 3 and 11, and the tone() command
Works with an ATmega328 and 16MHz clock

ISR is the basic beat finding code
*/


//Sets up CPU clocks and registers
// void interruptSetup() {
//     TCCR1A = 0x00;
//     TCCR1B = 0x0C;
//     OCR1A = 0x7C;
//     TIMSK1 = 0x02;
    
//     //Arduino global interupts enable
//     //sei();
// }


/*
VARIABLES
Signal (raw Pulse Sensor signal) 2mS
IBI (Inter Beat Interval) time between heartbeats in mS
BPM (Beats Per Minute)
QS (Quantified Self Flag) set true every beat
Pulse set true every beat
*/

int pulsePin = 0; //Analog pin # the sensor is plugged into
int blinkPin = 13; //Will blink with the pulse
int fadePin = 5;
int fadeRate = 0; //Fading LED effect
volatile int BPM;
volatile int Signal;
volatile int IBI = 600;
volatile boolean Pulse = false;
volatile boolean QS = false;
volatile int rate[10];
volatile unsigned long sampleCounter = 0;
volatile unsigned long lastBeatTime = 0;
volatile int P =512;
volatile int T = 512;
volatile int thresh = 512;
volatile int amp = 100;
volatile boolean firstBeat = true;
volatile boolean secondBeat = false;
Timer timer(1000);

//Particle vars
const char *PUBLISH_EVENT_NAME = "test1data";
char heartInfo[64];

void setup() {
    pinMode(13,OUTPUT);
    pinMode(10,OUTPUT);
    Serial.begin(115200);
    //interruptSetup();

    //Enables interrupts
    interrupts();
    
    //Only if different voltage to sensor than Arduino
    //analogReference(EXTERNAL);

    startShit();
}

//Corrects ISR vector
//Every 2 mS reads the sensor value and looks for heart beat
void startShit() {
    timer.startFromISR(); 
    Signal = analogRead(pulsePin);
    //Increment the counter
    sampleCounter += 2;
    int N = sampleCounter - lastBeatTime;

    //Keep track of highest and lowest values of PPG wave
    //Gives accurate measure of amplitude
    if(Signal < thresh && N > (IBI/5)*3) {
        if(Signal < T) {
            T = Signal;
        }
    }
    
    if(Signal > thresh && Signal > P) {
        P = Signal;
    }

    //Check for pulse
    //Minimum of 250 mS, upper limit of 240 BPM
    if(N > 250) {
        if((Signal > thresh) && (Pulse == false) && (N > ((IBI/5)*3)) {
            Pulse = true;
            digitalWrite(pulsePin,HIGH);
            IBI = sampleCounter - lastBeatTime;
            lastBeatTime = sampleCounter;

            //Start with arealistic BPM value
            //BPM is derived from average of last 10 IBI values
            if(secondBeat) {
                secondBeat = false;
                for(int i=0; i<=9; i++) {
                    rate[i] = IBI;
                }
            }

            if(firstBeat) {
                firstBeat = false;
                secondBeat = true;
                //sei();
                return;
            }

            //Calculate BPM
            runningTotal = 0;

            for(int i=0; i<=8; i++) {
                rate[i] = rate[i+1];
                runningTotal += rate[i];
            }

            rate[9] = IBI;
            runningTotal += rate[9];
            runningTotal /= 10;
            BPM = 60000/runningTotal;
            QS = true;
        }
    }

    //Clear pulsePin and pulse boolean
    //Mark new threshold at 50%
    if(Signal < thresh && Pulse == true) {
        digitalWrite(13,LOW);
        Pulse = false;
        amp = P - T;
        thresh = amp/2 + T;
        P = thresh;
        T = thresh;
    }

    //No beat for 2.5 seconds
    if(N > 2500) {
        thresh = 512;
        P = 512;
        T = 512;
        firstBeat = true;
        secondBeat = false;
        lastBeatTime = sampleCounter;
    }
}

//Runs every 20 mS
//Send data in here
//Check for QS flag to know when beat happens
void loop() {
    sendDataToProcessing('S', Signal);
    if(QS == true) {
        sendDataToProcessing('B',BPM);
        sendDataToProcessing('Q',IBI);

        //Publish
        sprintf(heartInfo, "%3.0f", BPM);
        Particle.publish(PUBLISH_EVENT_NAME, heartInfo, PRIVATE);

        fadeVal = 255;
        QS = false;
    }
    ledFadeToBeat();
    delay(20);
}

//Sends data to processing
void sendDataToProcessing(char symbol, int data ) {
    Serial.print(symbol);
    Serial.println(data);
}

//Prevents fade from going negative or > 255
void ledFadeToBeat() {
    fadeRate -= 15;
    fadeRate = constrain(fadeRate,0,255);
    analogWrite(fadePin, fadeRate);
}