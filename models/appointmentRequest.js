var db = require('../database.js');
var Sequelize = require('sequelize');

var AppointmentRequest = db.sequelize.define('AppointmentRequest', {
  id: {
    type: Sequelize.INTEGER,
    primaryKey: true,
    autoIncrement: true,
  },
  start: Sequelize.DATE,
  end: Sequelize.DATE,
  email: Sequelize.STRING,
  name: Sequelize.STRING,
  message: Sequelize.STRING,
  accepted: Sequelize.BOOLEAN,
  atheneId: Sequelize.STRING
});

module.exports = AppointmentRequest;
