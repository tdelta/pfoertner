'use strict';

module.exports = function(Device) {
  delete Device.validations.email;

  Device.observe( "after save", function( ctx, next) {
    let instance = ctx.instance;
    if (instance && instance.id != instance.username){
      instance.username = instance.id;
      instance.save();
    }
    next();
  });

  Device.afterRemote('prototype.__create__person',function(ctx,person,next){
    if(person && person.officeId){
      delete person.officeId;
      person.save();
    }
    next();
  });
};
