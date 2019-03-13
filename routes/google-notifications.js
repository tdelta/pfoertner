const firebase = require('../firebase/firebase.js');
const models = require('../models/models.js');

const express = require('express');
const router = express.Router();

router.post('/', (req, res) => {
  console.log('Received webhook notification from google, Yay');
  let channelId = req.header('X-Goog-Channel-ID');
  if (channelId) {
    models.Device.findOne({ where: { webhookId: channelId } }).then(device => {
      firebase.sendData(device.id, device.token, { event: 'CalendarUpdated' });
    });
  }
});

module.exports = router;
