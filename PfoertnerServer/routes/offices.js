var express = require('express');
var router = express.Router();

var models = require('../models/models.js');
// Get interface to firebase
const firebase = require('../firebase/firebase.js');

var util = require('util');

var auth = require('../authInit.js');
var authenticateAnyOfficeDevice = require('../deviceAuth.js')
  .authenticateAnyOfficeDevice;

var notify = require('../notify.js');
var notifyOfficeSubscribers = notify.notifyOfficeSubscribers;
var notifyPanel = notify.notifyPanel;

var pwgenerator = require('generate-password');

/**
 * ENDPOINT: POST /offices/
 * 
 * ATTENTION: YOU HAVE TO BE AUTHENTICATED FOR THIS
 * ACTION.
 * 
 * A request to that endpoint creates a new Office object
 * in the database and returns it.
 * 
 * EXAMPLE RETURN VALUE:
 * 
 * {
  "id": 4,
  "joinCode": "Hallo Welt",
  "updatedAt": "2019-01-22T17:58:37.868Z",
  "createdAt": "2019-01-22T17:58:37.868Z"
    }
 * 
 */
router.post('/', auth.authFun(), (req, res) => {
  const joinCode = pwgenerator.generate({
    length: 30,
    numbers: true,
    symbols: true,
    strict: true,
  });

  const device = req.user;

  models.Office.create({ joinCode: joinCode }).then(office => {
    device.setOffice(office).then(() => {
      res.send(office);
    });
  });
});

/**
 * This helper function returns a promise to create a perstitent
 * officemember models in the data base
 *
 * @param {*} firstName firstname of the created officemember model
 * @param {*} lastName lastname of the created officemember model
 * @param {*} device device to which the created officemember belongs
 * @param {*} office office to which the created officemember belongs
 */
function createOfficeMember(firstName, lastName, device, office) {
  return models.OfficeMember.create({
    firstName: firstName,
    lastName: lastName,
  }).then(officeMember => {
    officeMember.setDevice(device);
    officeMember.setOffice(office);
    return officeMember;
  });
}

/**
 * ENDPOINT: POST /offices/:officeId/members
 *
 * ATTENTION: YOU HAVE TO BE AUTHENTICATED FOR THIS
 * ACTION.
 *
 * This endpoint creates and adds a new officemember to an existing
 * office. If you have an valide joinCode, this endpoint
 * returns the newly created officemember object as a json(look below for
 * definition). If the joinCode isn't valid, return a 401 response code
 * with an error message.c
 *
 * The body of the request for this endpoint must have
 * the following form:
 *
 * Example request body:
 * {
 *  'firstName': 'someName',
 *  'lastName' : 'somelastName',
 *  'joinCode' : 'anValideAuthToken'
 * }
 *
 * Example response body:
 *
 * {
 *  "id": 1,
 *  "firstName": "Max",
 *  "lastName": "Mustermann",
 *  "updatedAt": "2019-01-22T18:50:53.418Z",
 *  "createdAt": "2019-01-22T18:50:53.209Z",
 *  "DeviceId": 1,
 *  "OfficeId": 1
 * }
 */
router.post('/:officeId/members', auth.authFun(), (req, res) => {
  findOffice(req, res).then(office => {
    if (office.joinCode === req.body.joinCode) {
      // Create office member that is connected to the logged in device
      // and the office.
      var newOfficeMember;
      createOfficeMember(
        req.body.firstName,
        req.body.lastName,
        req.user,
        office
      ).then(member => {
        newOfficeMember = member;
        // Send fcm notification
        notifyOfficeSubscribers(office, 'AdminJoined', office.id.toString());
        res.status(200);
        res.send(newOfficeMember);
      });
    } else {
      // Join code is incorrect
      res.status(401);
      res.send('Office join code is incorrect');
    }
  });
});

// ENDPOINT: POST /offices/:officeId/notify
// Send a notification event to all devices of an office
router.post('/:officeId/notify', (req, res) => {
  findOffice(req, res).then(office => {
    notifyOfficeSubscribers(office, req.body.event, req.body.payload);
    res.status(200).send('notified.');
  });
});

/**
 * ENDPOINT: GET /offices/:officesId/members
 *
 * ATTENTION: YOU HAVE TO BE AUTHENTICATED FOR THIS
 * ACTION.
 *
 * This functions returns all officemembers of an
 * office. There has to be an officemembers which belongs
 * to the authenticated device and that officemember has to
 * be a member of the request office
 *
 */
router.get('/:officeId/members', auth.authFun(), (req, res) => {
  if (req.params.officeId == null) {
    res.status(400).send({ message: 'Invalid office id.' });
  } else {
    findOffice(req, res).then(office => {
      office.getOfficeMembers().then(officeMembers => {
        // Get the authenticated device
        const device = req.user;
        const selectedOfficeId = parseInt(req.params.officeId, 10);

        // Get the OfficeMember which belongs to the authenticated device
        device.getOfficeMember().then(loggedIn => {
          if (
            // Check whether there is an officemember which belongs to this device
            // and if there is an officemember, then check whether is belongs to the
            // request office.
            (loggedIn != null && loggedIn.OfficeId === selectedOfficeId) ||
            device.OfficeId === selectedOfficeId
          ) {
            res.status(200).send(officeMembers);
          } else {
            res.status(401);
            res.send('Authorized user is not in requested office');
          }
        });
      });
    });
  }
});

/**
 * Helper function to find an office.
 *
 * @param {*} req contains the officeId for the office you are looking for
 * @param {*} res contains the office you are looking for or a 404 response code.
 */
function findOffice(req, res) {
  return new Promise(function(response) {
    models.Office.findByPk(req.params.officeId).then(office => {
      if (office) {
        response(office);
      } else {
        res.status(404);
        res.send(`Office with id ${req.params.officeId} does not exist`);
      }
    });
  });
}

/**
 * This is a helper function to ensure that any request to
 * a office, which requires authorization, can only be done by
 * a user which is authorized and which is a member of the requested
 * office.
 *
 *
 * @param {*} req request
 * @param {*} res response
 */
function authenticateOfficeMember(req, res) {
  return new Promise(function(response) {
    // Check whether there is a valid officeId in
    // the request
    if (req.params.officeId == null) {
      res.status(400).send({ message: 'The given id is invalid.' });
      return;
    }

    const device = req.user;
    // The request do not have an correct authorization header
    if (device === null) {
      res.status(401).send({
        message: 'You do not have the permission to access this office',
      });
    }
    // The request do have an correct authorization header
    else {
      const officeId = parseInt(req.params.officeId, 10);

      device.getOfficeMember().then(loggedIn => {
        // Check whether a officemember belongs to the authorized device
        // and whether that officemember is a part of the office
        if (
          (loggedIn != null && loggedIn.OfficeId === officeId) ||
          device.OfficeId === officeId
        ) {
          console.log(
            'Office member with device ' + device.id + ' authenticated'
          );
          response();
        }
        // No user belongs to the device or the user belonging to the
        // is not part of the office
        else {
          res.status(401).send({
            message: 'You do not have the permission to access this office.',
          });
        }
      });
    }
  });
}

/**
 * ENDPOINT: PATCH /offices/:officeId
 * 
 * This function updates the in the requested specified office object 
 * with the in the body given data
 *
 * @param {*} req request contains data which is supposed to be updated
 * @param {*} res response contains the updated office object
 */
router.patch('/:officeId', auth.authFun(), (req, res) => {
  console.log('Patching office');
  authenticateOfficeMember(req, res).then(() => {
    findOffice(req, res).then(office => {
      console.log(office);
      // Only server should set the joinCode
      delete req.body.joinCode;
      office.update(req.body).then(office => {
        res.status(200);
        res.send(office);
        notifyOfficeSubscribers(office, 'OfficeDataUpdated');
      });
    });
  });
});

/**
 * ENDPOINT: GET /offices/:id
 *
 * ATTENTION: YOU HAVE TO BE AUTHENTICATED FOR THIS
 * ACTION.
 *
 * This function return the in the request specified office object
 * 
 * @param {*} req request  
 * @param {*} res response contains the in the request speficid office object
 *
 */
router.get('/:officeId', auth.authFun(), (req, res) => {
  const officeId = parseInt(req.params.officeId, 10);

  authenticateOfficeMember(req, res).then(
    models.Office.findByPk(officeId).then(office => {
      res.send(office);
    })
  );
});

/** 
 * ENDPOINT: GET /offices/:id/spion
 *
 * This endpoint provides the spion picture for the admin apps
 * 
 * @param {*} req request
 * @param {*} res response file with the spion picture
 * 
 */
router.get('/:officeId/spion', auth.authFun(), (req, res) => {
  console.log('GET /offices/' + req.officeId + '/spion has been called.');

  const officeId = parseInt(req.params.officeId, 10);

  authenticateAnyOfficeDevice(officeId, req, res).then(office => {
    if (office.spionPicture == null) {
      res.status('404').send('There is no spion picture in this office');
    } else {
      res.sendFile('/' + req.params.officeId + '.jpg', {
        root: 'spionuploads',
      });
    }
  });
});

/**
 * ENDPOINT: PATCH /offices/:id/spion
 *
 * This endpoint is called, when the panelApp wants to upload a new 
 * spion picture.
 * 
 * @param {*} req request contains the picture which will be uploaded
 * @param {*} res response 
 *  
 */
router.patch('/:officeId/spion', auth.authFun(), (req, res) => {
  console.log('function patch spion called');

  const picture = req.files.spion;
  const hash = req.files.spion.md5;

  const officeId = parseInt(req.params.officeId, 10);

  authenticateAnyOfficeDevice(officeId, req, res).then(office => {
    picture.mv('spionuploads/' + req.params.officeId + '.jpg', function(err) {
      if (err) {
        return res.status(500).send(err);
      } else {
        const downloadPath =
          process.env.PUBLIC_URL + '/offices/' +
          req.params.officeId +
          '/spion';

        console.log(
          '\nUploaded new spion picture for office ' +
            office.id +
            ' with hash ' +
            hash +
            '. It will be available on ' +
            downloadPath +
            '.\n'
        );

        office
          .update({
            spionPicture: downloadPath,
            spionPictureMD5: hash,
          })
          .then(() => {
            notifyOfficeSubscribers(
              office,
              'OfficeDataUpdated',
              officeId.toString()
            );

            res.status(200).send('File uploaded!');
          });
      }
    });
  });
});

/**
 * ENDPOINT: GET /offices/:id/takephoto
 * 
 * This endpoint sends a fcmMessage to the office matching :officeId. The 
 * panel then starts to take a spion picture 
 * 
 * @param {*} req request
 * @param {*} res response
 * 
 */
router.get('/:officeId/takephoto', auth.authFun(), (req, res) => {
  const officeId = parseInt(req.params.officeId, 10);

  authenticateAnyOfficeDevice(officeId, req, res).then(office => {
    notifyPanel(office, 'takephoto');
  });

  return res.status(200).send('takephoto fcm event successfully sent.');
});

module.exports = router;
