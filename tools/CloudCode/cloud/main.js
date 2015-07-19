var foursquare = require('cloud/foursquare.js');
var cgm = require('cloud/cgm.js');
var bolus = require('cloud/bolus.js');
var _ = require('underscore');
var averages_job = require('cloud/averages_job.js');
var prune_cgm_job = require('cloud/prune_cgm_job.js');

foursquare.foursquarePushHandler.listen();

Parse.Cloud.afterSave('Meal', function(request) {

});

Parse.Cloud.afterSave('Snack', function(request) {

});

averages_job.RegisterPostBolusAveragesJob();
prune_cgm_job.RegisterPruneCGMJob();
