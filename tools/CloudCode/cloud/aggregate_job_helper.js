var _ = require('underscore');

var CreateAggregateRateJobConfig = function () {
  console.log("Create AggregateRateJob config");

  return {
    JobName: null,
    LogTableName: null,
    ChangeEventTableName: null,
    PatternChangeEventTableName: null,
    ChangeEventsAfterFun: null,
    ChangeEventDeserializeFun: null,
    ChangeEventLogFormatFun: null,
    ChangeEventSaveFun: null,
    PatternChangeEventsAfterFun: null,
    PatternChangeEventDeserializeFun: null,
    PatternChangeEventLogFormatFun: null,
    PatternChangeEventSaveFun: null
  };
};

var ValidateCreateAggregateRateJobConfig = function(config) {
  if (!!!config) {
    console.log("Check AggregateRateJob no config");
    return false;
  }

  var ok = true;
  ok = ok && !!config.JobName && config.JobName.length > 0;
  if (!!!ok) {
    console.log("Check AggregateRateJob bad JobName" + config.JobName);
    return;
  }
  ok = ok && !!config.LogTableName && config.LogTableName.length > 0;
  if (!!!ok) {
    console.log("Check AggregateRateJob bad LogTableName " + config.LogTableName);
    return;
  }
  ok = ok && !!config.ChangeEventTableName && config.ChangeEventTableName.length > 0;
  if (!!!ok) {
    console.log("Check AggregateRateJob bad ChangeEventTableName " + config.ChangeEventTableName);
    return;
  }
  ok = ok && !!config.PatternChangeEventTableName && config.PatternChangeEventTableName.length > 0;
  if (!!!ok) {
    console.log("Check AggregateRateJob bad PatternChangeEventTableName " + config.PatternChangeEventTableName);
    return;
  }
  ok = ok && !!config.ChangeEventsAfterFun;
  if (!!!ok) {
    console.log("Check AggregateRateJob bad ChangeEventsAfterFun " + config.ChangeEventsAfterFun);
    return;
  }
  ok = ok &&  !!config.ChangeEventDeserializeFun;
  if (!!!ok) {
    console.log("Check AggregateRateJob bad ChangeEventDeserializeFun " + config.ChangeEventDeserializeFun);
    return;
  }
  ok = ok && !!config.ChangeEventLogFormatFun;
  if (!!!ok) {
    console.log("Check AggregateRateJob bad ChangeEventLogFormatFun " + config.ChangeEventLogFormatFun);
    return;
  }
  ok = ok && !!config.ChangeEventSaveFun;
  if (!!!ok) {
    console.log("Check AggregateRateJob bad ChangeEventSaveFun " + config.ChangeEventSaveFun);
    return;
  }
  ok = ok && !!config.PatternChangeEventsAfterFun;
  if (!!!ok) {
    console.log("Check AggregateRateJob bad PatternChangeEventsAfterFun " + config.PatternChangeEventsAfterFun);
    return;
  }
  ok = ok && !!config.PatternChangeEventDeserializeFun;
  if (!!!ok) {
    console.log("Check AggregateRateJob bad PatternChangeEventDeserializeFun " + config.PatternChangeEventDeserializeFun);
    return;
  }
  ok = ok && !!config.PatternChangeEventLogFormatFun;
  if (!!!ok) {
    console.log("Check AggregateRateJob bad PatternChangeEventLogFormatFun " + config.PatternChangeEventLogFormatFun);
    return;
  }
  ok = ok && !!config.PatternChangeEventSaveFun;
  if (!!!ok) {
    console.log("Check AggregateRateJob bad PatternChangeEventSaveFun " + config.PatternChangeEventSaveFun);
    return;
  }

  console.log("Check AggregateRateJob config: " + ok);
  return ok;
};

var CreateAggregateRateJob = function(config) {
  if (!ValidateCreateAggregateRateJobConfig(config)) {
    console.log("Unable to generate AggregateRateJob, bad config");
    return function(request, status) {status.error("Bad config");};
  }

  console.log("Create job " + config.JobName);
  return function(request, status) {
    // Get the last time this ran
    var lastProcessedDate = null;
    var originalLastProcessedDate = null;
    var processLogTableName = config.LogTableName;

    console.log("A");
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
        originalLastProcessedDate = lastProcessedDate;

        console.log("Last processed date" + originalLastProcessedDate);
        return config.ChangeEventsAfterFun(originalLastProcessedDate, 250);
      },
      function (error) {
        status.success(config.JobName + " aggregation error " + error.message);
      }
    ).then(
      function(changeEvents) {
    console.log("B");
        var resolveCount = changeEvents.length;
        console.log(config.JobName + " " + resolveCount + " change events");
        if (resolveCount === 0) {
          return Parse.Promise.as(true);
        }

        var rateSavePromises = [];

        _.each(changeEvents, function(changeEvent) {

          if (changeEvent &&
            (!!!lastProcessedDate ||
              changeEvent.updatedAt.getTime() > lastProcessedDate.getTime())) {
                lastProcessedDate = changeEvent.updatedAt;
                // console.log(config.JobName + " update lastProcessedDate to " + lastProcessedDate);
          }

          var changeObj = config.ChangeEventDeserializeFun(changeEvent);

          // console.log("Saving rate " + config.ChangeEventLogFormatFun(changeObj));

          var rateItem = new Parse.Object(config.ChangeEventTableName);
          config.ChangeEventSaveFun(rateItem, changeObj);

          rateSavePromises.push(rateItem.save());
        });

        return Parse.Promise.when(rateSavePromises);
      },
      function(error) {
        status.error(config.JobName + " aggregation error " + error.message);
      }
    ).then(
      function() {
    console.log("C");
        return config.PatternChangeEventsAfterFun(originalLastProcessedDate);
      },
      function (saveError) {
        status.error(config.JobName + " aggregation error " + error.message);
      }
    ).then(
      function (patternChangeEvents) {
    console.log("D");
        var resolveCount = patternChangeEvents.length;
        console.log(config.JobName + " " + resolveCount + " pattern change events");

        var patternSavePromises = [];

        _.each(patternChangeEvents, function(changeEvent) {

          if (changeEvent &&
            (!!!lastProcessedDate ||
              changeEvent.updatedAt.getTime() > lastProcessedDate.getTime())) {
                lastProcessedDate = changeEvent.updatedAt;
                // console.log(config.JobName + " update lastProcessedDate to " + lastProcessedDate);
          }

          var changeObj = config.PatternChangeEventDeserializeFun(changeEvent);

          console.log("Saving pattern change " + config.PatternChangeEventLogFormatFun(changeObj));

          var parseObj = new Parse.Object(config.PatternChangeEventTableName);
          patternSavePromises.push(config.PatternChangeEventSaveFun(parseObj, changeObj));
        });

        return Parse.Promise.when(patternSavePromises);
      },
      function (error) {
        console.log(config.JobName + " aggregation error " + error.message);
        status.error(config.JobName + " aggregation error " + error.message);
      }
    ).then(
      function () {
        if (lastProcessedDate.getTime() == originalLastProcessedDate.getTime()) {
          console.log("No updates");
          return Parse.Promise.as(true);
        }
    console.log("E");
        console.log(config.JobName + " Saving aggregation last processed date at " + lastProcessedDate);
        var logItem = new Parse.Object(processLogTableName);
        logItem.set("lastProcessedDate", lastProcessedDate);
        return logItem.save();
      },
      function(saveError) {
        console.log(config.JobName + " aggregation log save error " + saveError[0].message);
        status.error(config.JobName + " aggregation log save error " + saveError[0].message);
      }
    ).then(
      function() {
        status.success(config.JobName + " aggregation success");
      },
      function(saveError) {
        status.error(config.Jobname + " aggregation log save error " + saveError.message);
      }
    );
  };
};

exports.CreateAggregateRateJobConfig = CreateAggregateRateJobConfig;
exports.CreateAggregateRateJob = CreateAggregateRateJob;
