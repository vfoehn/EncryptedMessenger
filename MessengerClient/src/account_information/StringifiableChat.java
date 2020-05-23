package account_information;

import java.text.SimpleDateFormat;
import java.util.LinkedList;

import cryptography.AsymmetricEncryption;

public class StringifiableChat {

    private String username;
    private String publicKeyString;
    private String mostRecentlyFetchedString;
    private LinkedList<StringifiableMessage> stringifiableMessages;

    public StringifiableChat(Chat chat) {
        this.username = chat.getUsername();
        this.publicKeyString = AsymmetricEncryption.keyToString(chat.getPublicKey());
        this.mostRecentlyFetchedString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS").
                format(chat.getMostRecentlyFetched());
        this.stringifiableMessages = new LinkedList<StringifiableMessage>();

        for (Message message : chat.getMessages()) {
            StringifiableMessage stringifiableMessage = new StringifiableMessage(message);
            stringifiableMessages.add(stringifiableMessage);
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPublicKeyString() {
        return publicKeyString;
    }

    public void setPublicKeyString(String publicKeyString) {
        this.publicKeyString = publicKeyString;
    }

    public String getMostRecentlyFetchedString() {
        return mostRecentlyFetchedString;
    }

    public void setMostRecentlyFetchedString(String mostRecentlyFetchedString) {
        this.mostRecentlyFetchedString = mostRecentlyFetchedString;
    }

    public LinkedList<StringifiableMessage> getStringifiableMessages() {
        return stringifiableMessages;
    }

    public void setStringifiableMessages(LinkedList<StringifiableMessage> stringifiableMessages) {
        this.stringifiableMessages = stringifiableMessages;
    }

}
