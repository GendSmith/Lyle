const TYPE = ["PublishOneMoment", "GetMoments", "LikeMoment", "UnLikeMoment"];
const {mysql,redisUtils} = require("../utils");

function publish(req, res) {
  const {
    user_id,
    moment_text,
    img_list,
    location_x,
    location_y,
    address
  } = req.body;
  console.log(user_id, moment_text, img_list, location_x, location_y, address);

  mysql
    .query(
      "INSERT INTO moment (user_id,moment_text,img_list,location,address,publish_timestamp) VALUES(?,?,?,ST_GeomFromText('POINT(? ?)'),?,NOW())",
      [user_id, moment_text, img_list, location_x, location_y, address]
    )
    .then(function({results}) {
      res.json({msg: "success", code: 200});
    })
    .catch(function(err) {
      console.log(err);
      res.json({code: 400, msg: "fail"});
    });
}

function getMoments(req, res) {
  const {user_id, begin, location_x, location_y, distance} = req.body;
  mysql
    .query(
      "select m.id, username,profilePic,job,age,gender,moment_text,img_list,location,address, st_distance(location,POINT(?,?))*111195 as distance,likes from moment as m,user as u  WHERE st_distance(location,POINT(?,?))*111195<? AND u.id=m.user_id  ORDER BY distance LIMIT ?,20",
      [location_x, location_y, location_x, location_y, distance, begin]
    )
    .then(async function({results}) {
      
      const resultsPromise = [];
      for(let i =0;i<results.length;i++){
        resultsPromise.push(new Promise((resolve,reject)=>{
            const idList = results[i].likes.split(",");
            idList.pop();
            redisUtils.getMultiUserInfo(idList)
            .then(function(result){
              results[i].likeUsernameList = result;
              resolve(results);
            });
        }))
      }

      Promise.all(resultsPromise).then((result)=>{
        res.json({code: 200, msg: "success", moments: result});
      })
      .catch(function(err){
        throw(err);
      })
    })
    .catch(function(err) {
      console.log(err);
      res.json({msg: "fail", code: 400});
    });
}

function likeMoment(req, res) {
  const {moment_id, user_id} = req.body;
  mysql
    .query(
      "update moment set likes=concat(likes,'?,') where id=? and position('?,' in likes)=0",
      [user_id, moment_id, user_id]
    )
    .then(function({results}) {
      console.log(results);
      res.json({code: 200, msg: "success"});
    })
    .catch(function(err) {
      console.log(err);
      res.json({msg: "fail", code: 400});
    });
}

function unLikeMoment(req, res) {
  const {moment_id, user_id} = req.body;
  mysql
    .query(
      "update moment set likes=replace(likes,'?,','') where id=? and position('?,' in likes)>0",
      [user_id, moment_id, user_id]
    )
    .then(function({results}) {
      console.log(results);
      res.json({code: 200, msg: "success"});
    })
    .catch(function(err) {
      console.log(err);
      res.json({msg: "fail", code: 400});
    });
}

function handle(req, res) {
  const {type} = req.body;
  switch (type) {
    case TYPE[0]:
      publish(req, res);
      break;
    case TYPE[1]:
      getMoments(req, res);
      break;
    case TYPE[2]:
      likeMoment(req, res);
      break;
    case TYPE[3]:
      unLikeMoment(req, res);
      break;
    default:
      res.json({msg: "没有这个api", code: 400});
  }
}

module.exports = handle;
