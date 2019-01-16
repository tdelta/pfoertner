var express = require('express');
var router = express.Router();

var models = require('../models/models.js');
// Get interface to firebase
const firebase = require('../firebase/firebase.js');

var util = require('util');

var auth = require('../authInit.js');
console.log(util.inspect(auth));
console.log(util.inspect(auth.authFun));
console.log(util.inspect(auth.authFun()));


router.post(
    '/',
    auth.authFun(),
    (req, res) => {
      var joinCode = 'Hallo Welt';
  
      const device = req.user;
  
      models.Office.create({joinCode: joinCode})
          .then(
              office => {
                  device.setOffice(office).then(() => {
                      res.send(office);
                  });
              }
          );
    }
  );


function notifyPanel(office,eventName){
    office.getDevice().then(device => {
        if(device.fcmToken){
            firebase.sendData(device.fcmToken,
                {'event': eventName}
            );
        }
    });
}

function createOfficeMember(firstName, lastName, device, office){
    models.OfficeMember.create(
        {
            firstName: firstName,
            lastName: lastName
        }
    ).then(officeMember => {
        officeMember.setDevice(device);
        officeMember.setOffice(office);
    });
}

function authenticateOfficeMember(req,res) {
  return new Promise(function(response){
      if (req.params.officeId == null) {
        res.status(400).send({message: 'The given id is invalid.'});
      }
  
      const device = req.user;
      const officeId = parseInt(req.params.officeId, 10);
  
      device.getOfficeMember().then(loggedIn => {
          if(
               loggedIn != null && loggedIn.OfficeId === officeId
            || device.OfficeId === officeId
          ){
              console.log('Office member authenticated');
              response();
          } else {
              res.status(401).send({message: 'You do not have the permission to access this office.'});
          }
      });
  });
}

router.get(
    '/:officeId',
    auth.authFun(),
    (req, res) => {
      if (req.params.officeId == null) {
        res.status(400).send({message: 'The given id is invalid.'});
      }
  
      const device = req.user;
      const officeId = parseInt(req.params.id, 10);
  
      device.getOfficeMember().then(loggedIn => {
          if(
               loggedIn != null && loggedIn.OfficeId === officeId
            || device.OfficeId === officeId
          ) {
            models.Office
              .findById(officeId)
              .then(
                office => {
                  res.send(office);
                }
              );
          }
  
          else {
            res.status(401).send({message: 'You do not have the permission to access this office.'});
          }
      });
    }
);

router.patch(
    '/:officeId',
    auth.authFun(),
    (req, res) => {
        console.log('Patching office');
        authenticateOfficeMember(req,res).then(() => {
            findOffice(req,res).then(office => {
              console.log(office);
                office.update(req.body).then(office => {
                    res.status(200);
                    res.send(office);
                    notifyPanel(office,'OfficeDataUpdated');
                });
            });
        });
    });


router.post(
    '/:officeId/members',
    auth.authFun(),
    (req,res) => {
      findOffice(req,res).then(office => {
          if(office.joinCode === req.body.joinCode){
              // Create office member that is connected to the logged in device
              // and the office.
              createOfficeMember(
                  req.body.firstName,
                  req.body.lastName,
                  req.user,
                  office
              );
              // Send fcm notification
              notifyPanel(office,'AdminJoined');
              res.status(200);
              res.send('Successfully joined office');
          } else {
              // Join code is incorrect
              res.status(401);
              res.send('Office join code is incorrect');
          }
      });
  });


router.get(
    '/:officeId/members',
    auth.authFun(),
    (req,res) => {
      if (req.params.officeId == null) {
        res.status(400).send({message: 'Invalid office id.'});
      }
  
      else {
        findOffice(req,res).then(office => {
            office.getOfficeMembers().then(officeMembers => {
                const device = req.user;
                const selectedOfficeId = parseInt(req.params.officeId, 10);
  
                device.getOfficeMember().then(loggedIn => {
                    if(
                         loggedIn != null && loggedIn.OfficeId === selectedOfficeId
                      || device.OfficeId === selectedOfficeId
                    ) {
                      res.status(200);
                      res.send(officeMembers);
                    } else {
                      res.status(401);
                      res.send('Authorized user is not in requested office');
                    }
                });
            });
        });
      }
  });

  function findOffice(req,res){
    return new Promise(function(response){
        models.Office.findById(req.params.officeId).then(office => {
            if(office){
                response(office);
            } else {
                res.status(404);
                res.send(`Office with id ${req.params.officeId} does not exist`);
            }
        });
    });
}

module.exports = router;
