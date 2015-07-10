var _ = require('underscore');

var CGMDataAfterDate = function(date, limit) {
  console.log("date " + date);
  var query = new Parse.Query('MedtronicMinimedParadigmRevel755PumpData');
  query.greaterThan('Timestamp', date);
  query.ascending('Timestamp');
  query.limit(limit || 250);
  return query.find();
};

// Returns a promise list of objects representing CGM readings in the time interval
var CGMReadingsForTimeRange = function (date, minutesPast) {
  var query = new Parse.Query('MedtronicMinimedParadigmRevel755PumpData');
  query.greaterThan('Timestamp', date);
  query.lessThan('Timestamp', new Date(date.getTime() + minutesPast * 60 * 1000));
  query.ascending('Timestamp');
  query.select('Sensor_Glucose__mg_dL_', 'Timestamp');
  return query.find();
};

// Returns a promise returning a list of average readings over the time interval, grouping
// at reductionInterval for the average
var ReducedCGMReadingsForTimeRange = function(date, minutesPast, reductionInterval) {
  var promise = new Parse.Promise();
  var currentTime = 0;
  var sum = 0;
  var count = 0;
  var averages = [];
  CGMReadingsForTimeRange(date, minutesPast).then(function (results) {
      _.each(results, function(reading) {
        var readingTime = reading.get('Timestamp').getTime();

        if (currentTime === 0) {
          currentTime = readingTime;
        }

        if (readingTime > currentTime + reductionInterval) {
          // Average
          if (sum === 0 || count === 0) {
            return;
          }
          averages.push(sum / count);
          sum = 0;
          count = 0;
          currentTime = readingTime;
        }

        var gluclose = reading.get('Sensor_Glucose__mg_dL_');
        if (!isNaN(gluclose)) {
          sum += gluclose;
        }
        count++;
      });
    promise.resolve(averages);
  });

  return promise;
};

var RepeatsOfCGMData = function(cgmData) {
  var query = new Parse.Query('MedtronicMinimedParadigmRevel755PumpData');
  query.equalTo('Raw_ID', cgmData.get('Raw_ID'));
  query.greaterThan('Timestamp', cgmData.get('Timestamp'));
  query.ascending('Timestamp');
  return query.find();
};

exports.CGMReadingsForTimeRange = CGMReadingsForTimeRange;
exports.ReducedCGMReadingsForTimeRange = ReducedCGMReadingsForTimeRange;
exports.CGMDataAfterDate = CGMDataAfterDate;
exports.RepeatsOfCGMData = RepeatsOfCGMData;
