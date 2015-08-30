var bolus = require('cloud/bolus.js');
var aggregateJobHelper = require('cloud/aggregate_job_helper.js');

exports.RegisterAggregateBolusRates = function() {
  console.log("Register aggregate bosul rate job");

  var config = aggregateJobHelper.CreateAggregateRateJobConfig();
  config.JobName = 'aggregateBolusRates';
  config.LogTableName = 'AggregateBolusRatesProcessLog';
  config.ChangeEventTableName = 'BolusRate';
  config.PatternChangeEventTableName = 'BolusPattern';
  config.ChangeEventsAfterFun = bolus.BolusChangeEventsAfter;
  config.ChangeEventDeserializeFun = bolus.DeserializeBolusChangeEvent;
  config.ChangeEventLogFormatFun = bolus.LogFormatBolusChangeEvent;
  config.ChangeEventSaveFun = function (parseObj, changeObj) {
    parseObj.set('rate', changeObj.Rate);
    parseObj.set('ordinal', changeObj.ProfileIndex);
    parseObj.set('startTime', changeObj.StartTime);
  };
  config.PatternChangeEventsAfterFun = bolus.BolusPatternChangeEventsAfter;
  config.PatternChangeEventDeserializeFun = bolus.DeserializeBolusPatternChangeEvent;
  config.PatternChangeEventLogFormatFun = bolus.LogFormatBolusPatternChangeEvent;
  config.PatternChangeEventSaveFun = function (parseObj, changeObj) {
    parseObj.set('rateCount', changeObj.NumRatios);
  };

  Parse.Cloud.job('aggregateBolusRates', aggregateJobHelper.CreateAggregateRateJob(config));
};
