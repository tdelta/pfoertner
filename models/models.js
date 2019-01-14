var db = require('../database.js');
var Sequelize = require('sequelize');

// Define device

var Device = db.sequelize.define('Device',{
    id: {
        type: Sequelize.INTEGER,
        primaryKey: true,
        autoIncrement: true
    },
    fcmToken: Sequelize.STRING
});


// Define officemember

var OfficeMember = db.sequelize.define('Office',{
    id: {
        type: Sequelize.INTEGER,
        primaryKey: true,
        autoIncrement: true
    },
    firstName: Sequelize.STRING,
    lastName : Sequelize.STRING,
    status: Sequelize.STRING
});

// Define office 

var Office = db.sequelize.define('Office',{
    id: {
        type: Sequelize.INTEGER,
        primaryKey: true,
        autoIncrement: true
    },
    joinCode: Sequelize.STRING,
    roomNumber: Sequelize.STRING,
    status: Sequelize.STRING
});


// Define relations


// Adds primary key from device to OfficeMember as a foreign key
OfficeMember.belongsTo(Device);

//Adds primary key from Office to OfficeMember as a foreign key
OfficeMember.belongsTo(Office);

// Adds primary key from Device to Office as a foreign key
// This device is the panel app
Office.belongsTo(Device);


module.exports = {
    Office,
    Device,
    OfficeMember
}
