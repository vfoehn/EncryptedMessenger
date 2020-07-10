# EncryptedMessenger 

### Structure
EncryptedMessenger consists of two parts:
1) MessengerClient: Command line interface for logging into accounts, sending and receiving
messages.
2) MessengerServer: Server that can be run using Apache Tomcat and MySQL. The server is used for 
exchanging messages and keys.

### Goals of Project
Originally, I merely wanted to take a take a look at the Java cryptography libraries. Once I had
done this, I sent encrypted messages from port to port on localhost.\
The project rapidly grew into something bigger than intended. In the end I had a messenger server 
running on a Raspberry Pi that allowed messenger clients to communicate.


### Technological Aspects Covered
- Security (in Java):
    - RSA encryption
    - Password-based encryption
    - KeyStore
- MySQL
- Apache Tomcat


### Dependencies
- Gson (https://github.com/google/gson)
- Bouncy Castle (https://www.bouncycastle.org/)

Establish connection to MySQL server in Java using JDBC:\
https://www.javatpoint.com/example-to-connect-to-the-mysql-database


### Future Work
I worked on this project during Winter break. Once the semester started, I stopped working. Thus,
there are some aspects that are still undesirable:
- GUI version using JavaFX was never completed.
- Messages are encrypted using asymmetric encryption (RSA) which is slow. Ideally, 
EncryptedMessenger should use symmetric encryption instead. RSA could still be used as a means of 
exchanging keys or authentication.
- Currently, the messages cannot exceed a certain length due to the RSA message length limit. 
- Add salt to the hashed passwords on the server.
