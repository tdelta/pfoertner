var express = require('express');
var router = express.Router();

var auth = require('../authInit.js');

var models = require('../models/models.js');

router.post('/', (req, res) => {
    if (req.body.password == null){
      res.status(400).json({
        message: 'Password needed.'
      });
    }
  
    else {
      const password = req.body.password;
  
      models.Device.create({
        password: password
      })
        .then((result) => res.send(result));
    }
  });


router.patch(
    '/:id/fcmToken',
    auth.authFun(),
    (req, res) => {
        if (req.body.fcmToken == null) {
        res.status(400).send({message: 'You need to provide a new fcm token.'});
        }

        else {
            const device = req.user;

        if (parseInt(req.params.id, 10) === device.id) {
            const fcmToken = req.body.fcmToken;

            device.update({
            fcmToken: fcmToken
            })
            .then(() => res.send(device));
        }

        else {
            res.status(401).send({message: 'You cannot access devices but your own.'});
        }
        }
    }
);

router.get(
    '/:id',
    auth.authFun(),
    function(req,res)
{
    const deviceId = parseInt(req.params.id, 10);
    const device = req.user;

    if (device.id !== deviceId) {
        res.status(401).send({message: 'You can not access information of other devices, but only your own device.'})
    }

    else {
        models.Device.findById(deviceId)
            .then(result => res.send(result));
    }
});

router.post(
    '/:id/authToken',
    (req, res) => {
        if (req.params.id == null) {
          res.status(401).json({message:'No device id given.'});
        }
      
        else if (req.body.password == null) {
          res.status(401).json({message:'No password given.'});
        }
      
        else  {
          const id = parseInt(req.params.id, 10);
          const password = req.body.password;
      
          models.Device.findByPk(id)
            .then(device => {
              if(device == null) {
                res.status(401).json({message:'No such device.'});
              }
      
              if(device.password === password) {
                res.json(
                  auth.auth.genToken(device)
                );
              }
              
              else {
                res.status(401).json({message:'Incorrect password.'});
              }
            });
        }
});

module.exports = router;