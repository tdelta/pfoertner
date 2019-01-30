var db = require('../database.js');
var Sequelize = require('sequelize');

var Appointment = db.sequelize.define('Appointment', {
  id: {
    type: Sequelize.INTEGER,
    primaryKey: true,
    autoIncrement: true,
  },
  start: Sequelize.DATE,
  end: Sequelize.DATE,
}


