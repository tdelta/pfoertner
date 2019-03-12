// Get our own models
const models = require('./models/models.js');

// setup authentication
var passport = require('passport');
var auth = require('./auth.js');

var passportStrategy = auth.getStrategy(
  'vZPE3TDNJqBJUkY5NXfWPuhPPM5sefkxn45HHfLkxK22Pe3mUL8iTTovktxQdMsyKzPPVfUD', // secret key of server for auth tokens
  deviceId => {
    const device = models.Device.findById(deviceId);

    return device == null ? undefined : device;
  }
);

function authFun() {
  return passport.authenticate('jwt', { session: false });
}

module.exports = {
  auth,
  authFun,
  passport,
  passportStrategy,
};
