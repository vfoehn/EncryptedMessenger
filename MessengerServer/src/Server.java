import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

// Server is a web servlet that accepts POST requests from messenger clients. The server is
// connected to a MySQL database to retrieve requested data for authorized users.
@WebServlet("/Server")
public class Server extends HttpServlet {
	
	private SQLDatabase sqlDatabase;
	private static final long serialVersionUID = 1L;

	// Different types of messages
	private final String REGISTER_ACCOUNT_STRING = "REGISTER_ACCOUNT";
	private final String AUTHENTICATE_ACCOUNT_STRING = "AUTHENTICATE_ACCOUNT";
	private final String FETCH_KEY_STRING = "FETCH_KEY";
	private final String REGISTER_DEVICE_STRING = "REGISTER_DEVICE";
	private final String EXCHANGE_KEYS_STRING = "EXCHANGE_KEYS";
	private final String SEND_MESSAGE_STRING = "SEND_MESSAGE";
	private final String FETCH_MESSAGES_STRING = "FETCH_MESSAGES";
	private final String ERROR_STRING = "ERROR";

    public Server() throws SQLException {
        super();
        
        sqlDatabase = new SQLDatabase();
    }

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String requestData = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
		System.out.println(requestData);
		
		JsonObject requestDataJson = new JsonParser().parse(requestData).getAsJsonObject();
		String type = requestDataJson.get("type").getAsString();

		if(type.equals(REGISTER_ACCOUNT_STRING))
			handleRegisterAccountRequest(requestDataJson, response);
		else if(type.equals(AUTHENTICATE_ACCOUNT_STRING))
			handleAuthenticationRequest(requestDataJson, response);
		else if(type.equals(SEND_MESSAGE_STRING))
			handleSendMessageRequest(requestDataJson, response);
		else if(type.equals(FETCH_KEY_STRING))
			handleFetchKeyRequest(requestDataJson, response);
		else if(type.equals(FETCH_MESSAGES_STRING))
			handleFetchMessagesRequest(requestDataJson, response);
		else		
			handleInvalidRequest(response);
	}
	
	// Creates a new account in the SQL database if there does not already exist an account with the
	// provided username.
	private void handleRegisterAccountRequest(JsonObject requestDataJson, HttpServletResponse response)
			throws ServletException, IOException  {
		boolean successful = sqlDatabase.addAccount(requestDataJson);
        sqlDatabase.getAccounts();
        
        JsonObject registerResponse = new JsonObject();
		registerResponse.addProperty("type", REGISTER_ACCOUNT_STRING);
		if (successful)
			registerResponse.addProperty("status", "successful");
		else {
			registerResponse.addProperty("status", "unsuccessful");	
			registerResponse.addProperty("error_description", "Username is already taken.");		
		}
        
        System.out.println(registerResponse.toString());
		response.getWriter().append(registerResponse.toString());
	}	
	
	private void handleAuthenticationRequest(JsonObject requestDataJson, HttpServletResponse response)
			throws ServletException, IOException  {
		String username = requestDataJson.get("username").getAsString();
		String password = requestDataJson.get("password").getAsString();
		
		AuthenticationPair authenticationPair = checkCredentials(username, password);

		JsonObject sendAuthenticationResponse = new JsonObject();
		sendAuthenticationResponse.addProperty("type", AUTHENTICATE_ACCOUNT_STRING);
		
		if (!authenticationPair.isAuthenticated()) {
			sendAuthenticationResponse.addProperty("status", "unsuccessful");
			sendAuthenticationResponse.addProperty("error_description", authenticationPair.getErrorMessage());				
		} else {
			sendAuthenticationResponse.addProperty("status", "successful");
		}
		response.getWriter().append(sendAuthenticationResponse.toString());
	}
	
	private void handleSendMessageRequest(JsonObject requestDataJson, HttpServletResponse response)
			throws ServletException, IOException  {
		String username = requestDataJson.get("username").getAsString();
		String password = requestDataJson.get("password").getAsString();
		
		AuthenticationPair authenticationPair = checkCredentials(username, password);

		JsonObject sendMessageResponse = new JsonObject();
		sendMessageResponse.addProperty("type", SEND_MESSAGE_STRING);
		
		if (!authenticationPair.isAuthenticated()) {
			sendMessageResponse.addProperty("status", "unsuccessful");
			sendMessageResponse.addProperty("error_description", authenticationPair.getErrorMessage());			
		} else {
			boolean messageAdded = sqlDatabase.addMessage(requestDataJson);
			if (!messageAdded) {
				sendMessageResponse.addProperty("status", "unsuccessful");
				sendMessageResponse.addProperty("error_description", "There exists no user with the receiver's username");	
			} else {
				sendMessageResponse.addProperty("status", "successful");
			}						
		}
		
		response.getWriter().append(sendMessageResponse.toString());
	}
	
	private void handleFetchMessagesRequest(JsonObject requestDataJson, HttpServletResponse response)
			throws ServletException, IOException  {
		String username = requestDataJson.get("username").getAsString();
		String password = requestDataJson.get("password").getAsString();
		String remoteUsername = requestDataJson.get("remote_username").getAsString();
		String mostRecentlyFetched = requestDataJson.get("most_recently_fetched").getAsString();
		
		AuthenticationPair authenticationPair = checkCredentials(username, password);	

		JsonObject fetchMessagesResponse = new JsonObject();
		fetchMessagesResponse.addProperty("type", FETCH_MESSAGES_STRING);
		
		LinkedList<Message> newMessages = new LinkedList<Message>();
		boolean chatExists = sqlDatabase.getNewMessages(username, remoteUsername, mostRecentlyFetched, newMessages);
		
		if (!chatExists) {
			fetchMessagesResponse.addProperty("status", "unsuccessful");
			fetchMessagesResponse.addProperty("error_description", "this chat does not exist at the server.");				
		} else {
			fetchMessagesResponse.addProperty("status", "successful");
			fetchMessagesResponse.addProperty("number_of_messages", newMessages.size());
			
			Gson gson = new Gson();
			JsonArray newMessagesJson = new JsonArray();
			for (Message message: newMessages) {
				JsonElement jsonElementMessage = gson.toJsonTree(message);
				JsonObject jsonObjectMessage = (JsonObject) jsonElementMessage;
				newMessagesJson.add(jsonObjectMessage);
			}
			fetchMessagesResponse.add("messages", newMessagesJson);
		}
		
		System.out.println(fetchMessagesResponse);
		response.getWriter().append(fetchMessagesResponse.toString());
	}

	private void handleFetchKeyRequest(JsonObject requestDataJson, HttpServletResponse response)
			throws ServletException, IOException  {
		String username = requestDataJson.get("username").getAsString();
		String password = requestDataJson.get("password").getAsString();
		String remoteUsername = requestDataJson.get("remote_username").getAsString();
		
		AuthenticationPair authenticationPair = checkCredentials(username, password);	

		JsonObject sendMessageResponse = new JsonObject();
		sendMessageResponse.addProperty("type", SEND_MESSAGE_STRING);
		
		if (!authenticationPair.isAuthenticated()) {
			sendMessageResponse.addProperty("status", "unsuccessful");
			sendMessageResponse.addProperty("error_description", authenticationPair.getErrorMessage());			
		} else {
			String keyString = sqlDatabase.getPublicKey(remoteUsername);
			System.out.println(keyString);
			if (keyString.equals("NOT_FOUND")) {
				sendMessageResponse.addProperty("status", "unsuccessful");
				sendMessageResponse.addProperty("error_description",
						"The user for whom the public key was requested"
						+ "does not exist in the database.");
			} else {
				sendMessageResponse.addProperty("status", "successful");
				sendMessageResponse.addProperty("public_key", keyString);			
			}
		}
		
		response.getWriter().append(sendMessageResponse.toString());
	}
	
	private void handleInvalidRequest(HttpServletResponse response) throws ServletException, IOException  {	
		JsonObject errorResponse = new JsonObject();
		errorResponse.addProperty("type", ERROR_STRING);
		errorResponse.addProperty("error_description", "The type of the request is unknown.");
        
        System.out.println(errorResponse.toString());
		response.getWriter().append(errorResponse.toString());
	}
	
	public AuthenticationPair checkCredentials(String username, String password) {
		String truePasswordHash = sqlDatabase.getPasswordHash(username);
		if (truePasswordHash.equals("NOT_FOUND")) {
			String errorMessage = "No entry with the username found.";
			return new AuthenticationPair(false, errorMessage);
		}
		
		boolean isAuthenticated = PasswordAuthentication.authenticate(password, truePasswordHash);
		if (!isAuthenticated) {
			String errorMessage = "Incorrect password.";
			return new AuthenticationPair(false, errorMessage);
		}
		
		return new AuthenticationPair(true);
	}

}
