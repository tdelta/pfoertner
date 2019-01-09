const initializeFirebase = require('../firebase/firebase.js').initializeFirebase;

module.exports = function(server){
  initializeFirebase();
}
