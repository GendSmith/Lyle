const mysql = require("ct-mysql");
const {config} = require("../config");

module.exports = mysql(config.MYSQL_OPTIONS);
