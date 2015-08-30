var _ = require('underscore');

var BasalChangeEventsAfter = function(afterDate, limit) {
  var query = new Parse.Query('MedtronicMinimedParadigmRevel755PumpData');
  query.greaterThan('Timestamp', afterDate);
  query.ascending('Timestamp');
  query.limit(limit || 250);
  query.equalTo("Raw_Type", "ChangeBasalProfile");
  query.select("Raw_Type", "Raw_Values", "Timestamp");
  return query.find();
};

var BasalPatternChangeEventsAfter = function (afterDate, limit) {
  var query = new Parse.Query('MedtronicMinimedParadigmRevel755PumpData');
  query.greaterThan('Timestamp', afterDate);
  query.ascending('Timestamp');
  query.limit(limit || 250);
  query.equalTo("Raw_Type", "ChangeBasalProfilePatternPre");
  query.select("Raw_Type", "Raw_Values", "Timestamp");
  return query.find();
};

var LogFormatBasalChangeEvent = function(changeEvent) {
  return "PatternDatumId: " + changeEvent.PatternDatumId +
  " ProfileIndex: " + changeEvent.ProfileIndex +
  " Rate: " + changeEvent.Rate +
  " StartTime: " + changeEvent.StartTime;
};

var LogFormatBasalPatternChangeEvent = function (changeEvent) {
  return "NumProfiles: " + changeEvent.NumProfiles;
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

var DeserializeBasalPatternChangeEvent = function(changeEventString) {
  // PATTERN_NAME=standard, NUM_PROFILES=5, ACTION_REQUESTOR=pump
  var changeEvent = {
    NumProfiles: null, // The number of basal rates in the day
  };
  var elements = changeEventString.split(",");

  _.each(elements, function(element) {
    var pair = element.split("=");
    if (pair.length < 2) {
      return;
    }

    if (/\s*NUM_PROFILES/.test(pair[0])) {
      changeEvent.NumProfiles = parseInt(pair[1], 10);
    }
  });

  return changeEvent;
};

exports.BasalChangeEventsAfter = BasalChangeEventsAfter;
exports.BasalPatternChangeEventsAfter = BasalPatternChangeEventsAfter;
exports.DeserializeBasalChangeEvent = DeserializeBasalChangeEvent;
exports.DeserializeBasalPatternChangeEvent = DeserializeBasalPatternChangeEvent;
exports.LogFormatBasalChangeEvent = LogFormatBasalChangeEvent;
exports.LogFormatBasalPatternChangeEvent = LogFormatBasalPatternChangeEvent;
