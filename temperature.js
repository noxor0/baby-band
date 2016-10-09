var five = require('tessel');
var Barcli = require('barcli');

var board = new five.Board();

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
  var temp = new five.Temperature('A0');

  temp.on('data', function(err, data){
    fahrenheitGraph.update(data.fahrenheit);
    celsiusGraph.update(data.celsius);
    kelvinGraph.update(data.Kelvin);
  });
})
