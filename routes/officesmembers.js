// Get the express module and use Router
var express = require('express');
var router = express.Router();

// Get the required models
var models = require('../models/models.js');



// ONLY FOR DEBUGING/TESTING PURPOSES. REMOVE FOR FINAL SUBMISSION
// List all users created in the database
router.get('/', (req, res) => {
  models.OfficeMember.findAll().then(officesmembers =>
    res.send(officesmembers)
  );
});

// ONLY FOR DEBUGING/TESTING PURPOSES. REMOVE FOR FINAL SUBMISSION
// Return a specific user (which matches a id)
router.get('/:id', (req, res) =>
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
 * ENDPOINT: PATCH /officemembers/:id/picture
 * 
 * Updates the picture of the officemember
 * DARK JAVASCRIPT MAGIC!
 */
router.patch('/:id/picture', (req, res) => {

  let picture = req.files.picture;
  picture.mv('uploads/'+ req.params.id + '.jpg', function(err){

    if(err){
      return res.status(500).send(err);
    } else{
      res.status(200).send('File uploaded!');
    }
  });
})

router.get('/:id/picture', (req, res) => {
  res.sendFile('uploads/' + req.params.id + '.jpg');
})

module.exports = router;
