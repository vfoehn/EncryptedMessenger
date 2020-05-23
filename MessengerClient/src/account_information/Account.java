package account_information;

import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.LinkedList;

import com.google.gson.Gson;

// Account contains all the information about a messenger account. This includes the user's
// cryptographic values as well as transitively all the messages sent and received by the account.
public class Account {

    final private String username;
    final private String password;
    private PrivateKey privateKey;
    private Certificate certificate;
    private LinkedList<Chat> chats;

    public Account(String username, String password, KeystorePair keystorePair) {
        this.username = username;
        this.password = password;
        this.privateKey = keystorePair.getPrivateKey();
        this.certificate = keystorePair.getCertificate();
        this.chats = new LinkedList<Chat>();
    }

    public Account(StringifiableAccount stringifiableAccount, String password, PrivateKey privateKey,
                   Certificate certificate) throws Exception {
        this.username = stringifiableAccount.getUsername();
        this.password = password;
        this.chats = new LinkedList<Chat>();

        for (StringifiableChat stringifiableChat : stringifiableAccount.getStringifiableChats()) {
            Chat chat = new Chat(stringifiableChat);
            chats.add(chat);
        }

        this.privateKey = privateKey;
        this.certificate = certificate;
    }

    public String chatToString(String remoteUsername) throws Exception {
        System.out.println(toJson());
        Chat chat = getChat(remoteUsername);
        chat.getMessages();

        return getChat(remoteUsername).chatToString(this.username, remoteUsername);
    }


    public Chat getChat(String remoteUsername) throws Exception {
        for (Chat chat : chats) {
            if (remoteUsername.equals(chat.getUsername()))
                return chat;
        }
        System.out.println("reached null part");
        return null;
    }

    public String toJson() {
        Gson gson = new Gson();
        String accountJson = gson.toJson(this);
        //System.out.println(accountJson);
        return accountJson;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public Certificate getCertificate() {
        return certificate;
    }

    public void setCertificate(Certificate certificate) {
        this.certificate = certificate;
    }

    public LinkedList<Chat> getChats() {
        return chats;
    }

    public void addChat(Chat chat) {
        chats.add(chat);
    }

}
