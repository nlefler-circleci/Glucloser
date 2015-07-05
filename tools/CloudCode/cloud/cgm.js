// Returns a promise list of objects representing CGM readings in the time interval
var CGMReadingsForTimeRange = function (date, minutesPast) {
  var query = new Parse.Query('MedtronicMinimedParadigmRevel755PumpData');
  query.greaterThan('Timestamp', date);
  query.lessThan('Timestamp', new Date(date.getTime() + minutesPast * 60 * 1000));
  query.ascending('Timestamp');
  query.select('Sensor_Glucose__mg_dL_');
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
      results.each(function(reading) {
        var readingTime = reading.get('Timestamp').getTime();

        if (currentTime === 0) {
          currentTime = readingTime;
        }

        if (readingTime > currentTime + reductionInterval) {
          // Average
          averages.push(sum / count);
          sum = 0;
          count = 0;
          currentTime = readingTime;
        }

        sum += reading.get('Sensor_Glucose__mg_dL_');
        count++;
      });
    promise.resolve(averages);
  });
};

exports.CGMReadingsForTimeRange = CGMReadingsForTimeRange;
exports.ReducedCGMReadingsForTimeRange = ReducedCGMReadingsForTimeRange;
