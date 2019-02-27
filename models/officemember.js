var db = require('../database.js');
var Sequelize = require('sequelize');

var OfficeMember = db.sequelize.define('OfficeMember', {
  id: {
    type: Sequelize.INTEGER,
    primaryKey: true,
    autoIncrement: true,
  },
  firstName: Sequelize.STRING,
  lastName: Sequelize.STRING,
  status: Sequelize.STRING,
  email: Sequelize.STRING,
  picture: Sequelize.STRING,
  pictureMD5: Sequelize.STRING,
  serverAuthCode: Sequelize.STRING,
  calendarId: Sequelize.STRING,
  appointmentRequests: Sequelize.VIRTUAL,
});

module.exports = OfficeMember;
