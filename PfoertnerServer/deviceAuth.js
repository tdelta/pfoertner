var models = require('./models/models.js');

/**
 * fullfills a promise with the requested officemember, only if the requestor is
 * authenticated as a panel and the requested member belongs to the corresponding office
 * @result a promise with an officemember
 */
exports.authenticatePanelOrOwner = function(req, res, id) {
  return new Promise(response => {
    let officememberid;
    if(id){
      officememberid = id;
    } else {
      officememberid = parseInt(req.params.id, 10);
    }

    // Get the officemember matching the given id
    models.OfficeMember.findByPk(officememberid).then(member => {
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
              if (officeMember != null) {
                if (officeMember.OfficeId === office.id) {
                  // The requesting device is also member in the office the requested office member belongs to
                  response(member);
                } else {
                  res
                    .status('401')
                    .send('You are not allowed to access this office member');
                }
              } else {
                console.error(
                  'Failed to authenticate member, since we could not deduce an office member from device ' +
                    req.user.id
                );
              }
            });
          }
        });
      }
    });
  });
};
/**
 * This function authenticated/checks whether the device owner
 * is in the given office
 * 
 * @param {*} req request 
 * @param {*} res result
 * @param {*} officeId officeId of the office which will be used for authentication/comparison
 * 
 */
exports.authenticateAnyOfficeDevice = function(officeId, req, res) {
  return new Promise(response => {
    if (req.user == null) {
      res
        .status('401')
        .send('Authentication token is not present or malformed.');
    } else if (officeId == null) {
      res.status('401').send('Office id is malformed: ' + officeId);
    } else {
      models.Office.findByPk(officeId).then(office => {
        if (office == null) {
          res.status('404').send('There is no office to your id');
        } else {
          // Device is the panel of the Office
          if (req.user.OfficeId === officeId) {
            response(office);
          } else {
            req.user.getOfficeMember().then(officeMember => {
              if (officeMember != null) {
                // Device is OfficeMember of the office.
                if (officeMember.OfficeId === officeId) {
                  response(office);
                } else {
                  res
                    .status('401')
                    .send(
                      'You are not authorized for this office since your device belongs to office ' +
                        officeMember.OfficeId +
                        ' not office ' +
                        officeId +
                        '.'
                    );
                }
              } else {
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
/**
 * This function authenticates/checks whether the owner of 
 * the (in the parameter request) given device matches the officemember
 * with the officeMemberId from officememberIdParam.
 * 
 * @param {*} req request contains owner of a device, who wants to be authenticated
 * @param {*} res result
 * @param {*} officememberIdParam officememberId, which will be used for the authentication/comparison
 */
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
        message:
          'You do not have the permission to access this officemember. Seems your authentication token is invalid.',
      });
    }
    // The request do have an correct authorization header
    else {
      const officeMemberId = parseInt(officememberIdParam, 10);

      device.getOfficeMember().then(loggedIn => {
        // Check whether a officemember belongs to the authorized device
        // and whether that officemember is a part of the office
        if (loggedIn && loggedIn.id === officeMemberId) {
          console.log(
            'Office member ' +
              officeMemberId +
              ' with device ' +
              device.id +
              ' authenticated'
          );
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
