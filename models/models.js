var db = require('../database.js');
var Sequelize = require('sequelize');

// Define device
var Device = require('./device.js');
// Define officemember
var OfficeMember = require('./officemember.js');
// Define office
var Office = require('./office.js');

// Define relations

// Adds primary key from device to OfficeMember as a foreign key
OfficeMember.belongsTo(Device);
Device.hasOne(OfficeMember);

//Adds primary key from Office to OfficeMember as a foreign key
OfficeMember.belongsTo(Office);
Office.hasMany(OfficeMember);

Office.hasOne(Device);
Device.belongsTo(Office);

module.exports = {
  Office,
  Device,
  OfficeMember,
};
