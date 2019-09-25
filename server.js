var mysql = require('mysql');
var bodyParser = require('body-parser');
var jwt = require('jsonwebtoken');
var express = require('express');
var app = express();

//Grabs variables from config.json
var config = require('./config.json');
var mySQLUsername = config['mySQLUsername']
var mySQLPassword = config['mySQLPassword']
var mySQLHost = config['mySQLHost']
var mySQLPort = config['mySQLPort']
var mySQLDatabase = config['mySQLDatabase']
var jwtSecret = config['jwtSecret']

//Heroku code to set the listening port
const PORT = process.env.PORT || 3000;

var con = mysql.createConnection({
    host: mySQLHost,
    user: mySQLUsername,
    password: mySQLPassword,
    database: mySQLDatabase
});

app.use(bodyParser.json());

app.post('/login', function(req, res) {
    var username = req.body.username;
    var password = req.body.password;

    var loginQuery = `SELECT username, password FROM users where username="${username}" and password="${password}"`;

    con.query(loginQuery, function (err, result, fields) {
        if (err) {
            console.log(err);
            res.writeHead(401, {'Content-Type':'text/plain'});
            res.wrtie('Unauthorized');
            res.send();
        } else {
            if (result.length == 1) {
                //get user information
                var userInformationQuery = `SELECT username, name, age, weight, address FROM users WHERE username="${username}"`;
                con.query(userInformationQuery, function(err, result, feilds) {
                    if (err) {
                        //this should never happen since the login query passed and the user exists
                        res.writeHead(401, {'Content-Type':'text/plain'});
                        res.wrtie('Unauthorized');
                        res.send();
                    } else {
                        var data = JSON.stringify(result[0]);
                        //create JWT token
                        var token = jwt.sign(data, jwtSecret);
                        //send token back to the app
                        res.writeHead(200, {'Content-Type':'text/plain'});
                        res.write(token);
                        res.send();
                    }
                });
            } else {
                //This should never happen
                res.writeHead(401, {'Content-Type':'text/plain'});
                res.write('Unauthorized');
                res.send();
            }
        }
    });
});

app.post('/signup', function(req, res) {
    var username = req.body.username;
    var password = req.body.password;
    var name = req.body.name;
    var age = req.body.age;
    var weight = req.body.weight;
    var address = req.body.address;

    var signUpQuery = `INSERT INTO users VALUES ('${username}', '${password}', '${name}', '${age}', '${weight}',' ${address}')`;

    con.query(signUpQuery, function(err, result) {
        if (err) {
            if (err['errno'] == '1062') {

                console.log('User tried to create an account with a pre-existing username.');
                res.writeHead(1062, {'Content-Type':'text/plain'});
                res.write('Tried to create an account with a username that already exists');
                res.send();
            } else {
                res.writeHead(400, {'Content-Type':'text/plain'});
                res.write("There was an error while adding user to database");
                res.send();
            }
        } else {
            //successful signup sends user back to login page on app so there is no need to create and return a JWT token
            res.writeHead(201, {'Content-Type':'text/plain'});
            res.write('User created');
            res.send();
        }
    });
});

app.post('/profile', function(req, res) {
    var token = req.body.token;

    //Verify that the user is authenticated and shold be able to access user data
    var jwtVerification = jwt.verify(token, jwtSecret, function(err) {
        if (err) {
            console.log('JWT token not verified, user is not authenticated.');
            res.writeHead(401, {'Content-Type':'text/plain'});
            res.write('JWT verification failed');
            res.send();
        } else {
            //app already had token, user is verified, nothing to return
            res.writeHead(200, {'Content-Type':'text/plain'});
            res.write('JWT verified');
            res.send();
        }
    });
});

app.post('/update_profile', function(req, res) {
    var token = req.body.token;

    var username = req.body.username;
    var name = req.body.name;
    var age = req.body.age;
    var weight = req.body.weight;
    var address = req.body.address;

    var oldName, oldAge, oldWeight, oldAddress;

    //Verify that the user is authenticated and should be able to change user data
    var jwtVerification = jwt.verify(token, jwtSecret, function(err) {
        if (err) {
            console.log('JWT token not verified, user is not authenticated.');
            res.writeHead(401, {'Content-Type':'text/plain'});
            res.write('JWT verification failed');
            res.send();
        } else {
            //get uer information first
            var profileQuery = `SELECT name, age, weight, address FROM users WHERE username="${username}"`;

            con.query(profileQuery, function(err, result) {
              if (err) {
                res.writeHead(400, {'Content-Type': 'text/plain'});
                res.write('Error getting user data');
                res.send();
              } else {
                var jsonResult = JSON.parse(result);
                oldName = jsonResult['name'];
                oldAge = jsonResult['age'];
                oldWeight = jsonResult['weight'];
                oldAddress = jsonResult['address'];
              }
            });

            //jwt is verifed, user can make changes
            var updateQuery = `UPDATE users SET name="${name}", age="${age}", weight="${weight}", address="${address}" WHERE
                username="${username}"`;

            //udpates users information in the databse
            con.query(updateQuery, function(err, result) {
                if (err) {
                    res.writeHead(400, {'Content-Type':'text/plain'});
                    res.write('Error while updating user information.');
                    res.send();
                } else {



                    if (name == "") {
                      name = oldName;
                    }

                    if (age == "") {
                      age = oldAge;
                    }

                    if (weight == "") {
                      weight = oldWeight;
                    }

                    if (address == "") {
                      address = oldAddress
                    }

                    //create new data payload
                    var newData = {
                        'name': name,
                        'age': age,
                        'weight': weight,
                        'address': address
                    };
                    //create new token with payload
                    var newToken = jwt.sign(JSON.stringify(newData), jwtSecret);
                    //return new token to app
                    res.writeHead(200, {'Content-Type':'text/plain'});
                    res.write(newToken);
                    res.send();
                }
            });
        }
    });
});

//sets port that API will listen on - Heroku code - different than localhost code
app.listen(PORT, () => {
    console.log(`Our app is running on port ${ PORT }`);
});

//espressjs.com said to add this at the bottom
