package account_information;

import java.util.LinkedList;

import com.google.gson.Gson;


public class StringifiableAccount {

	final private String username;
	private LinkedList<StringifiableChat> stringifiableChats;

	public StringifiableAccount(Account account) {
		this.username = account.getUsername();
		this.stringifiableChats = new LinkedList<StringifiableChat>();
		
		for (Chat chat: account.getChats()) {
			StringifiableChat stringifiableChat = new StringifiableChat(chat);
			stringifiableChats.add(stringifiableChat);
		}
	}
	
	public String toJson() {
		Gson gson = new Gson();
		String stringifiableAccountJson = gson.toJson(this);
		//System.out.println(accountJson);
		return stringifiableAccountJson;
	}

	public String getUsername() {
		return username;
	}	

	public LinkedList<StringifiableChat> getStringifiableChats() {
		return stringifiableChats;
	}


	public void setStringifiableChats(LinkedList<StringifiableChat> stringifiableChats) {
		this.stringifiableChats = stringifiableChats;
	}
	
}
