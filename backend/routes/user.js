const {mysql,redisUtils} = require("../utils");

const TYPE = [
  "AddOneUser",
  "GetOneUserInfo",
  "GetIdByUserName",
  "UpdateUserInfo"
];

function addOneUser(req, res) {
  const {username, password, job, age, gender, profilePicUrl} = req.body;
  mysql
    .query(
      "INSERT INTO user (username,password,job,age,gender,profilePic) VALUES (?,?,?,?,?,?)",
      [username, password, job, age, gender, profilePicUrl]
    )
    .then(function({results}) {
      //console.log({results});
      res.json({msg: "success", code: 200});
    })
    .catch(function(err) {
      console.log(err);
      res.json({code: 400, msg: "fail"});
    });
}

function getOneUserInfo(req, res) {
  const {id} = req.body;
  mysql
    .query("SELECT * FROM user WHERE id=?", [id])
    .then(function({results}) {
      res.json(results[0]);
    })
    .catch(function(err) {
      console.log(err);
      res.json({msg: "fail", code: 400});
    });
}

function getIdByUserName(req, res) {
  const {username} = req.body;

  mysql
    .query("SELECT * FROM user WHERE username =?", [username])
    .then(function({results}) {
      if (results.length >= 1) {
        res.json({msg: "用户名已存在", code: 200, id: results[0].id});
      } else {
        res.json({msg: "用户名不存在", code: 200, id: -1});
      }
    })
    .catch(function(err) {
      console.log(err);
      res.json({code: 400, msg: "fail"});
    });
}

function updateUserInfo(req, res) {
  const {
    user_id,
    username,
    password,
    job,
    age,
    gender,
    profilePicUrl
  } = req.body;
  mysql
    .query(
      "UPDATE user SET username=?,password=?,job=?,age=?,gender=?,profilePic=? WHERE id=?",
      [username, password, job, age, gender, profilePicUrl, user_id]
    )
    .then(function({results}) {
      //console.log({results});
      redisUtils(user_id,username);
      res.json({msg: "success", code: 200});
    })
    .catch(function(err) {
      console.log(err);
      res.json({code: 400, msg: "fail"});
    });
}

function handler(req, res) {
  const {type} = req.body;
  console.log(type);
  switch (type) {
    case TYPE[0]:
      addOneUser(req, res);
      break;
    case TYPE[1]:
      getOneUserInfo(req, res);
      break;
    case TYPE[2]:
      getIdByUserName(req, res);
      break;
    case TYPE[3]:
      updateUserInfo(req, res);
      break;
    default:
      res.json({msg: "没有这个api", code: 200});
  }
}

module.exports = handler;
