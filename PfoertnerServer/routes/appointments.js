// Get the express module and use Router
var express = require('express');
var router = express.Router();

var nodemailer = require('nodemailer');

// Get the required models
var models = require('../models/models.js');

var auth = require('../authInit.js');

var firebase = require('../firebase/firebase.js');

var authenticateOfficemember = require('../deviceAuth.js').authenticateOwner;
var authenticatePanelOrOwner = require('../deviceAuth.js').authenticatePanelOrOwner;
var notifyOfficeSubscribers = require('../notify.js').notifyOfficeSubscribers;

/**
 * ENDPOINT: PATCH /appointments/:id
 * 
 * This endpoint updates existing appointments and notifies
 * officemembers about the change. If there is no appointment
 * matching :id, the endpoint returns a 404 status code and informs
 * about it.
 * 
 * @param {*} req request contains the new values for the appointment
 * @param {*} res response
 * 
 */
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

/**
 * ENDPOINT: DELETE /appointments/:id
 * 
 * This endpoint deletes the appointment matching id and informs 
 * about the success. If there is no appointment with :id, then it returns
 * a 404 status code and informs about it.
 * 
 * @param {*} req request
 * @param {*} res result
 * 
 */
router.delete('/:id', auth.authFun(), (req, res) => {
  let appointmentId = parseInt(req.params.id, 10);
  models.AppointmentRequest.findByPk(appointmentId).then(appointment => {
    if (appointment == null) {
      res.status(404).send('The appointment does not exist');
      return;
    }
    authenticatePanelOrOwner(req, res, appointment.OfficeMemberId).then(
      officemember => {
        appointment.destroy().then(u => {
          officemember.getOffice().then(office => {
            console.log('Notifying office members about updated appointments');

            notifyOfficeSubscribers(
              office,
              'AppointmentsUpdated',
              officemember.id.toString()
            );
          });

          console.log('Successfully deleted appointment');

          if (!process.env.EMAIL_ADDRESS || !process.env.EMAIL_PASSWORD) {
            res.status(200).send('Successfully deleted appointment request');
            return;
          }

          console.log('Sending mail to ' + u.email);

          const transporter = nodemailer.createTransport({
            host: 'mail.gmx.net',
            port: 587,
            secureConnection: false,
            auth: {
              user: process.env.EMAIL_ADDRESS,
              pass: process.env.EMAIL_PASSWORD,
            },
            tls: {
              ciphers: 'SSLv3',
            },
          });

          // setup email data with unicode symbols
          const mailOptions = {
            from: `Pförtner App <${process.env.EMAIL_ADDRESS}>`,
            to: u.email,
            subject: 'Appointment has been rejected', // Subject line
            text:
              'Hello ' +
              u.name +
              ',\n\n sadly your appointment at ' +
              u.start +
              ' got rejected.', // plain text body
          };

          // send mail with defined transport object
          const info = transporter
            .sendMail(mailOptions)
            .catch(err => {
              console.error('Failed to send mail to ' + u.email, err);
            })
            .then(() => {
              console.log('Sent email to ' + u.email);
            });

          res.status(200).send('Successfully deleted appointment request');
        });
      }
    );
  });
});

module.exports = router;
