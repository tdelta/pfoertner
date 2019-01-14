var db = require('../database.js');
var Sequelize = require('sequelize');


var Office = db.sequelize.define('Office',{
    id: {
        type: Sequelize.INTEGER,
        primaryKey: true,
        autoIncrement: true
    },
    roomNumber: Sequelize.STRING,
    status: Sequelize.STRING
});

module.exports = {
    Office
}