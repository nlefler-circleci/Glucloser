var foursquare = require('cloud/foursquare.js');
var cgm = require('cloud/cgm.js');
var bolus = require('cloud/bolus.js');
var _ = require('underscore');

foursquare.foursquarePushHandler.listen();

Parse.Cloud.afterSave('Meal', function(request) {

});

Parse.Cloud.afterSave('Snack', function(request) {

});

Parse.Cloud.job('postBolusAverages', function(request, status) {
	// Get the last time this ran
	var lastProcessedTime = null;

	var query = new Parse.Query('CGMGraphProcessLog');
	query.descending('createdAt');
	query.first({
		success: function (object) {
			if (!!object) {
				// Last time
				lastProcessedTime = object.get('createdAt');
			}
			else {
				lastProcessedTime = new Date(0);
			}

			var bolusPromise = bolus.BolusesAfterDate(lastProcessedTime, 10);
			bolusPromise.then(function(bolusResults) {
				console.log("Num boluses " + bolusResults.length);

				var resolveCount = bolusResults.length;
				_.each(bolusResults, function(bolusResult) {
					console.log("Bolus result " + bolusResult.id + " " + bolusResult.createdAt);

					var minutesCovered = 120;
					var cgmPromise = cgm.ReducedCGMReadingsForTimeRange(bolusResult.createdAt, minutesCovered, 10);
					cgmPromise.then(function(results) {
						console.log("Num CGM results " + results.length);
						console.log("CGM results " + results.join(", "));

						var averagesRecord = new Parse.Object("PostBolusCGMAverages");
						averagesRecord.set(bolusResult.className.toLowerCase(), bolusResult);
						averagesRecord.set("minutesCovered", minutesCovered);
						averagesRecord.set("averages", results);
						averagesRecord.save();

						if (bolusResult.updatedAt.getTime() > lastProcessedTime) {
							lastProcessedTime = bolusResult.updatedAt.getTime();
						}
						if (--resolveCount === 0) {
							var logItem = new Parse.Object("CGMGraphProcessLog");
							logItem.set("lastProcessedDate", new Date(lastProcessedTime));
							logItem.save(null, {
								success: function() {
									status.success("Test success");
								},
								error: function(saveError) {
									status.error("Save error " + saveError.errors + " "+ saveError.code + " " + saveError.message);
								}
							});
						}
					}, function(error) {
						console.log("CGM Error " + error);
						if (--resolveCount === 0) {
							status.error("Test error");
						}
					});
				});
			}, function(error) {
				console.log("Bolus Error " + error);
				status.error(error);
			});
		},
		error: function (error) {
			console.log("No last run time");
			status.success("Test error");
		}
	});
});
