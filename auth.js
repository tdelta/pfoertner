var moment = require('moment');

var jwt = require('jsonwebtoken');

var passport = require("passport");
var passportJWT = require("passport-jwt");

var ExtractJwt = passportJWT.ExtractJwt;
var JwtStrategy = passportJWT.Strategy;

var jwtOptions = {
  jwtFromRequest: ExtractJwt.fromAuthHeaderAsBearerToken(),
}

function getStrategy(serverKey, findUserById) {
  jwtOptions.secretOrKey = serverKey;

  return new JwtStrategy(
    jwtOptions,
    (jwtPayload, next) => {
      // check whether the token has timed out:
      const ttl = jwtPayload.ttl;
      let created = moment(
        jwtPayload.created,
        moment.ISO_8601
      );
      const now = moment();

      created.add(ttl, 'second');

      if (now.isBefore(created)) {
        // token is still valid!

        findUserById(jwtPayload.id).then(
          user => {
            if (user) {
              next(null, user);
            } else {
              next(null, false);
            }
          }
        );
      }

      else {
        // token timed out!
        next(null, false);
      }
    }
  );
}

function genToken(user) {
  let ttl = 604800 // 2 weeks
  let created = moment().toISOString(); // return in ISO-8601 format

  let payload = {
    id: user.id,
    ttl: ttl,
    created: created
  }; 

  let token = jwt.sign(
    payload,
    jwtOptions.secretOrKey
  );

  return {
    id: 0, // can be removed, not needed for JWT
    token: token,
    created: created,
    ttl: ttl,
    userId: user.id
  };
}

module.exports = {
  genToken,
  getStrategy
};
