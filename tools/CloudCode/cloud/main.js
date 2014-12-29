var express = require('express');
var app = express();
 
// Global app configuration section
app.use(express.bodyParser());  // Populate req.body
 
app.post('/foursquareCheckin',
         function(req, res) {
  console.log(req.body);
  res.send(200);
});
 
app.listen();