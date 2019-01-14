var db = require('../database.js');
var Sequelize = require('sequelize');

var Office = require('./office.js');

var Device = db.sequelize.define('Device',{
    id: {
        type: Sequelize.INTEGER,
        primaryKey: true,
        autoIncrement: true
    },
    fcmToken: Sequelize.STRING
});

module.exports = {
    Device
}


// Define Relations
Device.belongsTo(Office);