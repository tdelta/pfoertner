// Get interface to firebase
const firebase = require('./firebase/firebase.js');

/**
 * This function sends a fcmMessage to all officeMembers of
 * a given office
 *
 * @param {*} office office of which officemembers will receive the message
 * @param {*} eventName eventName of the message
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

/**
 * This function send a fcmMessage to a (via parameters) given panel
 * 
 * @param {*} office office of the panel which shall be notified 
 * @param {*} eventName eventName of the event which will be send to the panel 
 * @param {*} payload payload of the message which will be send to the panel
 */
function notifyPanel(office, eventName, payload) {
  let message;

  message = {
    event: eventName,
  };

  office.getDevice().then(paneldevice => {
    if (paneldevice.fcmToken != null) {
      console.log(
        'Notifying panel device with this following message:' + message
      );
    }
    firebase.sendData(paneldevice.fcmToken, message);
  });
}

module.exports = {
  notifyOfficeSubscribers: notifyOfficeSubscribers,
  notifyPanel: notifyPanel,
};
