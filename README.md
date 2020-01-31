# InClass04 Android App with API and Database

This is a class project from Advanced Mobile Application Development - Graduate course. The goal of this project is to build ontop of a previous project that my group created. We had a functioning shopping application where users could signup, login, fill up a shopping cart, and then check out with Braintree. We were tasked to use a different API (not Braintree) to expose ourselves to new content. In the update of that project, this GitHub repository, I implemented Stripe into our mobile app to simulate real transactions.

This project includes the API and Android app needed to complete the requirements of InClass03.

The API was create in Node.js and uses Express for the routing. The data is stored on an Amazon AWS MySQL database.

The API is hosted on Heroku and can be accessed with a web address like: `https://inclass03.herokuapp.com/login`. This address specifically access the login route of the API.

## Routes

```/login```:

    https://inclass04.herokuapp.com/login

The login route takes in a username and password in a JSON object in the request body. The user name and password are verified to see if they exist in the database. If they do exist, the username is added as the payload to a JWT token. The JWT token is sent back to the app.

If authentication fails, a 'failure' response is sent back to the app and the app then handles that.

The payload of a JWT looks like this:

    {
        "username": "kevin"
    }
    
```/addCard```:
    
    https://inclass04.herokuapp.com/addCard

This route takes in a JWT token and a cardToken - a tokenized version of the users card. The server uses the JWT token to determine the identity of the user trying to add a card, and then uses the Stripe API to add the card to their profile.

```/charge```:

    https://inclass04.herokuapp.com/charge
    
This route is used once a user finally wants to complete a transaction. It takes in the JWT token of the user and the amout to be charged. The JWT identifies the user so Stripe can determine which customer to charge the amount to. The cards are all connected to the Stripe customer so we don't have to do anything with that (after the /addCard route is used - guarenteed before this route call).

```/signup```:

    https://inclass04.herokuapp.com/signup

The signup route takes in a username, password, name, age, weight, and address in a JSON object in the request body. A query is sent to the database to create a new row with the given information. Also, since this application uses Stripe, a new customer is created through their API and then kept track of in our database. Username and password are the only required attributes so it is possible to the user to not supply name, age, weight, and address.

Once the user is created, a 'success' response is sent back to the app. The app then sends the user back to the login page with a message saying that the account has been created successfully. The user does not immediately get sent to their profile page once they create an account so there is no need to create or verify a JWT token in this route.

```/profile```:

    https://inclass04.herokuapp.com/profile

The profile route takes in only a JWT token as a JSON object in the request body. The JWT token is verified in the route. If it passes, the user's data is grabbed from the database using the JWT token to identify the username of the user. All the data is put into a JSON object and returned. If the verification fails, a 401 - Unauthorized error is sent back.

The returned JSON looks like this:

    {
        "name": "kevin",
        "age": "23",
        "weight": "200",
        "address": "100 Main Street"
    }

```/update_profile```:

    https://inclass04.herokuapp.com/update_profile

The update_profile route takes in the JWT token, username, name, age, weight, and address as a JSON object in the request body.

First, the route attempts to verify the JWT token sent. If the user is validated, an update query is sent to the database that will update the current users information. A new JSON object is then created with the users new information. The JSON is then used to create a new JWT token with an updated payload containing the user information. The token is sent back to the app.

If the user is not verified, 'failure' is sent back as the response and the app will handle it by forcing the user back to the login page since the user is not authenticated.

## Database Schema

The MySQL database on Amazon AWS is pretty basic.

| username | password | name | age | weight | address |
|----------|----------|------|-----|--------|---------|

All columns are VARCHARs. Username, password, and address are all max 100 characters. Age is max 3 characters and weight is max 4 characters. Username and password are the only required values, the other can be left blank if the user wants to.

There is also another simple table to keep track of usernames and their Stripe customer ids. 

| username | customerId |
|----------|------------|

Both values are VARCHARs.

## Assumptions Required to Provide Authentication

The only things required for authentication are a valid username and password that can successfully be found within the MySQL database. This will return a JWT token for that user. If there is already a JWT token stored on the device, the user does not have to do anything for authentication.

## Data Stored on Device

The JWT token is stored on the device once a user logs in. It is stored in the apps SharedPreferences. When the user logs out, the JWT token is removed. If a user logs in while there is a JWT saved to the device, they will be brought to the profile page as that user - the user they logged in as.
