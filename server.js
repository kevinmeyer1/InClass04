var mysql = require('mysql')
var bodyParser = require('body-parser')
var jwt = require('jsonwebtoken')
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
const stripe = require("stripe")(config['secretKey']);

//Heroku code to set the listening port
const PORT = process.env.PORT || 3000

// ------------------------------------------------- CONNECTIONS  -------------------------------------------------

var con = mysql.createConnection({
    host: mySQLHost,
    user: mySQLUsername,
    password: mySQLPassword,
    database: mySQLDatabase
})

// ------------------------------------------------- FUNCTIONS -------------------------------------------------

var createCustomer = function(username, callback) {
    console.log('creating customer')

    stripe.customers.create({
        name: `${username}`
    }, function(err, customer) {
        if (err) {
            console.log('err creating customer in stripe')
            console.log(err)
            callback(null)
        } else {
            var createCustomerQuery = `INSERT INTO customers (username, id) VALUES ("${username}", "${customer['id']}")`

            con.query(createCustomerQuery, function(err, result) {
                if (err) {
                    callback(false)
                } else {
                    callback(true)
                }
            })
        }
    });
}

var foundCustomer = function(username, callback) {
    var customerQuery = `SELECT username, id FROM customers WHERE username="${username}"`

    con.query(customerQuery, function(err, result) {
        if (err) {
            console.log(err)
            callback(null)
        } else {
            if (result.length == 1) {
                callback(true)
            }  else {
                callback(false)
            }
        }
    })
}

var findCustomer = function(username, callback) {
    var customerQuery = `SELECT id FROM customers WHERE username="${username}"`
    con.query(customerQuery, function(err, result) {
        if (err) {
            console.log('there was an error finding the customer id')
            callback(null)
        } else {
            if (result.length == 1) {
                callback(result[0]['id'])
            }
        }
    })
}

// ------------------------------------------------- ROUTES -------------------------------------------------

app.use(bodyParser.json())

//Adds a credit card to a users account. App calls this when checking out, followed by /charge
app.post('/addCard', function(req, res) {
    var token = req.body.token
    var cardToken = req.body.cardToken

    jwt.verify(token, jwtSecret, function(err, decodedPayload) {
        if (err) {
            console.log('JWT token not verified, user is not authenticated.')
            res.status(401)
            res.setHeader('Content-Type', 'text/plain')
            res.write('JWT verification failed')
            res.send()
        } else {
            var username = decodedPayload['username']
            findCustomer(username, function(customerId) {

                stripe.customers.createSource(
                    customerId,
                    {
                        source: cardToken
                    },
                    function (err, card) {
                        if (err) {
                            console.log(err)
                            res.status(400)
                            res.write('Error while adding card')
                            res.send()
                        } else {
                            res.status(200)
                            res.setHeader('Content-Type', 'text/plain')
                            res.write(`Card added to users account with id - ${card['id']}`)
                            res.send()
                        }
                    }
                )
            })
        }
    })
})

//Create a charge for a user, uses the credit card given to the app from /addCard
app.post('/charge', function(req, res) {
    var token = req.body.token
    var chargeAmount = req.body.chargeAmount

    //Charge amount comes in as a value like 7.89 - needs to be 7890
    chargeAmount = chargeAmount * 100
    //removing decimals
    chargeAmount = chargeAmount.toFixed(0)

    jwt.verify(token, jwtSecret, function(err, decodedPayload) {
        if (err) {
            console.log('JWT token not verified, user is not authenticated.')
            res.status(401)
            res.setHeader('Content-Type', 'text/plain')
            res.write('JWT verification failed')
            res.send()
        } else {
            var username = decodedPayload['username']
            findCustomer(username, function(customerId) {
                console.log(customerId)

                stripe.charges.create({
                    amount: chargeAmount,
                    currency: 'usd',
                    description: 'Purchase from InClass04 shopping application',
                    customer: customerId
                }, function (err, something) {
                    if (err) {
                        console.log(err)
                        res.status(400)
                        res.write('Error while creating charge')
                        res.send()
                    } else {
                        res.status(200)
                        res.setHeader('Content-Type', 'text/plain')
                        res.write('Charge has been successfully create - check stripe')
                        res.send()
                    }
                })
            })
        }
    })
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
                res.status(500)
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
            foundCustomer(username, function (foundCustomerResult) {
                if (foundCustomerResult) {
                    console.log('customer found')
                    res.status(201)
                    res.setHeader('Content-Type', 'text/plain')
                    res.write('Customer found in Stripe')
                    res.send()
                } else {
                    console.log('customer not found')
                    createCustomer(username, function(createCustomerResult) {
                        if (createCustomerResult) {
                            res.status(201)
                            res.setHeader('Content-Type', 'text/plain')
                            res.write('User created')
                            res.send()
                        } else {
                            res.status(400)
                            res.setHeader('Content-Type', 'text/plain')
                            res.write("Error while creating customer - stripe")
                            res.send()
                        }
                    })
                }
            })
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

    var oldName, oldAge, oldWeight, oldAddress;

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

/*
app.listen(3000, function() {
    console.log('Example app listening on port 3000!')
})
*/

//sets port that API will listen on - Heroku code - different than localhost code
app.listen(PORT, () => {
    console.log(`Our app is running on port ${ PORT }`);
})

/*
//Get the customer Id
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
            findCustomer(username, function(result) {
                if (result === null) {
                    console.log(err)
                    res.status(400)
                    res.write('Error while getting customer ID from database')
                    res.send()
                } else {
                    if (result.length == 1) {
                        var customerData = {
                            id: result[0]['id']
                        }

                        res.status(200)
                        res.setHeader('Content-Type', 'application/json')
                        res.json(customerData)
                    }
                }
            })
        }
    })
})
*/
