var BolusesAfterDateFromTable = function(table, afterDate, limit) {
  var query = new Parse.Query(table);
  console.log("Looking for boluses after " + afterDate);
  query.greaterThan("updatedAt", afterDate);
  query.limit(limit + 0);
  return query.find();
};

exports.MealsAfterDate = function(afterDate, limit) {
  return BolusesAfterDateFromTable("Meal", afterDate, limit);
};

exports.SnacksAfterDate = function(afterDate, limit) {
  return BolusesAfterDateFromTable("Snack", afterDate, limit);
};

exports.BolusesAfterDate = function(afterDate, limit) {
  var that = this;
  var promise = new Parse.Promise();
  that.MealsAfterDate(afterDate, limit).then(function (meals) {
    that.SnacksAfterDate(afterDate, limit).then(function (snacks) {
      // TODO Sort by date
      promise.resolve(meals.concat(snacks).sort(function(a, b) {
        return a.updatedAt.getTime() - a.updatedAt.getTime();
      }));
    }, function(error) {
      promise.reject(error);
    });
  }, function(error) {
    promise.reject(error);
  });

  return promise;
}
