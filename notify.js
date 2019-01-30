// Get interface to firebase
const firebase = require('./firebase/firebase.js');

/**
 * TODO: MARTIN FRAGEN
 *
 *
 * @param {*} office
 * @param {*} eventName
 */
function notifyOfficeSubscribers(office, eventName) {
  office.getDevice().then(device => {
    if (device.fcmToken) {
      firebase.sendData(device.fcmToken, { event: eventName });
    }
  });

  office.getOfficeMembers().then(officeMembers => {
    officeMembers.forEach(member => {
      member.getDevice().then(device => {
        if (device.fcmToken != null) {
          firebase.sendData(device.fcmToken, { event: eventName });
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
