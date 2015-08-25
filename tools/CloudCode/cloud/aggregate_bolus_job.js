var _ = require('underscore');
var bolus = require('cloud/bolus.js');

exports.RegisterAggregateBolusRates = function() {
  Parse.Cloud.job('aggregateBolusRates', function(request, status) {
    // Get the last time this ran
    var lastProcessedDate = null;
    var processLogTableName = 'AggregateBolusRatesProcessLog';

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
        var changeEventsPromise = bolus.BolusChangeEventsAfter(lastProcessedDate, 250);
        lastProcessedDate = null;

        return changeEventsPromise;
      },
      function (error) {
        status.success("Basal aggregation error " + error.message);
      }
    ).then(
      function(changeEvents) {
        var resolveCount = changeEvents.length;
        console.log(resolveCount + " bolus change events");
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

          var changeObj = bolus.DeserializeBolusChangeEvent(changeEvent.get("Raw_Values"));
          console.log("Saving rate " + bolus.LogFormatBolusChangeEvent(changeObj));

          var rateItem = new Parse.Object('BolusRate');
          rateItem.set('rate', changeObj.Rate);
          rateItem.set('ordinal', changeObj.ProfileIndex);
          rateItem.set('startTime', changeObj.StartTime);

          rateSavePromises.push(rateItem.save());
        });

        Parse.Promise.when(rateSavePromises);
      },
      function(error) {
        status.error("Bolus aggregation error " + error.message);
      }
    ).then(
      function (shouldSave) {
        if (!!!shouldSave) {
          return Parse.Promise.as(false);
        }
        var logItem = new Parse.Object(processLogTableName);
        logItem.set("lastProcessedDate", lastProcessedDate);
        return logItem.save();
      },
      function (error) {
        status.error("Bolus aggregation error " + error.message);
      }
    ).then(
      function() {
        status.success("Bolus aggregation success");
      },
      function(saveError) {
        status.error("Bolus aggregation log save error " + saveError.errors + " "+ saveError.code + " " + saveError.message);
      }
    );
  });
};
