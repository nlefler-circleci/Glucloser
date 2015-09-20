var bolus = require('cloud/bolus.js');
var aggregateJobHelper = require('cloud/aggregate_job_helper.js');
var _ = require('underscore');

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
    parseObj.set('timestamp', changeObj.Timestamp);
  };
  config.PatternChangeEventsAfterFun = bolus.BolusPatternChangeEventsAfter;
  config.PatternChangeEventDeserializeFun = bolus.DeserializeBolusPatternChangeEvent;
  config.PatternChangeEventLogFormatFun = bolus.LogFormatBolusPatternChangeEvent;
  config.PatternChangeEventSaveFun = function (parseObj, changeObj) {
    parseObj.set('rateCount', changeObj.NumRatios);

    var makeResolveAdd = function (po, prms) {
      return function(rateObj) {
        po.add('rates', rateObj);
        if (!!prms.resolvedAt) {
          console.log("resolve " + prms.idd + " " + prms.resolvedAt);
        }
        prms.resolvedAt = new Date();
        prms.resolve();
      };
    };
    var pointerPromises = [];
    for (var i = 0; i < changeObj.NumRatios; i++) {
      var promise = new Parse.Promise();
      promise.idd = (new Date()).getTime() + _.random(100000);
      pointerPromises.push(promise);
      var query = new Parse.Query(config.ChangeEventTableName);
      query.limit(1);
      query.descending('timestamp');
      query.lessThan('timestamp', changeObj.Timestamp);
      query.equalTo('ordinal', i);
      query.first().then(makeResolveAdd(parseObj, promise), promise.reject);
    }
    var donePromise = new Parse.Promise();
    donePromise.idd = (new Date()).getTime() + _.random(10000);
    var makeResolveDone = function (po, prms) {
      return function() {
        po.save().then(function(){console.log("done " + prms.idd + " " + prms.resolvedAt);prms.resolvedAt = new Date();prms.resolve();}, prms.reject);
      }
    }
    Parse.Promise.when(pointerPromises).then(makeResolveDone(parseObj, donePromise), donePromise.reject);
    return donePromise;
  };

  Parse.Cloud.job('aggregateBolusRates', aggregateJobHelper.CreateAggregateRateJob(config));
};
