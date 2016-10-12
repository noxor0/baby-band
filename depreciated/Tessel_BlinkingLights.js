//Import the interface to hardware
var tessel = require('tessel');

//Turn on one LED
tessel.led[2].on();

//Blink
setInterval(function () {
    tessel.led[2].toggle();
    tessel.led[3].toggle();
}, 100);
console.log("I'm blinking!");