//Connect to WiFi
tessel.network.wifi.connect({
    ssid: 'GEvents',
    password: 'GEvents4325',
    security: 'wpa2'
}, function (error, settings) {
    if (error) {
        console.log('COULD NOT CONNECT')
    }
console.log(settings);
});