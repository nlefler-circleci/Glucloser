var cgm = require('cloud/cgm.js');
var _ = require('underscore');

exports.RegisterPruneCGMJob = function() {
  Parse.Cloud.job('pruneCGMData', function(request, status) {
    // Get the last time this ran
    var lastProcessedTime = null;

    var query = new Parse.Query('CGMPruneProcessLog');
    query.descending('createdAt');
    query.first({
      success: function (object) {
        if (!!object) {
          // Last time
          console.log("Last log " + object.id);
          lastProcessedTime = object.get('lastProcessedDate');
        }
        else {
          lastProcessedTime = new Date(0);
        }

        console.log("Last processed time " + lastProcessedTime);
        var cgmDataPromise = cgm.CGMDataAfterDate(lastProcessedTime, 250);
        lastProcessedTime = null;

        cgmDataPromise.then(function(cgmDataResults) {
          var resolveCount = cgmDataResults.length;
          console.log(resolveCount + " cgm data results");
          if (resolveCount === 0) {
            status.success("No CGM results to process");
            return;
          }

          var tryResolve = function(cgmData) {
            if (cgmData && (!!!lastProcessedTime || cgmData.updatedAt.getTime() < lastProcessedTime.getTime())) {
              lastProcessedTime = cgmData.updatedAt;
            }

            if (--resolveCount === 0) {
              var logItem = new Parse.Object("CGMPruneProcessLog");
              logItem.set("lastProcessedDate", new Date(lastProcessedTime));
              logItem.save(null, {
                success: function() {
                  status.success("Test success");
                },
                error: function(saveError) {
                  status.error("Save error " + saveError.errors + " "+ saveError.code + " " + saveError.message);
                }
              });
            }
          };

          _.each(cgmDataResults, function(cgmData) {

            var cgmRepeatPromise = cgm.RepeatsOfCGMData(cgmData);
            cgmRepeatPromise.then(function(results) {
              var count = results.length;
              console.log(count + " repeats of " + cgmData.id);

              if (count <= 1) {
                tryResolve(results[0] || cgmData);
                return;
              }
              for (var idx = 1; idx < count; idx++) {
                results[idx].destroy();
              }


              tryResolve();
            }, function(error) {
              if (--resolveCount === 0) {
                status.error("CGM error " + error.message);
              }
            });
          });
        }, function(error) {
          status.error("CGM Data error " + error.message);
        });
      },
      error: function (error) {
        status.success("Test error " + error.message);
      }
    });
  });
};
