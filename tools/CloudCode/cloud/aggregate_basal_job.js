var _ = require('underscore');
var basal = require('cloud/basal.js');

exports.RegisterAggregateBasalRates = function() {
  Parse.Cloud.job('aggregateBasalRates', function(request, status) {
    // Get the last time this ran
    var lastProcessedDate = null;
    var processLogTableName = 'AggregateBasalRatesProcessLog';

    var query = new Parse.Query(processLogTableName);
    query.descending('createdAt');
    query.first().then(
      function (object) {
        if (!!object) {
          // Last time
          console.log("Last log " + object.id);
          lastProcessedDate = object.get('lastProcessedDate');
        }
        else {
          lastProcessedDate = new Date(0);
        }

        console.log("Last processed date" + lastProcessedDate);
        var changeEventsPromise = basal.BasalChangeEventsAfter(lastProcessedDate, 250);
        lastProcessedDate = null;

        return changeEventsPromise;
      },
      function (error) {
        status.success("Basal aggregation error " + error.message);
      }
    ).then(
      function(changeEvents) {
        var resolveCount = changeEvents.length;
        console.log(resolveCount + " basal change events");
        if (resolveCount === 0) {
          return Parse.Promise.as(false);
        }

        var rateSavePromises = [];

        _.each(changeEvents, function(changeEvent) {

          if (changeEvent &&
            (!!!lastProcessedDate ||
              changeEvent.updatedAt.getTime() < lastProcessedDate.getTime())) {
            lastProcessedDate = changeEvent.updatedAt;
          }

          var changeObj = basal.DeserializeBasalChangeEvent(changeEvent.get("Raw_Values"));

          console.log("Saving rate " + basal.LogFormatBasalChangeEvent(changeObj));

          var rateItem = new Parse.Object('BasalRate');
          rateItem.set('rate', changeObj.Rate);
          rateItem.set('ordinal', changeObj.ProfileIndex);
          rateItem.set('startTime', changeObj.StartTime);

          rateSavePromises.push(rateItem.save());
        });

        return Parse.Promise.when(rateSavePromises);
      },
      function(error) {
        status.error("Basal aggregation error " + error.message);
      }
    ).then(
      function (shouldSave) {
        if (!!!shouldSave) {
          return Parse.Promise.as(false);
        }
        console.log("Saving basal aggregation last processed date at " + lastProcessedDate);
        var logItem = new Parse.Object(processLogTableName);
        logItem.set("lastProcessedDate", lastProcessedDate);
        return logItem.save();
      },
      function(saveError) {
        status.error("Basal aggregation log save error " + saveError.errors + " "+ saveError.code + " " + saveError.message);
      }
    ).then(
      function() {
        status.success("Basal aggregation success");
      },
      function(saveError) {
        status.error("Basal aggregation log save error " + saveError.errors + " "+ saveError.code + " " + saveError.message);
      }
    );
  });
};
