var express = require('express');
var app = express();
 
// Global app configuration section
app.use(express.bodyParser());  // Populate req.body
 
app.post('/foursquareCheckin',
         function(req, res) {
  console.log(req.body);

  Parse.Push.send({
	  channels: [ "foursquareCheckin" ],
	  data: {
	    alert: "You checked in",
	    uri: "com.nlefler.glucloser://logMeal",
	    checkinData: req.body
	  }
	}, {
	  success: function() {
	    // Push was successful
	    console.log("pushed");
	  },
	  error: function(error) {
	    // Handle error
	    console.log("push failed");
	    console.log("" + error);
	  }
	});

  res.send(200);
});
 
app.listen();