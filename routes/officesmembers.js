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
  const officememberid = parseInt(req.params.id, 10);

  picture.mv('uploads/' + req.params.id + '.jpg', function(err) {
    if (err) {
      return res.status(500).send(err);
    } else {
      models.OfficeMember.findById(officememberid).then(officemember => {
        const newEmailValue = '/uploads/' + req.params.id + '.jpg';
        officemember.update({picture: newEmailValue});
        res.status(200).send('File uploaded!');
      });
    }
  });
});

/**
 * ENDPOINT: GET /officemembers/
 *
 * Get the picture of the officemember
 *
 */
router.get('/:id/picture', (req, res) => {
  const officememberid = parseInt(req.params.id, 10);

  // Get the officemember matching the given id
  models.OfficeMember.findById(officememberid).then(member => {
    // If no officemember with this id is found, return 404
    if (member == null) {
      res.status('404').send('There is no person to your id');
    }
    // There is an officemember matching the id
    else {
      // Check wether there is a picture connected to the requested officemember
      if (member.picture == null) {
        // If there is no picture, return 404
        res.status('404').send('There is no picture to your person');
      } else {
        // If there is a picture, return 200 and the picture
        res.sendFile('/' + req.params.id + '.jpg', { root: 'uploads' });
      }
    }
  });
});

module.exports = router;