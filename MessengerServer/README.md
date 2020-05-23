# MessengerServer

This directory includes the source code that runs server-side. MessengerServer consists of two 
servers:
1) HTTP server (port 8080)
2) MySQL server (port 3306)

The job of the server usually looks like the following:
1) Receive an HTTP request from a client.
2) Check if the username and passwords correspond to an account stored in the database.
3) Fetch the information requested by the client from the database.
4) Send the response to the HTTP request back to the client.

This requires the server to store account information (usernames, hashed passwords, public keys
 etc.) as well as all the messages that have been sent thus far.
