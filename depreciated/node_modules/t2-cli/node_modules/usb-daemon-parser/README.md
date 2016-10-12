# usb-daemon-parser
A node module used by the CLI to generate packets and parse communication from the Tessel 2 USB Daemon

## Install
```
npm install usb-daemon-parser
```

## Usage

### Generate Packets
(subject to be encapsulated in a "Generator" object soon...);

```.js
var parser = require('usb-daemon-parser');

var id = 1;
console.log(parser.newProcess(id)); // < 0x01, 0x01, 0x00, 0x00 >

var command = new Buffer(100);
console.log(parser.controlWrite(id, command.length)); // < 0x10, 0x10, 0x00, 0x64 >
```

### Parsing Packets

First, pipe incoming data into the parser
```.js
var Parser = require('usb-daemon-parser').Parser;

// Create an instance of the parser
var parser = new Parser();

// Pipe data into it
someDataStream.pipe(parser);
```

Then, the parser will automatically emit events as they are parsed with payloads as arguments. For example:

```
// Get notified when remote processes die
parser.on('EXIT-STATUS', function(packet)) {
  console.log('here is what happened:', packet); // All the data about which process died and how
}
```

Available events include
```
"EXIT-STATUS" : A child process was killed
"ACK-CONTROL": A child process received a payload directed at the CONTROL stream and/or can add credit to the backpressure.
"ACK-STDIN": A child process received a payload directed at the STDIN stream and/or can add credit to the backpressure.
"ACK-CLOSE": All of the resources of a process have been freed (emitted after being commanded to close).
"WRITE-STDOUT" : Data has been received on the STDOUT stream for a process.
"WRITE-STDERR" : Data has been received on the STDERR stream for a process.
"CLOSE-CONTROL" : The remote CONTROL stream has been closed.
"CLOSE-STDIN" : The remote STDIN stream has been closed.
"CLOSE-STDOUT" : The remote STDOUT stream has been closed.
"CLOSE-STDERR" : The remote STDERR stream has been closed.
```