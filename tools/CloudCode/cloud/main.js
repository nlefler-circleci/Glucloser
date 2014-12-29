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

  var query = new Parse.Query(Parse.Installation);
  query.equalTo('channels', 'foursquareCheckin');
  query.equalTo('foursquareUserId', foursquareUserId);

  Parse.Push.send({
  		where: query,
		data: {
		alert: notifTitle,
		uri: "com.nlefler.glucloser://logMeal",
		checkinData: checkinData
		}
	}, {
		success: function() {
			console.log("Pushed");
			res.send(200);
		},
		error: function(error) {
			console.log("Push failed");
			console.log(JSON.stringify(error));
			res.send(500);
		}
	});
});
 
app.listen();