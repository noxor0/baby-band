var five = require("johnny-five");
var Tessel = require("tessel-io");
var Barcli = require("barcli");

var board = new five.Board({
  io: new Tessel()
});

board.on('ready', function(){
  var fahrenheitGraph = new Barcli({
    label: 'Fahrenheit',
    range: [20, 120]
  });

  var celsiusGraph = new Barcli({
    label: 'Celsius',
    range: [6, 50]
  });

  var kelvinGraph = new Barcli({
    label: 'Kelvin',
    range: [250, 325]
  })

  var temperature = new five.Temperature({
    controller: "TMP36",
    pin: "A4",
    aref: 3.3
  });

  temperature.on('data', function(err, data){
    console.log(this.celsius + "°C", this.fahrenheit + "°F");
    console.log(data);
    fahrenheitGraph.update(this.fahrenheit); //Should use data.fahrenheit
    celsiusGraph.update(this.celsius);
    kelvinGraph.update(this.kelvin);
  });
});
