/*
   Adapted from the example file "FullExample" included with the TinyGPS++ library
*/

#include <TinyGPS++/TinyGPS++.h>

//Change GPSBaud based on GPS unit
//9600 for Adafruit's Ultimate GPS

static const uint32_t GPSBaud = 9600;
//The TinyGPS++ object
TinyGPSPlus gps;

void setup() {

    //Begin serial at 115200 Baud
    Serial.begin(115200);

    //Serial1 reads from the Electron's TX/RX
    Serial1.begin(GPSBaud);

void loop() {

    //Coordinates for the Seattle Conference Center
    static const double CONFERENCE_CENTER_LAT = 47.6125877, CONFERENCE_CENTER_LON = -122.332679;

    //Uses library's "distanceBetween" to calculate distance
    unsigned long distanceKmToLondon = (unsigned long)TinyGPSPlus::distanceBetween(gps.location.lat(),gps.location.lng(),CONFERENCE_CENTER_LAT,CONFERENCE_CENTER_LON) / 1000;

    printInt(distanceKmToLondon, gps.location.isValid(), 9);

    double courseToLondon = TinyGPSPlus::courseTo(gps.location.lat(), gps.location.lng(), CONFERENCE_CENTER_LAT,CONFERENCE_CENTER_LON);

    printFloat(courseToLondon, gps.location.isValid(), 7, 2);
    const char *cardinalToLondon = TinyGPSPlus::cardinal(courseToLondon);
    printStr(gps.location.isValid() ? cardinalToLondon : "*** ", 6);
    printInt(gps.charsProcessed(), true, 6);
    printInt(gps.sentencesWithFix(), true, 10);
    printInt(gps.failedChecksum(), true, 9);
    Serial.println();

    // Now we prepare to publish location data to our webhook
    // Make strings of the Latitude and Longitude readings
    char coord1[10];
    sprintf(coord1, "%f", (gps.location.lat(), gps.location.isValid(), 11, 6));
    
    char coord2[10];
    sprintf(coord2, "%f", (gps.location.lng(), gps.location.isValid(), 12, 6));

    //Combine these strings into the coordinate format accepted by Initial State
    char coord3[21];
    strcpy(coord3,coord1);
    strcat(coord3,",");
    strcat(coord3,coord2);

    //Print to serial to check format
    Serial.println(coord3);

    //Send the data (coord3) by calling our webhook with the Event Name "sendgps"
    //Particle.publish("sendgps",coord3);

    //Wait 60 seconds
    smartDelay(60000);

    if (millis() > 5000 && gps.charsProcessed() < 10)
        Serial.println(F("No GPS data received: check wiring"));
}

// This custom version of delay() ensures that the gps object is being "fed".
static void smartDelay(unsigned long ms) {
    unsigned long start = millis();
    do {
    while (Serial1.available())
        gps.encode(Serial1.read());
    } while (millis() - start < ms);
}