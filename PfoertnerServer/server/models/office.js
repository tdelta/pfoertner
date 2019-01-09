'use strict';

var token = require('crypto-token');

var app = require('../../server/server')

module.exports = function(Office) {

  Office.observe('before save', function filterProperties(ctx, next) {
    let instance = ctx.instance
    if(instance){
      instance.userJoinCode = token(32);
    }
    next();
  });

  var addUserToOffice = function(userId, officeId,cb){
    console.log(`Adding user ${userId} to office ${officeId}`);
    app.models.Device.findById(userId,function(err, user){
      if(user){
        user.person(function(err,person){
          if(person){
            person.updateAttribute('officeId',officeId,function(err){
              // www.callbackhell.com
              if(err) {
                console.log(err);
                return cb(err);
              } else {
                return cb();
              }
            });
          }
        });
      }
    });
  }

  Office.join = function(joinCode,id,options,cb){
    Office.findById(id,function(err, instance){
      if(err){
        return cb(err);
      } else {
        if(instance === null){
          let error = new Error('Office does not exist');
          error.status = 404;
          return cb(error);
        }
        // office with given id exists
        if(joinCode === instance.userJoinCode){
          // Add user to office
          let user = options.accessToken.userId;
          return addUserToOffice(user,id,cb);
        } else {
          // join code was incorrect, return error
          let error = new Error('Office join code is incorrect');
          error.status = 401;
          return cb(error);
        }
      }
    });
  }

  Office.remoteMethod('join',{
    http: {path: '/:id/join', verb: 'put'},
    accepts: [
      {arg: 'joinCode', type: 'string', required: 'true'},
      {arg: 'id', type: 'number', required: 'true'},
      {"arg": "options", "type": "object", "http": "optionsFromRequest"}
    ]
  });
}
