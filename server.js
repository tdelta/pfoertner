// Get needed express module
const express = require('express');
const server = express();

// setup authentication
var passport = require('passport');
var auth = require('./auth.js');

var passportStrategy = auth.getStrategy(
  'This is the secret key of server',
  deviceId =>
    // find device by id. To be implemented

    return undefined;
  }
);

passport.use(passportStrategy);
server.use(passport.initialize());

// Get needed bodyparser module
const bodyParser = require('body-parser');

// Get our own database module
const db = require('./database.js');

// Get our own models
const models = require('./models/Office.js');

server.use(bodyParser.urlencoded({extended: true}));
server.use(bodyParser.json());

// Listen on port 3030 localhost
db.sequelize.sync()
  .then(() => server.listen(3031));

// START OF ENDPOINTS:

// Define Endpoint: /
server.get('/', function name(req, res) {
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
        {RoomNumber: req.body.name}
    ,
    {
        where: {id : 1}
    }
    )
});


app.post('/device/authToken', (req, res) => {
  if (req.body.name == null) {
    res.status(401).json({message:'No user name given.'});
  }

  else if (req.body.password == null) {
    res.status(401).json({message:'No password given.'});
  }

  else  {
    const name = req.body.name;
    const password = req.body.password;

    const device = findDeviceByName(name); // Todo

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
