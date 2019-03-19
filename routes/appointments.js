// Get the express module and use Router
var express = require('express');
var router = express.Router();

// Get the required models
var models = require('../models/models.js');

var auth = require('../authInit.js');

var firebase = require('../firebase/firebase.js');

var authenticateOfficemember = require('../deviceAuth.js').authenticateOwner;
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
