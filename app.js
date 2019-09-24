var mysql = require('mysql');
var bodyParser = require('body-parser');
var jwt = require('jsonwebtoken');
var express = require('express');
var app = express();

var config = require('./config.json');

var mySQLUsername = config['mySQLUsername']
var mySQLPassword = config['mySQLPassword']
var mySQLHost = config['mySQLHost']
var mySQLPort = config['mySQLPort']
var mySQLDatabase = config['mySQLDatabase']
var jwtSecret = config['jwtSecret']

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
            res.send('failure');
        } else {
            if (result.length == 1) {
                //get user information
                var userInformationQuery = `SELECT username, name, age, weight, address FROM users WHERE username="${username}"`;
                con.query(userInformationQuery, function(err, result, feilds) {
                    if (err) {
                        console.log(err);
                        res.send('failure');
                    } else {
                        if (result.length == 1) {
                            //set expiration time
                            //result[0]['exp'] = '1d';
                            var data = JSON.stringify(result[0]);
                            //create JWT token
                            var token = jwt.sign(data, jwtSecret);
                            //send token back to the app
                            res.send(token);
                        } else {
                            //This should never happen but error check is needed
                            res.send('failure');
                            console.log('got more than one row from username');
                        }
                    }
                });
            } else {
                //This should also never happen
                res.send('failure');
                console.log('No results found for username/password combination');
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
                res.send('There is already an account with this username');
            } else {
                res.send('failure');
            }
        } else {
            //successful signup sends user back to login page on app so there is no need to create and return a JWT token
            res.send('success');
        }
    });
});

app.post('/profile', function(req, res) {
    var token = req.body.token;

    //Verify that the user is authenticated and shold be able to access user data
    var jwtVerification = jwt.verify(token, jwtSecret, function(err) {
        if (err) {
            console.log('JWT token not verified, user is not authenticated.');
            res.send('failure');
        } else {
            //app already had token, user is verified, nothing to return
            res.send('success');
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

    //Verify that the user is authenticated and should be able to change user data
    var jwtVerification = jwt.verify(token, jwtSecret, function(err) {
        if (err) {
            console.log('JWT token not verified, user is not authenticated.');
            res.send('jwt-failure');
        } else {
            //jwt is verifed, user can make changes
            var updateQuery = `UPDATE users SET name="${name}", age="${age}", weight="${weight}", address="${address}" WHERE
                username="${username}"`;

            con.query(updateQuery, function(err, result) {
                if (err) {
                    console.log(err);
                    res.send('failure');
                } else {
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
                    res.send(newToken);
                }
            });
        }
    });
});

//I assume that this will change when the code is put on Heroku
app.listen(3000, function() {
    console.log('Example app listening on port 3000!');
});
