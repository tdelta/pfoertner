'use strict';

var token = require('crypto-token');

module.exports = function(Office) {

  Office.observe('before save', function filterProperties(ctx, next) {
    let instance = ctx.instance
    if(instance){
      instance.userJoinCode = token(32);
    }
    next();
  });
};
