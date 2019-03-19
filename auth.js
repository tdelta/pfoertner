var moment = require('moment');

var jwt = require('jsonwebtoken');

var passport = require('passport');
var passportJWT = require('passport-jwt');

var ExtractJwt = passportJWT.ExtractJwt;
var JwtStrategy = passportJWT.Strategy;

var jwtOptions = {
  jwtFromRequest: ExtractJwt.fromAuthHeaderAsBearerToken(),
};

/**
 * This function checks whether the information within the authToken are valid.
 * If the token is well-formed and the additional information are correct, 
 * the function then returns the owner of the authToken.
 * 
 * @param {*} serverKey  serverKey
 * @param {*} findUserById function to search for the owner of a authToken 
 */
function getStrategy(serverKey, findUserById) {
  jwtOptions.secretOrKey = serverKey;

  return new JwtStrategy(jwtOptions, (jwtPayload, next) => {
    // check whether the token has timed out:
    const ttl = jwtPayload.ttl;
    let created = moment(jwtPayload.created, moment.ISO_8601);
    const now = moment();

    created.add(ttl, 'second');

    if (now.isBefore(created)) {
      // token is still valid!

      findUserById(jwtPayload.id).then(user => {
        if (user) {
          next(null, user);
        } else {
          next(null, false);
        }
      });
    } else {
      // token timed out!
      next(null, false);
    }
  });
}

/**
 * This function genereates authTokens for devices
 * 
 * 
 * @param {*} user user which requested an authToken
 */
function genToken(user) {
  let ttl = 604800; // 2 weeks
  let created = moment().toISOString(); // return in ISO-8601 format

  let payload = {
    id: user.id,
    ttl: ttl,
    created: created,
  };

  let token = jwt.sign(payload, jwtOptions.secretOrKey);

  return {
    id: 'Bearer ' + token, // can be removed, not needed for JWT
    created: created,
    ttl: ttl,
    userId: user.id,
  };
}

module.exports = {
  genToken,
  getStrategy,
};
