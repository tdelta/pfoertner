// Get the express module and use Router
var express = require('express');
var router = express.Router();

// Get the required models
var models = require('../models/models.js');

var firebase = require('../firebase/firebase.js');
var notify = require('../notify.js');
var notifyOfficeSubscribers = notify.notifyOfficeSubscribers;

var authenticatePanelOrOwner = require('../deviceAuth.js')
  .authenticatePanelOrOwner;

var auth = require('../authInit.js');

/**
 * ENDPOINT: GET /officemembers/id
 *
 * Returns the officemember with the given id, if the panel
 * or the admin is authenticated.
 */
router.get('/:id', auth.authFun(), (req, res) => {
  authenticatePanelOrOwner(req, res).then(officemember => {
    res.status(200).send(officemember);
  });
});

/**
 * ENDPOINT: PATCH /officemembers/:id/picture
 *
 * Updates the picture of the officemember
 */
router.patch('/:id/picture', (req, res) => {
  const picture = req.files.picture;
  const hash = req.files.picture.md5;

  const officememberid = parseInt(req.params.id, 10);

  picture.mv('uploads/' + req.params.id + '.jpg', function(err) {
    if (err) {
      return res.status(500).send(err);
    } else {
      models.OfficeMember.findByPk(officememberid).then(officemember => {
        officemember.getOffice().then(office => {
          console.log('Das Officemember' + officemember);
          officemember
            .update({
              picture:
                process.env.PUBLIC_URL + '/officemembers/' +
                req.params.id +
                '/picture',
              pictureMD5: hash,
            })
            .then(() => {
              notifyOfficeSubscribers(
                office,
                'OfficeMemberUpdated',
                officememberid.toString()
              );

              res.status(200).send('File uploaded!');
            });
        });
      });
    }
  });
});

/**
 * ENDPOINT: GET /officemembers/:id/picture
 *
 * Get the picture of the officemember
 *
 */
router.get('/:id/picture', (req, res) => {
  const officememberid = parseInt(req.params.id, 10);

  // Get the officemember matching the given id
  models.OfficeMember.findByPk(officememberid).then(member => {
    // If no officemember with this id is found, return 404
    if (member == null) {
      res.status('404').send('There is no person to your id');
    }
    // There is an officemember matching the id
    else {
      // Check wether there is a picture connected to the requested officemember
      if (member.picture == null) {
        // If there is no picture, return 404
        res.status('404').send('There is no picture to your person');
      } else {
        // If there is a picture, return 200 and the picture
        res.sendFile('/' + req.params.id + '.jpg', { root: 'uploads' });
      }
    }
  });
});

/**
 * ENDPOINT: GET /officemembers/:id/picture/md5
 *
 * Get the md5 hash of the picture of the officemember
 *
 */
router.get('/:id/picture/md5', (req, res) => {
  const officememberid = parseInt(req.params.id, 10);

  // Get the officemember matching the given id
  models.OfficeMember.findByPk(officememberid).then(member => {
    // If no officemember with this id is found, return 404
    if (member == null) {
      res.status('404').send('There is no person to your id');
    }
    // There is an officemember matching the id
    else {
      // Check wether there is a picture hash connected to the requested officemember
      if (member.pictureMD5 == null) {
        // If there is no picture, return 404
        res.status('404').send('There is no picture to your person');
      } else {
        // If there is a picture hash, return 200 and the hash
        res.status('200').send(member.pictureMD5);
      }
    }
  });
});

/**
 * ENDPOINT: PATCH /officemembers/:id
 *
 * Patches general information of an office member
 */
router.patch('/:id', auth.authFun(), (req, res) => {
  const officeMemberId = parseInt(req.params.id, 10);
  console.log('Patching officemember');

  authenticatePanelOrOwner(req, res, req.params.id).then(() => {
    models.OfficeMember.findByPk(officeMemberId).then(officemember => {
      // Only server should set the id
      delete req.body.id;
      officemember.update(req.body).then(officemember => {
        res.status(200);
        res.send(officemember);

        officemember.getOffice().then(office => {
          if (office != null) {
            // TODO optimize to only update the affected member
            notifyOfficeSubscribers(
              office,
              'OfficeMemberUpdated',
              officeMemberId.toString()
            );
          } else {
            console.log(
              'Could not notify office members, that one of them got updated'
            );
          }
        });
      });
    });
  });
});

/**
 * ENDPOINT: GET /officemembers/:id/picture
 *
 * Get the picture of the officemember
 * 
 * @param {*} req request
 * @param {*} res response contains the picture of the officemember matching :id
 *
 */
router.get('/:id/picture', (req, res) => {
  const officememberid = parseInt(req.params.id, 10);

  // Get the officemember matching the given id
  models.OfficeMember.findByPk(officememberid).then(member => {
    // If no officemember with this id is found, return 404
    if (member == null) {
      res.status('404').send('There is no person to your id');
    }
    // There is an officemember matching the id
    else {
      // Check wether there is a picture connected to the requested officemember
      if (member.picture == null) {
        // If there is no picture, return 404
        res.status('404').send('There is no picture to your person');
      } else {
        // If there is a picture, return 200 and the picture
        res.sendFile('/' + req.params.id + '.jpg', { root: 'uploads' });
      }
    }
  });
});

/**
 * ENDPOINT: GET /officesmembers/:id/appointments
 * 
 * This endpoint provides all the appointment of the officemember matching :id
 * 
 * @param {*} req request 
 * @param {*} res response contains alll the appointments of the officemember matching :id
 * 
 */
router.get('/:id/appointments', auth.authFun(), (req, res) => {
  authenticatePanelOrOwner(req, res).then(officemember => {
    officemember.getAppointmentRequests().then(appointments => {
      res.status(200).send(appointments);
    });
  });
});

/**
 * ENDPOINT: POST /officesmembers/:id/appointment
 * 
 * Endpoint to create an appointment that belongs to an office member
 * 
 * @param {*} req request containing all the necessary data to create an appointment
 * @param {*} res response 
 * 
 */
router.post('/:id/appointment', auth.authFun(), (req, res) => {
  const start = req.body.start;
  const end = req.body.end;

  if (!start || !end) {
    res.status('400').send('You must specify a start and end date');
    return;
  }

  authenticatePanelOrOwner(req, res).then(officemember => {
    delete req.body.id; // Id needs to be auto generated.
    models.AppointmentRequest.create(req.body).then(appointment => {
      appointment.setOfficeMember(officemember);
      officemember.getDevice().then(device => {
        firebase.sendNotification(
          device.id,
          device.fcmToken,
          'New Appointment request',
          start.split(' ')[0] +
            ', from ' +
            start.split(' ')[1] +
            ' to ' +
            end.split(' ')[1],
          'MainActivity',
          [
            {
              title: 'Accept',
              intent: 'AcceptAppointmentRequest',
            },
            {
              title: 'Decline',
              intent: 'DeclineAppointmentRequest',
            },
          ],
          appointment
        );

        officemember.getOffice().then(office => {
          notifyOfficeSubscribers(
            office,
            'AppointmentsUpdated',
            officemember.id.toString()
          );
        });

        res.status('200').send('Successfully sent appointment request');
      });
    });
  });
});

/**
 * ENDPOINT: PATCH /officemembers/:id/status
 * Updates the officemember with the status given
 * in the request body
 * 
 * @param {*} req request contains the new value for the status field off the officemember matching :id
 * @param {*} res response
 * 
 */
router.patch('/:id/status', (req, res) => {
  const officememberid = parseInt(req.params.id, 10);
  console.log('DEBUG:' + req.body.status);
  models.OfficeMember.findByPk(officememberid).then(member => {
    if (member !== null) {
      member
        .update({ status: req.body.status })
        .then(res.status('200').send('Status has been updated successfully'));
    } else {
      res
        .status('404')
        .send('There is no officemember matching to the given id');
    }
  });
});

module.exports = router;
