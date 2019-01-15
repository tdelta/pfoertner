// Get needed express module
const express = require('express');
const server = express();

// Get our own models
const models = require('./models/models.js');

// Get interface to firebase
const firebase = require('./firebase/firebase.js');

// setup authentication
var passport = require('passport');
var auth = require('./auth.js');

var passportStrategy = auth.getStrategy(
  'This is the secret key of server',
  deviceId => {
    const device = models.Device.findById(deviceId);

    return device == null ?
      undefined : device;
  }
);

passport.use(passportStrategy);
server.use(passport.initialize());

// Get needed bodyparser module
const bodyParser = require('body-parser');

// Get our own database module
const db = require('./database.js');

server.use(bodyParser.urlencoded({extended: true}));
server.use(bodyParser.json());

// Listen on port 3030 localhost
db.sequelize.sync()
  .then(() => server.listen(3000));

// Connect to firebase 
firebase.initialize();

// START OF ENDPOINTS:

// Define Endpoint: /
server.get('/', function (req, res) {
    res.send('HelloWorld');    
});

// Define Endpoint: /somepost
server.post('/somepost', function(req, res){

    // On this way ill get the value of the attribute name of the request json
    var name = req.body.name;

    // On this way ill create a new json with the saved name
    var myJson = {
        'Vogel' : name 
    }

    res.send(myJson);
});

//Define Endpint: /sendErrorCode
server.get('/sendErrorCode',function(req,res){

    // This is how you can set the http response status code 
    res.status(400);
    // You still can give it a message.
    res.send('This is a 400 Error Code created by Marc');
})

server.get('/offices', function(req,res){
    models.Office.findAll().then(offices => {
        res.send(offices);
    });
});

server.get(
  '/offices/:officeId/members',
  passport.authenticate('jwt', { session: false }),
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

server.post(
  '/offices/:officeId/members',
  passport.authenticate('jwt', { session: false }),
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
            notifyPanel(office.id);
            res.status(200);
            res.send('Successfully joined office');
        } else {
            // Join code is incorrect
            res.status(401);
            res.send('Office join code is incorrect');
        }
    });
});

server.get(
  '/offices/:id',
  passport.authenticate('jwt', { session: false }),
  (req, res) => {
    if (req.params.id == null) {
      res.status(400).send({message: 'The given id is invalid.'});
    }

    const device = req.user;
    const officeId = parseInt(req.params.id, 19);

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

server.put(
  '/offices/:officeId/members',
  (req, res) => {
    if (req.params.officeId == null) {
      res.status(400).send({message: 'The given office id is invalid.'});
    }

    else if (req.body.joinCode == null) {
      res.status(400).send({message: 'You must provide a valid joinCode.'});
    }

    else {
      const officeId = parseInt(req.params.officeId, 10);
      const joinCode = req.body.joinCode;

      models.Office.findById(officeId).then(
        office => {
          if(office) {
            if(office.joinCode === joinCode){
                // Create office member that is connected to the logged in device
                // and the office.
                createOfficeMember( // TODO: Asynchronität beachten
                    req.body.firstName,
                    req.body.lastName,
                    req.user,
                    office
                );

                // Send fcm notification
                notifyPanel(office.id);

                res.status(200);
                res.send('Successfully joined office');
            } else {
                // Join code is incorrect
                res.status(401);
                res.send('Office join code is incorrect');
            }
          }
          
          else {
            // Office was not found
            res.status(404);
            res.send(`Office with id ${req.params.officeId} does not exist`);
          }
        }
      );
    }
  }
);

function notifyPanel(officeId){
    models.Device.findOne({
        where:{
            OfficeId: officeId
        }
    }).then(device => {
        if(device.fcmToken){
            firebase.sendData(device.fcmToken,
                {'event': 'AdminJoined'}
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

server.put('/office', function(req, res){
  models.Office.update(
    {RoomNumber: req.body.name},
    {
        where: {id : 1}
    }
  );
});

server.post('/devices/:id/authToken', (req, res) => {
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
            auth.genToken(device)
          );
        }
        
        else {
          res.status(401).json({message:'Incorrect password.'});
        }
      });
  }
});

server.post('/devices', (req, res) => {
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

server.patch(
  '/devices/:id/fcmToken',
  passport.authenticate('jwt', { session: false }),
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

server.post(
  '/offices',
  passport.authenticate('jwt', { session: false }),
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

server.get(
    '/devices/:id',
    passport.authenticate('jwt', { session: false }),
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
