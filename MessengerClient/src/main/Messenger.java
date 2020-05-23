package main;

import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import account_information.*;
import cryptography.AccountInformationIO;
import cryptography.AsymmetricEncryption;


public class Messenger {

    private final String ACCOUNT_FILE_NAME = "account_information.txt";
    private final String MESSENGER_URL = "http://localhost:8080/MessengerServer/Server";
    // Different types of messages
    private final String REGISTER_ACCOUNT_STRING = "REGISTER_ACCOUNT";
    private final String AUTHENTICATE_ACCOUNT_STRING = "AUTHENTICATE_ACCOUNT";
    private final String FETCH_KEY_STRING = "FETCH_KEY";
    private final String SEND_MESSAGE_STRING = "SEND_MESSAGE";
    private final String FETCH_MESSAGES_STRING = "FETCH_MESSAGES";
    public Account account;

    public Messenger() throws Exception {}

    // Asks the server to authenticate the account username and password.
    public boolean authenticateAccount(String username, String password) throws Exception {
        JsonObject authenticationRequest = new JsonObject();
        authenticationRequest.addProperty("type", AUTHENTICATE_ACCOUNT_STRING);
        authenticationRequest.addProperty("username", username);
        authenticationRequest.addProperty("password", password);

        String authenticateResponse = HTTPUtil.executePost(MESSENGER_URL, authenticationRequest.toString());
        JsonObject authenticateResponseJson = new JsonParser().parse(authenticateResponse).getAsJsonObject();
        String status = authenticateResponseJson.get("status").getAsString();

        return status.equals("successful");
    }

    // Loads the state of the account from a file when the program is first launched.
    public void loadAccount(String username, String password) throws Exception {
        // Load private key and certificate from the key store.
        KeystorePair keystorePair = AsymmetricEncryption.readFromKeyStore(username, password);
        PrivateKey privateKey = keystorePair.getPrivateKey();
        Certificate certificate = keystorePair.getCertificate();

        // Load other account information from the encrypted file.
        String filePath = username + "\\" + ACCOUNT_FILE_NAME;
        String accountInformation = AccountInformationIO.readAndDecrypt(filePath, password);

        Gson gson = new Gson();
        StringifiableAccount stringifiableAccount = gson.fromJson(accountInformation,
                StringifiableAccount.class);

        account = new Account(stringifiableAccount, password, privateKey, certificate);
        System.out.println(account.toJson());
    }


    // Registers a new account at the server. The account must have a unique username, a password
    // and a public key.
    public boolean registerAccountAtServer(String username, String password) throws Exception {
        KeystorePair keystorePair = AsymmetricEncryption.generateSelfSignedX509Certificate();
        account = new Account(username, password, keystorePair);

        JsonObject registerRequest = new JsonObject();
        registerRequest.addProperty("type", REGISTER_ACCOUNT_STRING);
        registerRequest.addProperty("username", account.getUsername());
        registerRequest.addProperty("password", password);
        String publicKeyString = AsymmetricEncryption.keyToString(account.getCertificate().getPublicKey());
        registerRequest.addProperty("public_key", publicKeyString);

        String registerResponse = HTTPUtil.executePost(MESSENGER_URL, registerRequest.toString());
        JsonObject registerResponseJson = new JsonParser().parse(registerResponse).getAsJsonObject();
        String status = registerResponseJson.get("status").getAsString();

        return status.equals("successful");
    }

    // Fetches the public key of a different user at the server.
    public PublicKey fetchPublicKeyAtServer(String remoteUsername) throws Exception {
        JsonObject registerRequest = new JsonObject();
        registerRequest.addProperty("type", FETCH_KEY_STRING);
        registerRequest.addProperty("username", account.getUsername());
        registerRequest.addProperty("password", account.getPassword());
        registerRequest.addProperty("remote_username", remoteUsername);

        String response = HTTPUtil.executePost(MESSENGER_URL, registerRequest.toString());
        JsonObject responseJson = new JsonParser().parse(response).getAsJsonObject();
        String status = responseJson.get("status").getAsString();

        if (!status.equals("successful")) {
            System.out.println("Public key of " + remoteUsername + " could not be fetched "
                    + "successfully. Perhaps there exists no user with that username.");
            return null;
        }

        String keyString = responseJson.get("public_key").getAsString();
        System.out.println(keyString);
        PublicKey remotePublicKey = (PublicKey) AsymmetricEncryption.stringToKey(keyString);
        System.out.println(remoteUsername);
        //account.getPublicKeys().add(new BoundPublicKey(remoteUsername, remotePublicKey));

        return remotePublicKey;
    }

    /*
     * Sends a message to the server which can be fetched by the receiver.
     */
    public boolean sendMessage(String receiver, String message) throws Exception {
        Chat chatWithReceiver = account.getChat(receiver);
        // There exists no chat with the given receiver. Therefore, we need to create one.
        if (chatWithReceiver == null) {
            PublicKey remotePublicKey = fetchPublicKeyAtServer(receiver);
            if (remotePublicKey == null)
                return false;
            chatWithReceiver = new Chat(receiver, remotePublicKey, new LinkedList<Message>());
            account.addChat(chatWithReceiver);
        }

        PublicKey receiverPublicKey = chatWithReceiver.getPublicKey();
        String encryptedMessage = AsymmetricEncryption.encrypt(receiverPublicKey, message);
        System.out.println(encryptedMessage);

        JsonObject registerRequest = new JsonObject();
        registerRequest.addProperty("type", SEND_MESSAGE_STRING);
        registerRequest.addProperty("username", account.getUsername());
        registerRequest.addProperty("password", account.getPassword());
        registerRequest.addProperty("receiver", receiver);
        registerRequest.addProperty("content", encryptedMessage);
        System.out.println(registerRequest.toString());

        String response = HTTPUtil.executePost(MESSENGER_URL, registerRequest.toString());
        JsonObject responseJson = new JsonParser().parse(response).getAsJsonObject();
        String status = responseJson.get("status").getAsString();
        if (!status.equals("successful")) {
            System.out.println("Sending messages was unsuccessful.");
            return false;
        }
        Timestamp sendTime = new Timestamp(System.currentTimeMillis());
        chatWithReceiver.addMessage(new Message(account.getUsername(), sendTime, message));
        writeAccountInformationToFile();

        return true;
    }

    /*
     * Fetches the messages that have been sent by remoteUserName and are currently stored
     * at the server.
     */
    public boolean fetchMessages(String remoteUsername) throws Exception {
        JsonObject registerRequest = new JsonObject();
        registerRequest.addProperty("type", FETCH_MESSAGES_STRING);
        registerRequest.addProperty("username", account.getUsername());
        registerRequest.addProperty("password", account.getPassword());
        registerRequest.addProperty("remote_username", remoteUsername);

        Chat chatWithRemoteUser = account.getChat(remoteUsername);
        // There exists no chat with the given remoteUsername. Therefore, we need to create one.
        if (chatWithRemoteUser == null) {
            PublicKey remotePublicKey = fetchPublicKeyAtServer(remoteUsername);
            if (remotePublicKey == null)
                return false;
            chatWithRemoteUser = new Chat(remoteUsername, remotePublicKey, new LinkedList<Message>());
            account.addChat(chatWithRemoteUser);
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");
        String mostRecentlyFetchedString = simpleDateFormat.format(chatWithRemoteUser.getMostRecentlyFetched());
        registerRequest.addProperty("most_recently_fetched", mostRecentlyFetchedString);

        String response = HTTPUtil.executePost(MESSENGER_URL, registerRequest.toString());
        JsonObject responseJson = new JsonParser().parse(response).getAsJsonObject();
        System.out.println(responseJson.toString());
        String status = responseJson.get("status").getAsString();
        if (!status.equals("successful")) {
            System.out.println("Fetching messages was unsuccessful.");
            return false;
        }

        Timestamp timestampNow = new Timestamp(Calendar.getInstance().getTimeInMillis());
        chatWithRemoteUser.setMostRecentlyFetched(timestampNow);
        int numberOfMessages = responseJson.get("number_of_messages").getAsInt();
        JsonArray fetchedMessages = responseJson.getAsJsonArray("messages");

        for (int i = 0; i < numberOfMessages; i++) {
            JsonObject fetchedMessageJson = fetchedMessages.get(i).getAsJsonObject();
            String sendTimeString = fetchedMessageJson.get("sendTimeString").getAsString();
            String encryptedContent = fetchedMessageJson.get("content").getAsString();

            Timestamp sendTime = new Timestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS").parse(sendTimeString).getTime());
            String decryptedContent = AsymmetricEncryption.decrypt(account.getPrivateKey(), encryptedContent);

            Message message = new Message(remoteUsername, sendTime, decryptedContent);
            if (!chatWithRemoteUser.containsMessage(message)) {
                chatWithRemoteUser.addMessage(message);
                System.out.println(decryptedContent);
            }
        }
        writeAccountInformationToFile();

        return true;
    }

    public void printChat(String username) throws Exception {
        System.out.println(account.chatToString(username));
    }

    /*
     * Writes the most up-to-date information regarding the state of the account to a file.
     */
    public void writeAccountInformationToFile() throws Exception {
        // Create a new directory for the user where we store the account information files.
        if (!new File(account.getUsername()).exists()) {
            new File(account.getUsername()).mkdirs();
        }

        // Store account information (which includes all the chats).
        StringifiableAccount stringifiableAccount = new StringifiableAccount(account);
        PublicKey publicKey = account.getCertificate().getPublicKey();
        String accountInformation = stringifiableAccount.toJson();
        String filePath = account.getUsername() + "\\" + ACCOUNT_FILE_NAME;
        AccountInformationIO.encryptAndWrite(accountInformation, filePath, account.getPassword());

        // Store private key and certificate.
        AsymmetricEncryption.writeToKeyStore(account.getPrivateKey(), account.getCertificate(),
                account.getUsername(), account.getPassword());
    }

}
