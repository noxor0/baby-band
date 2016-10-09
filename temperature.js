var five = require("johnny-five");
var Tessel = require("tessel-io");
var Barcli = require("barcli");

var board = new five.Board({
  io: new Tessel()
});

board.on('ready', function() {
  // Write your program locally and push to the Tessel 2 when ready!
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

  board.on('ready', function(){
    var temp = new Tessel.Temperature('A0');

    temp.on('data', function(err, data){
      fahrenheitGraph.update(data.fahrenheit);
      celsiusGraph.update(data.celsius);
      kelvinGraph.update(data.Kelvin);
    });
});
/*
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

board.on('ready', function(){
  var temp = new tessel.Temperature('A0');

  temp.on('data', function(err, data){
    fahrenheitGraph.update(data.fahrenheit);
    celsiusGraph.update(data.celsius);
    kelvinGraph.update(data.Kelvin);
  });
})
*/
