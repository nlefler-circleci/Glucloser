var basal = require('cloud/basal.js');
var aggregateJobHelper = require('cloud/aggregate_job_helper.js');

exports.RegisterAggregateBasalRates = function() {
  console.log("Register aggregate basal rate job");

  var config = aggregateJobHelper.CreateAggregateRateJobConfig();
  config.JobName = 'aggregateBasalRates';
  config.LogTableName = 'AggregateBasalRatesProcessLog';
  config.ChangeEventTableName = 'BasalRate';
  config.PatternChangeEventTableName = 'BasalPattern';
  config.ChangeEventsAfterFun = basal.BasalChangeEventsAfter;
  config.ChangeEventDeserializeFun = basal.DeserializeBasalChangeEvent;
  config.ChangeEventLogFormatFun = basal.LogFormatBasalChangeEvent;
  config.ChangeEventSaveFun = function (parseObj, changeObj) {
    parseObj.set('rate', changeObj.Rate);
    parseObj.set('ordinal', changeObj.ProfileIndex);
    parseObj.set('startTime', changeObj.StartTime);
  };
  config.PatternChangeEventsAfterFun = basal.BasalPatternChangeEventsAfter;
  config.PatternChangeEventDeserializeFun = basal.DeserializeBasalPatternChangeEvent;
  config.PatternChangeEventLogFormatFun = basal.LogFormatBasalPatternChangeEvent;
  config.PatternChangeEventSaveFun = function (parseObj, changeObj) {
    parseObj.set('rateCount', changeObj.NumProfiles);

    var pointerPromises = [];
    for (var i = 0; i < changeObj.NumRatios; i++) {
      var promise = new Parse.Promise();
      pointerPromises.push(promise);
      var query = new Parse.Query(config.ChangeEventTableName);
      query.limit(1);
      query.descending('timestamp');
      query.lessThan('timestamp', changeObj.Timestamp);
      query.equalTo('ordinal', i);
      query.find().then(function(rateObj) {
        parseObj.add('rate', rateObj);
        promise.resolve();
      }, promise.reject);
    }
    var donePromise = new Parse.Promise();
    Parse.Promise.when(pointerPromises).then(function() {
      parseObj.save().then(donePromise.resolve, donePromise.reject);
    }, donePromise.reject);
    return donePromise;
  };

  Parse.Cloud.job('aggregateBasalRates', aggregateJobHelper.CreateAggregateRateJob(config));
};
