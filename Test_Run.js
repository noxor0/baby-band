//Import firebase
var Firebase = require('firebase');

//Reference to Firebase
var myFirebaseRef = new Firebase("https://baby-band.firebaseio.com/");

//Send to firebase
myFirebaseRef.set({
    title:"hr",
    values:"80"
});
