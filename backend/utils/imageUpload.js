const {HOST} = require("../config");

function upload(req, res) {
  var files = req.files;
  var result = {};
  if (!files[0]) {
    result.code = 400;
    result.msg = "fail";
  } else {
    result.code = 200;
    result.url = (HOST + files[0].path).replace(/\\/g, "/");
    result.msg = "success";
  }
  res.json(result);
}

module.exports = upload;
