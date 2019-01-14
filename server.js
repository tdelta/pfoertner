// Get needed express module
const express = require('express');
const server = express();

// Get our own models
const models = require('./models/models.js');

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
  .then(() => server.listen(3031));

// START OF ENDPOINTS:

// Define Endpoint: /
server.get('/', function (req, res) {
    res.send('Hello World');    
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

//Define Endpoit: /office
server.post('/office',function(req,res){
        
    models.Office.create({RoomNumber: 'Audimax'})
    .then(() => res.send('Office was created'));
});

server.get('/offices', function(req,res){
    models.Office.findAll().then(offices => {
        res.send(offices);
    });
});

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

    models.Device.findById(id)
      .then(device => {
        if(device == null) {
          res.status(401).json({message:'No such device.'});
        }

        console.log(device);
        console.log(password);

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

server.post(
  '/offices',
  passport.authenticate('jwt', { session: false }),
  (req, res) => {
    var joinCode = 'HalloWelt';

    const device = req.user;
    console.log(device);

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


server.post('/offices/:id/member', function(req,res){

});
