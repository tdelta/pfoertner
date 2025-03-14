var db = require('../database.js');
var Sequelize = require('sequelize');
var Office = require('./office.js');

// Define the device sequelize data model
var Device = db.sequelize.define('Device', {
  id: {
    type: Sequelize.INTEGER,
    primaryKey: true,
    autoIncrement: true,
  },
  fcmToken: Sequelize.STRING,
  password: Sequelize.STRING,
});

module.exports = Device;
