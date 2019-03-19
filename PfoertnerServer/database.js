// Get needed orm caminte module
var Sequelize = require('sequelize');

// Create new sqlite database
var sequelize = new Sequelize('mainDB', null, null, {
  dialect: 'sqlite',
  storage: './database2.sqlite',
  logging: msg => {
    console.log('\x1b[34m   [ Sequelize ]   ' + msg + '\x1b[0m');
  },
});

module.exports = {
  sequelize,
};
