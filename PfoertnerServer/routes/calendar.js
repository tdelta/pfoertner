// Get the express module and use Router
var express = require('express');
var router = express.Router();

// Get the required models
var models = require('../models/models.js');

var notify = require('../notify.js');
var notifyOfficeSubscribers = notify.notifyOfficeSubscribers;

var authenticatePanelOrOwner = require('../deviceAuth.js')
  .authenticatePanelOrOwner;

var auth = require('../authInit.js');

var clientSecret = require('../client_secret.json');

var { google } = require('googleapis');
var { UserRefreshClient } = require('google-auth-library');


const CALENDAR_TITLE = "Office hours";

/**
 * ENDPOINT: POST /calendar/:id/token
 * 
 * Authorize calendar access by sending an access token here from the admin app
 * 
 * @param {*} req request containing an access token
 * @param {*} res response 
 * 
 */
router.post('/:id/token', auth.authFun(), (req, res) => {
  authenticatePanelOrOwner(req, res).then(officemember => {
    const auth = new google.auth.OAuth2(
      clientSecret.web.client_id,
      clientSecret.web.client_secret
    );
    auth.getToken(req.body.serverAuthCode).then(tokens => {
      models.GoogleOAuthToken.create({
        'refreshToken': tokens.tokens.refresh_token,
        'accessToken': tokens.tokens.access_token,
        'expiryDate': new Date(tokens.tokens.expiry_date),
      }).then(token => {
        token.setOfficeMember(officemember);

        // Get Google email and save it to the database
        auth.verifyIdToken({
          idToken: tokens.tokens.id_token,
          audience: clientSecret.web.client_id
        }).then(ticket => {
          const email = ticket.getPayload()['email'];

          officemember.update({email: email}).then(officemember => {
            officemember.getOffice().then(office => {
              if (office != null) {
                // TODO optimize to only update the affected member
                notifyOfficeSubscribers(
                  office,
                  'OfficeMemberUpdated',
                  req.params.id
                );
              } else {
                console.log(
                  'Could not notify office members, that one of them got updated'
                );
              }
            });
          });

          res.status('200').send('Successfully authorized calendar access');
        });
      });
    })
  })
});

/**
 * ENDPOINT: GET /calendar/:id
 * 
 * Retrieve calendar events for office member corresponding to the given id
 * 
 * @param {*} req request object
 * @param {*} req.params.id office member id
 * @param {*} req.query.until cutoff time - events with later start time will not be included
 *     if this is not provided, the Google calendar API will probably return the next 250 events
 * @param {*} res response 
 * 
 */
router.get('/:id', auth.authFun(), (req, res) => {
  authenticatePanelOrOwner(req, res).then(officemember => {
    getValidAccessToken(officemember).then(auth => {
      const calendarAPI = google.calendar({version: 'v3', auth});
      const until = Date.parse(req.query.until);
      const untilStr = until ? new Date(until).toISOString() : undefined;
      findCalendarId(calendarAPI).then(calendarId => {
        calendarAPI.events.list({
          calendarId: calendarId,
          timeMin: new Date().toISOString(),
          timeMax: untilStr,
          singleEvents: true,
          orderBy: 'startTime',
        }).then(events => {
          timeslots = events.data.items.map(e => eventToTimeslot(e, req.params.id))
          res.status('200').send(timeslots);
        });
      });
    }).catch(error => {
      console.error('Error getting access token', error);
      res.status('401').send('Unable to authorize Google Calendar access');
    })
  })
});

function eventToTimeslot(calendarEvent, officeMemberId) {
  var start;
  var end;
  if (calendarEvent.start.dateTime && calendarEvent.end.dateTime) {
    // Event has start and end with time information
    start = new Date(calendarEvent.start.dateTime);
    end = new Date(calendarEvent.end.dateTime);
  }
  else if (calendarEvent.start.date && calendarEvent.end.date) {
    // Event is all day, no time information
    start = new Date(calendarEvent.start.date);
    end = new Date(calendarEvent.end.date);

    // Set the start and end times to midnight using the timezone of the server
    // This will not work if the apps have different time zones
    // but whole day events don't make sense for our application anyways
    start.setHours(0,0,0);
    end.setHours(23,59,59);
  }
  else {
    // There shouldn't be any other cases, but the API docs aren't super clear
    throw new Error("Unable to parse calendar event "+calendarEvent)
  }
  return {
    id: calendarEvent.id,
    start: start.toISOString(),
    end: end.toISOString(),
    OfficeMemberId: officeMemberId
  }
}

async function findCalendarId(calendarAPI) {
  const response = await calendarAPI.calendarList.list({
    minAccessRole: 'writer'
  });
  const calendar = response.data.items.find(calendar => calendar.summary == CALENDAR_TITLE);
  if (calendar) {
    return calendar.id;
  }

  // Calendar does not exist, create it
  calendar = await calendarAPI.calendars.insert({
    requestBody: {
      summary: CALENDAR_TITLE
    }
  });
  return calendar.id;
}

async function getValidAccessToken(officemember) {
    const tokenData = await officemember.getGoogleOAuthToken();

    if (!tokenData || !tokenData.refreshToken) {
      throw new Error("No refresh token available");
    }

    const auth = new UserRefreshClient({
          clientId: clientSecret.web.client_id,
          clientSecret: clientSecret.web.client_secret,
          refreshToken: tokenData.refreshToken,
    });

    if (tokenData.accessToken && tokenData.expiryDate && Date.now() < tokenData.expiryDate) {
      // Access token is still valid
      auth.setCredentials({
        refreshToken: tokenData.refreshToken,
        access_token: tokenData.accessToken,
        expiry_date: tokenData.expiryDate
      });
      return auth;
    }

    // Refresh the token
    const response = await auth.refreshToken();

    // Update DB with new access token and expiry date
    tokenData.accessToken = response.tokens.access_token;
    tokenData.expiryDate = response.tokens.expiry_date;
    await tokenData.save();

    return auth;
}

module.exports = router;
