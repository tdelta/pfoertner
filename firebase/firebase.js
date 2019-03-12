var admin = require('firebase-admin');

var serviceAccount = require('./firebase-secret.json');

module.exports.initialize = function() {
  console.info('Initializing firebase connection');

  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    databaseURL: 'https://pfoertner-2b302.firebaseio.com',
  });
};

sendMessage = function(deviceId, deviceToken, message) {
  console.info("Sending message to device " + deviceId + ': ', message);

  admin
    .messaging()
    .send(message)
    .then(response => {
      console.info('Successfully sent message:', response);
    })
    .catch(error => {
      console.error('Error sending message to device ' + deviceId + ' with token ' + deviceToken + ':', error);
    });
};

module.exports.sendNotification = function(
  deviceId,
  deviceToken,
  title,
  body,
  activity,
  buttons,
  data
) {
  let notification = {
    title: title,
    body: body,
    buttons: buttons,
    activity: activity,
    data: data,
  };

  notification = JSON.stringify(notification);

  const message = {
    token: deviceToken,
    data: {
      notification: notification,
    },
  };

  sendMessage(deviceId, deviceToken, message);
};

// sends data to the app. this is not displayed as a notification when the app is in the background
module.exports.sendData = function(deviceId, deviceToken, data) {
  var message = {
    token: deviceToken,
    data: data,
  };
  sendMessage(deviceId, deviceToken, message);
};
