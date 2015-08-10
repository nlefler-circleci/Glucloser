var _ = require('underscore');

var BolusesAfterDateFromTable = function(table, afterDate, limit) {
  var query = new Parse.Query(table);
  console.log("Looking for boluses after " + afterDate);
  query.greaterThan("updatedAt", afterDate);
  query.limit(limit + 0);
  return query.find();
};

var BolusChangeEventsAfter = function(afterDate, limit) {
  var query = new Parse.Query('MedtronicMinimedParadigmRevel755PumpData');
  query.greaterThan('Timestamp', afterDate);
  query.ascending('Timestamp');
  query.limit(limit || 250);
  query.equalTo("Raw_Type", "ChangeCarbRatio");
  query.select("Raw_Type", "Raw_Values", "Timestamp");
  return query.find();
};

var LogFormatBolusChangeEvent = function(changeEvent) {
  return "Bolus PatternDatumId: " + changeEvent.PatternDatumId +
  " ProfileIndex: " + changeEvent.ProfileIndex +
  " Rate: " + changeEvent.Rate +
  " StartTime: " + changeEvent.StartTime;
};

var DeserializeBolusChangeEvent = function(changeEventString) {
  // PATTERN_DATUM_ID=15311235006, INDEX=0, AMOUNT=18, UNITS=grams, START_TIME=0
  var changeEvent = {
    PatternDatumId: null,
    ProfileIndex: null, // The first, second, etc rate in the day
    Amount: null, // Carbs per unit of insulin
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
    else if (/\s*INDEX/.test(pair[0])) {
      changeEvent.ProfileIndex = parseInt(pair[1], 10);
    }
    else if (/\s*AMOUNT/.test(pair[0])) {
      changeEvent.Rate = parseFloat(pair[1], 10);
    }
    else if (/\s*START_TIME/.test(pair[0])) {
      changeEvent.StartTime = parseInt(pair[1], 10);
    }
  });

  return changeEvent;
};

exports.BolusChangeEventsAfter = BolusChangeEventsAfter;
exports.DeserializeBolusChangeEvent = DeserializeBolusChangeEvent;
exports.LogFormatBolusChangeEvent = LogFormatBolusChangeEvent;

exports.MealsAfterDate = function(afterDate, limit) {
  return BolusesAfterDateFromTable("Meal", afterDate, limit);
};

exports.SnacksAfterDate = function(afterDate, limit) {
  return BolusesAfterDateFromTable("Snack", afterDate, limit);
};

exports.BolusesAfterDate = function(afterDate, limit) {
  var that = this;
  var promise = new Parse.Promise();
  that.MealsAfterDate(afterDate, limit).then(function (meals) {
    that.SnacksAfterDate(afterDate, limit).then(function (snacks) {
      // TODO Sort by date
      promise.resolve(meals.concat(snacks).sort(function(a, b) {
        return a.updatedAt.getTime() - a.updatedAt.getTime();
      }));
    }, function(error) {
      promise.reject(error);
    });
  }, function(error) {
    promise.reject(error);
  });

  return promise;
}
