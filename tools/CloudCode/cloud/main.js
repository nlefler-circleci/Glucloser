var foursquare = require('cloud/foursquare.js');
var cgm = require('cloud/cgm.js');
var bolus = require('cloud/bolus.js');
var basal = require('cloud/basal.js');
var _ = require('underscore');
var averages_job = require('cloud/averages_job.js');
var prune_cgm_job = require('cloud/prune_cgm_job.js');
var aggregate_basal_job = require('cloud/aggregate_basal_job.js');
var aggregate_bolus_job = require('cloud/aggregate_bolus_job.js');

foursquare.foursquarePushHandler.listen();

Parse.Cloud.afterSave('Meal', function(request) {

});
Parse.Cloud.afterSave('Snack', function(request) {

});

averages_job.RegisterPostBolusAveragesJob();
prune_cgm_job.RegisterPruneCGMJob();
aggregate_basal_job.RegisterAggregateBasalRates();
aggregate_bolus_job.RegisterAggregateBolusRates();
