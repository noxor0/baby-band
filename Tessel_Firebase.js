//Import firebase
var Firebase = require('firebase');

//Import tessel hardware library
var tessel = require('tessel');

//Reference to Firebase
var myFirebaseRef = new Firebase("https://baby-band.firebaseio.com/");

//Send to firebase
myFirebaseRef.set({
    title:"RandomData",
    values:"ABC"
});