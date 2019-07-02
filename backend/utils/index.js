const https = require("https");
exports.mysql = require("./mysql");
exports.validator = require("./validator");
exports.redisUtils = require("./redis");

exports.request = function request(options) {
  return new Promise(function(resolve, reject) {
    const reqeust = https.request(options, function(response) {
      let data = "";

      response.on("data", function(chunk) {
        data += chunk;
      });

      response.on("end", function() {
        resolve(JSON.parse(data));
      });

      response.on("error", function(err) {
        reject(err);
      });
    });

    reqeust.end();
  });
};
