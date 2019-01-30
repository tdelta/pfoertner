// Get interface to firebase
const firebase = require('./firebase/firebase.js');

/**
 * TODO: MARTIN FRAGEN
 *
 *
 * @param {*} office
 * @param {*} eventName
 */
function notifyOfficeSubscribers(office, eventName, /* optional */ payload) {
  let message;

  if (payload != null) {
    message = {
      event: eventName,
      payload: payload,
    };
  } else {
    message = { event: eventName };
  }

  office.getDevice().then(device => {
    if (device.fcmToken) {
      console.log(
        'Notifying panel for office ' + office.id + '. This is the message: ',
        message
      );
      firebase.sendData(device.fcmToken, message);
    }
  });

  office.getOfficeMembers().then(officeMembers => {
    officeMembers.forEach(member => {
      member.getDevice().then(device => {
        if (device.fcmToken != null) {
          console.log(
            'Notifying office member ' +
              member.id +
              ' of device ' +
              device.id +
              ' for office ' +
              office.id +
              '. This is the message: ',
            message
          );
          firebase.sendData(device.fcmToken, message);
        } else {
          console.log(
            'Could not notify an office member, since it did not set an fcm token.'
          );
        }
      });
    });
  });
}

module.exports = {
  notifyOfficeSubscribers: notifyOfficeSubscribers,
};
