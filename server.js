var mysql = require('mysql')
var bodyParser = require('body-parser')
var jwt = require('jsonwebtoken')
var braintree = require("braintree")
var express = require('express')
var app = express()
var config = require('./config.json')

// ------------------------------------------------- CONFIG VARAIBLES -------------------------------------------------

var mySQLUsername = config['mySQLUsername']
var mySQLPassword = config['mySQLPassword']
var mySQLHost = config['mySQLHost']
var mySQLPort = config['mySQLPort']
var mySQLDatabase = config['mySQLDatabase']
var jwtSecret = config['jwtSecret']
var merchantId = config['merchantId']
var publicKey = config['publicKey']
var privateKey = config['privateKey']

//Heroku code to set the listening port
//const PORT = process.env.PORT || 3000

// ------------------------------------------------- CONNECTIONS  -------------------------------------------------

var gateway = braintree.connect({
    environment: braintree.Environment.Sandbox,
    merchantId: merchantId,
    publicKey: publicKey,
    privateKey: privateKey
})

var con = mysql.createConnection({
    host: mySQLHost,
    user: mySQLUsername,
    password: mySQLPassword,
    database: mySQLDatabase
})

// ------------------------------------------------- FUNCTIONS -------------------------------------------------

var generateClientToken = function(res, username) {
    gateway.clientToken.generate({
        customerId: username
    }, function(err, response) {
        if (err) {
            console.log('error while getting client token after customer creation')
            console.log(err)
            res.status(400)
            res.setHeader('Content-Type', 'text/plain')
            res.write("There was an error while getting clientToken")
            res.send()
        } else {
            var clientTokenJson = {
                'clientToken': response.clientToken
            }

            res.status(200)
            res.setHeader('Content-Type', 'application/json')
            res.json(clientTokenJson)
        }
    })
}

var createCustomer = function(res, username) {
    gateway.customer.create({
        id: username,
    }, function(err, result) {
        if (err) {
            console.log('error while creating customer')
            console.log(err)
            res.status(400)
            res.setHeader('Content-Type', 'text/plain')
            res.write("There was an error while creating customer")
            res.send()
        } else {
            //user has been created so get the client token
            generateClientToken(res, username)
        }
    })
}

// ------------------------------------------------- ROUTES -------------------------------------------------

app.use(bodyParser.json())

//Find and get customer token or create new customer and get token
app.post('/customer', function(req, res) {
    var token = req.body.token

    jwt.verify(token, jwtSecret, function(err, decodedPayload) {
        if (err) {
            console.log('JWT token not verified, user is not authenticated.')
            res.status(401)
            res.setHeader('Content-Type', 'text/plain')
            res.write('JWT verification failed')
            res.send()
        } else {
            var username = decodedPayload['username']

            //Try to find the Customer attached to the username of the app user
            gateway.customer.find(username, function(err, customer) {
                if (err) {
                    //user was not found so create a customer with the user id
                    createCustomer(res, username)
                } else {
                    //user has been found so get the client token
                    generateClientToken(res, username)
                }
            })
        }
    })
})

//Add a credit card for a current user
app.post('/transaction', function(req, res) {
    var nonceFromTheClient = req.body.paymentMethodNonce
    //var paymentAmount = req.body.paymentMethodAmount
    var paymentAmount = "15.00"

    gateway.transaction.sale({
        amount: paymentAmount,
        paymentMethodNonce: nonceFromTheClient,
        options: {
            submitForSettlement: true
        }
    }, function(err, result) {
        if (result.success) {
            console.log('sale success')
        } else {
            console.log('sale failure')
            console.log(err)
        }
    });

    res.write('doing stuff - check the server')
    res.send()
})

app.post('/login', function(req, res) {
    var username = req.body.username
    var password = req.body.password

    var loginQuery = `SELECT username, password FROM users where username="${username}" and password="${password}"`

    con.query(loginQuery, function(err, result, fields) {
        if (err) {
            //this shouldnt happen - error with query code
            console.log(err)
            res.status(401)
            res.setHeader('Content-Type', 'text/plain')
            res.write('Unauthorized')
            res.send()
        } else {
            //make sure there is only one account connected to username/password
            if (result.length == 1) {
                var usernameJson = {
                    'username': username
                }

                //create a jwt token with the username of the user
                var token = jwt.sign(JSON.stringify(usernameJson), jwtSecret)

                //send token back to the app as json object
                var jwtJson = {
                    'token': token
                }

                res.status(200)
                res.setHeader('Content-Type', 'application/json')
                res.json(jwtJson)
            } else {
                //this means there were 0 or 2+ results found for a username/password
                res.status(401)
                res.setHeader('Content-Type', 'text/plain')
                res.write('Unauthorized')
                res.send()
            }
        }
    })
})

app.post('/signup', function(req, res) {
    var username = req.body.username
    var password = req.body.password
    var name = req.body.name
    var age = req.body.age
    var weight = req.body.weight
    var address = req.body.address

    var signUpQuery = `INSERT INTO users VALUES ('${username}', '${password}', '${name}', '${age}', '${weight}',' ${address}')`

    con.query(signUpQuery, function(err, result) {
        if (err) {
            if (err['errno'] == '1062') {
                //1062 is an SQL error code, not a response status but its easy to pass this way
                res.status(1062)
                res.setHeader('Content-Type', 'text/plain')
                res.write('Tried to create an account with a username that already exists')
                res.send()
            } else {
                res.status(400)
                res.setHeader('Content-Type', 'text/plain')
                res.write("There was an error while adding user to database")
                res.send()
            }
        } else {
            res.status(201)
            res.setHeader('Content-Type', 'text/plain')
            res.write('User created')
            res.send()
        }
    })
})

app.post('/profile', function(req, res) {
    var token = req.body.token

    //Verify that the user is authenticated and shold be able to access user data
    jwt.verify(token, jwtSecret, function(err, decodedPayload) {
        if (err) {
            console.log('JWT token not verified, user is not authenticated.')
            res.status(401)
            res.setHeader('Content-Type', 'text/plain')
            res.write('JWT verification failed')
            res.send()
        } else {
            var username = decodedPayload['username']
            var profileQuery = `SELECT name, age, weight, address FROM users WHERE username="${username}"`

            con.query(profileQuery, function(err, result) {
                if (err) {
                    res.status(400)
                    res.setHeader('Content-Type', 'text/plain')
                    res.write('There was an error while retrieving user data from database')
                    res.send()
                } else {
                    //create json object of user information
                    var userData = {
                        name: result[0]['name'],
                        age: result[0]['age'],
                        weight: result[0]['weight'],
                        address: result[0]['address']
                    }

                    //return it with 200
                    res.status(200)
                    res.setHeader('Content-Type', 'application/json')
                    res.json(userData)
                }
            })
        }
    })
})

app.post('/update_profile', function(req, res) {
    var token = req.body.token
    var name = req.body.name
    var age = req.body.age
    var weight = req.body.weight
    var address = req.body.address

    //Verify that the user is authenticated and should be able to change user data
    jwt.verify(token, jwtSecret, function(err, decodedPayload) {
        if (err) {
            console.log('JWT token not verified, user is not authenticated.')
            res.status(401)
            res.setHeader('Content-Type', 'text/plain')
            res.write('JWT verification failed')
            res.send()
        } else {
            var username = decodedPayload['username']
            var updateQuery = `UPDATE users SET name="${name}", age="${age}", weight="${weight}", address="${address}" WHERE
                username="${username}"`

            //udpates users information in the databse
            con.query(updateQuery, function(err, result) {
                if (err) {
                    res.writeHead(400, {
                        'Content-Type': 'text/plain'
                    })
                    res.write('Error while updating user information.')
                    res.send()
                } else {
                    //action has been completed - 200
                    res.status(200)
                    res.setHeader('Content-Type', 'text/plain')
                    res.write("User data has been updated")
                    res.send()
                }
            })
        }
    })
})

//sets port that API will listen on - Heroku code - different than localhost code
app.listen(3000, function() {
    console.log('Example app listening on port 3000!')
})
