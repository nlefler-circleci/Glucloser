var cgm = require('cloud/cgm.js');
var bolus = require('cloud/bolus.js');
var _ = require('underscore');

exports.RegisterPostBolusAveragesJob = function() {
  Parse.Cloud.job('postBolusAverages', function(request, status) {
  	// Get the last time this ran
  	var lastProcessedTime = null;

  	var query = new Parse.Query('CGMGraphProcessLog');
  	query.descending('createdAt');
  	query.first({
  		success: function (object) {
  			if (!!object) {
  				// Last time
  				console.log("Last log " + object.id);
  				lastProcessedTime = object.get('lastProcessedDate');
  			}
  			else {
  				lastProcessedTime = new Date(0);
  			}

  			console.log("Last processed time " + lastProcessedTime);
        var bolusPromise = bolus.BolusesAfterDate(lastProcessedTime, 10);
        lastProcessedTime = null;

  			bolusPromise.then(function(bolusResults) {
  				var resolveCount = bolusResults.length;
          var tryResolve = function() {
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
          };
  				_.each(bolusResults, function(bolusResult) {
  					var minutesCovered = 120;
  					var cgmPromise = cgm.ReducedCGMReadingsForTimeRange(bolusResult.createdAt, minutesCovered, 10);
  					cgmPromise.then(function(results) {
  						var averagesRecord = new Parse.Object("PostBolusCGMAverages");
  						averagesRecord.set(bolusResult.className.toLowerCase(), bolusResult);
  						averagesRecord.set("minutesCovered", minutesCovered);
  						averagesRecord.set("averages", results);
  						averagesRecord.save();

  						if (!!!lastProcessedTime || bolusResult.updatedAt.getTime() < lastProcessedTime.getTime()) {
  							lastProcessedTime = bolusResult.updatedAt;
  						}
  						tryResolve();
  					}, function(error) {
  						if (--resolveCount === 0) {
  							status.error("CGM error " + error.message);
  						}
  					});
  				});
  			}, function(error) {
  				status.error("Bolus error " + error.message);
  			});
  		},
  		error: function (error) {
  			status.success("Test error " + error.message);
  		}
  	});
  });
}
