use messenger_server;  
CREATE TABLE accounts (
    id int AUTO_INCREMENT,
    username varchar(255) NOT NULL,
    password_hash varchar(255) NOT NULL,
    public_key varchar(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE chats (
    id INT AUTO_INCREMENT,
    party_a_id INT NOT NULL,
    party_b_id INT NOT NULL, 
    PRIMARY KEY (id),
    FOREIGN KEY (party_a_id) REFERENCES accounts(id),
    FOREIGN KEY (party_b_id) REFERENCES accounts(id)
);

CREATE TABLE messages (
    id INT AUTO_INCREMENT,    
    sender VARCHAR(255) NOT NULL,    
    receiver VARCHAR(255) NOT NULL,    
    chat_id INT NOT NULL,
    send_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,    
    content VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (chat_id) REFERENCES chats(id)
);
