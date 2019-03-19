var db = require('../database.js');
var Sequelize = require('sequelize');

// Get device data model
var Device = require('./device.js');
// Get officemember data model
var OfficeMember = require('./officemember.js');
// Get office data model
var Office = require('./office.js');
// Get appointment data model
var AppointmentRequest = require('./appointmentRequest.js');

// Define relations

// Adds primary key from device to OfficeMember as a foreign key
OfficeMember.belongsTo(Device);
Device.hasOne(OfficeMember);

//Adds primary key from Office to OfficeMember as a foreign key
OfficeMember.belongsTo(Office);
Office.hasMany(OfficeMember);

Office.hasOne(Device);
Device.belongsTo(Office);

//Appointment has a foireign key pointing to an office member
AppointmentRequest.belongsTo(OfficeMember);
OfficeMember.hasMany(AppointmentRequest);

module.exports = {
  Office,
  Device,
  OfficeMember,
  AppointmentRequest,
};
