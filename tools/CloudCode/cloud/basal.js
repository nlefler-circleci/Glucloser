var _ = require('underscore');

var BasalChangeEventsAfter = function(afterDate, limit) {
  var query = new Parse.Query('MedtronicMinimedParadigmRevel755PumpData');
  query.greaterThan('Timestamp', date);
  query.ascending('Timestamp');
  query.limit(limit || 250);
  query.equalTo("Raw-Type", "ChangeBasalProfile");
  query.select("Raw-Type", "Raw-Values", "Timestamp");
  return query.find();
};

var DeserializeBasalChangeEvent = function(changeEventString) {
  // PATTERN_DATUM_ID=15277543362, PROFILE_INDEX=4, RATE=0.975, START_TIME=79200000
  var changeEvent = {
    PatternDatumId: null,
    ProfileIndex: null, // The first, second, etc rate in the day
    Rate: null, // Units per hour
    StartTime: null // Seconds since midnight
  };
  var elements = changeEventString.split(",");

  _.each(elements, function(element) {
    var pair = element.split("=");
    if (pair.length < 2) {
      return;
    }

    if (/\s*PATTERN_DATUM_ID/.test(pair[0])) {
      changeEvent.PatternDatumId = pair[1];
    }
    else if (/\s*PROFILE_INDEX/.test(pair[0])) {
      changeEvent.ProfileIndex = parseInt(pair[1], 10);
    }
    else if (/\s*RATE/.test(pair[0])) {
      changeEvent.Rate = parseFloat(pair[1], 10);
    }
    else if (/\s*START_TIME/.test(pair[0])) {
      changeEvent.StartTime = parseInt(pair[1], 10);
    }
  });

  return changeEvent;
};

exports.RegisterAggregateBolusRates = function() {
  Parse.Cloud.job('aggregateBolusRates', function(request, status) {
    // Get the last time this ran
    var lastProcessedDate = null;
    var processLogTableName = 'AggregateBolusRatesProcessLog';

    var query = new Parse.Query(processLogTableName);
    query.descending('createdAt');
    query.first({
      success: function (object) {
        if (!!object) {
          // Last time
          console.log("Last log " + object.id);
          lastProcessedDate = object.get('lastProcessedDate');
        }
        else {
          lastProcessedDate = new Date(0);
        }

        console.log("Last processed date" + lastProcessedDate);
        var changeEventsPromise = BasalChangeEventsAfter(lastProcessedDate, 250);
        lastProcessedDate = null;

        changeEventsPromise.then(function(changeEvents) {
          var resolveCount = changeEvents.length;
          console.log(resolveCount + " basal change events");
          if (resolveCount === 0) {
            status.success("No basal changes to process");
            return;
          }

          var rates = [];
          var tryResolve = function(changeEvent) {
            if (changeEvent && (!!!lastProcessedDate || changeEvent.updatedAt.getTime() < lastProcessedDate.getTime())) {
              lastProcessedDate = changeEvent.updatedAt;
            }

            if (--resolveCount === 0) {
              // TODO Save the rates
              
              var logItem = new Parse.Object(processLogTableName);
              logItem.set("lastProcessedDate", new Date(lastProcessedDate));
              logItem.save(null, {
                success: function() {
                  status.success("Basal aggregation success");
                },
                error: function(saveError) {
                  status.error("Basal aggregation log save error " + saveError.errors + " "+ saveError.code + " " + saveError.message);
                }
              });
            }
          };

          _.each(changeEvents, function(changeEvent) {
            var changeObj = DeserializeBasalChangeEvent(changeEvent.get("Raw-Values"));
            rates[changeObj.ProfileIndex] = changeObj;

            tryResolve(changeEvent);
          });
        }, function(error) {
          status.error("Basal aggregation error " + error.message);
        });
      },
      error: function (error) {
        status.success("Basal aggregation error " + error.message);
      }
    });
  });
};
