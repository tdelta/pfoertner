// Get the express module and use Router
var express = require('express');
var router = express.Router();

var mail = require("nodemailer").mail;

// Get the required models
var models = require('../models/models.js');

var auth = require('../authInit.js');

var firebase = require('../firebase/firebase.js');

var authenticateOfficemember = require('../deviceAuth.js').authenticateOwner;
var notifyOfficeSubscribers = require('../notify.js').notifyOfficeSubscribers;

router.patch('/:id', auth.authFun(), (req, res) => {
  let appointmentId = parseInt(req.params.id, 10);
  models.AppointmentRequest.findByPk(appointmentId).then(appointment => {
    if (appointment == null) {
      res.status(404).send('The appointment does not exist');
      return;
    }
    authenticateOfficemember(req, res, appointment.OfficeMemberId).then(
      officemember => {
        appointment.update(req.body).then(newAppointment => {
          res.status(200).send('Updated appointment request successfully');

          if (newAppointment.accepted) {
            if (newAppointment.email != null) {
              console.log('Sending mail to ' + newAppointment.email);

              mail({
                  from: "Pfoertner",
                  to: newAppointment.email,
                  subject: "Your appointment",
                  text: "Your appointment got accepted ✔"
              });
            }

            else {
              console.log('Cant send email, since it has not been set.');
            }
          }

          officemember.getOffice().then(office => {
            notifyOfficeSubscribers(
              office,
              'AppointmentsUpdated',
              officemember.id.toString()
            );
          });
        });
      }
    );
  });
});

router.delete('/:id', auth.authFun(), (req, res) => {
  let appointmentId = parseInt(req.params.id, 10);
  models.AppointmentRequest.findByPk(appointmentId).then(appointment => {
    if (appointment == null) {
      res.status(404).send('The appointment does not exist');
      return;
    }
    authenticateOfficemember(req, res, appointment.OfficeMemberId).then(
      officemember => {
        appointment.destroy().then(u => {
          if (u && u.deletedAt) {
            res.status(200).send('Successfully deleted appointment request');
          } else {
            res.status(500).send('Could not delete appointment request');
          }
          officemember.getOffice().then(office => {
            notifyOfficeSubscribers(
              office,
              'AppointmentsUpdated',
              officemember.id.toString()
            );
          });
        });
      }
    );
  });
});

module.exports = router;
