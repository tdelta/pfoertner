// Get needed express module
const express = require('express');
// Get needed bodyparser module
const bodyParser = require('body-parser');

// Run server
const server = express();

// Get our own database module
const db = require('./database.js');

// Get our own models
const models = require('./models/models.js');

// Get interface to firebase servers
const firebase = require('./firebase/firebase.js');

// Use the bodyparser module in our server
// We are not using server.use(bodyParser()) because the constructor is 
// deprecated:
// https://stackoverflow.com/questions/24330014/bodyparser-is-deprecated-express-4
server.use(bodyParser.urlencoded({extended: true}));
server.use(bodyParser.json());

// Listen on port 3030 localhost
db.sequelize.sync()
.then(() => server.listen(3031));

// Connect to firebase 
firebase.initialize();

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

server.post('/offices/:officeId/member',function(req,res){
    models.Office.findById(req.params.officeId).then(office => {
      if(office){
          if(office.joinCode === req.body.joinCode){
              // TODO Add user to office
              res.status(200);
              res.send('Successfully joined office');
          } else {
              // Join code is incorrect
              res.status(401);
              res.send('Office join code is incorrect');
          }
        } else {
          // Office was not found
          res.status(404);
          res.send(`Office with id ${req.params.officeId} does not exist`);
        }
    }
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

server.post('/offices', function(req, res){

    var joinCode = 'HalloWelt';

    models.Office.create({joinCode: joinCode})
    .then((result) => res.send(result));
});
