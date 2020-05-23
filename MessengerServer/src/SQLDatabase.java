import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.LinkedList;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/*
 * There needs to be a MySQL server running on port 3306. This server is responsible for storing
 * the encrypted messages. That is, the sender sends a message to the server which can be 
 * fetched by the receiver.
 * Additionally, the SQL database is used for exchanging public keys between different users.
 * 
 * The database consists of three tables: accounts, chats and messages.
 *  - accounts: (id, username, password_hash, public_key)
 *  - chats: (id, party_a_id, party_b_id)
 *  - messages: (id, chat_id, send_time, content)
 * To learn more about the SQL database tables see create_messenger_tables.sql.
 * 
 * At the moment, the server and tables are not instantiated by the program. They have to be 
 * created manually before running the program..
 */

// SQLDatabase constructs and sends the queries to the data base.
public class SQLDatabase {

	private final String SQL_SERVER_URL = "jdbc:mysql://localhost:3306/messenger_server";
	private final String TIME_ZONE_PARAMETERS = "?useUnicode=true&" + 
			"useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=CET";
	private final String SQL_SERVER_USERNAME = "root";
	private final String SQL_SERVER_PASSWORD = "!e79r&#JpwJFvZ";
	
	public SQLDatabase() throws SQLException {
		
	}
	
	public void getAccounts() {		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection connection = DriverManager.getConnection(SQL_SERVER_URL +
							TIME_ZONE_PARAMETERS, SQL_SERVER_USERNAME, SQL_SERVER_PASSWORD);
			
			String queryString = "SELECT * FROM accounts";
			PreparedStatement statement = connection.prepareStatement(queryString);
			ResultSet result = statement.executeQuery();
			
			while(result.next())
				System.out.println(accountResultSetToString(result));
			
			connection.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public boolean addAccount(JsonObject accountJson) {
		String username = accountJson.get("username").getAsString();
		String password = accountJson.get("password").getAsString();
		String publicKey = accountJson.get("public_key").getAsString();
		
		String passwordHash = PasswordAuthentication.hash(password);
		
		try {			
			if (getAccountId(username) != -1)
				return false;
			
			Connection connection = DriverManager.getConnection(SQL_SERVER_URL +
							TIME_ZONE_PARAMETERS, SQL_SERVER_USERNAME, SQL_SERVER_PASSWORD);
			
			String updateString = "INSERT INTO accounts(username, password_hash, public_key) " +
								"VALUES(?, ?, ?)";
			
			//System.out.println(updateString);
			PreparedStatement statement = connection.prepareStatement(updateString);
			statement.setString(1, username);
			statement.setString(2, passwordHash);
			statement.setString(3, publicKey);
			statement.executeUpdate();

			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return true;
	}
	
	public boolean addChat(String sender, String receiver) {
		int senderId = getAccountId(sender);
		int receiverId = getAccountId(receiver);
		if (senderId == -1 || receiverId == -1)
			return false;
		
		try {
			Connection connection = DriverManager.getConnection(SQL_SERVER_URL +
							TIME_ZONE_PARAMETERS, SQL_SERVER_USERNAME, SQL_SERVER_PASSWORD);
			
			String updateString = "INSERT INTO chats(party_a_id, party_b_id) " +
								"VALUES(?, ?)";
			
			//System.out.println(updateString);
			PreparedStatement statement = connection.prepareStatement(updateString);
			statement.setInt(1, senderId);
			statement.setInt(2, receiverId);
			statement.executeUpdate();
			
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return true;
	}
	
	public boolean addMessage(JsonObject accountJson) {
		String sender = accountJson.get("username").getAsString();
		String receiver = accountJson.get("receiver").getAsString();
		String content = accountJson.get("content").getAsString();
				
		// We cannot add the message to the database if either the sender or the receiver usernames
		// are do not exist in the database.
		if (getAccountId(sender) == -1 || getAccountId(receiver) == -1)
			return false;

		int chatId = getChatId(sender, receiver);
		System.out.println(chatId);
		if (chatId == -1) {
			addChat(sender, receiver);	
			chatId = getChatId(sender, receiver);
		}
		
		try {
			Connection connection = DriverManager.getConnection(SQL_SERVER_URL +
							TIME_ZONE_PARAMETERS, SQL_SERVER_USERNAME, SQL_SERVER_PASSWORD);
			
			String updateString = "INSERT INTO messages(chat_id, sender, receiver, content) " +
								"VALUES(?, ?, ?, ?)";
			
			//System.out.println(updateString);
			PreparedStatement statement = connection.prepareStatement(updateString);
			statement.setInt(1, chatId);
			statement.setString(2, sender);
			statement.setString(3, receiver);
			statement.setString(4, content);
			statement.executeUpdate();
			
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	
		return true;
	}
	
	/*
	 * Returns the messages that are stored at the server which were sent by remoteUsername and 
	 * are addressed to username. It only returns the messages that were sent after 
	 * mostRecentlyFetched (which is an SQL timestamp as a String).
	 */
	public boolean getNewMessages(String username, String remoteUsername, String mostRecentlyFetched, 
			LinkedList<Message> newMessages) {
		// Check if there exists a chat between username and remoteUsername
		int chatId = getChatId(remoteUsername, username);
		if (chatId == -1)
			return false;
		
		try {
			Connection connection = DriverManager.getConnection(SQL_SERVER_URL +
							TIME_ZONE_PARAMETERS, SQL_SERVER_USERNAME, SQL_SERVER_PASSWORD);
			
			String queryString = "SELECT send_time, content FROM messages " +
								"WHERE receiver = ? AND " +
								"sender = ? " +
								"ORDER BY send_time ASC";	
			
			//System.out.println(queryString);
			PreparedStatement statement = connection.prepareStatement(queryString);
			statement.setString(1, username);
			statement.setString(2, remoteUsername);
			
			ResultSet result = statement.executeQuery();
			
			// There is at least one entry with for the given username.
			while (result.next()) {
				Timestamp sendTime = result.getTimestamp(1);
				String content = result.getString(2);
				newMessages.add(new Message(sendTime, content));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return true;
	}
	
	public int getAccountId(String username) {
		try {
			Connection connection = DriverManager.getConnection(SQL_SERVER_URL +
							TIME_ZONE_PARAMETERS,
					SQL_SERVER_USERNAME, SQL_SERVER_PASSWORD);
			
			String queryString = "SELECT id FROM accounts " +
								"WHERE username = ?";
			
			//System.out.println(queryString);
			PreparedStatement statement = connection.prepareStatement(queryString);
			statement.setString(1, username);
			ResultSet result = statement.executeQuery();
			
			// There is at least one entry with for the given username.
			if (result.next())
				return result.getInt(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return -1;
	}	
	
	public int getChatId(String sender, String receiver) {
		try {
			Connection connection = DriverManager.getConnection(SQL_SERVER_URL + TIME_ZONE_PARAMETERS, 
					SQL_SERVER_USERNAME, SQL_SERVER_PASSWORD);
			
			String queryString = "SELECT chats.id FROM chats " +  
					"INNER JOIN accounts AS accounts_1 ON chats.party_a_id = accounts_1.id " +
					"INNER JOIN accounts AS accounts_2 ON chats.party_b_id = accounts_2.id " + 
					"WHERE " +
						"accounts_1.username = ? AND accounts_2.username = ? OR " + 
						"accounts_1.username = ? AND accounts_2.username = ?";
			
			//System.out.println(queryString);
			PreparedStatement statement = connection.prepareStatement(queryString);
			statement.setString(1, sender);
			statement.setString(2, receiver);
			statement.setString(3, receiver);
			statement.setString(4, sender);
			
			ResultSet result = statement.executeQuery();
			
			// There is at least one entry with for the given username.
			if (result.next()) {
				return result.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	public String getPublicKey(String username) {
		String keyString = "NOT_FOUND";
		try {
			Connection connection = DriverManager.getConnection(SQL_SERVER_URL +
							TIME_ZONE_PARAMETERS, SQL_SERVER_USERNAME, SQL_SERVER_PASSWORD);
			
			String queryString = "SELECT public_key FROM accounts " +
								"WHERE username = ?";
			
			//System.out.println(queryString);
			PreparedStatement statement = connection.prepareStatement(queryString);
			statement.setString(1, username);
			
			ResultSet result = statement.executeQuery();
			
			if (result.next())
				keyString = result.getString(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return keyString;
	}
		
	/*
	 * Retrieves the hashed password from the database based on a provided username. If there exists
	 * no account with the username, the method returns "NOT_FOUND".
	 * Note: Since the passwords are hashed to a fixed length, it is impossible for a hashed password to be 
	 * the exact String "NOT_FOUND".
	 */
	public String getPasswordHash(String username) {
		String passwordHash = "NOT_FOUND";
		try {
			Connection connection = DriverManager.getConnection(SQL_SERVER_URL +
							TIME_ZONE_PARAMETERS, SQL_SERVER_USERNAME, SQL_SERVER_PASSWORD);
			
			String queryString = "SELECT password_hash FROM accounts " +
								"WHERE username = ?";
			
			//System.out.println(queryString);
			PreparedStatement statement = connection.prepareStatement(queryString);
			statement.setString(1, username);
			
			ResultSet result = statement.executeQuery();
			
			if (result.next())
				passwordHash = result.getString(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return passwordHash;
	}
	
	public String accountResultSetToString(ResultSet result) throws SQLException {
		return result.getInt(1) + "\t" + result.getString(2) + "\t" + result.getString(3) +
				"\t" + result.getString(4);
	}

}