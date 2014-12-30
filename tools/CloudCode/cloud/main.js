var express = require('express');
var app = express();

function getFoursquareCheckInData(body) {
	return JSON.parse(body.checkin);
}

function getFoursquareUserId(checkinData) {
	return checkinData.user.id || false;
}
 
function getFoursquareVenueName(checkinData) {
	return checkinData.venue.name || false;
}

function getAppData(checkinData) {
	return {
		venueName: getFoursquareVenueName(checkinData) || "",
		venueId: checkinData.venue.id || "",
		venueLat: checkinData.venue.location.lat || "",
		venueLon: checkinData.venue.location.lng || ""
	};
}

// Global app configuration section
app.use(express.bodyParser());  // Populate req.body
 
app.post('/foursquareCheckin',
         function(req, res) {
  console.log(req.body);

  var checkinData = getFoursquareCheckInData(req.body);
  if (!checkinData) {
  	console.log("No checkin data");
  	res.send(400);
  	return;
  }

  var foursquareUserId = getFoursquareUserId(checkinData);
  if (!foursquareUserId) {
  	console.log("Cant get foursquare user id");
  	res.send(400);
  	return;
  } else {
  	console.log("Foursquare user id " + foursquareUserId)
  }

  var notifTitle = "You checked in";
  var venueName = getFoursquareVenueName(checkinData);
  if (venueName) {
  	notifTitle += " at " + venueName;
  }
  notifTitle += ". Log a meal?";

  var appData = getAppData(checkinData) || {};

  var query = new Parse.Query(Parse.Installation);
  query.equalTo('channels', 'foursquareCheckin');
  query.equalTo('foursquareUserId', foursquareUserId);

  Parse.Push.send({
  		where: query,
		data: {
			alert: notifTitle,
			uri: "com.nlefler.glucloser://logMeal",
			checkInData: appData
		}
	}, {
		success: function() {
			console.log("Pushed");
		},
		error: function(error) {
			console.log("Push failed");
			console.log(JSON.stringify(error));
		}
	});

	res.send(200);
});
 
app.listen();