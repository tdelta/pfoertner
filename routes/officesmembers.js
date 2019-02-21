// Get the express module and use Router
var express = require('express');
var router = express.Router();

// Get the required models
var models = require('../models/models.js');

var firebase = require('../firebase/firebase.js');
var notify = require('../notify.js');
var notifyOfficeSubscribers = notify.notifyOfficeSubscribers;

var authenticateOwner = require('../deviceAuth.js').authenticateOwner;
var authenticatePanelOrOwner = require('../deviceAuth.js').authenticatePanelOrOwner;

var auth = require('../authInit.js');

// ONLY FOR DEBUGING/TESTING PURPOSES. REMOVE FOR FINAL SUBMISSION
// List all users created in the database
router.get('/', (req, res) => {
  models.OfficeMember.findAll().then(officesmembers =>
    res.send(officesmembers)
  );
});

// ONLY FOR DEBUGING/TESTING PURPOSES. REMOVE FOR FINAL SUBMISSION
// Return a specific user (which matches a id)
router.get('/:id/debug', (req, res) =>
  models.OfficeMember.findById(req.params.id).then(officemember =>
    res.status(200).send(officemember)
  )
);

// ONLY FOR DEBUGING/TESTING PURPOSES. REMOVE FOR FINAL SUBMISSION
router.get('/:id/office', (req, res) =>
  models.OfficeMember.findById(req.params.id).then(officemember =>
    officemember.getOffice().then(office => res.send(office))
  )
);

/**
 * ENDPOINT: GET /officemembers/id
 *
 * Returns the officemember with the given id, if the panel
 * or the admin is authenticated.
 */
router.get('/:id', auth.authFun(), (req,res) => {
  authenticatePanelOrOwner(req,res).then(officemember => {
    models.OfficeMember.includeAppointmentRequests(officemember).then(officemember => {
      
      res.status(200).send(officemember);
    });
  });
});


/**
 * ENDPOINT: PATCH /officemembers/:id/picture
 *
 * Updates the picture of the officemember
 * DARK JAVASCRIPT MAGIC!
 */
router.patch('/:id/picture', (req, res) => {
  const picture = req.files.picture;
  const hash = req.files.picture.md5();

  const officememberid = parseInt(req.params.id, 10);

  picture.mv('uploads/' + req.params.id + '.jpg', function(err) {
    if (err) {
      return res.status(500).send(err);
    } else {
      models.OfficeMember.findById(officememberid).then(officemember => {
        officemember.getOffice().then(office => {
          console.log('Das Officemember' + officemember);
          officemember
            .update({
              picture: 'http://deh.duckdns.org:3000/officemembers/' + req.params.id + '/picture',
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
  models.OfficeMember.findById(officememberid).then(member => {
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
  models.OfficeMember.findById(officememberid).then(member => {
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

  authenticateOwner(req, res, req.params.id).then(() => {
    models.OfficeMember.findById(officeMemberId).then(officemember => {
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
 */
router.get('/:id/picture', (req, res) => {
  const officememberid = parseInt(req.params.id, 10);

  // Get the officemember matching the given id
  models.OfficeMember.findById(officememberid).then(member => {
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
 * Endpoint to create an appointment that belongs to an office member
 */
router.post('/:id/appointment', auth.authFun(), (req, res) => {
  let start = req.body.start;
  let end = req.body.end;
  if (!start || !end) {
    res.status('400').send('You must specify a start and end date');
    return;
  }

  authenticatePanelOrOwner(req, res).then(officemember => {
    models.AppointmentRequest.create(req.body).then(appointment => {
      appointment.setOfficeMember(officemember);
      officemember.getDevice().then(device => {
        firebase.sendNotification(
          device.fcmToken,
          'New Appointment request',
          'From ' + start + ' to ' + end,
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
          {
            start: start,
            end: end,
          }
        );
        officemember.getOffice().then(office => {
          notifyOfficeSubscribers(
            office,
            'OfficeMemberUpdated',
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
 */
router.patch('/:id/status', (req, res) => {
  const officememberid = parseInt(req.params.id, 10);
  console.log('DEBUG:' + req.body.status);
  models.OfficeMember.findById(officememberid).then(member => {
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
