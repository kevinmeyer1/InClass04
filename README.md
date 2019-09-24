# InClass03 Android App with API and Database

This project includes the API and Android app needed to complete the requirements of InClass03.

The API was create in Node.js and uses Express for the routing. The data is stored on an Amazon AWS MySQL database.

The API is hosted on Heroku and can be accessed with a web address like: `https://inclass03.herokuapp.com/login`. This address specifically access the login route of the API.

## Routes

```/login```:

    https://inclass03.herokuapp.com/login

The login route takes in a username and password in a json object in the request body. The user name and password are verified to see if they exist in the database. If they do exist, the rest of the users information (username, name, age, weight, address) is grabbed from the database and sent back to login route. The route then creates a JWT token using the returned data as the payload. The JWT token is sent back to the app.

If authentication fails, a 'failure' response is sent back to the app and the app then handles that.

The payload of a JWT looks like this:

    {
        "username": "kevin",
        "name": "Kevin Meyer",
        "age": "23",
        "weight": "200",
        "address": "100 Main Street"
    }

```/signup```:

    https://inclass03.herokuapp.com/signup

The signup route takes in a username, password, name, age, weight, and address in a json object in the request body. A query is sent to the database to create a new row with the given information. Username and password are the only required attributes so it is possible to the user to not supply name, age, weight, and address.

Once the user is created, a 'success' response is sent back to the app. The app then sends the user back to the login page with a message saying that the account has been created successfully. The user does not immediately get sent to their profile page once they create an account so there is no need to create or verify a JWT token in this route

```/profile```:

    https://inclass03.herokuapp.com/profile

The profile route takes in only a JWT token as a json object in the request body. The JWT token is verified in the route. If it passes, 'success' is returned in the response. If it fails, 'failure' is returned and the app will then force the user back to the login page since they are not authenticated. This really shouldn't happen through the app, the only real option for this would be if someone tried to access the API through Postman or something.

```/update_profile```:

    https://inclass03.herokuapp.com/update_profile

The update_profile route takes in the JWT token, username, name, age, weight, and address as a json object in the request body.

First, the route attempts to verify the JWT token sent. If the user is validated, an update query is sent to the database that will update the current users information. A new json object is then created with the users new information. The json is then used to create a new JWT token with an updated payload containing the user information. The token is sent back to the app.

If the user is not verified, 'failure' is sent back as the response and the app will handle it by forcing the user back to the login page since the user is not authenticated.

## Database Schema

The MySQL database on Amazon AWS is pretty basic.

| username | password | name | age | weight | address |
|----------|----------|------|-----|--------|---------|

All columns are strings. Username and password are the only required values, the other can be left blank if the user wants to.

## Assumptions Required to Provide Authentication

The only things required for authentication are a valid username and password that can successfully be found within the MySQL database

## Data Stored on Device

The only data stored on the device is the JWT token that is returned from the API.
