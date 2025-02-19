// Get our own models
const models = require('./models/models.js');

// setup authentication
var passport = require('passport');
var auth = require('./auth.js');

var passportStrategy = auth.getStrategy(
  process.env.JWT_SECRET, // secret key of server for auth tokens stored in .env file
  deviceId => {
    const device = models.Device.findByPk(deviceId);

    return device == null ? undefined : device;
  }
);

/**
 * This function checks whether a given authToken is a well-formed
 * Json-Web-Token.
 */
function authFun() {
  return passport.authenticate('jwt', { session: false });
}

module.exports = {
  auth,
  authFun,
  passport,
  passportStrategy,
};
