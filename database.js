// Get needed orm caminte module
var Sequelize = require('sequelize');

// Create new sqlite database
var sequelize = new Sequelize('mainDB', null, null, {
  dialect: 'sqlite',
  storage: './database2.sqlite',
});

module.exports = {
  sequelize,
};
