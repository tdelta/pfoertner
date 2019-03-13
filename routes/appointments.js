// Get the express module and use Router
var express = require('express');
var router = express.Router();

var nodemailer = require('nodemailer');

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
          console.log('Successfully deleted appointment. Sending mail to ' + u.email);

          const transporter = nodemailer.createTransport({
              host: "mail.gmx.net",
              port: 587,
              secureConnection: false,
              auth: {
                user: 'pfoertner.app@gmx.de',
                pass: '9x8e92UaPZSvw7ejpju3njcNbDRsWW7MEZRRqSnn'
              },
              tls:{
                  ciphers:'SSLv3'
              }
            });

          // setup email data with unicode symbols
          const mailOptions = {
                from: '"Pförtner App <pfoertner.app@gmx.de>',
                to: u.email,
                subject: "Appointment has been rejected", // Subject line
                text: "Hello " + u.name + ",\n\n sadly your appointment at " + u.start + " got rejected.", // plain text body
              };

          // send mail with defined transport object
          const info = transporter.sendMail(mailOptions)
            .catch(err => {
              console.error('Failed to send mail to ' + u.email, err);
            })
            .then(() => {
              console.log("Sent email to " + u.email);
            });

          res.status(200).send('Successfully deleted appointment request');

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
