// Get our own models
const models = require('./models/models.js');


// setup authentication
var passport = require('passport');
var auth = require('./auth.js');

var passportStrategy = auth.getStrategy(
  'This is the secret key of server',
  deviceId => {
    const device = models.Device.findById(deviceId);

    return device == null ?
      undefined : device;
  }
);


function authFun() {
    return passport.authenticate('jwt', {session: false});
  } 

  module.exports = {
    auth,
    authFun,
    passport,
    passportStrategy
  }