var express = require('express');
var router = express.Router();

var auth = require('../authInit.js');

var models = require('../models/models.js');

// Get interface to firebase
const firebase = require('../firebase/firebase.js');

var notify = require('../notify.js');
var notifyDevice = notify.notifyDevice;


/**
 * ENDPOINT: POST /devices/
 * 
 * Creates a new device
 * 
 * @param {*} req request contains needed password
 * @param {*} res response contains the newly created device object
 * 
 */
router.post('/', (req, res) => {
  if (req.body.password == null) {
    res.status(400).json({
      message: 'Password needed.',
    });
  } else {
    const password = req.body.password;

    models.Device.create({
      password: password,
    }).then(result => res.send(result));
  }
});

/**
 * ENDPOINT: PATCH /devices/:id/fcmToken
 * 
 * This endpoint sets a new fcmToken for the device matching :id
 * 
 * @param {*} req request contains the fcmToken which is to be set
 * @param {*} res response
 * 
 */
router.patch('/:id/fcmToken', auth.authFun(), (req, res) => {
  const device = req.user;

  if (req.body.fcmToken == null) {
    console.error(
      'Failed to set fcm token of device ' +
        device.id +
        ' since there was no token info in the body of the request.'
    );

    res.status(400).send({ message: 'You need to provide a new fcm token.' });
  } else {
    const targetId = parseInt(req.params.id, 10);

    if (targetId === device.id) {
      const fcmToken = req.body.fcmToken;

      device
        .update({
          fcmToken: fcmToken,
        })
        .then(updatedDevice => {
          console.info(
            'Saved new FCM token ' +
              updatedDevice.fcmToken +
              ' for device ' +
              updatedDevice.id
          );

          notifyDevice(
            updatedDevice,
            'DeviceUpdated',
            updatedDevice.id.toString()
          );

          res.send(device);
        });
    } else {
      console.error(
        'Setting the fcm token for device ' +
          device.id +
          ' failed, since it tried to set the token of device ' +
          targetId +
          ', not its own.'
      );

      res
        .status(401)
        .send({ message: 'You cannot access devices but your own.' });
    }
  }
});

/**
 * ENDPOINT: GET /devices/:id/
 * 
 * This endpoint returns the device matching :id
 * 
 * @param {*} req request
 * @param {*} res response contains the device object matching :id
 * 
 * 
 */
router.get('/:id', auth.authFun(), function(req, res) {
  const deviceId = parseInt(req.params.id, 10);
  const device = req.user;

  if (device.id !== deviceId) {
    res.status(401).send({
      message:
        'You can not access information of other devices, but only your own device.',
    });
  } else {
    models.Device.findByPk(deviceId).then(result => res.send(result));
  }
});

/**
 * ENDPOINT: POST /devices/:id/authToken
 * 
 * This endpoint creates a new authToken and sends it to the requesting device
 * 
 * @param {*} req request contains password to create a authToken
 * @param {*} res response contains the requested, newly generated authToken
 * 
 */
router.post('/:id/authToken', (req, res) => {
  if (req.params.id == null) {
    res.status(401).json({ message: 'No device id given.' });
  } else if (req.body.password == null) {
    res.status(401).json({ message: 'No password given.' });
  } else {
    const id = parseInt(req.params.id, 10);
    const password = req.body.password;

    models.Device.findByPk(id).then(device => {
      if (device == null) {
        res.status(401).json({ message: 'No such device.' });
      }

      if (device.password === password) {
        res.json(auth.auth.genToken(device));
      } else {
        res.status(401).json({ message: 'Incorrect password.' });
      }
    });
  }
});

module.exports = router;
