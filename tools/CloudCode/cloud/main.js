var foursquare = require('cloud/foursquare.js');
var cgm = require('cloud/cgm.js');

foursquare.foursquarePushHandler.listen();

Parse.Cloud.afterSave('Meal', function(request) {

});

Parse.Cloud.afterSave('Snack', function(request) {

});

Parse.Cloud.job('postBolusAverages', function(request, status) {
	// Get the last time this ran
	var lastRunTime = null;

	var query = Parse.Query('CGMGraphProcessLog');
	query.descending('createdAt');
	query.first({
		success: function (object) {
			// Last time
			lastRunTime = object.get('createdAt');
		},
		error: function (error) {

		}
	});

	status.success();
});
