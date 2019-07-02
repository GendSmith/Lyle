const redis = require("redis");
const mysql = require("./mysql")

const {REDIS_OPTIONS} = require("../config");
const client = redis.createClient(
  REDIS_OPTIONS.port,
  REDIS_OPTIONS.host,
  REDIS_OPTIONS.opts
);

client.select(5);

function insertAllUserInfo(){
  mysql.query("select id,username from user")
  .then(function({results}){
    let restoreObj = {}
    results.map((userInfo)=>{
      let id = userInfo.id;
      restoreObj[id] = userInfo.username;
    });
    let batch = client.batch()
    for(id in restoreObj){
      batch.mset(id,restoreObj[id])
    }
    batch.exec(function(err,result){
      if(err){
        console.log(err);
      }
    })
  })
}

function updateOneUserInfo(id,username) {
  client.batch().set(id,username);
}

function getOneUserInfo(id) {
  return client.get(id);
}

function getMultiUserInfo(idList){
  return new Promise(function(resolve,reject){
    client.mget(idList,async function(err,result){
      return resolve(result);
    })
  })
  
}

module.exports = {
  insertAllUserInfo,
  updateOneUserInfo,
  getOneUserInfo,
  getMultiUserInfo
}