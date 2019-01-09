// Get needed express module
const express = require('express');
// Get needed bodyparser module
const bodyParser = require('body-parser');
// Get needed mysql module
const db = require('mysql');

// Run server
const server = express();

// Use the bodyparser module in our server
// We are not using server.use(bodyParser()) because the constructor is 
// deprecated:
// https://stackoverflow.com/questions/24330014/bodyparser-is-deprecated-express-4
server.use(bodyParser.urlencoded({extended: true}));
server.use(bodyParser.json());

// Listen on port 3030 localhost
server.listen(3031);

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
    res.status(400)
    // You still can give it a message.
    res.send('This is a 400 Error Code created by Marc')

})

var conncection = db.createConnection({ // Open a new connection                                                                                                                                           
    user: 'root',
    host: '127.0.0.1',
    port: 3306,
    password: 'GEHEIM',
    // Without socketPath the connection alway refused.
    // Solutio found on: https://stackoverflow.com/questions/30266221/node-js-mysql-error-connect-econnrefused
    socketPath: '/run/mysqld/mysqld.sock'
});

conncection.connect(function(err){
    if(err){
        console.log('Something went wrong.' + err.stack);
    } else{
        console.log('connected');
    }
});