var admin = require('firebase-admin');

var serviceAccount = require('./firebase-secret.json');

module.exports.initialize = function() {
  console.log('Initializing firebase connection');
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    databaseURL: 'https://pfoertner-2b302.firebaseio.com',
  });
};

sendMessage = function(deviceToken, message) {
  admin
    .messaging()
    .send(message)
    .then(response => {
      console.log('Successfully sent message:', response);
    })
    .catch(error => {
      console.log('Error sending message:', error);
    });
};

module.exports.sendNotification = function(
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
    intent: activity,
    data: data,
  };
  notification = JSON.stringify(notification);

  var message = {
    token: deviceToken,
    data: {
      notification: notification,
    },
  };
  sendMessage(deviceToken, message);
};

// sends data to the app. this is not displayed as a notification when the app is in the background
module.exports.sendData = function(deviceToken, data) {
  var message = {
    token: deviceToken,
    data: data,
  };
  sendMessage(deviceToken, message);
};
