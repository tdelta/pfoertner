module.exports = function(app){
  var Role = app.models.Role;

  Role.registerResolver('officeMember', function(role,context,cb){
    if (context.modelName !== 'office') {
      // Only handle requests to offices, return false
      return process.nextTick(() => cb(null, false));
    }
    var userId = context.accessToken.userId;
    if (!userId) {
      // No logged in user, return false
      return process.nextTick(() => cb(null, false));
    }
    app.models.Device.findById(userId, function(err, user) {
      if(err) {
        return cb(err);
      }
      if(!user){
        return cb(new Error(`User with id ${userId} not found`));
      }
      user.person(function(err,person){ 
        console.log(person);
        if(!person || !person.officeId){
          // User doesnt belong to any office
          return process.nextTick(() => cb(null,false));
        }
        // Check if user belongs to the given office
        if(person.officeId === context.modelId) {
          return process.nextTick(() => cb(null,true));
        }
        return process.nextTick(() => cb(null,false));
      });
    });
  });
}
