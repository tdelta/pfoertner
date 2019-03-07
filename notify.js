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
  console.info('About to notify all members of office ' + office.id + ' as well as its panel to send event ' + eventName + ' with payload ' + payload);
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
      console.info(
        'Notifying panel for office ' + office.id + '. This is the message: ',
        message
      );

      firebase.sendData(device.id, device.fcmToken, message);
    }

    else {
      console.error('Could not notify panel ' + device.id + ' of office ' + office.id + ' since it has no fcm token set.');
    }
  });

  office.getOfficeMembers().then(officeMembers => {
    officeMembers.forEach(member => {
      member.getDevice().then(device => {
        if (device.fcmToken != null) {
          console.info(
            'Notifying office member ' +
              member.id +
              ' of device ' +
              device.id +
              ' for office ' +
              office.id +
              '. This is the message: ',
            message
          );
          firebase.sendData(device.id, device.fcmToken, message);
        } else {
          console.error(
            'Could not notify office member ' + member.id + ', since it did not set an fcm token.'
          );
        }
      });
    });
  });
}

function notifyPanel(office, eventName, payload) {
  console.info('About to notify just the panel of office ' + office.id + ' to send event ' + eventName + ' with payload ' + payload);

  let message;

  message = {
    event: eventName,
  };

  office.getDevice().then(paneldevice => {
    if (paneldevice.fcmToken != null) {
      console.info(
        'Notifying panel device ' + paneldevice.id + ' using fcmToken ' + paneldevice.fcmToken + ' and the following message:', message
      );

      firebase.sendData(paneldevice.id, paneldevice.fcmToken, message);
    }

    else {
      console.error('Failed to notify panel device ' + paneldevice.id + ' of office ' + office.id + ' since it has no fcm token set.');
    }
  });
}

module.exports = {
  notifyOfficeSubscribers: notifyOfficeSubscribers,
  notifyPanel: notifyPanel,
};
