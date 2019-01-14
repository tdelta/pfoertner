var db = require('../database.js');

var Device =require('./device.js');

var Sequelize = require('sequelize'); 


var OfficeMember = db.sequelize.define('Office',{
    id: {
        type: Sequelize.INTEGER,
        primaryKey: true,
        autoIncrement: true
    },
    firstName: Sequelize.STRING,
    lastName = Sequelize.STRING,
    status: Sequelize.STRING
});

module.exports = {
    OfficeMember
}

// Define relations
// Adds primary key from device to OfficeMember as a foreign key
OfficeMember.belongsTo(Device);

//Adds primary key from Office to OfficeMember as a foreign key
OfficeMember.belongsTo(Office);