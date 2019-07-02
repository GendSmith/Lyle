const multer = require("multer");
const {redisUtils} = require("./utils")
const storage = multer.diskStorage({
  destination: function(req, file, cb) {
    cb(null, "./static");
  },
  filename: function(req, file, cb) {
    console.log(2333, file);
    cb(null, `${Date.now()}-${file.originalname}`);
  }
});

const upload = multer({storage: storage});

const bodyParser = require("body-parser");

const express = require("express");

const app = express();
app.use("/static", express.static("static"));

app.use(bodyParser.json());

app.post("/user", require("./routes/user"));

app.post("/upload/img", upload.array("img", 1), require("./utils/imageUpload"));

app.post("/moment", require("./routes/moment"));

app.listen(7899, () => {
  redisUtils.insertAllUserInfo();
  console.log("Server start!");
});
