// Get needed express module
const express = require('express');
const server = express();

// Get our own models
const models = require('./models/models.js');

// Get interface to firebase
const firebase = require('./firebase/firebase.js');

const auth = require('./authInit.js');

auth.passport.use(auth.passportStrategy);
server.use(auth.passport.initialize());

// Get needed bodyparser module
const bodyParser = require('body-parser');

// Get the multer for imageupload
var fileupload = require('express-fileupload');

// Get our own database module
const db = require('./database.js');

server.use(bodyParser.urlencoded({ extended: true }));
server.use(bodyParser.json());
server.use(fileupload());

// Listen on port 3000 localhost
db.sequelize.sync().then(() => server.listen(3000));

// Connect to firebase
firebase.initialize();

// Get routes
const officesroutes = require('./routes/offices.js');
const devicesroutes = require('./routes/devices.js');
const officemembersroutes = require('./routes/officesmembers.js');
const appointmentroutes = require('./routes/appointments.js');

server.use('/offices', officesroutes);
server.use('/devices', devicesroutes);
server.use('/officemembers', officemembersroutes);
server.use('/appointments', appointmentroutes);
