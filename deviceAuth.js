var models = require('./models/models.js');

/**
 * fullfills a promise with the requested officemember, only if the requestor is
 * authenticated as a panel and the requested member belongs to the corresponding office
 * @result a promise with an officemember
 */
exports.authenticatePanelOrOwner = function(req, res) {
  return new Promise(response => {
    const officememberid = parseInt(req.params.id, 10);

    // Get the officemember matching the given id
    models.OfficeMember.findById(officememberid).then(member => {
      // If no officemember with this id is found, return 404
      if (member == null) {
        res.status('404').send('There is no person to your id');
      }
      // There is an officemember matching the id
      else {
        member.getOffice().then(office => {
          if (office == null) {
            res
              .status('401')
              .send(
                'The requested office member does not belong to any office'
              );
          } else if (office.id === req.user.OfficeId) {
            // The office of the requested office member matches the office id
            // of the requesting device (the panel)
            response(member);
          } else {
            req.user.getOfficeMember().then(officeMember => {
              if (officeMember.OfficeId === office.id) {
                // The requesting device is also member in the office the requested office member belongs to
                response(member);
              } else {
                res
                  .status('401')
                  .send('You are not allowed to access this office member');
              }
            });
          }
        });
      }
    });
  });
};

exports.authenticateAnyOfficeDevice = function(officeId, req, res) {
  return new Promise(response => {
    if (req.user == null) {
      res
        .status('401')
        .send(
          'Authentication token is not present or malformed.'
        );
    }

    else if (officeId == null) {
      res
        .status('401')
        .send(
          'Office id is malformed: ' + officeId
        );
    }

    else {
      models.Office.findById(officeId).then(office => {
          if (office == null) {
            res.status('404').send('There is no office to your id');
          }

          else {
            // Device is the panel of the Office
            if (req.user.OfficeId === officeId) {
              response(office);
            }

            else {
              req.user.getOfficeMember().then(officeMember => {
                if (officeMember != null) {
                  // Device is OfficeMember of the office.
                  if (officeMember.OfficeId === officeId) {
                    response(office);
                  }

                  else {
                    res
                      .status('401')
                      .send(
                        'You are not authorized for this office since your device belongs to office ' + officeMember.OfficeId + ' not office ' + officeId + '.'
                      );
                  }
                }

                else {
                  res
                    .status('401')
                    .send(
                      'You are not authorized for this office since your device is not part of any office.'
                    );
                }
              });
            }
          }
      });
    }
  });
};

exports.authenticateOwner = function authenticateOwner(
  req,
  res,
  officememberIdParam
) {
  return new Promise(function(response) {
    // Check whether there is a valid officeId in
    // the request
    if (officememberIdParam == null) {
      res.status(400).send({ message: 'The given id is invalid.' });
    }

    const device = req.user;
    // The request do not have an correct authorization header
    if (device === null) {
      res.status(401).send({
        message: 'You do not have the permission to access this officemember',
      });
    }
    // The request do have an correct authorization header
    else {
      const officeMemberId = parseInt(officememberIdParam, 10);

      device.getOfficeMember().then(loggedIn => {
        // Check whether a officemember belongs to the authorized device
        // and whether that officemember is a part of the office
        if (loggedIn && loggedIn.id === officeMemberId) {
          console.log('Office member authenticated');
          response(loggedIn);
        }
        // No user belongs to the device or the user belonging to the device
        // is not the authenticated office member
        else {
          res.status(401).send({
            message:
              'You do not have the permission to access this officemember.',
          });
        }
      });
    }
  });
};
