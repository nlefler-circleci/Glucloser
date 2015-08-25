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
    rateItem.set('rate', changeObj.Rate);
    rateItem.set('ordinal', changeObj.ProfileIndex);
    rateItem.set('startTime', changeObj.StartTime);
  };
  config.PatternChangeEventsAfterFun = basal.BasalPatternChangeEventsAfter;
  config.PatternChangeEventDeserializeFun = basal.DeserializeBasalPatternChangeEvent;
  config.PatternChangeEventLogFormatFun = basal.LogFormatBasalPatternChangeEvent;
  config.PatternChangeEventSaveFun = function (parseObj, changeObj) {
    // rateItem.set('rate', changeObj.Rate);
    // rateItem.set('ordinal', changeObj.ProfileIndex);
    // rateItem.set('startTime', changeObj.StartTime);
  };

  Parse.Cloud.job('aggregateBasalRates', aggregateJobHelper.CreateAggregateRateJob(config));
};
