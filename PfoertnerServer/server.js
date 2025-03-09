// Load configuration form .env file
require('dotenv').config();

// Get needed express module
const express = require('express');
const server = express();

// Get filesync and http module
const fs = require('fs');
const http = require('http');

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

// Connect to firebase
firebase.initialize();

// Get routes
const officesroutes = require('./routes/offices.js');
const devicesroutes = require('./routes/devices.js');
const officemembersroutes = require('./routes/officesmembers.js');
const appointmentroutes = require('./routes/appointments.js');
const calendarroutes = require('./routes/calendar.js');
const google_notifications = require('./routes/google-notifications.js');

// Use routes
server.use('/offices', officesroutes);
server.use('/devices', devicesroutes);
server.use('/officemembers', officemembersroutes);
server.use('/appointments', appointmentroutes);
server.use('/notifications', google_notifications);
server.use('/calendar', calendarroutes);
server.use(express.static('public_files'));

// Listen on port 3000 localhost
db.sequelize.sync().then(() => {
  const httpServer = http.createServer(server);
  httpServer.listen(3000, () => console.log('Listening on port 3000'));
});
