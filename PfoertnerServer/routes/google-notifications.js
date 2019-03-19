const firebase = require('../firebase/firebase.js');
const models = require('../models/models.js');

const express = require('express');
const router = express.Router();

router.post('/', (req, res) => {
  console.log('Received webhook notification from google');
  let channelId = req.header('X-Goog-Channel-ID');
  if (channelId) {
    models.OfficeMember.findOne({ where: { webhookId: channelId } }).then(officemember => {
      officemember.getOffice().then(office => {
        office.getDevice().then(device => {
          firebase.sendData(device.id, device.fcmToken, {
            event: 'CalendarUpdated',
            payload: officemember.calendarId
          });
        });
      });
    });
  }
  res.status(200).send();
});

module.exports = router;
