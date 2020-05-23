package account_information;

import java.security.PublicKey;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;

import cryptography.AsymmetricEncryption;

//
public class Chat {
	
	private String username;
	private PublicKey publicKey;
	private Timestamp mostRecentlyFetched; 
	private LinkedList<Message> messages;	
	
	public Chat(String username, PublicKey publicKey, LinkedList<Message> messages) {
		this.username = username;
		this.publicKey = publicKey;
		this.messages = messages;
		this.mostRecentlyFetched = new Timestamp(0); // Set the time to the year 1970
		this.publicKey = publicKey;
	}
	
	public Chat(StringifiableChat stringifiableChat) throws Exception {
		this.username = stringifiableChat.getUsername();
		this.publicKey = (PublicKey) AsymmetricEncryption.stringToKey(stringifiableChat.getPublicKeyString());
		this.mostRecentlyFetched = new Timestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS").
				parse(stringifiableChat.getMostRecentlyFetchedString()).getTime());
		this.messages = new LinkedList<Message>();
		
		for (StringifiableMessage stringifiableMessage: stringifiableChat.getStringifiableMessages()) {
			Message message = new Message(stringifiableMessage);
			messages.add(message);
		}
	}

	// RemoteUser contains information about other users that we are chatting with.
	public String chatToString(String localUsername, String remoteUsername) {
		orderMessagesChronologically();
		
		String offset = "\t\t";
		String chatString = remoteUsername + offset + localUsername;
		chatString += "\n-------------------------------\n";
		for (Message message: messages) {
			String messageBubble = "";
			
			// The messageBubbles for the local user are shifted to the right so that it is
			// easier to see which message was sent by whom.
			if (message.getSender().equals(localUsername))
				messageBubble += offset;
			
			messageBubble += "[" + message.getSendTime().toString() + ", " +
					String.valueOf(message.getSendTime().getTimezoneOffset()) + "] ";
			
			messageBubble += message.getContent() + "\n";
			
			chatString += messageBubble;
		}
		
		return chatString;
	}
	
	/*
	 * It is possible that we have undesirable interleavings between two users of the same chat.
	 * Here is an example:
	 * 		User A sends a message.
	 * 								User B sends a message.
	 * 								User B fetches the message sent by user A.
	 * To user B it will look like user A's message was sent after user B's message, even though
	 * that is not the case.
	 * 
	 * Here we can order the messages chronologically, but this is also not always desirable.
	 */
	public void orderMessagesChronologically() {
		Collections.sort(messages);
	}

	public LinkedList<Message> getMessages() {
		return messages;
	}

	public void addMessage(Message message) {
		messages.add(message);
	}
	
	public boolean containsMessage(Message message) {
		return messages.contains(message);
	}

	public Timestamp getMostRecentlyFetched() {
		return mostRecentlyFetched;
	}

	public void setMostRecentlyFetched(Timestamp mostRecentlyFetched) {
		this.mostRecentlyFetched = mostRecentlyFetched;
	}

	public String getUsername() {
		return username;
	}

	public PublicKey getPublicKey() {
		return publicKey;
	}

}
